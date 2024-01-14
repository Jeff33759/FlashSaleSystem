package jeff.highconcurrency.controller;

import com.sun.org.apache.xpath.internal.operations.Bool;
import jeff.common.entity.dto.send.ResponseObject;
import jeff.highconcurrency.persistent.model.mongo.dao.ReactiveFlashSaleEventLogRepo;
import jeff.highconcurrency.service.FlashSaleEventService;
import jeff.highconcurrency.util.redis.util.MyReactiveRedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
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
public class HighConcurrencyController {

    @Autowired
    FlashSaleEventService flashSaleEventService;

    @Autowired
    ReactiveFlashSaleEventLogRepo reactiveFlashSaleEventLogRepo;

    @Autowired
    MyReactiveRedisUtil myReactiveRedisUtil;

    @GetMapping("/hello")
    public Mono hello() {
        return Mono.just("Hello Spring WebFlux RestController");
    }

    @GetMapping("/test")
    public boolean test() {
        System.out.println(1);
        return reactiveFlashSaleEventLogRepo.selectByFseIdAndTransNumAndHasNotBeenConsumed(1, 1)
                .subscribe(
                        b -> {
                            System.out.println(b);
                        }, err -> {
                            System.out.println(err);
                        }
                ).isDisposed();
    }

    @GetMapping("/test2")
    public Mono<ResponseObject> test2() {
        return flashSaleEventService.consumeFlashSaleEvent();
    }

//    @GetMapping("/test3")
//    public Mono<String> test3() {
//        return myReactiveRedisUtil.putDataStrByKey("test", "test")
//                .then(myReactiveRedisUtil.getDataStrByKey("test").map(output -> output.get()));
//    }

    @GetMapping("/test4")
    public Mono<Boolean> test4() {
        return myReactiveRedisUtil.getDataStrByKey("test").map(va -> va.isPresent());
    }

    @GetMapping("/test5")
    public Mono<Void> test5() {
        return myReactiveRedisUtil.removeAllKeys();
    }
}
