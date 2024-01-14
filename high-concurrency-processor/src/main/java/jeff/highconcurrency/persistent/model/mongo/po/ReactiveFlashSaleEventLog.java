package jeff.highconcurrency.persistent.model.mongo.po;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

/**
 * 同my-double-datasource-api裡的PO。
 */
@Document(collection = "flash_sale_event_log")
@Data
@Accessors(chain = true) //lombok支援建構子鏈式賦值
public class ReactiveFlashSaleEventLog {

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
