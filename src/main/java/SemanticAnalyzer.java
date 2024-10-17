import java.util.*;

public class SemanticAnalyzer {
    private final SyntaxTree syntaxTree;
    private final Set<String> functionNames = new HashSet<>();
    private final Set<String> variableNames = new HashSet<>();
    private final Set<String> reservedKeywords = Set.of("main", "num", "text", "begin", "end", "skip", "halt", "print", "if", "then", "else", "not", "sqrt", "or", "and", "eq", "grt", "add", "sub", "mul", "div");

    public SemanticAnalyzer(SyntaxTree syntaxTree) {
        this.syntaxTree = syntaxTree;
    }

    public void analyze() throws SemanticException {
        checkFunctionNames(syntaxTree.getRoot(), null, new HashSet<>());
        checkVariableNames(syntaxTree.getRoot(), new HashSet<>());
    }

    private void checkFunctionNames(Node node, String parentScope, Set<String> siblingScopes) throws SemanticException {
        if (node instanceof InnerNode) {
            String nodeName = node.getSymbol();
            if (parentScope != null && nodeName.equals(parentScope)) {
                throw new SemanticException("Child scope cannot have the same name as its parent scope: " + nodeName);
            }
            if (siblingScopes.contains(nodeName)) {
                throw new SemanticException("Sibling scopes cannot have the same name: " + nodeName);
            }
            siblingScopes.add(nodeName);
            functionNames.add(nodeName);

            for (Node child : node.getChildren()) {
                checkFunctionNames(child, nodeName, new HashSet<>());
            }
        }
    }

    private void checkVariableNames(Node node, Set<String> currentScopeVariables) throws SemanticException {
        if (node instanceof LeafNode) {
            String nodeName = node.getSymbol();
            if (currentScopeVariables.contains(nodeName)) {
                throw new SemanticException("Variable name double declared in the same scope: " + nodeName);
            }
            if (reservedKeywords.contains(nodeName)) {
                throw new SemanticException("Variable name cannot be a reserved keyword: " + nodeName);
            }
            if (functionNames.contains(nodeName)) {
                throw new SemanticException("Variable name cannot be the same as a function name: " + nodeName);
            }
            currentScopeVariables.add(nodeName);
            variableNames.add(nodeName);
        }

        for (Node child : node.getChildren()) {
            checkVariableNames(child, new HashSet<>(currentScopeVariables));
        }
    }
}