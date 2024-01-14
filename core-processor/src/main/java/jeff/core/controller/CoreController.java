package jeff.core.controller;

import com.fasterxml.jackson.databind.JsonNode;
import jeff.common.entity.bo.MyRequestContext;
import jeff.common.entity.dto.send.ResponseObject;
import jeff.core.consts.DemoMember;
import jeff.core.exception.OrderException;
import jeff.core.service.OrderService;
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
    private OrderService normalOrderService;

    @Autowired
    private SystemService systemService;

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
     * 初始化redis和MySql，方便DEMO。
     */
    @GetMapping("/system/init")
    public ResponseEntity<ResponseObject> initRedisAndMySql() {
        return ResponseEntity.ok(systemService.initAllDBAndRedis());
    }

}
