package jeff.core.mq.consumer;

import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;

import java.util.function.Consumer;

/**
 * 集中註冊MQ的監聽者。
 */
@Configuration
public class MyMQConsumer {

    /**
     * 配置請看application.yml
     */
    @Bean(name = "coreProcessor_BusinessExchangeConsumerChannelForOrderCase")
    public Consumer<Message<String>> coreProcessor_BusinessExchangeConsumerChannelForOrderCase() {
        return message -> {
            System.out.println("*******");
            System.out.println(message.getPayload());
            System.out.println(message.getHeaders().get(AmqpHeaders.RECEIVED_ROUTING_KEY));
        };
    }

}
