package jeff.common.util;

import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.time.*;
import java.time.format.DateTimeFormatter;

/**
 * 日期格式轉換相關的工具包。
 * DateTimeFormatter為執行緒安全的物件。
 */
@Component
public class DateUtil {

    private DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private DateTimeFormatter isoDateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");


    /**
     * 將時間戳，轉換成國際標準時間(UTC)的ISO格式字串，顯示到秒數小數點後三位。
     * <p>
     * 輸出範例格式:
     * 2021-01-11T11:30:53.000Z
     *
     * @param timestamp 時間戳
     * @return ISO格式時間字串
     */
    public String convertTimestampToUTCTimeZoneISOString(Timestamp timestamp) {
        return isoDateTimeFormatter.withZone(ZoneId.from(ZoneOffset.UTC)).format(timestamp.toInstant());
    }

    /**
     * 將yyyy-MM-dd HH:mm:ss格式的字串(此字串非絕對時間，時區由呼叫者給予ZoneId來決定)，轉換成時間戳。
     *
     * @param timeString 日期格式範例: 2021-10-08 23:11:38
     * @param zoneId     上面日期的時區
     * @return 時間戳
     */
    public Timestamp convertTimeFormatStringToTimestamp(String timeString, ZoneId zoneId) {
        LocalDateTime localDateTime = LocalDateTime.parse(timeString, dateTimeFormatter);
        return Timestamp.from(localDateTime.toInstant(OffsetDateTime.now(zoneId).getOffset()));
    }


}
