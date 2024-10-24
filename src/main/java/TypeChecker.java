import java.util.HashMap;
import java.util.Map;

public class TypeChecker {

    public TypeChecker(Map<String, SymbolInfo> symbl)
    {
        symbolTable = symbl;
    }
    // Symbol table to store variable and function types

    private final Map<String, SymbolInfo> symbolTable ; // Make sure symbolTable is initialized

    public boolean typecheck(Node node) {
        switch (node.symbol) {
            case "PROG":
                return typecheckPROG(node);
            case "GLOBVARS":
                return typecheckGLOBVARS(node);
            case "ALGO":
                return typecheckALGO(node);
            case "INSTRUC":
                return typecheckINSTRUC(node);
            case "COMMAND":
                return typecheckCOMMAND(node);
            case "ATOMIC":
                return typecheckATOMIC(node);
            case "ASSIGN":
                return typecheckASSIGN(node);
            case "TERM":
                return typecheckTERM(node);
            case "CALL":
                return typecheckCALL(node);
            case "OP":
                return typecheckOP(node);
            case "ARG":
                return typecheckARG(node);
            case "UNOP":
                return typecheckUNOP(node);
            case "BINOP":
                return typecheckBINOP(node);
            case "BRANCH":
                return typecheckBRANCH(node);
            case "COND":
                return typecheckCOND(node);
            case "SIMPLE":
                return typecheckSIMPLE(node);
            case "COMPOSIT":
                return typecheckCOMPOSIT(node);
            case "FUNCTIONS":
                return typecheckFUNCTIONS(node);
            case "DECL":
                return typecheckDECL(node);
            case "HEADER":
                return typecheckHEADER(node);
            case "BODY":
                return typecheckBODY(node);
            case "PROLOG":
                return typecheckPROLOG(node);
            case "EPILOG":
                return typecheckEPILOG(node);
            case "LOCVARS":
                return typecheckLOCVARS(node);
            case "SUBFUNCS":
                return typecheckSUBFUNCS(node);
            default:
                return false;
        }
    }

    private boolean typecheckPROG(Node node) {
        return typecheck(node.children.get(1)) && typecheck(node.children.get(2)) && typecheck(node.children.get(3));
    }

    private boolean typecheckGLOBVARS(Node node) {
        if (node.children.isEmpty()) {
            return true;
        } else {
            Node vt = node.children.get(0);
            Node vn = node.children.get(1);
            String vtType = typeof(vt);
            String syb = vn.getChildren().getFirst().getSymbol();
            System.out.println(vt.getSymbol() + " --- " +vn.getSymbol());
            symbolTable.get(syb).updateDataType(vtType);
            System.out.println(syb);
            System.out.println(symbolTable.get(syb).getDataType());
            return typecheck(node.children.get(2));
        }
    }

    private boolean typecheckALGO(Node node) {
        return typecheck(node.children.get(1));
    }

    private boolean typecheckINSTRUC(Node node) {
        if (node.children.isEmpty()) {
            return true;
        } else {
            boolean v = typecheck(node.children.get(0)) && typecheck(node.children.get(1));
            System.out.println(v);
            return v;
        }
    }

    private boolean typecheckCOMMAND(Node node) {
        switch (node.children.get(0).symbol) {
            case "skip":
            case "halt":
                return true;
            case "print":
                return typecheck(node.children.get(1)) && ("n".equals(typeof(node.children.get(1))) || "t".equals(typeof(node.children.get(1))));
            case "return":
                // Find the function type node
                Node ftypNode = findFunctionTypeNode(node);
                return typecheck(node.children.get(1)) && typeof(node.children.get(1)).equals(typeof(ftypNode)) && "n".equals(typeof(ftypNode));
            case "ASSIGN":
                return typecheck(node.children.get(0));
            case "CALL":
                return "v".equals(typeof(node.children.get(0)));
            case "BRANCH":
                return typecheck(node.children.get(0));
            default:
                return false;
        }
    }

