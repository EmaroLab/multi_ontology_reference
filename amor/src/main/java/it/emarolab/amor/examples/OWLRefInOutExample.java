package it.emarolab.amor.examples;

import it.emarolab.amor.owlInterface.OWLLibrary;
import it.emarolab.amor.owlInterface.OWLReferences;
import it.emarolab.amor.owlInterface.OWLReferencesInterface;
import it.emarolab.amor.owlInterface.OWLReferencesInterface.OWLReferencesContainer;
import it.emarolab.amor.owlInterface.ReasonerExplanator;

/**
 * Project: aMOR <br>
 * File: .../src/aMOR.examples/OWLRefInOutExample.java <br>
 *  
 * @author Buoncompagni Luca <br><br>
 * DIBRIS emaroLab,<br> 
 * University of Genoa. <br>
 * Feb 11, 2016 <br>
 * License: GPL v2 <br><br>
 *  
 * <p>
 * This class implements a basic tutorial as an example for showing aMor API.
 * In particular, it shows how to create and load (from web or file) an ontology.
 * Moreover, it shows how to save by also exporting all the inferences.
 * Furthermore, it given an example of how to instances of the objects
 * that manipulate ontologies are managed from the system for a thread save
 * multi-ontological system implementation. 
 * Last but not the least, it shows also how to interact with different Reasoner
 * (also with custom once).
 * </p>
 *
 */
public class OWLRefInOutExample {

    public static final String ONTOLOGY_IRI_PATH = "http://www.semanticweb.org/luca-buoncompagni/aMor/examples";
    public static final String ONTOLOGY_FILE_BASE = "files/ontologies/";

    public static void main(String[] args) throws Throwable {
        // 1) create an new ontology by using the default reasoner (it has also explanation (no more with api 5.0???)) and save it to file
        String ontoRefName1 = "ontoRef1"; // this must be a unique identifier for all the references you what to create into the system
        String filePath1 = ONTOLOGY_FILE_BASE + "test_creation.owl";
        OWLReferences ontoRef1 = OWLReferencesContainer.newOWLReferencesCreated( ontoRefName1, filePath1, ONTOLOGY_IRI_PATH, false);
        // save the ontology in the specified file path
        ontoRef1.saveOntology();

        // 2) let open an ontology from web with Hermit reasoner (it does not have explanation "ReasonerExplanation" implemented yet) and save it to file
        String ontoRefName2 = "ontoRef2"; // this must be a unique identifier for all the references you what to create into the system
        String filePath2 = "http://protege.stanford.edu/ontologies/pizza/pizza.owl";
        String pizzaOntologyIri = "http://protege.stanford.edu/ontologies/pizza/pizza.owl";
        OWLReferences ontoRef2 = OWLReferencesContainer.newOWLReferenceFromWebWithHermit( ontoRefName2, filePath2, pizzaOntologyIri, true);
        // print the ontlogy for debugging
        ontoRef2.printOntologyOnConsole();
        // if you do not change the file path it will use filePath2 that may is not what you are looking for in the case of loading from web.
        ontoRef2.saveOntology( ONTOLOGY_FILE_BASE + "test_loadFromWeb.owl");

/*        FACT IS NOT WORKING JET WITH OLW API 5
        // 3) let now open the pizza ontology from file with Fact reasoner
        // (you must to install the reasoner on your machine see lib folder or https://code.google.com/archive/p/factplusplus/downloads)
        // specifically you must to generate (by compiling) the file libFaCTPlusPlusJNI.so and put it in the java.library.path of your machine (/usr/lib for ubuntu)
        String ontoRefName3 = "ontoRef3"; // this must be a unique identifier for all the references you what to create into the system
        Boolean bufferingReasoner = true; // reasoning is not synchronised and must be called manually
        OWLReferences ontoRef3 = OWLReferencesContainer.newOWLReferenceFromWebWithFact( ontoRefName3, filePath2, pizzaOntologyIri, bufferingReasoner);
        ontoRef3.synchronizeReasoner(); // perform reasoning
        // export all the inferences made by the reasoner in the saving file
        ontoRef3.saveOntology( true, ONTOLOGY_FILE_BASE + "test_loadFromFile.owl"); // change also the saving path with respect to the one given on constructor
*/

        // I have't found again the jar to be included in order to instantiate
        // au.csiro.snorocket.owlapi3.SnorocketReasonerFactory
        // so at this stage that reasoner would not work.
        // But if you find the jar and you include in your project the system is already ready to handle it.
        new MyReasonerInterface( "customRef", ONTOLOGY_FILE_BASE + "custom_ref.owl", OWLReferencesContainer.COMMAND_CREATE);

        // to note that the system stores all the instances of the OWLReferenes
        // that are instantiate in the system by using the referencesName field as unique qualifier.
        System.out.println( "1)\n\n" + OWLReferencesContainer.to_string());
        // you cannot instantiate another References with the same name.
        // Furthermore, you can remove one instance from the system
        ontoRef1.finalize();
        ontoRef1 = null; // for safity do not use this object anymore
        System.out.println( "2)\n\n" + OWLReferencesContainer.to_string());
        // Also, since OWLReferencesContainer is a static class you can refer
        // to an OWL References instanc in any part of your project by using its name.
        OWLReferencesInterface ontoRef2Bis = OWLReferencesContainer.getOWLReferences( ontoRefName2);
        if( ontoRef2.equals( ontoRef2Bis))
            System.out.print( "3)\n\n retrieve ontoRef2 again");

    }
}

