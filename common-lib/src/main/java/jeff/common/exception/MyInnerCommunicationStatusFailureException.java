package jeff.common.exception;

import jeff.common.entity.dto.inner.InnerCommunicationDto;
import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.util.Optional;

/**
 * 微服務之間溝通，收到狀態碼非2XX時要拋的異常。
 * 此例外不代表服務異常，有時候下游Server會回傳服務降級為429。
 */
@Getter
public class MyInnerCommunicationStatusFailureException extends MyException {

    private HttpStatus httpStatusFromInnerSystem;

    private Optional<InnerCommunicationDto> resObjectFromInnerSystem = Optional.empty();

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

    public MyInnerCommunicationStatusFailureException(HttpStatus httpStatusFromInnerSystem, InnerCommunicationDto resObjectFromInnerSystem) {
        super("");
        this.httpStatusFromInnerSystem = httpStatusFromInnerSystem;
        this.resObjectFromInnerSystem = Optional.ofNullable(resObjectFromInnerSystem);
    }

    public MyInnerCommunicationStatusFailureException(HttpStatus httpStatusFromInnerSystem) {
        super("");
        this.httpStatusFromInnerSystem = httpStatusFromInnerSystem;
    }

}
