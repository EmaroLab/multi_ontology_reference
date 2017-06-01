package it.emarolab.amor.owlInterface;

import it.emarolab.amor.owlDebugger.Logger;
import it.emarolab.amor.owlDebugger.Logger.LoggerFlag;
import org.semanticweb.owlapi.change.ConvertSuperClassesToEquivalentClass;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.util.OWLEntityRemover;
import org.semanticweb.owlapi.util.OWLEntityRenamer;

import java.util.*;


// TODO :  SWRL
// TODO : move mutex management from owl reference to here, and make this an interface

/**
 * This class implements basic ontology manipulations.
 *
 * It is NOT thread-safe. Users should refrain from using this class directly, especially in multi-ontology scenarios.
 * Use {@link OWLReferences} instead.<br>
 * 
 * Manipulations performed by this class can be applied immediately or buffered and applied later
 * ({@link #applyChanges()}). It is advised to use buffered changes when timing is an issue,
 * especially if you are using a non-incremental reasoner.<br>
 *
 * Manipulation methods can take as input an entity name (complete semantic IRI is not strictly required).
 * If an entity with such name exists, it will be manipulated accordingly.
 * Otherwise, the missing entity WILL be created.
 *
 * The following naming convention applies:<br>
 * {@literal B2} stands for "belong to".
 *
 * <div style="text-align:center;"><small>
 * <b>Project</b>:    aMOR <br>
 * <b>File</b>:       it.emarolab.amor.owlInterface.OWLManipulator <br>
 * <b>Licence</b>:    GNU GENERAL PUBLIC LICENSE. Version 3, 29 June 2007 <br>
 * <b>Author</b>:     Buoncompagni Luca (luca.buoncompagni@edu.unige.it) <br>
 * <b>affiliation</b>: DIBRIS, EMAROLab, University of Genoa. <br>
 * <b>date</b>:       Feb 10, 2016 <br>
 * </small></div>
 * 
 * @version 2.1
 */
public class OWLManipulator{

    /**
     * An internal tag to represent an universal restriction expression.
     * (see: {@link ClassExpressionType#OBJECT_ALL_VALUES_FROM})
     */
    protected static final int RESTRICTION_ONLY = 1;
    /**
     * A tag to represent an existential restriction expression.
     * (see: {@link ClassExpressionType#OBJECT_SOME_VALUES_FROM}
     */
    protected static final int RESTRICTION_SOME = 2;
    /**
     * A tag to represent an minimal cardinality restriction expression.
     * (see: {@link ClassExpressionType#OBJECT_MIN_CARDINALITY})
     */
    protected static final int RESTRICTION_MIN = 3; // >= 3 has cardinality
    /**
     * A tag to represent an exact cardinality restriction expression.
     * (see: {@link ClassExpressionType#OBJECT_EXACT_CARDINALITY})
     */
    protected static final int RESTRICTION_EXACT = 4;
    /**
     * A tag to represent an maximal cardinality restriction expression.
     * (see: {@link ClassExpressionType#OBJECT_MAX_CARDINALITY})
     */
    protected static final int RESTRICTION_MAX = 5;
    /**
     * The default value of the {@link #manipulationBuffering} field.
     * Used if no value is passed to {@link #OWLManipulator(OWLReferencesInterface)} constructor.
     */
    public static Boolean DEFAULT_MANIPULATION_BUFFERING = false;
    /**
     * This is a vector of buffered ontological changes to be applied to this class.
     * Changes can be applied by calling {@link #applyChanges()}.
     */
    private final List<OWLOntologyChange> changeList = new ArrayList<OWLOntologyChange>();

    // [[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[   CONSTRUCTOR   ]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]
    /**
     * Member required to log class activity.
     * Logs can be activated by setting the flag {@link LoggerFlag#LOG_OWL_MANIPULATOR}
     */
    private Logger logger = new Logger(this, LoggerFlag.getLogOWLManipulator());
    /**
     * Buffered manipulation change. If {@code true}, it buffers changes till {@link #applyChanges()} method is called.
     * Else, it applies changes immediately. In buffered mode, changes can be applied by {@link #applyChanges()}.
     */
    private Boolean manipulationBuffering;

    // [[[[[[[[[[[[[[[[[[[[[[[[[[[[[   CHANGE BUFFERING FLAG MANAGEMENT   ]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]
    /**
     * The basic Reference to an ontology.
     */
    private OWLReferencesInterface ontoRef;
    /**
     * Class constructor.
     * @param owlRef reference to the ontology to be manipulated.
     * @param manipulationBuffering {@code true}, buffers changes till {@link #applyChanges()} method is called.
     *                              Else, applies changes immediately.
     */
    protected OWLManipulator( OWLReferencesInterface owlRef, Boolean manipulationBuffering){
        this.ontoRef = owlRef;
        this.manipulationBuffering = manipulationBuffering;
    }
    /**
     * Class constructor. Automatically sets {@link #manipulationBuffering} to {@link #DEFAULT_MANIPULATION_BUFFERING}.
     * @param owlRef reference to the ontology to be manipulated.
     */
    protected OWLManipulator( OWLReferencesInterface owlRef){
        this.ontoRef = owlRef;
        this.manipulationBuffering = DEFAULT_MANIPULATION_BUFFERING;
    }


    // [[[[[[[[[[[[[[[[[[[[[[[[[[[[   OWL REFERENCE POINTER MANGMENT  ]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]

    /**
     * @return {@link #manipulationBuffering}. If {@code true}, it buffers changes till {@link #applyChanges()} method
     * is called. Else, it applies changes immediately.
     */
    public synchronized Boolean isChangeBuffering() {
        return manipulationBuffering;
    }

    /**
     * Sets the {@link #manipulationBuffering} flag.
     * @param manipulationBuffering {@code true}, buffers changes till {@link #applyChanges()} method is called.
     *                              Else, applies changes immediately.
     */
    public synchronized void setManipulationBuffering(Boolean manipulationBuffering) {
        this.manipulationBuffering = manipulationBuffering;
    }


    // [[[[[[[[[[[[[[[[[[[[   METHODS TO COLLECT AND APPLY ONTOLOGY CHANGES   ]]]]]]]]]]]]]]]]]]]]]]]]]]

    /**
     * @return a container of all entities in the ontology.
     */
    protected OWLReferencesInterface getOwlLibrary(){
        return ontoRef;
    }

    /**
     * Returns the list of additions necessary to express an axiom in the ontology.
     * This is equivalent to call {@link #getAddAxiom(OWLAxiom, boolean)} with
     * current {@link #manipulationBuffering} value as second argument.
     * @param axiom a logical axiom.
     * @return ordered set of changes to express the axiom in the ontology.
     */
    public synchronized OWLOntologyChange getAddAxiom( OWLAxiom axiom){
        return( getAddAxiom( axiom, manipulationBuffering));
    }
    /**
     * Returns the list of additions necessary to express an axiom in the ontology.
     * If {@code addToChangeList} is {@code true}, changes will be stored
     * inside the internal buffer ({@link #changeList}). Else, you need to
     * manually manage the returned values.
     * @param axiom a logical axiom.
     * @param addToChangeList if {@code true}, stores results in
     *        {@link #changeList}. Else, it returns the results.
     * @return ordered set of changes to express the axiom in the ontology.
     */
    public synchronized OWLOntologyChange getAddAxiom( OWLAxiom axiom, boolean addToChangeList){
        try{
            long initialTime = System.nanoTime();
            AddAxiom addAxiom = new AddAxiom( ontoRef.getOWLOntology(), axiom);//ontoRef.getOWLManager().addAxiom( ontoRef.getOWLOntology(), axiom);
            if( addToChangeList)
                changeList.add( addAxiom);
            logger.addDebugString( "get add axiom in: " + (System.nanoTime() - initialTime) + " [ns]");
            return( addAxiom);
        } catch( org.semanticweb.owlapi.reasoner.InconsistentOntologyException e){
            ontoRef.logInconsistency();
        }
        return null;
    }

    /**
     * Returns the list of removals necessary to delete an axiom from the ontology.
     * This is equivalent to call {@link #getRemoveAxiom(OWLAxiom, boolean OWLReferences)}
     * with current {@link #manipulationBuffering} value as second argument.
     * @param axiom a logical axiom.
     * @return ordered set of changes to remove the axiom from the ontology.
     */
    public synchronized OWLOntologyChange getRemoveAxiom( OWLAxiom axiom){
        return( getRemoveAxiom( axiom, manipulationBuffering));
    }
    /**
     * Returns the list of removals necessary to delete an axiom from the ontology.
     * If {@code addToChangeList} is {@code true}, changes will be stored
     * inside the internal buffer ({@link #changeList}). Else, you need to
     * manually manage the returned values.
     * @param axiom a logical axiom.
     * @param addToChangeList if {@code true}, stores results in
     *        {@link #changeList}. Else, it returns the results.
     * @return ordered set of changes to remove the axiom from the ontology.
     */
    public synchronized OWLOntologyChange getRemoveAxiom( OWLAxiom axiom, boolean addToChangeList){
        long initialTime = System.nanoTime();
        RemoveAxiom removeAxiom = null;
        try{
            removeAxiom = new RemoveAxiom( ontoRef.getOWLOntology(), axiom);//ontoRef.getOWLManager().removeAxiom( ontoRef.getOWLOntology(), axiom);
            if( addToChangeList)
                changeList.add( removeAxiom);
        } catch( org.semanticweb.owlapi.reasoner.InconsistentOntologyException e){
            ontoRef.logInconsistency();
        }
        logger.addDebugString( "get remove axiom in: " + (System.nanoTime() - initialTime) + " [ns]");
        return( removeAxiom);
    }

    /**
     * It applies all pending changes stored in {@link #changeList} then, it clears {@link #changeList}.
     */
    public synchronized void applyChanges(){
        long initialTime = System.nanoTime();
        try{
            ontoRef.getOWLManager().applyChanges( changeList);
            changeList.clear();
            logger.addDebugString( "apply changes in: " + (System.nanoTime() - initialTime) + " [ns]");
        } catch( org.semanticweb.owlapi.reasoner.InconsistentOntologyException e){
            ontoRef.logInconsistency();
        }
    }
    /**
     * It applies a change stored in {@code addAxiom} to the ontology.
     * @param addAxiom a change to be applied.
     * @param <T> a generic estionsion of {@link OWLOntologyChange}.
     */
    public synchronized <T extends OWLOntologyChange> void applyChanges( T addAxiom){
        long initialTime = System.nanoTime();
        try{
            ontoRef.getOWLManager().applyChange( addAxiom);
            logger.addDebugString( "apply changes in: " + (System.nanoTime() - initialTime) + " [ns]");
        } catch( org.semanticweb.owlapi.reasoner.InconsistentOntologyException e){
            ontoRef.logInconsistency();
        }
    }
    /**
     * It applies all changes stored in {@code addAxiom} to the ontology.
     * @param addAxiom a the changes to be applied.
     */
    public synchronized void applyChanges( List< ? extends OWLOntologyChange> addAxiom){
        long initialTime = System.nanoTime();
        try{
            ontoRef.getOWLManager().applyChanges( addAxiom);
            logger.addDebugString( "apply changes in: " + (System.nanoTime() - initialTime) + " [ns]");
        } catch( org.semanticweb.owlapi.reasoner.InconsistentOntologyException e){
            ontoRef.logInconsistency();
        }
    }



    // [[[[[[[[[[[[[[[[[[[[[[[[[[[   METHODS FOR ONTOLOGY MANIPULATION  ]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]
    // ---------------------------   methods for adding entities to the ontology
    /**
     * Returns the changes required to add an object property and its value to an individual.
     * If one individual does not exists, it will be created.
     * Changes will be buffered if {@link #isChangeBuffering()} is {@code true}, else they will be applied immediately.
     * @param ind individual the property must be assigned to.
     * @param prop object property to be added.
     * @param value individual value of the object property.
     * @return changes required to the ontology required to add the property.
     * Returned object can be ignored while working in buffering mode.
     */
    public OWLOntologyChange addObjectPropertyB2Individual( OWLNamedIndividual ind, OWLObjectProperty prop, OWLNamedIndividual value){
        long initialTime = System.nanoTime();
        try{
            OWLAxiom propertyAssertion = ontoRef.getOWLFactory().getOWLObjectPropertyAssertionAxiom( prop, ind, value);
            OWLOntologyChange add = getAddAxiom( propertyAssertion, manipulationBuffering);
            if( !manipulationBuffering)
                applyChanges( add);
            logger.addDebugString( "add object property (" + ontoRef.getOWLObjectName( prop) + ")  belong to individual (" + ontoRef.getOWLObjectName( ind) + ")"
                    + " with value (" + ontoRef.getOWLObjectName( value) + ") in: " + (System.nanoTime() - initialTime) + " [ns]");
            return( add);
        } catch( org.semanticweb.owlapi.reasoner.InconsistentOntologyException e){
            ontoRef.logInconsistency();
            return( null);
        }
    }
    /**
     * Returns the changes required to add an object property and its value to an individual.
     * If one individual does not exists, it is created.
     * Changes will be buffered if {@link #isChangeBuffering()} is {@code true}, else they will be applied immediately.
     * @param individualName name of the individual the property must be assigned to.
     * @param propName name of the object property to be added.
     * @param valueName name of the individual value of the object property.
     * @return changes required to the ontology required to add the property.
     * Returned object can be ignored while working in buffering mode.
     */
    public OWLOntologyChange addObjectPropertyB2Individual( String individualName, String propName, String valueName){
        OWLNamedIndividual indiv = ontoRef.getOWLIndividual( individualName);
        OWLObjectProperty prop = ontoRef.getOWLObjectProperty( propName);
        OWLNamedIndividual val = ontoRef.getOWLIndividual( valueName);
        return( addObjectPropertyB2Individual( indiv, prop, val));
    }

    /**
     * Returns the changes required to add all the object properties and their values to an individual.
     * It relies on {@link #addObjectPropertyB2Individual(OWLNamedIndividual, OWLObjectProperty, OWLNamedIndividual)},
     * where input parameters are respectively: {@link ObjectPropertyRelations#getIndividual()},
     * {@link ObjectPropertyRelations#getProperty()} and all {@link ObjectPropertyRelations#getValues()}.
     * Changes will be buffered if {@link #isChangeBuffering()} is {@code true}, else they will be applied immediately.
     * @param relations the container for all the object relations to add.
     * @return  changes to the ontology required to add the properties.
     * Returned object can be ignored while working in buffering mode.
     */
    public List< OWLOntologyChange> addObjectPropertyB2Individual( ObjectPropertyRelations relations){
        List< OWLOntologyChange> changes = new ArrayList<>();
        for ( OWLNamedIndividual l : relations.getValues())
            changes.add( addObjectPropertyB2Individual( relations.getIndividual(), relations.getProperty(), l));
        return changes;
    }
    /**
     * Returns the changes required to add all the object properties and their values to an individual.
     * It calls {@link #addObjectPropertyB2Individual(ObjectPropertyRelations)} for all the element in the
     * given set.
     * Changes will be buffered if {@link #isChangeBuffering()} is {@code true}, else they will be applied immediately.
     * @param relations the container for all the object relations to add.
     * @return  changes to the ontology required to add the properties.
     * Returned object can be ignored while working in buffering mode.
     */
    public List< OWLOntologyChange> addObjectPropertyB2Individual( Set< ObjectPropertyRelations> relations){
        List< OWLOntologyChange> changes = new ArrayList<>();
        for ( ObjectPropertyRelations r : relations)
            changes.addAll( addObjectPropertyB2Individual( r));
        return changes;
    }


