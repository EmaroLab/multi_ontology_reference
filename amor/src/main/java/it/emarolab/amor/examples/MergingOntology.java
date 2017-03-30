package it.emarolab.amor.examples;

import it.emarolab.amor.owlDebugger.Logger;
import it.emarolab.amor.owlInterface.OWLReferences;
import it.emarolab.amor.owlInterface.OWLReferencesInterface;
import java.util.List;
import java.util.Map;

public class MergingOntology {

    public static final Boolean BUFFERING_REASONER = true; // if true you must to update manually the reasoner. Otherwise it synchronises itself any time is needed
    public static final Boolean BUFFERING_MANIPULATOR = true; // if true you must to apply changes manually. Otherwise their are applied as soon as possible.

    public static final String ONTOLOGY_MARGED_SAVING_PATH = ""; // todo: set and use
    private static Logger logger = new Logger(OWLRefEnquirerExample.class, true);

    public static void main(String[] args) {

        //Logger.LoggerFlag.resetAllLoggingFlags();// let disable verbose logging (this call may be delayed!!)

        // load TBOX
        String TBOX_IRI = "http://www.cs.ox.ac.uk/isg/tools/RDFox/2014/AAAI/input/UOBM/owl/UOBM.owl";
        String TBOX_FILE = "files/benchmarks/LUBM-UOB/preload_generated_uobm/merged.owl";
        OWLReferences tRef = OWLReferencesInterface.OWLReferencesContainer.newOWLReferenceFromWeb( "T-BOX", TBOX_FILE, TBOX_IRI, BUFFERING_REASONER);

        // load uni2
        String ABOX0_FILE = "/home/matteo/NetBeansProjects/aMORAnalysis/multi_ontology_reference/files/benchmarks/LUBM-UOB/preload_generated_uobm/univ0.owl";
        String ABOX0_IRI = "file:" + ABOX0_FILE;
        OWLReferences u0Ref = OWLReferencesInterface.OWLReferencesContainer.newOWLReferenceFromWeb( "UNI0", ABOX0_FILE, ABOX0_IRI, BUFFERING_REASONER);

        // load uni1
        String ABOX1_FILE = "/home/matteo/NetBeansProjects/aMORAnalysis/multi_ontology_reference/files/benchmarks/LUBM-UOB/preload_generated_uobm/univ1.owl";
        String ABOX1_IRI = "file:" + ABOX1_FILE;
        OWLReferences u1Ref = OWLReferencesInterface.OWLReferencesContainer.newOWLReferenceFromWeb( "UNI1", ABOX1_FILE, ABOX1_IRI, BUFFERING_REASONER);

        // marge uni1 e uni0 in Tbox
        tRef.getOWLManager().addAxioms( tRef.getOWLOntology(), u0Ref.getOWLOntology().axioms());
        tRef.getOWLManager().addAxioms( tRef.getOWLOntology(), u1Ref.getOWLOntology().axioms());
        System.out.println( "Test a query person: " + tRef.getIndividualB2Class("Person"));
        System.out.println("IRI: " + tRef.getOWLClass("Person").getIRI());
        //tRef.saveOntology();
        
        String PREFIX = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>"
                + "PREFIX ub: <http://swat.cse.lehigh.edu/onto/univ-bench.owl#>"
                + "PREFIX d0u0: <http://www.Department0.University0.edu/>"; // !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
        String SELECT = "SELECT ?X";
        String WHERE = " WHERE{"
                + "?X  rdf:type ub:Student . "
                + "?X  ub:takesCourse d0u0:GraduateCourse0"
                + "}";
        
        String QUERY9 = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> " +
                            "PREFIX ub: <http://swat.cse.lehigh.edu/onto/univ-bench.owl#> " +
                            "SELECT ?X ?Y ?Z " +
                            "WHERE " +
                            "{?X rdf:type ub:Student . " +
                            "?Y rdf:type ub:Faculty . " +
                            "?Z rdf:type ub:Course . " +
                            "?X ub:advisor ?Y . " +
                            "?Y ub:teacherOf ?Z . " +
                            "?X ub:takesCourse ?Z}";
        
        String QUERY14 = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> " +
                            "PREFIX ub: <http://swat.cse.lehigh.edu/onto/univ-bench.owl#> " +
                            "SELECT ?X " +
                            "WHERE {?X rdf:type ub:UndergraduateStudent}";
        
        String query = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> "
                + "PREFIX ub: <http://www.cs.ox.ac.uk/isg/tools/RDFox/2014/AAAI/input/UOBM/owl/UOBM.owl#> "
                + "SELECT ?X "
                + "WHERE "
                + "{?X rdf:type ub:GraduateStudent}";
                
        tRef.synchronizeReasoner();
        
        List<Map<String, String>> result = tRef.sparql2Msg(query, -1L);
        logger.addDebugString("SPARQL results: " + result);
        
    }

}
