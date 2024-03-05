package jeff.common.consts;

/**
 * 回應的狀態碼。
 */
public enum ResponseCode {

    /**
     * 操作成功
     */
    Success(1),

    /**
     * 操作失敗通用碼
     */
    Failure(0),

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