    /**
     * Returns the changes required to add an inverse property to an object property.
     * Changes will be buffered if {@link #isChangeBuffering()} is {@code true}, else they will be applied immediately.
     * @param prop the name of the direct object property.
     * @param inverse the name of the inverse object property.
     * @return the change to be applied to set an inverse property to a direct property.
     */
    public OWLOntologyChange addObjectPropertyInverseOf( String prop, String inverse){
        return addObjectPropertyInverseOf( ontoRef.getOWLObjectProperty( prop), ontoRef.getOWLObjectProperty( inverse));
    }
    /**
     * Returns the changes required to add an inverse property to an object property.
     * Changes will be buffered if {@link #isChangeBuffering()} is {@code true}, else they will be applied immediately.
     * @param prop the direct object property.
     * @param inverse the inverse object property.
     * @return the change to be applied to set an inverse property to a direct property.
     */
    public OWLOntologyChange addObjectPropertyInverseOf( OWLObjectProperty prop, OWLObjectProperty inverse){
        long initialTime = System.nanoTime();
        try{
            OWLInverseObjectPropertiesAxiom inverseOf = ontoRef.getOWLFactory().getOWLInverseObjectPropertiesAxiom(prop, inverse);
            OWLOntologyChange add = getAddAxiom( inverseOf, manipulationBuffering);
            if( !manipulationBuffering)
                applyChanges( add);
            logger.addDebugString( "add object property (" + ontoRef.getOWLObjectName( prop) + ") inverse of (" + ontoRef.getOWLObjectName( inverse) + ")"
                    + " in: " + (System.nanoTime() - initialTime) + " [ns]");
            return( add);
        } catch( org.semanticweb.owlapi.reasoner.InconsistentOntologyException e){
            ontoRef.logInconsistency();
            return( null);
        }
    }


    /**
     * Returns the changes required to add a data property and its value to an individual.
     * If the individual does not exists, it is created.
     * Changes will be buffered if {@link #isChangeBuffering()} is {@code true}, else they will be applied immediately.
     * @param ind individual the property must be assigned to.
     * @param prop data property to be added.
     * @param value literal value to be assigned to the data property.
     * @return changes required to the ontology required to add the property.
     * Returned object can be ignored while working in buffering mode.
     */
    public OWLOntologyChange addDataPropertyB2Individual(OWLNamedIndividual ind, OWLDataProperty prop, OWLLiteral value) {
        try{
            long initialTime = System.nanoTime();
            OWLAxiom newAxiom = ontoRef.getOWLFactory().getOWLDataPropertyAssertionAxiom( prop, ind, value);
            OWLOntologyChange add = getAddAxiom( newAxiom, manipulationBuffering);
            if( !manipulationBuffering)
                applyChanges( add);
            logger.addDebugString( "add data property (" + ontoRef.getOWLObjectName( prop) + ") belong to individual"
                    + "(" + ontoRef.getOWLObjectName( ind) + ") with value (" + ontoRef.getOWLObjectName( value) + ") in: " + (System.nanoTime() - initialTime) + " [ns]");
            return( add);
        } catch( org.semanticweb.owlapi.reasoner.InconsistentOntologyException e){
            ontoRef.logInconsistency();
            return( null);
        }
    }
    /**
     * Returns the changes required to add a data property and its value to an individual.
     * If the individual does not exists, it is created.
     * Changes will be buffered if {@link #isChangeBuffering()} is {@code true}, else they will be applied immediately.
     * @param individualName name of the individual the property must be assigned to.
     * @param propertyName name of the data property to be added.
     * @param value literal value to be assigned to the data property.
     * @return  changes to the ontology required to add the property.
     * Returned object can be ignored while working in buffering mode.
     */
    public OWLOntologyChange addDataPropertyB2Individual( String individualName, String propertyName, Object value) {
        OWLNamedIndividual indiv = ontoRef.getOWLIndividual( individualName);
        OWLDataProperty prop = ontoRef.getOWLDataProperty( propertyName);
        OWLLiteral lit = ontoRef.getOWLLiteral( value, null);
        return( addDataPropertyB2Individual( indiv, prop, lit));
    }

    /**
     * Returns the changes required to add all the data properties and their values to an individual.
     * It relies on {@link #addDataPropertyB2Individual(OWLNamedIndividual, OWLDataProperty, OWLLiteral)},
     * where input parameters are respectively: {@link DataPropertyRelations#getIndividual()},
     * {@link DataPropertyRelations#getProperty()} and all {@link DataPropertyRelations#getValues()}.
     * Changes will be buffered if {@link #isChangeBuffering()} is {@code true}, else they will be applied immediately.
     * @param relations the container for all the data relations to add.
     * @return  changes to the ontology required to add the properties.
     * Returned object can be ignored while working in buffering mode.
     */
    public List< OWLOntologyChange> addDataPropertyB2Individual( DataPropertyRelations relations){
        List< OWLOntologyChange> changes = new ArrayList<>();
        for ( OWLLiteral l : relations.getValues())
            changes.add( addDataPropertyB2Individual( relations.getIndividual(), relations.getProperty(), l));
        return changes;
    }
    /**
     * Returns the changes required to add all the data properties and their values to an individual.
     * It calls {@link #addDataPropertyB2Individual(DataPropertyRelations)} for all the element in the
     * given set.
     * Changes will be buffered if {@link #isChangeBuffering()} is {@code true}, else they will be applied immediately.
     * @param relations the container for all the data relations to add.
     * @return  changes to the ontology required to add the properties.
     * Returned object can be ignored while working in buffering mode.
     */
    public List< OWLOntologyChange> addDataPropertyB2Individual( Set< DataPropertyRelations> relations){
        List< OWLOntologyChange> changes = new ArrayList<>();
        for ( DataPropertyRelations r : relations)
            changes.addAll( addDataPropertyB2Individual( r));
        return changes;
    }


    /**
     * Returns the changes required to classify an individual as belonging to a specific class.
     * If the individual or the class do not exists, they are created.
     * Changes will be buffered if {@link #isChangeBuffering()} is {@code true}, else they will be applied immediately.
     * @param ind individual to add into a class.
     * @param cls ontological class that must contain individual.
     * @return  changes to classify the individual in the given class.
     * Returned object can be ignored while working in buffering mode.
     */
    public OWLOntologyChange addIndividualB2Class(OWLNamedIndividual ind, OWLClass cls) {
        long initialTime = System.nanoTime();
        try{
            OWLAxiom newAxiom = ontoRef.getOWLFactory().getOWLClassAssertionAxiom( cls, ind);
            OWLOntologyChange add = getAddAxiom( newAxiom, manipulationBuffering);
            if( !manipulationBuffering)
                applyChanges(add);
            logger.addDebugString( "add individual (" + ontoRef.getOWLObjectName( ind) + ") belong to class (" + ontoRef.getOWLObjectName( cls) + ") in: " + (System.nanoTime() - initialTime) + " [ns]");
            return( add);
        } catch( org.semanticweb.owlapi.reasoner.InconsistentOntologyException e){
            ontoRef.logInconsistency();
            return( null);
        }

    }
    /**
     * Returns the changes required to classify an individual as belonging to a specific class.
     * If the individual or the class do not exists, they are created.
     * Changes will be buffered if {@link #isChangeBuffering()} is {@code true}, else they will be applied immediately.
     * @param individualName name of the individual to add into a class.
     * @param className name of the ontological class that must contain individual.
     * @return  changes to classify the individual in the given class.
     * Returned object can be ignored while working in buffering mode.
     */
    public OWLOntologyChange addIndividualB2Class(String individualName, String className) {
        OWLNamedIndividual indiv = ontoRef.getOWLIndividual( individualName);
        OWLClass cl = ontoRef.getOWLClass( className);
        return( addIndividualB2Class( indiv, cl));
    }

    /**
     * Returns the changes required to add in individual to the ontology.
     * This is equivalent to add a child to the top class {@code OWLThing}).
     * If the individual does not exists, it is created.
     * Changes will be buffered if {@link #isChangeBuffering()} is {@code true}, else they will be applied immediately.
     * @param individualName name of the individual to be added into the ontology.
     * @return changes required to to add an individual to the ontology.
     * Returned object can be ignored while working in buffering mode.
     */
    public OWLOntologyChange addIndividual( String individualName){
        return addIndividual( ontoRef.getOWLIndividual(individualName));
    }
    /**
     * Returns the changes required to add in individual to the ontology.
     * This is equivalent to add a child to the top class {@code OWLThing}).
     * Changes will be buffered if {@link #isChangeBuffering()} is {@code true}, else they will be applied immediately.
     * @param ind individual to be added into the ontology.
     * @return changes required to to add an individual to the ontology.
     * Returned object can be ignored while working in buffering mode.
     */
    public OWLOntologyChange addIndividual( OWLNamedIndividual ind){
        OWLClass things = ontoRef.getOWLFactory().getOWLThing();
        return addIndividualB2Class( ind, things);
    }

    /**
     * Returns the changes required to set a class as sub-class of another class.
     * If either class does not exists, it is created.
     * Changes will be buffered if {@link #isChangeBuffering()} is {@code true}, else they will be applied immediately.
     * @param superClass the super-class.
     * @param subClass the sub-class.
     * @return changes required to add a class as sub-class of another class.
     * Returned object can be ignored while working in buffering mode.
     */
    public OWLOntologyChange addSubClassOf( OWLClass superClass, OWLClass subClass){
        try{
            long initialTime = System.nanoTime();
            OWLSubClassOfAxiom subClAxiom = ontoRef.getOWLFactory().getOWLSubClassOfAxiom( subClass, superClass);
            OWLOntologyChange adding = getAddAxiom( subClAxiom, manipulationBuffering);
            if( !manipulationBuffering)
                applyChanges( adding);
            logger.addDebugString( "set sub class (" + ontoRef.getOWLObjectName( subClass) + ") of super class (" + ontoRef.getOWLObjectName( superClass) + ") in: " + (System.nanoTime() - initialTime) + " [ns]");
            return( adding);
        } catch( org.semanticweb.owlapi.reasoner.InconsistentOntologyException e){
            ontoRef.logInconsistency();
            return( null);
        }
    }
    /**
     * Returns the changes required to set a class as sub-class of another class.
     * If either class does not exists, it is created.
     * Changes will be buffered if {@link #isChangeBuffering()} is {@code true}, else they will be applied immediately.
     * @param superClassName name of the ontological super-class.
     * @param subClassName name of the ontological sub-class
     * @return  changes to add a class as sub-class of another class.
     * Returned object can be ignored while working in buffering mode.
     */
    public OWLOntologyChange addSubClassOf( String superClassName, String subClassName){
        OWLClass sup = ontoRef.getOWLClass( superClassName);
        OWLClass sub = ontoRef.getOWLClass( subClassName);
        return( addSubClassOf( sup, sub));
    }

    /**
     * Returns the changes required to add a class to the ontology.
     * Changes will be buffered if {@link #isChangeBuffering()} is {@code true}, else they will be applied immediately.
     * @param cls class to be added to the ontology.
     * @return changes required to add the class to the ontology.
     * Returned object can be ignored while working in buffering mode.
     */
    public OWLOntologyChange addClass( OWLClass cls){
        OWLClass think = ontoRef.getOWLFactory().getOWLThing();
        return addSubClassOf(think, cls);
    }
    /**
     * Returns the changes required to add a class to the ontology.
     * Changes will be buffered if {@link #isChangeBuffering()} is {@code true}, else they will be applied immediately.
     * @param className name of the class to be added to the ontology.
     * @return changes required to add the class to the ontology.
     * Returned object can be ignored while working in buffering mode.
     */
    public OWLOntologyChange addClass( String className){
        return addClass( ontoRef.getOWLClass( className));
    }

    /**
     * Returns the changes required to set a property as sub-property of another data property.
     * If either property does not exists, it is created.
     * Changes will be buffered if {@link #isChangeBuffering()} is {@code true}, else they will be applied immediately.
     * @param superProperty the super data property.
     * @param subProperty the sub data property.
     * @return changes required to add a data property as sub-property of another data property.
     * Returned object can be ignored while working in buffering mode.
     */
    public OWLOntologyChange addSubDataPropertyOf(OWLDataProperty superProperty, OWLDataProperty subProperty){
        try{
            long initialTime = System.nanoTime();
            OWLSubDataPropertyOfAxiom subPropAxiom = ontoRef.getOWLFactory().getOWLSubDataPropertyOfAxiom(subProperty, superProperty);
            OWLOntologyChange adding = getAddAxiom( subPropAxiom, manipulationBuffering);
            if( !manipulationBuffering)
                applyChanges( adding);
            logger.addDebugString( "set sub data property (" + ontoRef.getOWLObjectName( superProperty) + ") of super property (" + ontoRef.getOWLObjectName( subProperty) + ") in: " + (System.nanoTime() - initialTime) + " [ns]");
            return( adding);
        } catch( org.semanticweb.owlapi.reasoner.InconsistentOntologyException e){
            ontoRef.logInconsistency();
            return( null);
        }
    }
    /**
     * Returns the changes required to set a property as sub-property of another data property.
     * If either property does not exists, it is created.
     * Changes will be buffered if {@link #isChangeBuffering()} is {@code true}, else they will be applied immediately.
     * @param superPropertyName the name of the super data property.
     * @param subPropertyName the name of sub data property.
     * @return changes required to add a data property as sub-property of another data property.
     * Returned object can be ignored while working in buffering mode.
     */
    public OWLOntologyChange addSubDataPropertyOf(String superPropertyName, String subPropertyName){
        OWLDataProperty sup = ontoRef.getOWLDataProperty( superPropertyName);
        OWLDataProperty sub = ontoRef.getOWLDataProperty( subPropertyName);
        return( addSubDataPropertyOf( sup, sub));
    }

