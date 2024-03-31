package jeff.persistent;

import jeff.persistent.service.InitService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * 在各應用啟動後，根據環境去決定是否要做一些例如初始化DB資料的行為。
 */
@Component
public class DataSourceRunner implements CommandLineRunner {

    @Autowired
    private InitService initService;

    @Value("${spring.profiles.active}")
    private String env;

    @Value("${app.type}")
    private String appType;

    @Override
    public void run(String... args) {
//      根據環境選擇要執行什麼邏輯
        if ("dev".equals(env)) {
            setupTheDevEnv();
        } else {

        }
    }

    /**
     * 若環境為dev環境，所要進行的一些預建置動作。
     */
    private void setupTheDevEnv() {
        if ("core-processor".equals(appType)) { //只有core-processor在Dev環境時，啟動要做Demo資料初始化。
            initService.initAllDemoDataOfMySQL();
            initService.initFlashSaleEventLogDocumentOfMongoDB();
        }
    }

}
