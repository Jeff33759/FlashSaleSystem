package jeff.redis;

import jeff.redis.util.MyRedisUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * 在各應用啟動後，根據環境去決定是否要做一些例如初始化redis資料的行為。
 */
@Slf4j
@Component
public class RedisRunner implements CommandLineRunner {

    @Autowired
    private MyRedisUtil myRedisUtil;

    @Value("${app.type}")
    private String appType;

    @Value("${spring.profiles.active}")
    private String env;

    @Override
    public void run(String... args) {
        if ("dev".equals(env)) {
            setupTheDevEnv();
        } else {

        }
    }

    /**
     * 若環境為dev環境，所要進行的一些預建置動作。
     */
    private void setupTheDevEnv() {
        if ("core-processor".equals(appType)) { //只有core-processor在Dev環境時，啟動要做redis資料初始化。
            initRedisData();
        }
    }

    /**
     * 初始化redis資料。
     */
    private void initRedisData() {
        myRedisUtil.removeAllKeys();
        log.info("All redis data has been cleaned.");
    }

}
