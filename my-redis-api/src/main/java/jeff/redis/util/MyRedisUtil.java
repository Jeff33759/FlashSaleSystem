package jeff.redis.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import jeff.redis.exception.MyRedisException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 自訂義的Redis工具包，使用SpringDataRedis作為訪問Redis的API，其底層實作預設為Lettuce。
 * <p>
 * redis本身雖是SingleProcess-MultiThread，可是實際上處理CRUD的只有一個Thread，所以即使併發量再高，也不用擔心執行緒競爭與上鎖的問題。
 * 利用這個特性，結合redis-list的lpop&rpush去做一個原子性又可以在各服務間共享的Queue，此Queue就拿來應付快閃搶購的業務。
 */
@Component
public class MyRedisUtil {

    @Autowired
    private StringRedisTemplate sRedisTemplate;

    @Autowired
    private ObjectMapper mapper;

    private static final String LOCK_PREFIX = "lock:";

    private static final String INCR_PREFIX = "incr:";

    private static final String SET_PREFIX = "set:";


    /**
     * 增加某個 計數key 的值。
     *
     * @param key Redis 中key
     * @param delta 增量值(必為正整數)
     * @return 應加後的新值
     * @throws MyRedisException 如果 delta 不是正整數，則拋出
     */
    public Long increaseAndGetNewValue(String key, long delta) {
        if (delta <= 0) {
            throw new MyRedisException("Delta must be a positive integer.");
        }

        String incrKey = INCR_PREFIX + key;

        return sRedisTemplate.opsForValue().increment(incrKey, delta);
    }

    /**
     * 增加某個 計數key 的值，並為該計數key設置過期時間
     *
     * @param key Redis 中的key
     * @param delta 增量值(必為正整數)
     * @param expiration 過期時間
     * @return 增加後的新值
     * @throws MyRedisException 如果 delta 不是正整数
     */
    public Long increaseAndSetExpirationAndGetNewValue(String key, long delta, Instant expiration) {
        if (delta <= 0) {
            throw new MyRedisException("Delta must be a positive integer.");
        }

        String incrKey = INCR_PREFIX + key;

        List<Object> results = sRedisTemplate.executePipelined((RedisCallback<Object>) connection -> {
            connection.stringCommands().incrBy(incrKey.getBytes(), delta);
            connection.keyCommands().expireAt(incrKey.getBytes(), expiration.getEpochSecond());

            return null;
        });

        // 第一個命令是 incrBy，所以要取值，要用results.get(0)
        return results != null && !results.isEmpty() ? (Long) results.get(0) : null;
    }

    /**
     * 減少某個 計數key 的值。
     *
     * @param key Redis 中的key
     * @param delta 減量值(必為正整數)
     * @return 減少後的新值
     * @throws MyRedisException 如果 delta 不是正整數，則拋出
     */
    public Long decreaseAndGetNewValue(String key, long delta) {
        if (delta <= 0) {
            throw new MyRedisException("Delta must be a positive integer.");
        }

        String incrKey = INCR_PREFIX + key;

        return sRedisTemplate.opsForValue().increment(incrKey, -delta);
    }

    /**
     * 重置某個計數key的值
     *
     * @param key Redis 中的key
     */
    public void resetIncrementValue(String key) {
        String incrKey = INCR_PREFIX + key;

        this.removeKey(incrKey);
    }

    /**
     * 獲取分布式鎖。
     *
     * @param key 鎖的Key
     * @param lockDuration 鎖的持續時間，從成功獲取鎖的瞬間開始計算
     * @return 獲取成功則回傳true，失敗則回傳false
     */
    public boolean getLock(String key, Duration lockDuration) {
        String lockKey = LOCK_PREFIX + key;

        Boolean success = sRedisTemplate.boundValueOps(lockKey)
                .setIfAbsent("LOCKED", lockDuration);

        return Boolean.TRUE.equals(success);
    }

