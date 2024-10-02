public class InnerNode extends Node {
    private Node parent;

    public InnerNode(int unid, String symbol, Node parent) {
        super(unid, symbol);
        this.parent = parent;
    }

    public Node getParent() {
        return parent;
    }
}