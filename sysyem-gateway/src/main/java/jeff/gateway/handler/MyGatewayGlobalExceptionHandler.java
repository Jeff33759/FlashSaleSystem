package jeff.gateway.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import jeff.common.entity.bo.MyRequestContext;
import jeff.common.entity.dto.outer.OuterCommunicationDto;
import jeff.common.exception.MyException;
import jeff.common.util.LogUtil;
import jeff.gateway.bo.MyServerWebExchangeDecoratorWrapper;
import jeff.gateway.filter.global.ReactiveLoggingFilter;
import jeff.gateway.util.MyOuterHttpCommunicationUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.web.ErrorProperties;
import org.springframework.boot.autoconfigure.web.WebProperties;
import org.springframework.boot.autoconfigure.web.reactive.error.DefaultErrorWebExceptionHandler;
import org.springframework.boot.web.reactive.error.ErrorAttributes;
import org.springframework.cloud.gateway.filter.NettyRoutingFilter;
import org.springframework.cloud.gateway.filter.RemoveCachedBodyFilter;
import org.springframework.cloud.gateway.filter.factory.SpringCloudCircuitBreakerFilterFactory;
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.*;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.adapter.DefaultServerWebExchange;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLDecoder;

/**
 * 定義全局的gateway錯誤處理器，任何在轉發的途中遭遇錯誤，沒有在過濾鏈中被處理掉的，最後就會來到這裡。
 *
 * 參考文件:
 * https://www.baeldung.com/spring-cloud-global-exception-handling
 * https://www.baeldung.com/spring-webflux-errors
 * 或者可以參考{@link DefaultErrorWebExceptionHandler}去寫。
 */
@Slf4j
@Component
@Order(-2) //要比DefaultErrorWebExceptionHandler還要早，它是@Order(-1)
public class MyGatewayGlobalExceptionHandler extends DefaultErrorWebExceptionHandler {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private LogUtil logUtil;

    @Autowired
    private MyOuterHttpCommunicationUtil myOuterHttpCommunicationUtil;


    public MyGatewayGlobalExceptionHandler(ErrorAttributes errorAttributes, WebProperties webProperties, ApplicationContext applicationContext, ServerCodecConfigurer configurer) {
        super(errorAttributes, webProperties.getResources(), new ErrorProperties(), applicationContext);
        this.setMessageWriters(configurer.getWriters());
    }


