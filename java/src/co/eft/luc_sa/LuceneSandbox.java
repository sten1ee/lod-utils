package co.eft.luc_sa;

import co.eft.util.Exn;
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

import java.io.File;
import java.io.IOException;

import co.eft.util.Xml;
import co.eft.util.Iterables;


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


    public static void  main(String[] args) throws Exception
    {
        System.out.format("Working Directory = %s\n", System.getProperty("user.dir"));
        Xml.Doc doc = Xml.parseDoc(new File("resources/lucene-tests.short.xml"));
        String lang = "Sa-Ltn";
        String queryPath = String.format("/lucene-tests/lang[@lang='%s']/test/query", lang);
        String dataPath = String.format("/lucene-tests/lang[@lang='%s']/data/.", lang);

        dumpDataAndTests(doc, dataPath, queryPath);

        indexAll(Iterables.convert(doc.nodes(dataPath), Node::getTextContent));

        searchAll(Iterables.convert(doc.nodes(queryPath), Node::getTextContent));
    }
}


