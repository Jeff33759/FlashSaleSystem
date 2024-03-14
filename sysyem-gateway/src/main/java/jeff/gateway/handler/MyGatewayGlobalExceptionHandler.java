package jeff.gateway.handler;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jeff.common.entity.dto.send.ResponseObject;
import jeff.common.util.LogUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.web.WebProperties;
import org.springframework.boot.autoconfigure.web.reactive.error.AbstractErrorWebExceptionHandler;
import org.springframework.boot.autoconfigure.web.reactive.error.DefaultErrorWebExceptionHandler;
import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.reactive.error.ErrorAttributes;
import org.springframework.cloud.gateway.filter.NettyRoutingFilter;
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
import org.springframework.cloud.gateway.support.TimeoutException;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.*;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.annotation.PostConstruct;
import java.net.URI;
import java.util.Map;

/**
 * 定義全局的gateway錯誤處理器。
 *
 * 參考文件:
 * https://www.baeldung.com/spring-cloud-global-exception-handling
 * https://www.baeldung.com/spring-webflux-errors
 * 或者可以參考{@link DefaultErrorWebExceptionHandler}去寫。
 */
@Slf4j
@Component
@Order(-2) //要比DefaultErrorWebExceptionHandler還要早做，它是@Order(-1)
public class MyGatewayGlobalExceptionHandler extends AbstractErrorWebExceptionHandler {

    private final ErrorAttributeOptions myErrorAttributesOptions;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private LogUtil logUtil;


    /**
     * 非預期的例外，或者Http溝通沒成功的例外(如timeout)，目前定義大多數都是回空的content，因為很常用，所以做成單例常數，節省效能與記憶體空間。
     */
    private JsonNode EMPTY_CONTENT;


    public MyGatewayGlobalExceptionHandler(ErrorAttributes errorAttributes, WebProperties webProperties, ApplicationContext applicationContext, ServerCodecConfigurer configurer) {
        super(errorAttributes, webProperties.getResources(), applicationContext);
        this.setMessageWriters(configurer.getWriters());
        this.myErrorAttributesOptions = ErrorAttributeOptions.of();
    }


    /**
     * 等Spring容器完成啟動後，確定各元件都註冊完畢不會有NULL，再對EMPTY_CONTENT賦值。
     */
    @PostConstruct
    private void initVariableAfterTheSpringApplicationStartup() {
        EMPTY_CONTENT = objectMapper.createObjectNode();
    }

    /**
     * 這邊在定義哪些Api路由會經過此Handler進行錯誤判斷處理。
     */
    @Override
    protected RouterFunction<ServerResponse> getRoutingFunction(ErrorAttributes errorAttributes) {
        return RouterFunctions.route(RequestPredicates.all(), serverRequest -> this.renderErrorResponse(serverRequest)); //註冊了一個路由，本路由會將所有的請求都導向this.renderErrorResponse
    }

    /**
     * 在原方法基礎上，加印Log。
     *
     * HttpClient拋錯時(如遭遇超時)，才會進到這個方法處理，且之後SetStatus GatewayFilter不會起作用，所以要在這裡決定對外回覆的狀態碼。
     * 如果下游Server的回應狀態碼非2XX，此情況則不會被認定為是Error，所以不會進到這裡。
     *
     * 這裡接到的throwable，都是經過其他過濾器包裝過的，不會是最底層的cause。例如responseTimeout，是由{@link NettyRoutingFilter#filter}包裝後，拋出ResponseStatusException到這此層。
     * 如果想要對各式各樣的例外情況處理，可能要去參考官方文件看有哪些過濾器，然後進去看SourceCode，然後在這裡去捕捉。
     * 官方文件:
     * https://docs.spring.io/spring-cloud-gateway/docs/3.1.4/reference/html/#global-filters
     */
    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable throwable) {
        return super.handle(exchange, throwable)
                .then(this.logException(exchange, throwable));
    }

    private Mono<ServerResponse> renderErrorResponse(ServerRequest request) {
        Map<String, Object> errorAttributesMap = this.getErrorAttributes(request, this.myErrorAttributesOptions);

        int statusCode = (int) errorAttributesMap.get("status");
        String errorMsg = (String) errorAttributesMap.get("error"); //對外的錯誤訊息，如"Gateway Timeout"

        return ServerResponse
                .status(HttpStatus.OK) //對系統外部固定回200
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(new ResponseObject(statusCode, EMPTY_CONTENT, errorMsg)));
    }

    /**
     * 如何得到"gateway -> 服務"的調用前後的資訊，可以參考{@link NettyRoutingFilter#filter}
     */
    private Mono<Void> logException(ServerWebExchange exchange, Throwable exception) {
        URI requestUrl = exchange.getRequiredAttribute(ServerWebExchangeUtils.GATEWAY_REQUEST_URL_ATTR); //得到"gateway->服務A或B或C"的調用資訊，要這樣做。
        Throwable causeEx = exception.getCause();

        if(causeEx instanceof TimeoutException) {
            logUtil.logWarn(
                    log,
                    logUtil.composeLogPrefixForBusiness(null, exchange.getRequest().getHeaders().getFirst("myUUID")),
                    String.format(
                            "Call other service timeout, accessUrl: %s, causeMessage: %s",
                            requestUrl.toASCIIString(),
                            causeEx.getMessage()
                    )
            );

            return Mono.empty();
        }

        logUtil.logError(
                log,
                logUtil.composeLogPrefixForSystem(),
                String.format(
                        "Some errors occurred when calling other service, accessUrl: %s, causeMessage: %s",
                        requestUrl.toASCIIString(),
                        causeEx.getMessage()
                ),
                (Exception) exception
        );
        return Mono.empty();
    }


}
