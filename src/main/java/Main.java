import java.io.IOException;
import java.util.Map;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        try {
            Scanner scanner = new Scanner(System.in);
            System.out.println("Enter the filename to be executed: ");
            String filename = scanner.nextLine();
            String input = Lexer.readFile("src/main/" + filename + ".txt");
//            Uncomment the above code and comment the below code to take input from the user
//            String input = Lexer.readFile("src/main/input8.txt");
            Lexer lexer = new Lexer();
            lexer.tokenize(input);
            lexer.generateXML("output.xml");

            Parser parser = new Parser("output.xml");
            Node parseTree = parser.parse();
            SymbolTable duplicateSymbolTable = new SymbolTable(parser.getSymbolTable());
            Map<Integer, SymbolInfo> symbolTableMap = duplicateSymbolTable.viewSymbolTable();
            ParseTreeXMLGenerator generator = new ParseTreeXMLGenerator();
            TypeChecker typeChecker = new TypeChecker(symbolTableMap);
            typeChecker.typecheck(parseTree);


            try {
                generator.generateParseTreeXML(parseTree, "parse_tree.xml");
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }
}