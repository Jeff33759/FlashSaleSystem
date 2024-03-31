package jeff.gateway.filter.global;

import jeff.gateway.bo.MyServerWebExchangeDecoratorWrapper;
import jeff.gateway.filter.global.consts.GatewayGlobalFilterOrderNumberConst;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;


/**
 * 將請求與回應物件用包裝器包裝，讓資料流可以重複使用、或者包裝body的getter，不用在外面又做轉換。
 */
@Component
public class ReactiveWrapperFilter implements GlobalFilter, Ordered {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        MyServerWebExchangeDecoratorWrapper exchangeWrapper = new MyServerWebExchangeDecoratorWrapper(exchange); // 裡面也把請求、回應都給包裝了起來，可能會造成頻繁GC

        return chain.filter(exchangeWrapper); // 之後過濾鏈所取得的實例都是包裝器的實例，可以直接強轉
    }

    @Override
    public int getOrder() {
        return GatewayGlobalFilterOrderNumberConst.REACTIVE_WRAPPER_FILTER;
    }
}
