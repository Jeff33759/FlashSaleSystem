package jeff.persistent.model.mysql.po;

import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;
import java.util.LinkedHashSet;
import java.util.Set;

@Entity
@Table(name = "order")
@Data
public class Order implements Serializable {

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
    @Column(name = "fs_id")
    private Integer fsId;

    @JoinColumn(name ="fs_id")
    @ManyToOne //預設飢餓載入
    private FlashSaleEvent flashSaleEvent;

    private Integer total;

    /**
     * 訂單狀態。1:進行中，2:已完成，3:已取消
     * */
    private Integer status;

    @Column(name = "create_time")
    private long createTime;

    /**
     * flash_sale_temp_record搶購臨時表的對應id(如果有的話)，來自MongoDB
     * */
    @Column(name = "fstr_id")
    private String fstrId;

    /**
     * 為了讓查詢訂單時能夠順便抓出關聯的Detail。
     */
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL) //預設延遲載入
    private Set<OrderDetail> orderDetails = new LinkedHashSet<OrderDetail>();

}