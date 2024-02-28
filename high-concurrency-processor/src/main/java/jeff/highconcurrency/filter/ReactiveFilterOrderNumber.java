package jeff.highconcurrency.filter;

/**
 * 統一控管過濾器的順序。
 * 不用Enum是因為@Order(XX)只吃final宣告的常數。
 */
public class ReactiveFilterOrderNumber {

    private ReactiveFilterOrderNumber() {
    }

    public static final int REACTIVE_WRAPPER_FILTER = 1;

    public static final int REACTIVE_REQ_CONTEXT_GENERATION_FILTER = 2;

    public static final int REACTIVE_UUID_FILTER = 3;

    public static final int REACTIVE_LOGGING_FILTER = 4;

}
