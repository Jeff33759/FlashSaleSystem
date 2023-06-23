package jeff.persistent.model.mysql.po;

import lombok.Data;

import javax.persistence.*;
import java.sql.Timestamp;

@Entity
@Table(name = "flash_sale_event")
@Data
public class FlashSaleEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @JoinColumn(name ="g_id")
    @ManyToOne //預設飢餓載入，因為查詢銷售案件的場景，幾乎都會需要member，所以設飢餓載入，避免N+1 Query
    private Goods goods;

    /**
     * t: 上架中，f:下架中
     * 預設為T。
     * */
    @Column(name = "is_public")
    private Boolean isPublic = true;

    @Column(name = "start_time")
    private Timestamp startTime;

    @Column(name = "end_time")
    private Timestamp endTime;

    /**
     * 此筆資料是否已經被排程掃描過。
     * T: 已被掃描、F: 未被掃描
     * 預設為F。
     */
    @Column(name = "has_been_scanned")
    private Boolean hasBeenScaned = false;

}
