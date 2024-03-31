package jeff.highconcurrency.util.redis.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import jeff.highconcurrency.util.redis.exception.MyReactiveRedisException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * 自訂義的Redis工具包，使用spring-boot-starter-data-redis-reactive作為訪問Redis的API，其底層實作預設為Lettuce。
 * 不用在這裡去.subscribe()，回傳Mono讓外面的呼叫者去決定何時subscribe。
 * <p>
 * redis本身雖是SingleProcess-MultiThread，可是實際上處理CRUD的只有一個Thread，所以即使併發量再高，也不用擔心執行緒競爭與上鎖的問題。
 * 利用這個特性，結合redis-list的lpop&rpush去做一個原子性又可以在各服務間共享的Queue，此Queue就拿來應付快閃搶購的業務。
 */
@Component
public class MyReactiveRedisUtil {

    @Autowired
    private ReactiveStringRedisTemplate reactiveSRedisTemplate;

    @Autowired
    private ObjectMapper mapper;


    /**
     * 用Key去redis拉資料，拉出資料為字串型別。
     *
     * @return 若該key存在於redis，則回傳有值的Optional(包裹String)；若不存在於redis，則回傳空Optional
     */
    public Mono<Optional<String>> getDataStrByKey(String key) {
        return reactiveSRedisTemplate.opsForValue().get(key)
                .defaultIfEmpty("")
                .map(strValue -> strValue.isEmpty() ? Optional.empty() : Optional.of(strValue));
    }

    /**
     * 將某字串快取進Redis，該字串不一定要是json。
     */
    public Mono<Void> putDataStrByKey(String key, String cacheStr) {
        return reactiveSRedisTemplate.opsForValue().set(key, cacheStr)
                .then();
    }

    /**
     * 將某字串快取進Redis，該字串不一定要是json，並且設置超時。
     */
    public Mono<Void> putDataStrByKeyAndSetExpiration(String key, String cacheStr, Instant expiration) {
        return reactiveSRedisTemplate.opsForValue().set(key, cacheStr, Duration.between(Instant.now(), expiration))
                .then();
    }

    /**
     * 用Key去redis拉資料(資料假定都是Json)，將Json轉成POJO後回傳。
     *
     * @param clazz 欲轉成的POJO
     * @return 若該key存在於redis，則回傳有值的Optional(包裹POJO)；若不存在於redis，則回傳空Optional
     */
    public Mono<Optional<Object>> getDataObjByKey(String key, Class clazz) {
        Mono<Optional<String>> monoOptionalJsonStr = this.getDataStrByKey(key);// 若key不存在於redis，則為空Optional

        return monoOptionalJsonStr.map(optionalJsonStr -> { // 因經過defaultIfEmpty包裝，所以不會是空的流，一定會跑進來
            if (!optionalJsonStr.isPresent()) {
                return Optional.empty();
            }

            String jsonStr = optionalJsonStr.get();
            try {
                return Optional.of(mapper.readValue(jsonStr, clazz));
            } catch (JsonProcessingException e) {
                throw new MyReactiveRedisException(String.format("The value in redis is not json format cause JsonProcessingException, value: %s", jsonStr), e);
            }
        });
    }

    /**
     * 將一個POJO以Json型式存入redis。
     *
     * @param key
     * @param cacheObj
     */
    public Mono<Void> putDataObjByKey(String key, Object cacheObj) {
        try {
            String jsonStr = mapper.writeValueAsString(cacheObj);
            return this.putDataStrByKey(key, jsonStr);
        } catch (JsonProcessingException e){
            throw new MyReactiveRedisException("Some error occurred when converting POJO into jsonStr cause JsonProcessingException.", e);
        }
    }

