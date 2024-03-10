package jeff.common.interfaces;

import com.fasterxml.jackson.databind.JsonNode;
import jeff.common.entity.bo.MyContext;
import jeff.common.entity.dto.receive.ResponseObjectFromInnerSystem;
import jeff.common.exception.MyException;

public interface IOrderService {

    /**
     * 新增一筆訂單。
     */
    ResponseObjectFromInnerSystem createOrder(JsonNode param, MyContext context) throws MyException;

    /**
     * 完成一筆訂單。
     */
    ResponseObjectFromInnerSystem finishOrder(JsonNode param, MyContext context) throws MyException;
}
