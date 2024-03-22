package jeff.gateway.filter.global.consts;

import jeff.gateway.bo.MyServerHttpResponseDecoratorWrapper;
import org.springframework.cloud.gateway.filter.NettyWriteResponseFilter;
import org.springframework.cloud.gateway.route.RouteDefinitionRouteLocator;

/**
 * 統一控管gateway全局路由過濾器的順序。
 * 全局路由過去器，會做用於所有router。
 */
public class GatewayGlobalFilterOrderNumberConst {

    private GatewayGlobalFilterOrderNumberConst() {
    }

    /**
     * Spring Cloud Gateway的既有全局路由過濾器是有順序的。
     *
     * 框架既有過濾器優先級，參考文件:
     * https://docs.spring.io/spring-cloud-gateway/docs/3.1.4/reference/html/#gateway-global-filters
     */
    private static final int START_ORDER_NUMBER = -50;

    public static final int REACTIVE_WRAPPER_FILTER = START_ORDER_NUMBER; //-50

    public static final int REACTIVE_REQ_CONTEXT_GENERATION_FILTER = START_ORDER_NUMBER + 1; //-49

    public static final int REACTIVE_UUID_FILTER = START_ORDER_NUMBER + 2; //-48

    /**
     * 順序要小於{@link NettyWriteResponseFilter}，正常情況下{@link MyServerHttpResponseDecoratorWrapper#getBodyDataAsString()}才會取得到東西。
     */
    public static final int REACTIVE_LOGGING_FILTER = START_ORDER_NUMBER + 3; //-47

    public static final int ADD_REQUEST_HEADER_FILTER = START_ORDER_NUMBER + 4; //-46

    /**
     * 要比SpringCloudCircuitBreakerFilter還大，因為要在post logic時，先檢查下游Server的回傳是否為預期的狀態碼。
     * SpringCloudCircuitBreakerFilter的順序，參考{@link RouteDefinitionRouteLocator}，預設從0開始，看你配置檔配了幾個filter，依序+1。
     */
    public static final int CHECK_RES_STATUS_OF_DOWNSTREAM_FILTER = START_ORDER_NUMBER + 61; //11

}
