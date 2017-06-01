package it.emarolab.amor.examples;

import it.emarolab.amor.owlInterface.OWLLibrary;
import it.emarolab.amor.owlInterface.OWLReferences;
import it.emarolab.amor.owlInterface.OWLReferencesInterface.OWLReferencesContainer;
 
public class OWLRefUtilityExample {

    public static final String OWLREFERENCES_NAME = "refName";
    public static final String ONTOLOGY_FILE_PATH = "amor/files/ontologies/utility.owl";
    public static final String ONTOLOGY_IRI_PATH = "http://www.semanticweb.org/luca-buoncompagni/aMor/examples";
    public static final String REASONER_FACTORY = OWLLibrary.REASONER_DEFAULT;
    public static final Boolean BUFFERING_REASONER = true; // if true you must to update manually the reasoner. Otherwise it synchronises itself any time is needed
    public static final Integer COMMAND = OWLReferencesContainer.COMMAND_LOAD_FILE;
    public static final Boolean BUFFERING_OWLMANIPULATOR = false; // if true you must to apply changes manually. Otherwise their are applied as soon as possible.

    public static void main(String[] args) {
        // 1) [LOAD_FILE_] load the ontology from file with the more general constructor
        OWLReferences ontoRef = OWLReferencesContainer.newOWLReferences( OWLREFERENCES_NAME, ONTOLOGY_FILE_PATH,
                ONTOLOGY_IRI_PATH, REASONER_FACTORY, BUFFERING_REASONER, COMMAND);
        ontoRef.setOWLManipulatorBuffering( BUFFERING_OWLMANIPULATOR);

        // TODO : load file/web UN/MOUNTED are implemented just on the ros service aRMOR.

        // 2) [APPLY__] to apply the changes into an ontology
        ontoRef.applyOWLManipulatorChanges();

        // 3) [REASON__] to call the reasoner
        ontoRef.synchronizeReasoner();

        // 4) [SAVE__] to save ontology
        String path = "";
        if( path.isEmpty())
            ontoRef.saveOntology();
        else ontoRef.saveOntology( path);

        // 5)  [SAVE_INFERENCES_] to save ontology
        if( path.isEmpty())
            ontoRef.saveOntology( true);
        else ontoRef.saveOntology( true, "exported_" + path);
    }
}
