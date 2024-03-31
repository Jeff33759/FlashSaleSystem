package jeff.gateway.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import jeff.common.consts.ResponseCode;
import jeff.common.entity.bo.MyRequestContext;
import jeff.common.entity.dto.outer.OuterCommunicationDto;
import jeff.common.exception.MyException;
import jeff.common.exception.MyInnerCommunicationStatusFailureException;
import jeff.common.util.LogUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.net.URI;

/**
 * 把接到下游Server的回應，處理成gateway對外回應。
 */
@Slf4j
@Component
public class MyOuterHttpCommunicationUtil {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private LogUtil logUtil;


    /**
     * fallback都是回同樣的東西，所以就用同實例。
     * gateWay對外回應這個，有幾種情況:
     *
     * 1、當接到下游Server的服務降級(status=429)
     * 2、當gateway斷路器開啟
     */
    private OuterCommunicationDto responseEntityForFallback;

    /**
     * 當遭遇一些超時的例外。
     * gateWay對外回應這個，有幾種情況:
     *
     * 1、當gateway調用下游Server遭遇timeout
     * 2、當收到下游Server回應status=408。除了gateway以外的api鏈路任一節點發生timeout，例如下游Server呼叫下下游Server遭遇timeout，此時下游Server會回給gateway狀態碼=408
     *
     * timeout問題統一對外處理成504，因為有必要告知外部系統，這個操作不一定是成功或失敗，只是超時了。
     */
    private OuterCommunicationDto responseEntityForTimeout;

    /**
     * 當遭遇一些無法預期的錯誤。
     * gateWay對外回應這個，有幾種情況:
     *
     * 1、當接到下游Server的異常回應(status != 200 && status != 408 && status!=429)
     * 2、當過濾鏈中任一地方拋錯
     */
    private OuterCommunicationDto responseEntityForError;

    /**
     * 等Spring容器完成啟動後，確定各元件都註冊完畢不會有NULL，再對EMPTY_CONTENT賦值。
     */
    @PostConstruct
    private void initVariableAfterTheSpringApplicationStartup() {
        this.responseEntityForFallback = new OuterCommunicationDto(ResponseCode.TooManyReq.getCode(), objectMapper.createObjectNode(), "Server is busy, please try again later.");

        this.responseEntityForTimeout = new OuterCommunicationDto(HttpStatus.GATEWAY_TIMEOUT.value(), objectMapper.createObjectNode(), HttpStatus.GATEWAY_TIMEOUT.getReasonPhrase());

        this.responseEntityForError = new OuterCommunicationDto(ResponseCode.Failure.getCode(), objectMapper.createObjectNode(), "Some errors occurred while processing request, please call the application owner.");
    }


    /**
     * @param requestUriToDownstreamServer gateway發給下游Server的一些請求資訊
     */
    public OuterCommunicationDto logExceptionAndGetOuterCommunicationDto(Exception exception, MyRequestContext myContext, URI requestUriToDownstreamServer) {

        //防呆
        if(exception == null) {
            logUtil.logError(
                    log,
                    logUtil.composeLogPrefixForBusiness(myContext.getAuthenticatedMemberId(), myContext.getUUID()),
                    "Unexpected error occurred during myDefaultFallback execution.",
                    new MyException("The exception is null but it should not occurred.")
            );

            return this.responseEntityForError;
        }

//      通常進到handler都會有cause，除非在過濾鏈中插入自己的過濾器，然後於該過濾器拋自己的例外，而該例外沒有cause
        Throwable causeEx = exception.getCause() == null ? exception : exception.getCause();

        //斷路器開啟中，沒設CB的router就必定不會進到這裡面
        if(causeEx instanceof CallNotPermittedException) {
            logUtil.logDebug(
                    log,
                    logUtil.composeLogPrefixForBusiness(myContext.getAuthenticatedMemberId(), myContext.getUUID()),
                    causeEx.getMessage()
            );

            return responseEntityForFallback;
        }

//      gateway調用下游Server遭遇Timeout
        if (causeEx instanceof org.springframework.cloud.gateway.support.TimeoutException //由NettyRoutingFilter拋的，若router沒有設CB，那到這一層就是這個
                || causeEx instanceof java.util.concurrent.TimeoutException) { // 若router有設CB，那到這一層就是這個。
            logUtil.logWarn(
                    log,
                    logUtil.composeLogPrefixForBusiness(myContext.getAuthenticatedMemberId(), myContext.getUUID()),
                    String.format(
                            "Call other service timeout, accessUrl: %s, reason: %s",
                            requestUriToDownstreamServer.toASCIIString(), //gateway發到哪個下游位址
                            causeEx.getMessage()
                    )
            );

            return responseEntityForTimeout;
        }

        //下游傳來服務降級 or 錯誤狀態碼
        if(causeEx instanceof MyInnerCommunicationStatusFailureException) {
            MyInnerCommunicationStatusFailureException micsfe = (MyInnerCommunicationStatusFailureException) exception;

            switch (micsfe.getHttpStatusFromInnerSystem()) {
                case REQUEST_TIMEOUT: //Api鏈路中任一節點遭遇timeout
                    logUtil.logWarn(
                            log,
                            logUtil.composeLogPrefixForBusiness(myContext.getAuthenticatedMemberId(), myContext.getUUID()),
                            "The timeout error occurred in the API chain."
                    );

                    return responseEntityForTimeout;
                case TOO_MANY_REQUESTS: //下游Server回傳服務降級
                    logUtil.logDebug(
                            log,
                            logUtil.composeLogPrefixForBusiness(myContext.getAuthenticatedMemberId(), myContext.getUUID()),
                            "Receive fallback from downstream-server."
                    );

                    return responseEntityForFallback;
                default:
                    logUtil.logWarn(
                            log,
                            logUtil.composeLogPrefixForBusiness(myContext.getAuthenticatedMemberId(), myContext.getUUID()),
                            String.format("Receive unexpected http status code from downstream-server, httpStatusCode: %s", micsfe.getHttpStatusFromInnerSystem().value())
                    );

                    return responseEntityForError;
            }
        }

        //其他未預期的錯誤
        logUtil.logError(
                log,
                logUtil.composeLogPrefixForBusiness(myContext.getAuthenticatedMemberId(), myContext.getUUID()),
                "Some errors occurred during myDefaultFallback execution.",
                (Exception) causeEx
        );

        return responseEntityForError;
    }


}
