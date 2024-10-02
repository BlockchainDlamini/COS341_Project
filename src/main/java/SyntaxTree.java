import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SyntaxTree {
    private Node root;
    private List<InnerNode> innerNodes;
    private List<LeafNode> leafNodes;

    public SyntaxTree() {
        this.innerNodes = new ArrayList<>();
        this.leafNodes = new ArrayList<>();
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
}