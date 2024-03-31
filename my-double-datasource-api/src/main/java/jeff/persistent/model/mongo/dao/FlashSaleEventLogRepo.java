package jeff.persistent.model.mongo.dao;

import jeff.persistent.model.mongo.po.FlashSaleEventLog;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface FlashSaleEventLogRepo extends MongoRepository<FlashSaleEventLog, String> {

    /**
     * 根據fseId和isConsumed的狀態，刪除符合條件的資料。
     */
    @Transactional
    @Modifying
    @Query(value = "db_dev_flash_sale_mongo.flash_sale_event_log.deleteMany({\"fse_id\":?1,\"is_consumed\":?2})", nativeQuery = true)
    void deleteByFseIdAndIsConsumed(int fseId, boolean isConsumed);

}