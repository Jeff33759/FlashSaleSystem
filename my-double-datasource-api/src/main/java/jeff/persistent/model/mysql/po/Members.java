package jeff.persistent.model.mysql.po;

import javax.persistence.*;
import java.io.Serializable;
import java.sql.Timestamp;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "members")
//@Data //lombok的這東西跟@OneToMany衝突
public class Members implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String name;

    /**
     * 1:買家(只能下單)，2:賣家(只能發單)
     * */
    private Integer role;

    /**
     * 1:啟用狀態，2:黑名單狀態，3:軟刪除狀態(凍結)
     * */
    private Integer status;

    @Column(name = "create_time")
    private Timestamp createTime;

    /**
     * 一對多的關聯設置，把主控方交給sellerMember
     * 每當我查詢某會員時，不一定每次都要查出該會員發佈那些商品，只有特定頁面才要，所以設置成LAZY
     */
    @OneToMany(fetch = FetchType.LAZY ,mappedBy = "sellerMember", cascade = CascadeType.ALL)
    private Set<Goods> goodsSet = new HashSet<>();


    public Integer getId() {
        return id;
    }

    public Members setId(Integer id) {
        this.id = id;
        return this;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getRole() {
        return role;
    }

    public void setRole(Integer role) {
        this.role = role;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Timestamp getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Timestamp createTime) {
        this.createTime = createTime;
    }

    public Set<Goods> getGoodsSet() {
        return goodsSet;
    }

    public void setGoodsSet(Set<Goods> goodsSet) {
        this.goodsSet = goodsSet;
    }
}