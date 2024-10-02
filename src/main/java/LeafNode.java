public class LeafNode extends Node {
    private Node parent;
    private Token token;

    public LeafNode(int unid, Token token, Node parent) {
        super(unid, token.getType().name());
        this.token = token;
        this.parent = parent;
    }

    public Node getParent() {
        return parent;
    }

    public Token getToken() {
        return token;
    }
}