import java.util.ArrayList;
import java.util.List;

public class Node {
    public int unid;
    public String symbol;
    public List<Node> children;
    private Node parent;

    public Node(int unid, String symbol) {
        this.unid = unid;
        this.symbol = symbol;
        this.children = new ArrayList<>();
        this.parent = null;
    }

    public int getUnid() {
        return unid;
    }

    public String getSymbol() {
        return symbol;
    }

    public List<Node> getChildren() {
        return children;
    }

    public void addChild(Node child) {
        children.add(child);
        child.parent = this;
    }

    public String getName() {
        return symbol;
    }

    public Node getParent() {
        return parent;
    }

    public void setParent(Node parent) {
        this.parent = parent;
    }
}