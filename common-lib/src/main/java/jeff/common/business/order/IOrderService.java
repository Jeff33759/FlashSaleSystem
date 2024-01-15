package jeff.common.business.order;

import com.fasterxml.jackson.databind.JsonNode;
import jeff.common.entity.bo.MyRequestContext;
import jeff.common.entity.dto.send.ResponseObject;
import jeff.common.exception.MyException;

public interface IOrderService {

    /**
     * 新增一筆訂單。
     */
    ResponseObject createOrder(JsonNode param, MyRequestContext context) throws MyException;

    /**
     * 完成一筆訂單。
     */
    ResponseObject finishOrder(JsonNode param, MyRequestContext context) throws MyException;
}
