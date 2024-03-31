package jeff.common.entity.bo;

import lombok.Data;

/**
 * 自己做的Context物件，用來標示某些業務邏輯的生命週期。
 */
@Data
public abstract class MyContext {

    /**
     * 代表一個業務場景生命週期的UUID，方便日誌中心化時的搜尋(例如業務場景=WebApi時，可以用UUID去找到某個請求在業務邏輯中跑了啥方法)。
     * 業務邏輯有時候是誇多服務的(上游Server和下游Server)，只要是同個業務場景的鏈路，都使用同個UUID，方便之後日誌集中化，可以用同一組UUID抓出跨服務的整條業務鏈路日誌。
     */
    private String UUID;

}
