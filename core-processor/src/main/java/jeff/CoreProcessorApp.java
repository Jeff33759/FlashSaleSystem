package jeff;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * 核心伺服端，負責處理所有非高併發的場景。
 */
@SpringBootApplication
@EnableDiscoveryClient //註明自己是服務發現的客戶端，用於向註冊中心(consul、zookeeper)註冊自己這個實例
public class CoreProcessorApp {
    public static void main(String[] args) {
        SpringApplication.run(CoreProcessorApp.class, args);
    }
}