package jeff.persistent.model.mysql.po;

import javax.persistence.*;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "goods")
//@Data //lombok的這東西跟@OneToMany衝突
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

    
    public Integer getId() {
        return id;
    }

    public Goods setId(Integer id) {
        this.id = id;
        return this;
    }

    public Members getSellerMember() {
        return sellerMember;
    }

    public void setSellerMember(Members sellerMember) {
        this.sellerMember = sellerMember;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getStock() {
        return stock;
    }

    public void setStock(Integer stock) {
        this.stock = stock;
    }

    public Integer getPrice() {
        return price;
    }

    public void setPrice(Integer price) {
        this.price = price;
    }

    public Set<FlashSaleEvent> getFlashSaleEventSet() {
        return flashSaleEventSet;
    }

    public void setFlashSaleEventSet(Set<FlashSaleEvent> flashSaleEventSet) {
        this.flashSaleEventSet = flashSaleEventSet;
    }
}
