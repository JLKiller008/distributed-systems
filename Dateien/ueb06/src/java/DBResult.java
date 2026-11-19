import java.io.Serializable;

public class DBResult implements Serializable {
    private static final long serialVersionUID = 1L;

    private int key;
    private String value;

    public DBResult() {
    }

    public DBResult(int key, String value) {
        this.key = key;
        this.value = value;
    }

    public int getKey() {
        return key;
    }

    public void setKey(int key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return "DBResult{key=" + key + ", value='" + value + "'}";
    }
}
