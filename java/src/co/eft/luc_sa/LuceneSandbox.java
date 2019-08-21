package co.eft.luc_sa;

import io.bdrc.lucene.sa.SanskritAnalyzer;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.NumericDocValuesField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;
import java.io.*;

import java.util.Iterator;
import java.util.function.Function;


class Iterables {

    private Iterables() {}

    private static class IteratorAdapter<S, T> implements Iterator<T> {
        private final Iterator<S>    src;
        private final Function<S, T> converter;

        IteratorAdapter(Iterator<S> src, Function<S, T> converter) {
            this.src = src;
            this.converter = converter;
        }

        @Override
        public boolean hasNext() { return src.hasNext(); }

        @Override
        public T next() { return converter.apply(src.next()); }

        @Override
        public void remove() { src.remove(); }
    }

    public static <S, T>  Iterable<T> convert(Iterable<S> src, Function<S, T> converter) {
        return () -> new IteratorAdapter<>(src.iterator(), converter);
    }
}


class Exn {

    public static RuntimeException  wrap(Exception exn) {
        if (exn instanceof RuntimeException)
            return (RuntimeException) exn;
        else
            return new RuntimeException(exn);
    }
}


class Xml {

    public static class Doc {
        private final org.w3c.dom.Document xmlDocument;
        private final XPath xPath;
        private static final XPathFactory xPathFactory = XPathFactory.newInstance();

        private Doc(Object source) {
            xmlDocument = prepareDocument(source);
            xPath = xPathFactory.newXPath();
        }

        private org.w3c.dom.Document prepareDocument(Object source) {
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
        return new Doc(is);
    }

    public static Doc parseDoc(Reader rdr) {
        return new Doc(rdr);
    }

    /** Parse xml file */
    public static Doc parseDoc(File file) {
        return new Doc(file);
    }

