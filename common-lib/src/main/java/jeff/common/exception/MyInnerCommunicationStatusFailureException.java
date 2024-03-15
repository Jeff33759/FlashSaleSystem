package jeff.common.exception;

import jeff.common.entity.dto.receive.ResponseObjectFromInnerSystem;
import lombok.Getter;

/**
 * 微服務之間溝通，收到狀態碼非2XX時要拋的異常。
 * 此例外不代表服務異常，有時候下游Server找不到資料也會傳404。
 */
@Getter
public class MyInnerCommunicationStatusFailureException extends MyException {

    private int statusCode;

    private ResponseObjectFromInnerSystem resObjectFromInnerSystem;

    /**
     * @deprecated 此方法棄用
     */
    @Deprecated
    public MyInnerCommunicationStatusFailureException(String message) {
        super(message);
    }

    /**
     * @deprecated 此方法棄用
     */
    @Deprecated
    public MyInnerCommunicationStatusFailureException(String message, Throwable cause) {
        super(message, cause);
    }

    public MyInnerCommunicationStatusFailureException(int statusCode, ResponseObjectFromInnerSystem resObjectFromInnerSystem) {
        super("");
        this.statusCode = statusCode;
        this.resObjectFromInnerSystem = resObjectFromInnerSystem;
    }

}
