import org.w3c.dom.*;
import javax.xml.parsers.*;
import java.io.*;
import java.util.*;

// TODO: implement symbol type map for Bob

public class Parser {
    private final List<Token> tokens;
    private Iterator<Token> tokenIterator;
    private Token currentToken;
    private int nodeId = 1;
    private final SymbolTable symbolTable;
    private final FunctionSymbolTable functionSymbolTable;
    private String funcName;
    private SymbolTable duplicateSymbolTable;
    private String tokenString;

    public Parser(String xmlFilePath) throws Exception {
        this.tokens = parseXML(xmlFilePath);
        this.tokenIterator = tokens.iterator();
        this.currentToken = tokenIterator.next();
        this.symbolTable = new SymbolTable();
        this.functionSymbolTable = new FunctionSymbolTable();
        this.duplicateSymbolTable = new SymbolTable();
        this.tokenString = "";
        displayTokens();
    }

    public SymbolTable getSymbolTable() {
        return duplicateSymbolTable;
    }

    public FunctionSymbolTable getFunctionSymbolTable() {
        return functionSymbolTable;
    }

    private void nextToken() {
        if (tokenIterator.hasNext()) {
            currentToken = tokenIterator.next();
        } else {
            currentToken = null; // End of tokens
        }
    }

    private void expect(TokenType type, String value) {
        System.out.println("Expected token of type " + type + " and value "+value+" but got " + currentToken.getType() + " with value " + currentToken.getValue());
        if (currentToken == null || (currentToken.getType() != type && !currentToken.getValue().equals(value))) {

                throw new RuntimeException("Expected token of type " + type + " and value "+value+" but got " + currentToken.getType() + " with value " + currentToken.getValue());
//            } else {
//                throw new RuntimeException("Expected token of type " + type + ", but got end of tokens");
            }

        nextToken();
    }

    private void expect(TokenType type) {
        System.out.println("Expected token of type " + type + ", and got " + currentToken.getType() + " with value " + currentToken.getValue());
        if (currentToken == null || currentToken.getType() != type) {
                throw new RuntimeException("Expected token of type " + type + ", but got " + currentToken.getType() + " with value " + currentToken.getValue());
         
        }
        nextToken();
    }

    public Node parse() {
        // First pass: Collect function declarations
        collectFunctionDeclarations();
        // Second pass: Parse the program
        return parsePROG();
    }

    private Node parsePROG() {
        Node node = new Node(nodeId++, "PROG");
        expect(TokenType.RESERVED_KEYWORD, "main");
        node.addChild(new Node(nodeId++, "main"));
        symbolTable.enterScope();
        duplicateSymbolTable.enterScope();
        node.addChild(parseGLOBVARS());
        node.addChild(parseALGO());
        node.addChild(parseFUNCTIONS());
        symbolTable.exitScope();
        return node;
    }

    private Node parseGLOBVARS() {
        Node node = new Node(nodeId++, "GLOBVARS");
        if (currentToken != null && (currentToken.getType() == TokenType.RESERVED_KEYWORD && currentToken.getValue().equals("num") || (currentToken.getType() == TokenType.RESERVED_KEYWORD && currentToken.getValue().equals("text")))) {
            node.addChild(parseVTYP());
            if (currentToken.getType() == TokenType.VARIABLE) {
                String varName = currentToken.getValue();
                symbolTable.bind(varName, new SymbolInfo("variableType", symbolTable.getScopeLevel(), nodeId + 1));
                Node prev = node.getLastChild().getLastChild();
                if (prev.getSymbol().equals("num")) {
                    duplicateSymbolTable.bind(Integer.toString(nodeId + 1), new SymbolInfo("variableType", duplicateSymbolTable.getScopeLevel(), varName, nodeId + 1,"n"));
                } else {
                    duplicateSymbolTable.bind(Integer.toString(nodeId + 1), new SymbolInfo("variableType", duplicateSymbolTable.getScopeLevel(), varName, nodeId + 1,"t"));
                }
            }
            node.addChild(parseVNAME());
            expect(TokenType.RESERVED_KEYWORD, ",");
            node.addChild(parseGLOBVARS());
        }
        return node;
    }


        private Node parseVTYP() {
        Node node = new Node(nodeId++, "VTYP");
        if (((currentToken.getType() == TokenType.RESERVED_KEYWORD && currentToken.getValue().equals("num")))) {
            node.addChild(new Node(nodeId++, currentToken.getValue()));
            expect(TokenType.RESERVED_KEYWORD, "num");
        } else if ((currentToken.getType() == TokenType.RESERVED_KEYWORD && currentToken.getValue().equals("text"))) {
            node.addChild(new Node(nodeId++, currentToken.getValue()));
            expect(TokenType.RESERVED_KEYWORD, "text");
        }
        return node;
    }

