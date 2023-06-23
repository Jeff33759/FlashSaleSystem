package jeff.persistent.service;

/**
 * 一些初始化相關的方法。
 */
public interface InitService {

    /**
     * 初始化MySQL裡的DEMO資料。
     */
    void initAllDemoDataOfMySQL();

    /**
     * 初始化MongoDB裡面的FlashSaleTempRecord表
     */
    void initFlashSaleTempRecordDocumentOfMongoDB();

}
