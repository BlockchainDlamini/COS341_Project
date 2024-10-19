import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        try {
            Lexer lexer = new Lexer();
            String input = Lexer.readFile("src/main/input2.txt");
            lexer.tokenize(input);
            lexer.generateXML("output.xml");


            Parser parser = new Parser("output.xml");
            Node parseTree = parser.parse();

            ParseTreeXMLGenerator generator = new ParseTreeXMLGenerator();
            try {
                generator.generateXML(parseTree, "parse_tree.xml");
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }
}