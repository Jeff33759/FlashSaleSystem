package jeff.core.service;

import com.fasterxml.jackson.databind.JsonNode;
import jeff.common.entity.bo.MyContext;
import jeff.common.entity.bo.MyMQConsumptionContext;
import jeff.common.entity.dto.receive.ResponseObjectFromInnerSystem;
import jeff.common.exception.MyException;
import jeff.common.interfaces.IOrderService;
import jeff.common.util.LogUtil;
import jeff.core.entity.bo.OrderCreationFlowContext;
import jeff.core.exception.OrderException;
import jeff.core.manager.OrderManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Map;

/**
 * 快閃銷售案件的訂單服務器。
 * 因為有導入MQ，所以訂單的處理本身不會是高併發場景，只有消費快閃銷售案件(搶購門票)是高併發，但搶完門票後成立訂單的一系列流程都不是高併發，因為利用MQ削峰了。
 */
@Slf4j
@Service("flashOrderService")
public class FlashOrderService implements IOrderService {

    @Autowired
    private OrderManager orderManager;

    @Autowired
    private LogUtil logUtil;

    /**
     * 快閃銷售案件，限制玩家一個請求只能買一件商品，所以param沒有下定數量。
     *
     * @param param 範例資料: {"id":"65db8bab8ffcd84a6d4906e2","fseId":1,"transNum":3,"isConsumed":true,"smid":2,"gid":3,"cmid":1}
     * @param context 實際上應是MyMQConsumptionContext的實例
     * @return 固定回傳null(因為消費MQ的場合不會用到)
     */
    @Override
    public ResponseObjectFromInnerSystem createOrder(JsonNode param, MyContext context) throws MyException {
        MyMQConsumptionContext mqConsumptionContext = (MyMQConsumptionContext) context;

        OrderCreationFlowContext orderCreationFlowContext = this.generateContextForOrderCreationFlowByParam(param); // 這個context是for訂單流程的，作用域跟MyMQConsumptionContext不同

        try{
            int newOrderId = orderManager.startOrderCreationFlow(orderCreationFlowContext);

            logUtil.logInfo(
                    log,
                    logUtil.composeLogPrefixForBusiness(null, mqConsumptionContext.getUUID()),
                    String.format("Order created successfully, orderId: %s", newOrderId)
            );

            return null;
        } catch (DataAccessException dae) { //Spring JDBC當操作DB遇到問題時會拋出的例外的基類，先印log後，統一包裝成OrderException
            // 因為MQConsumer那邊有捕捉例外並且印log了，所以這裡就直接拋出例外，不印log。
            throw new OrderException("Some errors occurred when creating order.", dae); //會在此捕捉的，都是一些沒有預期到的DB相關的例外，前面有預期的例外，就會先包成OrderException了。
        }
    }

    @Override
    public ResponseObjectFromInnerSystem finishOrder(JsonNode param, MyContext context) throws MyException {
        throw new MyException("Unexpected invoke of FlashOrderService.finishOrder.");
    }

    /**
     * 進入成立訂單的流程前，預先將參數處理成新增訂單流程所需的Context物件。
     */
    private OrderCreationFlowContext generateContextForOrderCreationFlowByParam(JsonNode param) {
        Map<Integer, Integer> idToQuantityMap = Collections.singletonMap(param.get("gid").asInt(), 1); //欲下訂商品ID-欲下購數量的Map。快閃銷售案件規定一個下訂只能購買一件限量商品，所以數量寫死為1

        return new OrderCreationFlowContext(idToQuantityMap, param.get("smid").asInt(), param.get("cmid").asInt())
                .setFlashSaleEventLogId(param.get("id").asText()); // mongo的flash_sale_event_log的主鍵
    }

}