    /**
     * 將一個POJO以Json型式存入redis，並且設置超時。
     */
    public Mono<Void> putDataObjByKeyAndSetExpiration(String key, Object cacheObj, Instant expiration) {
        try {
            String jsonStr = mapper.writeValueAsString(cacheObj);
            return this.putDataStrByKeyAndSetExpiration(key, jsonStr, expiration);
        } catch (JsonProcessingException e){
            throw new MyReactiveRedisException("Some error occurred when converting POJO into jsonStr cause JsonProcessingException.", e);
        }
    }

    /**
     * 得到redis-list第一筆資料，同時移除該元素，資料為字串，可以不是Json。
     *
     * @return 若該key存在於redis，則回傳有值的Optional(包裹String)；若key不存在於redis或者陣列為空(等同於key不存在)，則回傳空Optional
     */
    public Mono<Optional<String>> leftPopListByKeyAndGetDataStr(String key) {
        return reactiveSRedisTemplate.opsForList().leftPop(key)
                .defaultIfEmpty("")
                .map(strValue -> strValue.isEmpty() ? Optional.empty() : Optional.of(strValue));
    }

    /**
     * 將List內的資料依序插入redis-list，每一筆資料都是插入在最後一筆之後，並且設置超時。
     *
     * @param cacheStrList 一個String的List，可以不是Json字串
     * @param expiration   key的有效時間
     */
    public Mono<Void> rightPushStrListByKeyAndSetExpiration(String key, List<String> cacheStrList, Instant expiration) {
        return reactiveSRedisTemplate.opsForList().rightPushAll(key, cacheStrList)
                .then(Mono.fromRunnable(() -> reactiveSRedisTemplate.expireAt(key, expiration)));
    }

    /**
     * 得到redis-list第一筆資料，同時移除該元素，並且將得到的資料轉成POJO(前提該資料必須是JSON字串)。
     *
     * @param clazz 欲轉成的POJO
     * @return 若該key存在於redis，則回傳有值的Optional(包裹POJO)；若key不存在於redis或者陣列為空(等同於key不存在)，則回傳空Optional
     */
    public Mono<Optional<Object>> leftPopListByKeyAndGetDataObj(String key, Class clazz) {
        Mono<Optional<String>> monoOptionalJsonStr = this.leftPopListByKeyAndGetDataStr(key);

        return monoOptionalJsonStr.map(optionalJsonStr -> { // 因經過defaultIfEmpty包裝，所以不會是空的流，一定會跑進來
            if (!optionalJsonStr.isPresent()) {
                return Optional.empty();
            }

            String jsonStr = optionalJsonStr.get();
            try {
                return Optional.of(mapper.readValue(jsonStr, clazz));
            } catch (JsonProcessingException e){
                throw new MyReactiveRedisException(String.format("The value in redis is not json format cause JsonProcessingException, value: %s", jsonStr), e);
            }
        });
    }

    /**
     * 將List內的資料依序插入redis-list，每一筆資料都是插入在最後一筆之後，並且設置超時
     *
     * @param cacheList  一個POJO的List，可以轉成Json
     * @param expiration 有效時間
     */
    public Mono<Void> rightPushObjListByKeyAndSetExpiration(String key, List cacheList, Instant expiration) {
        ArrayNode jsonArr = mapper.valueToTree(cacheList);
        List<String> jsonStrList = new ArrayList<>();

        jsonArr.forEach(json -> {
            jsonStrList.add(json.toString());
        });

        return this.rightPushStrListByKeyAndSetExpiration(key, jsonStrList, expiration);
    }

    /**
     * 刪除某個Key。
     */
    public Mono<Void> removeKey(String key) {
        return reactiveSRedisTemplate.opsForValue().delete(key)
                .then();
    }

    /**
     * 移除redis上所有的key。
     */
    @SuppressWarnings("unchecked") //此方法執行maven install的時候會跳型別檢查的警告，不知為何，總之目前先用這個叫他別該
    public Mono<Void> removeAllKeys() {
        Flux<String> fluxAllKeys = reactiveSRedisTemplate.keys("*");

        return fluxAllKeys.thenMany(reactiveSRedisTemplate.delete(fluxAllKeys))
                .then();
    }


}
