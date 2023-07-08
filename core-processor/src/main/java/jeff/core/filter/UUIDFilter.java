package jeff.core.filter;

import jeff.common.util.LogUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
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
        ContentCachingRequestWrapper reqWrapper = (ContentCachingRequestWrapper) request;
        ContentCachingResponseWrapper resWrapper = (ContentCachingResponseWrapper) response;

        request.setAttribute("UUID", logUtil.generateUUIDForLogging()); // The UUID for an operation flow.

        filterChain.doFilter(reqWrapper, resWrapper);
    }

}