    private Node parseVNAME() {

        Node node = new Node(nodeId++, "VNAME");
        node.addChild(new Node(nodeId++, currentToken.getValue()));
        expect(TokenType.VARIABLE);

        return node;
    }

    private Node parseALGO() {
        Node node = new Node(nodeId++, "ALGO");
        expect(TokenType.RESERVED_KEYWORD, "begin");
        node.addChild(new Node(nodeId++, "begin"));
        node.addChild(parseINSTRUC());
        System.out.println("ALGO: " + currentToken.getValue());
        expect(TokenType.RESERVED_KEYWORD, "end");
        node.addChild(new Node(nodeId++, "end"));
        return node;
    }

    private Node parseINSTRUC() {
        Node node = new Node(nodeId++, "INSTRUC");
        if (currentToken == null || (currentToken.getType() == TokenType.RESERVED_KEYWORD && currentToken.getValue().equals("end"))) {
            return node; // Nullable
        }
        System.out.println("Current token: " + currentToken.getValue());
        node.addChild(parseCOMMAND());
        System.out.println("INSTRUC: " + currentToken.getValue());
        expect(TokenType.RESERVED_KEYWORD,";");
        System.out.println("INSTRUC: " + currentToken.getValue());
        node.addChild(parseINSTRUC());
        return node;
    }

    private Node parseCOMPOSIT() {
        Node node = new Node(nodeId++, "COMPOSIT");
        if (currentToken.getType() == TokenType.RESERVED_KEYWORD && (currentToken.getValue().equalsIgnoreCase("not") || currentToken.getValue().equalsIgnoreCase("sqrt"))) {
            node.addChild(parseUNOP());
            expect(TokenType.RESERVED_KEYWORD, "(");
            node.addChild(parseSIMPLE());
            expect(TokenType.RESERVED_KEYWORD, ")");
        } else if (currentToken.getType() == TokenType.RESERVED_KEYWORD && (currentToken.getValue().equalsIgnoreCase("or") || currentToken.getValue().equalsIgnoreCase("and") || currentToken.getValue().equalsIgnoreCase("eq") || currentToken.getValue().equalsIgnoreCase("grt") || currentToken.getValue().equalsIgnoreCase("add") || currentToken.getValue().equalsIgnoreCase("sub") || currentToken.getValue().equalsIgnoreCase("mul") || currentToken.getValue().equalsIgnoreCase("div"))) {
            System.out.println("Inside of inside the COMPOSITE FUNCTION" + currentToken.getValue());
            node.addChild(parseBINOP());
            expect(TokenType.RESERVED_KEYWORD, "(");
            node.addChild(parseSIMPLE());
            expect(TokenType.RESERVED_KEYWORD, ",");
            node.addChild(parseSIMPLE());
            expect(TokenType.RESERVED_KEYWORD, ")");
        } else {
            throw new RuntimeException("Unexpected token: " + currentToken);
        }
        return node;
    }


//    private Node parseCOMMAND() {
//        Node node = new Node(nodeId++, "COMMAND");
//        System.out.println("IN COMMAND: " + currentToken.getValue());
//        switch (currentToken.getType()) {
//            case RESERVED_KEYWORD:
//                switch (currentToken.getValue().toLowerCase()) {
//                    case "skip":
//                        node.addChild(new Node(nodeId++, "skip"));
//                        expect(TokenType.RESERVED_KEYWORD, "skip");
//                        break;
//                    case "halt":
//                        node.addChild(new Node(nodeId++, "halt"));
//                        expect(TokenType.RESERVED_KEYWORD, "halt");
//                        break;
//                    case "print":
//                        node.addChild(new Node(nodeId++, "print"));
//                        expect(TokenType.RESERVED_KEYWORD, "print");
//                        System.out.println(currentToken.getValue());
//                        node.addChild(parseATOMIC());
//                        System.out.println("Hello there");
//                        System.out.println(currentToken.getValue());
//                        break;
//                    case "input":
//                        node.addChild(new Node(nodeId++, "input"));
//                        expect(TokenType.RESERVED_KEYWORD, "input");
//                        node.addChild(parseVNAME());
//                        break;
//                    case "if":
//                        node.addChild(parseBRANCH());
//                        break;
//                    case "return":
//                        node.addChild(new Node(nodeId++, "return"));
//                        expect(TokenType.RESERVED_KEYWORD, "return");
//                        node.addChild(parseATOMIC());
//                        System.out.println("RETURN: " + currentToken.getValue());
//                        break;
//                    default:
//                        throw new RuntimeException("Unexpected token: " + currentToken.getValue());
//                }
//                return node;
//            case VARIABLE:
//                System.out.println("Hello there man");
//                node.addChild(parseASSIGN());
//                System.out.println("Hello there after assign of the function " + currentToken.getValue());
//                break;
//            case FUNCTION:
//                System.out.println("Function: " + currentToken.getValue());
//                node.addChild(parseCALL());
//                break;
//            default:
//                throw new RuntimeException("Unexpected token: " + currentToken.getValue());
//        }
//        System.out.println("Parsed command: " + node.getSymbol());
//        return node;
//    }

