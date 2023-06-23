package jeff.schedule.scheduler;

import jeff.schedule.service.ScanFlashSaleEventService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;

/**
 * 排程去定期掃描MySQL裡面的銷售活動表，只抓出快閃的活動，將其發布到MongoDB裡面，並且put進redis Queue。
 */
@Slf4j
@Component
public class ScanFlashSaleEventScheduler {

    @Autowired
    private ScanFlashSaleEventService scanFlashSaleEventService;

    /**
     * 每5秒就去掃一次MySQL，看有沒有新發布的快閃銷售活動。
     */
    @Scheduled(initialDelay = 5000, fixedDelay = 5000)
    public void scanFlashSaleEventFromMySQLAndInsertIntoMongoAndPutToRedis() {
        log.info("ScanFlashSaleEventFromMySQLAndInsertIntoMongoAndPutToRedis schedule is started.");
        Instant startTime = Instant.now();

        int executionAmount = scanFlashSaleEventService.executeTheProcessingFlow();

        Instant endTime = Instant.now();
        log.info("ScanFlashSaleEventFromMySQLAndInsertIntoMongoAndPutToRedis schedule is finished, executionTime: {}sec, beExecutedFlashSaleEventAmount: {}",
                Duration.between(startTime, endTime).getSeconds(), executionAmount);
    }




}
