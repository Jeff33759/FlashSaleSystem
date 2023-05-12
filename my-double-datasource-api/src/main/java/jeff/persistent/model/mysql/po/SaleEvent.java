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

    @JoinColumn(name ="m_id")
    @ManyToOne //預設飢餓載入，因為查詢銷售案件的場景，幾乎都會需要member，所以設飢餓載入，避免N+1 Query
    private Members member;

    @JoinColumn(name ="g_id")
    @ManyToOne //預設飢餓載入，因為查詢銷售案件的場景，幾乎都會需要member，所以設飢餓載入，避免N+1 Query
    private Goods goods;

    /**
     * 1:一般銷售案件，2:快閃銷售案件
     */
    private int type;

    /**
     * 1: 上架中，2:下架中
     * */
    private Integer status;

    @Column(name = "start_time")
    private Timestamp startTime;

}