    private Node parseCOMMAND() {
        Node node = new Node(nodeId++, "COMMAND");
        System.out.println("IN COMMAND: " + currentToken.getValue());
        switch (currentToken.getType()) {
            case RESERVED_KEYWORD:
                switch (currentToken.getValue().toLowerCase()) {
                    case "skip":
                        node.addChild(new Node(nodeId++, "skip"));
                        expect(TokenType.RESERVED_KEYWORD, "skip");
                        break;
                    case "halt":
                        node.addChild(new Node(nodeId++, "halt"));
                        expect(TokenType.RESERVED_KEYWORD, "halt");
                        break;
                    case "print":
                        node.addChild(new Node(nodeId++, "print"));
                        expect(TokenType.RESERVED_KEYWORD, "print");
                        if (currentToken.getType() == TokenType.VARIABLE) {
                            String varName = currentToken.getValue();
                            if (symbolTable.lookup(varName) == null) {
                                throw new RuntimeException("Variable " + varName + " not declared");
                            }
                        }
                        node.addChild(parseATOMIC());
                        break;
                    case "input":
                        node.addChild(new Node(nodeId++, "input"));
                        expect(TokenType.RESERVED_KEYWORD, "input");
                        node.addChild(parseVNAME());
                        break;
                    case "if":
                        node.addChild(parseBRANCH());
                        break;
                    case "return":
                        node.addChild(new Node(nodeId++, "return"));
                        expect(TokenType.RESERVED_KEYWORD, "return");
                        node.addChild(parseATOMIC());
                        break;
                    default:
                        throw new RuntimeException("Unexpected token: " + currentToken.getValue());
                }
                break;
            case VARIABLE:
                node.addChild(parseASSIGN());
                break;
            case FUNCTION:
                node.addChild(parseCALL());
                break;
            default:
                throw new RuntimeException("Unexpected token: " + currentToken.getValue());
        }
        return node;
    }

    private Node parseATOMIC() {
        Node node = new Node(nodeId++, "ATOMIC");
        System.out.println("ATOMIC: " + currentToken.getValue());
        if (currentToken.getType() == TokenType.VARIABLE) {
            String varName = currentToken.getValue();
            if (symbolTable.lookup(varName) == null) {
                throw new RuntimeException("Variable " + varName + " not declared");
            }
            node.addChild(parseVNAME());
        } else if (currentToken.getType() == TokenType.NUMBER || currentToken.getType() == TokenType.TEXT) {
            node.addChild(parseCONST());
        }
        return node;
    }

    private Node parseCONST() {
        Node node = new Node(nodeId++, "CONST");
        node.addChild(new Node(nodeId++, currentToken.getValue()));
        if (currentToken.getType() == TokenType.NUMBER) {
            expect(TokenType.NUMBER);
        } else if (currentToken.getType() == TokenType.TEXT) {
            expect(TokenType.TEXT);
        } else {
            throw new RuntimeException("Unexpected token: " + currentToken.getValue());
        }
        return node;
    }

