import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.io.BufferedWriter;
import java.io.FileWriter;

public class Translator {
    private SymbolTab symbolTable;  // Manages the variable renaming across scopes
    private List<String> code;        // Stores the translated code
    private int labelCounter;         // For generating unique labels

    public Translator() {
        this.symbolTable = new SymbolTab(); // Initialize the symbol table
        this.code = new ArrayList<>();        // Initialize the code list
        this.labelCounter = 0;                // Initialize label counter
    }

    // Generates a new unique label
    private String newLabel() {
        labelCounter++;
        return "L" + labelCounter;
    }

    // Entry method to process the entire input program from a file
    public String translate(String filename) {
        StringBuilder inputProgram = new StringBuilder();

        // Read the file content into a string
        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            String line;
            while ((line = reader.readLine()) != null) {
                inputProgram.append(line).append("\n");
            }
        } catch (IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
            return "";
        }

        // Split the program into lines
        String[] lines = inputProgram.toString().trim().split("\n");

        // Process the main program and functions
        processMain(lines);

        // Return the generated intermediate code as a string
        return String.join("\n", code);
    }

    // Processes the main part of the program (main, begin, end are ignored)
    private void processMain(String[] lines) {
        boolean inFunction = false;
        List<String> functionCode = new ArrayList<>();
        symbolTable.enterScope("global");  // Enter the global scope

        for (String line : lines) {
            line = line.trim();

            // If entering a function scope
            if (line.startsWith("num F_")) {
                inFunction = true;
                functionCode.clear();
                functionCode.add(line); // Start collecting function code
            } else if (line.equals("end")) {
                if (inFunction) {
                    processFunction(functionCode);
                    inFunction = false;
                } else {
                    code.add("STOP"); // Add stop after main program
                }
            } else if (inFunction) {
                functionCode.add(line); // Collect function lines
            } else {
                processStatement(line); // Process regular statements
            }
        }

        symbolTable.exitScope();  // Exit the global scope
    }

    // Processes a function by translating its content
    private void processFunction(List<String> functionLines) {
        String functionName = functionLines.get(0).split(" ")[1]; // Extract function name
        String translatedCode = functionName;
        code.add(translatedCode);

        // Enter a new scope for the function
        symbolTable.enterScope(functionName);

        for (int i = 1; i < functionLines.size(); i++) {
            String line = functionLines.get(i).trim();
            processStatement(line);
        }

        code.add("End Function");

        // Exit the function scope after processing
        symbolTable.exitScope();
    }

    // Processes individual statements in the main program or a function
    private void processStatement(String line) {
        if (line.contains("=") && !line.contains("return")) {
            processAssignment(line);
        } else if (line.startsWith("print")) {
            processPrint(line);
        } else if (line.startsWith("if")) {
            processIfStatement(line);
        } else if (line.startsWith("return")) {
            processReturn(line);
        }
    }

    // Processes an assignment statement
    private void processAssignment(String line) {
        String[] parts = line.split("=");
        String varName = parts[0].trim();
        String expr = parts[1].trim();

        String translatedVar = symbolTable.getOrCreate(varName); // Get or create a new identifier for the variable

        if (expr.contains("(")) {
            processFunctionCall(translatedVar, expr);
        } else {
            code.add(translatedVar + " := " + expr);
        }
    }

    // Processes a function call assignment like V_result1 = add(V_a, V_b)
    private void processFunctionCall(String varName, String expr) {
        String functionName = expr.substring(0, expr.indexOf("("));
        String params = expr.substring(expr.indexOf("(") + 1, expr.indexOf(")"));
        String[] arguments = params.split(",");

        String translatedCall = "CALL_" + functionName + "(";
        for (int i = 0; i < arguments.length; i++) {
            String arg = arguments[i].trim();
            translatedCall += symbolTable.getOrCreate(arg); // Translate function arguments
            if (i < arguments.length - 1) {
                translatedCall += ", ";
            }
        }
        translatedCall += ")";

        code.add(varName + " := " + translatedCall);
    }

    // Processes a print statement
    private void processPrint(String line) {
        String var = line.substring(line.indexOf("print") + 6).trim();
        String translatedVar = symbolTable.getOrCreate(var);
        code.add("PRINT " + translatedVar);
    }

    // Processes an if statement
    private void processIfStatement(String line) {
        String condition = line.substring(line.indexOf("if") + 3, line.indexOf("then")).trim();
        String labelTrue = newLabel();
        String labelFalse = newLabel();

        processCondition(condition, labelTrue, labelFalse);
        code.add("LABEL " + labelTrue);
    }

    // Processes return statements
    private void processReturn(String line) {
        String returnVar = line.substring(line.indexOf("return") + 7).trim();
        String translatedVar = symbolTable.getOrCreate(returnVar);
        code.add("RETURN " + translatedVar);
    }

    // Processes conditions (for if and loops)
    private void processCondition(String condition, String labelTrue, String labelFalse) {
        condition = condition.trim();

        // Check if the condition is in the form of a function-style operation like grt(arg1, arg2)
        if (condition.matches("\\w+\\(.*\\)")) {
            // Extract the operator and arguments (e.g., grt(V_innerresult, V_c))
            String operator = condition.substring(0, condition.indexOf('(')); // e.g., "grt"
            String args = condition.substring(condition.indexOf('(') + 1, condition.lastIndexOf(')')); // e.g., "V_innerresult, V_c"

            String[] argParts = args.split(","); // Split arguments by comma

            if (argParts.length == 2) {
                // Handle binary operators (like grt, eq, add, etc.)
                String left = symbolTable.getOrCreate(argParts[0].trim());
                String right = symbolTable.getOrCreate(argParts[1].trim());

                // Translate the operator (e.g., grt -> >)
                String translatedOp = translateOp(operator);

                // Add translated condition to the code
                code.add("IF " + left + " " + translatedOp + " " + right + " THEN GOTO " + labelTrue + " ELSE GOTO " + labelFalse);
                code.add("LABEL " + labelFalse);

            } else if (argParts.length == 1) {
                // Handle unary operators if needed (e.g., not(arg1))
                String operand = symbolTable.getOrCreate(argParts[0].trim());

                if (operator.equals("not")) {
                    // Swap labels for negation
                    code.add("IF " + operand + " = 0 THEN GOTO " + labelTrue + " ELSE GOTO " + labelFalse);
                    code.add("LABEL " + labelFalse);
                } else {
                    // Other unary operators (e.g., sqrt)
                    String translatedOp = translateOp(operator);
                    code.add("IF " + translatedOp + "(" + operand + ") THEN GOTO " + labelTrue + " ELSE GOTO " + labelFalse);
                    code.add("LABEL " + labelFalse);
                }
            }
        }
    }

    // Translates operators from source language to target language
    private String translateOp(String op) {
        // Check if the operator is in function-like syntax
        if (op.matches("\\w+\\(.*\\)")) {
            String functionName = op.substring(0, op.indexOf('(')); // Extract the function name
            String params = op.substring(op.indexOf('(') + 1, op.lastIndexOf(')')); // Extract parameters
            String[] arguments = params.split(","); // Split parameters by comma

            // Handle based on the function name
            switch (functionName) {
                case "grt":
                    // Expecting 2 parameters
                    if (arguments.length == 2) {
                        return symbolTable.getOrCreate(arguments[0].trim()) + " > " + symbolTable.getOrCreate(arguments[1].trim());
                    }
                    break;
                case "eq":
                    // Expecting 2 parameters
                    if (arguments.length == 2) {
                        return symbolTable.getOrCreate(arguments[0].trim()) + " = " + symbolTable.getOrCreate(arguments[1].trim());
                    }
                    break;
                case "add":
                    // Expecting 2 parameters
                    if (arguments.length == 2) {
                        return symbolTable.getOrCreate(arguments[0].trim()) + " + " + symbolTable.getOrCreate(arguments[1].trim());
                    }
                    break;
                case "sub":
                    // Expecting 2 parameters
                    if (arguments.length == 2) {
                        return symbolTable.getOrCreate(arguments[0].trim()) + " - " + symbolTable.getOrCreate(arguments[1].trim());
                    }
                    break;
                case "mul":
                    // Expecting 2 parameters
                    if (arguments.length == 2) {
                        return symbolTable.getOrCreate(arguments[0].trim()) + " * " + symbolTable.getOrCreate(arguments[1].trim());
                    }
                    break;
                case "div":
                    // Expecting 2 parameters
                    if (arguments.length == 2) {
                        return symbolTable.getOrCreate(arguments[0].trim()) + " / " + symbolTable.getOrCreate(arguments[1].trim());
                    }
                    break;
                // Add more operators here if needed
                default:
                    return op; // If the operator is unknown, return as is
            }
        } else {
            // Handle simple operators without parentheses
            switch (op) {
                case "grt":
                    return ">";
                case "eq":
                    return "=";
                case "add":
                    return "+";
                case "sub":
                    return "-";
                case "mul":
                    return "*";
                case "div":
                    return "/";
                default:
                    return op; // Default to the operator itself if not found
            }
        }
        return "";
    }

    public void writeTranslatedCodeToFile(String filename) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename))) {
            for (String line : code) {
                writer.write(line);
                writer.newLine();
            }
        } catch (IOException e) {
            System.err.println("Error writing to file: " + e.getMessage());
        }
    }

    // Symbol Table Class for managing scope-based variable renaming
    private static class SymbolTab {
        private Map<String, Deque<String>> scopes;  // Maps variable names to their unique identifiers
        private int varCounter;                     // Unique variable ID counter
        private Deque<String> scopeStack;           // Stack to manage scope names

        public SymbolTab() {
            this.scopes = new HashMap<>();
            this.varCounter = 0;
            this.scopeStack = new ArrayDeque<>();
        }

        // Enter a new scope
        public void enterScope(String scopeName) {
            scopeStack.push(scopeName);
        }

        // Exit the current scope
        public void exitScope() {
            scopeStack.pop();
        }

        // Get or create a new identifier for a variable
        public String getOrCreate(String varName) {
            String currentScope = scopeStack.peek(); // Current scope
            String fullName = currentScope + "_" + varName; // Full name with scope

            // Check if the variable exists in the current scope
            if (!scopes.containsKey(fullName)) {
                varCounter++;
                scopes.put(fullName, new ArrayDeque<>(List.of("V" + varCounter))); // Create a new variable identifier
            }

            // Return the current variable identifier
            return scopes.get(fullName).peek();
        }
    }
}