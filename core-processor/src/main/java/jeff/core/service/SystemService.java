package jeff.core.service;

import jeff.common.entity.dto.send.ResponseObject;

/**
 * 一些跟系統設置有關的業務邏輯。
 */
public interface SystemService {

    /**
     * 初始化所有的DB(RDBMS和NoSQL)與Redis
     */
    ResponseObject initAllDBAndRedis();

}
