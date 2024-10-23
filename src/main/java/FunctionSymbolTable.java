import java.util.*;

class FunctionSymbolTable {
    private Map<String, Map<String, SymbolInfo>> table;
    private String currentScopeName;
    private Map<String, SymbolInfo> map;


    public FunctionSymbolTable() {
        map = new HashMap<String, SymbolInfo>();
        table = new HashMap<>();
        enterScope("main"); // Initialize with the global scope
    }

    public void enterScope(String scopeName) {
        if (table.containsKey(scopeName)) {
            throw new RuntimeException("Scope " + scopeName + " already declared");
        }
        table.put(scopeName, new HashMap<>());
        currentScopeName = scopeName;
    }

    public String exitScope(String scopeName) {
        if (table.containsKey(scopeName)) {
            table.remove(scopeName);
            // Set currentScopeName to the previous scope name if available
            if (!table.isEmpty()) {
                currentScopeName = table.keySet().iterator().next();
            } else {
                currentScopeName = null; // No scopes left
            }
        } else {
            throw new RuntimeException("No scope to exit, scope " + scopeName + " not found");
        }
        return currentScopeName;
    }

    public void bind(String name, SymbolInfo info) {
        Map<String, SymbolInfo> currentScope = table.get(currentScopeName);
        if (currentScope.containsKey(name)) {
            throw new RuntimeException("Function " + name + " already declared in the current scope");
        }
        currentScope.put(name, info);
    }

    public SymbolInfo lookup(String name) {
        for (Map<String, SymbolInfo> scope : table.values()) {
            if (scope.containsKey(name)) {
                return scope.get(name);
            }
        }
        throw new RuntimeException("Function " + name + " not declared");
    }

    public String getCurrentScopeName() {
        return currentScopeName;
    }

    public void setId(String currentScopeName, int id) {
        for (Map<String, SymbolInfo> scope : table.values()) {
            for (Map.Entry<String, SymbolInfo> entry: scope.entrySet()) {
                if (entry.getValue().getName().equals(currentScopeName)) {
                    entry.getValue().setId(id);
                }
            }
        }
    }

    public Map<String, SymbolInfo> display() {
        for (Map.Entry<String, Map<String, SymbolInfo>> entry : table.entrySet()) {
//            System.out.println("Scope: " + entry.getKey());
            for (Map.Entry<String, SymbolInfo> innerEntry : entry.getValue().entrySet()) {
//                System.out.println(innerEntry.getKey() + " -> " + innerEntry.getValue());
                map.put(innerEntry.getValue().getName(), innerEntry.getValue());
            }
        }
        return map;
    }
    public void viewFunctionSymbolTable(Map<String, SymbolInfo> symbolTableMap) {
        for (Map.Entry<String, SymbolInfo> entry : symbolTableMap.entrySet()) {
            System.out.println(entry.getKey() + " -> " + entry.getValue());
        }
    }
}