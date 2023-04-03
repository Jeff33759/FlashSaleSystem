package jeff.persistent.model.mysql.po;

import lombok.Data;
import lombok.experimental.Accessors;

import javax.persistence.*;
import java.io.Serializable;
import java.util.LinkedHashSet;
import java.util.Set;

@Entity
@Table(name = "order")
@Data
@Accessors(chain = true) //lombok支援建構子鏈式賦值
public class Order implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /**
     * 賣家ID
     */
    @Transient
    @Column(name = "s_m_id")
    private Integer sMId;

    /**
     * 賣家
     */
    @JoinColumn(name ="s_m_id")
    @ManyToOne //預設飢餓載入
    private Member sellerMember;

    /**
     * 買家ID
     */
    @Transient
    @Column(name = "c_m_id")
    private Integer cMId;

    /**
     * 買家
     */
    @JoinColumn(name ="c_m_id")
    @ManyToOne //預設飢餓載入
    private Member customerMember;


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