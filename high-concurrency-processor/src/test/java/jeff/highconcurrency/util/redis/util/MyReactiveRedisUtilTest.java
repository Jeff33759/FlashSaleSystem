package jeff.highconcurrency.util.redis.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import jeff.highconcurrency.util.redis.exception.MyReactiveRedisException;
import jeff.test.pojo.MyTestPOJO;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.ReactiveListOperations;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.data.redis.core.ReactiveValueOperations;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.time.Instant;
import java.util.*;

@SpringBootTest(classes = MyReactiveRedisUtilTest.class)
class MyReactiveRedisUtilTest {

    @Spy
    @InjectMocks
    MyReactiveRedisUtil spyMyReactiveRedisUtil; //待測元件

    @Mock
    private ReactiveStringRedisTemplate mockReactiveStringRedisTemplate;
    @Mock
    private ReactiveValueOperations mockReactiveValueOperations; //ReactiveStringRedisTemplate.opsForValue實際上是呼叫此元件去做後續動作，所以必須再Mock一層，方便Mockito.verify去驗證
    @Mock
    private ReactiveListOperations mockReactiveListOperations; //理由同上

    @Mock
    private ObjectMapper mockObjectMapper;

    @BeforeEach
    void mockReactiveRedisTemplate() {
        Mockito.when(mockReactiveStringRedisTemplate.opsForValue()).thenReturn(this.mockReactiveValueOperations);
        Mockito.when(mockReactiveStringRedisTemplate.opsForList()).thenReturn(this.mockReactiveListOperations);
    }

    @Test
    void GivenKeyExistInRedis_WhenGetDataStrByKey_ThenReturnMonoOptionalWhichContainsString() {
        String stubKey = "keyForTesting.";
        String stubValue = "valueForTesting.";
        Mono<String> stubMono = Mono.just(stubValue);
        Mockito.when(mockReactiveStringRedisTemplate.opsForValue().get(stubKey)).thenReturn(stubMono);

        Mono<Optional<String>> actualMono = spyMyReactiveRedisUtil.getDataStrByKey(stubKey);

        StepVerifier.create(actualMono)
                .expectNext(Optional.of(stubValue))
                .expectComplete()
                .verify();
    }

    @Test
    void GivenKeyDoesNotExistInRedis_WhenGetDataStrByKey_ThenReturnMonoEmptyOptional() {
        String stubKey = "keyForTesting.";
        Mono<String> stubMono = Mono.empty();
        Mockito.when(mockReactiveStringRedisTemplate.opsForValue().get(stubKey)).thenReturn(stubMono);

        Mono<Optional<String>> actualMono = spyMyReactiveRedisUtil.getDataStrByKey(stubKey);

        StepVerifier.create(actualMono)
                .expectNext(Optional.empty())
                .expectComplete()
                .verify();
    }

    @Test
    void GivenKeyAndStringValue_WhenPutDataStrByKey_ThenInvokeExpectedMethodOfReactiveStringRedisTemplateAndReturnEmptyMono() {
        String stubKey = "keyForTesting.";
        String stubValue = "valueForTesting.";
        Mono<Void> stubMono = Mono.empty();
        Mockito.when(mockReactiveValueOperations.set(Mockito.anyString(), Mockito.anyString())).thenReturn(stubMono);

        Mono<Void> actualMono = spyMyReactiveRedisUtil.putDataStrByKey(stubKey, stubValue);

        StepVerifier.create(actualMono)
                .expectNextCount(0)
                .expectComplete()
                .verify();
        Mockito.verify(this.mockReactiveValueOperations, Mockito.times(1)).set(stubKey, stubValue);
    }