    private Node parseASSIGN() {
        Node node = new Node(nodeId++, "ASSIGN");
        String varName = currentToken.getValue();
        if (symbolTable.lookup(varName) == null) {
            throw new RuntimeException("Variable " + varName + " not declared");
        }
        node.addChild(parseVNAME());
        StringBuilder valueBuilder = new StringBuilder();
        if (currentToken.getType() == TokenType.RESERVED_KEYWORD && currentToken.getValue().equalsIgnoreCase("< input")) {
            node.addChild(new Node(nodeId++, "< input"));
            expect(TokenType.RESERVED_KEYWORD, "< input");
        } else if (currentToken.getType() == TokenType.RESERVED_KEYWORD && currentToken.getValue().equalsIgnoreCase("=")) {
            System.out.println("CHEKCING --- THIS IS IN PARSE ASSING ");
            System.out.println("ASSIGN: " + currentToken.getValue());
            node.addChild(new Node(nodeId++, "="));
            expect(TokenType.RESERVED_KEYWORD, "=");

            // Create a deep copy of the tokenIterator
            List<Token> tokenList = new ArrayList<>(tokens);
            ListIterator<Token> tokenCopyIterator = tokenList.listIterator(tokens.indexOf(currentToken));
            Token currentTokenCopy = tokenCopyIterator.next();

            // Collect token values until ';' is encountered
            while (currentTokenCopy != null && (currentTokenCopy.getType() != TokenType.RESERVED_KEYWORD || !currentTokenCopy.getValue().equals(";"))) {
                valueBuilder.append(currentTokenCopy.getValue()).append("");
                if (tokenCopyIterator.hasNext()) {
                    currentTokenCopy = tokenCopyIterator.next();
                } else {
                    currentTokenCopy = null;
                }
            }

            for (int currentScopeLevel = duplicateSymbolTable.getScopeLevel(); currentScopeLevel >= 0; currentScopeLevel--) {
                if (duplicateSymbolTable.idLookup(currentScopeLevel, varName) != null) {
                    duplicateSymbolTable.setSymbolValue(duplicateSymbolTable.idLookup(currentScopeLevel, varName), valueBuilder.toString().trim());
                    break;
                }
            }

            node.addChild(parseTERM());
            System.out.println("AFTER TERM IN ASSIGN: " + currentToken.getValue());
        }
        return node;
    }
    private Node parseCALL() {
        Node node = new Node(nodeId++, "CALL");
        if (currentToken.getType() == TokenType.FUNCTION) {
            String funcName = currentToken.getValue();
            if (!funcName.equals("main") && !functionSymbolTable.getCurrentScopeName().equals(funcName) && functionSymbolTable.lookup(funcName) == null) {
                throw new RuntimeException("Function " + funcName + " not declared");
            }
        }
        node.addChild(parseFNAME());
        expect(TokenType.RESERVED_KEYWORD, "(");
        node.addChild(parseATOMIC());
        expect(TokenType.RESERVED_KEYWORD,",");
        node.addChild(parseATOMIC());
        expect(TokenType.RESERVED_KEYWORD,",");
        node.addChild(parseATOMIC());
        expect(TokenType.RESERVED_KEYWORD, ")");

        return node;
    }

    private Node parseBRANCH() {
        Node node = new Node(nodeId++, "BRANCH");
        expect(TokenType.RESERVED_KEYWORD, "if");
        node.addChild(new Node(nodeId++, "if"));
        node.addChild(parseCOND());
        expect(TokenType.RESERVED_KEYWORD, "then");
        node.addChild(new Node(nodeId++, "then"));
        node.addChild(parseALGO());
        System.out.println("BRANCH ifs END: " + currentToken.getValue());
        expect(TokenType.RESERVED_KEYWORD, "else");
        node.addChild(new Node(nodeId++, "else"));
        node.addChild(parseALGO());
        System.out.println("BRANCH ifs END: " + currentToken.getValue());
        return node;
    }

    private Node parseTERM() {
        Node node = new Node(nodeId++, "TERM");
        System.out.println("TERM: " + currentToken.getValue());
        switch (currentToken.getType()) {
            case VARIABLE:
            case NUMBER:
            case TEXT:
                if (currentToken.getType() == TokenType.VARIABLE) {
                    String varName = currentToken.getValue();
                    if (symbolTable.lookup(varName) == null) {
                        throw new RuntimeException("Variable " + varName + " not declared");
                    }
                }
                node.addChild(parseATOMIC());
                break;
            case FUNCTION:
                System.out.println("TERM: " + currentToken.getValue());
                node.addChild(parseCALL());
                System.out.println("AFTER CALL IN TERM: " + currentToken.getValue());
                System.out.println("YOLO MANS");
                break;
            case RESERVED_KEYWORD:
                switch (currentToken.getValue()) {
                    case "not":
                    case "sqrt":
                    case "or":
                    case "and":
                    case "eq":
                    case "grt":
                    case "add":
                    case "sub":
                    case "mul":
                    case "div":
                        node.addChild(parseOP());
                        break;
                    default:
                        throw new RuntimeException("Unexpected token: " + currentToken.getValue());
                }

                return node;
            default:
                throw new RuntimeException("Unexpected token: " + currentToken.getValue());
        }
        return node;
    }

//    private Node parseCOND() {
//        Node node = new Node(nodeId++, "COND");
//        node.addChild(parseSIMPLE());
//        return node;
//    }

//    private Node parseCOND() {
//        Node node = new Node(nodeId++, "COND");
//        if(currentToken != null) {
//            Iterator<Token> it =  this.tokenIterator;
//            List<Token> cpy = new ArrayList<>();
//            it.forEachRemaining(cpy::add);
//            Iterator<Token> tokenIterator = cpy.iterator();
//            boolean simple = false;
//            int param_count = 0;
//            while (tokenIterator.hasNext()) {
//                System.out.println("yessss:: "+ tokenIterator.next().getValue());
//                Token token = tokenIterator.next();
//                if (token.getType() == TokenType.RESERVED_KEYWORD && token.getValue().equals("(")) {
//                    System.out.println("TOKEN: " + token.getValue());
//                    if(param_count<1) {
//                        param_count++;
//                        continue;
//                    }
//                    simple = true;
//                    break;
//                }else if(token.getType() == TokenType.RESERVED_KEYWORD && token.getValue().equals(","))
//                {
//                    System.out.println("in the commmma");
//                    break;
//                }
//            }
//            System.out.println("SIMPLE: " + simple);
//
//            System.out.println("COND CCCURRTENT TOKEN: " + currentToken.getValue());
//            if ((!simple) && currentToken.getType() == TokenType.RESERVED_KEYWORD && (currentToken.getType() == TokenType.RESERVED_KEYWORD && (currentToken.getValue().equalsIgnoreCase("or") || currentToken.getValue().equalsIgnoreCase("and") || currentToken.getValue().equalsIgnoreCase("eq") || currentToken.getValue().equalsIgnoreCase("grt") || currentToken.getValue().equalsIgnoreCase("add") || currentToken.getValue().equalsIgnoreCase("sub") || currentToken.getValue().equalsIgnoreCase("mul") || currentToken.getValue().equalsIgnoreCase("div")))) {
//                node.addChild(parseSIMPLE());
//            } else if (simple && (currentToken.getValue().equalsIgnoreCase("not") || currentToken.getValue().equalsIgnoreCase("sqrt")) || (currentToken.getType() == TokenType.RESERVED_KEYWORD && (currentToken.getValue().equalsIgnoreCase("or") || currentToken.getValue().equalsIgnoreCase("and") || currentToken.getValue().equalsIgnoreCase("eq") || currentToken.getValue().equalsIgnoreCase("grt") || currentToken.getValue().equalsIgnoreCase("add") || currentToken.getValue().equalsIgnoreCase("sub") || currentToken.getValue().equalsIgnoreCase("mul") || currentToken.getValue().equalsIgnoreCase("div")))) {
//                System.out.println("In the composite condition");
//                node.addChild(parseCOMPOSIT());
//            } else {
//                throw new RuntimeException("Unexpected token: " + currentToken);
//            }
//        }
//        return node;
//    }

