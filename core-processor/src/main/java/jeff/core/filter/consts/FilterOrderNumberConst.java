package jeff.core.filter.consts;

/**
 * 統一控管過濾器的順序。
 * 不用Enum是因為@Order(XX)只吃final宣告的常數。
 */
public class FilterOrderNumberConst {

    private FilterOrderNumberConst() {
    }

    private static final int START_ORDER_NUMBER = 1;

    public static final int WRAPPER_FILTER = START_ORDER_NUMBER;

    public static final int REQ_CONTEXT_GENERATION_FILTER = START_ORDER_NUMBER + 1;

    public static final int UUID_FILTER = START_ORDER_NUMBER + 2;

    public static final int LOGGING_FILTER = START_ORDER_NUMBER + 3;

}
