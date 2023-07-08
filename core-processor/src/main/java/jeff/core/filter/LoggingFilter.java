package jeff.core.filter;


import jeff.common.util.LogUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

/**
 * 紀錄請求參數與回應的過濾器。
 */
@Slf4j
@Component
@WebFilter
@Order(10)
public class LoggingFilter extends OncePerRequestFilter {

    @Autowired
    private LogUtil logUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        ContentCachingRequestWrapper reqWrapper = (ContentCachingRequestWrapper) request;
        ContentCachingResponseWrapper resWrapper = (ContentCachingResponseWrapper) response;

        filterChain.doFilter(reqWrapper, resWrapper);

        logAPI(reqWrapper, resWrapper); //要放在doFilter下面，getContentAsByteArray才有東西
        resWrapper.copyBodyToResponse(); //若沒加這個，客戶端不會獲得body
    }

    /**
     * 針對API進行logging。
     */
    private void logAPI(ContentCachingRequestWrapper reqWrapper, ContentCachingResponseWrapper resWrapper) throws UnsupportedEncodingException {
        logUtil.logInfo(
                log,
                logUtil.composeLogPrefixForBusiness(null, reqWrapper.getAttribute("UUID").toString()),
                String.format(
                        "The info of request, clientIP: %s, method: %s, path: %s, queryString: %s, body: %s",
                        reqWrapper.getRemoteAddr(),
                        reqWrapper.getMethod(),
                        reqWrapper.getServletPath(),
                        decodeQueryString(reqWrapper.getQueryString()),
                        convertContentByteArrToString(reqWrapper.getContentAsByteArray())
                )
        );

        logUtil.logInfo(
                log,
                logUtil.composeLogPrefixForBusiness(null, reqWrapper.getAttribute("UUID").toString()),
                String.format(
                        "The info of response, body: %s",
                        convertContentByteArrToString(resWrapper.getContentAsByteArray())
                )
        );
    }

    /**
     * request.getQueryString()得出來的中文字會被編碼過，變成%。
     * 在這裡進行解碼，解碼成可以閱讀的中文字。
     */
    private String decodeQueryString(String queryStr) throws UnsupportedEncodingException {
        return queryStr == null ? "null" : URLDecoder.decode(queryStr, "UTF-8");
    }

    /**
     * 將byte資料轉換成字串
     */
    private String convertContentByteArrToString(byte[] contentBytes) {
        return new String(contentBytes);
    }

}
