import java.util.*;

class SymbolTable {
    private Stack<Map<String, SymbolInfo>> stack;
    private Map<Integer, SymbolInfo> map;

    public SymbolTable() {
        map = new HashMap<Integer, SymbolInfo>();
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

    public Map<Integer, SymbolInfo>  viewSymbolTable() {
        for (Map<String, SymbolInfo> scope : stack) {
            for (Map.Entry<String, SymbolInfo> entry : scope.entrySet()) {
                System.out.println("Var id: " + entry.getKey() + ", Info: " + entry.getValue().toString());
                map.put(Integer.valueOf(entry.getKey()), entry.getValue());
            }
        }
        return map;
    }

    public int getScopeLevel() {
        return stack.size() - 1;
    }
}