    private Node parseCOND() {
        Node node = new Node(nodeId++, "COND");
        if (currentToken != null) {
            if (isCOMPOSIT()) {
                node.addChild(parseCOMPOSIT());
            } else if (isSIMPLE()) {
                System.out.println("helllo");
                node.addChild(parseSIMPLE());
            } else {
                throw new RuntimeException("Unexpected token: " + currentToken);
            }
        }
        return node;
    }

    private boolean isCOMPOSIT() {
        // Lookahead logic to determine if the next tokens form a COMPOSIT
        List<Token> tokens = new ArrayList<>(this.tokens); // Create a new list from the original tokens
        ListIterator<Token> iterator = tokens.listIterator(tokens.indexOf(currentToken));
        int openBrackets = 0;

        while (iterator.hasNext()) {
            Token token = iterator.next();
            if (token.getType() == TokenType.RESERVED_KEYWORD && token.getValue().equals("(")) {
                openBrackets++;
            } else if (token.getType() == TokenType.RESERVED_KEYWORD && token.getValue().equals(")")) {
                openBrackets--;
            } else if (token.getType() == TokenType.RESERVED_KEYWORD && token.getValue().equals(",")) {
                return openBrackets != 1; // Only one level of nesting
            }
            System.out.println("TOKEN: " + openBrackets);
        }
        return false;
    }

    private boolean isSIMPLE() {
        // Check if the current structure matches SIMPLE's pattern
        return currentToken != null && currentToken.getType() == TokenType.RESERVED_KEYWORD &&
                (currentToken.getValue().equalsIgnoreCase("or") || currentToken.getValue().equalsIgnoreCase("and") ||
                        currentToken.getValue().equalsIgnoreCase("eq") || currentToken.getValue().equalsIgnoreCase("grt") ||
                        currentToken.getValue().equalsIgnoreCase("add") || currentToken.getValue().equalsIgnoreCase("sub") ||
                        currentToken.getValue().equalsIgnoreCase("mul") || currentToken.getValue().equalsIgnoreCase("div"));
    }


    private Node parseSIMPLE() {
        Node node = new Node(nodeId++, "SIMPLE");
        node.addChild(parseBINOP());
        expect(TokenType.RESERVED_KEYWORD, "(");
        node.addChild(parseATOMIC());
        expect(TokenType.RESERVED_KEYWORD, ",");
        node.addChild(parseATOMIC());
        expect(TokenType.RESERVED_KEYWORD, ")");
        return node;
    }

