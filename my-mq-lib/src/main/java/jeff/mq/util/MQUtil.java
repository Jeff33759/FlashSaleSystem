package jeff.mq.util;

import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * MQ相關的工具包。
 */
@Component
public class MQUtil {

    /**
     * msgId用於紀錄一個message的生命週期，日誌集中化爬LOG時，可以用這個ID橫跨producer和consumer，去觀察某個msg的生命週期內到底幹了啥。
     * msgId不等於logging的UUID。
     * 同一個msgId，會橫跨producer和consumer，所以msg裡面的msgId會一樣，可是producer和consumer印log時的uuid，會是不同的，分別代表兩個服務執行某業務的生命週期。
     */
    public String generateMsgId() {
        return "mq_" + UUID.randomUUID().toString().substring(0, 4);
    }

}
