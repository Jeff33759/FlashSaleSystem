package jeff.highconcurrency.filter;

import jeff.common.entity.bo.MyRequestContext;
import jeff.common.util.LogUtil;
import jeff.highconcurrency.entity.bo.MyServerHttpRequestDecoratorWrapper;
import jeff.highconcurrency.entity.bo.MyServerWebExchangeDecoratorWrapper;
import jeff.highconcurrency.filter.consts.ReactiveFilterOrderNumberConst;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;


/**
 * 生成UUID去代表一個請求的生命週期，方便日誌中心化時的搜尋(可以用UUID去找到某個請求在業務鏈路中跑了啥方法)。
 */
@Component
@Order(ReactiveFilterOrderNumberConst.REACTIVE_UUID_FILTER)
public class ReactiveUUIDFilter implements WebFilter {

    @Autowired
    private LogUtil logUtil;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        MyServerWebExchangeDecoratorWrapper exchangeWrapper = (MyServerWebExchangeDecoratorWrapper) exchange;

        MyRequestContext myContext = exchangeWrapper.getAttribute("myContext");
        myContext.setUUID(this.checkAndGetUUIDByReq(exchangeWrapper.getRequest())); //因為上面getAttribute得到的是參考，所以這裡set，會直接set進該物件

        return chain.filter(exchangeWrapper);
    }

    /**
     * 檢查標頭中"myUUID"欄位是否已經有值，若有值了，代表這個請求是其他上游微服務發的，那就用上游服務的UUID。
     */
    private String checkAndGetUUIDByReq(MyServerHttpRequestDecoratorWrapper request) {
        String myUUIDFromOtherInnerService = request.getHeaders().getFirst("myUUID");

        if(myUUIDFromOtherInnerService == null || "".equals(myUUIDFromOtherInnerService)) {
            return logUtil.generateUUIDForLogging();
        }

        return myUUIDFromOtherInnerService;
    }

}
