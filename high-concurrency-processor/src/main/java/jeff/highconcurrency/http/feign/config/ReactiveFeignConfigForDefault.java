package jeff.highconcurrency.http.feign.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import jeff.common.entity.dto.inner.InnerCommunicationDto;
import jeff.common.exception.MyException;
import jeff.common.exception.MyInnerCommunicationStatusFailureException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpStatus;
import reactivefeign.ReactiveOptions;
import reactivefeign.client.ReactiveFeignException;
import reactivefeign.client.ReactiveHttpResponse;
import reactivefeign.client.log.DefaultReactiveLogger;
import reactivefeign.client.log.ReactiveLoggerListener;
import reactivefeign.client.statushandler.ReactiveStatusHandler;
import reactivefeign.retry.BasicReactiveRetryPolicy;
import reactivefeign.retry.ReactiveRetryPolicy;
import reactivefeign.utils.HttpUtils;
import reactivefeign.webclient.WebReactiveOptions;
import reactivefeign.webclient.client.WebReactiveHttpClient;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.time.Clock;

/**
 * 預設的feign配置。
 *
 * 文件參考(搜尋"application.properties configuration"):
 * https://github.com/PlaytikaOSS/feign-reactive/tree/develop/feign-reactor-spring-configuration
 * 它範例沒有很完整，實際上有哪些東西可以配置，要直接去看官方SourceCode。
 */
@Slf4j
@Configuration
public class ReactiveFeignConfigForDefault {

    @Autowired
    private ObjectMapper objectMapper;


    /**
     * http的一些策略配置，如超時等等。
     */
    @Bean
    @Primary //用Primary修飾，當容器內有多個同類Bean時，此實例優先級最高。所以如果有@ReactiveFeignClient沒指定config，又或者該config裡沒有此同類Bean，那就會抓預設的這個
    public ReactiveOptions reactiveOptionsForDefault() {
        return WebReactiveOptions.DEFAULT_OPTIONS; //使用套件預設
    }

    /**
     * 錯誤碼處理。
     */
    @Bean
    @Primary //用Primary修飾，當容器內有多個同類Bean時，此實例優先級最高。所以如果有@ReactiveFeignClient沒指定config，又或者該config裡沒有此同類Bean，那就會抓預設的這個
    public ReactiveStatusHandler reactiveStatusHandlerForDefault() {
        return new ReactiveStatusHandler() {

            @Override
            public boolean shouldHandle(int status) {
                return HttpUtils.familyOf(status).isError(); //狀態碼非2XX的，都要進行decode處理。
            }

            /**
             * 這裡設置完後，當接到非2XX例外，reactive feign會對外拋我們自己做的錯誤(原本是拋FeignException)，接著再由控制器aop去統一處理。
             *
             * 目前有個問題，就是當拋錯，固定會印紅字的error stack。
             * 試過調整reactive feign的logging策略，沒辦法取消不印(畢竟不是log)，應該是官方的函式庫在哪個環節呼叫了printStackTrace，經查後，發現很有可能是這樣。
             * 參考:
             * https://github.com/PlaytikaOSS/feign-reactive/pull/629
             * 相關Class:
             * {@link WebReactiveHttpClient#executeRequest}
             * {@link ReactiveFeignException#ReactiveFeignException}
             *
             * 解決方案:
             * 改用3.3以上版本的reactive-feign，但會依賴於更高的java版本。
             * 本專題因為使用java8，所以只能用3.2版以下。
             */
            @Override
            public Mono<? extends Throwable> decode(String methodKey, ReactiveHttpResponse<?> response) { //預設會拋FeignException，現在覆寫方法，處理成拋自己的例外
                return response.bodyData()
                        .flatMap(body -> {
                            try {
                                InnerCommunicationDto bodyObj = objectMapper.readValue(body, InnerCommunicationDto.class);
                                return Mono.just(new MyInnerCommunicationStatusFailureException(HttpStatus.valueOf(response.status()), bodyObj));
                            } catch (IOException e) {
                                return Mono.error(new MyException("Cannot convert body data into jsonObj but it should not occur."));
                            }
                        });
            }

        };
    }

    /**
     * log配置。
     */
    @Bean
    @Primary //用Primary修飾，當容器內有多個同類Bean時，此實例優先級最高。所以如果有@ReactiveFeignClient沒指定config，又或者該config裡沒有此同類Bean，那就會抓預設的這個
    public ReactiveLoggerListener loggerListenerForDefault() {
        return new DefaultReactiveLogger(Clock.systemUTC(), log); //幾乎使用套件預設
    }

    /**
     * 重試策略配置
     */
    @Bean
    @Primary //用Primary修飾，當容器內有多個同類Bean時，此實例優先級最高。所以如果有@ReactiveFeignClient沒指定config，那就會抓預設的這個
    public ReactiveRetryPolicy reactiveRetryPolicyForDefault() {
        return new BasicReactiveRetryPolicy.Builder().build(); //使用套件預設
    }

}
