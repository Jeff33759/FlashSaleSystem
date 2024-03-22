package jeff.core.controller;

import jeff.common.entity.dto.inner.InnerCommunicationDto;
import jeff.core.service.SystemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 存放內部人員用Api的控制器，這裡的接口路徑僅提供給其他內部系統伺服端訪問又或者開發人員自己CURL訪問，用於調整一些系統參數，不開放給外部訪問。
 * 也許之後可以考慮加個認證機制。
 */
@RestController
@RequestMapping(path = "/inner", produces = org.springframework.http.MediaType.APPLICATION_JSON_VALUE)
public class PrivateApiController {

    @Autowired
    private SystemService systemService;

    /**
     * 初始化redis和MySql，方便DEMO。
     */
    @GetMapping("/system/init")
    public ResponseEntity<InnerCommunicationDto> initRedisAndMySql() {
        return ResponseEntity.ok(systemService.initAllDBAndRedis());
    }

}
