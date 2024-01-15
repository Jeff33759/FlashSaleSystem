package jeff.core.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import jeff.common.consts.ResponseCode;
import jeff.common.entity.bo.MyRequestContext;
import jeff.common.entity.dto.send.ResponseObject;
import jeff.common.util.LogUtil;
import jeff.core.entity.bo.OrderCreationFlowContext;
import jeff.common.consts.DemoMember;
import jeff.core.exception.OrderException;
import jeff.core.manager.OrderManager;
import jeff.core.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * 一般下單的情境非高併發情境，所以處理訂單的相關流程會直接訪問DB。
 */
@Slf4j
@Component("normalOrderService")
public class NormalOrderServiceImpl implements OrderService {

    @Autowired
    private OrderManager orderManager;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private LogUtil logUtil;

    /**
     * TODO 目前因為還沒做認證相關的邏輯，所以下單的買家與賣家的Id都先寫死，外部就先不用傳了。
     *
     * @param param 範例資料: {"goods_list":[{"g_id":1,"g_name":"螺絲套組","quantity":20},...]}
     */
    @Override
    public ResponseObject createOrder(JsonNode param, MyRequestContext context) throws OrderException{
        OrderCreationFlowContext orderCreationFlowContext = generateContextForOrderCreationFlowByParam(param, context); // 這個context是for訂單流程的，作用域跟MyRequestContext不同

        try{
            int newOrderId = orderManager.startOrderCreationFlow(orderCreationFlowContext);

            return new ResponseObject(ResponseCode.Successful.getCode(), objectMapper.createObjectNode().put("oId",newOrderId), "Create order successful.");
        } catch (DataAccessException dae) { //Spring JDBC當操作DB遇到問題時會拋出的例外的基類，先印log後，統一包裝成OrderException
            logUtil.logWarn(log, logUtil.composeLogPrefixForBusiness(context.getAuthenticatedMemberId(), context.getUUID()), String.format("Some errors occurred when creating order, message:%s", dae.getMessage()));
            throw new OrderException("Some errors occurred when creating order."); //會在此捕捉的，都是一些沒有預期到的DB相關的例外，前面有預期的例外，就會先包成OrderException了
        }

    }

    /**
     * TODO 目前因為還沒做認證相關的邏輯，所以賣家的Id都先寫死，外部就先不用傳了。
     *
     * @param param 範例資料: {"o_id":1}
     */
    @Override
    public ResponseObject finishOrder(JsonNode param, MyRequestContext context) {
        int oId = param.get("o_id").asInt();
        orderManager.startOrderFinishFlow(oId, context);

        return new ResponseObject(ResponseCode.Successful.getCode(), objectMapper.createObjectNode().put("o_id", oId), "Finish order successful.");
    }

    /**
     * 進入成立訂單的流程前，預先將參數處理成新增訂單流程所需的Context物件。
     * TODO 買家與賣家都先寫死。
     */
    private OrderCreationFlowContext generateContextForOrderCreationFlowByParam(JsonNode param, MyRequestContext context) {
        ArrayNode orderedGoodsList = param.withArray("goods_list");
        Map<Integer,Integer> idToQuantityMap = new HashMap<>(); //欲下訂商品ID-欲下購數量的Map

        for (JsonNode goods : orderedGoodsList) {
            idToQuantityMap.put(goods.get("g_id").asInt(), goods.get("quantity").asInt());
        }

        return new OrderCreationFlowContext(idToQuantityMap, DemoMember.SELLER.getId(), context.getAuthenticatedMemberId()); // TODO 賣家先寫死。
    }

}