    @Test
    void GivenKeyAndStringValueAndExpiration_WhenPutDataStrByKeyAndSetExpiration_ThenInvokeExpectedMethodOfReactiveStringRedisTemplateAndReturnEmptyMono() {
        try (MockedStatic<Duration> mockDuration = Mockito.mockStatic(Duration.class)) {
            String stubKey = "keyForTesting.";
            String stubValue = "valueForTesting.";
            Instant stubExpiration = Instant.ofEpochMilli(1709369729900L); // 2024-03-02 16:55:29
            Duration stubDuration = Duration.between(Instant.now(), stubExpiration);
            mockDuration.when(() -> Duration.between(Mockito.any(), Mockito.eq(stubExpiration))).thenReturn(stubDuration);
            Mono<Void> stubMono = Mono.empty();
            Mockito.when(mockReactiveValueOperations.set(Mockito.anyString(), Mockito.anyString(), Mockito.eq(stubDuration))).thenReturn(stubMono);

            Mono<Void> actualMono = spyMyReactiveRedisUtil.putDataStrByKeyAndSetExpiration(stubKey, stubValue, stubExpiration);

            StepVerifier.create(actualMono)
                    .expectNextCount(0)
                    .expectComplete()
                    .verify();
            Mockito.verify(this.mockReactiveValueOperations, Mockito.times(1)).set(stubKey, stubValue, stubDuration);
        }
    }

    @Test
    void GivenGetDataStrByKeyMethodReturnsOptionalWhichContainsJsonString_WhenGetDataObjByKey_ThenReturnMonoOptionalWhichContainsExpectedPOJO() throws JsonProcessingException {
        String stubKey = "keyForTesting.";
        String stubJsonValue = "{\"id\":1,\"name\":\"stubName.\"}";
        Mono<Optional<String>> stubStrMono = Mono.just(Optional.of(stubJsonValue));
        Mockito.doReturn(stubStrMono).when(spyMyReactiveRedisUtil).getDataStrByKey(stubKey);
        MyTestPOJO stubPOJO = new MyTestPOJO(1, "stubName.");
        Mockito.when(mockObjectMapper.readValue(stubJsonValue, MyTestPOJO.class)).thenReturn(stubPOJO);

        Mono<Optional<Object>> actualMono = spyMyReactiveRedisUtil.getDataObjByKey(stubKey, MyTestPOJO.class);

        StepVerifier.create(actualMono)
                .expectNext(Optional.of(stubPOJO))
                .expectComplete()
                .verify();
    }

    @Test
    void GivenGetDataStrByKeyMethodReturnsEmptyOptional_WhenGetDataObjByKey_ThenReturnMonoEmptyOptional() throws JsonProcessingException {
        String stubKey = "keyForTesting.";
        Mono<Optional<Object>> stubMono = Mono.just(Optional.empty());
        Mockito.doReturn(stubMono).when(spyMyReactiveRedisUtil).getDataStrByKey(stubKey);

        Mono<Optional<Object>> actualMono = spyMyReactiveRedisUtil.getDataObjByKey(stubKey, MyTestPOJO.class);

        StepVerifier.create(actualMono)
                .expectNext(Optional.empty())
                .expectComplete()
                .verify();
        Mockito.verify(mockObjectMapper, Mockito.times(0)).readValue(Mockito.anyString(), Mockito.eq(MyTestPOJO.class));
    }

    @Test
    void GivenGetDataStrByKeyMethodReturnsOptionalWhichContainsNotJsonString_WhenGetDataObjByKey_ThenThrowMyReactiveRedisException() throws JsonProcessingException {
        String stubKey = "keyForTesting.";
        String stubJsonValue = "not json.";
        Mono<Optional<String>> stubMono = Mono.just(Optional.of(stubJsonValue));
        Mockito.doReturn(stubMono).when(spyMyReactiveRedisUtil).getDataStrByKey(stubKey);
        Mockito.doThrow(JsonProcessingException.class).when(mockObjectMapper).readValue(stubJsonValue, MyTestPOJO.class);

        Mono<Optional<Object>> actualMono = spyMyReactiveRedisUtil.getDataObjByKey(stubKey, MyTestPOJO.class);

        StepVerifier.create(actualMono)
                .expectError(MyReactiveRedisException.class)
                .verifyThenAssertThat()
                .hasOperatorErrorWithMessage("The value in redis is not json format cause JsonProcessingException, value: not json.");
    }

