package jeff.core.filter;


import jeff.common.entity.bo.MyRequestContext;
import jeff.common.util.LogUtil;
import jeff.core.entity.bo.MyContentCachingReqWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingResponseWrapper;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * 紀錄請求參數與回應的過濾器。
 * 通常會是過濾鏈中的最後一層，所以到這裡的時候，MyRequestContext裡面該有的東西都會被賦值了，所以也可以進行log了。
 * 但因為目前還沒實作登入認證，所以到這一層的時候MyRequestContext的AuthenticatedMemberId會是null。
 */
@Slf4j
@Component
@WebFilter
@Order(FilterOrderNumberConst.LOGGING_FILTER)
public class LoggingFilter extends OncePerRequestFilter {

    @Autowired
    private LogUtil logUtil;

    /**
     * 不走本過濾器邏輯的Api路徑。
     *
     * java有針對String覆寫hashCode方法，所以Set調用contains時，可以針對字串的值判斷是否重複。
     */
    private Set<String> ignorePathSet =
            new HashSet<>(Arrays.asList(new String[]{
                    "/actuator/health" //actuator套件的健康檢測接口，consul會一直發Get過來監測伺服器健康度
            }));

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        MyContentCachingReqWrapper reqWrapper = (MyContentCachingReqWrapper) request;
        ContentCachingResponseWrapper resWrapper = (ContentCachingResponseWrapper) response;
        MyRequestContext myContext = (MyRequestContext) reqWrapper.getAttribute("myContext");

        this.logBeforeAPI(reqWrapper, myContext);
        filterChain.doFilter(reqWrapper, resWrapper);
        this.logAfterAPI(resWrapper, myContext);

        resWrapper.copyBodyToResponse(); //若沒加這個，客戶端不會獲得body
    }

    /**
     * 在進入API邏輯前，先log請求相關參數。
     */
    private void logBeforeAPI(MyContentCachingReqWrapper reqWrapper, MyRequestContext myContext) throws IOException {
        logUtil.logInfo(
                log,
                logUtil.composeLogPrefixForBusiness(myContext.getAuthenticatedMemberId(), myContext.getUUID()),
                String.format(
                        "The info of request, clientIP: %s, method: %s, path: %s, queryString: %s, body: %s",
                        reqWrapper.getRemoteAddr(),
                        reqWrapper.getMethod(),
                        reqWrapper.getServletPath(),
                        decodeQueryString(reqWrapper.getQueryString()),
                        StreamUtils.copyToString(reqWrapper.getInputStream(), StandardCharsets.UTF_8) //因為有覆寫過，所以這裡取出body，也不影響之後控制器再取用
                )
        );

    }

    /**
     * 在API邏輯做完後，log回應的相關參數。
     */
    private void logAfterAPI(ContentCachingResponseWrapper resWrapper, MyRequestContext myContext) {
        logUtil.logInfo(
                log,
                logUtil.composeLogPrefixForBusiness(myContext.getAuthenticatedMemberId(), myContext.getUUID()),
                String.format(
                        "The info of response, body: %s",
                        convertContentByteArrToUTF8String(resWrapper.getContentAsByteArray()) //要放在doFilter下面，getContentAsByteArray才有東西，因為getContentAsByteArray是獲取快取的資料，而快取要在原本的Stream被消耗後才會快取進去。
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
     * 將byte資料轉換成字串，編碼為UTF-8
     */
    private String convertContentByteArrToUTF8String(byte[] contentBytes) {
        return new String(contentBytes, Charset.forName("UTF-8"));
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        return ignorePathSet.contains(request.getServletPath());
    }
}
