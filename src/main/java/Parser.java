import org.w3c.dom.*;
import javax.xml.parsers.*;
import java.io.*;
import java.util.*;

public class Parser {
    private final List<Token> tokens;
    private final SyntaxTree syntaxTree;

    public Parser(String xmlFilePath) throws Exception {
        this.tokens = parseXML(xmlFilePath);
        displayTokens();
        this.syntaxTree = new SyntaxTree(this.tokens);
        performSemanticAnalysis();
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