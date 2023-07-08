package jeff.persistent.model.mongo.dao;

import jeff.persistent.model.mongo.po.FlashSaleEventLog;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FlashSaleEventLogRepo extends MongoRepository<FlashSaleEventLog, String> {

}