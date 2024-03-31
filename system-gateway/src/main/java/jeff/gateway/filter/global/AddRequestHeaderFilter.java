package jeff.gateway.filter.global;

import jeff.common.entity.bo.MyRequestContext;
import jeff.gateway.bo.MyServerWebExchangeDecoratorWrapper;
import jeff.gateway.filter.global.consts.GatewayGlobalFilterOrderNumberConst;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * 把要訪問下游Server的請求，帶上一些自訂的標頭。
 *
 * 參考文件:
 * https://cloud.spring.io/spring-cloud-static/spring-cloud-gateway/2.2.1.RELEASE/reference/html/#the-addrequestheader-gatewayfilter-factory
 */
@Component
public class AddRequestHeaderFilter implements GlobalFilter, Ordered {


    /**
     * 新增標頭myUUID。
     * 沒辦法用純配置檔的方式做掉，UUID有亂數產生的邏輯。
     */
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        MyServerWebExchangeDecoratorWrapper exchangeWrapper = (MyServerWebExchangeDecoratorWrapper) exchange;

        MyRequestContext myContext = exchangeWrapper.getAttribute("myContext");
        exchangeWrapper.getRequest().mutate().headers(httpHeaders -> httpHeaders.add("myUUID", myContext.getUUID()));

        return chain.filter(exchangeWrapper);
    }

    @Override
    public int getOrder() {
        return GatewayGlobalFilterOrderNumberConst.ADD_REQUEST_HEADER_FILTER;
    }

}
