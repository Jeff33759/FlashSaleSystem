package jeff.core.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
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
public class LoggingFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        ContentCachingRequestWrapper reqWrapper = new ContentCachingRequestWrapper(request); //使用包裝器，讓請求物件的資料流可以被之後的元件重複讀取
        ContentCachingResponseWrapper resWrapper = new ContentCachingResponseWrapper(response); //使用包裝器，讓回應物件的資料流可以被之後的元件重複讀取

        filterChain.doFilter(reqWrapper, resWrapper);

        logAPI(reqWrapper, resWrapper); //要放在doFilter下面，getContentAsByteArray才有東西
        resWrapper.copyBodyToResponse(); //若沒加這個，客戶端不會獲得body
    }

    /**
     * 針對API進行logging。
     */
    private void logAPI(ContentCachingRequestWrapper reqWrapper, ContentCachingResponseWrapper resWrapper) throws UnsupportedEncodingException {
        log.info("The info of request, clientIP: {}, method: {}, path: {}, queryString: {}, body: {}",
                reqWrapper.getRemoteAddr(), reqWrapper.getMethod(), reqWrapper.getServletPath(), decodeQueryString(reqWrapper.getQueryString()), convertContentByteArrToString(reqWrapper.getContentAsByteArray()));

        log.info("The info of response, body: {}", convertContentByteArrToString(resWrapper.getContentAsByteArray()));
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
