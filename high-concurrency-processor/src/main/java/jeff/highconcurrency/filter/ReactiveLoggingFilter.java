package jeff.highconcurrency.filter;


import jeff.common.entity.bo.MyRequestContext;
import jeff.common.exception.MyException;
import jeff.common.util.LogUtil;
import jeff.highconcurrency.entity.bo.MyServerHttpRequestDecoratorWrapper;
import jeff.highconcurrency.entity.bo.MyServerHttpResponseDecoratorWrapper;
import jeff.highconcurrency.entity.bo.MyServerWebExchangeDecoratorWrapper;
import jeff.highconcurrency.filter.consts.ReactiveFilterOrderNumberConst;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

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
@Order(ReactiveFilterOrderNumberConst.REACTIVE_LOGGING_FILTER)
public class ReactiveLoggingFilter implements WebFilter {

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
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        return chain.filter(exchange).doFinally((se) -> { // 使用doFinally，無論前面的流操作是成功、失敗、取消，都會執行。
            MyServerWebExchangeDecoratorWrapper exchangeWrapper = (MyServerWebExchangeDecoratorWrapper) exchange;

            try {
                this.logReqAndRes(exchangeWrapper.getRequest(), exchangeWrapper.getResponse(), exchangeWrapper.getAttribute("myContext"));
            } catch (IOException e) {
                new MyException("Some error occur when logging api info", e);
            }
        });
    }

    /**
     * 印出API的請求、回應資訊。
     *
     * TODO WebFlux要像core-processor的LoggingFilter一樣，在進入controller前先印請求，很麻煩，需要在過濾鏈讀取reqBody的流。讀取後還要把流重置讓後面的可以再讀一次，因為控制器預設就是讀流。
     * TODO 先做成在controller處理完時，印出請求和回應。
     */
    private void logReqAndRes(MyServerHttpRequestDecoratorWrapper reqWrapper, MyServerHttpResponseDecoratorWrapper resWrapper, MyRequestContext myContext) throws IOException {

        if(ignorePathSet.contains(reqWrapper.getPath().value())) {
            return;
        }

        logUtil.logInfo(
                log,
                logUtil.composeLogPrefixForBusiness(myContext.getAuthenticatedMemberId(), myContext.getUUID()),
                String.format(
                        "The info of request, clientIP: %s, method: %s, path: %s, queryString: %s, body: %s. The info of response, status:%s, body: %s",
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

}
