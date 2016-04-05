package it.cnr.ilc.lc.clavius.search;

import com.google.gson.Gson;
import it.cnr.ilc.lc.clavius.search.entity.Annotation;
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

    private static List<Annotation> results;
    private static String jsonResult;

    public static void main(String[] args) throws Exception {

        createEntity(false);
        //results = search("BOBBE2");
        //results = conceptSearch("Persona Pippo Lavoratore ");
        results = searchQueryParse("Pipp*");
        toJson(results);
    }

    private static void createEntity(boolean flag) throws InterruptedException {
        if (flag) {
            EntityManagerFactory entityManagerFactory = Persistence.createEntityManagerFactory("clavius");
            EntityManager entityManager = entityManagerFactory.createEntityManager();
            FullTextEntityManager fullTextEntityManager = org.hibernate.search.jpa.Search.getFullTextEntityManager(entityManager);

            entityManager.getTransaction().begin();
            Annotation a = new Annotation();
            a.setMatched("BOBBE2");
            a.setType("lexica");
            a.setLeftContext("testo prima di bobbe malle");
            a.setRightContext("testo dopo bobbe malle");
            a.setConcepts("Persona Persona-adulta Lavoratore Lavoratore-autonomo Pippo");
            a.setIdLetter(Long.valueOf(319));
            a.setPageNum(Long.valueOf(3));
            a.setIdNeo4j(123456l);

            entityManager.persist(a);
            fullTextEntityManager.createIndexer().startAndWait();
            fullTextEntityManager.flushToIndexes();
            entityManager.getTransaction().commit();
            entityManager.close();
            entityManagerFactory.close();

            System.err.println("createEntity: " + a);

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

        // convert java object to JSON format,
        // and returned as JSON formatted string
        String json = gson.toJson(loa);

        System.err.println("JSON result: " + json);

    }
}
