package jeff.gateway.controller;

import jeff.common.entity.bo.MyRequestContext;
import jeff.common.entity.dto.outer.OuterCommunicationDto;
import jeff.common.exception.MyException;
import jeff.gateway.util.MyOuterHttpCommunicationUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.NettyRoutingFilter;
import org.springframework.cloud.gateway.filter.factory.FallbackHeadersGatewayFilterFactory;
import org.springframework.cloud.gateway.filter.factory.SpringCloudCircuitBreakerFilterFactory;
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.net.URI;

/**
 * 斷路器或限流器觸發時的fallback轉址接口。
 */
@RestController
@RequestMapping(produces = org.springframework.http.MediaType.APPLICATION_JSON_VALUE)
public class FallbackController {

    @Autowired
    private MyOuterHttpCommunicationUtil myOuterHttpCommunicationUtil;


    /**
     * 當調用下游Server得到回應時，{@link NettyRoutingFilter#filter}會把回應暫存於serverWebExchange.getAttribute(CLIENT_RESPONSE_ATTR)。
     * Spring Cloud Gateway是利用ServerWebExchange的Attribute來作為Context把一個請求生命週期的所有東西暫存並帶到各個流中的。
     *
     * 透過{@link SpringCloudCircuitBreakerFilterFactory}所創建的filter轉接到這個fallback，並在那邊設置關於例外的一些資訊，可在此取用。
     * {@link FallbackHeadersGatewayFilterFactory}這個也可以參考下，如果有設置進過濾鏈的話，那轉到fallback的請求物件中，會多一些關於fallback的訊息。
     */
    @RequestMapping(value = "/cb/fallback/default") //這裡不能用GetMapping，不然原始方法也會被迫一定要Get
    public Mono<ResponseEntity<OuterCommunicationDto>> myDefaultFallback(ServerWebExchange serverWebExchange) throws MyException {

//      1、準備參數
        MyRequestContext myContext = serverWebExchange.getAttribute("myContext");
        Throwable exception = serverWebExchange.getAttribute(ServerWebExchangeUtils.CIRCUITBREAKER_EXECUTION_EXCEPTION_ATTR);
        URI requestUrlToDownstreamServer = serverWebExchange.getRequiredAttribute(ServerWebExchangeUtils.GATEWAY_REQUEST_URL_ATTR); //gateway調用下游Server的一些資訊

//      2、組織回應Body並log例外
        OuterCommunicationDto outerCommunicationDto = myOuterHttpCommunicationUtil.logExceptionAndGetOuterCommunicationDto((Exception) exception, myContext, requestUrlToDownstreamServer);

//      3、組織回應物件
        ResponseEntity<OuterCommunicationDto> body = ResponseEntity
                .status(HttpStatus.OK)
                .body(outerCommunicationDto);

        return Mono.just(body);
    }

}
