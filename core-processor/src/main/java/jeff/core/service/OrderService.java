package jeff.core.service;

import com.fasterxml.jackson.databind.JsonNode;
import jeff.common.entity.dto.send.ResponseObject;
import jeff.core.exception.OrderException;

public interface OrderService {

    /**
     * 新增一筆訂單。
     *
     * @param orderInfo 訂單資訊
     * @return
     */
    ResponseObject createOrder(JsonNode orderInfo) throws OrderException;

}
