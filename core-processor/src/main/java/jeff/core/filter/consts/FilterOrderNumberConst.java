package jeff.core.filter.consts;

/**
 * 統一控管過濾器的順序。
 * 不用Enum是因為@Order(XX)只吃final宣告的常數。
 */
public class FilterOrderNumberConst {

    private FilterOrderNumberConst() {
    }

    public static final int WRAPPER_FILTER = 1;

    public static final int REQ_CONTEXT_GENERATION_FILTER = 2;

    public static final int UUID_FILTER = 3;

    public static final int LOGGING_FILTER = 4;

}
