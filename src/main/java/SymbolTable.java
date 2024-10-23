import java.util.*;

class SymbolTable {
    private Stack<Map<String, SymbolInfo>> stack;
    private Map<String, SymbolInfo> map;

    public SymbolTable() {
        map = new HashMap<String, SymbolInfo>();
        stack = new Stack<>();
        enterScope(); // Initialize with the global scope
    }

    public SymbolTable(SymbolTable symbolTable) {
        map = new HashMap<String, SymbolInfo>();
        stack = new Stack<>();
        for (Map<String, SymbolInfo> scope : symbolTable.stack) {
            stack.push(new HashMap<>(scope));
        }
    }

    public void enterScope() {
        stack.push(new HashMap<>());
    }

    public void exitScope() {
        if (!stack.isEmpty()) {
            stack.pop();
        } else {
            throw new RuntimeException("No scope to exit");
        }
    }

    public void bind(String name, SymbolInfo info) {
        Map<String, SymbolInfo> currentScope = stack.peek();
        if (currentScope.containsKey(name)) {
            throw new RuntimeException("Variable " + name + " already declared in the current scope");
        }
        currentScope.put(name, info);
    }

    public SymbolInfo lookup(String name) {
        for (int i = stack.size() - 1; i >= 0; i--) {
            Map<String, SymbolInfo> scope = stack.get(i);
            if (scope.containsKey(name)) {
                return scope.get(name);
            }
        }
        throw new RuntimeException("Variable " + name + " not declared");
    }

    public String idLookup(int scopeLevel, String varName) {
        for (Map<String, SymbolInfo> scope : stack) {
            for (Map.Entry<String, SymbolInfo> entry: scope.entrySet()) {
                if (entry.getValue().getScopeLevel() == scopeLevel && entry.getValue().getName().equals(varName)) {
                    return entry.getKey();
                }
            }
        }
        return null;
    }

    public void setSymbolValue(String id, String value) {
        for (int i = stack.size() - 1; i >= 0; i--) {
            Map<String, SymbolInfo> scope = stack.get(i);
            if (scope.containsKey(id)) {
                scope.get(id).setValue(value);
                return;
            }
        }
        throw new RuntimeException("Variable " + id + " not declared");
    }


    public Map<String, SymbolInfo>  viewSymbolTable() {
        for (Map<String, SymbolInfo> scope : stack) {
            for (Map.Entry<String, SymbolInfo> entry : scope.entrySet()) {
//                System.out.println("Var: " + entry.getValue().getName() + ", Info: " + entry.getValue().toString());
                map.put(entry.getValue().getName(), entry.getValue());
            }
        }
        return map;
    }



    public int getScopeLevel() {
        return stack.size() - 1;
    }
}

