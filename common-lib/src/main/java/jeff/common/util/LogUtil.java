package jeff.common.util;

import jeff.common.consts.MyLogType;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.UUID;

/**
 * 用於Log的工具包。
 * 將Log的格式統一化，方便對日誌中心去下條件搜尋對應Log。
 * <p>
 * 格式如下：
 * [Log類型][AppType][Server實例ID][Member Id][UUID] Log本體
 * <p>
 * Log類型分為: 系統通知(例如通知初始化成功等等...)、業務邏輯
 * UUID用於紀錄一個請求的生命週期，因為有些微服務是reactive或者有用到服務降級，可能會有不同執行緒去處理同一個request flow的情形。
 * <p>
 * 元件雖為單例，但因為log的封裝方法是無狀態，logger皆由外部傳入，所以應該是不會有執行緒安全的問題。
 */
@Component
public class LogUtil {

    @Value("${app.type}")
    private String appType;

    @Value("${app.instance.name}")
    private String appInstanceName;

    /**
     * 系統通知Logging的前綴。
     * 因為永遠不會變動，所以寫成常數，不用每次呼叫log方法時都要重新組成。
     */
    private String LOG_PREFIX_FOR_SYSTEM;

    /**
     * 用於業務邏輯Logging前綴的一部份。
     * 這一部分因為是永遠不會變動的，所以寫成常數，不用每次呼叫log方法時都要重新組成這一段。
     */
    private String PART_OF_LOG_PREFIX_FOR_BUSINESS;

    @PostConstruct
    private void initVariableAfterTheSpringApplicationStartup() {
        this.LOG_PREFIX_FOR_SYSTEM = String.format("[%s][%s][%s]", MyLogType.SYSTEM.getTypeName(), this.appType, this.appInstanceName);
        this.PART_OF_LOG_PREFIX_FOR_BUSINESS = String.format("[%s][%s][%s]", MyLogType.BUSINESS.getTypeName(), this.appType, this.appInstanceName);
    }

    public void logInfo(Logger logger, String logPrefix, String logMsg) {
        logger.info("{} {}", logPrefix, logMsg);
    }

    public void logDebug(Logger logger, String logPrefix, String logMsg) {
        logger.debug("{} {}", logPrefix, logMsg);
    }

    public void logWarn(Logger logger, String logPrefix, String logMsg) {
        logger.warn("{} {}", logPrefix, logMsg);
    }

    public void logError(Logger logger, String logPrefix, String logMsg, Exception e) {
        logger.error("{} {} {}", logPrefix, logMsg, e); // error級別的要印stackTrace
    }

    /**
     * 組織系統通知Log的前綴。
     * 用String.format雖然效能較差，但可讀性較佳，考量到單個Log也不是什麼很大量的字串拼接，不用那麼看效能。
     */
    public String composeLogPrefixForSystem() {
        return this.LOG_PREFIX_FOR_SYSTEM;
    }

    /**
     * 組織業務邏輯Log的前綴。
     * 用String.format雖然效能較差，但可讀性較佳，考量到單個Log也不是什麼很大量的字串拼接，不用那麼看效能。
     */
    public String composeLogPrefixForBusiness(Integer memberId, String uuid) {
        return String.format("%s[%s][%s]",
                this.PART_OF_LOG_PREFIX_FOR_BUSINESS,
                memberId,
                uuid
        );
    }

    /**
     * 製作一個專門給logging的隨機流水號。
     * UUID用於紀錄一個請求的生命週期，因為有些微服務是reactive或者有用到服務降級，可能會有不同執行緒去處理同一個request flow的情形。
     */
    public String generateUUIDForLogging() {
        return UUID.randomUUID().toString().substring(0, 6);
    }

}
