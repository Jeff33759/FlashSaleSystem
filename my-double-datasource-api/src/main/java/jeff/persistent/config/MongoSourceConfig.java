package jeff.persistent.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@Configuration
@EnableMongoRepositories(basePackages = "jeff.persistent.model.mongo.dao") //告訴dataMongo屬於他的DAO去哪找
public class MongoSourceConfig {

}