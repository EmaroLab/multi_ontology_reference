package it.emarolab.amor.examples;

import java.util.Set;

import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLNamedIndividual;

import it.emarolab.amor.owlDebugger.Logger;
import it.emarolab.amor.owlInterface.OWLEnquirer;
import it.emarolab.amor.owlInterface.OWLEnquirer.DataPropertyRelatios;
import it.emarolab.amor.owlInterface.OWLEnquirer.ObjectPropertyRelatios;
import it.emarolab.amor.owlInterface.OWLLibrary;
import it.emarolab.amor.owlInterface.OWLReferences;
import it.emarolab.amor.owlInterface.OWLReferencesInterface.OWLReferencesContainer;

public class OWLRefEnquirerExample {

	public static final String OWLREFERENCES_NAME = "refName";
	public static final String ONTOLOGY_FILE_PATH = "http://protege.stanford.edu/ontologies/pizza/pizza.owl";
	public static final String ONTOLOGY_IRI_PATH = "http://www.co-ode.org/ontologies/pizza/pizza.owl";
	public static final String REASONER_FACTORY = OWLLibrary.REASONER_QUALIFIER_PELLET;
	public static final Boolean BUFFERING_REASONER = true; // if true you must to update manually the reasoner. Otherwise it synchronises itself any time is needed
	public static final Integer COMMAND = OWLReferencesContainer.COMMAND_LOAD_WEB;
	public static final Boolean BUFFERING_OWLMANIPULATOR = false; // if true you must to apply changes manually. Otherwise their are applied as soon as possible.

	public static final String ONTOLOGY_SAVING_PATH = "files/ontologies/pizza_enquired.owl";

