package jeff.redis.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import jeff.redis.exception.MyRedisNotFoundException;

/**
 * 自己做的Redis服務器介面，底層可以視情況抽換成其他實作。
 * */
public interface MyRedisService {

    /**
     * 輸入key，從Redis快取中拉取對應的資料。
     * 若該ID不存在於Redis，將會回傳Null，所以若外部呼叫者沒有做null check，可能造成NullPointException。
     * 這邊不想要出現那種情況，因為NullPointException在維護者看來是意義很不明確的例外，
     * 所以這裡設計成回傳Optional，
     * 強制外部呼叫者一定要Try catch。
     *
     * @return Json字串
     * */
    String getDataByKey(String key) throws MyRedisNotFoundException;

    /**
     * 將json字串快取進Redis。
     *
     * */
    void setDataStrByKey(String key, String cacheJsonStr);

    /**
     * 用Key去redis拉資料，並轉成POJO回傳。
     * */
    Object getDataObjByKey(String key) throws MyRedisNotFoundException, JsonProcessingException;

    /**
     * 將某物件轉成json字串後快取進redis。
     * */
    void setDataObjByKey(String key, Object cacheObj) throws JsonProcessingException;

}