    /**
     * Returns the changes required to set a property as sub-property of another data property.
     * If either property does not exists, it is created.
     * Changes will be buffered if {@link #isChangeBuffering()} is {@code true}, else they will be applied immediately.
     * @param superProperty the super object property.
     * @param subProperty the sub object property.
     * @return changes required to add a object property as sub-property of another object property.
     * Returned object can be ignored while working in buffering mode.
     */
    public OWLOntologyChange addSubObjectPropertyOf(OWLObjectProperty superProperty, OWLObjectProperty subProperty){
        try{
            long initialTime = System.nanoTime();
            OWLSubObjectPropertyOfAxiom subPropAxiom = ontoRef.getOWLFactory().getOWLSubObjectPropertyOfAxiom(subProperty, superProperty);
            OWLOntologyChange adding = getAddAxiom( subPropAxiom, manipulationBuffering);
            if( !manipulationBuffering)
                applyChanges( adding);
            logger.addDebugString( "set sub object property (" + ontoRef.getOWLObjectName( superProperty) + ") of super property (" + ontoRef.getOWLObjectName( subProperty) + ") in: " + (System.nanoTime() - initialTime) + " [ns]");
            return( adding);
        } catch( org.semanticweb.owlapi.reasoner.InconsistentOntologyException e){
            ontoRef.logInconsistency();
            return( null);
        }
    }
    /**
     * Returns the changes required to set a property as sub-property of another object property.
     * If either property does not exists, it is created.
     * Changes will be buffered if {@link #isChangeBuffering()} is {@code true}, else they will be applied immediately.
     * @param superPropertyName the name of the super object property.
     * @param subPropertyName the name of sub object property.
     * @return changes required to add an object property as sub-property of another object property.
     * Returned object can be ignored while working in buffering mode.
     */
    public OWLOntologyChange addSubObjectPropertyOf(String superPropertyName, String subPropertyName){
        OWLObjectProperty sup = ontoRef.getOWLObjectProperty( superPropertyName);
        OWLObjectProperty sub = ontoRef.getOWLObjectProperty( subPropertyName);
        return( addSubObjectPropertyOf( sup, sub));
    }

    /**
     * It returns the changes to be made in the ontology in order to add the
     * given restriction.
     * See {@link SemanticRestriction} for information on how to add restriction
     * on class definition ({@link #convertSuperClassesToEquivalentClass(OWLClass)} should also be called),
     * as well as data or object property domain and range.
     * Changes will be buffered if {@link #isChangeBuffering()} is {@code true}, else they will be applied immediately.
     * @param restriction the restriction to add.
     * @return th change for adding the restriction.
     */
    public OWLOntologyChange addRestriction( SemanticRestriction restriction){
        return restriction.addRestriction( this);
    }
    /**
     * It returns the changes to be made in the ontology in order to add all the
     * given restrictions.
     * See {@link SemanticRestriction} for information on how to add restriction
     * on class definition ({@link #convertSuperClassesToEquivalentClass(OWLClass)} should also be called),
     * as well as data or object property domain and range.
     * Changes will be buffered if {@link #isChangeBuffering()} is {@code true}, else they will be applied immediately.
     * @param restriction the restrictions to add.
     * @return th change for adding the restrictions.
     */
    public List< OWLOntologyChange> addRestriction( Set<SemanticRestriction> restriction){
        List<OWLOntologyChange> out = new ArrayList<>();
        for ( SemanticRestriction r : restriction)
            out.add( addRestriction( r));
        return out;
    }


    /**
     * Given a class {@code C}, it uses {@link org.semanticweb.owlapi.change.ConvertEquivalentClassesToSuperClasses}
     * to convert all the sub class axioms of {@code C} into a conjunctions of expressions
     * in the definition of the class itself.
     * @param className the name of the class to be converted from sub classing to equivalent expression.
     * @return the changes to be applied in order to make all the sub axioms of a class being
     * the conjunction of its equivalent expression.
     */
    public List<OWLOntologyChange> convertSuperClassesToEquivalentClass(String className){
        return convertSuperClassesToEquivalentClass( ontoRef.getOWLClass( className));
    }
    /**
     * Given a class {@code C}, it uses {@link org.semanticweb.owlapi.change.ConvertEquivalentClassesToSuperClasses}
     * to convert all the sub class axioms of {@code C} into a conjunctions of expressions
     * in the definition of the class itself.
     * @param cl the class to be converted from sub classing to equivalent expression.
     * @return the changes to be applied in order to make all the sub axioms of a class being
     * the conjunction of its equivalent expression.
     */
    public List<OWLOntologyChange> convertSuperClassesToEquivalentClass(OWLClass cl){
        try {
            long initialTime = System.nanoTime();
            Set<OWLOntology> onts = new HashSet<>();
            onts.add( ontoRef.getOWLOntology());
            List<OWLOntologyChange> changes = new ConvertSuperClassesToEquivalentClass( ontoRef.getOWLFactory(), cl, onts, ontoRef.getOWLOntology()).getChanges();
            for( OWLOntologyChange c : changes) {
                if ( ! manipulationBuffering)
                    applyChanges( c);
                changeList.add( c);
            }
            logger.addDebugString( "converting super class: " + ontoRef.getOWLObjectName( cl) + " to equivalent class" +
                    " in: " + (System.nanoTime() - initialTime) + " [ns]");
            return( changes);
        } catch( org.semanticweb.owlapi.reasoner.InconsistentOntologyException e){
            ontoRef.logInconsistency();
            return( null);
        }
    }


    /**
     * It returns the changes to be made in the ontology in order to
     * make a data property functional.
     * The property will be created if it does not exists.
     * Changes will be buffered if {@link #isChangeBuffering()} is {@code true},
     * else they will be applied immediately.
     * @param propertyName the name of the property to make functional.
     * @return th change for adding functional directive to a data property.
     */
    public OWLOntologyChange addFunctionalDataProperty( String propertyName){
        return addFunctionalDataProperty( ontoRef.getOWLDataProperty( propertyName));
    }
    /**
     * It returns the changes to be made in the ontology in order to
     * make a data property functional.
     * The property will be created if it does not exists.
     * Changes will be buffered if {@link #isChangeBuffering()} is {@code true},
     * else they will be applied immediately.
     * @param property the property to make functional.
     * @return th change for adding functional directive to a data property.
     */
    public OWLOntologyChange addFunctionalDataProperty( OWLDataProperty property){
        try{
            long initialTime = System.nanoTime();
            OWLFunctionalDataPropertyAxiom funcAx = ontoRef.getOWLFactory().getOWLFunctionalDataPropertyAxiom(property);
            OWLOntologyChange ad = getAddAxiom(funcAx);
            if ( ! manipulationBuffering)
                applyChanges( ad);
            logger.addDebugString( "add functional data property: " + ontoRef.getOWLObjectName( property) +
                    " in: " + (System.nanoTime() - initialTime) + " [ns]");
            return ad;
        } catch( org.semanticweb.owlapi.reasoner.InconsistentOntologyException e){
            ontoRef.logInconsistency();
            return( null);
        }
    }

    /**
     * It returns the changes to be made in the ontology in order to
     * make an object property functional.
     * The property will be created if it does not exists.
     * Changes will be buffered if {@link #isChangeBuffering()} is {@code true},
     * else they will be applied immediately.
     * @param propertyName the name of the property to make functional.
     * @return th change for adding functional directive to an object property.
     */
    public OWLOntologyChange addFunctionalObjectProperty( String propertyName){
        return addFunctionalObjectProperty( ontoRef.getOWLObjectProperty( propertyName));
    }
    /**
     * It returns the changes to be made in the ontology in order to
     * make an object property functional.
     * The property will be created if it does not exists.
     * Changes will be buffered if {@link #isChangeBuffering()} is {@code true},
     * else they will be applied immediately.
     * @param property the property to make functional.
     * @return th change for adding functional directive to an object property.
     */
    public OWLOntologyChange addFunctionalObjectProperty( OWLObjectProperty property){
        try{
            long initialTime = System.nanoTime();
            OWLFunctionalObjectPropertyAxiom funcAx = ontoRef.getOWLFactory().getOWLFunctionalObjectPropertyAxiom(property);
            OWLOntologyChange ad = getAddAxiom(funcAx);
            if ( ! manipulationBuffering)
                applyChanges( ad);
            logger.addDebugString( "add functional object property: " + ontoRef.getOWLObjectName( property) +
                    " in: " + (System.nanoTime() - initialTime) + " [ns]");
            return ad;
        } catch( org.semanticweb.owlapi.reasoner.InconsistentOntologyException e){
            ontoRef.logInconsistency();
            return( null);
        }
    }

    /**
     * It returns the changes to be made in the ontology in order to
     * make an object property inverse functional.
     * The property will be created if it does not exists.
     * Changes will be buffered if {@link #isChangeBuffering()} is {@code true},
     * else they will be applied immediately.
     * @param propertyName the name of the property to make functional.
     * @return th change for adding inverse functional directive to an object property.
     */
    public OWLOntologyChange addInverseFunctionalObjectProperty( String propertyName){
        return addInverseFunctionalObjectProperty( ontoRef.getOWLObjectProperty( propertyName));
    }
    /**
     * It returns the changes to be made in the ontology in order to
     * make an object property inverse functional.
     * The property will be created if it does not exists.
     * Changes will be buffered if {@link #isChangeBuffering()} is {@code true},
     * else they will be applied immediately.
     * @param property the property to make functional.
     * @return th change for adding inverse functional directive to an object property.
     */
    public OWLOntologyChange addInverseFunctionalObjectProperty( OWLObjectProperty property){
        try{
            long initialTime = System.nanoTime();
            OWLInverseFunctionalObjectPropertyAxiom funcAx = ontoRef.getOWLFactory().getOWLInverseFunctionalObjectPropertyAxiom(property);
            OWLOntologyChange ad = getAddAxiom(funcAx);
            if ( ! manipulationBuffering)
                applyChanges( ad);
            logger.addDebugString( "add inverse functional object property: " + ontoRef.getOWLObjectName( property) +
                    " in: " + (System.nanoTime() - initialTime) + " [ns]");
            return ad;
        } catch( org.semanticweb.owlapi.reasoner.InconsistentOntologyException e){
            ontoRef.logInconsistency();
            return( null);
        }
    }

    /**
     * It returns the changes to be made in the ontology in order to
     * make an object property transitive.
     * The property will be created if it does not exists.
     * Changes will be buffered if {@link #isChangeBuffering()} is {@code true},
     * else they will be applied immediately.
     * @param propertyName the name of the property to make functional.
     * @return th change for adding transitive directive to an object property.
     */
    public OWLOntologyChange addTransitiveObjectProperty( String propertyName){
        return addTransitiveObjectProperty( ontoRef.getOWLObjectProperty( propertyName));
    }
    /**
     * It returns the changes to be made in the ontology in order to
     * make an object property transitive.
     * The property will be created if it does not exists.
     * Changes will be buffered if {@link #isChangeBuffering()} is {@code true},
     * else they will be applied immediately.
     * @param property the property to make functional.
     * @return th change for adding transitive directive to an object property.
     */
    public OWLOntologyChange addTransitiveObjectProperty( OWLObjectProperty property){
        try{
            long initialTime = System.nanoTime();
            OWLTransitiveObjectPropertyAxiom funcAx = ontoRef.getOWLFactory().getOWLTransitiveObjectPropertyAxiom(property);
            OWLOntologyChange ad = getAddAxiom(funcAx);
            if ( ! manipulationBuffering)
                applyChanges( ad);
            logger.addDebugString( "add transitive object property: " + ontoRef.getOWLObjectName( property) +
                    " in: " + (System.nanoTime() - initialTime) + " [ns]");
            return ad;
        } catch( org.semanticweb.owlapi.reasoner.InconsistentOntologyException e){
            ontoRef.logInconsistency();
            return( null);
        }
    }

    /**
     * It returns the changes to be made in the ontology in order to
     * make an object property symmetric.
     * The property will be created if it does not exists.
     * Changes will be buffered if {@link #isChangeBuffering()} is {@code true},
     * else they will be applied immediately.
     * @param propertyName the name of the property to make functional.
     * @return th change for adding symmetric directive to an object property.
     */
    public OWLOntologyChange addSymmetricObjectProperty( String propertyName){
        return addSymmetricObjectProperty( ontoRef.getOWLObjectProperty( propertyName));
    }
    /**
     * It returns the changes to be made in the ontology in order to
     * make an object property symmetric.
     * The property will be created if it does not exists.
     * Changes will be buffered if {@link #isChangeBuffering()} is {@code true},
     * else they will be applied immediately.
     * @param property the property to make functional.
     * @return th change for adding symmetric directive to an object property.
     */
    public OWLOntologyChange addSymmetricObjectProperty( OWLObjectProperty property){
        try{
            long initialTime = System.nanoTime();
            OWLSymmetricObjectPropertyAxiom funcAx = ontoRef.getOWLFactory().getOWLSymmetricObjectPropertyAxiom(property);
            OWLOntologyChange ad = getAddAxiom(funcAx);
            if ( ! manipulationBuffering)
                applyChanges( ad);
            logger.addDebugString( "add symmetric object property: " + ontoRef.getOWLObjectName( property) +
                    " in: " + (System.nanoTime() - initialTime) + " [ns]");
            return ad;
        } catch( org.semanticweb.owlapi.reasoner.InconsistentOntologyException e){
            ontoRef.logInconsistency();
            return( null);
        }
    }

    /**
     * It returns the changes to be made in the ontology in order to
     * make an object property asymmetric.
     * The property will be created if it does not exists.
     * Changes will be buffered if {@link #isChangeBuffering()} is {@code true},
     * else they will be applied immediately.
     * @param propertyName the name of the property to make functional.
     * @return th change for adding asymmetric directive to an object property.
     */
    public OWLOntologyChange addAsymmetricObjectProperty( String propertyName){
        return addAsymmetricObjectProperty( ontoRef.getOWLObjectProperty( propertyName));
    }
    /**
     * It returns the changes to be made in the ontology in order to
     * make an object property asymmetric.
     * The property will be created if it does not exists.
     * Changes will be buffered if {@link #isChangeBuffering()} is {@code true},
     * else they will be applied immediately.
     * @param property the property to make functional.
     * @return th change for adding asymmetric directive to an object property.
     */
    public OWLOntologyChange addAsymmetricObjectProperty( OWLObjectProperty property){
        try{
            long initialTime = System.nanoTime();
            OWLAsymmetricObjectPropertyAxiom funcAx = ontoRef.getOWLFactory().getOWLAsymmetricObjectPropertyAxiom(property);
            OWLOntologyChange ad = getAddAxiom(funcAx);
            if ( ! manipulationBuffering)
                applyChanges( ad);
            logger.addDebugString( "add asymmetric object property: " + ontoRef.getOWLObjectName( property) +
                    " in: " + (System.nanoTime() - initialTime) + " [ns]");
            return ad;
        } catch( org.semanticweb.owlapi.reasoner.InconsistentOntologyException e){
            ontoRef.logInconsistency();
            return( null);
        }
    }

    /**
     * It returns the changes to be made in the ontology in order to
     * make an object property reflexive.
     * The property will be created if it does not exists.
     * Changes will be buffered if {@link #isChangeBuffering()} is {@code true},
     * else they will be applied immediately.
     * @param propertyName the name of the property to make functional.
     * @return th change for adding reflexive directive to an object property.
     */
    public OWLOntologyChange addReflexiveObjectProperty(String propertyName){
        return addReflexiveObjectProperty( ontoRef.getOWLObjectProperty( propertyName));
    }
    /**
     * It returns the changes to be made in the ontology in order to
     * make an object property reflexive.
     * The property will be created if it does not exists.
     * Changes will be buffered if {@link #isChangeBuffering()} is {@code true},
     * else they will be applied immediately.
     * @param property the property to make functional.
     * @return th change for adding reflexive directive to an object property.
     */
    public OWLOntologyChange addReflexiveObjectProperty( OWLObjectProperty property){
        try{
            long initialTime = System.nanoTime();
            OWLReflexiveObjectPropertyAxiom funcAx = ontoRef.getOWLFactory().getOWLReflexiveObjectPropertyAxiom(property);
            OWLOntologyChange ad = getAddAxiom(funcAx);
            if ( ! manipulationBuffering)
                applyChanges( ad);
            logger.addDebugString( "add reflexive object property: " + ontoRef.getOWLObjectName( property) +
                    " in: " + (System.nanoTime() - initialTime) + " [ns]");
            return ad;
        } catch( org.semanticweb.owlapi.reasoner.InconsistentOntologyException e){
            ontoRef.logInconsistency();
            return( null);
        }
    }

