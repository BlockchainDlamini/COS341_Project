import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.*;
import javax.xml.transform.stream.*;
import org.w3c.dom.*;
import java.io.*;
import java.util.*;

public class Parser {
    private Document inputDoc;
    private Document outputDoc;
    private Element root;
    private Element innernodes;
    private Element leafnodes;
    private int currentTokenIndex = 0;
    private int uniqueNodeId = 1;
    private List<Element> tokens;

    public Parser(String inputFile) {
        try {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            inputDoc = dBuilder.parse(new File(inputFile));
            inputDoc.getDocumentElement().normalize();

            NodeList tokenList = inputDoc.getElementsByTagName("TOK");
            tokens = new ArrayList<>();
            for (int i = 0; i < tokenList.getLength(); i++) {
                tokens.add((Element) tokenList.item(i));
            }

            DocumentBuilderFactory outputFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder outputBuilder = outputFactory.newDocumentBuilder();
            outputDoc = outputBuilder.newDocument();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void parse() {
        try {
            Element syntree = outputDoc.createElement("SYNTREE");
            outputDoc.appendChild(syntree);

            root = outputDoc.createElement("ROOT");
            syntree.appendChild(root);

            innernodes = outputDoc.createElement("INNERNODES");
            syntree.appendChild(innernodes);

            leafnodes = outputDoc.createElement("LEAFNODES");
            syntree.appendChild(leafnodes);

            Element prog = parsePROG();
            if (prog != null && currentTokenIndex == tokens.size()) {
                root.appendChild(createUNID(uniqueNodeId++));
                root.appendChild(createElementWithTextContent("SYMB", "PROG"));
                Element children = outputDoc.createElement("CHILDREN");
                children.appendChild(createElementWithTextContent("ID", prog.getElementsByTagName("UNID").item(0).getTextContent()));
                root.appendChild(children);

                saveOutputXml("syntax_tree.xml");
                System.out.println("Parsing completed successfully. Syntax tree saved in syntax_tree.xml");
            } else {
                System.out.println("Syntax error!");
            }
        } catch (Exception e) {
            System.out.println("Syntax error: " + e.getMessage());
        }
    }

    // Implement other parsing methods for each non-terminal in the grammar
    // For example: parseGLOBVARS(), parseALGO(), parseFUNCTIONS(), etc.

    private Element parseTerminal(String expected) {
        if (currentTokenIndex < tokens.size()) {
            Element token = tokens.get(currentTokenIndex);
            String word = token.getElementsByTagName("WORD").item(0).getTextContent();
            if (word.equals(expected)) {
                currentTokenIndex++;
                Element leaf = createLeafNode(token);
                return leaf;
            }
        }
        throw new RuntimeException("Expected '" + expected + "' but found " + getCurrentTokenWord());
    }

    private Element createInnerNode(String symbol) {
        Element in = outputDoc.createElement("IN");
        in.appendChild(createUNID(uniqueNodeId++));
        in.appendChild(createElementWithTextContent("SYMB", symbol));
        in.appendChild(outputDoc.createElement("CHILDREN"));
        innernodes.appendChild(in);
        return in;
    }

    private Element createLeafNode(Element token) {
        Element leaf = outputDoc.createElement("LEAF");
        leaf.appendChild(createUNID(uniqueNodeId++));
        Element terminal = outputDoc.createElement("TERMINAL");
        terminal.appendChild(outputDoc.adoptNode(token.cloneNode(true)));
        leaf.appendChild(terminal);
        leafnodes.appendChild(leaf);
        return leaf;
    }

    private Element createUNID(int id) {
        return createElementWithTextContent("UNID", String.valueOf(id));
    }

    private Element createElementWithTextContent(String tagName, String content) {
        Element element = outputDoc.createElement(tagName);
        element.setTextContent(content);
        return element;
    }

    private String getCurrentTokenWord() {
        if (currentTokenIndex < tokens.size()) {
            return tokens.get(currentTokenIndex).getElementsByTagName("WORD").item(0).getTextContent();
        }
        return "end of input";
    }

    private void saveOutputXml(String fileName) {
        try {
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
            DOMSource source = new DOMSource(outputDoc);
            StreamResult result = new StreamResult(new File(fileName));
            transformer.transform(source, result);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Element parsePROG() {
        Element prog = createInnerNode("PROG");
        prog.appendChild(parseTerminal("main"));
        prog.appendChild(parseGLOBVARS());
        prog.appendChild(parseALGO());
        prog.appendChild(parseFUNCTIONS());
        return prog;
    }

    private Element parseGLOBVARS() {
        Element globvars = createInnerNode("GLOBVARS");
        while (isVTYP(getCurrentTokenWord())) {
            globvars.appendChild(parseVTYP());
            globvars.appendChild(parseVNAME());
            globvars.appendChild(parseTerminal(","));
        }
        return globvars;
    }

    private boolean isVTYP(String word) {
        return word.equals("num") || word.equals("text");
    }

    private Element parseVTYP() {
        Element vtyp = createInnerNode("VTYP");
        if (getCurrentTokenWord().equals("num")) {
            vtyp.appendChild(parseTerminal("num"));
        } else if (getCurrentTokenWord().equals("text")) {
            vtyp.appendChild(parseTerminal("text"));
        } else {
            throw new RuntimeException("Expected 'num' or 'text' but found " + getCurrentTokenWord());
        }
        return vtyp;
    }

    private Element parseVNAME() {
        Element vname = createInnerNode("VNAME");
        if (getCurrentTokenWord().startsWith("V_")) {
            vname.appendChild(parseTerminal(getCurrentTokenWord()));
        } else {
            throw new RuntimeException("Expected variable name but found " + getCurrentTokenWord());
        }
        return vname;
    }

    private Element parseALGO() {
        Element algo = createInnerNode("ALGO");
        algo.appendChild(parseTerminal("begin"));
        algo.appendChild(parseINSTRUC());
        algo.appendChild(parseTerminal("end"));
        return algo;
    }

    private Element parseINSTRUC() {
        Element instruc = createInnerNode("INSTRUC");
        while (!getCurrentTokenWord().equals("end")) {
            instruc.appendChild(parseCOMMAND());
            instruc.appendChild(parseTerminal(";"));
        }
        return instruc;
    }

    private Element parseCOMMAND() {
        Element command = createInnerNode("COMMAND");
        switch (getCurrentTokenWord()) {
            case "skip":
                command.appendChild(parseTerminal("skip"));
                break;
            case "halt":
                command.appendChild(parseTerminal("halt"));
                break;
            case "print":
                command.appendChild(parseTerminal("print"));
                command.appendChild(parseATOMIC());
                break;
            case "return":
                command.appendChild(parseTerminal("return"));
                command.appendChild(parseATOMIC());
                break;
            default:
                if (getCurrentTokenWord().startsWith("V_")) {
                    command.appendChild(parseASSIGN());
                } else if (getCurrentTokenWord().startsWith("F_")) {
                    command.appendChild(parseCALL());
                } else if (getCurrentTokenWord().equals("if")) {
                    command.appendChild(parseBRANCH());
                } else {
                    throw new RuntimeException("Unexpected token in COMMAND: " + getCurrentTokenWord());
                }
        }
        return command;
    }

    private Element parseATOMIC() {
        Element atomic = createInnerNode("ATOMIC");
        if (getCurrentTokenWord().startsWith("V_")) {
            atomic.appendChild(parseVNAME());
        } else {
            atomic.appendChild(parseCONST());
        }
        return atomic;
    }

    private Element parseCONST() {
        Element const_ = createInnerNode("CONST");
        if (getCurrentTokenWord().matches("-?\\d+(\\.\\d+)?")) {
            const_.appendChild(parseTerminal(getCurrentTokenWord())); // Number
        } else if (getCurrentTokenWord().startsWith("\"") && getCurrentTokenWord().endsWith("\"")) {
            const_.appendChild(parseTerminal(getCurrentTokenWord())); // Text
        } else {
            throw new RuntimeException("Expected CONST but found " + getCurrentTokenWord());
        }
        return const_;
    }

    private Element parseASSIGN() {
        Element assign = createInnerNode("ASSIGN");
        assign.appendChild(parseVNAME());
        if (getCurrentTokenWord().equals("<")) {
            assign.appendChild(parseTerminal("<"));
            assign.appendChild(parseTerminal("input"));
        } else if (getCurrentTokenWord().equals("=")) {
            assign.appendChild(parseTerminal("="));
            assign.appendChild(parseTERM());
        } else {
            throw new RuntimeException("Expected '<' or '=' in ASSIGN but found " + getCurrentTokenWord());
        }
        return assign;
    }

    private Element parseCALL() {
        Element call = createInnerNode("CALL");
        call.appendChild(parseFNAME());
        call.appendChild(parseTerminal("("));
        call.appendChild(parseATOMIC());
        call.appendChild(parseTerminal(","));
        call.appendChild(parseATOMIC());
        call.appendChild(parseTerminal(","));
        call.appendChild(parseATOMIC());
        call.appendChild(parseTerminal(")"));
        return call;
    }

    private Element parseBRANCH() {
        Element branch = createInnerNode("BRANCH");
        branch.appendChild(parseTerminal("if"));
        branch.appendChild(parseCOND());
        branch.appendChild(parseTerminal("then"));
        branch.appendChild(parseALGO());
        branch.appendChild(parseTerminal("else"));
        branch.appendChild(parseALGO());
        return branch;
    }

    private Element parseTERM() {
        Element term = createInnerNode("TERM");
        if (getCurrentTokenWord().startsWith("V_") || getCurrentTokenWord().matches("-?\\d+(\\.\\d+)?") || getCurrentTokenWord().startsWith("\"")) {
            term.appendChild(parseATOMIC());
        } else if (getCurrentTokenWord().startsWith("F_")) {
            term.appendChild(parseCALL());
        } else {
            term.appendChild(parseOP());
        }
        return term;
    }

    private Element parseOP() {
        Element op = createInnerNode("OP");
        if (isUNOP(getCurrentTokenWord())) {
            op.appendChild(parseUNOP());
            op.appendChild(parseTerminal("("));
            op.appendChild(parseARG());
            op.appendChild(parseTerminal(")"));
        } else if (isBINOP(getCurrentTokenWord())) {
            op.appendChild(parseBINOP());
            op.appendChild(parseTerminal("("));
            op.appendChild(parseARG());
            op.appendChild(parseTerminal(","));
            op.appendChild(parseARG());
            op.appendChild(parseTerminal(")"));
        } else {
            throw new RuntimeException("Expected UNOP or BINOP but found " + getCurrentTokenWord());
        }
        return op;
    }

    private Element parseARG() {
        Element arg = createInnerNode("ARG");
        if (getCurrentTokenWord().startsWith("V_") || getCurrentTokenWord().matches("-?\\d+(\\.\\d+)?") || getCurrentTokenWord().startsWith("\"")) {
            arg.appendChild(parseATOMIC());
        } else {
            arg.appendChild(parseOP());
        }
        return arg;
    }

    private Element parseCOND() {
        Element cond = createInnerNode("COND");
        if (isBINOP(getCurrentTokenWord())) {
            cond.appendChild(parseSIMPLE());
        } else if (getCurrentTokenWord().equals("not") || getCurrentTokenWord().equals("sqrt")) {
            cond.appendChild(parseCOMPOSIT());
        } else {
            throw new RuntimeException("Expected BINOP, 'not', or 'sqrt' in COND but found " + getCurrentTokenWord());
        }
        return cond;
    }

    private Element parseSIMPLE() {
        Element simple = createInnerNode("SIMPLE");
        simple.appendChild(parseBINOP());
        simple.appendChild(parseTerminal("("));
        simple.appendChild(parseATOMIC());
        simple.appendChild(parseTerminal(","));
        simple.appendChild(parseATOMIC());
        simple.appendChild(parseTerminal(")"));
        return simple;
    }

    private Element parseCOMPOSIT() {
        Element composit = createInnerNode("COMPOSIT");
        if (getCurrentTokenWord().equals("not") || getCurrentTokenWord().equals("sqrt")) {
            composit.appendChild(parseUNOP());
            composit.appendChild(parseTerminal("("));
            composit.appendChild(parseSIMPLE());
            composit.appendChild(parseTerminal(")"));
        } else {
            composit.appendChild(parseBINOP());
            composit.appendChild(parseTerminal("("));
            composit.appendChild(parseSIMPLE());
            composit.appendChild(parseTerminal(","));
            composit.appendChild(parseSIMPLE());
            composit.appendChild(parseTerminal(")"));
        }
        return composit;
    }

    private Element parseUNOP() {
        Element unop = createInnerNode("UNOP");
        if (getCurrentTokenWord().equals("not") || getCurrentTokenWord().equals("sqrt")) {
            unop.appendChild(parseTerminal(getCurrentTokenWord()));
        } else {
            throw new RuntimeException("Expected 'not' or 'sqrt' but found " + getCurrentTokenWord());
        }
        return unop;
    }

    private Element parseBINOP() {
        Element binop = createInnerNode("BINOP");
        if (isBINOP(getCurrentTokenWord())) {
            binop.appendChild(parseTerminal(getCurrentTokenWord()));
        } else {
            throw new RuntimeException("Expected BINOP but found " + getCurrentTokenWord());
        }
        return binop;
    }

    private boolean isUNOP(String op) {
        return op.equals("not") || op.equals("sqrt");
    }

    private boolean isBINOP(String op) {
        return op.equals("or") || op.equals("and") || op.equals("eq") || op.equals("grt") ||
                op.equals("add") || op.equals("sub") || op.equals("mul") || op.equals("div");
    }

    private Element parseFNAME() {
        Element fname = createInnerNode("FNAME");
        if (getCurrentTokenWord().startsWith("F_")) {
            fname.appendChild(parseTerminal(getCurrentTokenWord()));
        } else {
            throw new RuntimeException("Expected function name but found " + getCurrentTokenWord());
        }
        return fname;
    }

    private Element parseFUNCTIONS() {
        Element functions = createInnerNode("FUNCTIONS");
        while (isVTYP(getCurrentTokenWord()) || getCurrentTokenWord().equals("void")) {
            functions.appendChild(parseDECL());
        }
        return functions;
    }

    private Element parseDECL() {
        Element decl = createInnerNode("DECL");
        decl.appendChild(parseHEADER());
        decl.appendChild(parseBODY());
        return decl;
    }

    private Element parseHEADER() {
        Element header = createInnerNode("HEADER");
        header.appendChild(parseFTYP());
        header.appendChild(parseFNAME());
        header.appendChild(parseTerminal("("));
        header.appendChild(parseVNAME());
        header.appendChild(parseTerminal(","));
        header.appendChild(parseVNAME());
        header.appendChild(parseTerminal(","));
        header.appendChild(parseVNAME());
        header.appendChild(parseTerminal(")"));
        return header;
    }

    private Element parseFTYP() {
        Element ftyp = createInnerNode("FTYP");
        if (getCurrentTokenWord().equals("num") || getCurrentTokenWord().equals("void")) {
            ftyp.appendChild(parseTerminal(getCurrentTokenWord()));
        } else {
            throw new RuntimeException("Expected 'num' or 'void' but found " + getCurrentTokenWord());
        }
        return ftyp;
    }

    private Element parseBODY() {
        Element body = createInnerNode("BODY");
        body.appendChild(parsePROLOG());
        body.appendChild(parseLOCVARS());
        body.appendChild(parseALGO());
        body.appendChild(parseEPILOG());
        body.appendChild(parseSUBFUNCS());
        body.appendChild(parseTerminal("end"));
        return body;
    }

    private Element parsePROLOG() {
        return parseTerminal("{");
    }

    private Element parseEPILOG() {
        return parseTerminal("}");
    }

    private Element parseLOCVARS() {
        Element locvars = createInnerNode("LOCVARS");
        for (int i = 0; i < 3; i++) {
            locvars.appendChild(parseVTYP());
            locvars.appendChild(parseVNAME());
            locvars.appendChild(parseTerminal(","));
        }
        return locvars;
    }

    private Element parseSUBFUNCS() {
        return parseFUNCTIONS();
    }

    public static void main(String[] args) {
        Parser parser = new Parser("output.xml");
        parser.parse();
    }
}