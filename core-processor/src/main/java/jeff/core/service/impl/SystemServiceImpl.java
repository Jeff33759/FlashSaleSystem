package jeff.core.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import jeff.common.consts.ResponseCode;
import jeff.common.entity.dto.send.ResponseObject;
import jeff.core.service.SystemService;
import jeff.persistent.service.InitService;
import jeff.redis.util.MyRedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class SystemServiceImpl implements SystemService {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MyRedisUtil myRedisUtil;

    @Autowired
    private InitService dbInitService;

    @Override
    public ResponseObject initAllDBAndRedis() {
        dbInitService.initAllDemoDataOfMySQL(); //初始化在MySQL裡的Demo資料
        dbInitService.initFlashSaleEventLogDocumentOfMongoDB(); //初始化在Mongo裡的快閃銷售表
        myRedisUtil.removeAllKeys(); //清除redis上面所有的key
        return new ResponseObject(ResponseCode.Successful.getCode(), objectMapper.createObjectNode(), "Initialize Mysql, Mongo and Redis successfully.");
    }

}