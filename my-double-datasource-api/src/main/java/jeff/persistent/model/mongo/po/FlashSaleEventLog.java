package jeff.persistent.model.mongo.po;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import javax.persistence.Id;
import java.io.Serializable;

/**
 * 快閃活動狀態紀錄表，存放於MongoDB。
 * 當搶購活動被發布後，會有排程去定時掃描搶購任務的內容，此表會根據MySQL裡的商品庫存，新增對應的狀態紀錄，同時將此表的紀錄快取進Redis。
 *
 * 當有人搶購時，會做兩件事：
 * 1、redis該商品的庫存-1
 * 2、記Log
 * 3、發送MQ給訂單處理服務端
 * 發布MQ可能會發生一種情況，假設Redis的庫存50個都被搶購完了，但MQ裡卻只有45條訊息，有5條丟失了，會造成有人明明接收到搶購成功的回應，實際上訂單卻沒確實成立。
 * 這是「發布->broker」的訊息丟失，所以第二步我們記Log，並搭配RabbitMQ的Confirm設置去保證訊息可靠度；
 *
 * 訂單處理會做兩件事：
 * 1、消費MQ。
 * 2、在MySQL新增訂單紀錄，在此訂單就算是正式成立了
 * 3、當訂單紀錄寫進RDBMS後，會將此MongoDB表的is_consumed改為true。
 *
 * 消費MQ會發生一種情況，就是明明消費MQ成功了，但伺服端卻因為一些事情導致後續根本沒做，所以會變成MQ被白白消費了，該做的業務邏輯卻沒有做到。
 * 這是「broker->消費」的訊息丟失，所以我們的系統才有個排程去把MySQL的資料給讀進Mongo的紀錄表，並在第三步，當該做的業務邏輯做完時，去更新紀錄表的is_consumed。
 * 此表的定位就像是狀態紀錄，紀錄一筆快閃訂單的狀態。所以當上述「broker->消費」的訊息丟失真的發生時，也可以拿快閃搶購活動的ID來搜尋此表，搜出is_consumed為false的，代表是有問題的快閃搶購紀錄，可以人為去處理完成訂單。
 *
 *
 * 因此表對於Schema沒有強烈要求，之後可能會根據業務需求而增減欄位，所以使用MongoDB來持久化。
 * 「發布->broker」的流程所以不用Mongo而是單純記Log，是因為認為RabbitMQ的Confirm設置已經足以保證「發布->broker」的訊息可靠度，所以選擇只記Log減少阻塞，盡量快速回應給客戶端。
 * 「broker->消費」的流程之所以用Mongo而不是單純記Log，是為了方便去下Where條件搜尋出對應的資料，且這個環節已經不是高併發場景，相對而言較不在意寫DB動作的阻塞。
 * */
@Document(collection = "flash_sale_event_log")
@Data
@Accessors(chain = true) //lombok支援建構子鏈式賦值
public class FlashSaleEventLog implements Serializable {

    @Id
    private String id;

    /**
     * 銷售案件表的ID。
     */
    @Field("fse_id")
    private Integer fseId;

    @Field("g_id")
    private Integer gId;

    /**
     * 賣家ID
     */
    @Field("s_m_id")
    private Integer sMId;

    /**
     * 買家ID
     */
    @Field("c_m_id")
    @JsonInclude(JsonInclude.Include.NON_NULL) //若沒被賦值就不被ObjectMapper序列化
    private Integer cMId;

    /**
     * 消費順序編號，標示這個快閃銷售案件是第幾個被消費的。
     * 例如現在發布一個快閃銷售案件，商品庫存數有50個，那麼在本表就會多出50筆資料，每筆資料代表一個庫存，本欄位代表的就是該庫存是50總庫存個裡面的第幾個。
     */
    @Field("trans_num")
    private Integer transNum;

    /**
     * T:已被消費；F:未被消費
     * */
    @Field("is_consumed")
    private Boolean isConsumed;

}
