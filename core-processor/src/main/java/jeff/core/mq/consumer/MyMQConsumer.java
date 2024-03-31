package jeff.core.mq.consumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jeff.common.entity.bo.MyMQConsumptionContext;
import jeff.common.interfaces.IOrderService;
import jeff.common.util.LogUtil;
import jeff.mq.consts.MyRabbitMQConsts;
import jeff.mq.entity.dto.MyMessagePayloadTemplate;
import jeff.mq.exception.MyMQException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;

import javax.annotation.Resource;
import java.util.function.Consumer;

/**
 * 集中註冊MQ的監聽者。
 */
@Slf4j
@Configuration
public class MyMQConsumer {

    @Resource(name = "flashOrderService")
    private IOrderService flashOrderService;

    @Autowired
    private LogUtil logUtil;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * 快閃銷售案件的訂單相關事件。
     * 配置請看application.yml，只會訂閱routingKey=fse.order的Queue。
     *
     * 範例payload(成立訂單事件): {"title":"generation","content":"{\"id\":\"65db8bab8ffcd84a6d4906e2\",\"fseId\":1,\"transNum\":3,\"isConsumed\":true,\"smid\":2,\"gid\":3,\"cmid\":1}","publishTimestamp":1708886969528}
     */
    @Bean(name = "coreProcessor_BusinessExchangeConsumerChannelForOrderCase")
    public Consumer<Message<String>> coreProcessor_BusinessExchangeConsumerChannelForOrderCase() {
        return message -> {
            MyMQConsumptionContext myMQConsumptionContext = new MyMQConsumptionContext().chainSetUUID(logUtil.generateUUIDForLogging()); //不可以寫在lambda之外，不然MyMQConsumptionContext會變成單例

            String payload = message.getPayload();
            String routingKey = (String) message.getHeaders().get(AmqpHeaders.RECEIVED_ROUTING_KEY);

            logUtil.logInfo(
                    log,
                    logUtil.composeLogPrefixForMQConsumer(routingKey, myMQConsumptionContext.getUUID()),
                    String.format("Receive the msg from BusinessExchangeConsumerChannelForOrderCase, payload: %s", payload)
            );

            try {
                this.routeFeatureByRoutingKey(objectMapper.readValue(payload, MyMessagePayloadTemplate.class), routingKey, myMQConsumptionContext);
            } catch (JsonProcessingException jpe) { // payload轉成MyMessagePayloadTemplate失敗
                logUtil.logError(
                        log,
                        logUtil.composeLogPrefixForMQConsumer(routingKey, myMQConsumptionContext.getUUID()),
                        "Failed to convert messagePayload to MyMessagePayloadTemplate.",
                        jpe
                );

                // MQ的應答模式設置為none，所以消費完後若做失敗，訊息就會丟失
                // 如果設定為auto，則當throw出例外時，同實例會重試2遍消費邏輯，接著走死信機制。
                // TODO 失敗的情況，有機會再設計。

            } catch (Exception e) { // 路由開始到業務邏輯的任何一層失敗
                logUtil.logError(
                        log,
                        logUtil.composeLogPrefixForMQConsumer(routingKey, myMQConsumptionContext.getUUID()),
                        e.getMessage(),
                        e
                );

                // MQ的應答模式設置為none，所以消費完後若做失敗，訊息就會丟失
                // 如果設定為auto，則當throw出例外時，同實例會重試2遍消費邏輯，接著走死信機制。
                // TODO 失敗的情況，有機會再設計。

            }
        };
    }

    /**
     * 根據routingKey去路由功能。
     */
    private void routeFeatureByRoutingKey(MyMessagePayloadTemplate myMessagePayloadTemplate, String routingKey, MyMQConsumptionContext myMQConsumptionContext) throws MyMQException {
        switch (routingKey) {
            case MyRabbitMQConsts.ROUTING_KEY_NAME_FOR_FLASH_SALE_EVENT_ORDER_CASE:
                this.routeFlashSaleEventOrderCase(myMessagePayloadTemplate, myMQConsumptionContext);
                break;
            default:
                throw new MyMQException(String.format("Cannot route feature by this routingKey: %s", routingKey));
        }
    }

    /**
     * 針對fse.order的事件，根據title去路遊細部功能。
     */
    private void routeFlashSaleEventOrderCase(MyMessagePayloadTemplate myMessagePayloadTemplate, MyMQConsumptionContext myMQConsumptionContext) throws MyMQException {
        String title = myMessagePayloadTemplate.getTitle();

        try {
            switch (title) {
                case MyRabbitMQConsts.TITLE_NAME_FOR_ORDER_GENERATION_OF_FLASH_SALE_EVENT_ORDER_CASE:
                    flashOrderService.createOrder(objectMapper.readTree(myMessagePayloadTemplate.getContent()), myMQConsumptionContext);
                    break;
                default:
                    throw new MyMQException(String.format("Cannot route feature of \"fse.order\" by this title: %s", title));
            }
        } catch (JsonProcessingException jpe) { // 轉換content失敗
            throw new MyMQException("Failed to convert content of myMessagePayloadTemplate to jsonNode.", jpe);
        }
    }

}
