package jeff.common.util;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Spy;
import org.springframework.boot.test.context.SpringBootTest;

import java.sql.Timestamp;
import java.time.ZoneId;

@SpringBootTest(classes = DateUtilTest.class)
class DateUtilTest {

    @Spy
    private DateUtil spyDateUtil; //待測元件

    String stubTimeString;
    long stubMatchTimestamp;

    @AfterEach
    void resetAllArgsForTestCase() {
        this.stubTimeString = null;
        this.stubMatchTimestamp = 0;
    }

    @Test
    void GivenTimestamp_WhenConvertTimestampToUTCTimeZoneISOString_ThenReturnExpectedString() {
        prepareArgsForTestCase("2022-05-13T16:00:00.000Z", 1652457600000L);

        String actual = spyDateUtil.convertTimestampToUTCTimeZoneISOString(new Timestamp(this.stubMatchTimestamp));

        Assertions.assertEquals(this.stubTimeString, actual);
    }

    @Test
    void GivenTimeFormatString_WhenConvertTimeFormatStringToTimestamp_ThenReturnExpectedTimestamp() {
        prepareArgsForTestCase("2022-05-14 00:00:00", 1652457600000L);

        Timestamp actual = spyDateUtil.convertTimeFormatStringToTimestamp(this.stubTimeString, ZoneId.systemDefault()); //開發環境電腦的系統時區為UTC+8

        Assertions.assertEquals(new Timestamp(this.stubMatchTimestamp), actual);
    }

    /**
     * @param timeString        時間格式字串
     * @param matchTimestamp    時間格式字串所對應的時間戳
     */
    private void prepareArgsForTestCase(String timeString, long matchTimestamp) {
        this.stubTimeString = timeString;
        this.stubMatchTimestamp = matchTimestamp;
    }

}