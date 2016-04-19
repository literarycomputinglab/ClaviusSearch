package it.cnr.ilc.lc.clavius.search;

import com.google.gson.Gson;
import it.cnr.ilc.lc.clavius.search.entity.Annotation;
import it.cnr.ilc.lc.clavius.search.entity.PlainText;
import it.cnr.ilc.lc.clavius.search.entity.TEADocument;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.List;
import java.util.Properties;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.Fields;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.PostingsEnum;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.DocIdSetIterator;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.spans.SpanQuery;
import org.apache.lucene.search.spans.SpanTermQuery;
import org.apache.lucene.search.spans.SpanWeight;
import org.apache.lucene.search.spans.Spans;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.BytesRef;
import org.hibernate.search.jpa.FullTextEntityManager;
import org.hibernate.search.jpa.FullTextQuery;
import org.hibernate.search.query.dsl.QueryBuilder;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author angelo
 */
public class Tester {

    private static final Logger logger = LogManager.getLogger(Tester.class);

    private static String TEAoutput = "{\"id\":3079,"
            + "\"name\":\"name\","
            + "\"code\":\"\\\\n\\u003cOxygonium vero, quod tres habet acutos angulos\\u003e(s1). "
            + "Omne\\\\n\\u003ctriangulum Oxygonium\\u003e(t1), sive \\u003cacutangulum\\u003e(t2), potest esse \\\\nvel \\u003caequilaterum\\u003e(t3), "
            + "vel \\u003cisosceles\\u003e(t4), vel \\u003cscalenum\\u003e(t5), \\\\nut cernere licet in \\u003ctriangulis\\u003e(e1), "
            + "quae in speciebus prioris \\\\n\\\\ntest\\\\n\\\\ndivisionis spectanda exhibuimus, ne eadem hic frustra repetantur.\\\\n+++\\\\n"
            + "(s1) lvont:language lexvo:iso639-3/lat\\\\n(s1) lvont:translation \\\\\\\"...\\\\\\\"\\\\n(t1) rdfs:seeAlso cll:math/triangulum_oxygonium\\\\n"
            + "(t2) rdfs:seeAlso cll:math/triangulum_acutangulum\\\\n"
            + "(t3) rdfs:seeAlso cll:math/triangulum_aequilaterum\\\\n"
            + "(t4) rdfs:seeAlso cll:math/triangulum_isosceles\\\\n"
            + "(t5) rdfs:seeAlso cll:math/triangulum_scalenum\\\\n"
            + "(e1) rdfs:seeAlso dbr:Triangle\\\\n"
            + "(e1) foaf:page https://en.wikipedia.org/wiki/Triangle\\\\n+++\\\\n\\\\n\\\\n\\\\n\","
            + "\"text\":\"Oxygonium vero, quod tres habet acutos angulos. Omne\\ntriangulum Oxygonium, sive acutangulum, potest esse \\nvel aequilaterum, vel isosceles, vel scalenum, \\nut cernere licet in triangulis, quae in speciebus prioris \","
            + "\"idDoc\":\"319\","
            + "\"triples\":[{\"start\":18,\"end\":25,\"subject\":\"(s1)\",\"predicate\":\"rdfs:seeAlso\",\"object\":\"cll:math/triangulum_acutangulum\"},{\"start\":38,\"end\":45,\"subject\":\"(s2)\",\"predicate\":\"rdfs:seeAlso\",\"object\":\"cll:math/triangulum_oxygonium\"}]}";
    private static List<Annotation> results;
    private static String jsonResult;

    private static Properties conceptsMap;

    private static int ctxLen = 30;

    public static void main(String[] args) throws Exception {

        readProperies();
        // createEntity(true);
        //results = search("BOBBE2");
        //results = conceptSearch("Persona Pippo Lavoratore ");
        /*
        results = searchQueryParse("triangolo");
        toJson(results);
         */

        //createFullTextEntity(TEAoutput);
        //fullTextSearch("triangul*");
        searchWithContext("triangulum");
    }

