package it.emarolab.amor.owlInterface;

import java.util.Set;

import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.reasoner.OWLReasoner;

import it.emarolab.amor.owlDebugger.Logger;
import it.emarolab.amor.owlDebugger.Logger.LoggerFlag;

/**
 * Static class used to export an ontology.
 * Reasoner generated asserted property will be exported as fixed.
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
		Set< OWLNamedIndividual> allIndividuals = ontoRef.getIndividualB2Class( ontoRef.getFactory().getOWLThing());
		Set<OWLObjectProperty> allObjProp = ontoRef.getOntology().getObjectPropertiesInSignature( importingClosure);
		Set<OWLDataProperty> allDataProp = ontoRef.getOntology().getDataPropertiesInSignature( importingClosure);
		for( OWLNamedIndividual i : allIndividuals){ //for all individuals belong to the ontology
			exportObjectProperties( allObjProp, i, ontoRef);
			exportDataProperties( allDataProp, i, ontoRef);
			exportClassAssertion( i, ontoRef);
		}
		ontoRef.applyOWLManipulatorChanges();
		//ontoRef.getReasoner().dispose();
		//ontoRef.setPelletReasoner( ontoRef.useBufferingReasoner());
		logger.addDebugString( "Ontology infered axiom succesfully exported in: " + (System.nanoTime() - initialTime) + " [ns]");
		return( ontoRef);
	}

	private synchronized static void exportObjectProperties( Set< OWLObjectProperty> allProp, OWLNamedIndividual ind, OWLReferences ontoRef){
		synchronized( ontoRef.getReasoner()){
			OWLReasoner reasoner = ontoRef.getReasoner();
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
		synchronized( ontoRef.getReasoner()){
			OWLReasoner reasoner = ontoRef.getReasoner();
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
		synchronized( ontoRef.getReasoner()){
			OWLReasoner reasoner = ontoRef.getReasoner();
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
