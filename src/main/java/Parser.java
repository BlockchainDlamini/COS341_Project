import org.w3c.dom.*;
import javax.xml.parsers.*;
import java.io.*;
import java.util.*;

public class Parser {
    private final List<Token> tokens;
    private final Iterator<Token> tokenIterator;
    private final SyntaxTree syntaxTree;
    private Token currentToken;
    private int nodeId = 1;

    public Parser(String xmlFilePath) throws Exception {
        this.tokens = parseXML(xmlFilePath);
        this.tokenIterator = tokens.iterator();
        this.currentToken = tokenIterator.next();
        displayTokens();
        this.syntaxTree = new SyntaxTree(this.tokens);
        performSemanticAnalysis();
    }

    private void nextToken() {
        if (tokenIterator.hasNext()) {
            currentToken = tokenIterator.next();
        } else {
            currentToken = null; // End of tokens
        }
    }

    private void expect(TokenType type, String value) {
        if (currentToken == null || currentToken.getType() != type) {
            if (currentToken != null) {
                throw new RuntimeException("Expected token of type " + type + ", but got " + currentToken.getType() + " with value " + currentToken.getValue());
            } else {
                throw new RuntimeException("Expected token of type " + type + ", but got end of tokens");
            }
        }
        nextToken();
    }

    private void expect(TokenType type) {
        if (currentToken == null || currentToken.getType() != type) {
            if (currentToken != null) {
                throw new RuntimeException("Expected token of type " + type + ", but got " + currentToken.getType() + " with value " + currentToken.getValue());
            } else {
                throw new RuntimeException("Expected token of type " + type + ", but got end of tokens");
            }
        }
        nextToken();
    }

    public Node parse() {
        return parsePROG();
    }

    private Node parsePROG() {
        Node node = new Node(nodeId++, "PROG");
        expect(TokenType.RESERVED_KEYWORD, "main");
        node.addChild(new Node(nodeId++, "main"));
        node.addChild(parseGLOBVARS());
        node.addChild(parseALGO());
        node.addChild(parseFUNCTIONS());
        return node;
    }

    private Node parseGLOBVARS() {
        Node node = new Node(nodeId++, "GLOBVARS");
        while (currentToken != null && (currentToken.getType() == TokenType.NUMBER || currentToken.getType() == TokenType.TEXT)) {
            node.addChild(parseVTYP());
            node.addChild(parseVNAME());
            expect(TokenType.RESERVED_KEYWORD, ",");
        }
        return node;
    }

