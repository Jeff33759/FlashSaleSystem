package jeff.persistent.model.mysql.po;

import lombok.Data;
import lombok.experimental.Accessors;

import javax.persistence.*;
import java.io.Serializable;

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
    @ManyToOne(fetch = FetchType.LAZY) //預設飢餓載入，改延遲
    private Members sellerMember;

    private String name;

    private Integer stock;

    private Integer price;

}
