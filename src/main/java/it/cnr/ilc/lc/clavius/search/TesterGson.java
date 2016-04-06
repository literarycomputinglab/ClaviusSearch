package it.cnr.ilc.lc.clavius.search;

import com.google.gson.Gson;
import it.cnr.ilc.lc.clavius.search.entity.Annotation;
import it.cnr.ilc.lc.clavius.search.entity.TEADocument;
import java.util.ArrayList;
import java.util.List;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author angelo
 */
public class TesterGson {

    public static void main(String[] args) throws Exception {

        TEADocument teadoc = new TEADocument();
        teadoc.code = "\\n<Oxygonium vero, quod tres habet acutos angulos>(s1). Omne\\n<triangulum Oxygonium>(t1), sive <acutangulum>(t2), potest esse \\nvel <aequilaterum>(t3), vel <isosceles>(t4), vel <scalenum>(t5), \\nut cernere licet in <triangulis>(e1), quae in speciebus prioris \\n\\ntest\\n\\ndivisionis spectanda exhibuimus, ne eadem hic frustra repetantur.\\n+++\\n(s1) lvont:language lexvo:iso639-3/lat\\n(s1) lvont:translation \\\"...\\\"\\n(t1) rdfs:seeAlso cll:math/triangulum_oxygonium\\n(t2) rdfs:seeAlso cll:math/triangulum_acutangulum\\n(t3) rdfs:seeAlso cll:math/triangulum_aequilaterum\\n(t4) rdfs:seeAlso cll:math/triangulum_isosceles\\n(t5) rdfs:seeAlso cll:math/triangulum_scalenum\\n(e1) rdfs:seeAlso dbr:Triangle\\n(e1) foaf:page https://en.wikipedia.org/wiki/Triangle\\n+++\\n\\n\\n\\n";
        teadoc.id = 3079l;
        teadoc.idLetter = "319";
        teadoc.text = "Oxygonium vero, quod tres habet acutos angulos. Omne\n"
                + "triangulum Oxygonium, sive acutangulum, potest esse \n"
                + "vel aequilaterum, vel isosceles, vel scalenum, \n"
                + "ut cernere licet in triangulis, quae in speciebus prioris ";
        teadoc.name = "name";
        List<TEADocument.Triple> lot = new ArrayList<>();
        lot.add(new TEADocument.Triple(18, 25, "(s1)", "rdfs:seeAlso", "cll:math/triangulum_acutangulum"));
        lot.add(new TEADocument.Triple(38, 45, "(s2)", "rdfs:seeAlso", "cll:math/triangulum_oxygonium"));

        teadoc.triples = lot;
        parseJson(createJson(teadoc));

    }

    private static String createJson(TEADocument teadoc) {

        Gson gson = new Gson();

        // convert java resourceObject to JSON format,
        // and returned as JSON formatted string
        String json = gson.toJson(teadoc);

        System.err.println("JSON result: " + json);
        return json;

    }

    private static TEADocument parseJson(String json) {

        Gson gson = new Gson();

        // convert java resourceObject to JSON format,
        // and returned as JSON formatted string
        TEADocument teadoc = gson.fromJson(json, TEADocument.class);

        System.err.println("TEADoc result: " + teadoc);

        return teadoc;

    }

}
