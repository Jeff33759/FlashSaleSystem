package jeff.core.filter;

import jeff.common.entity.bo.MyRequestContext;
import jeff.common.util.LogUtil;
import jeff.core.entity.bo.MyContentCachingReqWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingResponseWrapper;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 生成UUID去代表一個請求的生命週期，方便日誌中心化時的搜尋(可以用UUID去找到某個請求在業務邏輯中跑了啥方法)。
 */
@Component
@WebFilter
@Order(5)
public class UUIDFilter extends OncePerRequestFilter {

    @Autowired
    private LogUtil logUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        MyContentCachingReqWrapper reqWrapper = (MyContentCachingReqWrapper) request;
        ContentCachingResponseWrapper resWrapper = (ContentCachingResponseWrapper) response;

        MyRequestContext myContext = (MyRequestContext) request.getAttribute("myContext");
        myContext.setUUID(logUtil.generateUUIDForLogging()); //因為上面getAttribute得到的是參考，所以這裡set，會直接set進該物件

        filterChain.doFilter(reqWrapper, resWrapper);
    }

}
