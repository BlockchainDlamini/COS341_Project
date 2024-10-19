import java.util.ArrayList;
import java.util.List;

public class Node {
    public int unid;
    public String symbol;
    public List<Node> children;

    public Node(int unid, String symbol) {
        this.unid = unid;
        this.symbol = symbol;
        this.children = new ArrayList<>();
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
    }

    public String getName() {
        return symbol;
    }
}