    /**
     * It returns the changes to be made in the ontology in order to
     * make an object property irreflexive.
     * The property will be created if it does not exists.
     * Changes will be buffered if {@link #isChangeBuffering()} is {@code true},
     * else they will be applied immediately.
     * @param propertyName the name of the property to make functional.
     * @return th change for adding irreflexive directive to an object property.
     */
    public OWLOntologyChange addIrreflexiveObjectProperty(String propertyName){
        return addIrreflexiveObjectProperty( ontoRef.getOWLObjectProperty( propertyName));
    }
    /**
     * It returns the changes to be made in the ontology in order to
     * make an object property irreflexive.
     * The property will be created if it does not exists.
     * Changes will be buffered if {@link #isChangeBuffering()} is {@code true},
     * else they will be applied immediately.
     * @param property the property to make functional.
     * @return th change for adding irreflexive directive to an object property.
     */
    public OWLOntologyChange addIrreflexiveObjectProperty( OWLObjectProperty property){
        try{
            long initialTime = System.nanoTime();
            OWLIrreflexiveObjectPropertyAxiom funcAx = ontoRef.getOWLFactory().getOWLIrreflexiveObjectPropertyAxiom(property);
            OWLOntologyChange ad = getAddAxiom(funcAx);
            if ( ! manipulationBuffering)
                applyChanges( ad);
            logger.addDebugString( "add irreflexive object property: " + ontoRef.getOWLObjectName( property) +
                    " in: " + (System.nanoTime() - initialTime) + " [ns]");
            return ad;
        } catch( org.semanticweb.owlapi.reasoner.InconsistentOntologyException e){
            ontoRef.logInconsistency();
            return( null);
        }
    }


    // ---------------------------   methods for removing entities to the ontology
    /**
     * Returns the changes required to remove an object property instance with a specific value from an individual.
     * Unlike addition manipulations, if an entity does not exists, it will not be created automatically. 
     * Changes will be buffered if {@link #isChangeBuffering()} is {@code true}, else they will be applied immediately.
     * @param ind individual from which to remove a given object property instance.
     * @param prop object property whose instance should be removed.
     * @param value value individual characterizing the instance to be removed.
     * @return changes required to remove the given object property instance from an individual.
     * Returned object can be ignored while working in buffering mode.
     */
    public OWLOntologyChange removeObjectPropertyB2Individual( OWLNamedIndividual ind, OWLObjectProperty prop, OWLNamedIndividual value){
        try{
            long initialTime = System.nanoTime();
            OWLAxiom propertyAssertion = ontoRef.getOWLFactory().getOWLObjectPropertyAssertionAxiom( prop, ind, value);
            OWLOntologyChange remove = getRemoveAxiom( propertyAssertion, manipulationBuffering);
            if( !manipulationBuffering)
                applyChanges(remove);
            logger.addDebugString( "remove object property (" + ontoRef.getOWLObjectName( prop) + ") belong to individual (" + ontoRef.getOWLObjectName( ind) + ") "
                    + " with value (" + ontoRef.getOWLObjectName( value) + ") in: " + (System.nanoTime() - initialTime) + " [ns]");
            return( remove);
        } catch( org.semanticweb.owlapi.reasoner.InconsistentOntologyException e){
            ontoRef.logInconsistency();
            return( null);
        }

    }
    /**
     * Returns the changes required to remove an object property instance with a specific value from an individual.
     * Unlike addition manipulations, if an entity does not exists, it will not be created automatically. 
     * Changes will be buffered if {@link #isChangeBuffering()} is {@code true}, else they will be applied immediately.
     * @param individualName name of individual from which to remove a given object property instance.
     * @param propName name of object property whose instance should be removed.
     * @param valueName name of value individual characterizing the instance to be removed.
     * @return changes required to remove the given object property instance from an individual.
     * Returned object can be ignored while working in buffering mode.
     */
    public OWLOntologyChange removeObjectPropertyB2Individual( String individualName, String propName, String valueName){
        OWLNamedIndividual indiv = ontoRef.getOWLIndividual( individualName);
        OWLObjectProperty prop = ontoRef.getOWLObjectProperty( propName);
        OWLNamedIndividual val = ontoRef.getOWLIndividual( valueName);
        return( removeObjectPropertyB2Individual( indiv, prop, val));
    }

    /**
     * Returns the changes required to remove all the object properties and their values from an individual.
     * It relies on {@link #removeObjectPropertyB2Individual(OWLNamedIndividual, OWLObjectProperty, OWLNamedIndividual)},
     * where input parameters are respectively: {@link ObjectPropertyRelations#getIndividual()},
     * {@link ObjectPropertyRelations#getProperty()} and all {@link ObjectPropertyRelations#getValues()}.
     * Changes will be buffered if {@link #isChangeBuffering()} is {@code true}, else they will be applied immediately.
     * @param relations the container for all the object relations to remove.
     * @return  changes to the ontology required to remove the properties.
     * Returned object can be ignored while working in buffering mode.
     */
    public List< OWLOntologyChange> removeObjectPropertyB2Individual( ObjectPropertyRelations relations){
        List< OWLOntologyChange> changes = new ArrayList<>();
        for ( OWLNamedIndividual l : relations.getValues())
            changes.add( removeObjectPropertyB2Individual( relations.getIndividual(), relations.getProperty(), l));
        return changes;
    }
    /**
     * Returns the changes required to remove all the object properties and their values from an individual.
     * It calls {@link #removeObjectPropertyB2Individual(ObjectPropertyRelations)} for all the element in the
     * given set.
     * Changes will be buffered if {@link #isChangeBuffering()} is {@code true}, else they will be applied immediately.
     * @param relations the container for all the object relations to remove.
     * @return  changes to the ontology required to remove the properties.
     * Returned object can be ignored while working in buffering mode.
     */
    public List< OWLOntologyChange> removeObjectPropertyB2Individual( Set< ObjectPropertyRelations> relations){
        List< OWLOntologyChange> changes = new ArrayList<>();
        for ( ObjectPropertyRelations r : relations)
            changes.addAll( removeObjectPropertyB2Individual( r));
        return changes;
    }


    /**
     * Returns the changes required to add an inverse property to an object property.
     * Changes will be buffered if {@link #isChangeBuffering()} is {@code true}, else they will be applied immediately.
     * @param prop the name of the direct object property.
     * @param inverse the name of the inverse object property.
     * @return the change to be applied to set an inverse property to a direct property.
     */
    public OWLOntologyChange removeObjectPropertyInverseOf( String prop, String inverse){
        return removeObjectPropertyInverseOf( ontoRef.getOWLObjectProperty( prop), ontoRef.getOWLObjectProperty( inverse));
    }
    /**
     * Returns the changes required to remove an inverse property to an object property.
     * Changes will be buffered if {@link #isChangeBuffering()} is {@code true}, else they will be applied immediately.
     * @param prop the direct object property.
     * @param inverse the inverse object property.
     * @return the change to be applied to set an inverse property to a direct property.
     */
    public OWLOntologyChange removeObjectPropertyInverseOf( OWLObjectProperty prop, OWLObjectProperty inverse){
        long initialTime = System.nanoTime();
        try{
            OWLInverseObjectPropertiesAxiom inverseOf = ontoRef.getOWLFactory().getOWLInverseObjectPropertiesAxiom(prop, inverse);
            OWLOntologyChange remove = getRemoveAxiom( inverseOf, manipulationBuffering);
            if( !manipulationBuffering)
                applyChanges( remove);
            logger.addDebugString( "remove object property (" + ontoRef.getOWLObjectName( prop) + ") inverse of (" + ontoRef.getOWLObjectName( inverse) + ")"
                    + " in: " + (System.nanoTime() - initialTime) + " [ns]");
            return( remove);
        } catch( org.semanticweb.owlapi.reasoner.InconsistentOntologyException e){
            ontoRef.logInconsistency();
            return( null);
        }
    }

    /**
     * Returns the changes required to remove a data property instance with a specific value from an individual.
     * Unlike addition manipulations, if an entity does not exists, it will not be created automatically. 
     * Changes will be buffered if {@link #isChangeBuffering()} is {@code true}, else they will be applied immediately.
     * @param ind individual from which remove the given data property.
     * @param prop data property to be removed.
     * @param value literal which is the value of the given data property.
     * @return changes required to remove a specific data property from an individual.
     * Returned object can be ignored while working in buffering mode.
     */
    public OWLOntologyChange removeDataPropertyB2Individual(OWLNamedIndividual ind, OWLDataProperty prop, OWLLiteral value) {
        long initialTime = System.nanoTime();
        try{
            OWLAxiom newAxiom = ontoRef.getOWLFactory().getOWLDataPropertyAssertionAxiom( prop, ind, value);
            OWLOntologyChange remove = getRemoveAxiom( newAxiom, manipulationBuffering);
            if( !manipulationBuffering)
                applyChanges( remove);
            logger.addDebugString( "remove data property (" + ontoRef.getOWLObjectName( prop) + ") belong to individual "
                    + "(" + ontoRef.getOWLObjectName( ind) + ") with value (" + ontoRef.getOWLObjectName( value) + ") in: " + (System.nanoTime() - initialTime) + " [ns]");
            return( remove);
        } catch( org.semanticweb.owlapi.reasoner.InconsistentOntologyException e){
            ontoRef.logInconsistency();
            return( null);
        }
    }
    /**
     * Returns the changes required to remove a data property instance with a specific value from an individual.
     * Unlike addition manipulations, if an entity does not exists, it will not be created automatically. 
     * Changes will be buffered if {@link #isChangeBuffering()} is {@code true}, else they will be applied immediately.
     * @param individualName the name of the individual from which remove the given data property.
     * @param propertyName name of the data property to be removed.
     * @param value literal to be removed as the value of a data property.
     * @return changes required to remove a specific data property from an individual.
     * Returned object can be ignored while working in buffering mode.
     */
    public OWLOntologyChange removeDataPropertyB2Individual( String individualName, String propertyName, Object value) {
        OWLNamedIndividual indiv = ontoRef.getOWLIndividual( individualName);
        OWLDataProperty prop = ontoRef.getOWLDataProperty( propertyName);
        OWLLiteral lit = ontoRef.getOWLLiteral( value, null);
        return( removeDataPropertyB2Individual( indiv, prop, lit));
    }

    /**
     * Returns the changes required to remove all the data properties and their values from an individual.
     * It relies on {@link #removeDataPropertyB2Individual(OWLNamedIndividual, OWLDataProperty, OWLLiteral)},
     * where input parameters are respectively: {@link DataPropertyRelations#getIndividual()},
     * {@link DataPropertyRelations#getProperty()} and all {@link DataPropertyRelations#getValues()}.
     * Changes will be buffered if {@link #isChangeBuffering()} is {@code true}, else they will be applied immediately.
     * @param relations the container for all the data relations to remove.
     * @return  changes to the ontology required to remove the properties.
     * Returned object can be ignored while working in buffering mode.
     */
    public List< OWLOntologyChange> removeDataPropertyB2Individual( DataPropertyRelations relations){
        List< OWLOntologyChange> changes = new ArrayList<>();
        for ( OWLLiteral l : relations.getValues())
            changes.add( removeDataPropertyB2Individual( relations.getIndividual(), relations.getProperty(), l));
        return changes;
    }
    /**
     * Returns the changes required to remove all the data properties and their values to an individual.
     * It calls {@link #removeDataPropertyB2Individual(DataPropertyRelations)} for all the element in the
     * given set.
     * Changes will be buffered if {@link #isChangeBuffering()} is {@code true}, else they will be applied immediately.
     * @param relations the container for all the data relations to remove.
     * @return  changes to the ontology required to remove the properties.
     * Returned object can be ignored while working in buffering mode.
     */
    public List< OWLOntologyChange> removeDataPropertyB2Individual( Set< DataPropertyRelations> relations){
        List< OWLOntologyChange> changes = new ArrayList<>();
        for ( DataPropertyRelations r : relations)
            changes.addAll( removeDataPropertyB2Individual( r));
        return changes;
    }

    /**
     * Returns the changes required to remove an individual from those belonging to a class.
     * Unlike addition manipulations, if an entity does not exists, it will not be created automatically.
     * Changes will be buffered if {@link #isChangeBuffering()} is {@code true}, else they will be applied immediately.
     * @param ind individual to remove from a class.
     * @param cls class from which to remove the individual.
     * @return changes required to remove a specific individual from a class.
     * Returned object can be ignored while working in buffering mode.
     */
    public OWLOntologyChange removeIndividualB2Class(OWLNamedIndividual ind, OWLClass cls) {
        long initialTime = System.nanoTime();
        try{
            OWLAxiom newAxiom = ontoRef.getOWLFactory().getOWLClassAssertionAxiom( cls, ind);
            OWLOntologyChange remove = getRemoveAxiom( newAxiom, manipulationBuffering);
            if( !manipulationBuffering)
                applyChanges( remove);
            logger.addDebugString( "remove individual (" + ontoRef.getOWLObjectName( ind) + ") belong to class "
                    + "(" + ontoRef.getOWLObjectName( cls) + ")in: " + (System.nanoTime() - initialTime) + " [ns]");
            return( remove);
        } catch( org.semanticweb.owlapi.reasoner.InconsistentOntologyException e){
            ontoRef.logInconsistency();
            return( null);
        }
    }
    /**
     * Returns the changes required to remove an individual from those belonging to a class.
     * Unlike addition manipulations, if an entity does not exists, it will not be created automatically.
     * Changes will be buffered if {@link #isChangeBuffering()} is {@code true}, else they will be applied immediately.
     * @param individualName the name of the individual to remove from a class.
     * @param className the name of the class from which to remove the individual.
     * @return changes required to remove a specific individual from a class.
     * Returned object can be ignored while working in buffering mode.
     */
    public OWLOntologyChange removeIndividualB2Class(String individualName, String className) {
        OWLNamedIndividual indiv = ontoRef.getOWLIndividual( individualName);
        OWLClass cl = ontoRef.getOWLClass( className);
        return( removeIndividualB2Class( indiv, cl));
    }

    /**
     * Returns the changes required to remove an individual from the ontology.
     * Unlike addition manipulations, if an entity does not exists, it will not be created automatically.
     * Changes will be buffered if {@link #isChangeBuffering()} is {@code true}, else they will be applied immediately.
     * @param individual the individual to be removed.
     * @return changes required to remove an individual from the ontology.
     * Returned object can be ignored while working in buffering mode.
     */
    public List<RemoveAxiom> removeIndividual( OWLNamedIndividual individual){
        OWLEntityRemover remover = new OWLEntityRemover( Collections.singleton( ontoRef.getOWLOntology())); // ontoRef.getOWLManager(), Collections.singleton( ontoRef.getOWLOntology()));
        individual.accept(remover);
        long initialTime = System.nanoTime();
        List<RemoveAxiom> remove = remover.getChanges();
        if( !manipulationBuffering)
            applyChanges( remove);
        else changeList.addAll( remove);
        logger.addDebugString( "remove individual (" + ontoRef.getOWLObjectName( individual) + ") in: " + (System.nanoTime() - initialTime) + " [ns]");
        return( remove);
    }

