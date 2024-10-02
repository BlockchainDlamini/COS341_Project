import org.w3c.dom.*;
import javax.xml.parsers.*;
import java.io.*;
import java.util.*;

public class Parser {
    private List<Token> tokens;
    private SyntaxTree syntaxTree;

    public Parser(String xmlFilePath) throws Exception {
        this.tokens = parseXML(xmlFilePath);
        this.syntaxTree = new SyntaxTree();
        buildSyntaxTree();
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

    private void buildSyntaxTree() {
        // Initialize the root node
        Node rootNode = new Node(1, "startsymbol");
        syntaxTree.setRoot(rootNode);

        // Example of adding inner nodes and leaf nodes
        Node currentNode = rootNode;
        int nodeId = 2;

        for (Token token : tokens) {
            if (token.getType() == TokenType.RESERVED_KEYWORD) {
                InnerNode innerNode = new InnerNode(nodeId++, token.getValue(), currentNode);
                currentNode.addChild(innerNode);
                syntaxTree.addInnerNode(innerNode);
                currentNode = innerNode;
            } else {
                LeafNode leafNode = new LeafNode(nodeId++, token, currentNode);
                currentNode.addChild(leafNode);
                syntaxTree.addLeafNode(leafNode);
            }
        }
    }

    public SyntaxTree getSyntaxTree() {
        return syntaxTree;
    }

    public void generateSyntaxTreeXML(String outputFilePath) throws IOException {
        syntaxTree.generateXML(outputFilePath);
    }
}