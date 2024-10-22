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

    public String getType() {
        return type;
    }

    public int getScopeLevel() {
        return scopeLevel;
    }
}