    /** Parse inline xml */
    public static Doc parseDoc(String inlineXml) {
        return new Doc(inlineXml);
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

public class LuceneSandbox {
    static final Logger log = LoggerFactory.getLogger(LuceneSandbox.class);

    static final Version  LuceneVersion = Version.LUCENE_4_10_4;
    static final String FN_BODY = "sa-ltn";
    static final String FN_DOCID = "doc-id";

    static class AnalyzerArgs {
        final Version version = LuceneVersion;
        final String  mode = "syl";
        final String  inputEncoding = "roman";
        final Boolean mergePrepositions = false;
        final Boolean indexFilterGeminates = true;
        final Boolean queryFilterGeminates = false;
        final String  indexLenient = "index";
        final String  queryLenient = "query";
        final String  noLenient = "no";
    }

    static SanskritAnalyzer  new_IndexSanskritAnalyzer() {
        AnalyzerArgs a = new AnalyzerArgs();
        try {
            return new SanskritAnalyzer.IndexLenientSyl();
        } catch (IOException exn) {
            log.error("Error initializing (index) SanskritAnalyzer: ");
            throw Exn.wrap(exn);
        }
    }

    static SanskritAnalyzer  new_QuerySanskritAnalyzer() {
        AnalyzerArgs a = new AnalyzerArgs();
        try {
            return new SanskritAnalyzer.QueryLenientSyl();
        } catch (IOException exn) {
            log.error("Error initializing (query) SanskritAnalyzer: ");
            throw Exn.wrap(exn);
        }
    }

    final static Directory indexDir;
    static {
        try {
            indexDir = FSDirectory.open(new File("index-tmp/"));
        } catch (IOException exn) {
            throw Exn.wrap(exn);
        }
    }

    static IndexWriter  new_IndexWriter() {
        return new_IndexWriter(indexDir);
    }

    static IndexWriter  new_IndexWriter(Directory dir) {
        try {
            IndexWriterConfig config = new IndexWriterConfig(LuceneVersion, new_IndexSanskritAnalyzer());
            config.setOpenMode(IndexWriterConfig.OpenMode.CREATE);
            return new IndexWriter(dir, config);
        } catch (IOException exn) {
            log.error("Error initializing IndexWriter: ");
            throw Exn.wrap(exn);
        }
    }

    static void  indexAll(Iterable<String> terms) throws IOException {
        try (IndexWriter iwriter = new_IndexWriter()) {
            int i = 0;
            for (String term : terms) {
                ++i;
                Document doc = new Document();
                doc.add(new NumericDocValuesField(FN_DOCID, i));
                doc.add(new Field(FN_BODY, term, TextField.TYPE_STORED));
                log.info(String.format("*** Indexing doc[%d]: '%s' ...", i, term));
                iwriter.addDocument(doc);
            }
            log.info(String.format("%d docs indexed", i));
        }
    }

    static void  searchAll(Iterable<String> queryPhrases) throws IOException, ParseException {
        try (DirectoryReader ireader = DirectoryReader.open(indexDir)) {
            IndexSearcher isearcher = new IndexSearcher(ireader);
            // Parse a simple query that searches for "text":
            QueryParser parser = new QueryParser(FN_BODY, new_QuerySanskritAnalyzer());
            for (String phrase : queryPhrases) {
                Query query = parser.parse(phrase);
                ScoreDoc[] hits = isearcher.search(query, null, 1000).scoreDocs;
                // Iterate through the results:
                log.info("----------------------------------------");
                log.info(String.format("Searching for phrase '%s', parsed query looks like: (%s)", phrase, query));
                for (ScoreDoc hit : hits) {
                    Document doc = ireader.document(hit.doc);
                    int    docId = (hit.doc + 1);//doc.get(FN_DOCID);
                    String body  = doc.get(FN_BODY);
                    log.info(String.format("  scored %2.2f on doc[%d] '%s'",
                                            hit.score, docId, body));
                }
            }
        }
    }

    static void  dumpDataAndTests(Xml.Doc doc, String dataPath, String queryPath) {
        int i = 0;
        for (Node node : doc.nodes(dataPath))
            log.info(String.format("Data[%d]: %s", ++i, node.getTextContent()));
        log.info("");

        i = 0;
        for (Node node : doc.nodes(queryPath))
            log.info(String.format("Query[%d]: %s", ++i, node.getTextContent()));
        log.info("");
    }

    final static String lucene_tests_content =
        "<lucene-tests xmlns='http://read.84000.co/ns/1.0'>\n"+
        "    <lang xml:lang='Sa-Ltn'>\n"+
        "        <label>Sanskrit</label>\n"+
        "        <test xml:id='test-1'>\n"+
        "            <query>Maitreya­praṇidhana</query>\n"+
        "        </test>\n"+
        "        <data xml:id='data-1'>maitreyapraṇid</data>\n"+
        "        <data xml:id='data-2'>Maitreyapraṇid</data>\n"+
        "        <data xml:id='data-3'>MaitreyaPraṇid</data>\n"+
        "        <data xml:id='data-4'>Maitreyapraṇidhana</data>\n"+
        "        <data xml:id='data-5'>Maitreya­praṇidhana</data>\n"+
        "        <data xml:id='data-6'>Maitreya­praṇidhana­rāja</data>\n"+
        "        <data xml:id='data-7'>maitreyapra[mukhas]ṇidhana</data>\n"+
        "        <data xml:id='data-8'>Maitreyapraṇidhanarāja</data>\n"+
        "        <data xml:id='data-9'>Maitreyaṇidhana</data>\n"+
        "    </lang>\n"+
        "</lucene-tests>\n"+
        "";

    public static void  main(String[] args) throws Exception
    {
        System.out.format("Working Directory = %s\n", System.getProperty("user.dir"));
        Xml.Doc doc = Xml.parseDoc(lucene_tests_content);
        String lang = "Sa-Ltn";
        String queryPath = String.format("/lucene-tests/lang[@lang='%s']/test/query", lang);
        String dataPath = String.format("/lucene-tests/lang[@lang='%s']/data/.", lang);

        dumpDataAndTests(doc, dataPath, queryPath);

        indexAll(Iterables.convert(doc.nodes(dataPath), Node::getTextContent));

        searchAll(Iterables.convert(doc.nodes(queryPath), Node::getTextContent));
    }
}
