package jeff.common.entity.bo;

import lombok.Data;

/**
 * 自己做的Context物件，用來標示某些業務邏輯的生命週期。
 */
@Data
public abstract class MyContext {

    /**
     * 代表一個業務場景生命週期的UUID，方便日誌中心化時的搜尋(例如業務場景=WebApi時，可以用UUID去找到某個請求在業務邏輯中跑了啥方法)。
     */
    private String UUID;

}