    /**
     * Returns the changes required to remove a list of individuals from the ontology.
     * Unlike addition manipulations, if an entity does not exists, it will not be created automatically.
     * Changes will be buffered if {@link #isChangeBuffering()} is {@code true}, else they will be applied immediately.
     * @param individuals set of individuals to be removed.
     * @return changes required to remove a list of individuals from the ontology.
     * Returned object can be ignored while working in buffering mode.
     */
    public List<OWLOntologyChange> removeIndividual( Set< OWLNamedIndividual> individuals){
        List<OWLOntologyChange> changes = new ArrayList<OWLOntologyChange>();
        for( OWLNamedIndividual i : individuals)
            changes.addAll( removeIndividual( i));
        return( changes);
    }
    /**
     * Returns the changes required to remove an individual from the ontology.
     * Unlike addition manipulations, if an entity does not exists, it will not be created automatically.
     * Changes will be buffered if {@link #isChangeBuffering()} is {@code true}, else they will be applied immediately.
     * @param indName the name of the individual to be removed.
     * @return changes required to remove an individual from the ontology.
     * Returned object can be ignored while working in buffering mode.
     */
    public List<RemoveAxiom> removeIndividual( String indName){
        return removeIndividual( ontoRef.getOWLIndividual( indName));
    }

    /**
     * Returns the changes required to remove a class from those contained in a super-class.
     * Unlike addition manipulations, if an entity does not exists, it will not be created automatically.
     * Changes will be buffered if {@link #isChangeBuffering()} is {@code true}, else they will be applied immediately.
     * @param superClass super-class from which to remove the child.
     * @param subClass class to remove from super-class.
     * @return changes required to remove the sub-class from a given glass.
     * Returned object can be ignored while working in buffering mode.
     */
    public OWLOntologyChange removeSubClassOf( OWLClass superClass, OWLClass subClass){
        try{
            long initialTime = System.nanoTime();
            OWLSubClassOfAxiom subClAxiom = ontoRef.getOWLFactory().getOWLSubClassOfAxiom( subClass, superClass);
            OWLOntologyChange adding = getRemoveAxiom( subClAxiom, manipulationBuffering);

            if( !manipulationBuffering)
                applyChanges( adding);
            logger.addDebugString( "remove sub class (" + ontoRef.getOWLObjectName( subClass) + ") of super class (" + ontoRef.getOWLObjectName( superClass) + ") in: " + (System.nanoTime() - initialTime) + " [ns]");
            return( adding);
        } catch( org.semanticweb.owlapi.reasoner.InconsistentOntologyException e){
            ontoRef.logInconsistency();
        }
        return( null);
    }
    /**
     * Returns the changes required to remove a class from those contained in a super-class.
     * Unlike addition manipulations, if an entity does not exists, it will not be created automatically.
     * Changes will be buffered if {@link #isChangeBuffering()} is {@code true}, else they will be applied immediately.
     * @param superClassName name of the super-class from which to remove the child.
     * @param subClassName name of the class to remove from super-class.
     * @return changes required to remove the sub-class from a given glass.
     * Returned object can be ignored while working in buffering mode.
     */
    public OWLOntologyChange removeSubClassOf( String superClassName, String subClassName){
        OWLClass sup = ontoRef.getOWLClass( superClassName);
        OWLClass sub = ontoRef.getOWLClass( subClassName);
        return( removeSubClassOf( sup, sub));
    }

    /**
     * Returns the changes required to remove a class from the ontology.
     * Unlike addition manipulations, if an entity does not exists, it will not be created automatically.
     * Changes will be buffered if {@link #isChangeBuffering()} is {@code true}, else they will be applied immediately.
     * @param cls class to be removed from the ontology.
     * @return changes required to remove a class from the ontology.
     * Returned object can be ignored while working in buffering mode.
     */
    public OWLOntologyChange removeClass( OWLClass cls){
        try{
            long initialTime = System.nanoTime();
            OWLClass think = ontoRef.getOWLFactory().getOWLThing();
            OWLSubClassOfAxiom subClAxiom = ontoRef.getOWLFactory().getOWLSubClassOfAxiom( cls, think);
            OWLOntologyChange remove = getRemoveAxiom( subClAxiom, manipulationBuffering);
            if( !manipulationBuffering)
                applyChanges( remove);
            logger.addDebugString( "remove sub class (" + ontoRef.getOWLObjectName( cls) + ") in: " + (System.nanoTime() - initialTime) + " [ns]");
            return( remove);
        } catch( org.semanticweb.owlapi.reasoner.InconsistentOntologyException e){
            ontoRef.logInconsistency();
            return( null);
        }
    }
    /**
     * Returns the changes required to remove a class from the ontology.
     * Unlike addition manipulations, if an entity does not exists, it will not be created automatically.
     * Changes will be buffered if {@link #isChangeBuffering()} is {@code true}, else they will be applied immediately.
     * @param className name of the class to be removed from the ontology.
     * @return changes required to remove a class from the ontology.
     * Returned object can be ignored while working in buffering mode.
     */
    public OWLOntologyChange removeClass( String className){
        return removeClass( ontoRef.getOWLClass( className));
    }

    /**
     * Returns the changes required to remove the fact that a property is a sub-property of another data property.
     * If either property does not exists, it is created.
     * Unlike addition manipulations, if an entity does not exists, it will not be created automatically.
     * Changes will be buffered if {@link #isChangeBuffering()} is {@code true}, else they will be applied immediately.
     * @param superProperty the super data property, from which to remove the child.
     * @param subProperty the sub data property, to be removed from its super property.
     * @return changes required to remove a data property from being a sub-property of another data property.
     * Returned object can be ignored while working in buffering mode.
     */
    public OWLOntologyChange removeSubDataPropertyOf(OWLDataProperty superProperty, OWLDataProperty subProperty){
        try{
            long initialTime = System.nanoTime();
            OWLSubDataPropertyOfAxiom subPropAxiom = ontoRef.getOWLFactory().getOWLSubDataPropertyOfAxiom(subProperty, superProperty);
            OWLOntologyChange adding = getRemoveAxiom( subPropAxiom, manipulationBuffering);
            if( !manipulationBuffering)
                applyChanges( adding);
            logger.addDebugString( "set sub data property (" + ontoRef.getOWLObjectName( superProperty) + ") of super property (" + ontoRef.getOWLObjectName( subProperty) + ") in: " + (System.nanoTime() - initialTime) + " [ns]");
            return( adding);
        } catch( org.semanticweb.owlapi.reasoner.InconsistentOntologyException e){
            ontoRef.logInconsistency();
            return( null);
        }
    }
    /**
     * Returns the changes required to remove the fact that a property is a sub-property of another data property.
     * Unlike addition manipulations, if an entity does not exists, it will not be created automatically.
     * If either property does not exists, it is created.
     * Changes will be buffered if {@link #isChangeBuffering()} is {@code true}, else they will be applied immediately.
     * @param superPropertyName the name of the super data property, from which to remove the child.
     * @param subPropertyName the name of sub data property, to be removed from its super property.
     * @return changes required to remove a data property from being a sub-property of another data property.
     * Returned object can be ignored while working in buffering mode.
     */
    public OWLOntologyChange removeSubDataPropertyOf(String superPropertyName, String subPropertyName){
        OWLDataProperty sup = ontoRef.getOWLDataProperty( superPropertyName);
        OWLDataProperty sub = ontoRef.getOWLDataProperty( subPropertyName);
        return( removeSubDataPropertyOf( sup, sub));
    }

    /**
     * Returns the changes required to remove the fact that a property is a sub-property of another object property.
     * Unlike addition manipulations, if an entity does not exists, it will not be created automatically.
     * If either property does not exists, it is created.
     * Changes will be buffered if {@link #isChangeBuffering()} is {@code true}, else they will be applied immediately.
     * @param superProperty the super object property, from which to remove the child.
     * @param subProperty the sub object property, to be removed from its super property.
     * @return changes required to remove a object property from being a sub-property of another object property.
     * Returned object can be ignored while working in buffering mode.
     */
    public OWLOntologyChange removeSubObjectPropertyOf(OWLObjectProperty superProperty, OWLObjectProperty subProperty){
        try{
            long initialTime = System.nanoTime();
            OWLSubObjectPropertyOfAxiom subPropAxiom = ontoRef.getOWLFactory().getOWLSubObjectPropertyOfAxiom(subProperty, superProperty);
            OWLOntologyChange adding = getRemoveAxiom( subPropAxiom, manipulationBuffering);
            if( !manipulationBuffering)
                applyChanges( adding);
            logger.addDebugString( "set sub object property (" + ontoRef.getOWLObjectName( superProperty) + ") of super property (" + ontoRef.getOWLObjectName( subProperty) + ") in: " + (System.nanoTime() - initialTime) + " [ns]");
            return( adding);
        } catch( org.semanticweb.owlapi.reasoner.InconsistentOntologyException e){
            ontoRef.logInconsistency();
            return( null);
        }
    }
    /**
     * Returns the changes required to remove the fact that a property is a sub-property of another object property.
     * Unlike addition manipulations, if an entity does not exists, it will not be created automatically.
     * If either property does not exists, it is created.
     * Changes will be buffered if {@link #isChangeBuffering()} is {@code true}, else they will be applied immediately.
     * @param superPropertyName the name of the super object property, from which to remove the child.
     * @param subPropertyName the name of sub object property, to be removed from its super property.
     * @return changes required to remove an object property from being a sub-property of another object property.
     * Returned object can be ignored while working in buffering mode.
     */
    public OWLOntologyChange removeSubObjectPropertyOf(String superPropertyName, String subPropertyName){
        OWLObjectProperty sup = ontoRef.getOWLObjectProperty( superPropertyName);
        OWLObjectProperty sub = ontoRef.getOWLObjectProperty( subPropertyName);
        return( removeSubObjectPropertyOf( sup, sub));
    }


    /**
     * It returns the changes to be made in the ontology in order to remove the
     * given restriction.
     * See {@link SemanticRestriction} for information on how to add restriction
     * on class definition as well as data or object property domain and range.
     * Changes will be buffered if {@link #isChangeBuffering()} is {@code true}, else they will be applied immediately.
     * @param restriction the restriction to reomve.
     * @return th change for remove the restriction.
     */
    public OWLOntologyChange removeRestriction( SemanticRestriction restriction){
        return restriction.removeRestriction( this);
    }
    /**
     * It returns the changes to be made in the ontology in order to remove all the
     * given restrictions.
     * See {@link SemanticRestriction} for information on how to remove restriction
     * on class definition ({@link #convertSuperClassesToEquivalentClass(OWLClass)} should also be called),
     * as well as data or object property domain and range.
     * Changes will be buffered if {@link #isChangeBuffering()} is {@code true}, else they will be applied immediately.
     * @param restriction the restrictions to add.
     * @return th change for adding the restrictions.
     */
    public List< OWLOntologyChange> removeRestriction( Set<SemanticRestriction> restriction){
        List<OWLOntologyChange> out = new ArrayList<>();
        for ( SemanticRestriction r : restriction)
            out.add( addRestriction( r));
        return out;
    }


    /**
     * It returns the changes to be made in the ontology in order to
     * make a data property not functional anymore.
     * The property will be created if it does not exists.
     * Changes will be buffered if {@link #isChangeBuffering()} is {@code true},
     * else they will be applied immediately.
     * @param propertyName the name of the property to make functional.
     * @return th change for removing functional directive to a data property.
     */
    public OWLOntologyChange removeFunctionalDataProperty( String propertyName){
        return removeFunctionalDataProperty( ontoRef.getOWLDataProperty( propertyName));
    }
    /**
     * It returns the changes to be made in the ontology in order to
     * make a data property not functional anymore.
     * The property will be created if it does not exists.
     * Changes will be buffered if {@link #isChangeBuffering()} is {@code true},
     * else they will be applied immediately.
     * @param property the property to make functional.
     * @return th change for removing functional directive to a data property.
     */
    public OWLOntologyChange removeFunctionalDataProperty( OWLDataProperty property){
        try{
            long initialTime = System.nanoTime();
            OWLFunctionalDataPropertyAxiom funcAx = ontoRef.getOWLFactory().getOWLFunctionalDataPropertyAxiom(property);
            OWLOntologyChange ad = getRemoveAxiom(funcAx);
            if ( ! manipulationBuffering)
                applyChanges( ad);
            logger.addDebugString( "remove functional data property: " + ontoRef.getOWLObjectName( property) +
                    " in: " + (System.nanoTime() - initialTime) + " [ns]");
            return ad;
        } catch( org.semanticweb.owlapi.reasoner.InconsistentOntologyException e){
            ontoRef.logInconsistency();
            return( null);
        }
    }

    /**
     * It returns the changes to be made in the ontology in order to
     * make an object property not functional anymore.
     * The property will be created if it does not exists.
     * Changes will be buffered if {@link #isChangeBuffering()} is {@code true},
     * else they will be applied immediately.
     * @param propertyName the name of the property to make functional.
     * @return th change for removing functional directive to an object property.
     */
    public OWLOntologyChange removeFunctionalObjectProperty( String propertyName){
        return removeFunctionalObjectProperty( ontoRef.getOWLObjectProperty( propertyName));
    }
    /**
     * It returns the changes to be made in the ontology in order to
     * make an object property not functional anymore.
     * The property will be created if it does not exists.
     * Changes will be buffered if {@link #isChangeBuffering()} is {@code true},
     * else they will be applied immediately.
     * @param property the property to make functional.
     * @return th change for removing functional directive to an object property.
     */
    public OWLOntologyChange removeFunctionalObjectProperty( OWLObjectProperty property){
        try{
            long initialTime = System.nanoTime();
            OWLFunctionalObjectPropertyAxiom funcAx = ontoRef.getOWLFactory().getOWLFunctionalObjectPropertyAxiom(property);
            OWLOntologyChange ad = getRemoveAxiom(funcAx);
            if ( ! manipulationBuffering)
                applyChanges( ad);
            logger.addDebugString( "remove functional object property: " + ontoRef.getOWLObjectName( property) +
                    " in: " + (System.nanoTime() - initialTime) + " [ns]");
            return ad;
        } catch( org.semanticweb.owlapi.reasoner.InconsistentOntologyException e){
            ontoRef.logInconsistency();
            return( null);
        }
    }

