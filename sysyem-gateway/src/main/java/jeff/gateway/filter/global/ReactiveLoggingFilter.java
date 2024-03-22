package jeff.gateway.filter.global;


import jeff.common.entity.bo.MyRequestContext;
import jeff.common.exception.MyException;
import jeff.common.util.LogUtil;
import jeff.gateway.bo.MyServerHttpRequestDecoratorWrapper;
import jeff.gateway.bo.MyServerHttpResponseDecoratorWrapper;
import jeff.gateway.bo.MyServerWebExchangeDecoratorWrapper;
import jeff.gateway.filter.global.consts.GatewayGlobalFilterOrderNumberConst;
import jeff.gateway.handler.MyGatewayGlobalExceptionHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.filter.NettyWriteResponseFilter;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.core.publisher.SignalType;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * 紀錄請求參數與回應的過濾器。
 * 通常會是自製過濾鏈中的最後一層，所以到這裡的時候，MyRequestContext裡面該有的東西都會被賦值了，所以也可以進行log了。
 * 但因為目前還沒實作登入認證，所以到這一層的時候MyRequestContext的AuthenticatedMemberId會是null。
 */
@Slf4j
@Component
public class ReactiveLoggingFilter implements GlobalFilter, Ordered {

    @Autowired
    private LogUtil logUtil;

    /**
     * 不走本過濾器邏輯的Api路徑。
     *
     * java有針對String覆寫hashCode方法，所以Set調用contains時，可以針對字串的值判斷是否重複。
     */
    private Set<String> ignorePathSet =
            new HashSet<>(Arrays.asList(new String[]{
                    "/actuator/health" //actuator套件的健康檢測接口，consul會一直發Get過來監測伺服器健康度
            }));


    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        return chain.filter(exchange).doFinally((signalType) -> { // 使用doFinally，無論前面的流操作是成功、失敗、取消，都會執行，且會在流的最後才執行，而不是在filter post logic的當下就執行。

            if(signalType == SignalType.ON_COMPLETE) { //前面的流沒有發生任何異常(例如timeout等等)
                MyServerWebExchangeDecoratorWrapper exchangeWrapper = (MyServerWebExchangeDecoratorWrapper) exchange;

                try {
                    this.logReqAndRes(exchangeWrapper.getRequest(), exchangeWrapper.getResponse(), exchangeWrapper.getAttribute("myContext"));
                } catch (IOException e) {
                    new MyException("Some error occur when logging api info", e);
                }
            }

            // 當遭遇timeout，因為在NettyRoutingFilter時發布Mono.error了，所以到這裡的Mono是error的
            // 只有前面的流都沒異常，才印log，錯誤的情況，則由Handler那邊去處理並log
        });
    }

    /**
     * 印出API的請求、回應資訊。
     *
     * 因為是寫在doFinally，不管前面流結果怎樣，都會跑此方法。
     * 當HttpClient遭遇錯誤時，例如readTimeout，那會造成resWrapper.getBodyDataAsString()取到空值。因為{@link NettyWriteResponseFilter}不會在Mono.error時，做writeWith。
     * 因此當遭遇readTimeout時，改由後續的{@link MyGatewayGlobalExceptionHandler}來logging。
     */
    private void logReqAndRes(MyServerHttpRequestDecoratorWrapper reqWrapper, MyServerHttpResponseDecoratorWrapper resWrapper, MyRequestContext myContext) throws IOException {

        if(ignorePathSet.contains(reqWrapper.getPath().value())) {
            return;
        }

        logUtil.logDebug( //gateway為最上游的節點，會乘載大量請求，所以指定為debug級別供開發使用
                log,
                logUtil.composeLogPrefixForBusiness(null, myContext.getUUID()),
                String.format(
                        "The info of request, clientIP: %s, method: %s, path: %s, queryString: %s, body: %s. The info of response, status: %s, body: %s",
                        reqWrapper.getRemoteAddress(),
                        reqWrapper.getMethod(),
                        reqWrapper.getPath(),
                        this.decodeQueryString(reqWrapper.getURI().getRawQuery()), // 會回傳沒經過編碼的原始字串，例如空白鍵不會被轉譯成%20
                        reqWrapper.getBodyDataAsString(),
                        resWrapper.getRawStatusCode(),
                        resWrapper.getBodyDataAsString()
                )
        );
    }

    /**
     * reqWrapper.getURI().getRawQuery()得出來的中文字會被編碼過，變成%。
     * 在這裡進行解碼，解碼成可以閱讀的中文字。
     */
    private String decodeQueryString(String queryStr) throws UnsupportedEncodingException {
        return queryStr == null ? "null" : URLDecoder.decode(queryStr, "UTF-8");
    }


    /**
     * 順序要小於{@link NettyWriteResponseFilter}，正常情況下{@link MyServerHttpResponseDecoratorWrapper#getBodyDataAsString()}才會取得到東西，因為要先有人呼叫writeWith。
     */
    @Override
    public int getOrder() {
        return GatewayGlobalFilterOrderNumberConst.REACTIVE_LOGGING_FILTER;
    }
}