    private boolean typecheckATOMIC(Node node) {
        System.out.println(node.getSymbol());
        System.out.println(node.children.getFirst().getChildren().getFirst().getSymbol());
        if (node.children.get(0).symbol.matches("VNAME")) {
            return "n".equals(typeof(node.children.get(0))) || "t".equals(typeof(node.children.get(0)));
        } else {
            return "n".equals(typeof(node.children.get(0))) || "t".equals(typeof(node.children.get(0)));
        }
    }

    private boolean typecheckASSIGN(Node node) {
        System.out.println("In typecheckASSIGN");
        Node vname = node.children.get(0);
        Node term = node.children.get(2);
        System.out.println(vname.getSymbol() + " --- " +term.getSymbol());
        System.out.println(typeof(vname));
        System.out.println(typeof(term));
        return typecheck(term) ;
    }

    private boolean typecheckTERM(Node node) {
        return typecheck(node.children.get(0));
    }

    private boolean typecheckCALL(Node node) {
        System.out.println("In typecheckCALL");

        boolean validTypes = typecheck(node.children.get(1)) && "n".equals(typeof(node.children.get(1)))
                && typecheck(node.children.get(2)) && "n".equals(typeof(node.children.get(2)))
                && typecheck(node.children.get(3)) && "n".equals(typeof(node.children.get(3)));
        return validTypes && typeof(node.children.get(0)).equals("n");
    }

    private boolean typecheckOP(Node node) {
        Node firstChild = node.children.get(0);
        if (firstChild.symbol.equals("UNOP")) {
            Node arg = node.children.get(1);
            return typecheck(firstChild) && typecheck(arg) && typeof(firstChild).equals(typeof(arg));
        } else if (firstChild.symbol.equals("BINOP")) {
            Node arg1 = node.children.get(1);
            Node arg2 = node.children.get(2);
            System.out.println("inside typecheckOP");
            System.out.println(firstChild.getSymbol() + " --- " +arg1.getSymbol() + " --- " +arg2.getSymbol());
             return typecheck(firstChild) && typecheck(arg1) && typecheck(arg2)
                    && ((typeof(firstChild).equals(typeof(arg1)) && typeof(arg1).equals(typeof(arg2))) || ("c".equals(typeof(firstChild)) && typeof(arg1).equals(typeof(arg2))));
        }
        return false;
    }

    private boolean typecheckARG(Node node) {
        System.out.println(node.symbol);
        return typecheck(node.children.get(0));
    }

    private boolean typecheckUNOP(Node node) {
        Node child = node.children.get(0);
        return (child.symbol.equals("not") && "b".equals(typeof(child))) ||
                (child.symbol.equals("sqrt") && "n".equals(typeof(child)));
    }

    private boolean typecheckBINOP(Node node) {
//        Node firstChild = node.children.get(0);
        Node firstChild = node;
        System.out.println(firstChild.children.getFirst().getSymbol());
        if (firstChild.children.getFirst().getSymbol().equals("or") || firstChild.children.getFirst().getSymbol().equals("and")) {
            return "b".equals(typeof(firstChild));
        } else if (firstChild.children.getFirst().getSymbol().equals("eq") || firstChild.children.getFirst().getSymbol().equals("grt")) {
            return "c".equals(typeof(firstChild));
        } else {
            return "n".equals(typeof(firstChild));
        }
    }

    private boolean typecheckBRANCH(Node node) {
        Node condNode = node.children.get(0);
        Node algo1 = node.children.get(1);
        Node algo2 = node.children.get(2);
        return typecheck(condNode) && "b".equals(typeof(condNode)) && typecheck(algo1) && typecheck(algo2);
    }

    private boolean typecheckCOND(Node node) {
        if (node.children.get(0).symbol.equals("SIMPLE")) {
            return typecheck(node.children.get(0)) && "b".equals(typeof(node.children.get(0)));
        } else {
            return typecheck(node.children.get(0)) && "b".equals(typeof(node.children.get(0)));
        }
    }