    private static void createEntity(boolean flag) throws InterruptedException {

        if (flag) {
            EntityManagerFactory entityManagerFactory = Persistence.createEntityManagerFactory("clavius");
            EntityManager entityManager = entityManagerFactory.createEntityManager();
            FullTextEntityManager fullTextEntityManager = org.hibernate.search.jpa.Search.getFullTextEntityManager(entityManager);

            entityManager.getTransaction().begin();

            /*
            Annotation a = new Annotation();
            a.setMatched("diametro");
            a.setType("circle-diameter");
            a.setLeftContext("l'istessa luna con l'occhio naturale; et la veggo adesso di ");
            a.setRightContext(" eguale al semidiametro della #LUNA# veduta con la vista semplice. Hora");
            a.setConcepts("figure plane-figure line circle-diameter");
            a.setResourceObject("http://claviusontheweb.it/lexicon/math/diameter_circuli");
            a.setIdLetter(Long.valueOf(319));
            a.setPageNum(Long.valueOf(3));
            a.setIdNeo4j(123456l);
            
            Annotation a2 = new Annotation();
            a2.setMatched("parallele");
            a2.setType("straight-line");
            a2.setLeftContext("capello la costituzione delle sue 3. stelle, le quali per quanto io stimo sono esattamente");
            a2.setRightContext("non al zodiaco, ma all’Equinoziale. La notte passata osservai ");
            a2.setConcepts("figure plane-figure line straight-line");
            a2.setResourceObject("http://claviusontheweb.it/lexicon/math/linea_recta");
            a2.setIdLetter(Long.valueOf(319));
            a2.setPageNum(Long.valueOf(3));
            a2.setIdNeo4j(123456l);
            // entityManager.persist(a);
            entityManager.persist(a2);
             */
            TEADocument teadoc = parseTEAJson(TEAoutput);

            String plainText = teadoc.text;
            String idDoc = teadoc.idDoc;
            List<TEADocument.Triple> triples = teadoc.triples;

            for (TEADocument.Triple triple : triples) {
                Annotation a = new Annotation();
                a.setLeftContext(plainText.substring(triple.start > ctxLen ? triple.start - ctxLen : 0, triple.start));
                a.setRightContext(plainText.substring(triple.end, triple.end + ctxLen < plainText.length() ? triple.end + ctxLen : plainText.length()));
                a.setIdDoc(Long.valueOf(idDoc));
                a.setConcept(conceptsMap.getProperty(triple.object.substring(triple.object.lastIndexOf("/") + 1))); //@FIX triple.object sara' la chiave di accesso alla mappa dei concetti
                a.setType(triple.object.substring(triple.object.lastIndexOf("/") + 1));
                a.setResourceObject(triple.object);
                a.setIdNeo4j(teadoc.id);
                a.setMatched(plainText.substring(triple.start, triple.end));
                entityManager.persist(a);
                logger.info("createEntity: " + a);
            }

            fullTextEntityManager.createIndexer().startAndWait();
            fullTextEntityManager.flushToIndexes();
            entityManager.getTransaction().commit();
            entityManager.close();
            entityManagerFactory.close();

        }

    }

    private static List<Annotation> searchQueryParse(String query) {

        EntityManagerFactory entityManagerFactory = Persistence.createEntityManagerFactory("clavius");
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        FullTextEntityManager fullTextEntityManager = org.hibernate.search.jpa.Search.getFullTextEntityManager(entityManager);
        entityManager.getTransaction().begin();

        List result = null;
        QueryParser parser = new QueryParser(
                "concept",
                fullTextEntityManager.getSearchFactory().getAnalyzer(Annotation.class)
        );

        org.apache.lucene.search.Query luceneQuery;
        try {
            luceneQuery = parser.parse(query);
            FullTextQuery fullTextQuery
                    = fullTextEntityManager.createFullTextQuery(luceneQuery, Annotation.class);

            result = fullTextQuery.getResultList();

        } catch (ParseException ex) {
            logger.error(ex);
        }

        entityManager.getTransaction().commit();
        fullTextEntityManager.flushToIndexes();
        fullTextEntityManager.clear();
        entityManager.close();
        entityManagerFactory.close();

        return result;
    }

    private static List<Annotation> conceptSearch(String w) {
        EntityManagerFactory entityManagerFactory = Persistence.createEntityManagerFactory("clavius");
        EntityManager entityManager = entityManagerFactory.createEntityManager();

        FullTextEntityManager fullTextEntityManager = org.hibernate.search.jpa.Search.getFullTextEntityManager(entityManager);

        entityManager.getTransaction().begin();

        QueryBuilder qb = fullTextEntityManager.getSearchFactory().buildQueryBuilder().forEntity(Annotation.class).get();
        org.apache.lucene.search.Query query = qb
                .keyword()
                .onField("concept")
                .matching(w)
                .createQuery();

        javax.persistence.Query persistenceQuery
                = fullTextEntityManager.createFullTextQuery(query, Annotation.class);
        List<Annotation> result = persistenceQuery.getResultList();

        for (Annotation a : result) {
            System.err.println("Search " + a);
        }

        entityManager.getTransaction().commit();
        fullTextEntityManager.flushToIndexes();
        fullTextEntityManager.clear();
        entityManager.close();
        entityManagerFactory.close();

        //fullTextEntityManager.close(); // attenzione se resta aperto non indicizza lucene
        return result;
    }

