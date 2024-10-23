import java.util.ArrayList;

public class SymbolInfo {
    private String type;
    private int scopeLevel;
    private int parentScopeLevel;
    private ArrayList<String> value;
    private String name;
    private int id;
    private String dataType;

    public SymbolInfo(String type, int scopeLevel, int id) {
        this.type = type;
        this.scopeLevel = scopeLevel;
        this.id = id;
        this.value = new ArrayList<String>();
    }

    public void setValue(String value) {
        this.value.add(value);
    }

    public SymbolInfo( String type, int scopeLevel, String name, int id, String dataType) {
        this.type = type;
        this.scopeLevel = scopeLevel;
        this.name = name;
        this.id = id;
        this.dataType = dataType;
        this.value = new ArrayList<String>();
    }

    public String getType() {
        return type;
    }

    public String toString() {
        return "SymbolInfo{" +
                "type='" + type + '\'' +
                ", scopeLevel=" + scopeLevel +
                ", value='" + value.toString() + '\'' +
                ", name='" + name + '\'' +
                ", id=" + id +
                ", dataType=" + dataType +
                '}';
    }

    public int getId() {
        return id;
    }

    public int getScopeLevel() {
        return scopeLevel;
    }

    public String getName() {
        return name;
    }


    public void updateDataType(String dataType) {
        this.dataType = dataType;
    }

    public String getDataType() {
        return dataType;
    }
}