    private boolean typecheckSIMPLE(Node node) {
        Node binop = node.children.get(0);
        Node atomic1 = node.children.get(1);
        Node atomic2 = node.children.get(2);
        boolean typeCheck = typecheck(binop) && typecheck(atomic1) && typecheck(atomic2);
        if ("b".equals(typeof(binop)) && "b".equals(typeof(atomic1)) && "b".equals(typeof(atomic2))) {
            return typeCheck;
        } else if ("c".equals(typeof(binop)) && "n".equals(typeof(atomic1)) && "n".equals(typeof(atomic2))) {
            return typeCheck;
        }
        return false;
    }


        private boolean typecheckCOMPOSIT(Node node) {
        Node firstChild = node.children.get(0);
        if (firstChild.symbol.equals("BINOP")) {
            Node simple1 = node.children.get(1);
            Node simple2 = node.children.get(2);
            return typecheck(firstChild) && typecheck(simple1) && typecheck(simple2)
                    && "b".equals(typeof(firstChild)) && "b".equals(typeof(simple1)) && "b".equals(typeof(simple2));
        } else if (firstChild.symbol.equals("UNOP")) {
            Node simple = node.children.get(1);
            return typecheck(firstChild) && typecheck(simple) && "b".equals(typeof(firstChild)) && "b".equals(typeof(simple));
        }
        return false;
    }

    private boolean typecheckFUNCTIONS(Node node) {
        if (node.children.isEmpty()) {
            return true;
        } else {
            return typecheck(node.children.get(0)) && typecheck(node.children.get(1));
        }
    }

    private boolean typecheckDECL(Node node) {
        return typecheck(node.children.get(0)) && typecheck(node.children.get(1));
    }

    private boolean typecheckHEADER(Node node) {
        Node ftyp = node.children.get(0);
        Node fname = node.children.get(1);
        Node vname1 = node.children.get(2);
        Node vname2 = node.children.get(3);
        Node vname3 = node.children.get(4);

        System.out.println(ftyp.getSymbol());
        System.out.println(fname.getSymbol());
        System.out.println(vname1.getSymbol());
        System.out.println(vname2.getSymbol());
        System.out.println(vname3.getSymbol());
//        vn.getChildren().getFirst().getSymbol();
        String ftypType = typeof(ftyp);
        String fnameId = fname.getChildren().getFirst().getSymbol();
        System.out.println(fnameId);
        symbolTable.get(fnameId).updateDataType(ftypType);
        boolean vnameCheck = "n".equals(typeof(vname1)) && "n".equals(typeof(vname2)) && "n".equals(typeof(vname3));
        System.out.println(vnameCheck);
        return vnameCheck;
    }

    private boolean typecheckBODY(Node node) {
        System.out.println("In typecheckBODY");
        System.out.println(node.children.get(0).getSymbol());
        System.out.println(node.children.get(1).getSymbol());
        System.out.println(node.children.get(2).getSymbol());
        System.out.println(node.children.get(3).getSymbol());
        System.out.println(node.children.get(4).getSymbol());
        System.out.println(node.children.get(5).getSymbol());
        return typecheck(node.children.get(1)) && typecheck(node.children.get(2)) && typecheck(node.children.get(4));
    }

    private boolean typecheckPROLOG(Node node) {
        return true; // Base case
    }

    private boolean typecheckEPILOG(Node node) {
        return true; // Base case
    }