    private static List<Annotation> search(String w) {
        EntityManagerFactory entityManagerFactory = Persistence.createEntityManagerFactory("clavius");
        EntityManager entityManager = entityManagerFactory.createEntityManager();

        FullTextEntityManager fullTextEntityManager = org.hibernate.search.jpa.Search.getFullTextEntityManager(entityManager);

        entityManager.getTransaction().begin();

        QueryBuilder qb = fullTextEntityManager.getSearchFactory().buildQueryBuilder().forEntity(Annotation.class).get();
        org.apache.lucene.search.Query query = qb
                .keyword()
                .onField("matched")
                .matching(w)
                .createQuery();

        javax.persistence.Query persistenceQuery
                = fullTextEntityManager.createFullTextQuery(query, Annotation.class);
        List<Annotation> result = persistenceQuery.getResultList();

        for (Annotation a : result) {
            System.err.println("Search " + a);
        }

        entityManager.getTransaction().commit();
        fullTextEntityManager.flushToIndexes();
        fullTextEntityManager.clear();
        entityManager.close();
        entityManagerFactory.close();

        //fullTextEntityManager.close(); // attenzione se resta aperto non indicizza lucene
        return result;
    }

    private static List<PlainText> fullTextSearch(String w) {
        EntityManagerFactory entityManagerFactory = Persistence.createEntityManagerFactory("clavius");
        EntityManager entityManager = entityManagerFactory.createEntityManager();

        FullTextEntityManager fullTextEntityManager = org.hibernate.search.jpa.Search.getFullTextEntityManager(entityManager);

        entityManager.getTransaction().begin();

        QueryBuilder qb = fullTextEntityManager.getSearchFactory().buildQueryBuilder().forEntity(PlainText.class).get();
        org.apache.lucene.search.Query query = qb
                .keyword()
                .wildcard()
                .onField("content")
                .matching(w)
                .createQuery();

        javax.persistence.Query persistenceQuery
                = fullTextEntityManager.createFullTextQuery(query, PlainText.class);
        List<PlainText> result = persistenceQuery.getResultList();

        for (PlainText a : result) {
            System.err.println("Search (" + a.getContent() + ")");
        }

        entityManager.getTransaction().commit();
        fullTextEntityManager.flushToIndexes();
        fullTextEntityManager.clear();
        entityManager.close();
        entityManagerFactory.close();

        //fullTextEntityManager.close(); // attenzione se resta aperto non indicizza lucene
        return result;
    }

    private static void toJson(List<Annotation> loa) {

        Gson gson = new Gson();

        // convert java resourceObject to JSON format,
        // and returned as JSON formatted string
        String json = gson.toJson(loa);

        System.err.println("JSON result: " + json);

    }

    private static String createTEAJson(TEADocument teadoc) {

        Gson gson = new Gson();

        // convert java resourceObject to JSON format,
        // and returned as JSON formatted string
        String json = gson.toJson(teadoc);

        System.err.println("JSON result: " + json);
        return json;

    }

    private static TEADocument parseTEAJson(String json) {

        Gson gson = new Gson();

        // convert java resourceObject to JSON format,
        // and returned as JSON formatted string
        TEADocument teadoc = gson.fromJson(json, TEADocument.class);

        System.err.println("TEADoc result: " + teadoc);

        return teadoc;

    }

