package jeff.gateway.filter.global.consts;

/**
 * 統一控管gateway全局路由過濾器的順序。
 * 全局路由過去器，會做用於所有router。
 */
public class GatewayGlobalFilterOrderNumberConst {

    private GatewayGlobalFilterOrderNumberConst() {
    }

    /**
     * Spring Cloud Gateway的既有全局路由過濾器是有順序的。我希望自己安插的這幾個自訂過濾器優先於框架既有全局過濾器，於是優先級從負數算起，越小越先。
     *
     * 框架既有過濾器優先級，參考文件:
     * https://docs.spring.io/spring-cloud-gateway/docs/3.1.4/reference/html/#gateway-global-filters
     */
    private static final int START_ORDER_NUMBER = -50;

    public static final int REACTIVE_WRAPPER_FILTER = START_ORDER_NUMBER; //-50

    public static final int REACTIVE_REQ_CONTEXT_GENERATION_FILTER = START_ORDER_NUMBER + 1; //-49

    public static final int REACTIVE_UUID_FILTER = START_ORDER_NUMBER + 2; //-48

    public static final int REACTIVE_LOGGING_FILTER = START_ORDER_NUMBER + 3; //-47

    public static final int ADD_REQUEST_HEADER_FILTER = START_ORDER_NUMBER + 4; //-46

}
