package jeff;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 高併發伺服端，負責處理所有高併發場景。
 */
@SpringBootApplication
public class HighConcurrencyProcessorApp {
    public static void main(String[] args) {
        SpringApplication.run(HighConcurrencyProcessorApp.class, args);
    }
}
