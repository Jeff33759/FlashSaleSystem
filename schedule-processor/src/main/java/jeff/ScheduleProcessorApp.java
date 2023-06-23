package jeff;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 排程伺服端，專門跑排程任務。
 */
@SpringBootApplication
@EnableScheduling //開啟排程
public class ScheduleProcessorApp {
    public static void main(String[] args) {
        SpringApplication.run(ScheduleProcessorApp.class, args);
    }
}
