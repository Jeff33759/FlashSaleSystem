package jeff.test.pojo;

/**
 * 專門用於測試的POJO。
 */
public class MyTestPOJO {

    private int id;

    private String name;

    public MyTestPOJO(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}
