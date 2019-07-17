package it.emarolab.amor.examples;

import it.emarolab.amor.owlInterface.OWLReferences;
import it.emarolab.amor.owlInterface.OWLReferencesInterface.OWLReferencesContainer;
import it.emarolab.amor.owlInterface.SemanticRestriction;
import org.semanticweb.owlapi.model.*;

import java.util.HashSet;

public class OWLRefManipulationExample {

    public static final String OWLREFERENCES_NAME = "refName";
    public static final String ONTOLOGY_FILE_PATH = "../files/ontologies/ontology_manipulation.owl";
    public static final String ONTOLOGY_IRI_PATH = "http://www.semanticweb.org/emarolab/aMor/examples";
    public static final Boolean BUFFERING_REASONER = true; // if true you must to update manually the reasoner. Otherwise it synchronises itself any time is needed
    public static final Boolean BUFFERING_OWLMANIPULATOR = true; // if true you must to apply changes manually. Otherwise their are applied as soon as possible.

    public static void main(String[] args) {
        // 1) let create new ontology with default reasoner (Pellet)
        //OWLReferences ontoRef = OWLReferencesContainer.newOWLReferencesCreated( OWLREFERENCES_NAME, ONTOLOGY_FILE_PATH, ONTOLOGY_IRI_PATH, BUFFERING_REASONER);
        //ontoRef.setOWLManipulatorBuffering( BUFFERING_OWLMANIPULATOR); // if not specified, default value=false {@link OWLManipulator#DEFAULT_CHANGE_BUFFERING}

        OWLReferences ontoRef = OWLReferencesContainer.newOWLReferenceFromFileWithPellet( OWLREFERENCES_NAME, ONTOLOGY_FILE_PATH, ONTOLOGY_IRI_PATH, true);

        // todo adjust also path absolute
        ontoRef.addRestriction( new SemanticRestriction.DataRangeRestricted(
                ontoRef.getOWLDataProperty( "hasDataProperty_renamed"),
                ontoRef.getOWLFactory().getBooleanOWLDatatype()
        ));

        // note that changes made by the OWLManipulator are such that if an entity (given through its name) exists in the ontology
        // it will be used. On the other hand, if it does not exist a new entity with the specified name will be created.

        // 2) [ADD_IND] let add an two individuals into the ontology
        ontoRef.addIndividual( "newIndividualName0");
        ontoRef.addIndividual( "newIndividualName1");
        ontoRef.addIndividual( "newIndividualName2");

        // 3) [ADD_IND_CLASS] let add an individual into a specific class
        ontoRef.addIndividualB2Class( "newIndividualName3", "ClassName");
        // you may want to set an individual to be belonging also to another class (it may cause inconsistencies if the two classes are disjointed)
        ontoRef.addIndividualB2Class( "newIndividualName2", "ClassName");

        // 4) [ADD_CLASS] let add a class into the ontology
        ontoRef.addClass( "newClass");

        // 5) [ADD_CLASS_CLASS] let add a class as a sub class of another
        ontoRef.addSubClassOf( "superClass", "subClass");

        // 6) [ADD_DATAPROP_IND] let add a data property to an individual (the type is infered from the value)
        ontoRef.addDataPropertyB2Individual( "newIndividualName1", "hasDataProperty", 15.5f);
        // you may want to use owl objects
        OWLLiteral dataPropValue = ontoRef.getOWLLiteral( true);
        OWLNamedIndividual indWithProp = ontoRef.getOWLIndividual( "individual");
        OWLDataProperty dataProp = ontoRef.getOWLDataProperty( "hasDataProperty");
        ontoRef.addDataPropertyB2Individual( indWithProp, dataProp, dataPropValue);

        // 7) [ADD_OBJPROP_IND] let add an object property to an individual (the value must be another individual)
        ontoRef.addObjectPropertyB2Individual( "newIndividualName2", "hasObjectProperty", "newIndividualName3");
        // you may want to use owl objects
        OWLNamedIndividual objPropValue = ontoRef.getOWLIndividual( "individualObjectPropertyValue");
        OWLObjectProperty objProp = ontoRef.getOWLObjectProperty( "hasObjectProperty");
        ontoRef.addObjectPropertyB2Individual( indWithProp, objProp, objPropValue);

        // 8) apply all the changes made (it have effects only if BUFFERING_MANIPULATOR=true, otherwise are applied by default)
        ontoRef.applyOWLManipulatorChanges();

        // 10) [REMOVE_IND] let remove an individual from the ontology
        ontoRef.removeIndividual( "newIndividualName0");

        // 11) [REMOVE_IND_CLASS] let remove the assertion of an individual to be belonging to a class
        ontoRef.removeIndividualB2Class( "newIndividualName2", "ClassName");

        // 12) [REMOVE_CLASS] let remove a class from the ontology
        ontoRef.removeClass( "newClass");

        // 13) [REMOVE_CLASS_CLASS] let remove a sub classing assertion (the sub class goes to top hierarchy level)
        // (if in this procedure the super or sub class is empty it will be removed)
        ontoRef.addIndividualB2Class("ind1", "subClass"); // put something in the class otherwise it gets deleted from OWL API
        ontoRef.addIndividualB2Class("ind2", "superClass"); // put something in the class otherwise it gets deleted from OWL API
        // remove the assertion
        ontoRef.removeSubClassOf( "superClass", "subClass");

        // 14) [REMOVE_DATAPROP_IND] let remove a data property from an individual
        ontoRef.removeDataPropertyB2Individual( "newIndividualName1", "hasDataProperty", 15.5f);

        // 15) [REMOVE_OBJPROP_IND] let remove an object property from an individual
        ontoRef.removeObjectPropertyB2Individual( "newIndividualName2", "hasObjectProperty", "newIndividualName3");

        // 16) [REPLACE_DATAPROP_IND] let replace a data property value attached to an individual
        OWLLiteral newValue = ontoRef.getOWLLiteral( false);
        ontoRef.replaceDataProperty( indWithProp, dataProp, dataPropValue, newValue);

        // 17) [REPLACE_OBJECTPROP_IND] let replace an object property value attached to an individual
        OWLNamedIndividual newObjPropValue = ontoRef.getOWLIndividual( "NEW_individualObjectPropertyValue");
        ontoRef.replaceObjectProperty(indWithProp, objProp, objPropValue, newObjPropValue);

        // 18) [RENAME_IND] let rename an individual
        ontoRef.applyOWLManipulatorChanges(); // the API may not work with buffering manipulator. So, synchronise it before to rename
        ontoRef.renameEntity( indWithProp, IRI.create( ONTOLOGY_IRI_PATH + "#RENAMED_individual"));

        // otherwise you may want to change the buffering behaviour. Remember to clear the buffer before to change the flag!
        ontoRef.applyOWLManipulatorChanges();
        ontoRef.setOWLManipulatorBuffering( false);

        // 19) [RENAME_CLASS] let rename a class (be careful of the buffering consideration above)
        OWLClass cl = ontoRef.getOWLClass( "ClassName");
        ontoRef.renameEntity( cl, "ClassRenamed");

        // 20) [RENAME_DATAPROP] let rename a data property (be careful of the buffering consideration above)
        //ontoRef.applyOWLManipulatorChanges(); // the API may not work with buffering manipulator. So, synchronise it before to rename
        ontoRef.renameEntity( dataProp, "hasDataProperty_renamed");

        // 21) [RENAME_OBJECTPROP] let rename an object property (be careful of the buffering consideration above)
        //ontoRef.applyOWLManipulatorChanges(); // the API may not work with buffering manipulator. So, synchronise it before to rename
        ontoRef.renameEntity( objProp, "hasObjectProperty_renamed");


        // 22) add and remove property range and domain
        System.out.println("query data range " + ontoRef.getDataRangeRestrictions( ontoRef.getOWLDataProperty( "hasDataProperty_res")));
        System.out.println("query data domain " + ontoRef.getDataDomainRestrictions( ontoRef.getOWLDataProperty( "hasDataProperty_res")));
        System.out.println("query object range " + ontoRef.getObjectRangeRestrictions( ontoRef.getOWLObjectProperty( "hasObjectProperty_res")));
        System.out.println("query object domain " + ontoRef.getObjectDomainRestrictions( ontoRef.getOWLObjectProperty( "hasObjectProperty_res")));

        // manipulate data range and domain axioms
        ontoRef.addRestriction(new SemanticRestriction.DataDomainRestrictedOnExactData(
                ontoRef.getOWLDataProperty( "hasDataProperty_res"),
                ontoRef.getOWLFactory().getBooleanOWLDatatype(),
                ontoRef.getOWLDataProperty( "restrictionProp"),
                3));
        SemanticRestriction.DataDomainRestrictedOnClass resData = new SemanticRestriction.DataDomainRestrictedOnClass(
                ontoRef.getOWLDataProperty("hasDataProperty_res"),
                ontoRef.getOWLClass("subClass"));
        ontoRef.addRestriction( resData);
        ontoRef.addRestriction(new SemanticRestriction.DataDomainRestrictedOnSomeData(
                ontoRef.getOWLDataProperty( "hasDataProperty_res"),
                ontoRef.getOWLFactory().getBooleanOWLDatatype(),
                ontoRef.getOWLDataProperty( "restrictionProp2")));
        System.out.println("change data domain " + ontoRef.getDataDomainRestrictions( ontoRef.getOWLDataProperty( "hasDataProperty_res")));
        ontoRef.removeRestriction( resData);
        System.out.println("remove data range " + ontoRef.getDataDomainRestrictions( ontoRef.getOWLDataProperty( "hasDataProperty_res")));

        // manipulate object range and domain axioms
        HashSet<SemanticRestriction> restr = new HashSet<>();
        restr.add( new SemanticRestriction.ObjectDomainRestrictedOnMinObject(
                ontoRef.getOWLObjectProperty( "hasObjectProperty_res"),
                ontoRef.getOWLClass( "ResCls"),
                ontoRef.getOWLObjectProperty( "restrictionProp3"),
                3));
        SemanticRestriction.ObjectDomainRestrictedOnClass resObj = new SemanticRestriction.ObjectDomainRestrictedOnClass(
                ontoRef.getOWLObjectProperty("hasObjectProperty_res"),
                ontoRef.getOWLClass("subClass"));
        restr.add( resObj);
        restr.add(new SemanticRestriction.ObjectDomainRestrictedOnMaxObject(
                ontoRef.getOWLObjectProperty( "hasObjectProperty_res"),
                ontoRef.getOWLClass( "ResCls"),
                ontoRef.getOWLObjectProperty( "restrictionProp4"),
                3));
        ontoRef.addRestriction(restr);
        System.out.println("change object domain " + ontoRef.getObjectDomainRestrictions( ontoRef.getOWLObjectProperty( "hasObjectProperty_res")));
        ontoRef.removeRestriction( resObj);
        System.out.println("remove object range " + ontoRef.getObjectDomainRestrictions( ontoRef.getOWLObjectProperty( "hasObjectProperty_res")));

        // manipulate data range and domain conjunction of axioms
        HashSet<SemanticRestriction> restrAxiom = new HashSet<>(); // all elements must involve domain or range and the same subject
        restrAxiom.add( new SemanticRestriction.ObjectDomainRestrictedOnMinObject(  // domain
                ontoRef.getOWLObjectProperty( "hasObjectProperty_res_ax"), // subject
                ontoRef.getOWLClass( "ResCls"),
                ontoRef.getOWLObjectProperty( "restrictionProp3"),
                3));
        restrAxiom.add(new SemanticRestriction.ObjectDomainRestrictedOnClass(  // domain
                ontoRef.getOWLObjectProperty( "hasObjectProperty_res_ax"), // subject
                ontoRef.getOWLClass( "ResCls")));
        ontoRef.addRestrictionAxiom( restrAxiom);

        // print the ontology and save for manipulation check
        ontoRef.saveOntology( "../files/ontologies/ontology_manipulation1.owl");

        System.out.println( "DONE !!");

        // there are also other functions of OWLReferences (OWLManipulator) that you may what to use ... check out the JavaDoc

        // also be sure to check how the complete documentation to see how is easy to integrate your manipulation
        // in the system and contribute to the community
    }
}
