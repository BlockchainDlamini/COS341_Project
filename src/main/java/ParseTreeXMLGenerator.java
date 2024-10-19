import java.io.FileWriter;
import java.io.IOException;

public class ParseTreeXMLGenerator {
    private int idCounter = 1;

    public void generateXML(Node root, String filePath) throws IOException {
        FileWriter writer = new FileWriter(filePath);

        writer.write("<SYNTREE>\n");
        writeRootNode(root, writer);
        writer.write("<INNERNODES>\n");
        writeInnerNodes(root, writer);
        writer.write("</INNERNODES>\n<LEAFNODES>\n");
        writeLeafNodes(root, writer);
        writer.write("</LEAFNODES>\n</SYNTREE>");

        writer.close();
    }

    private void writeRootNode(Node node, FileWriter writer) throws IOException {
        writer.write("<ROOT>\n");
        writer.write("<UNID>" + node.unid + "</UNID>\n");
        writer.write("<SYMB>" + node.symbol + "</SYMB>\n");
        writer.write("<CHILDREN>\n");
        for (Node child : node.children) {
            writer.write("<ID>" + child.unid + "</ID>\n");
        }
        writer.write("</CHILDREN>\n</ROOT>\n");
    }

    private void writeInnerNodes(Node node, FileWriter writer) throws IOException {
        if (!node.children.isEmpty()) {
            for (Node child : node.children) {
                if (!child.children.isEmpty()) {
                    writer.write("<IN>\n");
                    writer.write("<PARENT>" + node.unid + "</PARENT>\n");
                    writer.write("<UNID>" + child.unid + "</UNID>\n");
                    writer.write("<SYMB>" + child.symbol + "</SYMB>\n");
                    writer.write("<CHILDREN>\n");
                    for (Node grandchild : child.children) {
                        writer.write("<ID>" + grandchild.unid + "</ID>\n");
                    }
                    writer.write("</CHILDREN>\n</IN>\n");
                    writeInnerNodes(child, writer);
                }
            }
        }
    }

    private void writeLeafNodes(Node node, FileWriter writer) throws IOException {
        if (node.children.isEmpty()) {
            writer.write("<LEAF>\n");
            writer.write("<PARENT>" + node.unid + "</PARENT>\n");
            writer.write("<UNID>" + idCounter++ + "</UNID>\n");
            writer.write("<TERMINAL>" + node.symbol + "</TERMINAL>\n");
            writer.write("</LEAF>\n");
        } else {
            for (Node child : node.children) {
                writeLeafNodes(child, writer);
            }
        }
    }
}