    /**
     * 檢查某個鎖頭是否上鎖中。
     * @param key 鎖的key
     * @return 上鎖中回傳true，沒上鎖則回傳false
     */
    public boolean isLocking(String key) {
        String lockKey = LOCK_PREFIX + key;

        return this.getDataStrByKey(lockKey).isPresent();
    }

    /**
     * 釋放分布式鎖
     *
     * @param key 鎖的Key
     */
    public void releaseLock(String key) {
        sRedisTemplate.delete(LOCK_PREFIX + key);
    }

    /**
     * 刷新某個鎖頭的過期時間
     *
     * @param expirationTime 若小於當前時間，則鎖會直接被移除
     */
    public void refreshLockExpirationTime(String key, Instant expirationTime) {
        String lockKey = LOCK_PREFIX + key;

        sRedisTemplate.expireAt(lockKey, expirationTime);
    }

    /**
     * 將某個字串作為元素添加進redis set裡面
     */
    public void addToSet(String key, String value) {
        String setKey = SET_PREFIX + key;
        sRedisTemplate.opsForSet().add(setKey, value);
    }

    /**
     * 將某個字串作為元素添加進redis set裡面，並設置整個set的過期時間
     */
    public void addToSetWithExpiration(String key, String value, Instant expiration) {
        sRedisTemplate.executePipelined((RedisCallback<List<Object>>) connection -> {
            String setKey = SET_PREFIX + key;

            connection.sAdd(setKey.getBytes(), value.getBytes());

            connection.expireAt(setKey.getBytes(), expiration.getEpochSecond());

            return null; // executePipelined 會忽略回傳值
        });
    }

    /**
     * 判斷某個字串元素存不存在於redis set之中。
     * @return 存在則回傳true，不存在則回傳false。若set本身都不存在，也會回傳false。
     */
    public boolean isSetMember(String key, String value) {
        String setKey = SET_PREFIX + key;

        return Boolean.TRUE.equals(sRedisTemplate.opsForSet().isMember(setKey, value));
    }

