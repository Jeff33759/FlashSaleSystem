package jeff.redis.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * 自訂義的RedisService介面實作，
 * 使用SpringDataRedis作為訪問Redis的API，其底層實作預設為Lettuce。
 *
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
     * 用Key去redis拉資料(資料假定都是Json)，將Json轉成POJO後回傳。
     *
     * @param clazz 欲轉成的POJO
     * @return 若該key存在於redis，則回傳有值的Optional(包裹POJO)；若不存在於redis，則回傳空Optional
     */
    public Object getJsonDataObjByKey(String key, Class clazz) throws JsonProcessingException {
        String jsonStr = sRedisTemplate.opsForValue().get(key); // 若key不存在於redis，則為null

        if (jsonStr == null) {
            return Optional.empty();
        }

        return Optional.of(mapper.readValue(jsonStr, clazz));
    }

    /**
     * 將一個POJO以Json型式存入redis。
     *
     * @param key
     * @param cacheObj
     * @throws JsonProcessingException
     */
    public void setJsonDataObjByKey(String key, Object cacheObj) throws JsonProcessingException {
        String jsonStr = mapper.writeValueAsString(cacheObj);
        sRedisTemplate.opsForValue().set(key, jsonStr);
    }

    /**
     * 得到redis-list第一筆資料，同時移除該元素，並且將得到的資料轉成POJO。
     *
     * @param clazz 欲轉成的POJO
     * @return 若該key存在於redis，則回傳有值的Optional(包裹POJO)；若key不存在於redis或者陣列為空(等同於key不存在)，則回傳空Optional
     */
    public Object leftPopListByKeyAndGetDataStr(String key, Class clazz) throws JsonProcessingException {
        String jsonStr = sRedisTemplate.opsForList().leftPop(key); //若陣列為空或該key不存在，則為null

        if (jsonStr == null) {
            return Optional.empty();
        }

        return Optional.of(mapper.readValue(jsonStr, clazz));
    }

    /**
     * 將List內的資料依序插入redis-list，每一筆資料都是插入在最後一筆之後。
     */
    public void rightPushListByKey(String key, List cacheList, Instant expiration) {
        ArrayNode jsonArr = (ArrayNode) mapper.valueToTree(cacheList);
        List<String> jsonStrList = new ArrayList<>();

        jsonArr.forEach(json -> {
            jsonStrList.add(json.toString());
        });

        sRedisTemplate.opsForList().rightPushAll(key, jsonStrList);
        sRedisTemplate.expireAt(key, expiration); //同fse的到期時間，若fse有延長到期時間的api，記得redis也要延長
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