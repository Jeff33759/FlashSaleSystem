package jeff.redis.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import jeff.redis.exception.MyRedisException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * 自訂義的Redis工具包，使用SpringDataRedis作為訪問Redis的API，其底層實作預設為Lettuce。
 * <p>
 * redis本身雖是SingleProcess-MultiThread，可是實際上處理CRUD的只有一個Thread，所以即使併發量再高，也不用擔心執行緒競爭與上鎖的問題。
 * 利用這個特性，結合redis-list的lpush&rpop去做一個原子性又可以在各服務間共享的Queue，此Queue就拿來應付快閃搶購的業務。
 */
@Component
public class MyRedisUtil {

    @Autowired
    private StringRedisTemplate sRedisTemplate;

    @Autowired
    private ObjectMapper mapper;


    /**
     * 用Key去redis拉資料，拉出資料為字串型別。
     *
     * @return 若該key存在於redis，則回傳有值的Optional(包裹String)；若不存在於redis，則回傳空Optional
     */
    public Optional<String> getDataStrByKey(String key) {
        String strValue = sRedisTemplate.opsForValue().get(key);
        return Optional.ofNullable(strValue);
    }

    /**
     * 將某字串快取進Redis，該字串不一定要是json。
     */
    public void putDataStrByKey(String key, String cacheStr) {
        sRedisTemplate.opsForValue().set(key, cacheStr);
    }

    /**
     * 用Key去redis拉資料(資料假定都是Json)，將Json轉成POJO後回傳。
     *
     * @param clazz 欲轉成的POJO
     * @return 若該key存在於redis，則回傳有值的Optional(包裹POJO)；若不存在於redis，則回傳空Optional
     */
    public Optional<Object> getDataObjByKey(String key, Class clazz) {
        Optional<String> optionalJsonStr = this.getDataStrByKey(key);// 若key不存在於redis，則為空Optional

        if (!optionalJsonStr.isPresent()) {
            return Optional.empty();
        }

        String jsonStr = optionalJsonStr.get();
        try {
            return Optional.of(mapper.readValue(jsonStr, clazz));
        } catch (JsonProcessingException e) {
            throw new MyRedisException(String.format("The value in redis is not json format cause JsonProcessingException, value: %s", jsonStr));
        }

    }

    /**
     * 將一個POJO以Json型式存入redis。
     *
     * @param key
     * @param cacheObj
     */
    public void putDataObjByKey(String key, Object cacheObj) {
        try {
            String jsonStr = mapper.writeValueAsString(cacheObj);
            this.putDataStrByKey(key, jsonStr);
        }catch (JsonProcessingException e){
            throw new MyRedisException("Some error occurred when converting POJO into jsonStr cause JsonProcessingException.");
        }
    }

    /**
     * 得到redis-list第一筆資料，同時移除該元素，資料為字串，可以不是Json。
     *
     * @return 若該key存在於redis，則回傳有值的Optional(包裹String)；若key不存在於redis或者陣列為空(等同於key不存在)，則回傳空Optional
     */
    public Optional<String> leftPopListByKeyAndGetDataStr(String key) {
        String strValue = sRedisTemplate.opsForList().leftPop(key); //若陣列為空或該key不存在，則為null
        return Optional.ofNullable(strValue);
    }

    /**
     * 將List內的資料依序插入redis-list，每一筆資料都是插入在最後一筆之後。
     *
     * @param cacheStrList 一個String的List，可以不是Json字串
     * @param expiration   key的有效時間
     */
    public void rightPushStrListByKey(String key, List<String> cacheStrList, Instant expiration) {
        sRedisTemplate.opsForList().rightPushAll(key, cacheStrList);
        sRedisTemplate.expireAt(key, expiration);
    }

    /**
     * 得到redis-list第一筆資料，同時移除該元素，並且將得到的資料轉成POJO(前提該資料必須是JSON字串)。
     *
     * @param clazz 欲轉成的POJO
     * @return 若該key存在於redis，則回傳有值的Optional(包裹POJO)；若key不存在於redis或者陣列為空(等同於key不存在)，則回傳空Optional
     */
    public Optional<Object> leftPopListByKeyAndGetDataObj(String key, Class clazz) {
        Optional<String> optionalJsonStr = this.leftPopListByKeyAndGetDataStr(key);

        if (!optionalJsonStr.isPresent()) {
            return Optional.empty();
        }

        String jsonStr = optionalJsonStr.get();
        try {
            return Optional.of(mapper.readValue(jsonStr, clazz));
        }catch (JsonProcessingException e){
            throw new MyRedisException(String.format("The value in redis is not json format cause JsonProcessingException, value: %s", jsonStr));
        }
    }

    /**
     * 將List內的資料依序插入redis-list，每一筆資料都是插入在最後一筆之後。
     *
     * @param cacheList  一個POJO的List，可以轉成Json
     * @param expiration 有效時間
     */
    public void rightPushObjListByKey(String key, List cacheList, Instant expiration) {
        ArrayNode jsonArr = mapper.valueToTree(cacheList);
        List<String> jsonStrList = new ArrayList<>();

        jsonArr.forEach(json -> {
            jsonStrList.add(json.toString());
        });

        this.rightPushStrListByKey(key, jsonStrList, expiration);
    }

    /**
     * 移除redis上所有的key。
     */
    @SuppressWarnings("unchecked") //此方法執行maven install的時候會跳型別檢查的警告，不知為何，總之目前先用這個叫他別該
    public void removeAllKeys() {
        Set<String> allKeys = sRedisTemplate.keys("*");
        sRedisTemplate.delete(allKeys);
    }

}