package jeff.core.manager;

import jeff.common.entity.bo.MyRequestContext;
import jeff.common.util.LogUtil;
import jeff.core.entity.bo.OrderCreationFlowContext;
import jeff.core.exception.OrderException;
import jeff.persistent.model.mysql.dao.GoodsDAO;
import jeff.persistent.model.mysql.dao.OrderDAO;
import jeff.persistent.model.mysql.dao.OrderDetailDAO;
import jeff.persistent.model.mysql.po.Goods;
import jeff.persistent.model.mysql.po.Members;
import jeff.persistent.model.mysql.po.Orders;
import jeff.persistent.model.mysql.po.OrdersDetail;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.*;

/**
 * OrderService與相關DAO的中間層，讓業務邏輯別下沉到DAO。
 */
@Slf4j
@Component
public class OrderManager {

    @Autowired
    private OrderDAO orderDAO;

    @Autowired
    private GoodsDAO goodsDAO;

    @Autowired
    private OrderDetailDAO orderDetailDAO;


    /**
     * 成立訂單的流程。
     * 要求原子性，過程中有任一步失敗，就全部都要回滾，所以整個流程會一直占用交易。
     * 最上層方法要設為public，spring AOP才會奏效，Transactional才有用。
     *
     * @return 新建立的orderId
     */
    @Transactional(rollbackFor = Exception.class) //rollbackFor若不寫，預設只會針對RuntimeException去回滾
    public int startOrderCreationFlow(OrderCreationFlowContext context) throws OrderException {
        this.checkThatAllGoodsAreInStockInDBAndCalculateTotalAmount(context);
        this.destock(context);
        this.insertANewOrderIntoDB(context);
        this.insertOrdersDetailsIntoDB(context);

        return context.getNewOrderId();
    }

    /**
     * 完成訂單的流程。
     */
    public void startOrderFinishFlow(int orderId, MyRequestContext context) {
        //這裡省略對sellerMemberId的檢查，不想在update前又去select一次，取而代之的是過濾練會LOG，就算前端送錯封包也有得查

        orderDAO.updateStatusById(orderId, 2);
    }


    /**
     * 檢查所有欲下訂的商品是否還有現貨。
     * 先檢查，有現貨才往下去做insert的流程，以免動不動就Rollback損耗效能。
     *
     * 途中順便去計算訂單的總金額，就不用之後還要再遍歷一次。
     */
    private void checkThatAllGoodsAreInStockInDBAndCalculateTotalAmount(OrderCreationFlowContext context) throws OrderException {
        Map<Integer, Integer> goodsIdToQuantityMap = context.getGoodsIdToQuantityMap();

        List<Goods> goodsListFromDB = goodsDAO.findAllById(goodsIdToQuantityMap.keySet());

        goodsListFromDB.forEach(goods -> {
            int quantityToBeOrdered = goodsIdToQuantityMap.get(goods.getId());
            if (quantityToBeOrdered > goods.getStock()) {
                throw new OrderException(String.format("The stock of goods-id:%d is not enough.", goods.getId()));
            }

            context.calculateTotalAmount(goods.getPrice() * quantityToBeOrdered);
        });
    }


    /**
     * 減去庫存。
     * 針對所有欲下訂的商品，從DB中扣除欲下定的數量。
     * <p>
     * 很不想把executeUpdate的動作寫在for迴圈裡，但找不到更好的做法，JPA不像JDBC，可以組織好完整的SQL語句後再一起執行。
     * 之所以下原生SQL而非用物件的方式，是因為物件沒辦法實現「storage = storage-?」的操作，除非先查詢一次，查詢出商品資料後，再針對庫存欄位減去欲下定的數量，
     * 但那本質上寫進DB端並不是「storage = storage-?」的操作，而是「「storage = ?」的操作，
     * 所以若「查詢 -> 減去數量」的途中，其他執行緒搶先執行了減去數量就會發生一些問題，例如明明現實庫存已經為0卻被再次消費成功，因此並不可行。
     * 減去庫存的動作，必須由DB端去執行「storage = storage-?」的指令，才能確保庫存資料安全性。
     * <p>
     * 目前的做法經實測，若某一輪迴圈出錯，那麼所有資料都會RollBack，因此至少DB的庫存資料是安全的，不會有扣除錯誤的問題。
     * 至於如何解決效能(也就是別每一輪迴圈都executeUpdate一次)? 也許使用JDBC或者是MyBatis來做這個Batch Update/Insert的邏輯會更好一些，
     * 兩者都有辦法去動態組織並執行SQL語句，後者更是為了動態組織並執行SQL語句而存在的框架。
     * <p>
     * 雖然外面那一層有@Transactional先從連接池中取走一個與DB的連接，但也不曉得JPA(預設實作為Hibernate)底層是怎麼去實作相關執行的，
     * 也許把executeUpdate寫在迴圈裡也沒關係?因為@Transactional會最後才一次提交所有操作；
     * 又或者根本不會一次提交所有操作，會真的每次呼叫executeUpdate就跟DB互動一次，這樣效能就很不好，目前印出sql log，推測應該是後者，效能會很差。
     *
     * 總之JPA特性如何待釐清，不過目前更傾向用JDBC或者MyBatis去做動態SQL，先用字串組織好多個UPDATE，再一次executeUpdate。
     *
     * @throws DataAccessException 若DB庫存數量小於欲扣除的數量，因為DB有將欄位設置為不能小於0，所以執行時會跳錯。
     */
    private void destock(OrderCreationFlowContext context) throws DataAccessException {
        Map<Integer, Integer> goodsIdToQuantityMap = context.getGoodsIdToQuantityMap();

        goodsIdToQuantityMap.forEach((gId, gQuantity) -> {
            goodsDAO.destock(gQuantity, gId);
        });
    }

    /**
     * 在訂單表中插入一筆新訂單資料，並對context的OrderId賦值。
     */
    private void insertANewOrderIntoDB(OrderCreationFlowContext context) {
        Orders order = new Orders()
                .setSellerMember(new Members().setId(context.getSellerMId())) //因為用了@Transient，所以要setMenbers物件
                .setCustomerMember(new Members().setId(context.getCustomerMId()))
                .setTotal(context.getTotal())
                .setStatus(1)
                .setCreateTime(new Timestamp(System.currentTimeMillis()));

        Orders newOrder = orderDAO.save(order);
        context.setNewOrderId(newOrder.getId());
    }

    /**
     * 新增訂單明細表。
     */
    private void insertOrdersDetailsIntoDB(OrderCreationFlowContext context) {
        Map<Integer, Integer> goodsIdToQuantityMap = context.getGoodsIdToQuantityMap();
        ArrayList<OrdersDetail> odList = new ArrayList<>();

        goodsIdToQuantityMap.forEach((gId, gQuantity) -> {
            OrdersDetail orderDetail = new OrdersDetail()
                    .setOrders(new Orders().setId(context.getNewOrderId()))
                    .setGoods(new Goods().setId(gId))
                    .setQuantity(gQuantity);

            odList.add(orderDetail);
        });

        orderDetailDAO.saveAll(odList); // JPA實現批次新增，效能略輸JDBC，但程式碼較簡潔
    }


}