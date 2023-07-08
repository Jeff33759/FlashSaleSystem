package jeff.schedule.service;

import jeff.persistent.model.mongo.dao.FlashSaleEventLogRepo;
import jeff.persistent.model.mongo.po.FlashSaleEventLog;
import jeff.persistent.model.mysql.dao.FlashSaleEventDAO;
import jeff.persistent.model.mysql.po.FlashSaleEvent;
import jeff.redis.util.MyRedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 把掃描FlashSaleEvent的執行邏輯從Scheduler中拆出來。
 */
@Service
public class ScanFlashSaleEventService {

    @Autowired
    private FlashSaleEventDAO flashSaleEventDAO;

    @Autowired
    private FlashSaleEventLogRepo flashSaleEventLogRepo;

    @Autowired
    private MyRedisUtil myRedisUtil;

    /**
     * 搜尋未被掃描過的FlashSaleEvent，並且新增資料進Mongo Log表同時也put進Redis。
     *
     * @return 總共幾筆銷售案件被執行
     */
    public int executeTheProcessingFlow() {
        List<FlashSaleEvent> flashSaleEventList = flashSaleEventDAO.selectFlashSaleEventWhichIsPublicAndHasNotBeenScanned();

        Map<Integer, List<FlashSaleEventLog>> groupedFlashSaleEventLogMap = this.generateMultiFlashSaleEventLogListsGroupByFlashSaleEventId(flashSaleEventList); //分群
        Map<Integer, Instant> expirationMap = this.generateExpiredInstantMapByFlashEventId(flashSaleEventList);

        groupedFlashSaleEventLogMap.forEach((fseId, flashSaleEventLogList) -> {
            flashSaleEventLogRepo.saveAll(flashSaleEventLogList); //將資料寫入Mongo Log表
            myRedisUtil.rightPushListByKey("fse_" + fseId, flashSaleEventLogList, expirationMap.get(fseId)); //將資料寫入redis-list，以fseId為Key分群
        });

        flashSaleEventList.forEach(fse -> {
            fse.setHasBeenScanned(true); //標註此快閃銷售案件已經被掃描過
        });

        flashSaleEventDAO.saveAll(flashSaleEventList);
        return flashSaleEventList.size();
    }

    /**
     * 創建數個FlashSaleEventLogList，根據flashSaleEventId去分群，同個fseId的FlashSaleEventLogList放在同一群，並且每一群的FlashSaleEventLogList都會從頭去計數交易順序編號。
     * 這個設計的前提是，一個商品只能對應一個正在上架中的快閃銷售案件，不能夠同一個商品被發布在兩個上架中的快閃銷售案件，否則這裡分群邏輯就要再設計。
     * 之所以要分群，是為了快閃銷售案件的"消費順序編號"，每一群的消費順序編號都是獨立計算的。
     */
    private Map<Integer, List<FlashSaleEventLog>> generateMultiFlashSaleEventLogListsGroupByFlashSaleEventId(List<FlashSaleEvent> flashSaleEventList) {
        Map<Integer, List<FlashSaleEventLog>> groupsMap = new HashMap<>(); //群集Map，k=fseId

        flashSaleEventList.forEach(fse -> { //分群+製作MongoDB Log表的資料
            groupsMap.put(fse.getId(), this.generateFlashSaleEventLogListAccordingToGoodsStock(fse));
        });

        return groupsMap;
    }

    /**
     * 根據商品的庫存量，新增要存入Mongo Log表的資料。
     * 例如現在某個限量商品庫存200個，針對該商品發布了一個快閃銷售活動，這時該快閃銷售活動就必須延伸出200筆Mongo Log表資料存進DB，並且每筆資料都要從1計數交易順序編號。
     */
    private List<FlashSaleEventLog> generateFlashSaleEventLogListAccordingToGoodsStock(FlashSaleEvent flashSaleEvent) {
        Integer stockOfGoods = flashSaleEvent.getGoods().getStock();
        List<FlashSaleEventLog> flashSaleEventLogList = new ArrayList<>();

        for (int i = 1; i <= stockOfGoods; i++) {
            flashSaleEventLogList.add(
                    new FlashSaleEventLog() // 買家ID統一先不賦值，有人消費時才給。
                            .setFseId(flashSaleEvent.getId())
                            .setGId(flashSaleEvent.getGoods().getId())
                            .setSMId(flashSaleEvent.getGoods().getSellerMember().getId())
                            .setTransNum(i)
                            .setIsConsumed(false)
            );
        }

        return flashSaleEventLogList;
    }

    /**
     * @return k: fseId，v: expiredTime
     */
    private Map<Integer, Instant> generateExpiredInstantMapByFlashEventId(List<FlashSaleEvent> flashSaleEventList) {
        Map<Integer, Instant> expiredInstantMap = new HashMap<>(); //快閃案件Id-到期時間

        flashSaleEventList.forEach(fse -> {
            long expiration = fse.getEndTime().getTime();
            expiredInstantMap.put(fse.getId(), Instant.ofEpochMilli(expiration));
        });

        return expiredInstantMap;
    }

}