    private Node parseBINOP() {
        Node node = new Node(nodeId++, "BINOP");
        switch (currentToken.getValue().toLowerCase()) {
            case "or":
                node.addChild(new Node(nodeId++, "or"));
                expect(TokenType.RESERVED_KEYWORD, "or");
                break;
            case "and":
                node.addChild(new Node(nodeId++, "and"));
                expect(TokenType.RESERVED_KEYWORD, "and");
                break;
            case "eq":
                node.addChild(new Node(nodeId++, "eq"));
                expect(TokenType.RESERVED_KEYWORD, "eq");
                break;
            case "grt":
                node.addChild(new Node(nodeId++, "grt"));
                expect(TokenType.RESERVED_KEYWORD, "grt");
                break;
            case "add":
                node.addChild(new Node(nodeId++, "add"));
                expect(TokenType.RESERVED_KEYWORD, "add");
                break;
            case "sub":
                node.addChild(new Node(nodeId++, "sub"));
                expect(TokenType.RESERVED_KEYWORD, "sub");
                break;
            case "mul":
                node.addChild(new Node(nodeId++, "mul"));
                expect(TokenType.RESERVED_KEYWORD, "mul");
                break;
            case "div":
                node.addChild(new Node(nodeId++, "div"));
                expect(TokenType.RESERVED_KEYWORD, "div");
                break;
            default:
                throw new RuntimeException("Unexpected token: " + currentToken.getValue());
        }
        System.out.println("BINOP: " + currentToken.getValue());
        return node;
    }

    private Node parseFNAME() {
        System.out.println("FNAME: " + currentToken.getValue());
        Node node = new Node(nodeId++, "FNAME");
        node.addChild(new Node(nodeId++, currentToken.getValue()));
        expect(TokenType.FUNCTION);
        return node;
    }

    private Node parseFUNCTIONS() {
        Node node = new Node(nodeId++, "FUNCTIONS");
        if (currentToken == null || (currentToken.getType() == TokenType.RESERVED_KEYWORD && currentToken.getValue().equals("end"))) {
            return node; // Nullable
        }
        node.addChild(parseDECL());
//        System.out.println("FUNCTIONS: " + currentToken.getValue());
        node.addChild(parseFUNCTIONS());
        return node;
    }

    private Node parseDECL() {
        symbolTable.enterScope();
        duplicateSymbolTable.enterScope();
        System.out.println("DECL: " + currentToken.getValue());
        Node node = new Node(nodeId++, "DECL");
        node.addChild(parseHEADER());
        node.addChild(parseBODY());
        symbolTable.exitScope();
        funcName = functionSymbolTable.exitScope(funcName);
        return node;
    }

    private Node parseHEADER() {
        System.out.println("HEADER: " + currentToken.getValue());
        Node node = new Node(nodeId++, "HEADER");
        node.addChild(parseFTYP());
        if (currentToken.getType() == TokenType.FUNCTION) {
            funcName = currentToken.getValue();
            functionSymbolTable.enterScope(funcName);
        }
        node.addChild(parseFNAME());
        expect(TokenType.RESERVED_KEYWORD, "(");
        if (currentToken.getType() == TokenType.VARIABLE) {
            String varName = currentToken.getValue();
            symbolTable.bind(varName, new SymbolInfo("variableType", symbolTable.getScopeLevel(), nodeId + 1));
            duplicateSymbolTable.bind(Integer.toString(nodeId + 1), new SymbolInfo("variableType", duplicateSymbolTable.getScopeLevel(), varName, nodeId + 1, "n"));
        }
        node.addChild(parseVNAME());
        expect(TokenType.RESERVED_KEYWORD, ",");
        if (currentToken.getType() == TokenType.VARIABLE) {
            String varName = currentToken.getValue();
            symbolTable.bind(varName, new SymbolInfo("variableType", symbolTable.getScopeLevel(), nodeId + 1));
            duplicateSymbolTable.bind(Integer.toString(nodeId + 1), new SymbolInfo("variableType", duplicateSymbolTable.getScopeLevel(), varName, nodeId + 1, "n"));
        }
        node.addChild(parseVNAME());
        expect(TokenType.RESERVED_KEYWORD, ",");
        if (currentToken.getType() == TokenType.VARIABLE) {
            String varName = currentToken.getValue();
            symbolTable.bind(varName, new SymbolInfo("variableType", symbolTable.getScopeLevel(), nodeId + 1));
            duplicateSymbolTable.bind(Integer.toString(nodeId + 1), new SymbolInfo("variableType", duplicateSymbolTable.getScopeLevel(), varName, nodeId + 1, "n"));
        }
        node.addChild(parseVNAME());
        expect(TokenType.RESERVED_KEYWORD, ")");
        return node;
    }