    @Test
    void GivenPOJO_WhenPutDataObjByKey_ThenInvokeWriteValueAsStringMethodOfObjectMapperAndPassExpectedJsonStrToPutDataStrByKeyMethodAndReturnEmptyMono() throws JsonProcessingException {
        String stubKey = "keyForTesting.";
        MyTestPOJO stubPOJO = new MyTestPOJO(1, "stubName.");
        String stubJsonValueFromConvertedStubPOJO = "{\"id\":1,\"stubName.\"}";
        Mockito.when(mockObjectMapper.writeValueAsString(stubPOJO)).thenReturn(stubJsonValueFromConvertedStubPOJO);
        Mono<Object> stubMono = Mono.empty();
        Mockito.doReturn(stubMono).when(spyMyReactiveRedisUtil).putDataStrByKey(Mockito.anyString(), Mockito.anyString());

        Mono<Void> actualMono = spyMyReactiveRedisUtil.putDataObjByKey(stubKey, stubPOJO);

        StepVerifier.create(actualMono)
                .expectNextCount(0)
                .expectComplete()
                .verify();
        Mockito.verify(mockObjectMapper, Mockito.times(1)).writeValueAsString(stubPOJO);
        Mockito.verify(spyMyReactiveRedisUtil, Mockito.times(1)).putDataStrByKey(stubKey, stubJsonValueFromConvertedStubPOJO);
    }

    @Test
    void GivenJsonProcessingExceptionFromObjectMapper_WhenPutDataObjByKey_ThenThrowMyReactiveRedisException() throws JsonProcessingException {
        String stubKey = "keyForTesting.";
        MyTestPOJO stubPOJO = new MyTestPOJO(1, "stubName.");
        Mockito.when(mockObjectMapper.writeValueAsString(stubPOJO)).thenThrow(JsonProcessingException.class); //JsonProcessingException不讓人new
        Mono<Object> stubMono = Mono.empty();
        Mockito.doReturn(stubMono).when(spyMyReactiveRedisUtil).putDataStrByKey(Mockito.anyString(), Mockito.anyString());

        MyReactiveRedisException actual = Assertions.assertThrows(MyReactiveRedisException.class, () -> {
            spyMyReactiveRedisUtil.putDataObjByKey(stubKey, stubPOJO);
        });

        Assertions.assertEquals("Some error occurred when converting POJO into jsonStr cause JsonProcessingException.", actual.getMessage());
        Assertions.assertInstanceOf(JsonProcessingException.class, actual.getCause());
        Mockito.verify(mockObjectMapper, Mockito.times(1)).writeValueAsString(stubPOJO);
        Mockito.verify(spyMyReactiveRedisUtil, Mockito.times(0)).putDataStrByKey(Mockito.anyString(), Mockito.anyString());
    }

    @Test
    void GivenPOJOAndExpiration_WhenPutDataObjByKeyAndSetExpiration_ThenInvokeWriteValueAsStringMethodOfObjectMapperAndPassExpectedJsonStrToPutDataStrByKeyMethodAndReturnEmptyMono() throws JsonProcessingException {
        String stubKey = "keyForTesting.";
        MyTestPOJO stubPOJO = new MyTestPOJO(1, "stubName.");
        String stubJsonValueFromConvertedStubPOJO = "{\"id\":1,\"stubName.\"}";
        Instant stubExpiration = Instant.ofEpochMilli(1709369729900L); // 2024-03-02 16:55:29
        Mockito.when(mockObjectMapper.writeValueAsString(stubPOJO)).thenReturn(stubJsonValueFromConvertedStubPOJO);
        Mono<Object> stubMono = Mono.empty();
        Mockito.doReturn(stubMono).when(spyMyReactiveRedisUtil).putDataStrByKeyAndSetExpiration(Mockito.anyString(), Mockito.anyString(), Mockito.eq(stubExpiration));

        Mono<Void> actualMono = spyMyReactiveRedisUtil.putDataObjByKeyAndSetExpiration(stubKey, stubPOJO, stubExpiration);

        StepVerifier.create(actualMono)
                .expectNextCount(0)
                .expectComplete()
                .verify();
        Mockito.verify(mockObjectMapper, Mockito.times(1)).writeValueAsString(stubPOJO);
        Mockito.verify(spyMyReactiveRedisUtil, Mockito.times(1)).putDataStrByKeyAndSetExpiration(stubKey, stubJsonValueFromConvertedStubPOJO, stubExpiration);
    }

