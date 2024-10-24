import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        try {
            Scanner scanner = new Scanner(System.in);
            System.out.println("Enter the filename to be executed: ");

            String filename = scanner.nextLine();

            String jarDir = new File(Main.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getParent();
//
//            // Construct the file path relative to the JAR directory
            File file = new File(jarDir, filename);
//
//            // Check if the file exists
            if (!file.exists()) {
                System.out.println("File not found: " + file.getAbsolutePath());
                return;
            }

            // Read the file using the Lexer class (or any other logic)
            String input = Lexer.readFile(file.getAbsolutePath());

             Lexer.readFile(file.getAbsolutePath());
//            Uncomment the above code and comment the below code to take input from the user
//            String input = Lexer.readFile("src/main/input8.txt");
            Lexer lexer = new Lexer();
            lexer.tokenize(input);
            lexer.generateXML("output.xml");


            Map<String, SymbolInfo> symbolTableMap = null;
            Parser parser = new Parser("output.xml");
            Node parseTree = parser.parse();
            SymbolTable duplicateSymbolTable = new SymbolTable(parser.getSymbolTable());
            symbolTableMap = duplicateSymbolTable.viewSymbolTable();
            symbolTableMap.putAll(parser.getFunctionSymbolTable().display());
//            viewMapSymbolInfo(symbolTableMap);
//            System.out.println();
//            System.out.println();
//            System.out.println();
//            System.out.println();
            ParseTreeXMLGenerator generator = new ParseTreeXMLGenerator();
            try {
                generator.generateParseTreeXML(parseTree, "parse_tree.xml");
            } catch (IOException e) {
                e.printStackTrace();
            }

            TypeChecker typeChecker = new TypeChecker(symbolTableMap);
            if(!typeChecker.typecheck(parseTree))
                throw new Exception("Type checking error");

        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }

    public static void  viewMapSymbolInfo(Map<String, SymbolInfo> mp) {
        for (Map.Entry<String, SymbolInfo> scope : mp.entrySet() ) {
            if(scope.getValue().getName().charAt(0) == 'F') {
                System.out.println("Function: " + scope.getValue().getName() + ", Info: " + scope.getValue().toString());
            }
            else
            {
                System.out.println("Var: " + scope.getValue().getName() + ", Info: " + scope.getValue().toString());
            }
        }
    }
}