    private Node parseLOCVARS() {
        System.out.println("HEY THERE IN LOCAL VARS");
        System.out.println("Current token: " + currentToken.getValue());
        Node node = new Node(nodeId++, "LOCVARS");
        if (currentToken != null && (currentToken.getType() == TokenType.RESERVED_KEYWORD && currentToken.getValue().equals("num") || (currentToken.getType() == TokenType.RESERVED_KEYWORD && currentToken.getValue().equals("text")))) {
            node.addChild(parseVTYP());
            if (currentToken.getType() == TokenType.VARIABLE) {
                String varName = currentToken.getValue();
                symbolTable.bind(varName, new SymbolInfo("variableType", symbolTable.getScopeLevel(), nodeId + 1));
                Node prev = node.getLastChild().getLastChild();
                if (prev.getSymbol().equals("num")) {
                    System.out.println("Previous: " + prev.getSymbol());
                    duplicateSymbolTable.bind(Integer.toString(nodeId + 1), new SymbolInfo("variableType", duplicateSymbolTable.getScopeLevel(), varName, nodeId + 1,"n"));
                } else {
                    System.out.println("Previous: " + prev.getSymbol());
                    duplicateSymbolTable.bind(Integer.toString(nodeId + 1), new SymbolInfo("variableType", duplicateSymbolTable.getScopeLevel(), varName, nodeId + 1,"t"));
                }
            }
            node.addChild(parseVNAME());
            expect(TokenType.RESERVED_KEYWORD, ",");
            node.addChild(parseLOCVARS());
        }
        return node;
    }


    private Node parseFTYP() {
        Node node = new Node(nodeId++, "FTYP");
        //made a change to the check from checking Number to it checking for Reserved_Keyword num
        //it also now expects keyword num aswell for the first if statement
        if ((currentToken.getType() == TokenType.RESERVED_KEYWORD && currentToken.getValue().equalsIgnoreCase("num"))) {
            node.addChild(new Node(nodeId++, currentToken.getValue()));
            expect(TokenType.RESERVED_KEYWORD, "num");
        } else if (currentToken.getType() == TokenType.RESERVED_KEYWORD && currentToken.getValue().equalsIgnoreCase("void")) {
            node.addChild(new Node(nodeId++, "void"));
            expect(TokenType.RESERVED_KEYWORD, "void");
        }
        return node;
    }

    private Node parseBODY() {
        Node node = new Node(nodeId++, "BODY");
        expect(TokenType.RESERVED_KEYWORD, "{");
        node.addChild(new Node(nodeId++, "{"));
        node.addChild(parseLOCVARS());
        System.out.println("BODY: " + currentToken.getValue());
        node.addChild(parseALGO());
        expect(TokenType.RESERVED_KEYWORD, "}");
        node.addChild(new Node(nodeId++, "}"));
        System.out.println("BODY: " + currentToken.getValue());
        node.addChild(parseSUBFUNCS());
        expect(TokenType.RESERVED_KEYWORD, "end");
        node.addChild(new Node(nodeId++, "end"));
        return node;
    }


    private Node parseSUBFUNCS() {
        Node node = new Node(nodeId++, "SUBFUNCS");
        node.addChild(parseFUNCTIONS());
        return node;
    }

    private Node parseOP() {
        Node node = new Node(nodeId++, "OP");
        if (currentToken.getType() == TokenType.RESERVED_KEYWORD && (currentToken.getValue().equalsIgnoreCase("not") || currentToken.getValue().equalsIgnoreCase("sqrt"))) {
            node.addChild(parseUNOP());
            expect(TokenType.RESERVED_KEYWORD,"(");
            if (currentToken.getType() == TokenType.VARIABLE) {
                String varName = currentToken.getValue();
                if (symbolTable.lookup(varName) == null) {
                    throw new RuntimeException("Variable " + varName + " not declared");
                }
            }
            node.addChild(parseARG());
            expect(TokenType.RESERVED_KEYWORD,")");
        } else if (currentToken.getType() == TokenType.RESERVED_KEYWORD && (currentToken.getValue().equalsIgnoreCase("or") || currentToken.getValue().equalsIgnoreCase("and") || currentToken.getValue().equalsIgnoreCase("eq") || currentToken.getValue().equalsIgnoreCase("grt") || currentToken.getValue().equalsIgnoreCase("add") || currentToken.getValue().equalsIgnoreCase("sub") || currentToken.getValue().equalsIgnoreCase("mul") || currentToken.getValue().equalsIgnoreCase("div"))) {
            node.addChild(parseBINOP());
            expect(TokenType.RESERVED_KEYWORD, "(");
            if (currentToken.getType() == TokenType.VARIABLE) {
                String varName = currentToken.getValue();
                if (symbolTable.lookup(varName) == null) {
                    throw new RuntimeException("Variable " + varName + " not declared");
                }
            }
            node.addChild(parseARG());
            expect(TokenType.RESERVED_KEYWORD,",");
            if (currentToken.getType() == TokenType.VARIABLE) {
                String varName = currentToken.getValue();
                if (symbolTable.lookup(varName) == null) {
                    throw new RuntimeException("Variable " + varName + " not declared");
                }
            }
            node.addChild(parseARG());
            expect(TokenType.RESERVED_KEYWORD,")");
        } else {
            throw new RuntimeException("Unexpected token: " + currentToken.getValue());
        }
        return node;
    }

