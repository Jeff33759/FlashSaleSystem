package jeff.common.consts;

/**
 * 回應的狀態碼。
 */
public enum ResponseCode {

    Success(1), //操作成功
    Failure(0); //操作失敗

    int code;

    ResponseCode(int code) {
        this.code = code;
    }

    public int getCode() {
        return this.code;
    }

}
