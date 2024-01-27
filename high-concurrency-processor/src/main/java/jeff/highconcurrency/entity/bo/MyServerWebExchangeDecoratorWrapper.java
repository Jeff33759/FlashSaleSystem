package jeff.highconcurrency.entity.bo;

import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.ServerWebExchangeDecorator;

/**
 * 因WebFlux向下傳遞資料，都是把請求和回應包在exchange。
 * 所以exchange也要寫個wrapper，覆寫getRequest和getResponse，用於把請求、回應的wrapper向下傳遞。
 */
public class MyServerWebExchangeDecoratorWrapper extends ServerWebExchangeDecorator {

    private MyServerHttpRequestDecoratorWrapper reqWrapper;

    private MyServerHttpResponseDecoratorWrapper resWrapper;

    public MyServerWebExchangeDecoratorWrapper(ServerWebExchange exchange) {
        super(exchange);
        this.reqWrapper = new MyServerHttpRequestDecoratorWrapper(exchange.getRequest());
        this.resWrapper = new MyServerHttpResponseDecoratorWrapper(exchange.getResponse());
    }

    @Override
    public MyServerHttpRequestDecoratorWrapper getRequest() {
        return this.reqWrapper;
    }

    @Override
    public MyServerHttpResponseDecoratorWrapper getResponse() {
        return this.resWrapper;
    }

}