    /**
     * 集體判斷某些字串元素，存不存在於redis set之中。
     * @param values 要集體判斷的字串元素
     * @return 回傳有存在於redis set之中的字串元素
     */
    public Set<String> findPresentInRedisSet(String redisKey, Set<String> values) {
        List<String> valueList = new ArrayList<>(values);

        // 呼叫 Redis 的 sInter 命令來獲取交集
        return sRedisTemplate.opsForSet().intersect(redisKey, valueList);
    }

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
     * 將某字串快取進Redis，該字串不一定要是json，並且設置超時。
     */
    public void putDataStrByKeyAndSetExpiration(String key, String cacheStr, Instant expiration) {
        sRedisTemplate.opsForValue().set(key, cacheStr, Duration.between(Instant.now(), expiration));
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
            throw new MyRedisException(String.format("The value in redis is not json format cause JsonProcessingException, value: %s", jsonStr), e);
        }

    }

    /**
     * 根據 key 獲取 Redis 中儲存的 JSON 陣列，並轉換為 List<T>
     *
     * @param key Redis key
     * @param clazz 欲轉換的物件類型
     * @return 若 key 不存在則回傳 Optional.empty()，否則回傳對應的 List<T>
     */
    public <T> Optional<List<T>> getDataListByKey(String key, Class<T> clazz) {
        Optional<String> optionalJsonStr = this.getDataStrByKey(key);

        if (!optionalJsonStr.isPresent()) {
            return Optional.empty();
        }

        String jsonStr = optionalJsonStr.get();
        try {
            List<T> list = mapper.readValue(
                    jsonStr,
                    mapper.getTypeFactory().constructCollectionType(List.class, clazz)
            );

            return Optional.of(list);
        } catch (Exception e) {
            throw new MyRedisException(String.format("The value in redis is not a valid JSON array format, value: %s", jsonStr), e);
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
        } catch (JsonProcessingException e){
            throw new MyRedisException("Some error occurred when converting POJO into jsonStr cause JsonProcessingException.", e);
        }
    }

    /**
     * 將一個POJO以Json型式存入redis，並且設置超時。
     */
    public void putDataObjByKeyAndSetExpiration(String key, Object cacheObj, Instant expiration) {
        try {
            String jsonStr = mapper.writeValueAsString(cacheObj);
            this.putDataStrByKeyAndSetExpiration(key, jsonStr, expiration);
        } catch (JsonProcessingException e){
            throw new MyRedisException("Some error occurred when converting POJO into jsonStr cause JsonProcessingException.", e);
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
     * 將List內的資料依序插入redis-list，每一筆資料都是插入在最後一筆之後(所以陣列順序不會變)
     *
     * @param cacheStrList 一個String的List，可以不是Json字串
     */
    public void rightPushStrListByKey(String key, List<String> cacheStrList) {
        if (cacheStrList.isEmpty()) {
            return;
        }

        sRedisTemplate.opsForList().rightPushAll(key, cacheStrList); // 不接受空陣列
    }

    /**
     * 將List內的資料依序插入redis-list，每一筆資料都是插入在最後一筆之後(所以陣列順序不會變)，並設置超時時間。
     *
     * @param cacheStrList 一個String的List，可以不是Json字串
     * @param expiration   key的有效時間
     */
    public void rightPushStrListByKeyAndSetExpiration(String key, List<String> cacheStrList, Instant expiration) {
        if (cacheStrList.isEmpty()) {
            return;
        }

        sRedisTemplate.executePipelined((RedisCallback<Void>) connection -> {
            byte[] rawKey = key.getBytes(StandardCharsets.UTF_8);

            for (String value : cacheStrList) {
                connection.rPush(rawKey, value.getBytes(StandardCharsets.UTF_8));
            }

            connection.expireAt(rawKey, expiration.getEpochSecond());
            return null;
        });
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
        } catch (JsonProcessingException e){
            throw new MyRedisException(String.format("The value in redis is not json format cause JsonProcessingException, value: %s", jsonStr), e);
        }
    }

    /**
     * 將List內的資料依序插入redis-list，每一筆資料都是插入在最後一筆之後(所以陣列順序不會變)
     *
     * @param cacheList  一個POJO的List，可以轉成Json。
     */
    public void rightPushObjListByKey(String key, List<?> cacheList) {
        if (cacheList.isEmpty()) {
            return;
        }

        List<String> jsonStrList = new ArrayList<>();

        for (Object obj : cacheList) {
            try {
                jsonStrList.add(mapper.writeValueAsString(obj));
            } catch (JsonProcessingException e) {
                throw new MyRedisException("Failed to serialize object to JSON: " + obj, e);
            }
        }

        this.rightPushStrListByKey(key, jsonStrList);
    }

    /**
     * 將List內的資料依序插入redis-list，每一筆資料都是插入在最後一筆之後(所以陣列順序不會變)，並且設置超時。
     *
     * @param cacheList  一個POJO的List，可以轉成Json
     * @param expiration 有效時間
     */
    public void rightPushObjListByKeyAndSetExpiration(String key, List cacheList, Instant expiration) {
        if (cacheList == null) {
            throw new MyRedisException("cacheList cannot be null.");
        }

        ArrayNode jsonArr = mapper.valueToTree(cacheList);
        List<String> jsonStrList = new ArrayList<>();

        jsonArr.forEach(json -> {
            jsonStrList.add(json.toString());
        });

        this.rightPushStrListByKeyAndSetExpiration(key, jsonStrList, expiration);
    }

    /**
     * 將List內的資料依序插入redis-list，每一筆資料都是插在第一筆之前(所以順序會反過來)，並為整個redis-list設置過期時間
     *
     * @param cacheStrList 一個String的List，可以不是Json字串
     * @param expiration   key的有效時間
     */
    public void leftPushStrListByKeyAndSetExpiration(String key, List<String> cacheStrList, Instant expiration) {
        if (cacheStrList.isEmpty()) {
            return;
        }

        sRedisTemplate.executePipelined((RedisCallback<Void>) connection -> {
            byte[] rawKey = key.getBytes(StandardCharsets.UTF_8);

            for (int i = 0; i < cacheStrList.size(); i++) {
                connection.lPush(rawKey, cacheStrList.get(i).getBytes(StandardCharsets.UTF_8));
            }

            connection.expireAt(rawKey, expiration.getEpochSecond());
            return null;
        });
    }

    /**
     * 將 List 內的資料依序插入 redis-list，每一筆資料都是插在第一筆之前（所以陣列順序會相反）。
     *
     * @param cacheList 一個 POJO 的 List，可以轉成 JSON
     * @throws MyRedisException 如果cacheList=null，則拋出
     */
    public void leftPushObjListByKey(String key, List<?> cacheList) {
        if (cacheList == null) {
            throw new MyRedisException("cacheList cannot be null.");
        }

        if (cacheList.isEmpty()) {
            return;
        }

        List<String> jsonStrList = new ArrayList<>();
        for (Object obj : cacheList) {
            try {
                jsonStrList.add(mapper.writeValueAsString(obj));
            } catch (JsonProcessingException e) {
                throw new MyRedisException("Failed to serialize object to JSON: " + obj, e);
            }
        }

        this.leftPushStrListByKey(key, jsonStrList);
    }

    /**
     * 將 List 內的資料依序插入 redis-list，每一筆資料都是插在第一筆之前（所以陣列順序會相反），並且設置超時。
     *
     * @param cacheList  一個 POJO 的 List，可以轉成 JSON。
     * @param expiration 有效時間
     * @throws MyRedisException 如果cacheList=null，則拋出
     */
    public void leftPushObjListByKeyAndSetExpiration(String key, List<?> cacheList, Instant expiration) {
        if (cacheList == null) {
            throw new MyRedisException("cacheList cannot be null.");
        }

        if (cacheList.isEmpty()) {
            return;
        }

        List<String> jsonStrList = new ArrayList<>();
        for (Object obj : cacheList) {
            try {
                jsonStrList.add(mapper.writeValueAsString(obj));
            } catch (JsonProcessingException e) {
                throw new MyRedisException("Failed to serialize object to JSON: " + obj, e);
            }
        }

        this.leftPushStrListByKeyAndSetExpiration(key, jsonStrList, expiration);
    }

    /**
     * 將List內的資料依序插入redis-list，每一筆資料都是插在第一筆之前(所以順序會反過來)
     *
     * @param cacheStrList 一個String的List，可以不是Json字串
     */
    public void leftPushStrListByKey(String key, List<String> cacheStrList) {
        if (cacheStrList.isEmpty()) {
            return;
        }

        sRedisTemplate.opsForList().leftPushAll(key, cacheStrList); // 不接受空陣列
    }

    /**
     * 查詢redis-list所有元素，以字串呈现，每一條元素都可以不用是JSON
     */
    public List<String> getAllElementsStrFromListByKey(String key) {
        return sRedisTemplate.opsForList().range(key, 0, -1);
    }

    /**
     * 查詢 redis-list 所有元素，以物件呈現。每一條元素都必須是 JSON。
     */
    public <T> List<T> getAllElementsObjFromListByKey(String key, Class<T> clazz) {
        List<String> jsonList = sRedisTemplate.opsForList().range(key, 0, -1);

        if (jsonList == null || jsonList.isEmpty()) {
            return Collections.emptyList();
        }

        try {
            return jsonList.stream()
                    .map(json -> {
                        try {
                            return mapper.readValue(json, clazz);
                        } catch (JsonProcessingException e) {
                            throw new RuntimeException("Failed to parse JSON: " + json, e);
                        }
                    })
                    .collect(Collectors.toList());
        } catch (RuntimeException e) {
            throw new MyRedisException(String.format("The value in Redis is not JSON format, key: %s, values: %s", key, jsonList), e);
        }
    }

    /**
     * 取出Queue-A的最尾端元素，將其放入QueueB的最前端，並回傳該元素(可以不是JSON)
     * @param sourceKey         QueueA 的 Redis Key
     * @param destinationKey    QueueB 的 Redis Key
     */
    public Optional<String> rightPopFromSourceAndLeftPushToDestinationAndGetDataStr(String sourceKey, String destinationKey) {
        String strValue = sRedisTemplate.opsForList().rightPopAndLeftPush(sourceKey, destinationKey);
        return Optional.ofNullable(strValue);
    }

    /**
     * 取出Queue-A的最尾端元素，將其放入QueueB的最前端，並嘗試反序列化為指定物件(必須是JSON)。
     *
     * @param sourceKey         QueueA 的 Redis Key
     * @param destinationKey    QueueB 的 Redis Key
     * @param clazz             目标对象类型的 Class
     * @throws MyRedisException 當取出的元素反序列化成物件時失敗，則拋出
     */
    public <T> Optional<T> rightPopFromSourceAndLeftPushToDestinationAndGetDataObj(String sourceKey, String destinationKey, Class<T> clazz) {
        Optional<String> dataStrOpt = this.rightPopFromSourceAndLeftPushToDestinationAndGetDataStr(sourceKey, destinationKey);

        if (!dataStrOpt.isPresent()) {
            return Optional.empty();
        }

        String jsonStr = dataStrOpt.get();
        try {
            T obj = mapper.readValue(jsonStr, clazz);
            return Optional.ofNullable(obj);
        } catch (Exception e) {
            throw new MyRedisException(String.format("The value in Redis is not valid JSON. value: %s", jsonStr), e);
        }
    }

    /**
     * 獲取某個redis-list的長度
     * @return redis-list的長度，如果該Key不存在，或者key的類型不是 list，則回傳0
     */
    public long getListSize(String key) {
        return Optional.ofNullable(sRedisTemplate.opsForList().size(key)).orElse(0L);
    }

    /**
     * 取出Queue的最尾端元素(可以不是json)
     */
    public Optional<String> rightPopAndGetDataStr(String key) {
        String strValue = sRedisTemplate.opsForList().rightPop(key);
        return Optional.ofNullable(strValue);
    }

    /**
     * 取出 Queue 的最尾端元素(需為Json)，並嘗試反序列為指定物件
     * @throws MyRedisException 當取出的元素反序列化成物件時失敗，則拋出
     */
    public <T> Optional<T> rightPopAndGetDataObj(String key, Class<T> clazz) {
        Optional<String> dataStrOpt = this.rightPopAndGetDataStr(key);

        if (!dataStrOpt.isPresent()) {
            return Optional.empty();
        }

        String jsonStr = dataStrOpt.get();
        try {
            T obj = mapper.readValue(jsonStr, clazz);
            return Optional.ofNullable(obj);
        } catch (Exception e) {
            throw new MyRedisException(String.format("The value in Redis is not a valid JSON for class %s, value: %s", clazz.getName(), jsonStr), e);
        }
    }

    /**
     * 刪除某個Key。
     */
    public void removeKey(String key) {
        sRedisTemplate.delete(key);
    }

    /**
     * 移除redis上所有的key。
     */
    @SuppressWarnings("unchecked") //此方法執行maven install的時候會跳型別檢查的警告，不知為何，總之目前先用這個叫他別該
    public void removeAllKeys() {
        Set<String> allKeys = sRedisTemplate.keys("*");
        sRedisTemplate.delete(allKeys);
    }

    /**
     * 檢查某個key是否存在
     */
    public boolean doesKeyExist(String key) {
        Boolean exists = sRedisTemplate.hasKey(key);
        return Boolean.TRUE.equals(exists);
    }

}
