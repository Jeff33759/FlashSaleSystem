package jeff.persistent.model.mysql.po;

import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "sale_event")
@Data
public class SaleEvent implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Transient
    @Column(name = "m_id")
    private Integer mId;

    @JoinColumn(name ="m_id")
    @ManyToOne //預設飢餓載入
    private Member member;

    @Transient
    @Column(name = "g_id")
    private String gId;

    @JoinColumn(name ="g_id")
    @ManyToOne //預設飢餓載入
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
    private long startTime;

}