    /**
     * It returns the changes to be made in the ontology in order to
     * make an object property not inverse functional anymore.
     * The property will be created if it does not exists.
     * Changes will be buffered if {@link #isChangeBuffering()} is {@code true},
     * else they will be applied immediately.
     * @param propertyName the name of the property to make functional.
     * @return th change for removing inverse functional directive to an object property.
     */
    public OWLOntologyChange removeInverseFunctionalObjectProperty( String propertyName){
        return removeInverseFunctionalObjectProperty( ontoRef.getOWLObjectProperty( propertyName));
    }
    /**
     * It returns the changes to be made in the ontology in order to
     * make an object property no inverse functional anymore.
     * The property will be created if it does not exists.
     * Changes will be buffered if {@link #isChangeBuffering()} is {@code true},
     * else they will be applied immediately.
     * @param property the property to make functional.
     * @return th change for remove inverse functional directive to an object property.
     */
    public OWLOntologyChange removeInverseFunctionalObjectProperty( OWLObjectProperty property){
        try{
            long initialTime = System.nanoTime();
            OWLInverseFunctionalObjectPropertyAxiom funcAx = ontoRef.getOWLFactory().getOWLInverseFunctionalObjectPropertyAxiom(property);
            OWLOntologyChange ad = getRemoveAxiom(funcAx);
            if ( ! manipulationBuffering)
                applyChanges( ad);
            logger.addDebugString( "remove inverse functional object property: " + ontoRef.getOWLObjectName( property) +
                    " in: " + (System.nanoTime() - initialTime) + " [ns]");
            return ad;
        } catch( org.semanticweb.owlapi.reasoner.InconsistentOntologyException e){
            ontoRef.logInconsistency();
            return( null);
        }
    }

    /**
     * It returns the changes to be made in the ontology in order to
     * make an object property no transitive anymore.
     * The property will be created if it does not exists.
     * Changes will be buffered if {@link #isChangeBuffering()} is {@code true},
     * else they will be applied immediately.
     * @param propertyName the name of the property to make functional.
     * @return th change for removing transitive directive to an object property.
     */
    public OWLOntologyChange removeTransitiveObjectProperty( String propertyName){
        return removeTransitiveObjectProperty( ontoRef.getOWLObjectProperty( propertyName));
    }
    /**
     * It returns the changes to be made in the ontology in order to
     * make an object property no transitive anymore.
     * The property will be created if it does not exists.
     * Changes will be buffered if {@link #isChangeBuffering()} is {@code true},
     * else they will be applied immediately.
     * @param property the property to make functional.
     * @return th change for adding transitive directive to an object property.
     */
    public OWLOntologyChange removeTransitiveObjectProperty( OWLObjectProperty property){
        try{
            long initialTime = System.nanoTime();
            OWLTransitiveObjectPropertyAxiom funcAx = ontoRef.getOWLFactory().getOWLTransitiveObjectPropertyAxiom(property);
            OWLOntologyChange ad = getRemoveAxiom(funcAx);
            if ( ! manipulationBuffering)
                applyChanges( ad);
            logger.addDebugString( "remove transitive object property: " + ontoRef.getOWLObjectName( property) +
                    " in: " + (System.nanoTime() - initialTime) + " [ns]");
            return ad;
        } catch( org.semanticweb.owlapi.reasoner.InconsistentOntologyException e){
            ontoRef.logInconsistency();
            return( null);
        }
    }

    /**
     * It returns the changes to be made in the ontology in order to
     * make an object property no symmetric anymore.
     * The property will be created if it does not exists.
     * Changes will be buffered if {@link #isChangeBuffering()} is {@code true},
     * else they will be applied immediately.
     * @param propertyName the name of the property to make functional.
     * @return th change for removing symmetric directive to an object property.
     */
    public OWLOntologyChange removeSymmetricObjectProperty( String propertyName){
        return removeSymmetricObjectProperty( ontoRef.getOWLObjectProperty( propertyName));
    }
    /**
     * It returns the changes to be made in the ontology in order to
     * make an object property symmetric.
     * The property will be created if it does not exists.
     * Changes will be buffered if {@link #isChangeBuffering()} is {@code true},
     * else they will be applied immediately.
     * @param property the property to make functional.
     * @return th change for adding symmetric directive to an object property.
     */
    public OWLOntologyChange removeSymmetricObjectProperty( OWLObjectProperty property){
        try{
            long initialTime = System.nanoTime();
            OWLSymmetricObjectPropertyAxiom funcAx = ontoRef.getOWLFactory().getOWLSymmetricObjectPropertyAxiom(property);
            OWLOntologyChange ad = getRemoveAxiom(funcAx);
            if ( ! manipulationBuffering)
                applyChanges( ad);
            logger.addDebugString( "remove symmetric object property: " + ontoRef.getOWLObjectName( property) +
                    " in: " + (System.nanoTime() - initialTime) + " [ns]");
            return ad;
        } catch( org.semanticweb.owlapi.reasoner.InconsistentOntologyException e){
            ontoRef.logInconsistency();
            return( null);
        }
    }

    /**
     * It returns the changes to be made in the ontology in order to
     * make an object property no asymmetric anymore.
     * The property will be created if it does not exists.
     * Changes will be buffered if {@link #isChangeBuffering()} is {@code true},
     * else they will be applied immediately.
     * @param propertyName the name of the property to make functional.
     * @return th change for removing asymmetric directive to an object property.
     */
    public OWLOntologyChange removeAsymmetricObjectProperty( String propertyName){
        return removeAsymmetricObjectProperty( ontoRef.getOWLObjectProperty( propertyName));
    }
    /**
     * It returns the changes to be made in the ontology in order to
     * make an object property no asymmetric anymore.
     * The property will be created if it does not exists.
     * Changes will be buffered if {@link #isChangeBuffering()} is {@code true},
     * else they will be applied immediately.
     * @param property the property to make functional.
     * @return th change for removing asymmetric directive to an object property.
     */
    public OWLOntologyChange removeAsymmetricObjectProperty( OWLObjectProperty property){
        try{
            long initialTime = System.nanoTime();
            OWLAsymmetricObjectPropertyAxiom funcAx = ontoRef.getOWLFactory().getOWLAsymmetricObjectPropertyAxiom(property);
            OWLOntologyChange ad = getRemoveAxiom(funcAx);
            if ( ! manipulationBuffering)
                applyChanges( ad);
            logger.addDebugString( "remove asymmetric object property: " + ontoRef.getOWLObjectName( property) +
                    " in: " + (System.nanoTime() - initialTime) + " [ns]");
            return ad;
        } catch( org.semanticweb.owlapi.reasoner.InconsistentOntologyException e){
            ontoRef.logInconsistency();
            return( null);
        }
    }

    /**
     * It returns the changes to be made in the ontology in order to
     * make an object property no reflexive anymore.
     * The property will be created if it does not exists.
     * Changes will be buffered if {@link #isChangeBuffering()} is {@code true},
     * else they will be applied immediately.
     * @param propertyName the name of the property to make functional.
     * @return th change for removing reflexive directive to an object property.
     */
    public OWLOntologyChange removeReflexiveObjectProperty(String propertyName){
        return removeReflexiveObjectProperty( ontoRef.getOWLObjectProperty( propertyName));
    }
    /**
     * It returns the changes to be made in the ontology in order to
     * make an object property no reflexive anymore.
     * The property will be created if it does not exists.
     * Changes will be buffered if {@link #isChangeBuffering()} is {@code true},
     * else they will be applied immediately.
     * @param property the property to make functional.
     * @return th change for removing reflexive directive to an object property.
     */
    public OWLOntologyChange removeReflexiveObjectProperty( OWLObjectProperty property){
        try{
            long initialTime = System.nanoTime();
            OWLReflexiveObjectPropertyAxiom funcAx = ontoRef.getOWLFactory().getOWLReflexiveObjectPropertyAxiom(property);
            OWLOntologyChange ad = getRemoveAxiom(funcAx);
            if ( ! manipulationBuffering)
                applyChanges( ad);
            logger.addDebugString( "remove reflexive object property: " + ontoRef.getOWLObjectName( property) +
                    " in: " + (System.nanoTime() - initialTime) + " [ns]");
            return ad;
        } catch( org.semanticweb.owlapi.reasoner.InconsistentOntologyException e){
            ontoRef.logInconsistency();
            return( null);
        }
    }

    /**
     * It returns the changes to be made in the ontology in order to
     * make an object property no irreflexive anymore.
     * The property will be created if it does not exists.
     * Changes will be buffered if {@link #isChangeBuffering()} is {@code true},
     * else they will be applied immediately.
     * @param propertyName the name of the property to make functional.
     * @return th change for remove irreflexive directive to an object property.
     */
    public OWLOntologyChange removeIrreflexiveObjectProperty(String propertyName){
        return removeIrreflexiveObjectProperty( ontoRef.getOWLObjectProperty( propertyName));
    }
    /**
     * It returns the changes to be made in the ontology in order to
     * make an object property no irreflexive anymore.
     * The property will be created if it does not exists.
     * Changes will be buffered if {@link #isChangeBuffering()} is {@code true},
     * else they will be applied immediately.
     * @param property the property to make functional.
     * @return th change for remove irreflexive directive to an object property.
     */
    public OWLOntologyChange removeIrreflexiveObjectProperty( OWLObjectProperty property){
        try{
            long initialTime = System.nanoTime();
            OWLIrreflexiveObjectPropertyAxiom funcAx = ontoRef.getOWLFactory().getOWLIrreflexiveObjectPropertyAxiom(property);
            OWLOntologyChange ad = getRemoveAxiom(funcAx);
            if ( ! manipulationBuffering)
                applyChanges( ad);
            logger.addDebugString( "remove irreflexive object property: " + ontoRef.getOWLObjectName( property) +
                    " in: " + (System.nanoTime() - initialTime) + " [ns]");
            return ad;
        } catch( org.semanticweb.owlapi.reasoner.InconsistentOntologyException e){
            ontoRef.logInconsistency();
            return( null);
        }
    }


    // ---------------------------   methods for replace entities to the ontology
    /*
     * Atomically (with respect to reasoner update) replacing of a data property.
     * Indeed, it will remove all the possible data property with a given values
     * using {@link #removeDataPropertyB2Individual(OWLNamedIndividual, OWLDataProperty, OWLLiteral)}.
     * Than, it add the new value calling {@link #addDataPropertyB2Individual(OWLNamedIndividual, OWLDataProperty, OWLLiteral)}.
     * Refer to those methods and to {@link #isChangeBuffering()} for more information.
     * @param ind individual for which a data property will be replaced.
     * @param prop property to replace
     * @param oldValue set of old values to remove
     * @param newValue new value to add
     * @return  the changes to be done into the refereed ontology to replace the value of a data property balue attached into an individual.
     * Returned object can be ignored while working in buffering mode.

    public List<OWLOntologyChange> replaceDataPropertyB2Individual( OWLNamedIndividual ind, OWLDataProperty prop, Set< OWLLiteral> oldValue, OWLLiteral newValue){
        List< OWLOntologyChange> changes = new ArrayList< OWLOntologyChange>();
        try{
            if( oldValue != null)
                for( OWLLiteral l : oldValue)
                    changes.add( this.removeDataPropertyB2Individual( ind, prop, l));
            changes.add( this.addDataPropertyB2Individual( ind, prop, newValue));
        } catch( org.semanticweb.owlapi.reasoner.InconsistentOntologyException e){
            ontoRef.logInconsistency();
            return null;
        }
        return changes;
    }*/
    /**
     * Returns the changes required to replace the value of a data property instance belonging to an individual.
     * The manipulation is atomical with respect to the reasoner (i.e., the reasoner fires only after all required
     * manipulations are performed).
     * Changes will be buffered if {@link #isChangeBuffering()} is {@code true}, else they will be applied immediately.
     * @param ind individual the data property instance to be modified belongs to.
     * @param prop type of property to replace.
     * @param oldValue value to be replaced.
     * @param newValue value to replace.
     * @return changes required to replace the value of the data property instance belonging to an individual.
     * Returned object can be ignored while working in buffering mode.
     */
    public List<OWLOntologyChange> replaceDataPropertyB2Individual( OWLNamedIndividual ind,  OWLDataProperty prop, OWLLiteral oldValue, OWLLiteral newValue){
        List< OWLOntologyChange> changes = new ArrayList< OWLOntologyChange>();
        try{
            if( oldValue != null)
                changes.add( this.removeDataPropertyB2Individual( ind, prop, oldValue));
            changes.add( this.addDataPropertyB2Individual( ind, prop, newValue));
        } catch( org.semanticweb.owlapi.reasoner.InconsistentOntologyException e){
            ontoRef.logInconsistency();
            return null;
        }
        return changes;
    }
    /**
     * Returns the changes required to replace the value of a object property instance belonging to an individual.
     * The manipulation is atomical with respect to the reasoner (i.e., the reasoner fires only after all required
     * manipulations are performed).
     * Changes will be buffered if {@link #isChangeBuffering()} is {@code true}, else they will be applied immediately.
     * @param ind individual the object property to be modified belongs to.
     * @param prop type of property to replace.
     * @param oldValue value to be replaced.
     * @param newValue value to replace.
     * @return changes required to replace the value of the object property instance belonging to an individual.
     * Returned object can be ignored while working in buffering mode.
     */
    public List<OWLOntologyChange> replaceObjectProperty( OWLNamedIndividual ind, OWLObjectProperty prop, OWLNamedIndividual oldValue, OWLNamedIndividual newValue){
        List< OWLOntologyChange> changes = new ArrayList< OWLOntologyChange>();
        try{
            if( oldValue != null)
                changes.add( this.removeObjectPropertyB2Individual( ind, prop, oldValue));
            changes.add( this.addObjectPropertyB2Individual( ind, prop, newValue));
        } catch( org.semanticweb.owlapi.reasoner.InconsistentOntologyException e){
            ontoRef.logInconsistency();
            return null;
        }
        return changes;
    }
    /**
     * Returns the changes required to replace a class one individual belongs to with a new one.
     * The manipulation is atomical with respect to the reasoner (i.e., the reasoner fires only after all required
     * manipulations are performed).
     * Changes will be buffered if {@link #isChangeBuffering()} is {@code true}, else they will be applied immediately.
     * @param ind individual whose classification must be changed.
     * @param oldValue old classification to be replaced.
     * @param newValue new classification.
     * @return changes required to change the classification of an individual.
     * Returned object can be ignored while working in buffering mode.
     */
    public List<OWLOntologyChange> replaceIndividualClass( OWLNamedIndividual ind, OWLClass oldValue, OWLClass newValue){
        List< OWLOntologyChange> changes = new ArrayList< OWLOntologyChange>();
        try{
            if( oldValue != null)
                changes.add( this.removeIndividualB2Class( ind, oldValue));
            changes.add( this.addIndividualB2Class( ind, newValue));
        } catch( org.semanticweb.owlapi.reasoner.InconsistentOntologyException e){
            ontoRef.logInconsistency();
            return null;
        }
        return changes;
    }



