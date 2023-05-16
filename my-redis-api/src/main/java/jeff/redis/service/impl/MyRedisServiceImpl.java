package jeff.redis.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jeff.redis.exception.MyRedisNotFoundException;
import jeff.redis.service.MyRedisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.Optional;

/**
 * 自訂義的RedisService介面實作，
 * 使用SpringDataRedis作為訪問Redis的API，其底層實作預設為Lettuce。
 * */
public class MyRedisServiceImpl implements MyRedisService {

    @Autowired
    private StringRedisTemplate sRedisTemplate;

    @Autowired
    private ObjectMapper mapper;

    @Override
    public String getDataStrByKey(String key) throws MyRedisNotFoundException {
        String jsonStr = sRedisTemplate.opsForValue().get(key);
        return Optional.ofNullable(jsonStr)
                .orElseThrow(()->
                        new MyRedisNotFoundException("This key is not exist in the redis."));
    }

    @Override
    public void setDataStrByKey(String key, String cacheJsonStr) {
        sRedisTemplate.opsForValue().set(key, cacheJsonStr);
    }

    @Override
    public Object getDataObjByKey(String key) throws MyRedisNotFoundException, JsonProcessingException {
        String jsonStr = this.getDataStrByKey(key); // 若有值，則確定不為null
        return mapper.readValue(jsonStr, Object.class);
    }

    @Override
    public void setDataObjByKey(String key, Object cacheObj) throws JsonProcessingException {
        String jsonStr = mapper.writeValueAsString(cacheObj);
        this.setDataStrByKey(key, jsonStr);
    }

}