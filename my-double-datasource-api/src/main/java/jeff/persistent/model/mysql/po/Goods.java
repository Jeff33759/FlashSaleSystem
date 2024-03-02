package jeff.persistent.model.mysql.po;

import lombok.Data;
import lombok.experimental.Accessors;

import javax.persistence.*;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "goods")
@Data
@Accessors(chain = true) //lombok支援建構子鏈式賦值
public class Goods implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /**
     * 擁有此商品的賣家
     */
    @JoinColumn(name ="s_m_id")
    @ManyToOne //預設飢餓載入，因為查詢商品的時候，大多都會想要一起知道賣家，避免N+1 Query
    private Members sellerMember;

    private String name;

    private Integer stock;

    private Integer price;

    /**
     * 一對多的關聯設置，把主控方交給goods。
     * 當我查詢一個商品時，不一定要查出此商品被發佈到哪些快閃銷售案件，所以使用預設的LAZY。
     *
     * 一個商品，可以被發佈到多個快閃銷售案件。
     */
    @OneToMany(mappedBy = "fseGoods", cascade = CascadeType.ALL) // 預設延遲載入
    private Set<FlashSaleEvent> flashSaleEventSet = new HashSet<>();

}
