package jeff.gateway.bo;

import jeff.gateway.filter.global.ReactiveLoggingFilter;
import org.reactivestreams.Publisher;
import org.springframework.cloud.gateway.filter.NettyWriteResponseFilter;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.http.server.reactive.ServerHttpResponseDecorator;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;

/**
 * ServerHttpResponseDecorator的包裝器。
 * 高併發場景下，可能有頻繁GC造成的效能問題
 */
public class MyServerHttpResponseDecoratorWrapper extends ServerHttpResponseDecorator {

    private final StringBuilder resBodyBuilder = new StringBuilder();

    public MyServerHttpResponseDecoratorWrapper(ServerHttpResponse res) {
        super(res);
    }

    /**
     * ServerHttpResponse的writeWith方法用於將回應寫入客戶端。
     * 在這裡覆寫此方法，在回應寫入客戶端的過程中插入一些自訂義邏輯(例如把回應資料流以字串形式快取，方便之後呼叫getter就是可以用的字串)
     */
    @Override
    public Mono<Void> writeWith(Publisher<? extends DataBuffer> body) {
        return super.writeWith(Flux.from(body).doOnNext( // Flux.from(body)將回應體轉換為Flux<DataBuffer>，透過doOnNext註冊一個回調，將每個DataBuffer轉換為字串並append到StringBuilder中
                (dataBuffer) -> this.appendResDataBufferToStringBuilder(dataBuffer))
        );
    }

    /**
     * Gateway和Circuitbreaker的整合沒有很好，當收到下游Server狀態碼非2XX，觸發轉址fallBackUri時，會呼叫到兩次writeWith。
     * 第一次是fallbackUri接口某個環節呼叫，第二次是經過{@link NettyWriteResponseFilter}又呼叫一次，造成在{@link ReactiveLoggingFilter}印log時印到了兩個body。
     * 第二次writeWith的時候，response已經是committed的狀態，代表客戶端已經收到回應了，所以第二次的writeWith其實是沒有意義的。
     * 要想取消第二次writeWith的行為，可能要去覆寫{@link NettyWriteResponseFilter#filter}，但太麻煩了，所以直接在這裡加個判斷。
     * 如果已經被committed，那就不要把body寫進resBodyBuilder，讓{@link ReactiveLoggingFilter}取值正常。
     */
    private void appendResDataBufferToStringBuilder(DataBuffer buffer) {
        if(this.getDelegate().isCommitted()) {
            return;
        }

        this.resBodyBuilder.append(StandardCharsets.UTF_8.decode(buffer.asByteBuffer()).toString());
    }

    /**
     * writeWith要被呼叫一次後，才會有東西。
     */
    public String getBodyDataAsString() {
        return this.resBodyBuilder.toString();
    }


}
