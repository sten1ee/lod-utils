package co.eft.util;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;
import java.io.File;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.util.Iterator;

public class Xml {

    public static class Doc {
        private final Document xmlDocument;
        private final XPath xPath;
        private static final XPathFactory xPathFactory = XPathFactory.newInstance();

        private Doc(Object source) {
            xmlDocument = prepareDocument(source);
            xPath = xPathFactory.newXPath();
        }

        private Document  prepareDocument(Object source) {
            try {
                DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
                DocumentBuilder builder = builderFactory.newDocumentBuilder();
                if (source instanceof InputStream)
                    return builder.parse((InputStream)source);
                if (source instanceof String)
                    source = new StringReader((String)source);
                if (source instanceof Reader)
                    return builder.parse(new InputSource((Reader)source));
                if (source instanceof File)
                    return builder.parse((File)source);
                throw new RuntimeException(String.format("Unrecognized Xml.Doc source: %s", source));
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

    public static Doc parseDoc(Reader rdr) {
        return new Xml.Doc(rdr);
    }

    /** Parse xml file */
    public static Doc parseDoc(File file) {
        return new Xml.Doc(file);
    }

    /** Parse inline xml */
    public static Doc parseDoc(String inlineXml) {
        return new Xml.Doc(inlineXml);
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
