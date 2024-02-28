package jeff.mq.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Spy;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = MQUtilTest.class)
class MQUtilTest {

    @Spy
    MQUtil spyMQUtil; // 待測元件

    @Test
    void Given_WhenGenerateMsgId_ThenReturnStringWhichHasExpectedFormat() {
        String expectedPrefix = "mq_";
        int expectedLength = 7;

        String actual = spyMQUtil.generateMsgId();

        Assertions.assertTrue(actual.startsWith(expectedPrefix));
        Assertions.assertEquals(expectedLength, actual.length());
    }

}