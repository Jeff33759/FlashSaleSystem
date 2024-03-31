package jeff;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 系統的網關，負責對系統外部提供統一的訪問位址，以及為Api提供路由以及流量控制。
 */
@SpringBootApplication
public class SystemGatewayApp {
    public static void main(String[] args) {
        SpringApplication.run(SystemGatewayApp.class, args);
    }
}
