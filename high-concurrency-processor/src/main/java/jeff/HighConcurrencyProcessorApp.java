package jeff;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import reactivefeign.spring.config.EnableReactiveFeignClients;

/**
 * 高併發伺服端，負責處理所有高併發場景。
 */
@SpringBootApplication
@EnableReactiveFeignClients // 開啟reactive feign的功能
public class HighConcurrencyProcessorApp {
    public static void main(String[] args) {
        SpringApplication.run(HighConcurrencyProcessorApp.class, args);
    }
}
