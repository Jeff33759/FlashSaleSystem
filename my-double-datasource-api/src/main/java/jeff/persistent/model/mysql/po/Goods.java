package jeff.persistent.model.mysql.po;

import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "goods")
@Data
public class Goods implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /**
     * 擁有此商品的賣家的會員ID
     */
    @Transient
    @Column(name = "s_m_id")
    private Integer sMId;

    /**
     * 擁有此商品的賣家
     */
    @JoinColumn(name ="s_m_id")
    @ManyToOne //預設飢餓載入
    private Member sellerMember;

    private String name;

    private Integer stock;

    private Integer price;

}