//By the way the system uses java reflection for reasoner instantiation,
// so you can use all the implementation you wish;
// as long as they it is on the java build path of you project
// and it is an implementation of OWLReasonerFactory (and so also OWLReasoner). 
// Practically:
class MyReasonerInterface extends OWLReferences{

    // the java qualifier to whatever reasoning factory you like
    public static final String REASONER_FACTORY = "org.semanticweb.HermiT.Reasoner$ReasonerFactory";
    public static final Boolean USE_BUFFERING_REASONER = true;

    protected MyReasonerInterface(String referenceName, String filePath, Integer command) {
        // as an example I am using static final values for initialisation
        // on constructor but you may want to do something smarter.
        super(referenceName, filePath, OWLRefInOutExample.ONTOLOGY_IRI_PATH,
                REASONER_FACTORY, USE_BUFFERING_REASONER, command);

    }

    @Override
    protected synchronized void setOWLReasoner(String reasonerFactoryName,
                                               boolean buffering, String lggingName) {
        // change this call if you want to modify how the library instantiates your reasoner.
        super.setOWLReasoner(reasonerFactoryName, buffering, lggingName);

        // do not forgot to add debugging features by implementing the above class
        // and set to your implementation as:
        MyReasonerExplanator explanator = new MyReasonerExplanator( this);
        this.setReasonerExplanator( explanator);
    }

    @Override
    // otherwise you may to want to do the above operations also for the method
    // setDefaultReasoner. Note that here you must to call setOWLReasoner().
    protected void setDefaultReasoner( Boolean buffering){
        this.setOWLReasoner(REASONER_FACTORY, buffering, this.getReferenceName());
        this.setReasonerExplanator( new MyReasonerExplanator( this));
    }

    // where this is the implementation of the class that
    // notifies inconsistencies for your reasoner
    class MyReasonerExplanator extends ReasonerExplanator{
        protected MyReasonerExplanator( OWLLibrary owlLibrary) {
            super(owlLibrary);
        }

        @Override
        protected String getExplanation() {
            // return a string that explains the inconsistency, if any.
            // Remember you can rely on the data given on constructor.
            return "We got an inconsistency in" + this.getOwlLibrary() + " because ....";
        }

        @Override
        protected void notifyInconsistency() {
            // log on console the inconsistency
            this.getLogger().addDebugString( "Inconsistency Occurs !!!");
            // you may want to show a graphical dialog to notify an inconsistency
            this.showErrorDialog( "Inconsistency");
        }

    }

}