    @Test
    void GivenKeyExistInRedis_WhenLeftPopListByKeyAndGetDataStr_ThenReturnMonoOptionalWhichContainsExpectedString() {
        String stubKey = "keyForTesting.";
        String stubJsonValue = "{\"id\":1,\"name\":\"stubName.\"}";
        Mono<String> stubMono = Mono.just(stubJsonValue);
        Mockito.when(mockReactiveStringRedisTemplate.opsForList().leftPop(stubKey)).thenReturn(stubMono);

        Mono<Optional<String>> actualMono = spyMyReactiveRedisUtil.leftPopListByKeyAndGetDataStr(stubKey);

        StepVerifier.create(actualMono)
                .expectNext(Optional.of(stubJsonValue))
                .expectComplete()
                .verify();
        Mockito.verify(mockReactiveListOperations, Mockito.times(1)).leftPop(stubKey);
    }

    @Test
    void GivenKeyDoesNotExistInRedis_WhenLeftPopListByKeyAndGetDataStr_ThenReturnMonoEmptyOptional() {
        String stubKey = "keyForTesting.";
        Mono<String> stubMono = Mono.empty();
        Mockito.when(mockReactiveStringRedisTemplate.opsForList().leftPop(stubKey)).thenReturn(stubMono);

        Mono<Optional<String>> actualMono = spyMyReactiveRedisUtil.leftPopListByKeyAndGetDataStr(stubKey);

        StepVerifier.create(actualMono)
                .expectNext(Optional.empty())
                .expectComplete()
                .verify();
    }

    @Test
    void GivenKeyExistInRedis_WhenLeftPopListByKeyAndGetDataObj_ThenReturnMonoOptionalWhichContainsExpectedPOJO() throws JsonProcessingException {
        String stubKey = "keyForTesting.";
        String stubJsonValue = "{\"id\":1,\"name\":\"stubName.\"}";
        Mono<Optional<String>> stubMono = Mono.just(Optional.of(stubJsonValue));
        MyTestPOJO stubPOJO = new MyTestPOJO(1, "stubName.");
        Mockito.when(mockObjectMapper.readValue(stubJsonValue, MyTestPOJO.class)).thenReturn(stubPOJO);
        Mockito.doReturn(stubMono).when(spyMyReactiveRedisUtil).leftPopListByKeyAndGetDataStr(stubKey);

        Mono<Optional<Object>> actualMono = spyMyReactiveRedisUtil.leftPopListByKeyAndGetDataObj(stubKey, MyTestPOJO.class);

        StepVerifier.create(actualMono)
                .expectNext(Optional.of(stubPOJO))
                .expectComplete()
                .verify();
    }

    @Test
    void GivenKeyDoesNotExistInRedis_WhenLeftPopListByKeyAndGetDataObj_ThenReturnMonoEmptyOptional() throws JsonProcessingException {
        String stubKey = "keyForTesting.";
        String stubJsonValue = "{\"id\":1,\"name\":\"stubName.\"}";
        Mockito.doReturn(Mono.just(Optional.empty())).when(spyMyReactiveRedisUtil).leftPopListByKeyAndGetDataStr(stubKey);

        Mono<Optional<Object>> actualMono = spyMyReactiveRedisUtil.leftPopListByKeyAndGetDataObj(stubKey, MyTestPOJO.class);

        StepVerifier.create(actualMono)
                .expectNext(Optional.empty())
                .expectComplete()
                .verify();
        Mockito.verify(mockObjectMapper, Mockito.times(0)).readValue(stubJsonValue, MyTestPOJO.class);
    }

