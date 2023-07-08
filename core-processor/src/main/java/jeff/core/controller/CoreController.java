package jeff.core.controller;

import com.fasterxml.jackson.databind.JsonNode;
import jeff.common.entity.dto.send.ResponseObject;
import jeff.core.exception.OrderException;
import jeff.core.service.OrderService;
import org.springframework.http.HttpRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * 本伺服端的入口控制器。
 * 因為不多，所以沒有再根據業務邏輯細分。
 */
@RestController
@RequestMapping(produces = org.springframework.http.MediaType.APPLICATION_JSON_VALUE)
public class CoreController {

    @Resource(name = "normalOrderService")
    private OrderService normalOrderService;

    /**
     * 客戶端一般銷售案件的商品下單時的接口。
     * 通常是購物車結帳後按下送出所打的API。
     */
    @PostMapping("/order/normal")
    public ResponseEntity<ResponseObject> createAnOrderFromNormalSalesEvent(@RequestBody JsonNode param, @RequestAttribute String UUID) throws OrderException {
        return ResponseEntity.ok(normalOrderService.createOrder(param, UUID));
    }

}
