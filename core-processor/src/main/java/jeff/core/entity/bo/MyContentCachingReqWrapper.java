package jeff.core.entity.bo;

import org.springframework.web.util.ContentCachingRequestWrapper;

import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import java.io.*;

/**
 * 自己寫的，針對ContentCachingRequestWrapper的再包裝。
 * 因為在LoggingFilter時，想要在doFilter之前，就對請求的body進行記錄，可是body的輸入流只能讀取一次，若filter讀取了，會造成控制器報錯，且原生HttpServlet API沒有辦法把流的光標重置到開頭(必須自己去覆寫)。
 * 而ContentCachingRequestWrapper的getContentAsByteArray本質上是獲取快取的資料，而快取要在原本的Stream被消耗後才會快取進去，所以getContentAsByteArray沒辦法用在doFilter之前。
 *
 * 控制器要獲取請求的body，呼叫的還是getInputStream方法，所以在這裡覆寫該方法，讓getInputStream.read的資料來源，改寫成本物件的暫存。
 */
public class MyContentCachingReqWrapper extends ContentCachingRequestWrapper {

    final byte[] reqBody; //可以重用的body資料

    /**
     * 實例化此物件時，就一定會去讀取inputStream
     */
    public MyContentCachingReqWrapper(HttpServletRequest request) throws IOException {
        super(request);
        this.reqBody = new byte[request.getContentLength()]; //別用ServletInputStream.available，當容器是Tomcat時，此方法沒辦法得到content，若是jetty就沒差

        try(
                ServletInputStream is = request.getInputStream();
                BufferedInputStream bis = new BufferedInputStream(is)
        ) {
            bis.read(this.reqBody);
        }

    }

    /**
     * 把getInputStream.read的資料來源調包。
     */
    @Override
    public ServletInputStream getInputStream() {
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(this.reqBody);

        return new ServletInputStream() {

            @Override
            public int read() { //資料來源是this.reqBody
                return byteArrayInputStream.read();
            }

            @Override
            public boolean isFinished() {
                return false;
            }

            @Override
            public boolean isReady() {
                return false;
            }

            @Override
            public void setReadListener(ReadListener readListener) {

            }

        };
    }
}

