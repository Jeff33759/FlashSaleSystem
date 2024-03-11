package jeff.highconcurrency.http.feign.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactivefeign.ReactiveOptions;
import reactivefeign.webclient.WebReactiveOptions;

/**
 * 針對訪問core-processor的feign配置。
 *
 * 文件參考(搜尋"application.properties configuration"):
 * https://github.com/PlaytikaOSS/feign-reactive/tree/develop/feign-reactor-spring-configuration
 * 它範例沒有很完整，實際上有哪些東西可以配置，要直接去看官方SourceCode。
 */
@Configuration
public class ReactiveFeignConfigForCoreProcessor {

    @Value("${my.reactive.feign.client.config.core-processor.connectTimeout}")
    private int connectTimeout;

    @Value("${my.reactive.feign.client.config.core-processor.readTimeout}")
    private int readTimeout;

    @Value("${my.reactive.feign.client.config.core-processor.writeTimeout}")
    private int writeTimeout;

    /**
     * http的一些策略配置，如超時等等。
     */
    @Bean
    public ReactiveOptions reactiveOptionsForCoreProcessor() {
        return new WebReactiveOptions.Builder()
                .setReadTimeoutMillis(readTimeout)
                .setWriteTimeoutMillis(writeTimeout)
                .setConnectTimeoutMillis(connectTimeout)
                .build();
    }

}
