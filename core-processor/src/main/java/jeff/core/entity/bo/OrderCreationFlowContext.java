package jeff.core.entity.bo;

import java.util.Map;

/**
 * 上下文物件，貫穿整個成立訂單的Flow。
 * 目前系統設計成，一個購物車裡面只能放同個賣家的系列商品，所以一筆訂單的賣家和買家會是固定的。
 */
public class OrderCreationFlowContext {

    private int sellerMId; //賣家的memberId。

    private int customerMId; //買家的memberId

    private Map<Integer, Integer> goodsIdToQuantityMap; //欲下訂商品ID-欲下購數量的Map

    private int total = 0; //本次交易的總金額。會在訂單處理的流程中逐漸累加。

    private int newOrderId; //成立新訂單的Id，會在訂單處理的流程中才賦值，所以給一個setter，而非建構子賦值

    /**
     * 強制外部在建立此元件時一定要賦值，且沒有setter，因為成立訂單流程中不會有重新賦值成另個Map的情形發生(更改Map裡面值的情況就另當別論)
     *
     * @param idToQuantityMap
     */
    public OrderCreationFlowContext(Map<Integer, Integer> idToQuantityMap, int sellerMId, int customerMId) {
        this.goodsIdToQuantityMap = idToQuantityMap;
        this.customerMId = customerMId;
        this.sellerMId = sellerMId;
    }

    public Map<Integer, Integer> getGoodsIdToQuantityMap() {
        return goodsIdToQuantityMap;
    }

    public int getCustomerMId() {
        return customerMId;
    }

    public int getSellerMId() {
        return sellerMId;
    }

    public int getTotal() {
        return total;
    }

    public int getNewOrderId() {
        return newOrderId;
    }

    public void calculateTotalAmount(int amount) {
        this.total += amount;
    }

    public void setNewOrderId(int newOrderId) {
        this.newOrderId = newOrderId;
    }

}
