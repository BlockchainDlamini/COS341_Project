import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SyntaxTree {
    private Node root;
    private List<InnerNode> innerNodes;
    private List<LeafNode> leafNodes;

    public SyntaxTree(List<Token> tokens) {
        this.innerNodes = new ArrayList<>();
        this.leafNodes = new ArrayList<>();
        buildSyntaxTree(tokens);
    }

    public void setRoot(Node root) {
        this.root = root;
    }

    public void addInnerNode(InnerNode node) {
        innerNodes.add(node);
    }

    public void addLeafNode(LeafNode node) {
        leafNodes.add(node);
    }

    private void buildSyntaxTree(List<Token> tokens) {
        if (tokens.isEmpty()) {
            throw new IllegalStateException("Token list is empty");
        }

        // Recursive descent algorithm to build the syntax tree
        int[] index = {0};
        root = parseProgram(tokens, index);
    }

    private Node parseProgram(List<Token> tokens, int[] index) {
        Node rootNode = new Node(1, "main");
        setRoot(rootNode);
        index[0]++; // Skip 'main'

        Node currentNode = rootNode;
        int nodeId = 2;

        while (index[0] < tokens.size()) {
            Token token = tokens.get(index[0]);
            if (token.getType() == TokenType.RESERVED_KEYWORD) {
                InnerNode innerNode = new InnerNode(nodeId++, token.getValue(), currentNode);
                currentNode.addChild(innerNode);
                addInnerNode(innerNode);
                currentNode = innerNode;
                index[0]++;
            } else {
                LeafNode leafNode = new LeafNode(nodeId++, token, currentNode);
                currentNode.addChild(leafNode);
                addLeafNode(leafNode);
                index[0]++;
            }
        }

        return rootNode;
    }

    public void generateXML(String outputFilePath) throws IOException {
        try (FileWriter writer = new FileWriter(outputFilePath)) {
            writer.write("<SYNTREE>\n");
            writer.write("  <ROOT>\n");
            writer.write("    <UNID>" + root.getUnid() + "</UNID>\n");
            writer.write("    <SYMB>" + root.getSymbol() + "</SYMB>\n");
            writer.write("    <CHILDREN>\n");
            for (Node child : root.getChildren()) {
                writer.write("      <ID>" + child.getUnid() + "</ID>\n");
            }
            writer.write("    </CHILDREN>\n");
            writer.write("  </ROOT>\n");

            writer.write("  <INNERNODES>\n");
            for (InnerNode node : innerNodes) {
                writer.write("    <IN>\n");
                writer.write("      <PARENT>" + node.getParent().getUnid() + "</PARENT>\n");
                writer.write("      <UNID>" + node.getUnid() + "</UNID>\n");
                writer.write("      <SYMB>" + node.getSymbol() + "</SYMB>\n");
                writer.write("      <CHILDREN>\n");
                for (Node child : node.getChildren()) {
                    writer.write("        <ID>" + child.getUnid() + "</ID>\n");
                }
                writer.write("      </CHILDREN>\n");
                writer.write("    </IN>\n");
            }
            writer.write("  </INNERNODES>\n");

            writer.write("  <LEAFNODES>\n");
            for (LeafNode node : leafNodes) {
                writer.write("    <LEAF>\n");
                writer.write("      <PARENT>" + node.getParent().getUnid() + "</PARENT>\n");
                writer.write("      <UNID>" + node.getUnid() + "</UNID>\n");
                writer.write("      <TERMINAL>\n");
                writer.write("        <CLASS>" + node.getToken().getType() + "</CLASS>\n");
                writer.write("        <WORD>" + node.getToken().getValue() + "</WORD>\n");
                writer.write("      </TERMINAL>\n");
                writer.write("    </LEAF>\n");
            }
            writer.write("  </LEAFNODES>\n");
            writer.write("</SYNTREE>\n");
        }
    }

    public Node getRoot() {
        return root;
    }
}