    @Test
    void GivenKeyExistInRedisButIsNotJson_WhenLeftPopListByKeyAndGetDataObj_ThenThrowMyReactiveRedisException() throws JsonProcessingException {
        String stubKey = "keyForTesting.";
        String stubValue = "Not json.";
        Mono<Optional<String>> stubMono = Mono.just(Optional.of(stubValue));
        Mockito.doReturn(stubMono).when(spyMyReactiveRedisUtil).leftPopListByKeyAndGetDataStr(stubKey);
        Mockito.when(mockObjectMapper.readValue(stubValue, MyTestPOJO.class)).thenThrow(JsonProcessingException.class);

        Mono<Optional<Object>> actualMono = spyMyReactiveRedisUtil.leftPopListByKeyAndGetDataObj(stubKey, MyTestPOJO.class);

        StepVerifier.create(actualMono)
                .expectError(MyReactiveRedisException.class)
                .verifyThenAssertThat()
                .hasOperatorErrorWithMessage("The value in redis is not json format cause JsonProcessingException, value: Not json.");
        Mockito.verify(mockObjectMapper, Mockito.times(1)).readValue(stubValue, MyTestPOJO.class);
    }

    @Test
    void GivenPOJOList_WhenRightPushObjListByKeyAndSetExpiration_ThenInvokeAndPassExpectedArgsToRightPushStrListByKeyAndSetExpirationMethodAndReturnEmptyMono() throws JsonProcessingException {
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
        Mono<Object> stubMono = Mono.empty();
        Mockito.doReturn(stubMono).when(spyMyReactiveRedisUtil).rightPushStrListByKeyAndSetExpiration(Mockito.anyString(), Mockito.anyList(), Mockito.any());

        Mono<Void> actualMono = spyMyReactiveRedisUtil.rightPushObjListByKeyAndSetExpiration(stubKey, stubPojoList, stubInstant);

        StepVerifier.create(actualMono)
                .expectNextCount(0)
                .verifyComplete();
        Mockito.verify(mockObjectMapper, Mockito.times(1)).valueToTree(stubPojoList);
        Mockito.verify(spyMyReactiveRedisUtil, Mockito.times(1)).rightPushStrListByKeyAndSetExpiration(stubKey, stubJsonStrList, stubInstant);
    }

    @Test
    void GivenKey_WhenRemoveKey_ThenInvokeExpectedMethodOfReactiveStringRedisTemplateAndReturnEmptyMono() {
        String stubKey = "keyForTesting.";
        Mono<Boolean> deleteResultMono = Mono.just(true);
        Mockito.when(mockReactiveValueOperations.delete(stubKey)).thenReturn(deleteResultMono);

        Mono<Void> actualMono = spyMyReactiveRedisUtil.removeKey(stubKey);

        StepVerifier.create(actualMono)
                .expectNextCount(0)
                .verifyComplete();
        Mockito.verify(this.mockReactiveValueOperations, Mockito.times(1)).delete(stubKey);
    }

    @Test
    void Given_WhenRemoveAllKeys_ThenInvokeExpectedMethodOfStringRedisTemplateAndReturnEmptyMono() {
        String expectedKeysPattern = "*";
        Flux<String> stubFlux = Flux.just("key1", "key2", "key3");
        Mockito.when(mockReactiveStringRedisTemplate.keys(expectedKeysPattern)).thenReturn(stubFlux);
        Mono<Long> deleteResultMono = Mono.just(3L);
        Mockito.when(mockReactiveStringRedisTemplate.delete(stubFlux)).thenReturn(deleteResultMono);

        Mono<Void> actualMono = spyMyReactiveRedisUtil.removeAllKeys();

        StepVerifier.create(actualMono)
                .expectNextCount(0)
                .verifyComplete();
        Mockito.verify(mockReactiveStringRedisTemplate, Mockito.times(1)).keys(expectedKeysPattern);
        Mockito.verify(mockReactiveStringRedisTemplate, Mockito.times(1)).delete(stubFlux);
    }

}