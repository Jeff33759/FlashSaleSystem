package jeff.common.consts;

/**
 * 自訂義的Log類型
 */
public enum MyLogType {

    SYSTEM("SYS"), //系統通知

    BUSINESS("BUS"), //業務邏輯

    MQ("MQ"); // MQ相關

    private String typeName;

    MyLogType(String typeName) {
        this.typeName = typeName;
    }

    public String getTypeName() {
        return typeName;
    }

}