    private boolean typecheckLOCVARS(Node node) {
        Node vt1 = node.children.get(0);
        System.out.println(vt1.getSymbol());
        System.out.println(vt1.children.get(0).getSymbol());

        Node vn1 = node.children.get(1);
        System.out.println(vn1.getSymbol());
        System.out.println(vn1.children.get(0).getSymbol());

        Node vt2 = node.children.get(2).getChildren().get(0);
        System.out.println(vt2.getSymbol());
        System.out.println(vt2.children.get(0).getSymbol());


        Node vn2 = node.children.get(2).getChildren().get(1);
        System.out.println(vn2.getSymbol());
        System.out.println(vn2.children.get(0).getSymbol());


        Node vt3 = node.children.get(2).getChildren().get(2).getChildren().get(0);
        System.out.println(vt3.getSymbol());
        System.out.println(vt3.children.get(0).getSymbol());

        Node vn3 = node.children.get(2).getChildren().get(2).getChildren().get(1);
        System.out.println(vn3.getSymbol());
        System.out.println(vn3.children.get(0).getSymbol());

        if(node.children.get(2).getChildren().get(2).getChildren().get(2).getChildren().size() > 0)
            return false;


        String vt1Type = typeof(vt1);
        String key1 = vn1.children.get(0).getSymbol();
        symbolTable.get(key1).updateDataType(vt1Type);
        String vt2Type = typeof(vt2);
        String key2 = vn2.children.get(0).getSymbol();
        symbolTable.get(key2).updateDataType(vt2Type);
        String vt3Type = typeof(vt3);
        String key3 = vn3.children.get(0).getSymbol();
        symbolTable.get(key3).updateDataType(vt3Type);

        return true;
    }

    private boolean typecheckSUBFUNCS(Node node) {
        return typecheckFUNCTIONS(node);
    }

    private String typeof(Node node) {
        System.out.println(node.symbol);
        switch (node.symbol) {
            case "VTYP":
                System.out.println(node.children.get(0).symbol);
                return node.children.get(0).symbol.equals("num") ? "n" : "t";
            case "FTYP":
                return node.children.get(0).symbol.equals("num") ? "n" : "v"; // Numeric return type or void
            case "ATOMIC":
                System.out.println(node.children.get(0).getSymbol());
                return typeof(node.children.get(0));
            case "CONST":
                System.out.println(node.children.get(0).symbol);
                return node.children.get(0).symbol.matches("\\d+(\\.\\d+)?") ? "n" : "t"; // Checking if it is a numeric constant or text
            case "TERM":
            case "ARG":
                return typeof(node.children.get(0));
            case "CALL":
                if (typecheckCALL(node)) {
                    System.out.println(node.children.getFirst().getChildren().getFirst().getSymbol());
                    return symbolTable.get(node.children.getFirst().getChildren().getFirst().getSymbol()).getDataType().equals("n") ? "n" : "u";
                } else {
                    return "u"; // Undefined
                }
            case "OP":
                Node firstChild = node.children.get(0);
                if (firstChild.symbol.equals("UNOP")) {
                    return typeof(firstChild);
                } else if (firstChild.symbol.equals("BINOP")) {
                    return typeof(firstChild);
                }
            case "UNOP":
                return node.children.get(0).symbol.equals("not") ? "b" : "n";
            case "BINOP":
                if (node.children.get(0).symbol.equals("or") || node.children.get(0).symbol.equals("and")) {
                    return "b";
                } else if (node.children.get(0).symbol.equals("eq") || node.children.get(0).symbol.equals("grt")) {
                    return "c";
                } else {
                    return "n";
                }
            case "FNAME":
            case "VNAME":
                System.out.println(node.getSymbol());
                System.out.println(node.children.getFirst().getSymbol());
                System.out.println(symbolTable.get(node.children.getFirst().getSymbol()).getDataType());

                return symbolTable.get(node.children.getFirst().getSymbol()).getDataType();
            case "SIMPLE":
            case "COMPOSIT":
                return "b";
            default:
                return "u";
        }
    }

    private Node findFunctionTypeNode(Node node) {
        Node parent = node.getParent(); // Assume Node has a reference to its parent
        while (parent != null && !parent.symbol.equals("DECL")) {
            parent = parent.getParent();
            System.out.println(parent.getSymbol());
        }
        Node header = null;
        if(parent.getSymbol().equals("DECL"))
             header = parent.getChildren().get(0);
        System.out.println(header.getSymbol());

        return parent != null ? header.getChildren().getFirst() : null; // The FTYP node is the first child of HEADER
    }

}