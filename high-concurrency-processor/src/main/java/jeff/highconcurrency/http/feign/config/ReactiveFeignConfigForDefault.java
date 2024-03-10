package jeff.highconcurrency.http.feign.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import reactivefeign.ReactiveOptions;
import reactivefeign.webclient.WebReactiveOptions;

/**
 * 預設的feign配置。
 */
@Configuration
public class ReactiveFeignConfigForDefault {

    @Bean
    @Primary //用Primary修飾，當容器內有多個同類Bean時，此實例優先級最高。所以如果有@ReactiveFeignClient沒指定config，那就會抓預設的這個
    public ReactiveOptions reactiveOptionsForDefault() {
        return WebReactiveOptions.DEFAULT_OPTIONS;
    }

}
