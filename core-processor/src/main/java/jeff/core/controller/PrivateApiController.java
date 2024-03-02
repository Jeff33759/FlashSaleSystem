package jeff.core.controller;

import jeff.common.entity.dto.send.ResponseObject;
import jeff.core.service.SystemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 存放系統內部溝通用Api的控制器，這裡的接口僅提供給其他系統內部伺服端訪問，也許之後可以考慮加個認證機制。
 */
@RestController
@RequestMapping(produces = org.springframework.http.MediaType.APPLICATION_JSON_VALUE)
public class PrivateApiController {

    @Autowired
    private SystemService systemService;


    /**
     * 初始化redis和MySql，方便DEMO。
     */
    @GetMapping("/system/init")
    public ResponseEntity<ResponseObject> initRedisAndMySql() {
        return ResponseEntity.ok(systemService.initAllDBAndRedis());
    }

}
