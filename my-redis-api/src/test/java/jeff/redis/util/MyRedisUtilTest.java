package jeff.redis.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import jeff.redis.exception.MyRedisException;
import jeff.test.pojo.MyTestPOJO;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Instant;
import java.util.*;

@SpringBootTest(classes = MyRedisUtilTest.class)
class MyRedisUtilTest {

    @Spy
    @InjectMocks
    private MyRedisUtil spyMyRedisUtil; //待測元件

    @Mock
    private StringRedisTemplate mockStringRedisTemplate;
    @Mock
    private ValueOperations mockValueOperations; //StringRedisTemplate.opsForValue實際上是呼叫此元件去做後續動作，所以必須再Mock一層，方便Mockito.verify去驗證
    @Mock
    private ListOperations mockListOperations; //理由同上

    @Mock
    private ObjectMapper mockObjectMapper;

    @BeforeEach
    void mockRedisTemplate() {
        Mockito.when(mockStringRedisTemplate.opsForValue()).thenReturn(this.mockValueOperations);
        Mockito.when(mockStringRedisTemplate.opsForList()).thenReturn(this.mockListOperations);
    }

    @Test
    void GivenKeyExistInRedis_WhenGetDataStrByKey_ThenReturnOptionalWhichContainsString() {
        String stubKey = "keyForTesting.";
        String stubValue = "valueForTesting.";
        Mockito.when(mockStringRedisTemplate.opsForValue().get(stubKey)).thenReturn(stubValue);

        Optional<String> actual = spyMyRedisUtil.getDataStrByKey(stubKey);

        Assertions.assertEquals(stubValue, actual.get());
    }

    @Test
    void GivenKeyDoesNotExistInRedis_WhenGetDataStrByKey_ThenReturnEmptyOptional() {
        String stubKey = "keyForTesting.";
        Mockito.when(mockStringRedisTemplate.opsForValue().get(stubKey)).thenReturn(null);

        Optional<String> actual = spyMyRedisUtil.getDataStrByKey(stubKey);

        Assertions.assertFalse(actual.isPresent());
    }

    @Test
    void GivenKeyAndStringValue_WhenPutDataStrByKey_ThenInvokeExpectedMethodOfStringRedisTemplate() {
        String stubKey = "keyForTesting.";
        String stubValue = "valueForTesting.";
        Mockito.doNothing().when(this.mockValueOperations).set(Mockito.anyString(), Mockito.anyString());

        spyMyRedisUtil.putDataStrByKey(stubKey, stubValue);

        Mockito.verify(this.mockValueOperations, Mockito.times(1)).set(stubKey, stubValue);
    }

    @Test
    void GivenGetDataStrByKeyMethodReturnsOptionalWhichContainsJsonString_WhenGetDataObjByKey_ThenReturnOptionalWhichContainsExpectedPOJO() throws JsonProcessingException {
        String stubKey = "keyForTesting.";
        String stubJsonValue = "{\"id\":1,\"name\":\"stubName.\"}";
        MyTestPOJO stubPOJO = new MyTestPOJO(1, "stubName.");
        Mockito.doReturn(Optional.of(stubJsonValue)).when(spyMyRedisUtil).getDataStrByKey(stubKey);
        Mockito.when(mockObjectMapper.readValue(stubJsonValue, MyTestPOJO.class)).thenReturn(stubPOJO);

        Optional<Object> actual = spyMyRedisUtil.getDataObjByKey(stubKey, MyTestPOJO.class);

        Assertions.assertEquals(stubPOJO, actual.get());
        Mockito.verify(mockObjectMapper, Mockito.times(1)).readValue(stubJsonValue, MyTestPOJO.class);
    }

    @Test
    void GivenGetDataStrByKeyMethodReturnsEmptyOptional_WhenGetDataObjByKey_ThenReturnEmptyOptional() throws JsonProcessingException {
        String stubKey = "keyForTesting.";
        Mockito.doReturn(Optional.empty()).when(spyMyRedisUtil).getDataStrByKey(stubKey);

        Optional<Object> actual = spyMyRedisUtil.getDataObjByKey(stubKey, MyTestPOJO.class);

        Assertions.assertFalse(actual.isPresent());
        Mockito.verify(mockObjectMapper, Mockito.times(0)).readValue(Mockito.anyString(), Mockito.eq(MyTestPOJO.class));
    }

