package jeff.highconcurrency.persistent.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.repository.config.EnableReactiveMongoRepositories;

@Configuration
@EnableReactiveMongoRepositories(basePackages = "jeff.highconcurrency.persistent.model.mongo.dao") //告訴reactiveDataMongo屬於他的DAO去哪找
public class ReactiveMongoSourceConfig {

}
