package jeff.common.util;

import jeff.common.consts.MyLogType;
import org.junit.jupiter.api.*;
import org.mockito.Spy;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = LogUtil.class)
class LogUtilTest {

    @Spy
    LogUtil spyLogUtil; //待測元件

    MyLogType stubMyLogType;
    Integer stubMemberId;
    String stubUUID;

    @BeforeEach
    void stubMemberVarForTestCase() {
        ReflectionTestUtils.setField(spyLogUtil, "appType", "TestAPP");
        ReflectionTestUtils.setField(spyLogUtil, "appInstanceName", "TestApp01");
    }

    @AfterEach
    void resetAllArgsForTestCase() {
        stubMyLogType = null;
        stubMemberId = null;
        stubUUID = null;
    }

    @Test
    void GivenArgs_WhenComposeLogPrefixForNotify_ThenReturnExpectedString() {
        this.prepareArgsForNotifyTestCase();

        String actual = spyLogUtil.composeLogPrefixForNotify(this.stubMyLogType);

        Assertions.assertEquals("[Notify][TestAPP][TestApp01]", actual);
    }

    @Test
    void GivenArgs_WhenComposeLogPrefixForBusiness_ThenReturnExpectedString() {
        this.prepareArgsForBusinessTestCase(1, "stubUUID");

        String actual = spyLogUtil.composeLogPrefixForBusiness(this.stubMyLogType, this.stubMemberId, this.stubUUID);

        Assertions.assertEquals("[Business][TestAPP][TestApp01][1][stubUUID]", actual);
    }

    @Test
    void GivenArgs_WhenGenerateUUIDForLogging_ThenReturnExpectedLengthString() {
        int expectedLength = 6;

        String actual = spyLogUtil.generateUUIDForLogging();

        Assertions.assertEquals(expectedLength, actual.length());
    }

    private void prepareArgsForNotifyTestCase() {
        this.stubMyLogType = MyLogType.NOTIFY;
    }

    private void prepareArgsForBusinessTestCase(Integer memberId, String uuid) {
        this.stubMyLogType = MyLogType.BUSINESS;
        this.stubMemberId = memberId;
        this.stubUUID = uuid;
    }

}