    @Test
    void GivenGetDataStrByKeyMethodReturnsOptionalWhichContainsNotJsonString_WhenGetDataObjByKey_ThenThrowMyRedisException() throws JsonProcessingException {
        String stubKey = "keyForTesting.";
        String stubJsonValue = "not json.";
        Mockito.doReturn(Optional.of(stubJsonValue)).when(spyMyRedisUtil).getDataStrByKey(stubKey);
        Mockito.doThrow(JsonProcessingException.class).when(mockObjectMapper).readValue(stubJsonValue, MyTestPOJO.class);

        MyRedisException actual = Assertions.assertThrows(MyRedisException.class, () -> {
            spyMyRedisUtil.getDataObjByKey(stubKey, MyTestPOJO.class);
        });

        Assertions.assertEquals("The value in redis is not json format cause JsonProcessingException, value: not json.", actual.getMessage());
        Mockito.verify(mockObjectMapper, Mockito.times(1)).readValue(stubJsonValue, MyTestPOJO.class);
    }

    @Test
    void GivenPOJO_WhenPutDataObjByKey_ThenInvokeWriteValueAsStringMethodOfObjectMapperAndPassExpectedJsonStrToPutDataStrByKeyMethod() throws JsonProcessingException {
        String stubKey = "keyForTesting.";
        MyTestPOJO stubPOJO = new MyTestPOJO(1, "stubName.");
        String stubJsonValueFromConvertedStubPOJO = "{\"id\":1,\"stubName.\"}";
        Mockito.when(mockObjectMapper.writeValueAsString(stubPOJO)).thenReturn(stubJsonValueFromConvertedStubPOJO);

        spyMyRedisUtil.putDataObjByKey(stubKey, stubPOJO);

        Mockito.verify(mockObjectMapper, Mockito.times(1)).writeValueAsString(stubPOJO);
        Mockito.verify(spyMyRedisUtil, Mockito.times(1)).putDataStrByKey(stubKey, stubJsonValueFromConvertedStubPOJO);
    }

    @Test
    void GivenJsonProcessingExceptionFromObjectMapper_WhenPutDataObjByKey_ThenThrowMyRedisException() throws JsonProcessingException {
        String stubKey = "keyForTesting.";
        MyTestPOJO stubPOJO = new MyTestPOJO(1, "stubName.");
        Mockito.when(mockObjectMapper.writeValueAsString(stubPOJO)).thenThrow(JsonProcessingException.class); //JsonProcessingException不讓人new

        MyRedisException actual = Assertions.assertThrows(MyRedisException.class, () -> {
            spyMyRedisUtil.putDataObjByKey(stubKey, stubPOJO);
        });

        Assertions.assertEquals("Some error occurred when converting POJO into jsonStr cause JsonProcessingException.", actual.getMessage());
        Mockito.verify(mockObjectMapper, Mockito.times(1)).writeValueAsString(stubPOJO);
        Mockito.verify(spyMyRedisUtil, Mockito.times(0)).putDataStrByKey(Mockito.anyString(), Mockito.anyString());
    }

    @Test
    void GivenKeyExistInRedis_WhenLeftPopByKeyAndGetDataStr_ThenReturnOptionalWhichContainsExpectedString() {
        String stubKey = "keyForTesting.";
        String stubJsonValue = "{\"id\":1,\"name\":\"stubName.\"}";
        Mockito.when(mockStringRedisTemplate.opsForList().leftPop(stubKey)).thenReturn(stubJsonValue);

        Optional<String> actual = spyMyRedisUtil.leftPopListByKeyAndGetDataStr(stubKey);

        Assertions.assertEquals(stubJsonValue, actual.get());
        Mockito.verify(mockListOperations, Mockito.times(1)).leftPop(stubKey);
    }

    @Test
    void GivenKeyDoesNotExistInRedis_WhenLeftPopByKeyAndGetDataStr_ThenReturnEmptyOptional() {
        String stubKey = "keyForTesting.";
        Mockito.when(mockStringRedisTemplate.opsForList().leftPop(stubKey)).thenReturn(null);

        Optional<String> actual = spyMyRedisUtil.leftPopListByKeyAndGetDataStr(stubKey);

        Assertions.assertFalse(actual.isPresent());
    }

