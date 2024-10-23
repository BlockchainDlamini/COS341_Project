import java.util.*;

class SymbolTable {
    private Stack<Map<String, SymbolInfo>> stack;

    public SymbolTable() {
        stack = new Stack<>();
        enterScope(); // Initialize with the global scope
    }

    public SymbolTable(SymbolTable symbolTable) {
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

    public void viewSymbolTable() {
        for (Map<String, SymbolInfo> scope : stack) {
            for (Map.Entry<String, SymbolInfo> entry : scope.entrySet()) {
                System.out.println("Var id: " + entry.getKey() + ", Info: " + entry.getValue().toString());
            }
        }
    }

    public int getScopeLevel() {
        return stack.size() - 1;
    }
}