	public static void main(String[] args) {
		// let disable verbose logging (this call may be delayed!!)
		Logger.LoggerFlag.resetAllLoggingFlags();
		
		// 1) [LOAD_WEB] load the ontology from web with the more general constructor
		OWLReferences ontoRef = OWLReferencesContainer.newOWLReferences( OWLREFERENCES_NAME, ONTOLOGY_FILE_PATH, ONTOLOGY_IRI_PATH, REASONER_FACTORY, BUFFERING_REASONER, COMMAND);
		ontoRef.setOWLManipulatorBuffering( BUFFERING_OWLMANIPULATOR);
		
		// 2) [QUERY_IND_CLASS] let query all the individuals belonging to a class
		Set<OWLNamedIndividual> individuals = ontoRef.getIndividualB2Class( "DomainConcept");
		System.out.println( " individuals belonging to \"DomainConcept\": " + individuals);
		// [PARAMETER] you may want to use less verbose name, in case see:
		Set< String> individualsName = ontoRef.getOWLObjectName( individuals);
		System.out.println( " individuals belonging to \"DomainConcept\": " + individualsName);
		System.out.println( " -------------------------------- 2 ------------------------------------------ \n");

		// add some properties for showing purposes (since the manipulator is not buffering we do not need to apply the changes)
		String individualName = (String) individualsName.toArray()[0];
		String propertyName = "hasDataProperty";
		ontoRef.addDataPropertyB2Individual( individualName, propertyName + "1", true);
		ontoRef.addDataPropertyB2Individual( individualName, propertyName, 1);
		ontoRef.addDataPropertyB2Individual( individualName, propertyName, 1.5f);

		// 3) [QUERY_IND_DATAPROP] let query all the data properties of an individual
		Set<DataPropertyRelatios> allData = ontoRef.getDataPropertyB2Individual(individualName);
		System.out.println( "Query withoud synchronising the reasoner: " + individualName + " has Data Properties: " + allData);
		// be careful while do queries with respect to buffering flags. For example this query require to 
		// assess "hasDataProperty" as a sub property of "TopDataProperty" which requires the synchronisation of the reasoner 
		ontoRef.synchroniseReasoner(); // you may not call this if both buffering flags are false (this calls also applyChanges())
		allData = ontoRef.getDataPropertyB2Individual(individualName);
		System.out.println( "Query with synchronising the reasoner" + individualName + " has Data Properties: " + allData);
		System.out.println( " -------------------------------- 3 ------------------------------------------ \n");
		
		// 4) [QUERY_DATAPROP_IND] let query the values of a specific data property belonging to an individual
		Set<OWLLiteral> values = ontoRef.getDataPropertyB2Individual( individualName, propertyName);
		System.out.println( " values of the data property " + propertyName + " of the individual: " + individualName + " are: " + values); 
		Set< String> valuesStr = ontoRef.getOWLObjectName( values);
		System.out.println( " values of the data property " + propertyName + " of the individual: " + individualName + " are: " + valuesStr);
		System.out.println( " -------------------------------- 4 ------------------------------------------ \n");
		
		// add some entities to make the think interesting
		String interestingInd = "interPizza";
		ontoRef.addIndividualB2Class( "base", "DeepPanBase");
		ontoRef.addIndividualB2Class( "topping1", "CheeseTopping");
		ontoRef.addIndividualB2Class( "topping2", "SlicedTomatoTopping");
		ontoRef.addIndividualB2Class( "topping3", "ParmaHamTopping");
		ontoRef.addObjectPropertyB2Individual( interestingInd, "hasBase", "base");
		ontoRef.addObjectPropertyB2Individual( interestingInd, "hasTopping", "topping1");
		ontoRef.addObjectPropertyB2Individual( interestingInd, "hasTopping", "topping2");
		ontoRef.addObjectPropertyB2Individual( interestingInd, "hasTopping", "topping3");

		// 5) [QUERY_IND_OBJECTPROP] let query all the object properties of an individual
		Set<ObjectPropertyRelatios> allObject = ontoRef.getObjectPropertyB2Individual( interestingInd);
		System.out.println( allObject);
		// if you update the ontology state the same functions will consider also inferred axioms
		ontoRef.synchroniseReasoner(); // you may not call this if both buffering flags are false (this calls also applyChanges())
		Set<ObjectPropertyRelatios> allReasonedObject = ontoRef.getObjectPropertyB2Individual( interestingInd);
		System.out.println( allReasonedObject);
		System.out.println( " -------------------------------- 5 ------------------------------------------ \n");
		
		// 6) [QUERY_OBJECTPROP_IND] let query the value of a specific object property of an individual
		// this method is more efficient than the previous
		Set<OWLNamedIndividual> indValues = ontoRef.getObjectPropertyB2Individual( "base", "isBaseOf");
		if( ! indValues.isEmpty()) // this is a standard convention in all the API
			System.out.println( " base isBaseOf( " + ontoRef.getOWLObjectName( indValues) + ")");
		else System.out.println( " \"base\" does not have values fro object property \"isBaseOf\"");
		System.out.println( " -------------------------------- 6 ------------------------------------------ \n");
		
		// 7) [QUERY_CLASS_CLASS] let query all the sub classes of a class
		
		ontoRef.setOWLEnquirerCompletenessFlag( false);
		Set<OWLClass> subClasses = ontoRef.getSubClassOf( "DomainConcept"); // get only first layer of children
		System.out.println( " direct sub classes of DomainConcept are: " + subClasses);
		ontoRef.setOWLManipulatorBuffering( OWLEnquirer.DEFAULT_RETURN_COMPLETE_DESCRIPTION); // reset to default value (true)
		Set<OWLClass> allSubClasses = ontoRef.getSubClassOf( "DomainConcept"); // get all the children
		System.out.println( " all the sub classes of DomainConcept are: " + allSubClasses);
		System.out.println( " -------------------------------- 7 ------------------------------------------ \n");
		
		System.out.println( "DONE !!");
		
		// there are also other functions of OWLReferences (OWLEnquirer) that you may what to use ... check out the JavaDoc
		
		// also be sure to check how the complete documentation to see how is easy to integrate your query 
		// in the system and contribute to the comunity 
		
		ontoRef.saveOntology( ONTOLOGY_SAVING_PATH);
	}
}