package jeff.highconcurrency.filter;

import jeff.highconcurrency.entity.bo.MyServerWebExchangeDecoratorWrapper;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;


/**
 * 將請求與回應物件用包裝器包裝，讓資料流可以重複使用、或者包裝body的getter，不用在外面又做轉換。
 */
@Component
@Order(ReactiveFilterOrderNumber.REACTIVE_WRAPPER_FILTER)
public class ReactiveWrapperFilter implements WebFilter {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        MyServerWebExchangeDecoratorWrapper exchangeWrapper = new MyServerWebExchangeDecoratorWrapper(exchange); // 裡面也把請求、回應都給包裝了起來，可能會造成頻繁GC

        return chain.filter(exchangeWrapper); // 之後過濾鏈所取得的實例都是包裝器的實例，可以直接強轉
    }

}
