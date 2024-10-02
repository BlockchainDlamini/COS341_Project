import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        try {
            Lexer lexer = new Lexer();
            String input = Lexer.readFile("src/main/input1.txt");
            lexer.tokenize(input);
            lexer.generateXML("output.xml");
        } catch (IOException | LexicalError e) {
            System.err.println(e.getMessage());
        }
    }
}