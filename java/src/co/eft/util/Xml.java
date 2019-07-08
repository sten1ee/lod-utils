package co.eft.util;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Iterator;

public class Xml {

    public static class Doc {
        private final Document xmlDocument;
        private final XPath xPath;

        Doc(InputStream is) {
            try {
                DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
                DocumentBuilder builder = builderFactory.newDocumentBuilder();
                xmlDocument = builder.parse(is);
                xPath = XPathFactory.newInstance().newXPath();
            }
            catch (Exception exn) {
                throw Exn.wrap(exn);
            }
        }

        public Iterable<Node> nodes(String xpathExpression) {
            try {
                XPathExpression xpathExp = xPath.compile(xpathExpression);
                NodeList nodeList = (NodeList) xpathExp.evaluate(xmlDocument, XPathConstants.NODESET);
                return () -> new NodeListIterator(nodeList);
            }
            catch (Exception exn) {
                throw Exn.wrap(exn);
            }
        }
    }

    public static Doc parseDoc(InputStream is) {
        return new Xml.Doc(is);
    }

    public static Doc parseDoc(String fileName) {
        try {
            return parseDoc(new FileInputStream(fileName));
        }
        catch (FileNotFoundException exn) {
            throw Exn.wrap(exn);
        }
    }
}

class NodeListIterator implements Iterator<Node> {
    private final NodeList  nodeList;
    private final int endIdx;
    private int nextIdx = 0;


    NodeListIterator(NodeList nodeList) {
        this.nodeList = nodeList;
        this.endIdx = nodeList.getLength();
    }

    @Override
    public boolean hasNext() { return nextIdx < endIdx; }

    @Override
    public Node next() { return nodeList.item(nextIdx++); }
}
