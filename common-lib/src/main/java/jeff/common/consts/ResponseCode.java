package jeff.common.consts;

/**
 * 回應的狀態碼。
 */
public enum ResponseCode {

    Successful(1), //操作成功
    Failed(0); //操作失敗

    int code;

    ResponseCode(int code) {
        this.code = code;
    }

    public int getCode() {
        return this.code;
    }

}
