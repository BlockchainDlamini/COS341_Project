import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.StringWriter;

public class ParseTreeXMLGenerator {
    public void generateParseTreeXML(Node rootNode, String outputFilePath) throws Exception {
        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
        Document doc = docBuilder.newDocument();

        // Root element
        Element syntreeElement = doc.createElement("SYNTREE");
        doc.appendChild(syntreeElement);

        // ROOT element
        Element rootElement = doc.createElement("ROOT");
        syntreeElement.appendChild(rootElement);

        // UNID element
        Element unidElement = doc.createElement("UNID");
        unidElement.appendChild(doc.createTextNode(String.valueOf(rootNode.getUnid())));
        rootElement.appendChild(unidElement);

        // SYMB element
        Element symbElement = doc.createElement("SYMB");
        symbElement.appendChild(doc.createTextNode(rootNode.getSymbol()));
        rootElement.appendChild(symbElement);

        // CHILDREN element
        Element childrenElement = doc.createElement("CHILDREN");
        rootElement.appendChild(childrenElement);
        for (Node child : rootNode.getChildren()) {
            Element idElement = doc.createElement("ID");
            idElement.appendChild(doc.createTextNode(String.valueOf(child.getUnid())));
            childrenElement.appendChild(idElement);
        }

        // INNERNODES and LEAFNODES elements
        Element innerNodesElement = doc.createElement("INNERNODES");
        Element leafNodesElement = doc.createElement("LEAFNODES");
        syntreeElement.appendChild(innerNodesElement);
        syntreeElement.appendChild(leafNodesElement);

        // Recursively process children
        processChildren(doc, rootNode, innerNodesElement, leafNodesElement);

        // Write the content into XML file
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
        DOMSource source = new DOMSource(doc);
        StreamResult result = new StreamResult(new File(outputFilePath));
        transformer.transform(source, result);
    }

    private void processChildren(Document doc, Node parentNode, Element innerNodesElement, Element leafNodesElement) {
        for (Node child : parentNode.getChildren()) {
            if (child.getChildren().isEmpty()) {
                Element leafElement = doc.createElement("LEAF");
                leafNodesElement.appendChild(leafElement);

                Element parentElement = doc.createElement("PARENT");
                parentElement.appendChild(doc.createTextNode(String.valueOf(parentNode.getUnid())));
                leafElement.appendChild(parentElement);

                Element unidElement = doc.createElement("UNID");
                unidElement.appendChild(doc.createTextNode(String.valueOf(child.getUnid())));
                leafElement.appendChild(unidElement);

                Element terminalElement = doc.createElement("TERMINAL");
                terminalElement.appendChild(doc.createTextNode(child.getSymbol()));
                leafElement.appendChild(terminalElement);
            } else {
                Element innerElement = doc.createElement("IN");
                innerNodesElement.appendChild(innerElement);

                Element parentElement = doc.createElement("PARENT");
                parentElement.appendChild(doc.createTextNode(String.valueOf(parentNode.getUnid())));
                innerElement.appendChild(parentElement);

                Element unidElement = doc.createElement("UNID");
                unidElement.appendChild(doc.createTextNode(String.valueOf(child.getUnid())));
                innerElement.appendChild(unidElement);

                Element symbElement = doc.createElement("SYMB");
                symbElement.appendChild(doc.createTextNode(child.getSymbol()));
                innerElement.appendChild(symbElement);

                Element childrenElement = doc.createElement("CHILDREN");
                innerElement.appendChild(childrenElement);
                for (Node grandChild : child.getChildren()) {
                    Element idElement = doc.createElement("ID");
                    idElement.appendChild(doc.createTextNode(String.valueOf(grandChild.getUnid())));
                    childrenElement.appendChild(idElement);
                }

                // Recursively process the children of this inner node
                processChildren(doc, child, innerNodesElement, leafNodesElement);
            }
        }
    }
}