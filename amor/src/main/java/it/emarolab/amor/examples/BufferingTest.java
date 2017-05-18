package it.emarolab.amor.examples;

import it.emarolab.amor.owlInterface.OWLReferences;
import it.emarolab.amor.owlInterface.OWLReferencesInterface.OWLReferencesContainer;
import org.semanticweb.owlapi.model.OWLLiteral;

public class BufferingTest {

    // ontology configurations
    public static final String OWLREFERENCES_NAME = "refName";
    public static final String ONTOLOGY_FILE_PATH = "files/ontologies/ontology_manipulation.owl";
    public static final String ONTOLOGY_IRI_PATH = "http://www.semanticweb.org/luca-buoncompagni/aMor/examples";
    public static final Boolean BUFFERING_REASONER = true; // if true you must to update manually the reasoner. Otherwise it synchronizes itself any time is needed
    public static final Boolean BUFFERING_OWLMANIPULATOR = true; // if true you must to apply changes manually. Otherwise their are applied as soon as possible.

    // ontological entities name
    public static final Integer INITIAL_CNT = 3;
    public static final String CNT_IND = "Counter"; // this is an individual with cares about the new ID to assign to X_IND
    public static final String CNT_DATAPROP = "hasMaxCount";
    public static final String X1_IND = "Ind1"; // this is an individual which must have a unique (in the ontology w.r.t. CNT_IND) identifier
    public static final String X2_IND = "Ind2"; // this is an individual which must have a unique (in the ontology w.r.t. CNT_IND) identifier
    public static final String X3_IND = "Ind3"; // this is an individual which must have a unique (in the ontology w.r.t. CNT_IND) identifier
    public static final String X_DATAPROP = "hasId";

    public static void main(String[] args) {
        // 1) let create new ontology with default reasoner (Pellet) and start GUI result visualisation
        OWLReferences ontoRef = OWLReferencesContainer.newOWLReferencesCreated( OWLREFERENCES_NAME, ONTOLOGY_FILE_PATH, ONTOLOGY_IRI_PATH, BUFFERING_REASONER);
        ontoRef.setOWLManipulatorBuffering( BUFFERING_OWLMANIPULATOR); // if not specified, default value=false {@link OWLManipulator#DEFAULT_CHANGE_BUFFERING}

        // 2) add base ontological entities
        ontoRef.addIndividual( CNT_IND);
        ontoRef.addDataPropertyB2Individual( CNT_IND, CNT_DATAPROP, INITIAL_CNT);
        ontoRef.addIndividual( X1_IND);
        ontoRef.addIndividual( X2_IND);
        ontoRef.addIndividual( X3_IND);
        applyChanges( ontoRef); // this considers BUFFERING_MANIPULATOR flag

        // 3) get the starting cnt
        Integer javaCnt = 0;
        OWLLiteral ontoCnt = ontoRef.getOnlyDataPropertyB2Individual( CNT_IND, CNT_DATAPROP);
        if( ontoCnt != null)
            javaCnt = Integer.valueOf( ontoCnt.getLiteral());

        // 4) assign a new id for couple of times and apply owl manipulator changes (if flag is true)
        assignIncrementalId( ontoRef, X1_IND, javaCnt);              // id1 = INITIAL_CNT,         cnt = INITIAL_CNT + 1;
        assignIncrementalId( ontoRef, X2_IND, javaCnt);              // id2 = INITIAL_CNT,         cnt = INITIAL_CNT + 1;
        javaCnt = assignIncrementalId( ontoRef, X3_IND, javaCnt);    // id3 = INITIAL_CNT,         cnt = INITIAL_CNT + 1;
        applyChanges( ontoRef); // this considers BUFFERING_MANIPULATOR flag

        // 5) do something similar to (3) for testing purposes
        assignIncrementalId( ontoRef, X1_IND, javaCnt);              // id1 = INITIAL_CNT + 1,     cnt = INITIAL_CNT + 2;
        assignIncrementalId( ontoRef, X2_IND, javaCnt);              // id2 = INITIAL_CNT + 1,     cnt = INITIAL_CNT + 2;
        javaCnt = assignIncrementalId( ontoRef, X3_IND, javaCnt);    // id3 = INITIAL_CNT + 1,     cnt = INITIAL_CNT + 2;
        applyChanges( ontoRef); // this considers BUFFERING_MANIPULATOR flag

        // 6) do something similar to (3) for testing purposes
        assignIncrementalId( ontoRef, X1_IND, javaCnt);                // id1 = INITIAL_CNT + 2,     cnt = INITIAL_CNT + 3;
        assignIncrementalId( ontoRef, X2_IND, javaCnt);                // id2 = INITIAL_CNT + 2,     cnt = INITIAL_CNT + 3;
        javaCnt = assignIncrementalId( ontoRef, X3_IND, javaCnt);    // id3 = INITIAL_CNT + 2,     cnt = INITIAL_CNT + 3;
        applyChanges( ontoRef); // this considers BUFFERING_MANIPULATOR flag

        // 7) reason (if it is buffer)
        reason( ontoRef);

        // 8) show results
        ontoRef.printOntologyOnConsole();

        System.err.println( ontoRef.getIndividualB2Thing());

        ontoRef.saveOntology();

    }

    // procedure to assign a new id to X_IND and update the counter to point to an ID not assigned to any individuals
    private static Integer assignIncrementalId( OWLReferences ontoRef, String ind, Integer cnt){
        // get the actual value of the counter
        OWLLiteral ontoCnt = ontoRef.getOWLLiteral( cnt);
        OWLLiteral newOntoCount = ontoRef.getOWLLiteral( cnt + 1);
        // get the actual value of the id
        OWLLiteral ontoId = ontoRef.getOnlyDataPropertyB2Individual( ind,    X_DATAPROP);
        // update the new id to the individual by overwriting the old value
        ontoRef.replaceDataProperty( ontoRef.getOWLIndividual( ind), ontoRef.getOWLDataProperty( X_DATAPROP),    ontoId, ontoCnt);
        // get the actual cnt
        OWLLiteral oldCnt = ontoRef.getOnlyDataPropertyB2Individual( CNT_IND, CNT_DATAPROP);
        // update the counter individual by overwriting the old value
        ontoRef.replaceDataProperty( ontoRef.getOWLIndividual( CNT_IND), ontoRef.getOWLDataProperty( CNT_DATAPROP),    oldCnt, newOntoCount);
        return cnt + 1;
    }

    // apply changes w.r.t. to buggering flag
    private static void applyChanges( OWLReferences ontoRef){
        if( BUFFERING_OWLMANIPULATOR)
            ontoRef.applyOWLManipulatorChanges();
    }

    // update the state of the reasoner w.r.t. buffering flag
    private static void reason( OWLReferences ontoRef){
        if( BUFFERING_REASONER)
            ontoRef.synchronizeReasoner();
    }
}
