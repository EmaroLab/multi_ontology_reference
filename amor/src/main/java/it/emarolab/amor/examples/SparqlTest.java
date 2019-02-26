package it.emarolab.amor.examples;

import com.clarkparsia.pellet.owlapi.PelletReasoner;
import com.clarkparsia.pellet.sparqldl.jena.SparqlDLExecutionFactory;
import it.emarolab.amor.owlInterface.OWLReferences;
import it.emarolab.amor.owlInterface.OWLReferencesInterface;
import org.apache.jena.query.*;
import org.apache.jena.rdf.model.InfModel;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.mindswap.pellet.KnowledgeBase;
import org.mindswap.pellet.jena.PelletInfGraph;
import org.semanticweb.owlapi.formats.TurtleDocumentFormat;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;


import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

public class SparqlTest {

    // ontology configurations
    public static final String OWLREFERENCES_NAME = "refName";
    public static final String ONTOLOGY_FILE_PATH = "/home/luca-bnc/sparqlTest.owl";
    public static final String ONTOLOGY_IRI_PATH = "http://www.semanticweb.org/emaroLab/luca-buoncompagni/sit";
    public static final Boolean BUFFERING_REASONER = false; // if true you must to update manually the reasoner. Otherwise it synchronizes itself any time is needed
    public static final Boolean BUFFERING_OWLMANIPULATOR = false; // if true you must to apply changes manually. Otherwise their are applied as soon as possible.


    public static void main(String[] args) {
        OWLReferences ontoRef = OWLReferencesInterface.OWLReferencesContainer.newOWLReferenceFromFileWithPellet( OWLREFERENCES_NAME, ONTOLOGY_FILE_PATH, ONTOLOGY_IRI_PATH, BUFFERING_REASONER);
        ontoRef.setOWLManipulatorBuffering( BUFFERING_OWLMANIPULATOR); // if not specified, default value=false {@link OWLManipulator#DEFAULT_CHANGE_BUFFERING}


        String query = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> "
            + "PREFIX owl: <http://www.w3.org/2002/07/owl#> "
            + "PREFIX sit: <http://www.semanticweb.org/emaroLab/luca-buoncompagni/sit#> "
            + "SELECT ?min ?cls ?p WHERE {"
            + "sit:TestScene (owl:equivalentClass|(owl:intersectionOf/rdf:rest*/rdf:first))* ?restriction ."
            + "?restriction owl:minQualifiedCardinality ?min ."
                + "?restriction owl:onClass ?cls ."
                + "?restriction owl:onProperty ?p"
            + "}";


        List<QuerySolution> r = ontoRef.sparql(query);
    }

}
