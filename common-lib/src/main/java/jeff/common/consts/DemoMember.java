package jeff.common.consts;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * TODO 系統目前沒有寫登入認證相關的邏輯，所以User部分先寫死。
 */
public final class DemoMember {

    private DemoMember() {
    }

    /**
     * 顧客身分的會員，只能夠消費商品，不能發布案件。
     */
    public static final StubMember CUSTOMER = new StubMember()
            .setId(1)
            .setName("Jeff")
            .setRole(1)
            .setStatus(1);

    /**
     * 企業主身分的會員，只能夠發布案件，不能消費商品。
     */
    public static final StubMember SELLER = new StubMember()
            .setId(2)
            .setName("Amanda Company")
            .setRole(2)
            .setStatus(1);

    @Data
    @Accessors(chain = true) //lombok支援建構子鏈式賦值
    public static class StubMember {

        private Integer id;

        private String name;

        /**
         * 1:買家(只能下單)，2:賣家(只能發單)
         * */
        private Integer role;

        /**
         * 1:啟用狀態，2:黑名單狀態，3:軟刪除狀態(凍結)
         * */
        private Integer status;
    }

}
