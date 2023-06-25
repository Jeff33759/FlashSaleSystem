package jeff.common.consts;

/**
 * 自訂義的Log類型
 */
public enum MyLogType {

    NOTIFY("Notify"), //系統通知

    BUSINESS("Business"); //業務邏輯

    private String typeName;

    MyLogType(String typeName) {
        this.typeName = typeName;
    }

    public String getTypeName() {
        return typeName;
    }

}