    private Node parseUNOP() {
        Node node = new Node(nodeId++, "UNOP");
        switch (currentToken.getValue()) {
            case "not":
                node.addChild(new Node(nodeId++, "not"));
                expect(TokenType.RESERVED_KEYWORD, "not");
                break;
            case "sqrt":
                node.addChild(new Node(nodeId++, "sqrt"));
                expect(TokenType.RESERVED_KEYWORD, "sqrt");
                break;
            default:
                throw new RuntimeException("Unexpected token: " + currentToken.getValue());
        }
        return node;
    }

    private Node parseARG() {
        Node node = new Node(nodeId++, "ARG");
        if (currentToken.getType() == TokenType.VARIABLE || currentToken.getType() == TokenType.NUMBER || currentToken.getType() == TokenType.TEXT) {
            node.addChild(parseATOMIC());
        } else if ((currentToken.getType() == TokenType.RESERVED_KEYWORD && (currentToken.getValue().equalsIgnoreCase("or") || currentToken.getValue().equalsIgnoreCase("and")) || (currentToken.getType() == TokenType.RESERVED_KEYWORD && (currentToken.getValue().equalsIgnoreCase("eq") || currentToken.getValue().equalsIgnoreCase("grt") || currentToken.getValue().equalsIgnoreCase("add") || currentToken.getValue().equalsIgnoreCase("sub") || currentToken.getValue().equalsIgnoreCase("mul") || currentToken.getValue().equalsIgnoreCase("div"))))) {
            node.addChild(parseOP());
        } else {
            throw new RuntimeException("Unexpected token: " + currentToken.getValue());
        }
        return node;
    }

    private List<Token> parseXML(String xmlFilePath) throws Exception {
        List<Token> tokens = new ArrayList<>();
        File inputFile = new File(xmlFilePath);
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.parse(inputFile);
        doc.getDocumentElement().normalize();

        NodeList nList = doc.getElementsByTagName("TOK");
        for (int temp = 0; temp < nList.getLength(); temp++) {
            org.w3c.dom.Node nNode = nList.item(temp);
            if (nNode.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE) {
                Element eElement = (Element) nNode;
                String type = eElement.getElementsByTagName("CLASS").item(0).getTextContent();
                String value = eElement.getElementsByTagName("WORD").item(0).getTextContent();
                tokens.add(new Token(TokenType.valueOf(type), value));
            }
        }
        return tokens;
    }

    private void collectFunctionDeclarations() {
        int scopeLevel = 1;
        while (currentToken != null) {
            if (currentToken.getType() == TokenType.RESERVED_KEYWORD && (currentToken.getValue().equals("void") || currentToken.getValue().equals("num"))) {
                String type = currentToken.getValue();
                SymbolInfo functionInfo;
                nextToken();
                if (currentToken.getType() == TokenType.FUNCTION) {
                    String funcName = currentToken.getValue();
                    if (type.equals("num")) {
                        functionInfo = new SymbolInfo("functionType", ++scopeLevel,funcName, nodeId + 1, "n");
                    } else {
                        functionInfo = new SymbolInfo("functionType", ++scopeLevel,funcName, nodeId + 1, "v");
                    }
                    functionSymbolTable.bind(funcName, functionInfo);

                    while (currentToken != null && !currentToken.getValue().equals("}")) {
                        if (currentToken.getValue().equals("return")) {
                            nextToken();
                            if (currentToken != null) {
                                functionInfo.setValue(currentToken.getValue());
                            }
                        }
                        nextToken();
                    }
                }
            }
            nextToken();
        }
        // Reset token iterator for the second pass
        this.tokenIterator = tokens.iterator();
        this.currentToken = tokenIterator.next();
    }

//    private void performSemanticAnalysis() throws SemanticException {
//        SemanticAnalyzer analyzer = new SemanticAnalyzer(syntaxTree);
//        analyzer.analyze();
//    }

//    public SyntaxTree getSyntaxTree() {
//        return syntaxTree;
//    }

    public void displayTokens() {
        for (Token token : tokens) {
            System.out.println("Type: " + token.getType() + ", Value: " + token.getValue());
        }
    }

//    public void generateSyntaxTreeXML(String outputFilePath) throws Exception {
//        syntaxTree.generateXML(outputFilePath);
//    }
}