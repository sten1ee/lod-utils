package co.eft.luc_sa;

import io.bdrc.lucene.sa.SanskritAnalyzer;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.NumericDocValuesField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.lang.System.out;

import co.eft.util.Xml;
import co.eft.util.Iterables;

public class LuceneSandbox {
    static final Logger log = LoggerFactory.getLogger(LuceneSandbox.class);

    static final Version  LuceneVersion = Version.LUCENE_4_10_4;

    static class AnalyzerArgs {
        final Version version = LuceneVersion;
        final String  mode = "word";
        final String  inputEncoding = "roman";
        final Boolean mergePrepositions = false;
        final Boolean filterGeminates = false;
        final String  indexLenient = "index";
        final String  queryLenient = "query";
        final String  noLenient = "no";
    }

    static SanskritAnalyzer  new_IndexSanskritAnalyzer() {
        AnalyzerArgs a = new AnalyzerArgs();
        try {
            return new SanskritAnalyzer(
                a.version, a.mode,
                a.inputEncoding, a.mergePrepositions,
                a.filterGeminates, a.indexLenient);
        } catch (IOException exn) {
            log.error("While initializing (index) SanskritAnalyzer: ", exn);
            System.exit(-1);
            return null;
        }
    }

    static SanskritAnalyzer  new_QuerySanskritAnalyzer() {
        AnalyzerArgs a = new AnalyzerArgs();
        try {
            return new SanskritAnalyzer(
                a.version, a.mode,
                a.inputEncoding, a.mergePrepositions,
                a.filterGeminates, a.queryLenient);
        } catch (IOException exn) {
            log.error("While initializing (query) SanskritAnalyzer: ", exn);
            System.exit(-1);
            return null;
        }
    }

    static IndexWriter  new_IndexWriter() {
        return new_IndexWriter("index-tmp/");
    }

    static IndexWriter  new_IndexWriter(String indexDir) {
        try {
            Directory dir = FSDirectory.open(new File(indexDir));
            IndexWriterConfig config = new IndexWriterConfig(LuceneVersion, new_IndexSanskritAnalyzer());
            return new IndexWriter(dir, config);
        } catch (IOException exn) {
            log.error("While initializing IndexWriter: ", exn);
            System.exit(-1);
            return null;
        }
    }

    static void  indexAll(Iterable<String> terms) throws IOException {
        try (IndexWriter iwriter = new_IndexWriter()) {
            int i = 0;
            for (String term : terms) {
                ++i;
                Document doc = new Document();
                doc.add(new NumericDocValuesField("docId", i));
                doc.add(new Field("sa-ltn", term, TextField.TYPE_STORED));
                iwriter.addDocument(doc);
            }
        }
    }

    static void  searchAll(Iterable<String> terms) throws IOException {
    }

