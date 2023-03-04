package jeff.persistent.model.mongo.dao;

import jeff.persistent.model.mongo.po.FlashSaleTempRecord;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FlashSaleTempRecordRepo extends MongoRepository<FlashSaleTempRecord, String> {

}