package jeff.core.consts;

import jeff.persistent.model.mysql.po.Members;

/**
 * 系統目前沒有寫登入認證相關的邏輯，所以User部分先寫死。
 */
public final class DemoMember {

    private DemoMember() {
    }

    /**
     * 顧客身分的會員，只能夠消費商品，不能發布案件。
     */
    public static final Members CUSTOMER = new Members()
            .setId(1)
            .setName("Jeff")
            .setRole(1)
            .setStatus(1);

    /**
     * 企業主身分的會員，只能夠發布案件，不能消費商品。
     */
    public static final Members SELLER = new Members()
            .setId(2)
            .setName("Amanda Company")
            .setRole(2)
            .setStatus(1);

}