    @Test
    void GivenKeyExistInRedis_WhenLeftPopListByKeyAndGetDataObj_ThenReturnOptionalWhichContainsExpectedPOJO() throws JsonProcessingException {
        String stubKey = "keyForTesting.";
        String stubJsonValue = "{\"id\":1,\"name\":\"stubName.\"}";
        MyTestPOJO stubPOJO = new MyTestPOJO(1, "stubName.");
        Mockito.doReturn(Optional.of(stubJsonValue)).when(spyMyRedisUtil).leftPopListByKeyAndGetDataStr(stubKey);
        Mockito.when(mockObjectMapper.readValue(stubJsonValue, MyTestPOJO.class)).thenReturn(stubPOJO);

        Optional<Object> actual = spyMyRedisUtil.leftPopListByKeyAndGetDataObj(stubKey, MyTestPOJO.class);

        Assertions.assertEquals(stubPOJO, actual.get());
    }

    @Test
    void GivenKeyDoesNotExistInRedis_WhenLeftPopListByKeyAndGetDataObj_ThenReturnEmptyOptional() throws JsonProcessingException {
        String stubKey = "keyForTesting.";
        String stubJsonValue = "{\"id\":1,\"name\":\"stubName.\"}";
        Mockito.doReturn(Optional.empty()).when(spyMyRedisUtil).leftPopListByKeyAndGetDataStr(stubKey);

        Optional<Object> actual = spyMyRedisUtil.leftPopListByKeyAndGetDataObj(stubKey, MyTestPOJO.class);

        Assertions.assertFalse(actual.isPresent());
        Mockito.verify(mockObjectMapper, Mockito.times(0)).readValue(stubJsonValue, MyTestPOJO.class);
    }

    @Test
    void GivenKeyExistInRedisButIsNotJson_WhenLeftPopListByKeyAndGetDataObj_ThenThrowMyRedisException() throws JsonProcessingException {
        String stubKey = "keyForTesting.";
        String stubValue = "Not json.";
        Mockito.doReturn(Optional.of(stubValue)).when(spyMyRedisUtil).leftPopListByKeyAndGetDataStr(stubKey);
        Mockito.when(mockObjectMapper.readValue(stubValue, MyTestPOJO.class)).thenThrow(JsonProcessingException.class);

        MyRedisException actual = Assertions.assertThrows(MyRedisException.class, () -> {
            spyMyRedisUtil.leftPopListByKeyAndGetDataObj(stubKey, MyTestPOJO.class);
        });

        Assertions.assertEquals("The value in redis is not json format cause JsonProcessingException, value: Not json.", actual.getMessage());
        Mockito.verify(mockObjectMapper, Mockito.times(1)).readValue(stubValue, MyTestPOJO.class);
    }

    @Test
    void GivenPOJOList_WhenRightPushObjListByKey_ThenInvokeAndPassExpectedArgsToRightPushStrListByKeyMethod() throws JsonProcessingException {
        String stubKey = "keyForTesting.";
        List<MyTestPOJO> stubPojoList = new ArrayList<>();
        MyTestPOJO stubPojo = new MyTestPOJO(1, "stubName1");
        MyTestPOJO stubPojo2 = new MyTestPOJO(2, "stubName2");
        stubPojoList.add(stubPojo);
        stubPojoList.add(stubPojo2);
        ObjectMapper objectMapper = new ObjectMapper();
        ArrayNode stubArrayNode = objectMapper.valueToTree(stubPojoList); //MyTestPOJO要有getter才能用此方法。
        List<String> stubJsonStrList = new ArrayList<>();
        stubJsonStrList.add("{\"id\":1,\"name\":\"stubName1\"}");
        stubJsonStrList.add("{\"id\":2,\"name\":\"stubName2\"}");
        Instant stubInstant = Instant.now();
        Mockito.when(mockObjectMapper.valueToTree(stubPojoList)).thenReturn(stubArrayNode);

        spyMyRedisUtil.rightPushObjListByKey(stubKey, stubPojoList, stubInstant);

        Mockito.verify(mockObjectMapper, Mockito.times(1)).valueToTree(stubPojoList);
        Mockito.verify(spyMyRedisUtil, Mockito.times(1)).rightPushStrListByKey(stubKey, stubJsonStrList, stubInstant);
    }

    @Test
    void Given_WhenRemoveAllKeys_ThenInvokeExpectedMethodOfStringRedisTemplate() {
        String expectedKeysPattern = "*";
        Set<String> stubKeySet = new HashSet<>();
        Mockito.when(mockStringRedisTemplate.keys(expectedKeysPattern)).thenReturn(stubKeySet);

        spyMyRedisUtil.removeAllKeys();

        Mockito.verify(mockStringRedisTemplate, Mockito.times(1)).keys(expectedKeysPattern);
        Mockito.verify(mockStringRedisTemplate, Mockito.times(1)).delete(stubKeySet);
    }

}