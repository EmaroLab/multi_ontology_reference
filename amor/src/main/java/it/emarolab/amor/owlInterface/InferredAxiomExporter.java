package it.emarolab.amor.owlInterface;

import it.emarolab.amor.owlDebugger.Logger;
import it.emarolab.amor.owlDebugger.Logger.LoggerFlag;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.OWLReasoner;

import java.util.Set;

/**
 * Static class used to export an ontology.
 * Reasoner generated asserted property will be exported as fixed.
 *
 * <div style="text-align:center;"><small>
 * <b>Project</b>:    aMOR <br>
 * <b>File</b>:       it.emarolab.amor.owlInterface.InferredAxiomExporter <br>
 * <b>Licence</b>:    GNU GENERAL PUBLIC LICENSE. Version 3, 29 June 2007 <br>
 * <b>Author</b>:     Buoncompagni Luca (luca.buoncompagni@edu.unige.it) <br>
 * <b>affiliation</b>: DIBRIS, EMAROLab, University of Genoa. <br>
 * <b>date</b>:       Feb 10, 2016 <br>
 * </small></div>
 *
 * @author Buoncomapgni Luca
 * @version 1.0
 *
 */
public class InferredAxiomExporter {

    /**
     * Object used to log information about this class instances.
     * Logs are activated by flag: {@link LoggerFlag#LOG_ONTOLOGY_EXPORTER}
     */
    private static Logger logger = new Logger( InferredAxiomExporter.class, LoggerFlag.getLogOntologyExporter());


    private static boolean importingClosure = true;

    // set as non instantiable
    private InferredAxiomExporter() {
        throw new AssertionError();
    }

    /**
     * Saves all asserted entities in an ontology as fixed. Slow.
     *
     * @param ontoRef ontology with inferred entities.
     * @return ontology with exported entities
     */
    public synchronized static OWLReferences exportOntology( OWLReferences ontoRef){
        long initialTime = System.nanoTime();
        Set< OWLNamedIndividual> allIndividuals = ontoRef.getIndividualB2Class( ontoRef.getOWLFactory().getOWLThing());
        Set<OWLObjectProperty> allObjProp = ontoRef.getOWLOntology().getObjectPropertiesInSignature(importingClosure);
        Set<OWLDataProperty> allDataProp = ontoRef.getOWLOntology().getDataPropertiesInSignature( importingClosure);
        for( OWLNamedIndividual i : allIndividuals){ //for all individuals belong to the ontology
            exportObjectProperties( allObjProp, i, ontoRef);
            exportDataProperties( allDataProp, i, ontoRef);
            exportClassAssertion( i, ontoRef);
        }
        ontoRef.applyOWLManipulatorChanges();
        //ontoRef.getOWLReasoner().dispose();
        //ontoRef.setPelletReasoner( ontoRef.useBufferingReasoner());
        logger.addDebugString( "Ontology infered axiom succesfully exported in: " + (System.nanoTime() - initialTime) + " [ns]");
        return( ontoRef);
    }

    private synchronized static void exportObjectProperties( Set< OWLObjectProperty> allProp, OWLNamedIndividual ind, OWLReferences ontoRef){
        synchronized (ontoRef.getOWLReasoner()) {
            OWLReasoner reasoner = ontoRef.getOWLReasoner();
            for( OWLObjectProperty p : allProp){
                // for all the object property in the ontology
                Set< OWLNamedIndividual> indWithThisProp = reasoner.getObjectPropertyValues(ind, p).getFlattened();
                for( OWLNamedIndividual i : indWithThisProp){
                    ontoRef.addObjectPropertyB2Individual( ind, p, i); // ad an axiom in the applied change list of ontoRef
                }
            }
        }
    }


    private synchronized static void exportDataProperties( Set< OWLDataProperty> allProp, OWLNamedIndividual ind, OWLReferences ontoRef){
        synchronized (ontoRef.getOWLReasoner()) {
            OWLReasoner reasoner = ontoRef.getOWLReasoner();
            for( OWLDataProperty p : allProp){
                // for all the data property in the ontology
                Set< OWLLiteral> indWithThisProp = reasoner.getDataPropertyValues(ind, p);
                for( OWLLiteral i : indWithThisProp){
                    ontoRef.addDataPropertyB2Individual( ind, p, i); // ad an axiom in the applied change list of ontoRef
                }
            }
        }
    }


    private synchronized static void exportClassAssertion( OWLNamedIndividual ind, OWLReferences ontoRef){
        synchronized (ontoRef.getOWLReasoner()) {
            OWLReasoner reasoner = ontoRef.getOWLReasoner();
            Set<OWLClass> allClass = reasoner.getTypes( ind, false).getFlattened();
            for( OWLClass c : allClass){
                ontoRef.addIndividualB2Class( ind, c); // ad an axiom in the applied change list of ontoRef
            }
        }
    }



    /**
     * @return the importingClosure
     */
    public static boolean isImportingClosure() {
        return importingClosure;
    }

    /**
     * @param importingClosure set to true to import.
     */
    public static void setImportingClosure(boolean importingClosure) {
        InferredAxiomExporter.importingClosure = importingClosure;
    }
}
