package jeff.mq.entity.dto;


import lombok.Getter;

/**
 * 自己定義的message模板。
 */
@Getter
public class MyMessagePayloadTemplate {

    /**
     * 消息的標題。
     * 用於同一個routingKey底下，再進一步去路由不同的業務邏輯。
     *
     * 沒用到就傳空字串。
     */
    private String title;

    /**
     * 消息的本體。
     * 用來存放一些消費message時的業務邏輯所需的資料。
     *
     * 沒用到就傳空字串。
     */
    private String content;

    /**
     * 發布的時間點。
     */
    private long publishTimestamp;

    /**
     * @param title     沒用到就傳空字串。
     * @param content   沒用到就傳空字串。
     */
    public MyMessagePayloadTemplate(String title, String content) {
        this.title = title;
        this.content = content;
        this.publishTimestamp = System.currentTimeMillis();
    }

    /**
     * MyMQConsumer那邊要用，否則沒法轉換成Pojo。
     */
    public MyMessagePayloadTemplate() {
    }

}
