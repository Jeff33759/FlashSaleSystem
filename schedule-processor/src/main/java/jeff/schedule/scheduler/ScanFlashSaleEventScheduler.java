package jeff.schedule.scheduler;

import jeff.common.util.LogUtil;
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

    @Autowired
    private LogUtil logUtil;

    /**
     * 每5秒就去掃一次MySQL，看有沒有新發布還沒被掃描過並且已經到了該開賣時間的快閃銷售活動。
     *
     * 新發布但還沒開賣的快閃銷售案件，is_public=true，has_been_scanned=false。
     * 只要當下時間 > start_time，代表這個快閃銷售案件應該開賣了。
     */
    @Scheduled(initialDelay = 5000, fixedDelay = 5000)
    public void scanFlashSaleEventWhichShouldBeOpenFromMySQLAndInsertIntoMongoAndPutToRedis() {
        logUtil.logInfo(
                log,
                logUtil.composeLogPrefixForSystem(),
                "scanFlashSaleEventWhichShouldBeOpenFromMySQLAndInsertIntoMongoAndPutToRedis schedule is started."
        );

        Instant startTime = Instant.now();
        int executionAmount = scanFlashSaleEventService.scanFlashSaleEventWhichShouldBeOpenFromMySQLAndInsertIntoMongoAndPutToRedis();
        Instant endTime = Instant.now();

        logUtil.logInfo(
                log,
                logUtil.composeLogPrefixForSystem(),
                String.format(
                        "ScanFlashSaleEventFromMySQLAndInsertIntoMongoAndPutToRedis schedule is finished, executionTime: %ssec, beExecutedFlashSaleEventAmount: %s",
                        Duration.between(startTime, endTime).getSeconds(), executionAmount
                )
        );
    }


}
