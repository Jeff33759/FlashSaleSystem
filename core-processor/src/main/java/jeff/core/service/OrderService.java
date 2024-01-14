package jeff.core.service;

import com.fasterxml.jackson.databind.JsonNode;
import jeff.common.entity.bo.MyRequestContext;
import jeff.common.entity.dto.send.ResponseObject;
import jeff.core.exception.OrderException;

public interface OrderService {

    /**
     * 新增一筆訂單。
     */
    ResponseObject createOrder(JsonNode param, MyRequestContext context) throws OrderException;

    /**
     * 完成一筆訂單。
     */
    ResponseObject finishOrder(JsonNode param, MyRequestContext context);
}
