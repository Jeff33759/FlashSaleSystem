package jeff.persistent;

import jeff.persistent.model.mysql.dao.MemberDAO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
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
 * */
@Slf4j
@Component
public class DataSourceRunner implements CommandLineRunner {

    /**
     * 設置DataSource的一些屬性用
     * */
    @Autowired
    private DataSourceProperties dataSourceProp;

    /**
     * 為了獲得Conn物件
     */
    @Autowired
    private DataSource dataSource;

    /**
     * 若會員表裡已經有資料，代表DEMO資料已經被建置過，就不要再建置。
     */
    @Autowired
    private MemberDAO mDao;

    @Value("${spring.datasource.url}")
    private String uri;

    @Value("${mydb.uri.suffix}")
    private String uriSuffix;

    @Value("${mydb.script.patharr}")
    private List<String> sqlScriptPaths;

    @Value("${spring.profiles.active}")
    private String env;


    @Override
    public void run(String... args) throws Exception {
//      重新設置datasource的uri
        dataSourceProp.setUrl(uri + uriSuffix);
        if("dev".equals(env)){
            setupTheDevEnv();
        }else{

        }
    }

    /**
     * 若環境為dev環境，所要進行的一些預建置動作。
     */
    private void setupTheDevEnv(){
//      若會員表沒資料，代表系統還沒有建置DEMO資料
        if(mDao.count() == 0){
//          遍歷所有待執行的SQLScript，執行
            sqlScriptPaths.forEach(sp -> {
                        try {
                            executeSqlScript(sp);
                            log.info("DEMO資料新增成功!!");
                        } catch (SQLException throwables) {
                            throwables.printStackTrace();
                        }
                    }
            );
        }
    }

    private void executeSqlScript(String scriptPath) throws SQLException {
        Resource resource = new ClassPathResource(scriptPath);
//      執行SQL腳本檔最好用ScriptUtils，一般的jdbcTemplate若腳本內包含多個Insert語句會報語法錯誤
        ScriptUtils.executeSqlScript(dataSource.getConnection(),resource);
    }


}
