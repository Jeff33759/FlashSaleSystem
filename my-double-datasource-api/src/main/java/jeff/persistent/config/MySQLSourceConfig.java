package jeff.persistent.config;

import com.mysql.cj.jdbc.MysqlDataSource;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.datasource.init.ScriptUtils;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;
import java.sql.SQLException;

/**
 * 建置DB的邏輯，一定要先於JPA相關元件的初始化。
 * 反正就是一定要確保DB已經建完了且存在了，然後才對dataSource進行一些初始化實例化，這樣dataSource的URL才能夠指向DB嘛，否則不存在怎麼指。
 */
@Configuration
@EnableJpaRepositories(basePackages = {"jeff.persistent.model.mysql.dao"}) //告訴JPA屬於他的DAO去哪找
public class MySQLSourceConfig {

    /**
     * 程式啟動時，自動執行SQL腳本，建置DB
     */
    public MySQLSourceConfig() throws SQLException {
        Resource resource = new ClassPathResource("db/schema-dev.sql");

        MysqlDataSource a = new MysqlDataSource();
        a.setUrl("jdbc:mysql://localhost:3306");
        a.setUser("root");
        a.setPassword("sasa");
        ScriptUtils.executeSqlScript(a.getConnection(), resource);
    }


    /**
     * 上面把建置DB的邏輯寫在建構子裡了，所以跑到這個方法時，DB一定存在，因此setUrl才不會跳錯。
     * 這裡的DataSource註冊為原型模式，預設會有8個實例在Spring容器內供取用。
     */
    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public DataSource customDataSource() {
        DataSourceBuilder dsBuilder = DataSourceBuilder.create();
        dsBuilder.driverClassName("com.mysql.cj.jdbc.Driver");
        dsBuilder.url("jdbc:mysql://localhost:3306/db_dev_flash_sale_module?useUnicode=true&characterEncoding=utf-8&allowPublicKeyRetrieval=true&useSSL=false");
        dsBuilder.username("root");
        dsBuilder.password("sasa");
        return dsBuilder.build();
    }

}
