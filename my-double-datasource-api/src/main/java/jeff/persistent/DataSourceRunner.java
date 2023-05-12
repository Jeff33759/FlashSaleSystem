package jeff.persistent;

import jeff.persistent.config.MySQLSourceConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.List;

/**
 * Bean初始化後，Spring應用執行前，動態更改dataSource的uri(原本為了做建置DB的檢查所以沒有加上DB名等等的後綴)。
 * 並且藉由一些判斷決定要不要建置DEMO資料。
 * Schema的部分，已經在prop檔裡面藉由spring.sql.init.schema-locations執行了，這裡主要是在建置DEMO資料。
 */
@Slf4j
@Component
public class DataSourceRunner implements CommandLineRunner {


    /**
     * 這裡的DataSource，會在{@link MySQLSourceConfig}便實例化完成，經測會有好幾個實例被註冊進Spring容器。
     */
    @Autowired
    private DataSource dataSource;

    @Value("${mydb.script.patharr}")
    private List<String> sqlScriptPaths;

    @Value("${spring.profiles.active}")
    private String env;


    @Override
    public void run(String... args) throws Exception {
        if ("dev".equals(env)) {
            setupTheDevEnv();
        } else {

        }
    }

    /**
     * 若環境為dev環境，所要進行的一些預建置動作。
     */
    private void setupTheDevEnv() {
//      遍歷所有待執行的SQLScript，執行
        sqlScriptPaths.forEach(sp -> {
            try {
                executeSqlScript(sp);
                log.info("DEMO資料新增成功!!");
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        });

    }

    private void executeSqlScript(String scriptPath) throws SQLException {
        Resource resource = new ClassPathResource(scriptPath);

//      執行SQL腳本檔最好用ScriptUtils，一般的jdbcTemplate若腳本內包含多個Insert語句會報語法錯誤
        ScriptUtils.executeSqlScript(dataSource.getConnection(), resource);
    }


}
