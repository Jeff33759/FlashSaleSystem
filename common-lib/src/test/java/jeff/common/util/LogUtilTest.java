package jeff.common.util;

import org.junit.jupiter.api.*;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.slf4j.Logger;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.util.ReflectionTestUtils;

@SpringBootTest(classes = LogUtilTest.class)
class LogUtilTest {

    @Spy
    LogUtil spyLogUtil; //待測元件

    @BeforeEach
    void stubMemberVarForTestCase() {
        ReflectionTestUtils.setField(spyLogUtil, "appType", "TestAPP");
        ReflectionTestUtils.setField(spyLogUtil, "appInstanceName", "TestApp01");
        ReflectionTestUtils.invokeMethod(spyLogUtil, "initVariableAfterTheSpringApplicationStartup");
    }

    @Test
    void GivenArgs_WhenComposeLogPrefixForSystem_ThenReturnExpectedString() {
        String actual = spyLogUtil.composeLogPrefixForSystem();

        Assertions.assertEquals("[SYS][TestAPP][TestApp01]", actual);
    }

    @Test
    void GivenArgs_WhenComposeLogPrefixForBusiness_ThenReturnExpectedString() {
        Integer stubMemberId = 1;
        String stubUUID = "stubUUID";

        String actual = spyLogUtil.composeLogPrefixForBusiness(stubMemberId, stubUUID);

        Assertions.assertEquals("[BUS][TestAPP][TestApp01][1][stubUUID]", actual);
    }

    @Test
    void GivenArgs_WhenGenerateUUIDForLogging_ThenReturnExpectedLengthString() {
        int expectedLength = 6;

        String actual = spyLogUtil.generateUUIDForLogging();

        Assertions.assertEquals(expectedLength, actual.length());
    }

    @Test
    void GivenArgsAndMockLogger_WhenLogInfo_ThenInvokeExpectedMethodOfLogger() {
        Logger mockLogger = Mockito.mock(Logger.class);

        spyLogUtil.logInfo(mockLogger, "stubPrefix", "stubMsg");

        Mockito.verify(mockLogger, Mockito.times(1)).info("{} {}", "stubPrefix", "stubMsg");
    }

    @Test
    void GivenArgsAndMockLogger_WhenLogDebug_ThenInvokeExpectedMethodOfLogger() {
        Logger mockLogger = Mockito.mock(Logger.class);

        spyLogUtil.logDebug(mockLogger, "stubPrefix", "stubMsg");

        Mockito.verify(mockLogger, Mockito.times(1)).debug("{} {}", "stubPrefix", "stubMsg");
    }

    @Test
    void GivenArgsAndMockLogger_WhenLogWarn_ThenInvokeExpectedMethodOfLogger() {
        Logger mockLogger = Mockito.mock(Logger.class);

        spyLogUtil.logWarn(mockLogger, "stubPrefix", "stubMsg");

        Mockito.verify(mockLogger, Mockito.times(1)).warn("{} {}", "stubPrefix", "stubMsg");
    }

    @Test
    void GivenArgsAndMockLogger_WhenLogError_ThenInvokeExpectedMethodOfLogger() {
        Logger mockLogger = Mockito.mock(Logger.class);
        Exception stubException = new Exception("stubMsg");

        spyLogUtil.logError(mockLogger, "stubPrefix", stubException.getMessage(), stubException);

        Mockito.verify(mockLogger, Mockito.times(1)).error("{} {} {}", "stubPrefix", stubException.getMessage(), stubException);
    }

}