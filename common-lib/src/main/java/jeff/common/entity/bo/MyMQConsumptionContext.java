package jeff.common.entity.bo;

/**
 * MQ的消費事件執行時的上下文物件，用來代表一個消費事件的生命週期。
 */
public class MyMQConsumptionContext extends MyContext {

    /**
     * 自己寫一個鏈式setter，比較好用。
     */
    public MyMQConsumptionContext chainSetUUID(String UUID) {
        super.setUUID(UUID);
        return this;
    }
}
