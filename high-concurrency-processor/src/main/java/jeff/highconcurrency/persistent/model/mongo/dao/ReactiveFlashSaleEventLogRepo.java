package jeff.highconcurrency.persistent.model.mongo.dao;

import jeff.highconcurrency.persistent.model.mongo.po.ReactiveFlashSaleEventLog;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface ReactiveFlashSaleEventLogRepo extends ReactiveCrudRepository<ReactiveFlashSaleEventLog, String> {

    /**
     * 用fseId和transNum為條件，去搜尋未被消費過的FlashSaleEventLog，理論上只會有一筆符合條件，因為transNum和fseId可以視為雙主鍵。
     */
    @Query(value = "{'fse_id':?0,'trans_num':?1,'is_consumed':false}")
    Mono<ReactiveFlashSaleEventLog> selectByFseIdAndTransNumAndHasNotBeenConsumed(int fseId, int transNum);

}
