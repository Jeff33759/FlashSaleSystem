package jeff.core.service;

import com.fasterxml.jackson.databind.JsonNode;
import jeff.common.entity.bo.MyContext;
import jeff.common.entity.dto.send.ResponseObject;
import jeff.common.exception.MyException;
import jeff.common.interfaces.IOrderService;
import org.springframework.stereotype.Component;

/**
 * 快閃銷售案件的訂單服務器。
 * 因為有導入MQ，所以訂單的處理本身不會是高併發場景，只有消費快閃銷售案件(搶購門票)是高併發，但搶完門票後成立訂單的一系列流程都不是高併發，因為利用MQ削峰了。
 */
@Component("flashOrderService")
public class FlashOrderService implements IOrderService {


    /**
     * TODO 目前因為還沒做認證相關的邏輯，所以下單的買家與賣家的Id都先寫死，外部就先不用傳了。
     *
     * @param param 範例資料: {"id":"65db8bab8ffcd84a6d4906e2","fseId":1,"transNum":3,"isConsumed":true,"smid":2,"gid":3,"cmid":1}
     */
    @Override
    public ResponseObject createOrder(JsonNode param, MyContext context) throws MyException {
        System.out.println(param);
        return null;
    }

    @Override
    public ResponseObject finishOrder(JsonNode param, MyContext context) throws MyException {
        return null;
    }
}
