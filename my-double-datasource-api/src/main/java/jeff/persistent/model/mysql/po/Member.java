package jeff.persistent.model.mysql.po;

import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "member")
@Data
public class Member implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String name;

    /**
     * 1:一般會員(只能下單)，2:企業主(只能發單)
     * */
    private Integer role;

    /**
     * 1:啟用狀態，2:黑名單狀態，3:軟刪除狀態(凍結)
     * */
    private Integer status;

    @Column(name = "create_time")
    private long createTime;

}