package jeff.core.controller;

import com.fasterxml.jackson.databind.JsonNode;
import jeff.common.interfaces.IOrderService;
import jeff.common.entity.bo.MyRequestContext;
import jeff.common.entity.dto.send.ResponseObject;
import jeff.common.consts.DemoMember;
import jeff.core.exception.OrderException;
import jeff.core.service.AllSaleEventService;
import jeff.core.service.SystemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * 本伺服端的入口控制器。
 * 因為不多，所以沒有再根據業務邏輯細分。
 */
@RestController
@RequestMapping(produces = org.springframework.http.MediaType.APPLICATION_JSON_VALUE)
public class CoreController {

    @Resource(name = "normalOrderService")
    private IOrderService normalOrderService;

    @Autowired
    private SystemService systemService;

    @Autowired
    private AllSaleEventService allSaleEventService;

    /**
     * 客戶端一般銷售案件的商品下單時的接口。
     * 通常是購物車結帳後按下送出所打的API。
     */
    @PostMapping("/order/normal")
    public ResponseEntity<ResponseObject> createAnOrderFromNormalSalesEvent(@RequestBody JsonNode param, @RequestAttribute(value = "myContext") MyRequestContext myRequestContext) throws OrderException {
        myRequestContext.setAuthenticatedMemberId(DemoMember.CUSTOMER.getId()); // TODO 此API的請求者就是買家，目前先寫死，所以前端也不用傳這個參數
        return ResponseEntity.ok(normalOrderService.createOrder(param, myRequestContext));
    }

    /**
     * 當賣家出貨，買家確認收到後，由賣家將訂單的狀態設為已完成。
     * 詳細的交易流程就不設計了，先做成這樣。
     */
    @PostMapping("/finish-order")
    public ResponseEntity<ResponseObject> finishOrder(@RequestBody JsonNode param, @RequestAttribute(value = "myContext") MyRequestContext myRequestContext) throws OrderException {
        myRequestContext.setAuthenticatedMemberId(DemoMember.SELLER.getId()); // TODO 此API的請求者就是賣家，目前先寫死，所以前端也不用傳這個參數
        return ResponseEntity.ok(normalOrderService.finishOrder(param, myRequestContext));
    }

    /**
     * 下架某個一般銷售案件。
     * 被下架的銷售案件，將不會顯示在案件列表的頁面上，會員也就不會點選。
     */
    @PostMapping("/sale-event/update-state")
    public ResponseEntity<ResponseObject> updateNormalSalesEventState(@RequestBody JsonNode param, @RequestAttribute(value = "myContext") MyRequestContext myRequestContext) throws OrderException {
        myRequestContext.setAuthenticatedMemberId(DemoMember.SELLER.getId()); // TODO 此API的請求者是賣家，目前先寫死，所以前端也不用傳這個參數
        return ResponseEntity.ok(allSaleEventService.updateStateOfNormalSaleEvent(param, myRequestContext));
    }

    /**
     * 初始化redis和MySql，方便DEMO。
     */
    @GetMapping("/system/init")
    public ResponseEntity<ResponseObject> initRedisAndMySql() {
        return ResponseEntity.ok(systemService.initAllDBAndRedis());
    }

}
