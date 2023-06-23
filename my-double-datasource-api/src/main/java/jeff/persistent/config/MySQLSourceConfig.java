package jeff.persistent.config;

import com.mysql.cj.jdbc.MysqlDataSource;
import org.springframework.beans.factory.annotation.Value;
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
import java.util.List;

/**
 * 建置DB的邏輯，一定要先於JPA相關元件的初始化。
 * 反正就是一定要確保DB已經建完了且存在了，然後才對dataSource進行一些初始化實例化，這樣dataSource的URL才能夠指向DB嘛，否則不存在怎麼指。
 */
@Configuration
@EnableJpaRepositories(basePackages = {"jeff.persistent.model.mysql.dao"}) //告訴JPA屬於他的DAO去哪找
public class MySQLSourceConfig {

    @Value("${mydb.datasource.url.prefix}")
    private String dataSourceUrlPrefix;

    @Value("${mydb.datasource.url.suffix}")
    private String dataSourceUrlSuffix;

    @Value("${mydb.datasource.username}")
    private String userName;

    @Value("${mydb.datasource.password}")
    private String passWord;

    /**
     * 自動執行SQL腳本，建置DB。
     *
     * PostConstruct的執行順序為:
     * Constructor(建構子) -> @Autowired(依賴注入) -> @PostConstruct
     *
     * 把建構DB的邏輯寫在此方法與寫在建構子的區別，就是這種做法可以認得@Value所注入的值，因為已經是Spring元件。
     */
    @PostConstruct //當DI完成後所要執行的初始化方法，此時呼叫的已經是Spring代理物件，所以成員變數會有值(從prop撈)
    private void createDatabaseAfterThisComponentHasBeenInjectedIntoSpringContainer() throws SQLException {
        Resource resource = new ClassPathResource("db/schema-dev.sql");

        MysqlDataSource mysqlDataSource = new MysqlDataSource();
        mysqlDataSource.setUrl(dataSourceUrlPrefix);
        mysqlDataSource.setUser(userName);
        mysqlDataSource.setPassword(passWord);
        ScriptUtils.executeSqlScript(mysqlDataSource.getConnection(), resource);
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
        dsBuilder.url(dataSourceUrlPrefix + dataSourceUrlSuffix);
        dsBuilder.username(userName);
        dsBuilder.password(passWord);
        return dsBuilder.build();
    }

}
