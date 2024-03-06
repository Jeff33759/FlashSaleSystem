package jeff.core.filter;

import jeff.core.entity.bo.MyContentCachingReqWrapper;
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
 * 將請求與回應物件用包裝器包裝，讓資料流可以重複使用。
 */
@Component
@WebFilter
@Order(FilterOrderNumberConst.WRAPPER_FILTER)
public class WrapperFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        MyContentCachingReqWrapper reqWrapper = new MyContentCachingReqWrapper(request); //使用自訂義包裝器，讓請求物件的資料流可以被之後的元件重複讀取
        ContentCachingResponseWrapper resWrapper = new ContentCachingResponseWrapper(response); //使用包裝器，讓回應物件的資料流可以被之後的元件重複讀取

        filterChain.doFilter(reqWrapper, resWrapper); //之後的元件所取到的實例，其實都是包裝器的實例，所以可以強轉
    }

}
