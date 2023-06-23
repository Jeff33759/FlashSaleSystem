package jeff.common.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.test.context.SpringBootTest;

import java.sql.Timestamp;
import java.time.ZoneId;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@SpringBootConfiguration
class DateUtilTest {

    @Spy
    DateUtil spyDateUtil; //待測元件

    String timeString;
    long matchTimestamp;

    @Test
    void GivenTimestamp_WhenConvertTimestampToUTCTimeZoneISOString_ThenReturnExpectedString() {
        prepareArgsForTestCase("2022-05-13T16:00:00.000Z", 1652457600000L);

        String actual = spyDateUtil.convertTimestampToUTCTimeZoneISOString(new Timestamp(this.matchTimestamp));

        Assertions.assertEquals(this.timeString, actual);
    }

    @Test
    void GivenTimeFormatString_WhenConvertTimeFormatStringToTimestamp_ThenReturnExpectedTimestamp() {
        prepareArgsForTestCase("2022-05-14 00:00:00", 1652457600000L);

        Timestamp actual = spyDateUtil.convertTimeFormatStringToTimestamp(this.timeString, ZoneId.systemDefault()); //開發環境電腦的系統時區為UTC+8

        Assertions.assertEquals(new Timestamp(this.matchTimestamp), actual);
    }

    /**
     * @param timeString        時間格式字串
     * @param matchTimestamp    時間格式字串所對應的時間戳
     */
    private void prepareArgsForTestCase(String timeString, long matchTimestamp) {
        this.timeString = timeString;
        this.matchTimestamp = matchTimestamp;
    }

}