package jeff.highconcurrency.filter.consts;

/**
 * 統一控管過濾器的順序。
 * 不用Enum是因為@Order(XX)只吃final宣告的常數。
 */
public class ReactiveFilterOrderNumberConst {

    private ReactiveFilterOrderNumberConst() {
    }

    private static final int START_ORDER_NUMBER = 1;

    public static final int REACTIVE_WRAPPER_FILTER = START_ORDER_NUMBER; //1

    public static final int REACTIVE_REQ_CONTEXT_GENERATION_FILTER = START_ORDER_NUMBER + 1; //2

    public static final int REACTIVE_UUID_FILTER = START_ORDER_NUMBER + 2; //3

    public static final int REACTIVE_LOGGING_FILTER = START_ORDER_NUMBER + 3; //4

}
