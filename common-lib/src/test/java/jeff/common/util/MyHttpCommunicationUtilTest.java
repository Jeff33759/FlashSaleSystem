package jeff.common.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Spy;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;

@SpringBootTest(classes = MyHttpCommunicationUtilTest.class)
class MyHttpCommunicationUtilTest {

    @Spy
    private MyHttpCommunicationUtil spyMyHttpCommunicationUtil; //待測元件

    @Test
    public void GivenStatusIs200_WhenIsHttpStatusFromDownstreamCorrect_ThenReturnTrue() {
        HttpStatus stubStatus = HttpStatus.OK;

        boolean actual = spyMyHttpCommunicationUtil.isHttpStatusFromDownstreamCorrect(stubStatus);

        Assertions.assertTrue(actual);
    }

    @Test
    public void GivenStatusIsNot200_WhenIsHttpStatusFromDownstreamCorrect_ThenReturnFalse() {
        HttpStatus stubStatus = HttpStatus.INTERNAL_SERVER_ERROR;

        boolean actual = spyMyHttpCommunicationUtil.isHttpStatusFromDownstreamCorrect(stubStatus);

        Assertions.assertFalse(actual);
    }

}