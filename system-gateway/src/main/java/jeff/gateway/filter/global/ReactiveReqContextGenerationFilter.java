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
 * 設置自己做的MyRequestContext物件進request物件中，方便後續業務邏輯取用。
 * 在這一層中還只是實例化上下文物件，之後的過濾鏈才會視情況對裡面的成員變數賦值。
 */
@Component
public class ReactiveReqContextGenerationFilter implements GlobalFilter, Ordered {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        MyServerWebExchangeDecoratorWrapper exchangeWrapper = (MyServerWebExchangeDecoratorWrapper) exchange;

        exchangeWrapper.getAttributes().put("myContext", new MyRequestContext()); // The context for a request lifecycle.

        return chain.filter(exchangeWrapper);
    }

    @Override
    public int getOrder() {
        return GatewayGlobalFilterOrderNumberConst.REACTIVE_REQ_CONTEXT_GENERATION_FILTER;
    }
}
