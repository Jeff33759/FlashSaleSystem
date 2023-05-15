package jeff.persistent.model.mysql.po;

import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;
import java.sql.Timestamp;

@Entity
@Table(name = "sale_event")
@Data
public class SaleEvent implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @JoinColumn(name ="g_id")
    @ManyToOne //預設飢餓載入，因為查詢銷售案件的場景，幾乎都會需要goods，所以設飢餓載入，避免N+1 Query
    private Goods goods;

    /**
     * t: 上架中，f:下架中
     * */
    @Column(name = "is_public")
    private Boolean isPublic;

    @Column(name = "start_time")
    private Timestamp startTime;

}