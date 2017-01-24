package it.emarolab.amor.examples;


import it.emarolab.amor.owlDebugger.Logger;

import java.util.List;
import java.util.Map;

import it.emarolab.amor.owlInterface.OWLLibrary;
import it.emarolab.amor.owlInterface.OWLReferences;
import it.emarolab.amor.owlInterface.OWLReferencesInterface;
import it.emarolab.amor.owlInterface.OWLReferencesInterface.OWLReferencesContainer;

public class Benchmarks {

    public static final String OWLREFERENCES_NAME = "refName";

    // todo: manage folder
    //public static final String ONTOLOGY_FILE_PATH = "files/benchmarks/A-box_dep0-uni0.owl";
    //public static final String ONTOLOGY_IRI_PATH = "file:benchmarks/A-box_dep0-uni0.owl"; // may depends on your file path as well

    // try also with TABox obtained by copy paste the TBox in a generated ABox (see comments on that file):
    public static final String ONTOLOGY_FILE_PATH = "files/benchmarks/T-A-box_dep0-uni0.owl";
    public static final String ONTOLOGY_IRI_PATH = "http://swat.cse.lehigh.edu/onto/univ-bench.owl";

    public static final String REASONER_FACTORY = OWLLibrary.REASONER_QUALIFIER_PELLET;
    public static final Boolean BUFFERING_REASONER = true; // if true you must to update manually the reasoner. Otherwise it synchronises itself any time is needed
    public static final Integer COMMAND = OWLReferencesContainer.COMMAND_LOAD_FILE;

    public static final Boolean BUFFERING_OWLMANIPULATOR = false; // if true you must to apply changes manually. Otherwise their are applied as soon as possible.

    public static final String ONTOLOGY_SAVING_PATH = ""; // todo: set and use

    private static Logger logger = new Logger(OWLRefEnquirerExample.class, true);

    public static void main(String[] args) {
        //Logger.LoggerFlag.resetAllLoggingFlags();// let disable verbose logging (this call may be delayed!!)

        // example of benchmark ontology loading (e.g.: LUBM, http://swat.cse.lehigh.edu/projects/lubm/)
        OWLReferences ontoRef = OWLReferencesContainer.newOWLReferences(OWLREFERENCES_NAME, ONTOLOGY_FILE_PATH, ONTOLOGY_IRI_PATH, REASONER_FACTORY, BUFFERING_REASONER, COMMAND);
        ontoRef.setOWLManipulatorBuffering(BUFFERING_OWLMANIPULATOR);

        // classical MOR query example
        // ATTENTION: you may not see those reasoning results from Protétè !!!!
        // ATTENTION: you may not see those reasoning result if the ontology contains only the TBox
        ontoRef.synchronizeReasoner();
        System.out.println( "\nExample: query all the students  " + OWLReferencesInterface.getOWLName(
               ontoRef.getIndividualB2Class( "Student")) + "\n");

        // todo: make a loop over universities (manage files)
        // todo: make a loop over departments (manage files)
        // todo: loop over query. 1) Crete a Class that contains a query (String) and department+university ID. 2) Create a List of it and 3) iterate.
        // todo: 1) log times, 2) save ontology and 3) cvs: "timeStamp[ms], quryID, time[ns], resultSize, resultMapToString;\n"
        // example of SPARQL (LUBM qery 10)
        String PREFIX = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>"
                + "PREFIX ub: <http://swat.cse.lehigh.edu/onto/univ-bench.owl#>"
                + "PREFIX d0u0: <http://www.Department0.University0.edu/>"; // !!!!!!!
        String SELECT = "SELECT ?X";
        String WHERE = " WHERE{"
                + "?X  rdf:type ub:Student ."
                + "?X  ub:takesCourse d0u0:GraduateCourse0 ."
                + "}";
        List<Map<String, String>> result = ontoRef.sparql2Msg(PREFIX + SELECT + WHERE, 100L);
        logger.addDebugString("SPARQL results: " + result);

        // todo: cheep track of changes in queries and benchmarks in the 'file' directory
    }
}
