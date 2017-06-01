package it.emarolab.amor.examples;

import it.emarolab.amor.owlDebugger.FileManager;
import it.emarolab.amor.owlDebugger.Logger;
import it.emarolab.amor.owlDebugger.OFGUI.GuiRunner;
import it.emarolab.amor.owlInterface.OWLReferences;
import it.emarolab.amor.owlInterface.OWLReferencesInterface.OWLReferencesContainer;

public class OWLRefDebuggingExample {

    public static void main(String[] args) {
        // 1)  you may want to save on file the logs of the system. Set it to default (time-hour base name) or provide a directory path
        // you can enable and disable logs by changing the flags in the class Logger.LoggerFlag or you may want to set it like:
        Logger.LoggerFlag.setLogOntologyExporter( true); // [PARAMETER]
        Logger.setPrintOnFile( FileManager.DEFAULT_command); // [PARAMETER]

        // 2) let now open the pizza ontology from web with pellet reasoner (default)
        String ontoRefName = "ontoRef3"; // this must be a unique identifier for all the references you what to create into the system
        Boolean bufferingReasoner = false; // reasoning is automatically synchronised
        String pizzaOntologyIri = "http://www.co-ode.org/ontologies/pizza/pizza.owl";
        String filePath = "http://protege.stanford.edu/ontologies/pizza/pizza.owl";
        OWLReferences ontoRef = OWLReferencesContainer.newOWLReferenceFromWeb( ontoRefName, filePath, pizzaOntologyIri, bufferingReasoner);

        // 3) you may want to print the ontology.
        ontoRef.printOntologyOnConsole();

        // 4) [PERAMETER] the system comes with a simple GUI from which it is possible to see the state of the ontology
        // during run time as well as make some basic changes. Be aware that the GUI is not stable yet!
        //NOT PORTED TO OWL API 5 JET
         Thread t = new Thread( new GuiRunner( ontoRefName));
        t.start();

        // 5) be sure to see all logs!
        Logger.flush();

        // wait to give the possibility to interact with the GUI
        try {
            for( int i = 0; i < 100 ; i++) // about 20 minutes
                Thread.sleep( 15000);
        } catch (InterruptedException e) {}
    }
}
