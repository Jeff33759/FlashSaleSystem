package jeff.mq.consts;

/**
 * 集中管理RabbitMQ的常數。
 *
 * 術語筆記:
 * Binder(連結器): Spring Cloud Stream 提供的抽象層，用於連接應用程序與消息中介（如 RabbitMQ、Kafka 等）。Binder 負責將應用程序中的消息通道與底層消息中介進行連接，並處理與中介之間的通信。在配置文件中，可以定義一個或多個 Binder，每個 Binder 代表一個消息中介的配置，例如 RabbitMQ Binder。
 * Binding（綁定）: 將消息通道（Channel）和物理消息中介之間的連結。一個 Binding 定義了一個應用程序的輸入（input）或輸出（output）通道，並指定了與之關聯的消息中介的特定配置。在配置文件中，你會定義一個或多個 Binding，每個 Binding 將一個消息通道與特定的消息中介（由 Binder 定義）進行綁定。
 * 簡而言之，Binder用於配置消息中介的抽象層，而 Binding 是用於將消息通道與特定消息中介之間進行實際連結的配置。Binder 處理整個消息中介的連接，而 Binding 處理單個消息通道的配置。
 */
public class MyRabbitMQConsts {

    private MyRabbitMQConsts() {
    }

    // ----快閃銷售案件相關----

    /**
     * 快閃銷售案件中關於訂單相關的事件的routingKey。
     */
    public static final String ROUTING_KEY_NAME_FOR_FLASH_SALE_EVENT_ORDER_CASE = "fse.order";


    /**
     * 快閃銷售案件中關於訂單相關的事件，其中的訂單成立事件。
     */
    public static final String TITLE_NAME_FOR_ORDER_GENERATION_OF_FLASH_SALE_EVENT_ORDER_CASE = "generation";

}
