package jeff.schedule.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;

/**
 * 若此應用為開發環境，就載入dev相關的配置檔
 * */
@Configuration
@Profile("dev")
@PropertySources({
        @PropertySource({"classpath:myDataSource-dev.properties"})})
public class DevEnvConfig {

}
