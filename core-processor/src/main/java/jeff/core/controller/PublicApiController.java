package jeff.core.controller;

import com.fasterxml.jackson.databind.JsonNode;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import jeff.common.entity.dto.inner.InnerCommunicationDto;
import jeff.common.interfaces.IOrderService;
import jeff.common.entity.bo.MyRequestContext;
import jeff.common.consts.DemoMember;
import jeff.common.exception.BusyException;
import jeff.core.exception.OrderException;
import jeff.core.service.AllSaleEventService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * 本伺服端的入口控制器，存放對外開放的接口。
 * 因為不多，所以沒有再根據業務邏輯細分。
 */
@RestController
@RequestMapping(produces = org.springframework.http.MediaType.APPLICATION_JSON_VALUE)
public class PublicApiController {

    @Resource(name = "normalOrderService")
    private IOrderService normalOrderService;

    @Autowired
    private AllSaleEventService allSaleEventService;

    /**
     * 因為fallback的情況可能會一瞬間做很多次，而BusyException也沒要特別再針對場景包不同的cause，所以用同實例，就不用每次都new了。
     */
    private BusyException busyExceptionForFallback = new BusyException("Server is busy, please try again later.");

    /**
     * 客戶端一般銷售案件的商品下單時的接口。
     * 通常是購物車結帳後按下送出所打的API。
     *
     * 因為整個訂單新增流程比較耗時(經歷多個I/O與計算)，所以使用斷路器。如果一段時間內出錯太多次，代表可能一次太多人送購物車造成阻塞，就先熔斷此服務，讓伺服器可以慢慢消化之前的任務。
     */
    @PostMapping("/order/normal")
    @CircuitBreaker(name = "se-order-creation-cb", fallbackMethod = "createAnOrderFromNormalSalesEventFallback")
    public ResponseEntity<InnerCommunicationDto> createAnOrderFromNormalSalesEvent(@RequestBody JsonNode param, @RequestAttribute(value = "myContext") MyRequestContext myRequestContext) throws OrderException {
        myRequestContext.setAuthenticatedMemberId(DemoMember.CUSTOMER.getId()); // TODO 此API的請求者就是買家，目前先寫死，所以前端也不用傳這個參數
        return ResponseEntity.ok(normalOrderService.createOrder(param, myRequestContext));
    }

    /**
     * 當賣家出貨，買家確認收到後，由賣家將訂單的狀態設為已完成。
     * 詳細的交易流程就不設計了，先做成這樣。
     */
    @PostMapping("/finish-order")
    public ResponseEntity<InnerCommunicationDto> finishOrder(@RequestBody JsonNode param, @RequestAttribute(value = "myContext") MyRequestContext myRequestContext) {
        myRequestContext.setAuthenticatedMemberId(DemoMember.SELLER.getId()); // TODO 此API的請求者就是賣家，目前先寫死，所以前端也不用傳這個參數
        return ResponseEntity.ok(normalOrderService.finishOrder(param, myRequestContext));
    }

    /**
     * 更改某個一般銷售案件的狀態(上架中/已下架)。
     * 被下架的銷售案件，將不會顯示在案件列表的頁面上，會員也就不會點選。
     *
     * 一般銷售案件的下架，必須要人為設置，沒有定時下架的功能(快閃銷售案件才有。)。
     * 一度下架的一般銷售案件，可以經由人為設置使其再度上架。
     */
    @PostMapping("/sale-event/update-state")
    public ResponseEntity<InnerCommunicationDto> updateNormalSaleEventState(@RequestBody JsonNode param, @RequestAttribute(value = "myContext") MyRequestContext myRequestContext) throws OrderException {
        myRequestContext.setAuthenticatedMemberId(DemoMember.SELLER.getId()); // TODO 此API的請求者是賣家，目前先寫死，所以前端也不用傳這個參數
        return ResponseEntity.ok(allSaleEventService.updateStateOfNormalSaleEvent(param, myRequestContext));
    }


    /**
     * 將某個快閃銷售案件的狀態更改為已下架。
     * 被下架的銷售案件，將不會顯示在案件列表的頁面上，會員也就不會點選。
     *
     * 快閃銷售案件有時效問題(例如在上架時就要設定幾天後過期自動下架)，且還涉及redis與mongo等等中間件的資料暫存問題，所以統一設計成一旦下架，那就無法再重新上架，要嘛就廠商根據庫存再創一個新的快閃銷售活動。
     */
    @PostMapping("/flash-sale-event/close")
    public ResponseEntity<InnerCommunicationDto> closeFlashSaleEvent(@RequestBody JsonNode param, @RequestAttribute(value = "myContext") MyRequestContext myRequestContext) throws OrderException {
        myRequestContext.setAuthenticatedMemberId(DemoMember.SELLER.getId()); // TODO 此API的請求者是賣家，目前先寫死，所以前端也不用傳這個參數
        return ResponseEntity.ok(allSaleEventService.closeFlashSaleEvent(param, myRequestContext));
    }

    /**
     * createAnOrderFromNormalSalesEvent接口的fallback，回傳型別要跟原方法一樣。
     *
     * 這裡直接拋例外，讓AOP那裡去統一處理。
     */
    private ResponseEntity<InnerCommunicationDto> createAnOrderFromNormalSalesEventFallback(JsonNode param, MyRequestContext myRequestContext, Exception e) throws Exception {
        if (e instanceof CallNotPermittedException) { // 如果是斷路器開啟時就會拋此例外，處理成自己的例外
            throw this.busyExceptionForFallback;
        }

        throw e;
    }

}
