package jeff.highconcurrency.entity.bo;

import org.reactivestreams.Publisher;
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

    private void appendResDataBufferToStringBuilder(DataBuffer buffer) {
        this.resBodyBuilder.append(StandardCharsets.UTF_8.decode(buffer.asByteBuffer()).toString());
    }

    /**
     * writeWith要被呼叫一次後，才會有東西。
     */
    public String getBodyDataAsString() {
        return this.resBodyBuilder.toString();
    }


}
