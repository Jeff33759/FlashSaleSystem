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
     */
    TooManyReq(429);

    int code;

    ResponseCode(int code) {
        this.code = code;
    }

    public int getCode() {
        return this.code;
    }

}