    // ---------------------------   methods for rename entities into the ontology
    /**
     * It Returns the changes required to rename an entity in the ontology.
     * Changes will be buffered if {@link #isChangeBuffering()} is {@code true}, else they will be applied immediately.
     * @param entity entity to rename.
     * @param newIRI new name as ontological IRI path.
     * @return changes required to rename an entity with a new IRI.
     * Returned object can be ignored while working in buffering mode.
     */
    public List<OWLOntologyChange> renameEntity( OWLEntity entity, IRI newIRI){
        long initialTime = System.nanoTime();
        String oldName = ontoRef.getOWLObjectName( entity);
        OWLEntityRenamer renamer = new OWLEntityRenamer( ontoRef.getOWLManager(), ontoRef.getOWLManager().getOntologies());
        List<OWLOntologyChange> changes = renamer.changeIRI( entity, newIRI);
        if( !manipulationBuffering)
            applyChanges( changes);
        else changeList.addAll( changes);
        logger.addDebugString( "rename entity from (" + oldName + ") to (" + ontoRef.getOWLObjectName( newIRI) + ") in: " + (System.nanoTime() - initialTime) + " [ns]");
        return( changes);
    }
    /**
     * It Returns the changes required to rename an entity in the ontology.
     * Changes will be buffered if {@link #isChangeBuffering()} is {@code true}, else they will be applied immediately.
     * @param entity name of the entity to rename.
     * @param newName new name of the entity.
     * @return changes required to rename an entity.
     * Returned object can be ignored while working in buffering mode.
     */
    public List<OWLOntologyChange> renameEntity( OWLEntity entity, String newName){
        long initialTime = System.nanoTime();
        String oldName = ontoRef.getOWLObjectName( entity);
        OWLEntityRenamer renamer = new OWLEntityRenamer( ontoRef.getOWLManager(), ontoRef.getOWLManager().getOntologies());
        IRI newIRI = IRI.create( ontoRef.getIriOntologyPath() + "#" + newName);
        List<OWLOntologyChange> changes = renamer.changeIRI( entity, newIRI);
        if( !manipulationBuffering)
            applyChanges( changes);
        else changeList.addAll( changes);
        logger.addDebugString( "rename entity from (" + oldName + ") to (" + newName + ") in: " + (System.nanoTime() - initialTime) + " [ns]");
        return( changes);
    }


    // ---------------------------   methods to set individuals classes and properties disjoint
    /**
     * Returns the changes required to set some individuals disjoint among themselves.
     * Changes will be buffered if {@link #isChangeBuffering()} is {@code true}, else they will be applied immediately.
     * @param individualNames names of the individuals to set disjoint.
     * @return changes required to set some individual disjoint.
     * Returned object can be ignored while working in buffering mode.
     */
    public OWLOntologyChange makeDisjointIndividualName(Set< String> individualNames){
        Set< OWLNamedIndividual> inds = new HashSet< OWLNamedIndividual>();
        for( String i : individualNames)
            inds.add( ontoRef.getOWLIndividual( i));
        return makeDisjointIndividuals( inds);
    }
    /**
     * Returns the changes required to set some individuals disjoint among themselves.
     * Changes will be buffered if {@link #isChangeBuffering()} is {@code true}, else they will be applied immediately.
     * @param individuals set of individuals to set as disjoint.
     * @return changes required to set some individual disjoint.
     * Returned object can be ignored while working in buffering mode.
     */
    public OWLOntologyChange makeDisjointIndividuals(Set< OWLNamedIndividual> individuals){
        try{
            long initialTime = System.nanoTime();
            OWLDifferentIndividualsAxiom differentIndAxiom = ontoRef.getOWLFactory().getOWLDifferentIndividualsAxiom( individuals);
            OWLOntologyChange adding = getAddAxiom( differentIndAxiom, manipulationBuffering);

            if( !manipulationBuffering)
                applyChanges( adding);
            logger.addDebugString( "make disjoint individuals: " + ontoRef.getOWLObjectName(individuals) + ". in:" + (System.nanoTime() - initialTime) + " [ns]");
            return( adding);
        } catch( org.semanticweb.owlapi.reasoner.InconsistentOntologyException e){
            ontoRef.logInconsistency();
        }
        return( null);
    }

    /**
     * Returns the changes required to unset disjoint axiom among some individuals.
     * Changes will be buffered if {@link #isChangeBuffering()} is {@code true}, else they will be applied immediately.
     * @param individualNames names of the individuals to unset disjoint axiom among.
     * @return changes required to unset some individual disjoint.
     * Returned object can be ignored while working in buffering mode.
     */
    public OWLOntologyChange removeDisjointIndividualName( Set< String> individualNames){
        Set< OWLNamedIndividual> inds = new HashSet< OWLNamedIndividual>();
        for( String i : individualNames)
            inds.add( ontoRef.getOWLIndividual( i));
        return removeDisjointIndividuals( inds);
    }
    /**
     * Returns the changes required to unset disjoint axiom among some individuals.
     * Changes will be buffered if {@link #isChangeBuffering()} is {@code true}, else they will be applied immediately.
     * @param individuals set of individuals to unset disjoint axiom among.
     * @return changes required to unset some individual disjoint.
     * Returned object can be ignored while working in buffering mode.
     */
    public OWLOntologyChange removeDisjointIndividuals( Set< OWLNamedIndividual> individuals){
        try{
            long initialTime = System.nanoTime();
            OWLDifferentIndividualsAxiom differentIndAxiom = ontoRef.getOWLFactory().getOWLDifferentIndividualsAxiom( individuals);
            OWLOntologyChange adding = getRemoveAxiom( differentIndAxiom, manipulationBuffering);

            if( !manipulationBuffering)
                applyChanges( adding);
            logger.addDebugString( "make disjoint individuals: " + ontoRef.getOWLObjectName(individuals) + ". in:" + (System.nanoTime() - initialTime) + " [ns]");
            return( adding);
        } catch( org.semanticweb.owlapi.reasoner.InconsistentOntologyException e){
            ontoRef.logInconsistency();
        }
        return( null);
    }

    /**
     * Returns the changes required to set some classes disjoint among themselves.
     * Changes will be buffered if {@link #isChangeBuffering()} is {@code true}, else they will be applied immediately.
     * @param classesName names of the classes to set as disjoint.
     * @return changes required to set some classes disjoint.
     * Returned object can be ignored while working in buffering mode.
     */
    public OWLOntologyChange makeDisjointClassName( Set< String> classesName){
        Set< OWLClass> inds = new HashSet< OWLClass>();
        for( String i : classesName)
            inds.add( ontoRef.getOWLClass( i));
        return makeDisjointClasses( inds);
    }
    /**
     * Returns the changes required to set some classes disjoint among themselves.
     * Changes will be buffered if {@link #isChangeBuffering()} is {@code true}, else they will be applied immediately.
     * @param classes set of classes to set as disjoint.
     * @return changes required to set some classes disjoint.
     * Returned object can be ignored while working in buffering mode.
     */
    public OWLOntologyChange makeDisjointClasses( Set< OWLClass> classes){
        try{
            long initialTime = System.nanoTime();
            OWLDisjointClassesAxiom differentIndAxiom = ontoRef.getOWLFactory().getOWLDisjointClassesAxiom( classes);
            OWLOntologyChange adding = getAddAxiom( differentIndAxiom, manipulationBuffering);

            if( !manipulationBuffering)
                applyChanges( adding);
            logger.addDebugString( "remove disjoint class: " + ontoRef.getOWLObjectName(classes) + ". in:" + (System.nanoTime() - initialTime) + " [ns]");
            return( adding);
        } catch( org.semanticweb.owlapi.reasoner.InconsistentOntologyException e){
            ontoRef.logInconsistency();
        }
        return( null);
    }

    /**
     * Returns the changes required to unset disjoint axiom among some classes.
     * Changes will be buffered if {@link #isChangeBuffering()} is {@code true}, else they will be applied immediately.
     * @param classesName names of the classes to unset disjoint axiom among.
     * @return changes required to unset disjoint axiom among some classes.
     * Returned object can be ignored while working in buffering mode.
     */
    public OWLOntologyChange removeDisjointClassName( Set< String> classesName){
        Set< OWLClass> inds = new HashSet< OWLClass>();
        for( String i : classesName)
            inds.add( ontoRef.getOWLClass( i));
        return removeDisjointClasses( inds);
    }
    /**
     * Returns the changes required to unset disjoint axiom among some classes.
     * Changes will be buffered if {@link #isChangeBuffering()} is {@code true}, else they will be applied immediately.
     * @param classes set of classes to unset disjoint axiom among.
     * @return changes required to unset disjoint axiom among some classes.
     * Returned object can be ignored while working in buffering mode.
     */
    public OWLOntologyChange removeDisjointClasses( Set< OWLClass> classes){
        try{
            long initialTime = System.nanoTime();
            OWLDisjointClassesAxiom differentIndAxiom = ontoRef.getOWLFactory().getOWLDisjointClassesAxiom( classes);
            OWLOntologyChange adding = getRemoveAxiom( differentIndAxiom, manipulationBuffering);

            if( !manipulationBuffering)
                applyChanges( adding);
            logger.addDebugString( "remove disjoint class: " + ontoRef.getOWLObjectName(classes) + ". in:" + (System.nanoTime() - initialTime) + " [ns]");
            return( adding);
        } catch( org.semanticweb.owlapi.reasoner.InconsistentOntologyException e){
            ontoRef.logInconsistency();
        }
        return null;
    }

    /**
     * Returns the changes required to set some data properties disjoint among themselves.
     * Changes will be buffered if {@link #isChangeBuffering()} is {@code true}, else they will be applied immediately.
     * @param propertyNames names of the data properties to set disjoint.
     * @return changes required to set some data properties disjoint.
     * Returned object can be ignored while working in buffering mode.
     */
    public OWLOntologyChange makeDisjointDataPropertiesName(Set< String> propertyNames){
        Set< OWLDataProperty> prop = new HashSet<>();
        for( String i : propertyNames)
            prop.add( ontoRef.getOWLDataProperty( i));
        return makeDisjointDataProperties( prop);
    }
    /**
     * Returns the changes required to set some data property disjoint among themselves.
     * Changes will be buffered if {@link #isChangeBuffering()} is {@code true}, else they will be applied immediately.
     * @param properties set of data properties to set as disjoint.
     * @return changes required to set some data properties disjoint.
     * Returned object can be ignored while working in buffering mode.
     */
    public OWLOntologyChange makeDisjointDataProperties(Set< OWLDataProperty> properties){
        try{
            long initialTime = System.nanoTime();
            OWLDisjointDataPropertiesAxiom differentIndAxiom = ontoRef.getOWLFactory().getOWLDisjointDataPropertiesAxiom(properties);
            OWLOntologyChange adding = getAddAxiom( differentIndAxiom, manipulationBuffering);

            if( !manipulationBuffering)
                applyChanges( adding);
            logger.addDebugString( "make disjoint data property: " + ontoRef.getOWLObjectName(properties) + ". in:" + (System.nanoTime() - initialTime) + " [ns]");
            return( adding);
        } catch( org.semanticweb.owlapi.reasoner.InconsistentOntologyException e){
            ontoRef.logInconsistency();
        }
        return( null);
    }

    /**
     * Returns the changes required to unset disjoint axiom among some data properties.
     * Changes will be buffered if {@link #isChangeBuffering()} is {@code true}, else they will be applied immediately.
     * @param propertyName names of the data properties to unset disjoint axiom among.
     * @return changes required to unset some data properties disjoint.
     * Returned object can be ignored while working in buffering mode.
     */
    public OWLOntologyChange removeDisjointDataPropertyName( Set< String> propertyName){
        Set< OWLDataProperty> prop = new HashSet<>();
        for( String i : propertyName)
            prop.add( ontoRef.getOWLDataProperty( i));
        return removeDisjointDataProperty( prop);
    }
    /**
     * Returns the changes required to unset disjoint axiom among some data properties.
     * Changes will be buffered if {@link #isChangeBuffering()} is {@code true}, else they will be applied immediately.
     * @param properties set of data properties to unset disjoint axiom among.
     * @return changes required to unset some data properties disjoint.
     * Returned object can be ignored while working in buffering mode.
     */
    public OWLOntologyChange removeDisjointDataProperty( Set< OWLDataProperty> properties){
        try{
            long initialTime = System.nanoTime();
            OWLDisjointDataPropertiesAxiom differentIndAxiom = ontoRef.getOWLFactory().getOWLDisjointDataPropertiesAxiom(properties);
            OWLOntologyChange adding = getRemoveAxiom( differentIndAxiom, manipulationBuffering);

            if( !manipulationBuffering)
                applyChanges( adding);
            logger.addDebugString( "make disjoint data property: " + ontoRef.getOWLObjectName(properties) + ". in:" + (System.nanoTime() - initialTime) + " [ns]");
            return( adding);
        } catch( org.semanticweb.owlapi.reasoner.InconsistentOntologyException e){
            ontoRef.logInconsistency();
        }
        return( null);
    }

    /**
     * Returns the changes required to set some object properties disjoint among themselves.
     * Changes will be buffered if {@link #isChangeBuffering()} is {@code true}, else they will be applied immediately.
     * @param propertyNames names of the object properties to set disjoint.
     * @return changes required to set some object properties disjoint.
     * Returned object can be ignored while working in buffering mode.
     */
    public OWLOntologyChange makeDisjointObjectPropertyNames(Set< String> propertyNames){
        Set< OWLObjectProperty> prop = new HashSet<>();
        for( String i : propertyNames)
            prop.add( ontoRef.getOWLObjectProperty( i));
        return makeDisjointObjectProperties( prop);
    }
    /**
     * Returns the changes required to set some object properties disjoint among themselves.
     * Changes will be buffered if {@link #isChangeBuffering()} is {@code true}, else they will be applied immediately.
     * @param properties set of object properties to set as disjoint.
     * @return changes required to set some object properties disjoint.
     * Returned object can be ignored while working in buffering mode.
     */
    public OWLOntologyChange makeDisjointObjectProperties(Set< OWLObjectProperty> properties){
        try{
            long initialTime = System.nanoTime();
            OWLDisjointObjectPropertiesAxiom differentIndAxiom = ontoRef.getOWLFactory().getOWLDisjointObjectPropertiesAxiom(properties);
            OWLOntologyChange adding = getAddAxiom( differentIndAxiom, manipulationBuffering);

            if( !manipulationBuffering)
                applyChanges( adding);
            logger.addDebugString( "make disjoint Object property: " + ontoRef.getOWLObjectName(properties) + ". in:" + (System.nanoTime() - initialTime) + " [ns]");
            return( adding);
        } catch( org.semanticweb.owlapi.reasoner.InconsistentOntologyException e){
            ontoRef.logInconsistency();
        }
        return( null);
    }

    /**
     * Returns the changes required to unset disjoint axiom among some object properties.
     * Changes will be buffered if {@link #isChangeBuffering()} is {@code true}, else they will be applied immediately.
     * @param propertyName names of the object properties to unset disjoint axiom among.
     * @return changes required to unset some object properties disjoint.
     * Returned object can be ignored while working in buffering mode.
     */
    public OWLOntologyChange removeDisjointObjectPropertyNames(Set< String> propertyName){
        Set< OWLObjectProperty> prop = new HashSet<>();
        for( String i : propertyName)
            prop.add( ontoRef.getOWLObjectProperty( i));
        return removeDisjointObjectProperties( prop);
    }
    /**
     * Returns the changes required to unset disjoint axiom among some object properties.
     * Changes will be buffered if {@link #isChangeBuffering()} is {@code true}, else they will be applied immediately.
     * @param properties set of object properties to unset disjoint axiom among.
     * @return changes required to unset some object property disjoint.
     * Returned object can be ignored while working in buffering mode.
     */
    public OWLOntologyChange removeDisjointObjectProperties(Set< OWLObjectProperty> properties){
        try{
            long initialTime = System.nanoTime();
            OWLDisjointObjectPropertiesAxiom differentIndAxiom = ontoRef.getOWLFactory().getOWLDisjointObjectPropertiesAxiom(properties);
            OWLOntologyChange adding = getRemoveAxiom( differentIndAxiom, manipulationBuffering);

            if( !manipulationBuffering)
                applyChanges( adding);
            logger.addDebugString( "make disjoint Object properties: " + ontoRef.getOWLObjectName(properties) + ". in:" + (System.nanoTime() - initialTime) + " [ns]");
            return( adding);
        } catch( org.semanticweb.owlapi.reasoner.InconsistentOntologyException e){
            ontoRef.logInconsistency();
        }
        return( null);
    }



