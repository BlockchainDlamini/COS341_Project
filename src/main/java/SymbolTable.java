import java.util.*;

class SymbolTable {
    private Stack<Map<String, SymbolInfo>> stack;

    public SymbolTable() {
        stack = new Stack<>();
        enterScope(); // Initialize with the global scope
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

    public int getScopeLevel() {
        return stack.size() - 1;
    }
}

