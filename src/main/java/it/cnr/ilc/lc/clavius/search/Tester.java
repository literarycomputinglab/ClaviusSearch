package it.cnr.ilc.lc.clavius.search;

import it.cnr.ilc.lc.clavius.search.entity.Annotation;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import org.hibernate.search.jpa.FullTextEntityManager;
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

        createEntity(true);
        results = search("ciao");
        jsonResult = results.toString(); // da fare veramente il json
        System.err.println(jsonResult);

    }

    private static void createEntity(boolean flag) throws InterruptedException {
        if (flag) {
            EntityManagerFactory entityManagerFactory = Persistence.createEntityManagerFactory("clavius");
            EntityManager entityManager = entityManagerFactory.createEntityManager();
            FullTextEntityManager fullTextEntityManager = org.hibernate.search.jpa.Search.getFullTextEntityManager(entityManager);

            entityManager.getTransaction().begin();
            Annotation a = new Annotation();
            a.setText("ciao come stai?");
            a.setType("lexica");
            entityManager.persist(a);
            fullTextEntityManager.createIndexer().startAndWait();
            fullTextEntityManager.flushToIndexes();
            entityManager.getTransaction().commit();
            entityManager.close();
            entityManagerFactory.close();

            System.err.println(a);

        }

    }

    private static List<Annotation> search(String w) {
        EntityManagerFactory entityManagerFactory = Persistence.createEntityManagerFactory("clavius");
        EntityManager entityManager = entityManagerFactory.createEntityManager();

        FullTextEntityManager fullTextEntityManager = org.hibernate.search.jpa.Search.getFullTextEntityManager(entityManager);

        entityManager.getTransaction().begin();

        QueryBuilder qb = fullTextEntityManager.getSearchFactory().buildQueryBuilder().forEntity(Annotation.class).get();
        org.apache.lucene.search.Query query = qb
                .keyword()
                .onField("text")
                .matching(w)
                .createQuery();

        javax.persistence.Query persistenceQuery
                = fullTextEntityManager.createFullTextQuery(query, Annotation.class);
        List<Annotation> result = persistenceQuery.getResultList();

        for (Annotation a : result) {
            System.err.println(a);
        }

        entityManager.getTransaction().commit();
        fullTextEntityManager.flushToIndexes();
        fullTextEntityManager.clear();
        entityManager.close();
        entityManagerFactory.close();

        //fullTextEntityManager.close(); // attenzione se resta aperto non indicizza lucene
        return result;
    }

}
