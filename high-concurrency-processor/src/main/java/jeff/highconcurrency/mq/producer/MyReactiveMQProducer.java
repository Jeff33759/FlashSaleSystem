package jeff.highconcurrency.mq.producer;

import com.fasterxml.jackson.databind.ObjectMapper;
import jeff.common.entity.bo.MyContext;
import jeff.common.util.LogUtil;
import jeff.highconcurrency.mq.exception.MyReactiveMQException;
import jeff.mq.entity.dto.MyMessagePayloadTemplate;
import jeff.mq.exception.MyMQException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

/**
 * 發布MQ的方法包裝，暫時還是用"可能為阻塞性"的StreamBridge元件，官方的reactive範例看不太懂怎麼用，也不曉得那樣做會不會有執行緒競爭的問題。
 *
 * 參考官方文檔:
 * https://docs.spring.io/spring-cloud-stream/docs/3.1.0/reference/html/spring-cloud-stream.html#spring-cloud-stream-preface-new-features
 * https://docs.spring.io/spring-cloud-stream/docs/3.1.0/reference/html/spring-cloud-stream.html#_using_reactor_api
 *
 * 官方針對響應式的說明:
 * Native support for reactive programming - since v3.0.0 we no longer distribute spring-cloud-stream-reactive modules and instead relying on native reactive support provided by spring cloud function. For backward compatibility you can still bring spring-cloud-stream-reactive from previous versions.
 */
@Slf4j
@Component
public class MyReactiveMQProducer {

    /**
     * 本服務與BusinessExchange的綁定名稱。
     */
    private static String BUSINESS_EXCHANGE_BINDING_NAME = "highConcurrencyProcessor_BusinessExchangeProducerChannel-out-0";

    /**
     * Spring-Cloud-Stream 3.X版以後鼓勵用此元件去發布消息。
     */
    @Autowired
    private StreamBridge streamBridge;

    @Autowired
    private LogUtil logUtil;

    @Autowired
    private ObjectMapper objectMapper;


    /**
     * 發布消息到BusinessExchange。
     *
     * @throws MyReactiveMQException 當發布消息失敗時拋錯
     */
    public Mono<Void> produceMessageToBusinessExchange(String routingKey, MyMessagePayloadTemplate myMessagePayloadTemplate, MyContext myContext) throws MyMQException {
        try {
            boolean executedFlag = streamBridge.send( //如果執行不成功，則executedFlag=false或者直接拋錯
                    MyReactiveMQProducer.BUSINESS_EXCHANGE_BINDING_NAME,
                    MessageBuilder
                            .withPayload(myMessagePayloadTemplate)
                            .setHeader("routingKey", routingKey) //設定routing-key-expression: headers['routingKey']，會依照消息標頭去route消息佇列
                            .build()
            );

            if(!executedFlag) {
                throw new MyReactiveMQException("Failed to send data to businessExchange.");
            }

            logUtil.logInfo(
                    log,
                    logUtil.composeLogPrefixForMQProducer(routingKey, myContext.getUUID()),
                    String.format("Msg published successfully, routingKey: %s, payload: %s", routingKey, objectMapper.writeValueAsString(myMessagePayloadTemplate))
            );

            return Mono.empty();
        } catch (Exception e) {
            throw new MyReactiveMQException(String.format("Failed to send data to businessExchange, reason: %s", e.getMessage()), e);
        }
    }

}
