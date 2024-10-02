import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        try {
            Lexer lexer = new Lexer();
            String input = Lexer.readFile("src/main/input2.txt");
            lexer.tokenize(input);
            lexer.generateXML("output.xml");

            Parser parser = new Parser("output.xml");
            parser.generateSyntaxTreeXML("syntax_tree.xml");
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }
}