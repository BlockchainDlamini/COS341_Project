import java.util.*;

class Scope {
    private Stack<Map<String, SymbolInfo>> queue;
    private String currentScopeName;

    public Scope() {
        queue = new Stack<>();
        enterScope("main"); // Initialize with the global scope
    }

    public void enterScope(String scopeName) {
        if (!queue.isEmpty()) {
            Map<String, SymbolInfo> parentScope = queue.peek();
            if (parentScope.containsKey(scopeName)) {
                throw new RuntimeException("Scope " + scopeName + " already declared in the current scope");
            }
        }
        queue.push(new HashMap<>());
        currentScopeName = scopeName;
    }

    public void exitScope() {
        if (!queue.isEmpty()) {
            queue.pop();
        } else {
            throw new RuntimeException("No scope to exit");
        }
    }

    public void bind(String name, SymbolInfo info) {
        Map<String, SymbolInfo> currentScope = queue.peek();
        if (currentScope.containsKey(name)) {
            throw new RuntimeException("Function " + name + " already declared in the current scope");
        }
        currentScope.put(name, info);
    }

    public SymbolInfo lookup(String name) {
        for (int i = queue.size() - 1; i >= 0; i--) {
            Map<String, SymbolInfo> scope = queue.get(i);
            if (scope.containsKey(name)) {
                return scope.get(name);
            }
        }
        throw new RuntimeException("Function " + name + " not declared");
    }

    public String getCurrentScopeName() {
        return currentScopeName;
    }

    public int getScopeLevel() {
        return queue.size() - 1;
    }
}