    /**
     * 在原方法基礎上，加印Log。
     * 當過濾鏈任一環節，發了Mono.error才會進到handler。
     *
     * HttpClient拋錯時(如遭遇超時)，會進到這個方法處理，且SetStatus GatewayFilter不會起作用(因為Mono.error時不做事)，所以要在這裡決定對外回覆的狀態碼。
     *
     * 如果該router有設定circuitBreaker，也有設定fallbackUri，那即使遭遇超時，也不會跑到這裡，會跑進fallback。詳情看{@link SpringCloudCircuitBreakerFilterFactory}，也可以在那裡設定接到哪些狀態碼視為失敗的調用。
     * 這裡主要是給某些沒有設置circuitBreaker的router用的。
     *
     * 這裡接到的throwable，都是經過其他過濾器包裝過的，不會是最底層的cause。例如responseTimeout，是由{@link NettyRoutingFilter#filter}包裝後，拋出ResponseStatusException到這此層。
     * 如果想要對各式各樣的例外情況處理，可能要去參考官方文件看有哪些過濾器，然後進去看SourceCode，然後在這裡去捕捉。
     * 官方文件:
     * https://docs.spring.io/spring-cloud-gateway/docs/3.1.4/reference/html/#global-filters
     *
     * @param exchange 進到這裡的時候，已經和自製過濾鏈中的{@link MyServerWebExchangeDecoratorWrapper}是不同實例了，應該是在哪邊被包裝成了型別為{@link DefaultServerWebExchange}的新實例了。
     */
    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable throwable) {

        //因為配置檔有加CacheRequestBodyFilter，所以這裡可以取到CACHED_REQUEST_BODY_ATTR
        //但因為RemoveCachedBodyFilter的doFinally會比this.logReqAndRes早執行(當遭遇timeout的情況)，所以不得已又把reqBody暫存到自己的key。
        //這裡試過在handle()後面接各種doOnXXX或then，都沒辦法。
        Object reqCacheBody = exchange.getAttribute(ServerWebExchangeUtils.CACHED_REQUEST_BODY_ATTR);
        exchange.getAttributes().put("myHandlerCacheReqBody", reqCacheBody);

        return super.handle(exchange, throwable) //這裡面會呼叫renderErrorResponse
                .then(Mono.fromRunnable(() -> { //回應完後紀錄，記錄完後清除快取。
                    try {
                        this.logReqAndRes(exchange);
                    } catch (IOException e) {
                        new MyException("Some error occur when logging api info", e);
                    }

                    this.removeMyHandlerCacheInExchange(exchange);
                }));
    }

    /**
     * 如何得到"gateway -> 服務"的調用前後的資訊，可以參考{@link NettyRoutingFilter#filter}
     */
    @Override
    protected Mono<ServerResponse> renderErrorResponse(ServerRequest request) {

        //1、得到exchange物件，相當於流的上下文
        ServerWebExchange exchange = request.exchange();

        //2、準備method所需參數
        MyRequestContext myContext = exchange.getAttribute("myContext");
        URI requestUriToDownstream = exchange.getRequiredAttribute(ServerWebExchangeUtils.GATEWAY_REQUEST_URL_ATTR); //得到"gateway->服務A或B或C"的調用資訊
        Throwable exception = this.getError(request);

        //3、紀錄例外，得到回應的body物件
        OuterCommunicationDto outerCommunicationDto = myOuterHttpCommunicationUtil.logExceptionAndGetOuterCommunicationDto(
                (Exception) exception,
                myContext,
                requestUriToDownstream
        );

        //4、把回應的body物件暫存起來，用於this.logReqAndRes
        exchange.getAttributes().put("myHandlerCacheOuterRes", outerCommunicationDto); //應該不會有競爭的問題，因為各個請求的exchange是不同實例

        //4、組織回應物件，gateway對外的狀態碼統一都是200
        return ServerResponse
                .status(HttpStatus.OK)
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(outerCommunicationDto));
    }

    /**
     * 紀錄外部Server <-> gateway的資訊。
     *
     * 本來應該在{@link ReactiveLoggingFilter}做，但在那邊做，當Mono.error的時候會取不到回應的body，所以Mono.error的情況，就在這裡做。
     */
    private Mono<Void> logReqAndRes(ServerWebExchange exchange) throws IOException {
//      1、取得請求的資訊、回應body、myContext
        ServerHttpRequest request = exchange.getRequest();
        Object reqBody = exchange.getAttribute("myHandlerCacheReqBody");
        OuterCommunicationDto resBody = exchange.getAttribute("myHandlerCacheOuterRes");
        MyRequestContext myContext = exchange.getAttribute("myContext");

//      2、日誌紀錄
        logUtil.logDebug( //gateway為最上游的節點，會乘載大量請求，所以指定為debug級別供開發使用
                log,
                logUtil.composeLogPrefixForBusiness(myContext.getAuthenticatedMemberId(), myContext.getUUID()),
                String.format(
                        "The info of request, clientIP: %s, method: %s, path: %s, queryString: %s, body: %s. The info of response, status: %s, body: %s",
                        request.getRemoteAddress(),
                        request.getMethod(),
                        request.getPath(),
                        this.decodeQueryString(request.getURI().getRawQuery()), // 會回傳沒經過編碼的原始字串，例如空白鍵不會被轉譯成%20
                        objectMapper.writeValueAsString(reqBody),
                        HttpStatus.OK.value(), //統一都是200
                        objectMapper.writeValueAsString(resBody)
                )
        );

        return Mono.empty();
    }

    /**
     * reqWrapper.getURI().getRawQuery()得出來的中文字會被編碼過，變成%。
     * 在這裡進行解碼，解碼成可以閱讀的中文字。
     */
    private String decodeQueryString(String queryStr) throws UnsupportedEncodingException {
        return queryStr == null ? "null" : URLDecoder.decode(queryStr, "UTF-8");
    }

    /**
     * 移除一些用於handler的快取。
     * 參考{@link RemoveCachedBodyFilter}。
     *
     * TODO 因為看SpringCloud也有做清除快取的這件事，所以我也做了，但不是很明白為何要特地去做，有GC不是嗎。
     * TODO 也許是因為Body的資料量比較大，完全交給GC的話，會變成頻繁觸發GC造成卡頓，而gateway又是最上游Server，一旦卡了，整個系統都會卡。
     */
    private void removeMyHandlerCacheInExchange(ServerWebExchange exchange) {
        exchange.getAttributes().remove("myHandlerCacheReqBody");
        exchange.getAttributes().remove("myHandlerCacheOuterRes");
    }


}
