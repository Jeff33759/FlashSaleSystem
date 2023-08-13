package jeff.common.entity.bo;

import lombok.Data;

/**
 * 自己做的上下文物件，用來將一個請求的常用數值帶到往後的業務邏輯做使用，例如代表一個請求生命週期的UUID，或者是請求者的memberId等等......
 * 看是要拿去記Log還是啥的。
 */
@Data
public class MyRequestContext {

    /**
     * 代表一個請求生命週期的UUID，方便日誌中心化時的搜尋(可以用UUID去找到某個請求在業務邏輯中跑了啥方法)。
     */
    private String UUID;

    /**
     * 被認證後的會員ID。
     * 有一些API需要登入後才能使用，是專門for會員的Api。那麼經過認證流程後，會記住這個請求現在是由哪個會員發出的，方便後續業務邏輯使用。
     */
    private Integer authenticatedMemberId;

}
