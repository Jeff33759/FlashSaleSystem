package jeff.persistent.model.mysql.po;

import lombok.Data;
import lombok.experimental.Accessors;

import javax.persistence.*;
import java.io.Serializable;
import java.sql.Timestamp;
import java.util.LinkedHashSet;
import java.util.Set;

@Entity
@Table(name = "orders")
@Data
@Accessors(chain = true) //lombok支援建構子鏈式賦值
public class Orders implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /**
     * 賣家
     */
    @JoinColumn(name ="s_m_id")
    @ManyToOne //預設飢餓載入，因查詢訂單的場景，幾乎都會使用此變數，所以設成飢餓載入，避免N+1 Query
    private Members sellerMember;

    /**
     * 買家
     */
    @JoinColumn(name ="c_m_id")
    @ManyToOne //預設飢餓載入，因查詢訂單的場景，幾乎都會使用此變數，所以設成飢餓載入，避免N+1 Query
    private Members customerMember;

    private Integer total;

    /**
     * 訂單狀態。1:進行中，2:已完成，3:已取消
     * */
    private Integer status;

    @Column(name = "create_time")
    private Timestamp createTime;

    /**
     * flash_sale_event_log搶購Log表的對應id(如果有的話)，來自MongoDB
     * */
    @Column(name = "fsel_id")
    private String fselId;

    /**
     * 為了讓查詢訂單時能夠順便抓出關聯的Detail。
     */
    @OneToMany(mappedBy = "orders", cascade = CascadeType.ALL) //預設延遲載入
    private Set<OrdersDetail> ordersDetails = new LinkedHashSet<OrdersDetail>();

}