    private Node parseVTYP() {
        Node node = new Node(nodeId++, "VTYP");
        if (currentToken.getType() == TokenType.NUMBER) {
            node.addChild(new Node(nodeId++, currentToken.getValue()));
            expect(TokenType.NUMBER);
        } else if (currentToken.getType() == TokenType.TEXT) {
            node.addChild(new Node(nodeId++, currentToken.getValue()));
            expect(TokenType.TEXT);
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
        expect(TokenType.RESERVED_KEYWORD,";");
        node.addChild(parseINSTRUC());
        return node;
    }

    private Node parseCOMMAND() {
        Node node = new Node(nodeId++, "COMMAND");
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
                        node.addChild(parseATOMIC());
                        System.out.println("Hello there");
                        System.out.println(currentToken.getValue());
                        break;
                    case "input":
                        node.addChild(new Node(nodeId++, "input"));
                        expect(TokenType.RESERVED_KEYWORD, "input");
                        node.addChild(parseVNAME());
                        break;
                    case "if":
                        node.addChild(parseBRANCH());
                        break;
                    default:
                        throw new RuntimeException("Unexpected token: " + currentToken);
                }
            case VARIABLE:
                node.addChild(parseASSIGN());
                break;
            case FUNCTION:
                node.addChild(parseCALL());
                break;
            default:
                throw new RuntimeException("Unexpected token: " + currentToken);
        }
        System.out.println("Parsed command: " + node.getSymbol());
        return node;
    }

    private Node parseATOMIC() {
        Node node = new Node(nodeId++, "ATOMIC");
        if (currentToken.getType() == TokenType.VARIABLE) {
            node.addChild(parseVNAME());
        } else if (currentToken.getType() == TokenType.RESERVED_KEYWORD && currentToken.getValue().equalsIgnoreCase("const")) {
            node.addChild(parseCONST());
        }
        return node;
    }

    private Node parseCONST() {
        Node node = new Node(nodeId++, "CONST");
        node.addChild(new Node(nodeId++, currentToken.getValue()));
        expect(TokenType.RESERVED_KEYWORD, "const");
        return node;
    }

    private Node parseASSIGN() {
        Node node = new Node(nodeId++, "ASSIGN");
        node.addChild(parseVNAME());
        if (currentToken.getType() == TokenType.RESERVED_KEYWORD && currentToken.getValue().equalsIgnoreCase("< input")) {
            node.addChild(new Node(nodeId++, "< input"));
            expect(TokenType.RESERVED_KEYWORD, "< input");
        } else if (currentToken.getType() == TokenType.RESERVED_KEYWORD && currentToken.getValue().equalsIgnoreCase("=")) {
            node.addChild(new Node(nodeId++, "="));
            expect(TokenType.RESERVED_KEYWORD, "=");
            node.addChild(parseTERM());
        }
        return node;
    }

    private Node parseCALL() {
        Node node = new Node(nodeId++, "CALL");
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
        expect(TokenType.RESERVED_KEYWORD, "else");
        node.addChild(new Node(nodeId++, "else"));
        node.addChild(parseALGO());
        return node;
    }

    private Node parseTERM() {
        Node node = new Node(nodeId++, "TERM");
        if (currentToken.getType() == TokenType.VARIABLE || (currentToken.getType() == TokenType.RESERVED_KEYWORD && currentToken.getValue().equalsIgnoreCase("const"))) {
            node.addChild(parseATOMIC());
        } else {
            node.addChild(parseCALL());
        }
        return node;
    }

    private Node parseCOND() {
        Node node = new Node(nodeId++, "COND");
        node.addChild(parseSIMPLE());
        return node;
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
                throw new RuntimeException("Unexpected token: " + currentToken);
        }
        return node;
    }

    private Node parseFNAME() {
        Node node = new Node(nodeId++, "FNAME");
        node.addChild(new Node(nodeId++, currentToken.getValue()));
        expect(TokenType.FUNCTION);
        return node;
    }

    private Node parseFUNCTIONS() {
        Node node = new Node(nodeId++, "FUNCTIONS");
        if (currentToken == null) {
            return node; // Nullable
        }
        node.addChild(parseDECL());
        node.addChild(parseFUNCTIONS());
        return node;
    }

    private Node parseDECL() {
        Node node = new Node(nodeId++, "DECL");
        node.addChild(parseHEADER());
        node.addChild(parseBODY());
        return node;
    }

    private Node parseHEADER() {
        Node node = new Node(nodeId++, "HEADER");
        node.addChild(parseFTYP());
        node.addChild(parseFNAME());
        expect(TokenType.RESERVED_KEYWORD, "(");
        node.addChild(parseVNAME());
        expect(TokenType.RESERVED_KEYWORD, ",");
        node.addChild(parseVNAME());
        expect(TokenType.RESERVED_KEYWORD, ",");
        node.addChild(parseVNAME());
        expect(TokenType.RESERVED_KEYWORD, ")");
        return node;
    }

    private Node parseFTYP() {
        Node node = new Node(nodeId++, "FTYP");
        if (currentToken.getType() == TokenType.NUMBER) {
            node.addChild(new Node(nodeId++, currentToken.getValue()));
            expect(TokenType.NUMBER);
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
        node.addChild(parseALGO());
        expect(TokenType.RESERVED_KEYWORD, "}");
        node.addChild(new Node(nodeId++, "}"));
        node.addChild(parseSUBFUNCS());
        expect(TokenType.RESERVED_KEYWORD, "end");
        node.addChild(new Node(nodeId++, "end"));
        return node;
    }

    private Node parseLOCVARS() {
        Node node = new Node(nodeId++, "LOCVARS");
        while (currentToken != null && ((currentToken.getType() == TokenType.NUMBER) || currentToken.getType() == TokenType.TEXT)) {
            node.addChild(parseVTYP());
            node.addChild(parseVNAME());
            expect(TokenType.RESERVED_KEYWORD, ",");
        }
        return node;
    }

    private Node parseSUBFUNCS() {
        Node node = new Node(nodeId++, "SUBFUNCS");
        node.addChild(parseFUNCTIONS());
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

    private void performSemanticAnalysis() throws SemanticException {
        SemanticAnalyzer analyzer = new SemanticAnalyzer(syntaxTree);
        analyzer.analyze();
    }

    public SyntaxTree getSyntaxTree() {
        return syntaxTree;
    }

    public void displayTokens() {
        for (Token token : tokens) {
            System.out.println("Type: " + token.getType() + ", Value: " + token.getValue());
        }
    }

    public void generateSyntaxTreeXML(String outputFilePath) throws Exception {
        syntaxTree.generateXML(outputFilePath);
    }
}