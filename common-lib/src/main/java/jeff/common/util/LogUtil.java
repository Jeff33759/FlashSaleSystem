package jeff.common.util;

import jeff.common.consts.MyLogType;
import org.springframework.beans.factory.annotation.Value;

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
 */
public class LogUtil {

    @Value("${app.type}")
    private String appType;

    @Value("${app.instance.name}")
    private String appInstanceName;

    /**
     * 組織系統通知Log的前綴。
     * 用String.format雖然效能較差，但可讀性較佳，考量到單個Log也不是什麼很大量的字串拼接，不用那麼看效能。
     */
    public String composeLogPrefixForNotify(MyLogType myLogType) {
        return String.format("[%s][%s][%s]",
                myLogType.getTypeName(),
                appType,
                appInstanceName
        );
    }

    /**
     * 組織業務邏輯Log的前綴。
     * 用String.format雖然效能較差，但可讀性較佳，考量到單個Log也不是什麼很大量的字串拼接，不用那麼看效能。
     */
    public String composeLogPrefixForBusiness(MyLogType myLogType, Integer memberId, String uuid) {
        return String.format("[%s][%s][%s][%s][%s]",
                myLogType.getTypeName(),
                appType,
                appInstanceName,
                memberId,
                uuid
        );
    }

    public String generateUUID() {
        return UUID.randomUUID().toString();
    }

}
