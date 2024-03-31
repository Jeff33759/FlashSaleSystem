package jeff.common.entity.bo;

/**
 * MQ的消費事件執行時的上下文物件，用來代表一個消費事件的生命週期。
 */
public class MyMQConsumptionContext extends MyContext {

    /**
     * 自己寫一個鏈式setter，比較好用。
     *
     * 此UUID不等於MsgId。
     * MsgId是紀錄在msg裡面，代表一個msg從生產到被消費的生命週期。
     * 此UUID則是代表consumer從接收到MSG後進行的一連串業務行為的生命週期。
     */
    public MyMQConsumptionContext chainSetUUID(String UUID) {
        super.setUUID(UUID);
        return this;
    }
}
