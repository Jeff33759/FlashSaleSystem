package jeff.highconcurrency.controller;

import com.fasterxml.jackson.databind.JsonNode;
import io.github.resilience4j.ratelimiter.RequestNotPermitted;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import jeff.common.consts.DemoMember;
import jeff.common.entity.bo.MyRequestContext;
import jeff.common.entity.dto.receive.ResponseObjectFromInnerSystem;
import jeff.common.exception.MyException;
import jeff.common.exception.BusyException;
import jeff.highconcurrency.entity.bo.MyServerWebExchangeDecoratorWrapper;
import jeff.highconcurrency.service.FlashSaleEventService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

/**
 * 高併發業務接口。
 *
 * 在 Spring WebFlux 中，當編寫控制器(Controller)的時候，Spring 框架會負責處理訂閱(subscribe)的部分，因此，不需要顯式調用 subscribe 方法。
 * Spring WebFlux 框架負責處理響應式流的訂閱，以確保數據在正確的時機被發送到客戶端，這是 WebFlux 的優勢之一，它使異步和非阻塞編程更加方便和直觀。
 *
 * 當使用Mono和Flux來處理請求時，WebFlux不會為每個請求分配獨立的執行緒(不同於以往的同步模型)，而是通過非同步處理，更高效地利用執行緒和資源。
 */
@RestController
@RequestMapping(produces = org.springframework.http.MediaType.APPLICATION_JSON_VALUE)
public class HighConcurrencyController {

    @Autowired
    private FlashSaleEventService flashSaleEventService;

    /**
     * 因為fallback的情況可能會一瞬間做很多次，而BusyException也沒要特別再針對場景包不同的cause，所以用同實例，就不用每次都new了。
     */
    private BusyException busyExceptionForFallback = new BusyException("Server is busy, please try again later");

    /**
     * 客戶端快閃銷售案件的商品下單時的接口。
     * 通常是搶門票的頁面，進入快閃銷售案件特有的結帳頁面後，按下送出所打的API。
     *
     * 這裡不用斷路器的原因，是因為此功能不能接受斷路器長時間開啟，假如門票12點開賣，11:59斷路器被打開，那可能會變成12點到了，大家還辦法送出搶門票請求的情況。
     */
    @PostMapping("/order/flash")
    @RateLimiter(name = "fse-order-creation-rl", fallbackMethod = "createAnOrderFromFlashSalesEventFallback")
    public Mono<ResponseEntity<ResponseObjectFromInnerSystem>> createAnOrderFromFlashSalesEvent(MyServerWebExchangeDecoratorWrapper serverWebExchange, @RequestBody JsonNode param) throws MyException {
        MyRequestContext myReqContext = serverWebExchange.getAttribute("myContext");
        myReqContext.setAuthenticatedMemberId(DemoMember.CUSTOMER.getId()); // TODO 此API的請求者就是買家，目前先寫死，所以前端也不用傳這個參數

        return flashSaleEventService.consumeFlashSaleEvent(param, myReqContext)
                .map(resObj -> ResponseEntity.ok(resObj));
    }

    /**
     * 回傳快閃銷售案件的資訊，用於渲染搶購頁面的view。
     *
     * 會由high-concurrency-processor來承接請求，並做為上游Server打請求到core-processor要資料，core-processor做為下游Server提供資料。
     */
    @GetMapping("/flash-sale-event/query/{fse_id}")
    public Mono<ResponseEntity<Mono<ResponseObjectFromInnerSystem>>> getFlashSaleEventInfo(MyServerWebExchangeDecoratorWrapper serverWebExchange, @PathVariable("fse_id") int fseId) throws MyException {
        MyRequestContext myReqContext = serverWebExchange.getAttribute("myContext");

        return flashSaleEventService.getFlashSaleEventInfo(fseId, myReqContext);
    }

    /**
     * createAnOrderFromFlashSalesEvent接口的fallback，回傳型別要跟原方法一樣。
     *
     * 這裡直接拋例外，讓AOP那裡去統一處理。
     */
    private Mono<ResponseEntity<ResponseObjectFromInnerSystem>> createAnOrderFromFlashSalesEventFallback(MyServerWebExchangeDecoratorWrapper serverWebExchange, JsonNode param, Exception e) throws Exception {
        if (e instanceof RequestNotPermitted) { // 如果是限流觸發時就會拋此例外，處理成自己的例外
            throw this.busyExceptionForFallback;
        }

        throw e;
    }

}
