package jeff.persistent.service.impl;

import jeff.common.util.LogUtil;
import jeff.persistent.config.MySQLSourceConfig;
import jeff.persistent.model.mongo.dao.FlashSaleEventLogRepo;
import jeff.persistent.service.InitService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.List;

@Slf4j
@Service
public class InitServiceImpl implements InitService {

    /**
     * 這裡的DataSource，會在{@link MySQLSourceConfig}便實例化完成，經測會有好幾個實例被註冊進Spring容器當成連線池。
     */
    @Autowired
    private DataSource dataSource;

    @Autowired
    private FlashSaleEventLogRepo flashSaleEventLogRepo;

    @Autowired
    private LogUtil logUtil;

    @Value("${mydb.script.init.demodata.patharr}")
    private List<String> initDemoDataSqlScriptPaths;


    /**
     * 目前用執行SQL檔的方式來實做初始化的動作。
     */
    @Override
    public void initAllDemoDataOfMySQL() {
//      遍歷所有待執行的SQLScript，執行
        initDemoDataSqlScriptPaths.forEach(sp -> {
            try {
                executeSqlScript(sp);
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        });
        logUtil.logInfo(log, logUtil.composeLogPrefixForSystem(), "All MySQL data has been cleaned and insert demo data successful.");
    }

    @Override
    public void initFlashSaleEventLogDocumentOfMongoDB() {
        flashSaleEventLogRepo.deleteAll(); // 相當於truncate操作
        logUtil.logInfo(log, logUtil.composeLogPrefixForSystem(), "All mongo data has been cleaned.");
    }

    private void executeSqlScript(String scriptPath) throws SQLException {
        Resource resource = new ClassPathResource(scriptPath);

//      執行SQL腳本檔最好用ScriptUtils，一般的jdbcTemplate若腳本內包含多個Insert語句會報語法錯誤
        ScriptUtils.executeSqlScript(dataSource.getConnection(), resource);
    }

}
