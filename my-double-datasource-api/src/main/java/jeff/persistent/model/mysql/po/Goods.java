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

    @Transient
    @Column(name = "m_id")
    private Integer mId;

    @JoinColumn(name ="m_id")
    @ManyToOne //預設飢餓載入
    private Member member;

    private String name;

    private Integer storage;

    private Integer price;

}
