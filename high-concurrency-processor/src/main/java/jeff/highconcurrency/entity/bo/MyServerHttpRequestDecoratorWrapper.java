package jeff.highconcurrency.entity.bo;

import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpRequestDecorator;
import reactor.core.publisher.Flux;

import java.nio.charset.StandardCharsets;

/**
 * ServerHttpRequestDecorator的包裝器，將請求的body快取起來，讓之後可以覆用。
 * 高併發場景下，可能有頻繁GC造成的效能問題。
 */
public class MyServerHttpRequestDecoratorWrapper extends ServerHttpRequestDecorator {

    private final StringBuilder reqBodyBuilder = new StringBuilder();


    public MyServerHttpRequestDecoratorWrapper(ServerHttpRequest req) {
        super(req);
    }

    /**
     * 當controller那邊呼叫getBody去分段獲取body資料的同時，也把該分段資料流以字串形式累積暫存起來，讓filter可以利用getBodyDataAsString讀取資料。
     */
    @Override
    public Flux<DataBuffer> getBody() {
        return super.getBody().doOnNext( // WebFlux似乎把請求資料流分成好幾節(Flux)，這裡一節一節去讀取body資料流形成元素，元素到達流後便回調appendReqDataBufferToStringBuilder，且回調本身的內部邏輯不改變元素本身
                (dataBuffer) -> this.appendReqDataBufferToStringBuilder(dataBuffer)
        );
    }

    /**
     * getBody要被呼叫一次後，才會有東西。
     * 框架預設會在controller去呼叫。
     */
    public String getBodyDataAsString() {
        return this.reqBodyBuilder.toString();
    }

    /**
     * 把一個資料流轉成字串，append進StringBuilder
     */
    private void appendReqDataBufferToStringBuilder(DataBuffer buffer) {
        this.reqBodyBuilder.append(StandardCharsets.UTF_8.decode(buffer.asByteBuffer()).toString());
    }


}
