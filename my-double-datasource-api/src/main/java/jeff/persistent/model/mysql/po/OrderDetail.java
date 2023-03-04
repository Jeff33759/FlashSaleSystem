package jeff.persistent.model.mysql.po;

import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "order_detail")
@Data
public class OrderDetail implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Transient
    @Column(name = "o_id")
    private Integer oId;

    @JoinColumn(name ="o_id")
    @ManyToOne //預設飢餓載入
    private Order order;

    @Transient
    @Column(name = "g_id")
    private Integer gId;

    @JoinColumn(name ="g_id")
    @ManyToOne //預設飢餓載入
    private Goods goods;

}
