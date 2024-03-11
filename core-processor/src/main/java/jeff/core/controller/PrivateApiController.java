package jeff.core.controller;

import com.fasterxml.jackson.databind.JsonNode;
import io.github.resilience4j.ratelimiter.RequestNotPermitted;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import jeff.common.entity.bo.MyRequestContext;
import jeff.common.entity.dto.receive.ResponseObjectFromInnerSystem;
import jeff.common.exception.BusyException;
import jeff.core.service.AllSaleEventService;
import jeff.core.service.SystemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 存放系統內部溝通用Api的控制器，這裡的接口僅提供給其他內部系統伺服端訪問，又或者開發人員自己CURL訪問，不開放給外部訪問。
 * 也許之後可以考慮加個認證機制。
 */
@RestController
@RequestMapping(path = "/inner", produces = org.springframework.http.MediaType.APPLICATION_JSON_VALUE)
public class PrivateApiController {

    @Autowired
    private SystemService systemService;

    @Autowired
    private AllSaleEventService allSaleEventService;

    /**
     * 因為fallback的情況可能會一瞬間做很多次，而BusyException也沒要特別再針對場景包不同的cause，所以用同實例，就不用每次都new了。
     */
    private BusyException busyExceptionForFallback = new BusyException("Server is busy, please try again later");


    /**
     * 初始化redis和MySql，方便DEMO。
     */
    @GetMapping("/system/init")
    public ResponseEntity<ResponseObjectFromInnerSystem> initRedisAndMySql() {
        return ResponseEntity.ok(systemService.initAllDBAndRedis());
    }

    /**
     * 回傳快閃銷售案件的資訊，用於渲染搶購頁面的view。
     *
     * 當快閃銷售案件一被創建，is_public=true，那麼會員就可以在案件列表找到資料，並且進去搶購頁面，只是因為當時可能還沒開賣，has_been_scanned=false，所以送出搶購封包也沒用，會redis left pop失敗。
     *
     * 會由high-concurrency-processor來承接請求，並做為上游Server打請求到此接口要資料，core-processor做為下游Server提供資料。
     * 此API就是回傳當玩家進入搶購頁面的view所需要的data，因為在開賣前可能會被瘋狂F5，所以資料要快取起來，並且設置限流器，當一次太多人F5，則做服務降級，避免服務雪崩。
     * 這裡不用斷路器的原因，是因為此功能不能接受斷路器長時間開啟，假如門票12點開賣，11:59斷路器被打開，那可能會變成12點到了，大家還查不到view而不能搶門票的情況。
     *
     * 雖是查詢API，但還是用POST，因為這是innerAPI，可能會有需要在BODY夾帶系統溝通用資料的可能。
     */
    @PostMapping("/flash-sale-event/query")
    @RateLimiter(name = "fse-query-rl", fallbackMethod = "getFlashSaleEventInfoFallback")
    public ResponseEntity<ResponseObjectFromInnerSystem> getFlashSaleEventInfo(@RequestBody JsonNode param, @RequestAttribute(value = "myContext") MyRequestContext myRequestContext) {
        return ResponseEntity.ok(allSaleEventService.getCacheOfFlashSaleEventInfo(param, myRequestContext));
    }

    /**
     * getFlashSaleEventInfoFall接口的fallback，回傳型別要跟原方法一樣。
     *
     * 這裡直接拋例外，讓AOP那裡去統一處理。
     */
    private ResponseEntity<ResponseObjectFromInnerSystem> getFlashSaleEventInfoFallback(JsonNode param, MyRequestContext myRequestContext, Exception e) throws Exception {
        if (e instanceof RequestNotPermitted) { // 如果是限流觸發時就會拋此例外，處理成自己的例外
            throw this.busyExceptionForFallback;
        }

        throw e;
    }

}
