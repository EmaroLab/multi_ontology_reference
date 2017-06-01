package it.emarolab.amor.examples;


import it.emarolab.amor.owlDebugger.Logger;
import it.emarolab.amor.owlInterface.OWLLibrary;
import it.emarolab.amor.owlInterface.OWLReferences;
import it.emarolab.amor.owlInterface.OWLReferencesInterface.OWLReferencesContainer;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

import java.io.File;


public class Benchmarks {

    // todo: manage folders
    // example with only A-box
    public static final String ONTOLOGY_FILE_PATH = "files/benchmarks/LUBM-UOB/preload_generated_uobm/univ0.owl";//"files/benchmarks/A-box_dep0-uni0.owl";
    public static final String ONTOLOGY_IRI_PATH = "file:/home/bubx/ros_ws/src/ontology_reference/multi_ontology_reference/files/benchmarks/LUBM-UOB/preload_generated_uobm/univ0.owl";
    //"file:benchmarks/A-box_dep0-uni0.owl"; // may depends on your file path as well


    public static final String ONTOLOGY_FILE_PATH_1 = "files/benchmarks/LUBM-UOB/preload_generated_uobm/univ1.owl";//"files/benchmarks/A-box_dep0-uni0.owl";
    public static final String ONTOLOGY_IRI_PATH_1 = "file:/home/bubx/ros_ws/src/ontology_reference/multi_ontology_reference/files/benchmarks/LUBM-UOB/preload_generated_uobm/univ1.owl";
    //"file:benchmarks/A-box_dep0-uni0.owl"; // may depends on your file path as well




    // try also with TA-box obtained by copy paste the TBox in a generated ABox (see comments on that file):
    //public static final String ONTOLOGY_FILE_PATH = "files/benchmarks/T-A-box_dep0-uni0.owl";
    //public static final String ONTOLOGY_IRI_PATH = "http://swat.cse.lehigh.edu/onto/univ-bench.owl";



    public static final String OWLREFERENCES_NAME = "refName";
    public static final Integer COMMAND = OWLReferencesContainer.COMMAND_LOAD_FILE;
    public static final String REASONER_FACTORY = OWLLibrary.REASONER_QUALIFIER_PELLET;



    public static final Boolean BUFFERING_REASONER = true; // if true you must to update manually the reasoner. Otherwise it synchronises itself any time is needed
    public static final Boolean BUFFERING_MANIPULATOR = false; // if true you must to apply changes manually. Otherwise their are applied as soon as possible.
    public static final String ONTOLOGY_SAVING_PATH = ""; // todo: set and use
    private static final Long SPARQL_TIMEOUT = -1L; //[ms] set to <0 to disable time out.
    private static Logger logger = new Logger(OWLRefEnquirerExample.class, true);

    public static void main(String[] args) {

        //Logger.LoggerFlag.resetAllLoggingFlags();// let disable verbose logging (this call may be delayed!!)

        // example of benchmark ontology loading (e.g.: LUBM, http://swat.cse.lehigh.edu/projects/lubm/)
        OWLReferences ontoRef = OWLReferencesContainer.newOWLReferences(OWLREFERENCES_NAME, ONTOLOGY_FILE_PATH, ONTOLOGY_IRI_PATH, REASONER_FACTORY, BUFFERING_REASONER, COMMAND);
        ontoRef.setOWLManipulatorBuffering(BUFFERING_MANIPULATOR);


        try {
            ontoRef.getOWLManager().loadOntologyFromOntologyDocument(new File(ONTOLOGY_FILE_PATH_1));//.loadOntology( IRI.create( ONTOLOGY_IRI_PATH_1));
        } catch (OWLOntologyCreationException e) {
            e.printStackTrace();
        }
        ontoRef.saveOntology("/home/bubx/Desktop/aaa.owl");


//
//        // classical MOR query example
//        // ATTENTION: you may not see those reasoning results from Protétè !!!!
//        // ATTENTION: you may not see empty result if the ontology contains only the TBox
//        ontoRef.synchronizeReasoner();
//        System.out.println( "\nExample: query all the students  " + OWLReferencesInterface.getOWLName(
//               ontoRef.getIndividualB2Class( "Student")) + "\n");
//
//
//
//
//        /* todo:
//            1 make a loop over universities and departments (manage folder and save ontology)
//                        (A) create a class that describes:
//                            - department and university  (int,int -> String)
//                            - saving path (describe once where/how the test should be saved.)
//                                          (for example: use file name and time stamp to generate folders and logging file name)
//                            - a list of queries (see ArrayList<(B)>)
//                            - a method to iterate over the list of queries, evaluate them and save results.
//               1.1 make a loop over queries (produce cvs: "timeStamp[ms], queryID, time[ns], resultSize, resultMapToString;\n").
//                        (B) Crete a class 'LUBMTest' that describes:
//                            - a query text (String),
//                            - a call for the query (manage here once the time logging mechanism).
//                            - a csv string getter (manage here once how it is produced)
//                                                  (it could be returned from the previous method)
//        */
//
//
//
//
//        // example of SPARQL (LUBM query 10)
//        String PREFIX = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>"
//                + "PREFIX ub: <http://swat.cse.lehigh.edu/onto/univ-bench.owl#>"
//                + "PREFIX d0u0: <http://www.Department0.University0.edu/>"; // !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
//        String SELECT = "SELECT ?X";
//        String WHERE = " WHERE{"
//                + "?X  rdf:type ub:Student ."
//                + "?X  ub:takesCourse d0u0:GraduateCourse0 ."
//                + "}";
//        List<Map<String, String>> result = ontoRef.sparql2Msg(PREFIX + SELECT + WHERE, SPARQL_TIMEOUT);
//        logger.addDebugString("SPARQL results: " + result);
//
//

    }
}
