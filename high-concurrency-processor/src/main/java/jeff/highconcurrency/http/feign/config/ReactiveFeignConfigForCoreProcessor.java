package jeff.highconcurrency.http.feign.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactivefeign.ReactiveOptions;
import reactivefeign.webclient.WebReactiveOptions;

/**
 * 針對訪問core-processor的feign配置。
 */
@Configuration
public class ReactiveFeignConfigForCoreProcessor {

    @Value("${my.reactive.feign.client.config.core-processor.connectTimeout}")
    private int connectTimeout;

    @Value("${my.reactive.feign.client.config.core-processor.readTimeout}")
    private int readTimeout;

    @Value("${my.reactive.feign.client.config.core-processor.writeTimeout}")
    private int writeTimeout;

    @Bean
    public ReactiveOptions reactiveOptionsForCoreProcessor() {
        return new WebReactiveOptions.Builder()
                .setReadTimeoutMillis(readTimeout)
                .setWriteTimeoutMillis(writeTimeout)
                .setConnectTimeoutMillis(connectTimeout)
                .build();
    }

}
