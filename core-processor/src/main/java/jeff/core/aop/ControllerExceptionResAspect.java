package jeff.core.aop;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jeff.common.consts.ResponseCode;
import jeff.common.entity.dto.receive.ResponseObjectFromInnerSystem;
import jeff.common.util.LogUtil;
import jeff.common.exception.BusyException;
import jeff.common.exception.MyNotFoundException;
import jeff.core.exception.OrderException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.annotation.PostConstruct;

/**
 * 切面程式，攔截控制器所拋出的例外，根據例外去回應給客戶端不同的訊息。
 *
 * <p><del>遭遇錯誤，統一也回Http Code200，會有自訂義的code去表示請求的狀態。</del></p> <---如果這樣做，那麼上游Server的resilience4j不會把錯誤的請求算到斷路器count裡面。
 * 遭遇錯誤時，回應Http狀態碼為非2XX。
 */
@Slf4j
@RestControllerAdvice(basePackages = {"jeff.core.controller"})
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


    @ExceptionHandler(value = OrderException.class)
    public ResponseEntity<ResponseObjectFromInnerSystem> handleOrderException(OrderException oe) {
        return ResponseEntity
                .status(ResponseCode.Failure.getCode())
                .body(new ResponseObjectFromInnerSystem(ResponseCode.Failure.getCode(), EMPTY_CONTENT, oe.getMessage()));
    }

    @ExceptionHandler(value = MyNotFoundException.class)
    public ResponseEntity<ResponseObjectFromInnerSystem> handleMyNotFoundException(MyNotFoundException mnfe) {
        return ResponseEntity
                .status(ResponseCode.NotFound.getCode())
                .body(new ResponseObjectFromInnerSystem(ResponseCode.NotFound.getCode(), EMPTY_CONTENT, mnfe.getMessage()));
    }

    @ExceptionHandler(value = BusyException.class)
    public ResponseEntity<ResponseObjectFromInnerSystem> handleBusyException(BusyException be) {
        return ResponseEntity
                .status(ResponseCode.TooManyReq.getCode())
                .body(new ResponseObjectFromInnerSystem(ResponseCode.TooManyReq.getCode(), EMPTY_CONTENT, be.getMessage()));
    }

    /**
     * 當發生了一些沒有預期到的例外。
     */
    @ExceptionHandler(value = Exception.class)
    @ResponseStatus(code = HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseObjectFromInnerSystem handleException(Exception e) {
        logUtil.logError(
                log,
                logUtil.composeLogPrefixForSystem(),
                "Some errors occurred while processing request.",
                e
        );

        return new ResponseObjectFromInnerSystem(ResponseCode.Failure.getCode(), EMPTY_CONTENT, "Some errors occurred while processing request, please call the application owner.");
    }

}
