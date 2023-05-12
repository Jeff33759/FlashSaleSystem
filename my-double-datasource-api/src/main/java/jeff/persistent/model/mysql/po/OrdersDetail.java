package jeff.persistent.model.mysql.po;

import lombok.Data;
import lombok.experimental.Accessors;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "orders_detail")
@Data
@Accessors(chain = true) //lombok支援建構子鏈式賦值
public class OrdersDetail implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @JoinColumn(name ="o_id")
    @ManyToOne(fetch = FetchType.LAZY) //預設飢餓載入，改延遲
    private Orders orders;

    @JoinColumn(name ="g_id")
    @ManyToOne //預設飢餓載入，改延遲
    private Goods goods;

    /**
     * 欲下訂的數量。
     */
    @Column(name = "quantity")
    private Integer quantity;

}
