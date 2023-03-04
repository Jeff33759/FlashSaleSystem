package jeff;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 核心伺服端，負責處理所有非高併發的場景。
 */
@SpringBootApplication
public class CoreProcessorApp {
    public static void main(String[] args) {
        SpringApplication.run(CoreProcessorApp.class, args);
    }
}