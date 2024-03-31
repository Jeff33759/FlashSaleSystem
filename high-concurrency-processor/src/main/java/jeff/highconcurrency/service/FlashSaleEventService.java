package jeff.highconcurrency.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jeff.common.consts.ResponseCode;
import jeff.common.entity.bo.MyRequestContext;
import jeff.common.entity.dto.inner.InnerCommunicationDto;
import jeff.common.util.LogUtil;
import jeff.highconcurrency.exception.FlashSaleEventConsumeException;
import jeff.highconcurrency.http.feign.client.CoreProcessorFeignClient;
import jeff.highconcurrency.mq.producer.MyReactiveMQProducer;
import jeff.highconcurrency.persistent.model.mongo.dao.ReactiveFlashSaleEventLogRepo;
import jeff.highconcurrency.persistent.model.mongo.po.ReactiveFlashSaleEventLog;
import jeff.highconcurrency.util.redis.util.MyReactiveRedisUtil;
import jeff.mq.consts.MyRabbitMQConsts;
import jeff.mq.entity.dto.MyMessagePayloadTemplate;
import jeff.mq.util.MQUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

/**
 * 所有有關快閃銷售案件的方法封裝。
 */
@Slf4j
@Service
public class FlashSaleEventService {

    @Autowired
    private MyReactiveRedisUtil myReactiveRedisUtil;

    @Autowired
    private ReactiveFlashSaleEventLogRepo reactiveFlashSaleEventLogRepo;

    @Autowired
    private MyReactiveMQProducer myReactiveMQProducer;

    @Autowired
    private CoreProcessorFeignClient coreProcessorFeignClient;

    @Autowired
    private LogUtil logUtil;

    @Autowired
    private MQUtil mqUtil;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * 消費一個快閃銷售案件。
     * 快閃銷售案件的頁面送出搶購的請求。
     *
     * TODO 目前因為還沒做認證相關的邏輯，所以下單的買家與賣家的Id都先寫死，外部就先不用傳了。
     * @param param 範例資料: {"flash_event":{"fse_id":1}
     */
    public Mono<InnerCommunicationDto> consumeFlashSaleEvent(JsonNode param, MyRequestContext reqContext) {
        int fseId = param.get("flash_event").get("fse_id").asInt(-1);

        // 以下嵌套寫法，確保了前個操作完成才做下個操作。每個操作的結果都預期只會輸出一個流(例如mongo預計最多只會查出一筆資料)，所以回傳的都是Mono。
        // 第一個操作
        return myReactiveRedisUtil.leftPopListByKeyAndGetDataObj("fse_" + fseId, ReactiveFlashSaleEventLog.class)
                .flatMap(optionalPojo -> {
                    // 處理第一個結果
                    ReactiveFlashSaleEventLog fseInfo = (ReactiveFlashSaleEventLog) optionalPojo.orElseThrow(() -> new FlashSaleEventConsumeException("Out of stock.")); //redis無資料代表已經被消費完畢

                    logUtil.logInfo( //寫log雖然是IO操作，但這裡就不寫在下一層map了，寫log阻塞的機會應該是相對少的，為此多包一層map反而增加開銷，畢竟要把redis left pop的資料帶到下一動
                            log,
                            logUtil.composeLogPrefixForBusiness(reqContext.getAuthenticatedMemberId(), reqContext.getUUID()),
                            String.format(
                                    "Successfully left pop flashSaleEvent from redis, key: %s, transNum: %s",
                                    "fse_" + fseId,
                                    fseInfo.getTransNum()
                            )
                    );

                    // 第二個操作
                    return reactiveFlashSaleEventLogRepo.selectByFseIdAndTransNumAndHasNotBeenConsumed(fseInfo.getFseId(), fseInfo.getTransNum())
                            .flatMap(mongoFseInfoToUpdate -> {
                                // 處理第二個結果
                                mongoFseInfoToUpdate.setCMId(reqContext.getAuthenticatedMemberId());
                                mongoFseInfoToUpdate.setIsConsumed(true);

                                // 第三個操作
                                return reactiveFlashSaleEventLogRepo.save(mongoFseInfoToUpdate)
                                        .flatMap(updatedFseInfo -> {
                                            // 處理第三個結果
                                            try {
                                                myReactiveMQProducer.produceMessageToBusinessExchange(
                                                        MyRabbitMQConsts.ROUTING_KEY_NAME_FOR_FLASH_SALE_EVENT_ORDER_CASE,
                                                        new MyMessagePayloadTemplate(MyRabbitMQConsts.TITLE_NAME_FOR_ORDER_GENERATION_OF_FLASH_SALE_EVENT_ORDER_CASE, objectMapper.writeValueAsString(updatedFseInfo), mqUtil.generateMsgId()),
                                                        reqContext
                                                );

                                                // 第四個操作
                                                return Mono.just(new InnerCommunicationDto(ResponseCode.Success.getCode(), objectMapper.createObjectNode().put("transNum", updatedFseInfo.getTransNum()), "Consume flashSaleEvent successfully."));
                                            } catch (JsonProcessingException e) {
                                                // 第四個操作
                                                return Mono.error(new FlashSaleEventConsumeException("Failed to publish msg to mq because failing to convert to json."));
                                            }
                                        });
                            });
                });

    }

    /**
     * 回傳快閃銷售案件的資訊，用於渲染搶購頁面的view。
     *
     * 會由high-concurrency-processor來承接請求，並做為上游Server打請求到core-processor要資料，core-processor做為下游Server提供資料。
     */
    public Mono<ResponseEntity<Mono<InnerCommunicationDto>>> getFlashSaleEventInfo(int fseId, MyRequestContext reqContext) {
        return coreProcessorFeignClient.getFlashSaleEventInfo(
                reqContext.getUUID(),
                objectMapper.createObjectNode().put("fse_id", fseId)
        );
    }

}