    static final String xmlToIndex;
    static {
        xmlToIndex =
            "<data xml:id=\"data-1\">maitreyapraṇid</data>\n" +
            "<data xml:id=\"data-2\">Maitreyapraṇid</data>\n" +
            "<data xml:id=\"data-3\">MaitreyaPraṇid</data>\n" +
            "<data xml:id=\"data-4\">Maitreyapraṇidhana</data>\n" +
            "<data xml:id=\"data-5\">Maitreya\u00ADpraṇidhana</data>\n" +
            "<data xml:id=\"data-6\">Maitreya\u00ADpraṇidhana\u00ADrāja</data>\n" +
            "<data xml:id=\"data-7\">maitreyapra[mukhas]ṇidhana</data>\n" +
            "<data xml:id=\"data-8\">Maitreyapraṇidhanarāja</data>\n" +
            "<data xml:id=\"data-9\">Atyayajñānasūtra</data>\n" +
            "<data xml:id=\"data-10\">Maitreya</data>\n" +
            "<data xml:id=\"data-11\">Atyayajñāna\u00ADsūtra</data>\n" +
            "<data xml:id=\"data-13\">Abiding</data>\n" +
            "<data xml:id=\"data-14\">Calm-abiding</data>\n" +
            "<data xml:id=\"data-15\">Miracles</data>\n" +
            "<data xml:id=\"data-16\">miraculous</data>\n" +
            "<data xml:id=\"data-17\">Buddhadharma</data>\n" +
            "<data xml:id=\"data-18\">Maitreyaṇidhana</data>\n" +
            "<data xml:id=\"data-19\">sūtra</data>\n" +
            "<data xml:id=\"data-20\">Sūtta</data>\n" +
            "<data xml:id=\"data-21\">saptamuni</data>\n" +
            "<data xml:id=\"data-22\">sapta</data>\n" +
            "<data xml:id=\"data-23\">saptotsada</data>\n" +
            "<data xml:id=\"data-24\">tṛṣṇā</data>\n" +
            "<data xml:id=\"data-25\">trsna</data>\n" +
            "<data xml:id=\"data-26\">trishna</data>\n" +
            "<data xml:id=\"data-27\">Dhāraṇī</data>\n" +
            "<data xml:id=\"data-28\">Dhāraṇī</data>\n" +
            "<data xml:id=\"data-29\">śīla</data>\n" +
            "<data xml:id=\"data-30\">pari\u00ADjñātu\u00ADkāma</data>\n" +
            "<data xml:id=\"data-31\">śāliṃcī</data>\n" +
            "<data xml:id=\"data-32\">pari\u00ADjñātu\u00ADkāma</data>\n" +
            "<data xml:id=\"data-33\">ḷrkāra</data>\n" +
            "<data xml:id=\"data-34\">ḷkāra</data>\n" +
            "<data xml:id=\"data-35\">ḹ </data>\n" +
            "<data xml:id=\"data-36\">ṝkāra</data>\n" +
            "<data xml:id=\"data-37\">śaraḥ</data>\n" +
            "<data xml:id=\"data-38\">ācāryamuṣṭi</data>\n" +
            "<data xml:id=\"data-39\">ācāryamuṣṭi</data>\n" +
            "<data xml:id=\"data-40\">bodhimaṇḍa</data>\n";
    }

    static String  extractText(String textToIndex) {
        Pattern p = Pattern.compile("<data xml:id=\"([^\"]+)\">([^<]+)</data>\\s*");
        Matcher matcher = p.matcher(textToIndex);
        StringBuilder sb = new StringBuilder();
        for (int count = 0; matcher.find(); ) {
            count++;
            String match_id = matcher.group(1);
            String match_txt = matcher.group(2);
            out.printf("data-%d: '%s' : %s\n", count, match_id, match_txt);
            sb.append(match_txt + "\n");
        }
        return sb.toString();
    }

    static void  processWithRegEx() {
        String textToIndex = extractText(xmlToIndex);
        StringReader reader = new StringReader(textToIndex);

        try (Analyzer   anz = new_IndexSanskritAnalyzer();
             TokenStream ts = anz.tokenStream("field", reader)) {

            OffsetAttribute offsetAtt = ts.addAttribute(OffsetAttribute.class);
            CharTermAttribute termAtt = ts.addAttribute(CharTermAttribute.class);
            ts.reset();
            while (ts.incrementToken()) {
                String token = termAtt.toString();
                out.println("[" + token + "]");
                out.println("Token starting offset: " + offsetAtt.startOffset());
                out.println(" Token ending offset: " + offsetAtt.endOffset());
                out.println("");
            }
            ts.end();
        } catch (IOException exn) {
            log.error("Fatal error: ", exn);
        }
    }




    public static void  main(String[] args) throws Exception {
        System.out.format("Working Directory = %s\n", System.getProperty("user.dir"));
        Xml.Doc doc = Xml.Doc("resources/lucene-tests.xml");
        String lang = "Sa-Ltn";
        String testPath = String.format("/lucene-tests/lang[@lang='%s']/test/query", lang);
        String dataPath = String.format("/lucene-tests/lang[@lang='%s']/data/.", lang);

        int i=0;
        for (Node node : doc.nodes(testPath))
            out.format("Test[%d]: %s\n", ++i, node.getTextContent());

        List<String> data = new ArrayList<>();
        Iterable<Node> nodes = doc.nodes(dataPath);
        nodes.forEach(node -> {
            out.format("Data: %s\n", node.getTextContent());
            data.add(node.getTextContent());
        });

        indexAll(Iterables.convert(nodes, node -> node.getTextContent()));
    }
}


