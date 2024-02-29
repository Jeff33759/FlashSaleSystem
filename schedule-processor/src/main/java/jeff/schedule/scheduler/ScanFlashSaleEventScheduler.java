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
                        "scanFlashSaleEventWhichShouldBeOpenFromMySQLAndInsertIntoMongoAndPutToRedis schedule is finished, executionTime: %ssec, beOpenFlashSaleEventAmount: %s",
                        Duration.between(startTime, endTime).getSeconds(), executionAmount
                )
        );
    }


    /**
     * 每30秒就去掃一次MySQL，看有沒有超過販售時間的快閃銷售案件，將其下架。
     *
     * 開賣中的快閃銷售案件，is_public=true，has_been_scanned=true。
     * 只要當下時間 > end_time，就代表這個快閃銷售案件要被下架了，redis因為有設超時，所以應該也會已經不存在(但以防萬一，還是砍一下)，所以超過銷售時間的快閃銷售案件，不可能被會員消費到(redis left pop就會失敗了)。
     */
    @Scheduled(initialDelay = 30000, fixedDelay = 30000)
    public void scanFlashSaleEventWhichShouldBeClosedFromMySQLAndDeleteMongoAndRemoveRedis() {
        logUtil.logInfo(
                log,
                logUtil.composeLogPrefixForSystem(),
                "scanFlashSaleEventWhichShouldBeClosedFromMySQLAndDeleteMongoAndRemoveRedis schedule is started."
        );

        Instant startTime = Instant.now();
        int executionAmount = scanFlashSaleEventService.scanFlashSaleEventWhichShouldBeClosedFromMySQLAndDeleteMongoAndRemoveRedis();
        Instant endTime = Instant.now();

        logUtil.logInfo(
                log,
                logUtil.composeLogPrefixForSystem(),
                String.format(
                        "scanFlashSaleEventWhichShouldBeClosedFromMySQLAndDeleteMongoAndRemoveRedis schedule is finished, executionTime: %ssec, beClosedFlashSaleEventAmount: %s",
                        Duration.between(startTime, endTime).getSeconds(), executionAmount
                )
        );
    }


}
