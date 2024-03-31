package jeff.core.filter;

import jeff.common.entity.bo.MyRequestContext;
import jeff.core.entity.bo.MyContentCachingReqWrapper;
import jeff.core.filter.consts.FilterOrderNumberConst;
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
 * 設置自己做的MyRequestContext物件進request物件中，方便後續業務邏輯取用。
 * 在這一層中還只是實例化上下文物件，之後的過濾鏈才會視情況對裡面的成員變數賦值。
 */
@Component
@WebFilter
@Order(FilterOrderNumberConst.REQ_CONTEXT_GENERATION_FILTER)
public class ReqContextGenerationFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        MyContentCachingReqWrapper reqWrapper = (MyContentCachingReqWrapper) request;
        ContentCachingResponseWrapper resWrapper = (ContentCachingResponseWrapper) response;

        request.setAttribute("myContext", new MyRequestContext()); // The context for a request lifecycle.

        filterChain.doFilter(reqWrapper, resWrapper);
    }

}