    // ---------------------------   methods to set individuals, classes and properties equivalent
    /**
     * Returns the changes required to set some individuals equivalent among themselves.
     * Changes will be buffered if {@link #isChangeBuffering()} is {@code true}, else they will be applied immediately.
     * @param individualNames names of the individuals to set equivalent.
     * @return changes required to set some individual equivalent.
     * Returned object can be ignored while working in buffering mode.
     */
    public OWLOntologyChange makeEquivalentIndividualName(Set< String> individualNames){
        Set< OWLNamedIndividual> inds = new HashSet< OWLNamedIndividual>();
        for( String i : individualNames)
            inds.add( ontoRef.getOWLIndividual( i));
        return makeEquivalentIndividuals( inds);
    }
    /**
     * Returns the changes required to set some individuals equivalent among themselves.
     * Changes will be buffered if {@link #isChangeBuffering()} is {@code true}, else they will be applied immediately.
     * @param individuals set of individuals to set as equivalent.
     * @return changes required to set some individual equivalent.
     * Returned object can be ignored while working in buffering mode.
     */
    public OWLOntologyChange makeEquivalentIndividuals(Set< OWLNamedIndividual> individuals){
        try{
            long initialTime = System.nanoTime();
            OWLSameIndividualAxiom differentIndAxiom = ontoRef.getOWLFactory().getOWLSameIndividualAxiom(individuals);
            OWLOntologyChange adding = getAddAxiom( differentIndAxiom, manipulationBuffering);

            if( !manipulationBuffering)
                applyChanges( adding);
            logger.addDebugString( "make equivalent individuals: " + ontoRef.getOWLObjectName(individuals) + ". in:" + (System.nanoTime() - initialTime) + " [ns]");
            return( adding);
        } catch( org.semanticweb.owlapi.reasoner.InconsistentOntologyException e){
            ontoRef.logInconsistency();
        }
        return( null);
    }

    /**
     * Returns the changes required to unset equivalent axiom among some individuals.
     * Changes will be buffered if {@link #isChangeBuffering()} is {@code true}, else they will be applied immediately.
     * @param individualNames names of the individuals to unset equivalent axiom among.
     * @return changes required to unset some individual equivalent.
     * Returned object can be ignored while working in buffering mode.
     */
    public OWLOntologyChange removeEquivalentIndividualName(Set< String> individualNames){
        Set< OWLNamedIndividual> inds = new HashSet< OWLNamedIndividual>();
        for( String i : individualNames)
            inds.add( ontoRef.getOWLIndividual( i));
        return removeEquivalentIndividuals( inds);
    }
    /**
     * Returns the changes required to unset equivalent axiom among some individuals.
     * Changes will be buffered if {@link #isChangeBuffering()} is {@code true}, else they will be applied immediately.
     * @param individuals set of individuals to unset equivalent axiom among.
     * @return changes required to unset some individual equivalent.
     * Returned object can be ignored while working in buffering mode.
     */
    public OWLOntologyChange removeEquivalentIndividuals(Set< OWLNamedIndividual> individuals){
        try{
            long initialTime = System.nanoTime();
            OWLSameIndividualAxiom differentIndAxiom = ontoRef.getOWLFactory().getOWLSameIndividualAxiom(individuals);
            OWLOntologyChange adding = getRemoveAxiom( differentIndAxiom, manipulationBuffering);

            if( !manipulationBuffering)
                applyChanges( adding);
            logger.addDebugString( "make equivalent individuals: " + ontoRef.getOWLObjectName(individuals) + ". in:" + (System.nanoTime() - initialTime) + " [ns]");
            return( adding);
        } catch( org.semanticweb.owlapi.reasoner.InconsistentOntologyException e){
            ontoRef.logInconsistency();
        }
        return( null);
    }

    /**
     * Returns the changes required to set some classes equivalent among themselves.
     * Changes will be buffered if {@link #isChangeBuffering()} is {@code true}, else they will be applied immediately.
     * @param classesName names of the classes to set as equivalent.
     * @return changes required to set some classes equivalent.
     * Returned object can be ignored while working in buffering mode.
     */
    public OWLOntologyChange makeEquivalentClassName( Set< String> classesName){
        Set< OWLClass> inds = new HashSet< OWLClass>();
        for( String i : classesName)
            inds.add( ontoRef.getOWLClass( i));
        return makeEquivalentClasses( inds);
    }
    /**
     * Returns the changes required to set some classes equivalent among themselves.
     * Changes will be buffered if {@link #isChangeBuffering()} is {@code true}, else they will be applied immediately.
     * @param classes set of classes to set as equivalent.
     * @return changes required to set some classes equivalent.
     * Returned object can be ignored while working in buffering mode.
     */
    public OWLOntologyChange makeEquivalentClasses( Set< OWLClass> classes){
        try{
            long initialTime = System.nanoTime();
            OWLEquivalentClassesAxiom differentIndAxiom = ontoRef.getOWLFactory().getOWLEquivalentClassesAxiom(classes);
            OWLOntologyChange adding = getAddAxiom( differentIndAxiom, manipulationBuffering);

            if( !manipulationBuffering)
                applyChanges( adding);
            logger.addDebugString( "remove equivalent class: " + ontoRef.getOWLObjectName(classes) + ". in:" + (System.nanoTime() - initialTime) + " [ns]");
            return( adding);
        } catch( org.semanticweb.owlapi.reasoner.InconsistentOntologyException e){
            ontoRef.logInconsistency();
        }
        return( null);
    }

    /**
     * Returns the changes required to unset equivalent axiom among some classes.
     * Changes will be buffered if {@link #isChangeBuffering()} is {@code true}, else they will be applied immediately.
     * @param classesName names of the classes to unset equivalent axiom among.
     * @return changes required to unset equivalent axiom among some classes.
     * Returned object can be ignored while working in buffering mode.
     */
    public OWLOntologyChange removeEquivalentClassName( Set< String> classesName){
        Set< OWLClass> inds = new HashSet< OWLClass>();
        for( String i : classesName)
            inds.add( ontoRef.getOWLClass( i));
        return removeEquivalentClasses( inds);
    }
    /**
     * Returns the changes required to unset equivalent axiom among some classes.
     * Changes will be buffered if {@link #isChangeBuffering()} is {@code true}, else they will be applied immediately.
     * @param classes set of classes to unset equivalent axiom among.
     * @return changes required to unset equivalent axiom among some classes.
     * Returned object can be ignored while working in buffering mode.
     */
    public OWLOntologyChange removeEquivalentClasses( Set< OWLClass> classes){
        try{
            long initialTime = System.nanoTime();
            OWLEquivalentClassesAxiom differentIndAxiom = ontoRef.getOWLFactory().getOWLEquivalentClassesAxiom(classes);
            OWLOntologyChange adding = getRemoveAxiom( differentIndAxiom, manipulationBuffering);

            if( !manipulationBuffering)
                applyChanges( adding);
            logger.addDebugString( "remove equivalent class: " + ontoRef.getOWLObjectName(classes) + ". in:" + (System.nanoTime() - initialTime) + " [ns]");
            return( adding);
        } catch( org.semanticweb.owlapi.reasoner.InconsistentOntologyException e){
            ontoRef.logInconsistency();
        }
        return null;
    }

    /**
     * Returns the changes required to set some data properties equivalent among themselves.
     * Changes will be buffered if {@link #isChangeBuffering()} is {@code true}, else they will be applied immediately.
     * @param propertyNames names of the data properties to set equivalent.
     * @return changes required to set some data properties equivalent.
     * Returned object can be ignored while working in buffering mode.
     */
    public OWLOntologyChange makeEquivalentDataPropertiesName(Set< String> propertyNames){
        Set< OWLDataProperty> prop = new HashSet<>();
        for( String i : propertyNames)
            prop.add( ontoRef.getOWLDataProperty( i));
        return makeEquivalentDataProperties( prop);
    }
    /**
     * Returns the changes required to set some data property equivalent among themselves.
     * Changes will be buffered if {@link #isChangeBuffering()} is {@code true}, else they will be applied immediately.
     * @param properties set of data properties to set as equivalent.
     * @return changes required to set some data properties equivalent.
     * Returned object can be ignored while working in buffering mode.
     */
    public OWLOntologyChange makeEquivalentDataProperties(Set< OWLDataProperty> properties){
        try{
            long initialTime = System.nanoTime();
            OWLEquivalentDataPropertiesAxiom differentIndAxiom = ontoRef.getOWLFactory().getOWLEquivalentDataPropertiesAxiom(properties);
            OWLOntologyChange adding = getAddAxiom( differentIndAxiom, manipulationBuffering);

            if( !manipulationBuffering)
                applyChanges( adding);
            logger.addDebugString( "make equivalent data property: " + ontoRef.getOWLObjectName(properties) + ". in:" + (System.nanoTime() - initialTime) + " [ns]");
            return( adding);
        } catch( org.semanticweb.owlapi.reasoner.InconsistentOntologyException e){
            ontoRef.logInconsistency();
        }
        return( null);
    }

    /**
     * Returns the changes required to unset equivalent axiom among some data properties.
     * Changes will be buffered if {@link #isChangeBuffering()} is {@code true}, else they will be applied immediately.
     * @param propertyName names of the data properties to unset equivalent axiom among.
     * @return changes required to unset some data properties equivalent.
     * Returned object can be ignored while working in buffering mode.
     */
    public OWLOntologyChange removeEquivalentDataPropertyName( Set< String> propertyName){
        Set< OWLDataProperty> prop = new HashSet<>();
        for( String i : propertyName)
            prop.add( ontoRef.getOWLDataProperty( i));
        return removeEquivalentDataProperty( prop);
    }
    /**
     * Returns the changes required to unset equivalent axiom among some data properties.
     * Changes will be buffered if {@link #isChangeBuffering()} is {@code true}, else they will be applied immediately.
     * @param properties set of data properties to unset equivalent axiom among.
     * @return changes required to unset some data properties equivalent.
     * Returned object can be ignored while working in buffering mode.
     */
    public OWLOntologyChange removeEquivalentDataProperty( Set< OWLDataProperty> properties){
        try{
            long initialTime = System.nanoTime();
            OWLEquivalentDataPropertiesAxiom differentIndAxiom = ontoRef.getOWLFactory().getOWLEquivalentDataPropertiesAxiom(properties);
            OWLOntologyChange adding = getRemoveAxiom( differentIndAxiom, manipulationBuffering);

            if( !manipulationBuffering)
                applyChanges( adding);
            logger.addDebugString( "make equivalent data property: " + ontoRef.getOWLObjectName(properties) + ". in:" + (System.nanoTime() - initialTime) + " [ns]");
            return( adding);
        } catch( org.semanticweb.owlapi.reasoner.InconsistentOntologyException e){
            ontoRef.logInconsistency();
        }
        return( null);
    }

    /**
     * Returns the changes required to set some object properties equivalent among themselves.
     * Changes will be buffered if {@link #isChangeBuffering()} is {@code true}, else they will be applied immediately.
     * @param propertyNames names of the object properties to set equivalent.
     * @return changes required to set some object properties equivalent.
     * Returned object can be ignored while working in buffering mode.
     */
    public OWLOntologyChange makeEquivalentObjectPropertyNames(Set< String> propertyNames){
        Set< OWLObjectProperty> prop = new HashSet<>();
        for( String i : propertyNames)
            prop.add( ontoRef.getOWLObjectProperty( i));
        return makeEquivalentObjectProperties( prop);
    }
    /**
     * Returns the changes required to set some object properties equivalent among themselves.
     * Changes will be buffered if {@link #isChangeBuffering()} is {@code true}, else they will be applied immediately.
     * @param properties set of object properties to set as equivalent.
     * @return changes required to set some object properties equivalent.
     * Returned object can be ignored while working in buffering mode.
     */
    public OWLOntologyChange makeEquivalentObjectProperties(Set< OWLObjectProperty> properties){
        try{
            long initialTime = System.nanoTime();
            OWLEquivalentObjectPropertiesAxiom differentIndAxiom = ontoRef.getOWLFactory().getOWLEquivalentObjectPropertiesAxiom(properties);
            OWLOntologyChange adding = getAddAxiom( differentIndAxiom, manipulationBuffering);

            if( !manipulationBuffering)
                applyChanges( adding);
            logger.addDebugString( "make equivalent Object property: " + ontoRef.getOWLObjectName(properties) + ". in:" + (System.nanoTime() - initialTime) + " [ns]");
            return( adding);
        } catch( org.semanticweb.owlapi.reasoner.InconsistentOntologyException e){
            ontoRef.logInconsistency();
        }
        return( null);
    }

    /**
     * Returns the changes required to unset equivalent axiom among some object properties.
     * Changes will be buffered if {@link #isChangeBuffering()} is {@code true}, else they will be applied immediately.
     * @param propertyName names of the object properties to unset equivalent axiom among.
     * @return changes required to unset some object properties equivalent.
     * Returned object can be ignored while working in buffering mode.
     */
    public OWLOntologyChange removeEquivalentObjectPropertyNames(Set< String> propertyName){
        Set< OWLObjectProperty> prop = new HashSet<>();
        for( String i : propertyName)
            prop.add( ontoRef.getOWLObjectProperty( i));
        return removeEquivalentObjectProperties( prop);
    }
    /**
     * Returns the changes required to unset equivalent axiom among some object properties.
     * Changes will be buffered if {@link #isChangeBuffering()} is {@code true}, else they will be applied immediately.
     * @param properties set of object properties to unset equivalent axiom among.
     * @return changes required to unset some object property equivalent.
     * Returned object can be ignored while working in buffering mode.
     */
    public OWLOntologyChange removeEquivalentObjectProperties(Set< OWLObjectProperty> properties){
        try{
            long initialTime = System.nanoTime();
            OWLEquivalentObjectPropertiesAxiom differentIndAxiom = ontoRef.getOWLFactory().getOWLEquivalentObjectPropertiesAxiom(properties);
            OWLOntologyChange adding = getRemoveAxiom( differentIndAxiom, manipulationBuffering);

            if( !manipulationBuffering)
                applyChanges( adding);
            logger.addDebugString( "make equivalent Object properties: " + ontoRef.getOWLObjectName(properties) + ". in:" + (System.nanoTime() - initialTime) + " [ns]");
            return( adding);
        } catch( org.semanticweb.owlapi.reasoner.InconsistentOntologyException e){
            ontoRef.logInconsistency();
        }
        return( null);
    }


}