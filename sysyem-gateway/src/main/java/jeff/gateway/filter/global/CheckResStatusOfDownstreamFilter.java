package jeff.gateway.filter.global;

import jeff.common.exception.MyInnerCommunicationStatusFailureException;
import jeff.common.util.MyHttpCommunicationUtil;
import jeff.gateway.filter.global.consts.GatewayGlobalFilterOrderNumberConst;
import jeff.gateway.handler.MyGatewayGlobalExceptionHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;


/**
 * 檢查從下游Server回傳的狀態碼是否合乎預期。
 */
@Component
public class CheckResStatusOfDownstreamFilter implements GlobalFilter, Ordered {

    @Autowired
    private MyHttpCommunicationUtil myHttpCommunicationUtil;


    /**
     * 因為order比SpringCloudCircuitBreakerFilter還要大，所以post logic時會比SpringCloudCircuitBreakerFilter先做。
     * SpringCloudCircuitBreakerFilter其實就能做到"接到下遊Server特定狀態碼就拋錯"的功能，但某些router若沒設CB就沒這個功能。
     * 所以再多寫一個過濾器，專門做這件事情，讓沒有CB的router也能做到"接到下遊Server特定狀態碼就拋錯"，拋錯後後續就會轉到{@link MyGatewayGlobalExceptionHandler}處理。
     */
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

        return chain.filter(exchange)
                .doOnSuccess(result -> { //跑進這裡代表前面沒人拋錯，HttpClient請求到下游Server是成功的，有得到回應的
//                  這邊exchange能拿到啥東西? 參考NettyRoutingFilter
//                  TODO 嘗試過在這裡取得下游Server回傳的body，但太難了，放棄。

                    HttpStatus httpStatus = exchange.getResponse().getStatusCode();
                    if(myHttpCommunicationUtil.isHttpStatusFromDownstreamCorrect(httpStatus)) {
                        return;
                    }

                    throw new MyInnerCommunicationStatusFailureException(httpStatus); // 如果狀態碼不合預期，則拋此錯，後續讓handler處理。
                });
    }


    /**
     * 要比SpringCloudCircuitBreakerFilter還大。
     */
    @Override
    public int getOrder() {
        return GatewayGlobalFilterOrderNumberConst.CHECK_RES_STATUS_OF_DOWNSTREAM_FILTER;
    }

}
