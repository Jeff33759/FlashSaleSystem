package jeff.core.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import jeff.common.consts.ResponseCode;
import jeff.common.entity.dto.inner.InnerCommunicationDto;
import jeff.persistent.service.InitService;
import jeff.redis.util.MyRedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 一些跟系統設置有關的業務邏輯。
 */
@Service
public class SystemService {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MyRedisUtil myRedisUtil;

    @Autowired
    private InitService dbInitService;

    /**
     * 初始化所有的DB(RDBMS和NoSQL)與Redis
     */
    public InnerCommunicationDto initAllDBAndRedis() {
        dbInitService.initAllDemoDataOfMySQL(); //初始化在MySQL裡的Demo資料
        dbInitService.initFlashSaleEventLogDocumentOfMongoDB(); //初始化在Mongo裡的快閃銷售表
        myRedisUtil.removeAllKeys(); //清除redis上面所有的key
        return new InnerCommunicationDto(ResponseCode.Success.getCode(), objectMapper.createObjectNode(), "Initialize Mysql, Mongo and Redis successfully.");
    }

}
