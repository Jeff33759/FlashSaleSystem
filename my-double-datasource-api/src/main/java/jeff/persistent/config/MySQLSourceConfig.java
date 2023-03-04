package jeff.persistent.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EnableJpaRepositories(basePackages = {"jeff.persistent.model.mysql.dao"}) //告訴JPA屬於他的DAO去哪找
public class MySQLSourceConfig {


}
