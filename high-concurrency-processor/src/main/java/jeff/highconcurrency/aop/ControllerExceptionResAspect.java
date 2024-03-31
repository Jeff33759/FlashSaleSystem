package jeff.highconcurrency.aop;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jeff.common.consts.ResponseCode;
import jeff.common.entity.dto.inner.InnerCommunicationDto;
import jeff.common.exception.MyException;
import jeff.common.exception.MyInnerCommunicationStatusFailureException;
import jeff.common.util.LogUtil;
import jeff.common.exception.BusyException;
import jeff.highconcurrency.exception.FlashSaleEventConsumeException;
import jeff.highconcurrency.http.feign.config.ReactiveFeignConfigForDefault;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import reactivefeign.client.ReactiveFeignException;
import reactivefeign.client.ReadTimeoutException;

import javax.annotation.PostConstruct;

/**
 * 切面程式，攔截控制器所拋出的例外，根據例外去回應給客戶端不同的訊息。
 *
 *  <p><del>遭遇錯誤，統一也回Http Code200，會有自訂義的code去表示請求的狀態。</del></p> <---如果這樣做，那麼上游Server的resilience4j不會把錯誤的請求算到斷路器count裡面。
 *  遭遇非預期錯誤時，回應Http狀態碼為非2XX，預期內錯誤則回200。兩者差別在於會不會觸發上游Server的斷路器。
 */
@Slf4j
@RestControllerAdvice(basePackages = {"jeff.highconcurrency.controller"})
public class ControllerExceptionResAspect {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private LogUtil logUtil;

    /**
     * 不成功的操作目前定義大多數都是回空的content，因為很常用，所以做成單例常數，節省效能與記憶體空間。
     */
    private JsonNode EMPTY_CONTENT;

    /**
     * 等Spring容器完成啟動後，確定各元件都註冊完畢不會有NULL，再對EMPTY_CONTENT賦值。
     */
    @PostConstruct
    private void initVariableAfterTheSpringApplicationStartup() {
        EMPTY_CONTENT = objectMapper.createObjectNode();
    }

    @ExceptionHandler(value = FlashSaleEventConsumeException.class)
    @ResponseStatus(code = HttpStatus.OK)
    public InnerCommunicationDto handleFlashSaleEventConsumeException(FlashSaleEventConsumeException fsece) {
        return new InnerCommunicationDto(ResponseCode.Failure.getCode(), EMPTY_CONTENT, fsece.getMessage());
    }

    @ExceptionHandler(value = BusyException.class)
    @ResponseStatus(code = HttpStatus.TOO_MANY_REQUESTS)
    public InnerCommunicationDto handleBusyException(BusyException be) {
        return new InnerCommunicationDto(ResponseCode.TooManyReq.getCode(), EMPTY_CONTENT, be.getMessage());
    }

    /**
     * 當下游Server回應狀態碼非2XX，會在{@link ReactiveFeignConfigForDefault}處理成MyInnerCommunicationStatusFailureException，再由這邊處理。
     */
    @ExceptionHandler(value = MyInnerCommunicationStatusFailureException.class)
    public ResponseEntity<InnerCommunicationDto> handleMyInnerCommunicationStatusFailureException(MyInnerCommunicationStatusFailureException micsfe) {
        return ResponseEntity
                .status(micsfe.getHttpStatusFromInnerSystem())
                .body(micsfe.getResObjectFromInnerSystem().orElseThrow(() -> new MyException("InnerCommunicationDto field of MyInnerCommunicationStatusFailureException is empty, but this shouldn't happen in this server.")));
    }

    /**
     * 承接一些ReactiveFeign所拋的例外，例如遭遇超時等等。
     * 應該是可以在某層Handler處理成自己的例外，再由這個aop捕捉，但懶得再看Source Code了。
     */
    @ExceptionHandler(value = ReactiveFeignException.class)
    public ResponseEntity<InnerCommunicationDto> handleReactiveFeignException(ReactiveFeignException rfe) {
        logUtil.logError(
                log,
                logUtil.composeLogPrefixForSystem(),
                "Some errors occurred at httpClient.",
                rfe
        );

        if(rfe instanceof ReadTimeoutException) {
            return ResponseEntity
                    .status(HttpStatus.REQUEST_TIMEOUT)
                    .body(new InnerCommunicationDto(ResponseCode.RequestTimeout.getCode(), EMPTY_CONTENT, "Some errors occurred while processing request, please call the application owner."));
        }

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new InnerCommunicationDto(ResponseCode.RequestTimeout.getCode(), EMPTY_CONTENT, "Some errors occurred while processing request, please call the application owner."));
    }


    /**
     * 當發生了一些沒有預期到的例外。
     */
    @ExceptionHandler(value = Exception.class)
    @ResponseStatus(code = HttpStatus.INTERNAL_SERVER_ERROR)
    public InnerCommunicationDto handleException(Exception e) {
        logUtil.logError(
                log,
                logUtil.composeLogPrefixForSystem(),
                "Some errors occurred while processing request.",
                e
        );

        return new InnerCommunicationDto(ResponseCode.Failure.getCode(), EMPTY_CONTENT, "Some errors occurred while processing request, please call the application owner.");
    }

}
