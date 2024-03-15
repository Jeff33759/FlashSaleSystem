package jeff.common.consts;

/**
 * 回應的狀態碼。
 *
 * 操作失敗的錯誤碼避免使用2XX。
 */
public enum ResponseCode {

    /**
     * 操作成功
     */
    Success(0),

    /**
     * 操作失敗通用碼
     */
    Failure(1),

    /**
     * 查詢時找不到資料。
     */
    NotFound(404),

    /**
     * 請求太頻繁。
     * 開啟斷路器或限流器會回此狀態碼。
     */
    TooManyReq(429),

    /**
     * 系統節點A欲訪問節點B時，遲遲沒有成功得到回應，可能是遭遇readTimeout，或者是其他各種timeout。
     * 可能原因有兩節點之間網路抖動，節點B阻塞了等等...
     * 回應這個，告訴自己的上游Server，說你發來的這個請求，我這裡處理超時了。
     */
    RequestTimeout(408);

    private final int code;

    ResponseCode(int code) {
        this.code = code;
    }

    public int getCode() {
        return this.code;
    }

}
