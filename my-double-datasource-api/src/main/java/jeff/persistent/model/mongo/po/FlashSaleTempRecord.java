package jeff.persistent.model.mongo.po;

import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import javax.persistence.Id;
import java.io.Serializable;

/**
 * 臨時表，存放於MongoDB。
 * 當搶購活動被發布後，會有排程去定時掃描搶購任務的內容，此表會根據MySQL裡的商品庫存，新增對應的臨時紀錄，同時將此表的紀錄快取進Redis。
 *
 * 當有人搶購時，會做兩件事：
 * 1、redis該商品的庫存-1
 * 2、此臨時表的單筆紀錄的status改為T，代表狀態是「單筆秒殺搶購成功，回應給客戶端他搶購成功了，可是後端訂單還沒有正式成立(寫進RDBMS)」
 * 3、發送MQ給訂單處理服務端
 *
 * MQ消費者負責後續訂單處理。
 * 訂單處理會做兩件事：
 * 1、在MySQL新增訂單紀錄，在此訂單就算是正式成立了
 * 2、當訂單紀錄寫進RDBMS後，會將此表的對應紀錄刪除
 *
 * 之所以要有此臨時表存在，是為了防止MQ訊息丟失，保證MQ的訊息可靠度。
 * 假設Redis的庫存50個都被搶購完了，但MQ裡卻只有45條訊息，有5條丟失了，會造成有人明明接收到搶購成功的回應，實際上訂單卻沒確實成立。
 * 為解決這問題，可以讓別的服務端去定時掃描這張臨時表，拿搶購活動的ID來搜尋。
 * 若查出某些紀錄的條件是status為T的，代表已經被秒殺搶購成功，可是訂單卻還沒有被寫進RDBMS(由於MQ丟失)，這時候再補做成立訂單的流程。
 *
 * 總而言之，此臨時表的存在意義，就是為了防止搶購活動時的MQ訊息丟失。
 * 因為臨時表的紀錄對於Schema沒有強烈要求，之後可能會根據業務需求而增減欄位，所以使用MongoDB來持久化；
 * 可是相對來說，因為用了兩個DB，所以兩個DB之間的交易就沒辦法使用同一個Manager去進行回滾。
 * 多使用一個MongoDB只為了臨時資料的短暫持久化，此做法究竟好不好? 待驗證。
 *
 * MQ要保證訊息可靠度，原生也有一些設置可以去做，例如開啟事務交易等等.....不一定要依賴其他DB。
 * */
@Document(collection = "flash_sale_temp_record")
@Data
public class FlashSaleTempRecord implements Serializable {

    @Id
    private String id;

    /**
     * 銷售案件表的ID。
     */
    @Field("se_id")
    private String seId;

    @Field("g_id")
    private String gId;

    /**
     * 賣家ID
     */
    @Field("s_m_id")
    private String sMId;

    /**
     * 買家ID
     */
    @Field("c_m_id")
    private String cMId;

    /**
     * 交易編號，標示這個快閃銷售案件是第幾個被消費的。
     * 例如現在發布一個快閃銷售案件，商品庫存數有50個，那麼在本表就會多出50筆資料，每筆資料代表一個庫存，本欄位代表的就是該庫存是50總庫存個裡面的第幾個。
     */
    @Field("trans_num")
    private String transNum;

    /**
     * T:被搶購；F:未被搶購
     * */
    private boolean status;

}
