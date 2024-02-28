package jeff.core.mq.producer;

import jeff.mq.entity.dto.MyMessagePayloadTemplate;
import jeff.mq.exception.MyMQException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

/**
 * 發布MQ相關的方法包裝。
 */
@Component
public class MyMQProducer {

    /**
     * 本服務與BusinessExchange的綁定名稱。
     */
    private static String BUSINESS_EXCHANGE_BINDING_NAME = "coreProcessor_BusinessExchangeProducerChannel-out-0";

    /**
     * Spring-Cloud-Stream 3.X版以後鼓勵用此元件去發布消息。
     */
    @Autowired
    private StreamBridge streamBridge;

    /**
     * 發布消息到BusinessExchange。
     *
     * @throws MyMQException 當發布消息失敗時拋錯
     */
    public void produceMessageToBusinessExchange(String routingKey, MyMessagePayloadTemplate myMessagePayloadTemplate) throws MyMQException {
        try {
            boolean executedFlag = streamBridge.send( //如果執行不成功，則executedFlag=false或者直接拋錯
                    MyMQProducer.BUSINESS_EXCHANGE_BINDING_NAME,
                    MessageBuilder
                            .withPayload(myMessagePayloadTemplate)
                            .setHeader("routingKey", routingKey) //設定routing-key-expression: headers['routingKey']，會依照消息標頭去route消息佇列
                            .build()
            );

            if(!executedFlag) {
                throw new MyMQException("Failed to send data to businessExchange.");
            }
        } catch (Exception e) {
            throw new MyMQException(String.format("Failed to send data to businessExchange, reason: %s", e.getMessage()), e);
        }
    }

}
