package jeff.highconcurrency.filter;

import jeff.common.entity.bo.MyRequestContext;
import jeff.highconcurrency.entity.bo.MyServerWebExchangeDecoratorWrapper;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;


/**
 * 設置自己做的MyRequestContext物件進request物件中，方便後續業務邏輯取用。
 * 在這一層中還只是實例化上下文物件，之後的過濾鏈才會視情況對裡面的成員變數賦值。
 */
@Component
@Order(ReactiveFilterOrderNumberConst.REACTIVE_REQ_CONTEXT_GENERATION_FILTER)
public class ReactiveReqContextGenerationFilter implements WebFilter {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        MyServerWebExchangeDecoratorWrapper exchangeWrapper = (MyServerWebExchangeDecoratorWrapper) exchange;

        exchangeWrapper.getAttributes().put("myContext", new MyRequestContext()); // The context for a request lifecycle.

        return chain.filter(exchangeWrapper);
    }
}
