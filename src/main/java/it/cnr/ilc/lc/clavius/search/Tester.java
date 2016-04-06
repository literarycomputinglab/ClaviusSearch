package it.cnr.ilc.lc.clavius.search;

import com.google.gson.Gson;
import it.cnr.ilc.lc.clavius.search.entity.Annotation;
import it.cnr.ilc.lc.clavius.search.entity.TEADocument;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
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
            + "\"idLetter\":\"319\","
            + "\"triples\":[{\"start\":18,\"end\":25,\"subject\":\"(s1)\",\"predicate\":\"rdfs:seeAlso\",\"object\":\"cll:math/triangulum_acutangulum\"},{\"start\":38,\"end\":45,\"subject\":\"(s2)\",\"predicate\":\"rdfs:seeAlso\",\"object\":\"cll:math/triangulum_oxygonium\"}]}";
    private static List<Annotation> results;
    private static String jsonResult;

    public static void main(String[] args) throws Exception {

        createEntity(false);
        //results = search("BOBBE2");
        //results = conceptSearch("Persona Pippo Lavoratore ");
        results = searchQueryParse("lin?");
        toJson(results);
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
            String idLetter = teadoc.idLetter;
            List<TEADocument.Triple> triples = teadoc.triples;

            for (TEADocument.Triple triple : triples) {
                Annotation a = new Annotation();
                a.setLeftContext(plainText.substring(triple.start > 30 ? triple.start - 30 : 0, triple.start));
                a.setRightContext(plainText.substring(triple.end, triple.end + 30 < plainText.length() ? triple.end + 30 : plainText.length()));
                a.setIdLetter(Long.valueOf(idLetter));
                a.setConcepts("concepts-" + triple.object.substring(triple.object.lastIndexOf("/"))); //@FIX triple.object sara' la chiave di accesso alla mappa dei concetti
                a.setType("denotes-" + triple.object.substring(triple.object.lastIndexOf("/")));
                a.setResourceObject(triple.object);
                a.setIdNeo4j(teadoc.id);

                entityManager.persist(a);
                System.err.println("createEntity: " + a);

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
                "concepts",
                fullTextEntityManager.getSearchFactory().getAnalyzer(Annotation.class)
        );

        org.apache.lucene.search.Query luceneQuery;
        try {
            luceneQuery = parser.parse(query);
            FullTextQuery fullTextQuery
                    = fullTextEntityManager.createFullTextQuery(luceneQuery, Annotation.class);

            result = fullTextQuery.getResultList();

        } catch (ParseException ex) {
            System.err.println("AAAAAAAAAAAAHHHHHHHHHH");
            Logger.getLogger(Tester.class.getName()).log(Level.SEVERE, null, ex);
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
                .onField("concepts")
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
}
