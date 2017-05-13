package it.emarolab.amor.owlInterface;

import it.emarolab.amor.owlDebugger.Logger;
import it.emarolab.amor.owlDebugger.Logger.LoggerFlag;
import org.semanticweb.owlapi.change.ConvertSuperClassesToEquivalentClass;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.util.OWLEntityRemover;
import org.semanticweb.owlapi.util.OWLEntityRenamer;
import org.semanticweb.owlapi.vocab.OWL2Datatype;

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
     * Returns the changes to make a class be a sub class of an object property in existence with a class value.
     * In symbols: {@code C &sub; p(&exist; V)}, where: {@code C} is the class, {@code p} the object property
     * and {@code V}, the class value.
     * Changes will be buffered if {@link #isChangeBuffering()} is {@code true}, else they will be applied immediately.
     * @param cl the class object of the sub-setting relation ({@code C}).
     * @param property the property of the sub-setting relation ({@code p}).
     * @param value the value of the sub-setting relation ({@code V}).
     * @return the changes to be applied in order to make a class being a sub-set of an existential property.
     */
    public OWLOntologyChange addSomeObjectClassExpression(OWLClass cl, OWLObjectProperty property, OWLClass value){
        return addObjectClassExpression( cl, property, 0, value, RESTRICTION_SOME);
    }
    /**
     * Returns the changes to make a class be a sub class of an object property in existence with a class value.
     * In symbols: {@code C &sub; p(&exist; V)}, where: {@code C} is the class, {@code p} the object property
     * and {@code V}, the class value.
     * Changes will be buffered if {@link #isChangeBuffering()} is {@code true}, else they will be applied immediately.
     * @param className the name of the class object of the sub-setting relation ({@code C}).
     * @param propertyName the name of the property of the sub-setting relation ({@code p}).
     * @param valueName the name the value of the sub-setting relation ({@code V}).
     * @return the changes to be applied in order to make a class being a sub-set of an existential property.
     */
    public OWLOntologyChange addSomeObjectClassExpression(String className, String propertyName, String valueName){
        OWLClass cl = ontoRef.getOWLClass( className);
        OWLObjectProperty property = ontoRef.getOWLObjectProperty( propertyName);
        OWLClass value = ontoRef.getOWLClass( valueName);
        return addSomeObjectClassExpression( cl, property, value);
    }
    /**
     * Returns the changes to make a class be a sub class of an object property universally identifying a class value.
     * In symbols: {@code C &sub; p(&forall; V)}, where: {@code C} is the class, {@code p} the object property
     * and {@code V}, the class value.
     * Changes will be buffered if {@link #isChangeBuffering()} is {@code true}, else they will be applied immediately.
     * @param cl the class object of the sub-setting relation ({@code C}).
     * @param property the property of the sub-setting relation ({@code p}).
     * @param value the value of the sub-setting relation ({@code V}).
     * @return the changes to be applied in order to make a class being a sub-set of an universal property.
     */
    public OWLOntologyChange addOnlyObjectClassExpression(OWLClass cl, OWLObjectProperty property, OWLClass value){
        return addObjectClassExpression( cl, property, 0, value, RESTRICTION_ONLY);
    }
    /**
     * Returns the changes to make a class be a sub class of an object property universally identifying a class value.
     * In symbols: {@code C &sub; p(&forall; V)}, where: {@code C} is the class, {@code p} the object property
     * and {@code V}, the class value.
     * Changes will be buffered if {@link #isChangeBuffering()} is {@code true}, else they will be applied immediately.
     * @param className the name of the class object of the sub-setting relation ({@code C}).
     * @param propertyName the name of the property of the sub-setting relation ({@code p}).
     * @param valueName the name the value of the sub-setting relation ({@code V}).
     * @return the changes to be applied in order to make a class being a sub-set of an universal property.
     */
    public OWLOntologyChange addOnlyObjectClassExpression(String className, String propertyName, String valueName){
        OWLClass cl = ontoRef.getOWLClass( className);
        OWLObjectProperty property = ontoRef.getOWLObjectProperty( propertyName);
        OWLClass value = ontoRef.getOWLClass( valueName);
        return addOnlyObjectClassExpression( cl, property, value);
    }
    /**
     * Returns the changes to make a class be a sub class of an object property expression
     * minimally identified by a given cardinality class restriction.
     * In symbols: {@code C &sub; p(&lt;<sub>d</sub> V)}, where: {@code C} is the class, {@code p} the object property,
     * {@code V} is the class value and {@code d}, the cardinality.
     * Changes will be buffered if {@link #isChangeBuffering()} is {@code true}, else they will be applied immediately.
     * @param cl the class object of the sub-setting relation ({@code C}).
     * @param property the property of the sub-setting relation ({@code p}).
     * @param cardinality the cardinality of the minimal relation ({@code d}).
     * @param value the value of the sub-setting relation ({@code V}).
     * @return the changes to be applied in order to make a class being a sub-set of a minimum number of properties
     * restricted to a class.
     */
    public OWLOntologyChange addMinObjectClassExpression(OWLClass cl, OWLObjectProperty property, int cardinality, OWLClass value){
		return addObjectClassExpression( cl, property, cardinality, value, RESTRICTION_MIN);
	}
    /**
     * Returns the changes to make a class be a sub class of an object property  expression
     * minimally identified by a given cardinality class restriction.
     * In symbols: {@code C &sub; p(&lt;<sub>d</sub> V)}, where: {@code C} is the class, {@code p} the object property,
     * {@code V} is the class value and {@code d}, the cardinality.
     * Changes will be buffered if {@link #isChangeBuffering()} is {@code true}, else they will be applied immediately.
     * @param className the name of the class object of the sub-setting relation ({@code C}).
     * @param propertyName the name of the property of the sub-setting relation ({@code p}).
     * @param cardinality the cardinality of the minimal relation ({@code d}).
     * @param valueName the name of the value of the sub-setting relation ({@code V}).
     * @return the changes to be applied in order to make a class being a sub-set of a minimum number of properties
     * restricted to a class.
     */
	public OWLOntologyChange addMinObjectClassExpression(String className, String propertyName, int cardinality, String valueName){
		OWLClass cl = ontoRef.getOWLClass( className);
		OWLObjectProperty property = ontoRef.getOWLObjectProperty( propertyName);
		OWLClass value = ontoRef.getOWLClass( valueName);
		return addMinObjectClassExpression( cl, property, cardinality, value);
	}
    /**
     * Returns the changes to make a class be a sub class of an object property  expression
     * maximally identified by a given cardinality class restriction.
     * In symbols: {@code C &sub; p(&gt;<sub>d</sub> V)}, where: {@code C} is the class, {@code p} the object property,
     * {@code V} is the class value and {@code d}, the cardinality.
     * Changes will be buffered if {@link #isChangeBuffering()} is {@code true}, else they will be applied immediately.
     * @param cl the class object of the sub-setting relation ({@code C}).
     * @param property the property of the sub-setting relation ({@code p}).
     * @param cardinality the cardinality of the minimal relation ({@code d}).
     * @param value the value of the sub-setting relation ({@code V}).
     * @return the changes to be applied in order to make a class being a sub-set of a maximum number of properties
     * restricted to a class.
     */
	public OWLOntologyChange addMaxObjectClassExpression(OWLClass cl, OWLObjectProperty property, int cardinality, OWLClass value){
        return addObjectClassExpression( cl, property, cardinality, value, RESTRICTION_MAX);
    }
    /**
     * Returns the changes to make a class be a sub class of an object property expression
     * maximally identified by a given cardinality class restriction.
     * In symbols: {@code C &sub; p(&gt;<sub>d</sub> V)}, where: {@code C} is the class, {@code p} the object property,
     * {@code V} is the class value and {@code d}, the cardinality.
     * Changes will be buffered if {@link #isChangeBuffering()} is {@code true}, else they will be applied immediately.
     * @param className the name of the class object of the sub-setting relation ({@code C}).
     * @param propertyName the name of the property of the sub-setting relation ({@code p}).
     * @param cardinality the cardinality of the minimal relation ({@code d}).
     * @param valueName the name of the value of the sub-setting relation ({@code V}).
     * @return the changes to be applied in order to make a class being a sub-set of a maximum number of properties
     * restricted to a class.
     */
    public OWLOntologyChange addMaxObjectClassExpression( String className, String propertyName, int cardinality, String valueName){
        OWLClass cl = ontoRef.getOWLClass( className);
        OWLObjectProperty property = ontoRef.getOWLObjectProperty( propertyName);
        OWLClass value = ontoRef.getOWLClass( valueName);
        return addMaxObjectClassExpression( cl, property, cardinality, value);
    }
    /**
     * Returns the changes to make a class be a sub class of an object property expression
     * exactly identified by a given cardinality class restriction.
     * In symbols: {@code C &sub; p(=<sub>d</sub> V)}, where: {@code C} is the class, {@code p} the object property,
     * {@code V} is the class value and {@code d}, the cardinality.
     * Changes will be buffered if {@link #isChangeBuffering()} is {@code true}, else they will be applied immediately.
     * @param cl the class object of the sub-setting relation ({@code C}).
     * @param property the property of the sub-setting relation ({@code p}).
     * @param cardinality the cardinality of the minimal relation ({@code d}).
     * @param value the value of the sub-setting relation ({@code V}).
     * @return the changes to be applied in order to make a class being a sub-set of a exact number of properties
     * restricted to a class.
     */
    public OWLOntologyChange addExactObjectClassExpression(OWLClass cl, OWLObjectProperty property, int cardinality, OWLClass value){
        return addObjectClassExpression( cl, property, cardinality, value, RESTRICTION_EXACT);
    }
    /**
     * Returns the changes to make a class be a sub class of an object property expression
     * exactly identified by a given cardinality class restriction.
     * In symbols: {@code C &sub; p(=<sub>d</sub> V)}, where: {@code C} is the class, {@code p} the object property,
     * {@code V} is the class value and {@code d}, the cardinality.
     * Changes will be buffered if {@link #isChangeBuffering()} is {@code true}, else they will be applied immediately.
     * @param className the name of the class object of the sub-setting relation ({@code C}).
     * @param propertyName the name of the property of the sub-setting relation ({@code p}).
     * @param cardinality the cardinality of the minimal relation ({@code d}).
     * @param valueName the name of the value of the sub-setting relation ({@code V}).
     * @return the changes to be applied in order to make a class being a sub-set of a exact number of properties
     * restricted to a class.
     */
    public OWLOntologyChange addExactObjectClassExpression( String className, String propertyName, int cardinality, String valueName){
        OWLClass cl = ontoRef.getOWLClass( className);
        OWLObjectProperty property = ontoRef.getOWLObjectProperty( propertyName);
        OWLClass value = ontoRef.getOWLClass( valueName);
        return addExactObjectClassExpression( cl, property, cardinality, value);
    }

    private OWLOntologyChange addObjectClassExpression(OWLClass cl, OWLObjectProperty property, int cardinality, OWLClass value, int directive){
        try{
            long initialTime = System.nanoTime();
            if( cardinality < 0){
                logger.addDebugString( "cannot assign a negative cardinality to " + ontoRef.getOWLObjectName( cl), true);
                return null;
            }
            OWLObjectRestriction cardinalityAxiom = getObjectCardinalityAxioms(cardinality, property, value, directive);
            OWLSubClassOfAxiom subClassAxiom = ontoRef.getOWLFactory().getOWLSubClassOfAxiom( cl, cardinalityAxiom);
            OWLOntologyChange adding = getAddAxiom( subClassAxiom, manipulationBuffering);
            if( !manipulationBuffering)
                applyChanges( adding);
            logger.addDebugString( "add class expression on entity: " + ontoRef.getOWLObjectName( cl) + " " + ontoRef.getOWLObjectName( property)
                    + " " + getCardinalityDebug( directive) + " " + cardinality + " of " + ontoRef.getOWLObjectName( value)  +
                    " in: " + (System.nanoTime() - initialTime) + " [ns]");
            return( adding);
        } catch( org.semanticweb.owlapi.reasoner.InconsistentOntologyException e){
            ontoRef.logInconsistency();
            return( null);
        }
    }
    private OWLObjectRestriction getObjectCardinalityAxioms(int cardinality, OWLObjectProperty property, OWLClass value, int directive){
        // shared between add and removing (mutex)
        if( directive == RESTRICTION_EXACT)
            return ontoRef.getOWLFactory().getOWLObjectExactCardinality( cardinality, property, value);
        if( directive == RESTRICTION_MIN)
            return ontoRef.getOWLFactory().getOWLObjectMinCardinality( cardinality, property, value);
        if( directive == RESTRICTION_MAX)
            return ontoRef.getOWLFactory().getOWLObjectMaxCardinality( cardinality, property, value);
        if( directive == RESTRICTION_SOME)
            return ontoRef.getOWLFactory().getOWLObjectSomeValuesFrom( property, value);
        if( directive == RESTRICTION_ONLY)
            return ontoRef.getOWLFactory().getOWLObjectAllValuesFrom( property, value);
        return null; // should never happen
    }
    private String getCardinalityDebug( int directive){
        if( directive == RESTRICTION_EXACT)
            return  "EXACT";
        if( directive == RESTRICTION_MIN)
            return  "MIN";
        if( directive == RESTRICTION_MAX)
            return  "MAX";
        if( directive == RESTRICTION_ONLY)
            return "ONLY";
        if( directive == RESTRICTION_SOME)
            return "SOME";
        return null; // should never happen
    }

    /**
     * Returns the changes to make a class be a sub class of a data property in existence with a data type value.
     * In symbols: {@code C &sub; p(&exist; D)}, where: {@code C} is the class, {@code p} the data property
     * and {@code D}, the type of data (supported {@link String}, {@link Integer}, {@link Double},
     * {@link Float} and {@link Long}).
     * Changes will be buffered if {@link #isChangeBuffering()} is {@code true}, else they will be applied immediately.
     * @param cl the class object of the sub-setting relation ({@code C}).
     * @param property the property of the sub-setting relation ({@code p}).
     * @param type the Class representing a supported data type for the sub-setting relation ({@code D}).
     * @return the changes to be applied in order to make a class being a sub-set of an existential property.
     */
    public OWLOntologyChange addSomeDataClassExpression(OWLClass cl, OWLDataProperty property, Class type){
        return addDataClassExpression( cl, property, 0, type, RESTRICTION_SOME);
    }
    /**
     * Returns the changes to make a class be a sub class of a data property in existence with a data type value.
     * In symbols: {@code C &sub; p(&exist; D)}, where: {@code C} is the class, {@code p} the data property
     * and {@code D}, the type of data (supported {@link String}, {@link Integer}, {@link Double},
     * {@link Float} and {@link Long}).
     * Changes will be buffered if {@link #isChangeBuffering()} is {@code true}, else they will be applied immediately.
     * @param className the name of the class object of the sub-setting relation ({@code C}).
     * @param propertyName the name of the property of the sub-setting relation ({@code p}).
     * @param type the Class representing a supported data type for the sub-setting relation ({@code D}).
     * @return the changes to be applied in order to make a class being a sub-set of an existential property.
     */
    public OWLOntologyChange addSomeDataClassExpression(String className, String propertyName, Class type){
        OWLClass cl = ontoRef.getOWLClass( className);
        OWLDataProperty property = ontoRef.getOWLDataProperty( propertyName);
        return addSomeDataClassExpression( cl, property, type);
    }
    /**
     * Returns the changes to make a class be a sub class of a data property universally identified by a data type value.
     * In symbols: {@code C &sub; p(&forall; D)}, where: {@code C} is the class, {@code p} the data property
     * and {@code D}, the type of data (supported {@link String}, {@link Integer}, {@link Double},
     * {@link Float} and {@link Long}).
     * Changes will be buffered if {@link #isChangeBuffering()} is {@code true}, else they will be applied immediately.
     * @param cl the class object of the sub-setting relation ({@code C}).
     * @param property the property of the sub-setting relation ({@code p}).
     * @param type the Class representing a supported data type for the sub-setting relation ({@code D}).
     * @return the changes to be applied in order to make a class being a sub-set of an universal property.
     */
    public OWLOntologyChange addOnlyDataClassExpression(OWLClass cl, OWLDataProperty property, Class type){
        return addDataClassExpression( cl, property, 0, type, RESTRICTION_ONLY);
    }
    /**
     * Returns the changes to make a class be a sub class of a data property universally identified by a data type value.
     * In symbols: {@code C &sub; p(&forall; D)}, where: {@code C} is the class, {@code p} the data property
     * and {@code D}, the type of data (supported {@link String}, {@link Integer}, {@link Double},
     * {@link Float} and {@link Long}).
     * Changes will be buffered if {@link #isChangeBuffering()} is {@code true}, else they will be applied immediately.
     * @param className the name of the class object of the sub-setting relation ({@code C}).
     * @param propertyName the name of the property of the sub-setting relation ({@code p}).
     * @param type the Class representing a supported data type for the sub-setting relation ({@code D}).
     * @return the changes to be applied in order to make a class being a sub-set of an universal property.
     */
    public OWLOntologyChange addOnlyDataClassExpression(String className, String propertyName, Class type){
        OWLClass cl = ontoRef.getOWLClass( className);
        OWLDataProperty property = ontoRef.getOWLDataProperty( propertyName);
        return addOnlyDataClassExpression( cl, property, type);
    }
    /**
     * Returns the changes to make a class be a sub class of a data property expression
     * minimally identified by a given cardinality class restriction.
     * In symbols: {@code C &sub; p(&lt;<sub>d</sub> D)}, where: {@code C} is the class, {@code p} the object property,
     * {@code V} is the class value, {@code d} the cardinality and {@code D}, the type of data (supported {@link String}, {@link Integer}, {@link Double},
     * {@link Float} and {@link Long}).
     * Changes will be buffered if {@link #isChangeBuffering()} is {@code true}, else they will be applied immediately.
     * @param cl the class object of the sub-setting relation ({@code C}).
     * @param property the property of the sub-setting relation ({@code p}).
     * @param cardinality the cardinality of the minimal relation ({@code d}).
     * @param type the Class representing a supported data type for the sub-setting relation ({@code D}).
     * @return the changes to be applied in order to make a class being a sub-set of a minimum number of
     * properties restricted to a data type.
     */
    public OWLOntologyChange addMinDataClassExpression(OWLClass cl, OWLDataProperty property, int cardinality, Class type){
		return addDataClassExpression( cl, property, cardinality, type, RESTRICTION_MIN);
	}
    /**
     * Returns the changes to make a class be a sub class of a data property expression
     * minimally identified by a given cardinality class restriction.
     * In symbols: {@code C &sub; p(&lt;<sub>d</sub> D)}, where: {@code C} is the class, {@code p} the object property,
     * {@code V} is the class value, {@code d} the cardinality and {@code D}, the type of data (supported {@link String}, {@link Integer}, {@link Double},
     * {@link Float} and {@link Long}).
     * Changes will be buffered if {@link #isChangeBuffering()} is {@code true}, else they will be applied immediately.
     * @param className the name of the class object of the sub-setting relation ({@code C}).
     * @param propertyName the name of the property of the sub-setting relation ({@code p}).
     * @param cardinality the cardinality of the minimal relation ({@code d}).
     * @param type the Class representing a supported data type for the sub-setting relation ({@code D}).
     * @return the changes to be applied in order to make a class being a sub-set of a minimum number of
     * properties restricted to a data type.
     */
	public OWLOntologyChange addMinDataClassExpression( String className, String propertyName, int cardinality, Class type){
		OWLClass cl = ontoRef.getOWLClass( className);
		OWLDataProperty property = ontoRef.getOWLDataProperty( propertyName);
		return addMinDataClassExpression( cl, property, cardinality, type);
	}
    /**
     * Returns the changes to make a class be a sub class of a data property expression
     * maximally identified by a given cardinality class restriction.
     * In symbols: {@code C &sub; p(&gt;<sub>d</sub> D)}, where: {@code C} is the class, {@code p} the object property,
     * {@code V} is the class value, {@code d} the cardinality and {@code D}, the type of data (supported {@link String}, {@link Integer}, {@link Double},
     * {@link Float} and {@link Long}).
     * Changes will be buffered if {@link #isChangeBuffering()} is {@code true}, else they will be applied immediately.
     * @param cl the class object of the sub-setting relation ({@code C}).
     * @param property the property of the sub-setting relation ({@code p}).
     * @param cardinality the cardinality of the minimal relation ({@code d}).
     * @param type the Class representing a supported data type for the sub-setting relation ({@code D}).
     * @return the changes to be applied in order to make a class being a sub-set of a maximum number of
     * properties restricted to a data type.
     */
	public OWLOntologyChange addMaxDataClassExpression(OWLClass cl, OWLDataProperty property, int cardinality, Class type){
		return addDataClassExpression( cl, property, cardinality, type, RESTRICTION_MAX);
	}
    /**
     * Returns the changes to make a class be a sub class of a data property expression
     * maximally identified by a given cardinality class restriction.
     * In symbols: {@code C &sub; p(&gt;<sub>d</sub> D)}, where: {@code C} is the class, {@code p} the object property,
     * {@code V} is the class value, {@code d} the cardinality and {@code D}, the type of data (supported {@link String}, {@link Integer}, {@link Double},
     * {@link Float} and {@link Long}).
     * Changes will be buffered if {@link #isChangeBuffering()} is {@code true}, else they will be applied immediately.
     * @param className the name of the class object of the sub-setting relation ({@code C}).
     * @param propertyName the name of the property of the sub-setting relation ({@code p}).
     * @param cardinality the cardinality of the minimal relation ({@code d}).
     * @param type the Class representing a supported data type for the sub-setting relation ({@code D}).
     * @return the changes to be applied in order to make a class being a sub-set of a maximum number of
     * properties restricted to a data type.
     */
	public OWLOntologyChange addMaxDataClassExpression(String className, String propertyName, int cardinality, Class type){
		OWLClass cl = ontoRef.getOWLClass( className);
		OWLDataProperty property = ontoRef.getOWLDataProperty( propertyName);
		return addMaxDataClassExpression( cl, property, cardinality, type);
	}
    /**
     * Returns the changes to make a class be a sub class of a data property expression
     * exactly identified by a given cardinality class restriction.
     * In symbols: {@code C &sub; p(=<sub>d</sub> D)}, where: {@code C} is the class, {@code p} the object property,
     * {@code V} is the class value, {@code d} the cardinality and {@code D}, the type of data (supported {@link String}, {@link Integer}, {@link Double},
     * {@link Float} and {@link Long}).
     * Changes will be buffered if {@link #isChangeBuffering()} is {@code true}, else they will be applied immediately.
     * @param cl the class object of the sub-setting relation ({@code C}).
     * @param property the property of the sub-setting relation ({@code p}).
     * @param cardinality the cardinality of the minimal relation ({@code d}).
     * @param type the Class representing a supported data type for the sub-setting relation ({@code D}).
     * @return the changes to be applied in order to make a class being a sub-set of an exact number of
     * properties restricted to a data type.
     */
	public OWLOntologyChange addExactDataClassExpression(OWLClass cl, OWLDataProperty property, int cardinality, Class type){
		return addDataClassExpression( cl, property, cardinality, type, RESTRICTION_EXACT);
	}
    /**
     * Returns the changes to make a class be a sub class of a data property expression
     * exactly identified by a given cardinality class restriction.
     * In symbols: {@code C &sub; p(=<sub>d</sub> D)}, where: {@code C} is the class, {@code p} the object property,
     * {@code V} is the class value, {@code d} the cardinality and {@code D}, the type of data (supported {@link String}, {@link Integer}, {@link Double},
     * {@link Float} and {@link Long}).
     * Changes will be buffered if {@link #isChangeBuffering()} is {@code true}, else they will be applied immediately.
     * @param className the name of the class object of the sub-setting relation ({@code C}).
     * @param propertyName the name of the property of the sub-setting relation ({@code p}).
     * @param cardinality the cardinality of the minimal relation ({@code d}).
     * @param type the Class representing a supported data type for the sub-setting relation ({@code D}).
     * @return the changes to be applied in order to make a class being a sub-set of an exact number of
     * properties restricted to a data type.
     */
	public OWLOntologyChange addExactDataClassExpression( String className, String propertyName, int cardinality, Class type){
		OWLClass cl = ontoRef.getOWLClass( className);
		OWLDataProperty property = ontoRef.getOWLDataProperty( propertyName);
		return addExactDataClassExpression( cl, property, cardinality, type);
	}
	private OWLOntologyChange addDataClassExpression(OWLClass cl, OWLDataProperty property, int cardinality, Class c, int directive){
        return addDataClassExpression( cl, property, cardinality, getDataType( c), directive);
    }
    private OWLOntologyChange addDataClassExpression(OWLClass cl, OWLDataProperty property, int cardinality, OWLDatatype value, int directive){
		try{
			long initialTime = System.nanoTime();
            if( cardinality < 0){
                logger.addDebugString( "cannot assign a negative cardinality to " + ontoRef.getOWLObjectName( cl), true);
                return null;
            }
            if( value == null)
                return null;
            OWLDataRestriction cardinalityAxiom = getDataCardinalityAxioms(cardinality, property, value, directive);
			OWLSubClassOfAxiom subClassAxiom = ontoRef.getOWLFactory().getOWLSubClassOfAxiom( cl, cardinalityAxiom);
			OWLOntologyChange adding = getAddAxiom( subClassAxiom, manipulationBuffering);
			if( !manipulationBuffering)
				applyChanges( adding);
			logger.addDebugString( "add class expression on entity: " + ontoRef.getOWLObjectName( cl) + " " + ontoRef.getOWLObjectName( property)
					+ " " + getCardinalityDebug( directive) + " " + cardinality + " of " + ontoRef.getOWLObjectName( value)  +
					" in: " + (System.nanoTime() - initialTime) + " [ns]");
			return( adding);
		} catch( org.semanticweb.owlapi.reasoner.InconsistentOntologyException e){
			ontoRef.logInconsistency();
			return( null);
		}
	}
    private OWLDataRestriction getDataCardinalityAxioms(int cardinality, OWLDataProperty property, OWLDatatype type, int directive){
        if( directive == RESTRICTION_EXACT)
            return ontoRef.getOWLFactory().getOWLDataExactCardinality( cardinality, property, type);
        if( directive == RESTRICTION_MIN)
            return ontoRef.getOWLFactory().getOWLDataMinCardinality( cardinality, property, type);
        if( directive == RESTRICTION_MAX)
            return ontoRef.getOWLFactory().getOWLDataMaxCardinality( cardinality, property, type);
        if( directive == RESTRICTION_SOME)
            return ontoRef.getOWLFactory().getOWLDataSomeValuesFrom( property, type);
        if( directive == RESTRICTION_ONLY)
            return ontoRef.getOWLFactory().getOWLDataAllValuesFrom( property, type);
        return null; // should never happen
    }
    private OWLDatatype getDataType( Class c){
        if( c.equals( Double.class))
            return ontoRef.getOWLFactory().getDoubleOWLDatatype();
        else if( c.equals( Float.class))
            return ontoRef.getOWLFactory().getFloatOWLDatatype();
        else if( c.equals( String.class))
            return ontoRef.getOWLFactory().getStringOWLDatatype();
        else if( c.equals( Boolean.class))
            return ontoRef.getOWLFactory().getBooleanOWLDatatype();
        else if( c.equals( Long.class))
            return ontoRef.getOWLFactory().getOWLDatatype( OWL2Datatype.XSD_LONG.getIRI());
        else if( c.equals( Integer.class))
            return ontoRef.getOWLFactory().getIntegerOWLDatatype();
        else {
            logger.addDebugString( "cannot add data property cardinality over a class that is not ether: Float, Double, Boolean, Integer, Long or String. "
                    + c.getSimpleName() + " found instead.", true);
            return null;
        }
    }

    /**
     * Returns the changes to make a class for being a sub class of a data or object property expression,
     * exactly identified by a given cardinality class restriction and type.
     * @param restriction the object describing the class restriction
     * @return the change to be applied
     */
    public OWLOntologyChange addClassExpression( ClassRestriction restriction){
        if ( restriction.restrictsOverDataProperty())
            return addDataClassExpression( restriction.isDefinitionOf(),
                    restriction.getDataProperty(), restriction.getCardinality(),
                    restriction.getDataTypeRestriction().asOWLDatatype(), restriction.getExpressiontType());
        else // object property
            return addObjectClassExpression( restriction.isDefinitionOf(),
                    restriction.getObjectProperty(), restriction.getCardinality(),
                    restriction.getObjectRestriction(), restriction.getExpressiontType());
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
            Set<OWLOntology> onts = new HashSet<>();// todo move to MOR manipulator
            onts.add( ontoRef.getOWLOntology());
            List<OWLOntologyChange> changes = new ConvertSuperClassesToEquivalentClass( ontoRef.getOWLFactory(), cl, onts, ontoRef.getOWLOntology()).getChanges();
            for( OWLOntologyChange c : changes) {
                if ( ! manipulationBuffering)
                    applyChanges( c);
                else changeList.add( c);
            }
            logger.addDebugString( "converting super class: " + ontoRef.getOWLObjectName( cl) + " to equivalent class" +
                    " in: " + (System.nanoTime() - initialTime) + " [ns]");
            return( changes);
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


    // all on the same mutex oa add!!!!!
    /**
     * Returns the changes to make a class no more being a sub class of an
     * object property in existence with a class value.
     * In symbols, it will be no more true that: {@code C &sub; p(&exist; V)},
     * where: {@code C} is the class, {@code p} the object property
     * and {@code V}, the class value.
     * Changes will be buffered if {@link #isChangeBuffering()} is {@code true}, else they will be applied immediately.
     * @param cl the class object of the sub-setting relation ({@code C}).
     * @param property the property of the sub-setting relation ({@code p}).
     * @param value the value of the sub-setting relation ({@code V}).
     * @return the changes to be applied in order to remove the fact that a class
     * is a sub-set of an existential property.
     */
    public OWLOntologyChange removeSomeObjectClassExpression(OWLClass cl, OWLObjectProperty property, OWLClass value){
        return removeObjectClassExpression( cl, property, 0, value, RESTRICTION_SOME);
    }
    /**
     * Returns the changes to make a class no more being a sub class of an
     * object property in existence with a class value.
     * In symbols, it will be no more true that: {@code C &sub; p(&exist; V)},
     * where: {@code C} is the class, {@code p} the object property
     * and {@code V}, the class value.
     * Changes will be buffered if {@link #isChangeBuffering()} is {@code true}, else they will be applied immediately.
     * @param className the name of the class object of the sub-setting relation ({@code C}).
     * @param propertyName the name of the property of the sub-setting relation ({@code p}).
     * @param valueName the name of the value of the sub-setting relation ({@code V}).
     * @return the changes to be applied in order to remove the fact that a class
     * is a sub-set of an existential property.
     */
    public OWLOntologyChange removeSomeObjectClassExpression(String className, String propertyName, String valueName){
        OWLClass cl = ontoRef.getOWLClass( className);
        OWLObjectProperty property = ontoRef.getOWLObjectProperty( propertyName);
        OWLClass value = ontoRef.getOWLClass( valueName);
        return removeSomeObjectClassExpression( cl, property, value);
    }
    /**
     * Returns the changes to make a class no more being a sub class of an
     * object property universally identified with a class value.
     * In symbols, it will be no more true that: {@code C &sub; p(&forall; V)},
     * where: {@code C} is the class, {@code p} the object property
     * and {@code V}, the class value.
     * Changes will be buffered if {@link #isChangeBuffering()} is {@code true}, else they will be applied immediately.
     * @param cl the class object of the sub-setting relation ({@code C}).
     * @param property the property of the sub-setting relation ({@code p}).
     * @param value the value of the sub-setting relation ({@code V}).
     * @return the changes to be applied in order to remove the fact that a class
     * is a sub-set of an universal property.
     */
    public OWLOntologyChange removeOnlyObjectClassExpression(OWLClass cl, OWLObjectProperty property, OWLClass value){
        return removeObjectClassExpression( cl, property, 0, value, RESTRICTION_ONLY);
    }
    /**
     * Returns the changes to make a class no more being a sub class of an
     * object property universally identified with a class value.
     * In symbols, it will be no more true that: {@code C &sub; p(&forall; V)},
     * where: {@code C} is the class, {@code p} the object property
     * and {@code V}, the class value.
     * Changes will be buffered if {@link #isChangeBuffering()} is {@code true}, else they will be applied immediately.
     * @param className the name of the class object of the sub-setting relation ({@code C}).
     * @param propertyName the name of the property of the sub-setting relation ({@code p}).
     * @param valueName the name of the value of the sub-setting relation ({@code V}).
     * @return the changes to be applied in order to remove the fact that a class
     * is a sub-set of an universal property.
     */
    public OWLOntologyChange removeOnlyObjectClassExpression(String className, String propertyName, String valueName){
        OWLClass cl = ontoRef.getOWLClass( className);
        OWLObjectProperty property = ontoRef.getOWLObjectProperty( propertyName);
        OWLClass value = ontoRef.getOWLClass( valueName);
        return removeOnlyObjectClassExpression( cl, property, value);
    }
    /**
     * Returns the changes to make a class not being a sub class of an object property expression,
     * minimally identified by a given cardinality class restriction, anymore.
     * In symbols, it will be no more true that:: {@code C &sub; p(&lt;<sub>d</sub> V)}, 
     * where: {@code C} is the class, {@code p} the object property,
     * {@code V} is the class value and {@code d}, the cardinality.
     * Changes will be buffered if {@link #isChangeBuffering()} is {@code true}, else they will be applied immediately.
     * @param cl the class object of the sub-setting relation ({@code C}).
     * @param property the property of the sub-setting relation ({@code p}).
     * @param cardinality the cardinality of the minimal relation ({@code d}).
     * @param value the value of the sub-setting relation ({@code V}).
     * @return the changes to be applied in order to remove tha fact that
     * a class is a sub-set of a minimum number of properties
     * restricted to a class.
     */
    public OWLOntologyChange removeMinObjectClassExpression(OWLClass cl, OWLObjectProperty property, int cardinality, OWLClass value){
        return removeObjectClassExpression( cl, property, cardinality, value, RESTRICTION_MIN);
    }
    /**
     * Returns the changes to make a class not being a sub class of an object property expression,
     * minimally identified by a given cardinality class restriction, anymore.
     * In symbols, it will be no more true that:: {@code C &sub; p(&lt;<sub>d</sub> V)}, 
     * where: {@code C} is the class, {@code p} the object property,
     * {@code V} is the class value and {@code d}, the cardinality.
     * Changes will be buffered if {@link #isChangeBuffering()} is {@code true}, else they will be applied immediately.
     * @param className the name of the class object of the sub-setting relation ({@code C}).
     * @param propertyName the name of the property of the sub-setting relation ({@code p}).
     * @param cardinality the cardinality of the minimal relation ({@code d}).
     * @param valueName the name of the value of the sub-setting relation ({@code V}).
     * @return the changes to be applied in order to remove tha fact that
     * a class is a sub-set of a minimum number of properties
     * restricted to a class.
     */
    public OWLOntologyChange removeMinObjectClassExpression(String className, String propertyName, int cardinality, String valueName){
        OWLClass cl = ontoRef.getOWLClass( className);
        OWLObjectProperty property = ontoRef.getOWLObjectProperty( propertyName);
        OWLClass value = ontoRef.getOWLClass( valueName);
        return removeMinObjectClassExpression( cl, property, cardinality, value);
    }
    /**
     * Returns the changes to make a class not being a sub class of an object property expression,
     * maximally identified by a given cardinality class restriction, anymore.
     * In symbols, it will be no more true that:: {@code C &sub; p(&gt;<sub>d</sub> V)}, 
     * where: {@code C} is the class, {@code p} the object property,
     * {@code V} is the class value and {@code d}, the cardinality.
     * Changes will be buffered if {@link #isChangeBuffering()} is {@code true}, else they will be applied immediately.
     * @param cl the class object of the sub-setting relation ({@code C}).
     * @param property the property of the sub-setting relation ({@code p}).
     * @param cardinality the cardinality of the minimal relation ({@code d}).
     * @param value the value of the sub-setting relation ({@code V}).
     * @return the changes to be applied in order to remove tha fact that
     * a class is a sub-set of a maximum number of properties
     * restricted to a class.
     */
    public OWLOntologyChange removeMaxObjectClassExpression(OWLClass cl, OWLObjectProperty property, int cardinality, OWLClass value){
        return removeObjectClassExpression( cl, property, cardinality, value, RESTRICTION_MAX);
    }
    /**
     * Returns the changes to make a class not being a sub class of an object property expression,
     * maximally identified by a given cardinality class restriction, anymore.
     * In symbols, it will be no more true that:: {@code C &sub; p(&gt;<sub>d</sub> V)},
     * where: {@code C} is the class, {@code p} the object property,
     * {@code V} is the class value and {@code d}, the cardinality.
     * Changes will be buffered if {@link #isChangeBuffering()} is {@code true}, else they will be applied immediately.
     * @param className the name of the class object of the sub-setting relation ({@code C}).
     * @param propertyName the name of the property of the sub-setting relation ({@code p}).
     * @param cardinality the cardinality of the minimal relation ({@code d}).
     * @param valueName the name of the value of the sub-setting relation ({@code V}).
     * @return the changes to be applied in order to remove tha fact that
     * a class is a sub-set of a maximum number of properties
     * restricted to a class.
     */
    public OWLOntologyChange removeMaxObjectClassExpression( String className, String propertyName, int cardinality, String valueName){
        OWLClass cl = ontoRef.getOWLClass( className);
        OWLObjectProperty property = ontoRef.getOWLObjectProperty( propertyName);
        OWLClass value = ontoRef.getOWLClass( valueName);
        return removeMaxObjectClassExpression( cl, property, cardinality, value);
    }
    /**
     * Returns the changes to make a class not being a sub class of an object property expression,
     * exactly identified by a given cardinality class restriction, anymore.
     * In symbols, it will be no more true that:: {@code C &sub; p(=<sub>d</sub> V)},
     * where: {@code C} is the class, {@code p} the object property,
     * {@code V} is the class value and {@code d}, the cardinality.
     * Changes will be buffered if {@link #isChangeBuffering()} is {@code true}, else they will be applied immediately.
     * @param cl the class object of the sub-setting relation ({@code C}).
     * @param property the property of the sub-setting relation ({@code p}).
     * @param cardinality the cardinality of the minimal relation ({@code d}).
     * @param value the value of the sub-setting relation ({@code V}).
     * @return the changes to be applied in order to remove tha fact that
     * a class is a sub-set of a exact number of properties
     * restricted to a class.
     */
    public OWLOntologyChange removeExactObjectClassExpression(OWLClass cl, OWLObjectProperty property, int cardinality, OWLClass value){
        return removeObjectClassExpression( cl, property, cardinality, value, RESTRICTION_EXACT);
    }
    /**
     * Returns the changes to make a class not being a sub class of an object property expression,
     * exactly identified by a given cardinality class restriction, anymore.
     * In symbols, it will be no more true that:: {@code C &sub; p(=<sub>d</sub> V)},
     * where: {@code C} is the class, {@code p} the object property,
     * {@code V} is the class value and {@code d}, the cardinality.
     * Changes will be buffered if {@link #isChangeBuffering()} is {@code true}, else they will be applied immediately.
     * @param className the nme of the class object of the sub-setting relation ({@code C}).
     * @param propertyName the name of the property of the sub-setting relation ({@code p}).
     * @param cardinality the cardinality of the minimal relation ({@code d}).
     * @param valueName the name of the value of the sub-setting relation ({@code V}).
     * @return the changes to be applied in order to remove tha fact that
     * a class is a sub-set of a exact number of properties
     * restricted to a class.
     */
    public OWLOntologyChange removeExactObjectClassExpression( String className, String propertyName, int cardinality, String valueName){
        OWLClass cl = ontoRef.getOWLClass( className);
        OWLObjectProperty property = ontoRef.getOWLObjectProperty( propertyName);
        OWLClass value = ontoRef.getOWLClass( valueName);
        return removeExactObjectClassExpression( cl, property, cardinality, value);
    }
    private OWLOntologyChange removeObjectClassExpression(OWLClass cl, OWLObjectProperty property, int cardinality, OWLClass value, int directive){
        try{
            long initialTime = System.nanoTime();
            if( cardinality < 0){
                logger.addDebugString( "cannot assign a negative cardinality to " + ontoRef.getOWLObjectName( cl), true);
                return null;
            }
            OWLObjectRestriction cardinalityAxiom = getObjectCardinalityAxioms(cardinality, property, value, directive);
            OWLSubClassOfAxiom subClassAxiom = ontoRef.getOWLFactory().getOWLSubClassOfAxiom( cl, cardinalityAxiom);
            OWLOntologyChange adding = getRemoveAxiom( subClassAxiom, manipulationBuffering);
            if( !manipulationBuffering)
                applyChanges( adding);
            logger.addDebugString( "remove class expression on entity: " + ontoRef.getOWLObjectName( cl) + " " + ontoRef.getOWLObjectName( property)
                    + " " + getCardinalityDebug( directive) + " " + cardinality + " of " + ontoRef.getOWLObjectName( value)  +
                    " in: " + (System.nanoTime() - initialTime) + " [ns]");
            return( adding);
        } catch( org.semanticweb.owlapi.reasoner.InconsistentOntologyException e){
            ontoRef.logInconsistency();
            return( null);
        }
    }

    // all on the same mutex!!!!!
    /**
     * Returns the changes to make a class not being a sub class of a data property,
     * in existence with a data type value, anymore.
     * In symbols, it will be not long true that: {@code C &sub; p(&exist; D)},
     * where: {@code C} is the class, {@code p} the data property
     * and {@code D}, the type of data (supported {@link String}, {@link Integer}, {@link Double},
     * {@link Float} and {@link Long}).
     * Changes will be buffered if {@link #isChangeBuffering()} is {@code true}, else they will be applied immediately.
     * @param cl the class object of the sub-setting relation ({@code C}).
     * @param property the property of the sub-setting relation ({@code p}).
     * @param type the Class representing a supported data type for the sub-setting relation ({@code D}).
     * @return the changes to be applied in order to remove the fact that
     * a class is a sub-set of an existential property.
     */
    public OWLOntologyChange removeSomeDataClassExpression(OWLClass cl, OWLDataProperty property, Class type){
        return removeDataClassExpression( cl, property, 0, type, RESTRICTION_SOME);
    }
    /**
     * Returns the changes to make a class not being a sub class of a data property,
     * in existence with a data type value, anymore.
     * In symbols, it will be not long true that: {@code C &sub; p(&exist; D)},
     * where: {@code C} is the class, {@code p} the data property
     * and {@code D}, the type of data (supported {@link String}, {@link Integer}, {@link Double},
     * {@link Float} and {@link Long}).
     * Changes will be buffered if {@link #isChangeBuffering()} is {@code true}, else they will be applied immediately.
     * @param className the name of the class object of the sub-setting relation ({@code C}).
     * @param propertyName the name of the property of the sub-setting relation ({@code p}).
     * @param type the Class representing a supported data type for the sub-setting relation ({@code D}).
     * @return the changes to be applied in order to remove the fact that
     * a class is a sub-set of an existential property.
     */
    public OWLOntologyChange removeSomeDataClassExpression(String className, String propertyName, Class type){
        OWLClass cl = ontoRef.getOWLClass( className);
        OWLDataProperty property = ontoRef.getOWLDataProperty( propertyName);
        return removeSomeDataClassExpression( cl, property, type);
    }
    /**
     * Returns the changes to make a class not being a sub class of a data property,
     * universally qualified by a data type value, anymore.
     * In symbols, it will be not long true that: {@code C &sub; p(&forall; D)},
     * where: {@code C} is the class, {@code p} the data property
     * and {@code D}, the type of data (supported {@link String}, {@link Integer}, {@link Double},
     * {@link Float} and {@link Long}).
     * Changes will be buffered if {@link #isChangeBuffering()} is {@code true}, else they will be applied immediately.
     * @param cl the class object of the sub-setting relation ({@code C}).
     * @param property the property of the sub-setting relation ({@code p}).
     * @param type the Class representing a supported data type for the sub-setting relation ({@code D}).
     * @return the changes to be applied in order to remove the fact that
     * a class is a sub-set of an universal property.
     */
    public OWLOntologyChange removeOnlyDataClassExpression(OWLClass cl, OWLDataProperty property, Class type){
        return removeDataClassExpression( cl, property, 0, type, RESTRICTION_ONLY);
    }
    /**
     * Returns the changes to make a class not being a sub class of a data property,
     * universally qualified by a data type value, anymore.
     * In symbols, it will be not long true that: {@code C &sub; p(&forall; D)},
     * where: {@code C} is the class, {@code p} the data property
     * and {@code D}, the type of data (supported {@link String}, {@link Integer}, {@link Double},
     * {@link Float} and {@link Long}).
     * Changes will be buffered if {@link #isChangeBuffering()} is {@code true}, else they will be applied immediately.
     * @param className the name of the class object of the sub-setting relation ({@code C}).
     * @param propertyName the name of the property of the sub-setting relation ({@code p}).
     * @param type the Class representing a supported data type for the sub-setting relation ({@code D}).
     * @return the changes to be applied in order to remove the fact that
     * a class is a sub-set of an universal property.
     */
    public OWLOntologyChange removeOnlyDataClassExpression(String className, String propertyName, Class type){
        OWLClass cl = ontoRef.getOWLClass( className);
        OWLDataProperty property = ontoRef.getOWLDataProperty( propertyName);
        return removeOnlyDataClassExpression( cl, property, type);
    }
    /**
     * Returns the changes to make a class not being a sub class of a data property expression,
     * minimally identified by a given cardinality class restriction, anymore.
     * In symbols, it will not true that: {@code C &sub; p(&lt;<sub>d</sub> D)},
     * where: {@code C} is the class, {@code p} the object property,
     * {@code V} is the class value, {@code d} the cardinality and {@code D}, the type of data (supported {@link String}, {@link Integer}, {@link Double},
     * {@link Float} and {@link Long}).
     * Changes will be buffered if {@link #isChangeBuffering()} is {@code true}, else they will be applied immediately.
     * @param cl the class object of the sub-setting relation ({@code C}).
     * @param property the property of the sub-setting relation ({@code p}).
     * @param cardinality the cardinality of the minimal relation ({@code d}).
     * @param type the Class representing a supported data type for the sub-setting relation ({@code D}).
     * @return the changes to be applied in order to remove the fact that a class
     * is sub-set of a minimum number of properties restricted to a data type.
     */
    public OWLOntologyChange removeMinDataClassExpression(OWLClass cl, OWLDataProperty property, int cardinality, Class type){
        return removeDataClassExpression( cl, property, cardinality, type, RESTRICTION_MIN);
    }
    /**
     * Returns the changes to make a class not being a sub class of a data property expression,
     * minimally identified by a given cardinality class restriction, anymore.
     * In symbols, it will not true that: {@code C &sub; p(&lt;<sub>d</sub> D)},
     * where: {@code C} is the class, {@code p} the object property,
     * {@code V} is the class value, {@code d} the cardinality and {@code D}, the type of data (supported {@link String}, {@link Integer}, {@link Double},
     * {@link Float} and {@link Long}).
     * Changes will be buffered if {@link #isChangeBuffering()} is {@code true}, else they will be applied immediately.
     * @param className the name of the class object of the sub-setting relation ({@code C}).
     * @param propertyName the name of the property of the sub-setting relation ({@code p}).
     * @param cardinality the cardinality of the minimal relation ({@code d}).
     * @param type the Class representing a supported data type for the sub-setting relation ({@code D}).
     * @return the changes to be applied in order to remove the fact that a class
     * is sub-set of a minimum number of properties restricted to a data type.
     */
    public OWLOntologyChange removeMinDataClassExpression( String className, String propertyName, int cardinality, Class type){
        OWLClass cl = ontoRef.getOWLClass( className);
        OWLDataProperty property = ontoRef.getOWLDataProperty( propertyName);
        return removeMinDataClassExpression( cl, property, cardinality, type);
    }
    /**
     * Returns the changes to make a class not being a sub class of a data property expression,
     * maximally identified by a given cardinality class restriction, anymore.
     * In symbols, it will not true that: {@code C &sub; p(&gt;<sub>d</sub> D)},
     * where: {@code C} is the class, {@code p} the object property,
     * {@code V} is the class value, {@code d} the cardinality and {@code D}, the type of data (supported {@link String}, {@link Integer}, {@link Double},
     * {@link Float} and {@link Long}).
     * Changes will be buffered if {@link #isChangeBuffering()} is {@code true}, else they will be applied immediately.
     * @param cl the class object of the sub-setting relation ({@code C}).
     * @param property the property of the sub-setting relation ({@code p}).
     * @param cardinality the cardinality of the minimal relation ({@code d}).
     * @param type the Class representing a supported data type for the sub-setting relation ({@code D}).
     * @return the changes to be applied in order to remove the fact that a class
     * is sub-set of a maximum number of properties restricted to a data type.
     */
    public OWLOntologyChange removeMaxDataClassExpression(OWLClass cl, OWLDataProperty property, int cardinality, Class type){
        return removeDataClassExpression( cl, property, cardinality, type, RESTRICTION_MAX);
    }
    /**
     * Returns the changes to make a class not being a sub class of a data property expression,
     * maximally identified by a given cardinality class restriction, anymore.
     * In symbols, it will not true that: {@code C &sub; p(&gt;<sub>d</sub> D)},
     * where: {@code C} is the class, {@code p} the object property,
     * {@code V} is the class value, {@code d} the cardinality and {@code D}, the type of data (supported {@link String}, {@link Integer}, {@link Double},
     * {@link Float} and {@link Long}).
     * Changes will be buffered if {@link #isChangeBuffering()} is {@code true}, else they will be applied immediately.
     * @param className the name of the class object of the sub-setting relation ({@code C}).
     * @param propertyName the name of the property of the sub-setting relation ({@code p}).
     * @param cardinality the cardinality of the minimal relation ({@code d}).
     * @param type the Class representing a supported data type for the sub-setting relation ({@code D}).
     * @return the changes to be applied in order to remove the fact that a class
     * is sub-set of a maximum number of properties restricted to a data type.
     */
    public OWLOntologyChange removeMaxDataClassExpression(String className, String propertyName, int cardinality, Class type){
        OWLClass cl = ontoRef.getOWLClass( className);
        OWLDataProperty property = ontoRef.getOWLDataProperty( propertyName);
        return removeMaxDataClassExpression( cl, property, cardinality, type);
    }
    /**
     * Returns the changes to make a class not being a sub class of a data property expression,
     * exactly identified by a given cardinality class restriction, anymore.
     * In symbols, it will not true that: {@code C &sub; p(=<sub>d</sub> D)},
     * where: {@code C} is the class, {@code p} the object property,
     * {@code V} is the class value, {@code d} the cardinality and {@code D}, the type of data (supported {@link String}, {@link Integer}, {@link Double},
     * {@link Float} and {@link Long}).
     * Changes will be buffered if {@link #isChangeBuffering()} is {@code true}, else they will be applied immediately.
     * @param cl the class object of the sub-setting relation ({@code C}).
     * @param property the property of the sub-setting relation ({@code p}).
     * @param cardinality the cardinality of the minimal relation ({@code d}).
     * @param type the Class representing a supported data type for the sub-setting relation ({@code D}).
     * @return the changes to be applied in order to remove the fact that a class
     * is sub-set of a exact number of properties restricted to a data type.
     */
    public OWLOntologyChange removeExactClassExpression(OWLClass cl, OWLDataProperty property, int cardinality, Class type){
        return removeDataClassExpression( cl, property, cardinality, type, RESTRICTION_EXACT);
    }
    /**
     * Returns the changes to make a class not being a sub class of a data property expression,
     * exactly identified by a given cardinality class restriction, anymore.
     * In symbols, it will not true that: {@code C &sub; p(=<sub>d</sub> D)},
     * where: {@code C} is the class, {@code p} the object property,
     * {@code V} is the class value, {@code d} the cardinality and {@code D}, the type of data (supported {@link String}, {@link Integer}, {@link Double},
     * {@link Float} and {@link Long}).
     * Changes will be buffered if {@link #isChangeBuffering()} is {@code true}, else they will be applied immediately.
     * @param className the name of the class object of the sub-setting relation ({@code C}).
     * @param propertyName the name of the property of the sub-setting relation ({@code p}).
     * @param cardinality the cardinality of the minimal relation ({@code d}).
     * @param type the Class representing a supported data type for the sub-setting relation ({@code D}).
     * @return the changes to be applied in order to remove the fact that a class
     * is sub-set of a exact number of properties restricted to a data type.
     */
    public OWLOntologyChange removeExactDataClassExpression( String className, String propertyName, int cardinality, Class type){
        OWLClass cl = ontoRef.getOWLClass( className);
        OWLDataProperty property = ontoRef.getOWLDataProperty( propertyName);
        return removeExactClassExpression( cl, property, cardinality, type);
    }
    private OWLOntologyChange removeDataClassExpression(OWLClass cl, OWLDataProperty property, int cardinality, Class c, int directive){
        return removeDataClassExpression( cl, property, cardinality, getDataType( c), directive);
    }
    private OWLOntologyChange removeDataClassExpression(OWLClass cl, OWLDataProperty property, int cardinality, OWLDatatype value, int directive){
        try{
            long initialTime = System.nanoTime();
            if( cardinality < 0){
                logger.addDebugString( "cannot assign a negative cardinality to " + ontoRef.getOWLObjectName( cl), true);
                return null;
            }
            if( value == null)
                return null;
            OWLDataRestriction cardinalityAxiom = getDataCardinalityAxioms(cardinality, property, value, directive);
            OWLSubClassOfAxiom subClassAxiom = ontoRef.getOWLFactory().getOWLSubClassOfAxiom( cl, cardinalityAxiom);
            OWLOntologyChange adding = getRemoveAxiom( subClassAxiom, manipulationBuffering);
            if( !manipulationBuffering)
                applyChanges( adding);
            logger.addDebugString( "set minimal class expression on entity: " + ontoRef.getOWLObjectName( cl) + " " + ontoRef.getOWLObjectName( property)
                    + " " + getCardinalityDebug( directive) + " " + cardinality + " of " + ontoRef.getOWLObjectName( value)  +
                    " in: " + (System.nanoTime() - initialTime) + " [ns]");
            return( adding);
        } catch( org.semanticweb.owlapi.reasoner.InconsistentOntologyException e){
            ontoRef.logInconsistency();
            return( null);
        }
    }

    /**
     * Returns the changes to make a class not being a sub class of a data or object property expression,
     * exactly identified by a given cardinality class restriction and type, anymore.
     * @param restriction the object describing the class restriction
     * @return the change to be applied
     */
    public OWLOntologyChange removeClassExpression( ClassRestriction restriction){
        if ( restriction.restrictsOverDataProperty())
            return removeDataClassExpression( restriction.isDefinitionOf(),
                    restriction.getDataProperty(), restriction.getCardinality(),
                    restriction.getDataTypeRestriction().asOWLDatatype(), restriction.getExpressiontType());
        else // object property
            return removeObjectClassExpression( restriction.isDefinitionOf(),
                    restriction.getObjectProperty(), restriction.getCardinality(),
                    restriction.getObjectRestriction(), restriction.getExpressiontType());
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


	// ---------------------------   methods to set individuals and classes disjoint
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


}