    private static void readProperies() {

        InputStream input = null;

        try {
            input = Tester.class.getResourceAsStream("/concepts-map.properties");
            // load a properties file
            conceptsMap = new Properties();
            conceptsMap.load(input);
        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private static void createFullTextEntity(String json) {

        EntityManagerFactory entityManagerFactory = Persistence.createEntityManagerFactory("clavius");
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        FullTextEntityManager fullTextEntityManager = org.hibernate.search.jpa.Search.getFullTextEntityManager(entityManager);

        entityManager.getTransaction().begin();

        TEADocument teadoc = parseTEAJson(json);
        String content = teadoc.text;
        String idDoc = teadoc.idDoc;
        String extra = teadoc.name;

        PlainText ft = new PlainText();

        ft.setIdDoc(idDoc);
        ft.setContent(content);
        ft.setExtra(extra);

        entityManager.persist(ft);

//        try {
//            fullTextEntityManager.createIndexer().startAndWait();
//        } catch (InterruptedException ex) {
//            java.util.logging.Logger.getLogger(Tester.class.getName()).log(Level.SEVERE, null, ex);
//        }
        entityManager.getTransaction().commit();
        fullTextEntityManager.flushToIndexes();
        fullTextEntityManager.clear();
        entityManager.close();
        entityManagerFactory.close();

    }

    private static void searchWithContext(String term) {

        try {
            logger.info("searchWithContext(" + term + ")");
            SpanQuery spanQuery = new SpanTermQuery(new Term("content", term));
            Directory indexDirectory
                    = FSDirectory.open(Paths.get("/var/lucene/claviusTest/indexes/it.cnr.ilc.lc.clavius.search.entity.PlainText"));
            IndexReader indexReader = DirectoryReader.open(indexDirectory);
            IndexSearcher searcher = new IndexSearcher(indexReader);
            IndexReader reader = searcher.getIndexReader();
            spanQuery = (SpanQuery) spanQuery.rewrite(reader);
            SpanWeight weight = (SpanWeight) searcher.createWeight(spanQuery, false);

//            Spans spans2 = weight.getSpans(reader.leaves().get(0),
//                    SpanWeight.Postings.OFFSETS);
            Spans spans = weight.getSpans(reader.leaves().get(0), SpanWeight.Postings.PAYLOADS);
            int nextDoc = spans.nextDoc();
            logger.info("spans.docID(): " + nextDoc);
            Fields fields = reader.getTermVectors(nextDoc);
            Terms terms = fields.terms("content");

            TermsEnum termsEnum = terms.iterator();
            BytesRef text;
            int start = spans.startPosition() - 3;
            int end = spans.endPosition() + 3;
            while ((text = termsEnum.next()) != null) {
                //could store the BytesRef here, but String is easier for this example
                String s = new String(text.bytes, text.offset, text.length);
//                DocsAndPositionsEnum positionsEnum = termsEnum.docsAndPositions(null, null);
                PostingsEnum postingEnum = termsEnum.postings(null);

                if (postingEnum.nextDoc() != DocIdSetIterator.NO_MORE_DOCS) {
                    int i = 0;
                    int position = -1;
                    while (spans.nextStartPosition() != Spans.NO_MORE_POSITIONS) {
                        while (i < postingEnum.freq() && (position = postingEnum.nextPosition()) != -1) {
                            if (position >= start && position <= end) {
                                logger.info("pos: " + position + ", term: " + s + " offset: " + text + " length: " + text.length);
                            }
                            i++;
                        }
                    }
                }

//                while (postingEnum.nextDoc() != DocIdSetIterator.NO_MORE_DOCS) {
//                }
//                
//                
//                if (positionsEnum.nextDoc() != DocIdSetIterator.NO_MORE_DOCS) {
//                    int i = 0;
//                    int position = -1;
//                    while (i < positionsEnum.freq() && (position = positionsEnum.nextPosition()) != -1) {
//                        if (position >= start && position <= end) {
//                            entries.put(position, s);
//                        }
//                        i++;
//                    }
//                }
//            }
//            int i = 0;
//            int spanStart = -1;
//            int spanEnd = -1;
//            int docID = -1;
////            while (spans.nextDoc() != Spans.NO_MORE_DOCS) {
//                while (spans.nextStartPosition() != Spans.NO_MORE_POSITIONS) {
//                    spanStart = spans.startPosition();
//                    spanEnd = spans.endPosition();
//                    docID = spans.docID();
//                    i++;
//                    logger.info("spanStart: " + spanStart + ", spanEnd: " + spanEnd + ", docID: " + docID);
//                }
//            }
            }
        } catch (IOException e) {
            logger.error(e.getMessage());
        }

    }
//    
//    private void testOffsetForSingleSpanMatch(SpanOnlyParser p, String s,
//            int trueDocID, int trueSpanStart, int trueSpanEnd) throws Exception {
//        SpanQuery q = (SpanQuery) p.parse(s);
//        List<LeafReaderContext> ctxs = reader.leaves();
//        assert (ctxs.size() == 1);
//        LeafReaderContext ctx = ctxs.get(0);
//        q = (SpanQuery) q.rewrite(ctx.reader());
//        SpanWeight spanWeight = q.createWeight(searcher, true);
//        Spans spans = spanWeight.getSpans(ctx, null, SpanWeight.Postings.POSITIONS);
//        
//        int i = 0;
//        int spanStart = -1;
//        int spanEnd = -1;
//        int docID = -1;
//        while (spans.nextDoc() != Spans.NO_MORE_DOCS) {
//            while (spans.nextStartPosition() != Spans.NO_MORE_POSITIONS) {
//                spanStart = spans.startPosition();
//                spanEnd = spans.endPosition();
//                docID = spans.docID();
//                i++;
//            }
//        }
//        assertEquals("should only be one matching span", 1, i);
//        assertEquals("doc id", trueDocID, docID);
//        assertEquals("span start", trueSpanStart, spanStart);
//        assertEquals("span end", trueSpanEnd, spanEnd);
//    }
}
