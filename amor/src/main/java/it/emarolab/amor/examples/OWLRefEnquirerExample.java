package it.emarolab.amor.examples;

import it.emarolab.amor.owlDebugger.Logger;
import it.emarolab.amor.owlInterface.*;
import it.emarolab.amor.owlInterface.OWLReferencesInterface.OWLReferencesContainer;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLNamedIndividual;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class OWLRefEnquirerExample {

    public static final String OWLREFERENCES_NAME = "refName";
    public static final String ONTOLOGY_FILE_PATH = "http://protege.stanford.edu/ontologies/pizza/pizza.owl";
    public static final String ONTOLOGY_IRI_PATH = "http://www.co-ode.org/ontologies/pizza/pizza.owl";
    public static final String REASONER_FACTORY = OWLLibrary.REASONER_QUALIFIER_PELLET;
    public static final Boolean BUFFERING_REASONER = true; // if true you must to update manually the reasoner. Otherwise it synchronises itself any time is needed
    public static final Integer COMMAND = OWLReferencesContainer.COMMAND_LOAD_WEB;
    public static final Boolean BUFFERING_OWLMANIPULATOR = false; // if true you must to apply changes manually. Otherwise their are applied as soon as possible.

    public static final String ONTOLOGY_SAVING_PATH = "files/ontologies/pizza_enquired.owl";

    private static Logger logger = new Logger( OWLRefEnquirerExample.class, true);

    public static void main(String[] args) {
        // let disable verbose logging (this call may be delayed!!)
        //Logger.LoggerFlag.resetAllLoggingFlags();

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
        Set<DataPropertyRelations> allData = ontoRef.getDataPropertyB2Individual(individualName);
        System.out.println( "Query withoud synchronising the reasoner: " + individualName + " has Data Properties: " + allData);
        // be careful while do queries with respect to buffering flags. For example this query require to
        // assess "hasDataProperty" as a sub property of "TopDataProperty" which requires the synchronisation of the reasoner
        ontoRef.synchronizeReasoner(); // you may not call this if both buffering flags are false (this calls also applyChanges())
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
        Set<ObjectPropertyRelations> allObject = ontoRef.getObjectPropertyB2Individual( interestingInd);
        System.out.println( allObject);
        // if you update the ontology state the same functions will consider also inferred axioms
        ontoRef.synchronizeReasoner(); // you may not call this if both buffering flags are false (this calls also applyChanges())
        Set<ObjectPropertyRelations> allReasonedObject = ontoRef.getObjectPropertyB2Individual( interestingInd);
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

        // 8) make an SPARQL query
        String PREFIX = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>"
                + "PREFIX owl: <http://www.w3.org/2002/07/owl#>"
                + "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>"
                + "PREFIX piz: <http://www.co-ode.org/ontologies/pizza/pizza.owl#>";
        String SELECT = "SELECT ?p ?p1 ?p2 ?p3";
        String WHERE = " WHERE{"
                + "?p  rdf:type             owl:Class;"
                +     "rdfs:subClassOf      ?t."
                + "?p1 rdf:type             owl:Class;"
                +     "rdfs:subClassOf      ?t;"
                +     "rdfs:subClassOf      ?t1."

                + "?t  owl:onProperty       piz:hasTopping;"
                +     "owl:someValuesFrom   piz:PeperoniSausageTopping."
                + "?t1 owl:onProperty       piz:hasTopping;"
                +     "owl:someValuesFrom   piz:TomatoTopping."
                + "}";
        List<Map<String, String>> result = ontoRef.sparql2Msg( PREFIX + SELECT + WHERE, 10L);
        logger.addDebugString( "SPARQL results: " + result);
        System.out.println( " -------------------------------- 8 ------------------------------------------ \n");


        System.out.println( "DONE !!");

        // there are also other functions of OWLReferences (OWLEnquirer) that you may what to use ... check out the JavaDoc

        // also be sure to check how the complete documentation to see how is easy to integrate your query
        // in the system and contribute to the community

        ontoRef.saveOntology( ONTOLOGY_SAVING_PATH);

    }
}
