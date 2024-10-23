class SymbolInfo {
    private String type;
    private int scopeLevel;
    private String value;
    private String name;
    private int id;
    private char dataType;

    public SymbolInfo(String type, int scopeLevel) {
        this.type = type;
        this.scopeLevel = scopeLevel;
    }

    public SymbolInfo( String type, int scopeLevel, String name, int id) {
        this.type = type;
        this.scopeLevel = scopeLevel;
        this.name = name;
        this.id = id; }

    public String getType() {
        return type;
    }

    public String toString() {
        return "SymbolInfo{" +
                "type='" + type + '\'' +
                ", scopeLevel=" + scopeLevel +
                ", value='" + value + '\'' +
                ", name='" + name + '\'' +
                ", id=" + id +
                ", dataType=" + dataType +
                '}';
    }

    public int getScopeLevel() {
        return scopeLevel;
    }
}