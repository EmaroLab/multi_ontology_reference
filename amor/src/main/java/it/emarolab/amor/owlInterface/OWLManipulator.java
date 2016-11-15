package it.emarolab.amor.owlInterface;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.semanticweb.owlapi.model.AddAxiom;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLDifferentIndividualsAxiom;
import org.semanticweb.owlapi.model.OWLDisjointClassesAxiom;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLOntologyChange;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;
import org.semanticweb.owlapi.model.RemoveAxiom;
import org.semanticweb.owlapi.util.OWLEntityRemover;
import org.semanticweb.owlapi.util.OWLEntityRenamer;

import it.emarolab.amor.owlDebugger.Logger;
import it.emarolab.amor.owlDebugger.Logger.LoggerFlag;

// TODO :  SWRL

/**
 * Project: aMOR <br>
 * File: .../src/aMOR.owlInterface/OWLLibrary.java <br>
 *  
 * @author Buoncompagni Luca <br><br>
 * DIBRIS emaroLab,<br> 
 * University of Genoa. <br>
 * Feb 11, 2016 <br>
 * License: GPL v2 <br><br>
 *  
 * <p>
 * This class implements basic ontology manipulations.<br>
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
 * </p>
 * 
 * @version 2.0
 */
public class OWLManipulator{

	/**
	 * Member required to log class activity.
	 * Logs can be activated by setting the flag {@link LoggerFlag#LOG_OWL_MANIPULATOR}
	 */
	private Logger logger = new Logger( this, LoggerFlag.getLogOWLManipulator());

	/**
	 * The default value of the {@link #manipulationBuffering} field.
	 * Used if no value is passed to {@link #OWLManipulator(OWLReferencesInterface)} constructor.
	 */
	public static Boolean DEFAULT_MANIPULATION_BUFFERING = false;
	
	// [[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[   CONSTRUCTOR   ]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]
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

	// [[[[[[[[[[[[[[[[[[[[[[[[[[[[[   CHANGE BUFFERING FLAG MANAGEMENT   ]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]
	/**
	 * Buffered manipulation change. If {@code true}, it buffers changes till {@link #applyChanges()} method is called.
     * Else, it applies changes immediately. In buffered mode, changes can be applied by {@link #applyChanges()}.
	 */
	private Boolean manipulationBuffering;
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


	// [[[[[[[[[[[[[[[[[[[[[[[[[[[[   OWL REFERENCE POINTER MANGMENT  ]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]
	/**
	 * The basic Reference to an ontology.
	 */
	private OWLReferencesInterface ontoRef;
	/**
	 * @return a container of all entities in the ontology.
	 */
	protected OWLReferencesInterface getOwlLibrary(){
		return ontoRef;
	}


	// [[[[[[[[[[[[[[[[[[[[   METHODS TO COLLECT AND APPLY ONTOLOGY CHANGES   ]]]]]]]]]]]]]]]]]]]]]]]]]]
	/**
	 * This is a vector of buffered ontological changes to be applied to this class.
	 * Changes can be applied by calling {@link #applyChanges()}.
	 */
	private final List< OWLOntologyChange> changeList = new ArrayList< OWLOntologyChange>();

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
			AddAxiom addAxiom = new AddAxiom( ontoRef.getOntology(), axiom);//ontoRef.getManager().addAxiom( ontoRef.getOntology(), axiom);
			if( addToChangeList)
				changeList.add( addAxiom);
			logger.addDebugString( "get add axiom in: " + (System.nanoTime() - initialTime) + " [ns]");
			return( addAxiom);
		} catch( org.semanticweb.owlapi.reasoner.InconsistentOntologyException e){
			ontoRef.loggInconsistency();
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
			removeAxiom = new RemoveAxiom( ontoRef.getOntology(), axiom);//ontoRef.getManager().removeAxiom( ontoRef.getOntology(), axiom);
			if( addToChangeList)
				changeList.add( removeAxiom);
		} catch( org.semanticweb.owlapi.reasoner.InconsistentOntologyException e){
			ontoRef.loggInconsistency();
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
			ontoRef.getManager().applyChanges( changeList);
			changeList.clear();
			logger.addDebugString( "apply changes in: " + (System.nanoTime() - initialTime) + " [ns]");
		} catch( org.semanticweb.owlapi.reasoner.InconsistentOntologyException e){
			ontoRef.loggInconsistency();
		}
	}
	/**
	 * It applies a change stored in {@param addAxiom} to the ontology.
	 * @param addAxiom a change to be applied.
	 */
	public synchronized void applyChanges( OWLOntologyChange addAxiom){
		long initialTime = System.nanoTime();
		try{
			ontoRef.getManager().applyChange( addAxiom);
			logger.addDebugString( "apply changes in: " + (System.nanoTime() - initialTime) + " [ns]");
		} catch( org.semanticweb.owlapi.reasoner.InconsistentOntologyException e){
			ontoRef.loggInconsistency();
		}
	}
	/**
	 * It applies all changes stored in {@param addAxiom} to the ontology.
	 * @param addAxiom a the changes to be applied.
	 */
	public synchronized void applyChanges( List< OWLOntologyChange> addAxiom){
		long initialTime = System.nanoTime();
		try{
			ontoRef.getManager().applyChanges( addAxiom);
			logger.addDebugString( "apply changes in: " + (System.nanoTime() - initialTime) + " [ns]");
		} catch( org.semanticweb.owlapi.reasoner.InconsistentOntologyException e){
			ontoRef.loggInconsistency();
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
			OWLAxiom propertyAssertion = ontoRef.getFactory().getOWLObjectPropertyAssertionAxiom( prop, ind, value);
			OWLOntologyChange add = getAddAxiom( propertyAssertion, manipulationBuffering);
			if( !manipulationBuffering)
				applyChanges( add);
			logger.addDebugString( "add object property (" + ontoRef.getOWLObjectName( prop) + ")  belong to individual (" + ontoRef.getOWLObjectName( ind) + ")"
					+ " with value (" + ontoRef.getOWLObjectName( value) + ") in: " + (System.nanoTime() - initialTime) + " [ns]");
			return( add);
		} catch( org.semanticweb.owlapi.reasoner.InconsistentOntologyException e){
			ontoRef.loggInconsistency();
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
			OWLAxiom newAxiom = ontoRef.getFactory().getOWLDataPropertyAssertionAxiom( prop, ind, value);
			OWLOntologyChange add = getAddAxiom( newAxiom, manipulationBuffering);
			if( !manipulationBuffering)
				applyChanges( add);
			logger.addDebugString( "add data property (" + ontoRef.getOWLObjectName( prop) + ") belong to individual"
					+ "(" + ontoRef.getOWLObjectName( ind) + ") with value (" + ontoRef.getOWLObjectName( value) + ") in: " + (System.nanoTime() - initialTime) + " [ns]");
			return( add);
		} catch( org.semanticweb.owlapi.reasoner.InconsistentOntologyException e){
			ontoRef.loggInconsistency();
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
			OWLAxiom newAxiom = ontoRef.getFactory().getOWLClassAssertionAxiom( cls, ind);
			OWLOntologyChange add = getAddAxiom( newAxiom, manipulationBuffering);
			if( !manipulationBuffering)
				applyChanges(add);
			logger.addDebugString( "add individual (" + ontoRef.getOWLObjectName( ind) + ") belong to class (" + ontoRef.getOWLObjectName( cls) + ") in: " + (System.nanoTime() - initialTime) + " [ns]");
			return( add);
		} catch( org.semanticweb.owlapi.reasoner.InconsistentOntologyException e){
			ontoRef.loggInconsistency();
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
		OWLClass things = ontoRef.getFactory().getOWLThing();
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
			OWLSubClassOfAxiom subClAxiom = ontoRef.getFactory().getOWLSubClassOfAxiom( subClass, superClass);
			OWLOntologyChange adding = getAddAxiom( subClAxiom, manipulationBuffering);
			if( !manipulationBuffering)
				applyChanges( adding);
			logger.addDebugString( "set sub class (" + ontoRef.getOWLObjectName( subClass) + ") of super class (" + ontoRef.getOWLObjectName( superClass) + ") in: " + (System.nanoTime() - initialTime) + " [ns]");
			return( adding);
		} catch( org.semanticweb.owlapi.reasoner.InconsistentOntologyException e){
			ontoRef.loggInconsistency();
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
		OWLClass think = ontoRef.getFactory().getOWLThing();
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
			OWLAxiom propertyAssertion = ontoRef.getFactory().getOWLObjectPropertyAssertionAxiom( prop, ind, value);
			OWLOntologyChange remove = getRemoveAxiom( propertyAssertion, manipulationBuffering);
			if( !manipulationBuffering)
				applyChanges(remove);
			logger.addDebugString( "remove object property (" + ontoRef.getOWLObjectName( prop) + ") belong to individual (" + ontoRef.getOWLObjectName( ind) + ") "
					+ " with value (" + ontoRef.getOWLObjectName( value) + ") in: " + (System.nanoTime() - initialTime) + " [ns]");
			return( remove);
		} catch( org.semanticweb.owlapi.reasoner.InconsistentOntologyException e){
			ontoRef.loggInconsistency();
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
			OWLAxiom newAxiom = ontoRef.getFactory().getOWLDataPropertyAssertionAxiom( prop, ind, value);
			OWLOntologyChange remove = getRemoveAxiom( newAxiom, manipulationBuffering);
			if( !manipulationBuffering)
				applyChanges( remove);
			logger.addDebugString( "remove data property (" + ontoRef.getOWLObjectName( prop) + ") belong to individual "
					+ "(" + ontoRef.getOWLObjectName( ind) + ") with value (" + ontoRef.getOWLObjectName( value) + ") in: " + (System.nanoTime() - initialTime) + " [ns]");
			return( remove);
		} catch( org.semanticweb.owlapi.reasoner.InconsistentOntologyException e){
			ontoRef.loggInconsistency();
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
			OWLAxiom newAxiom = ontoRef.getFactory().getOWLClassAssertionAxiom( cls, ind);
			OWLOntologyChange remove = getRemoveAxiom( newAxiom, manipulationBuffering);
			if( !manipulationBuffering)
				applyChanges( remove);
			logger.addDebugString( "remove individual (" + ontoRef.getOWLObjectName( ind) + ") belong to class "
					+ "(" + ontoRef.getOWLObjectName( cls) + ")in: " + (System.nanoTime() - initialTime) + " [ns]");
			return( remove);
		} catch( org.semanticweb.owlapi.reasoner.InconsistentOntologyException e){
			ontoRef.loggInconsistency();
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
		OWLEntityRemover remover = new OWLEntityRemover( Collections.singleton( ontoRef.getOntology())); // ontoRef.getManager(), Collections.singleton( ontoRef.getOntology()));
		individual.accept(remover);
		long initialTime = System.nanoTime();
		List<RemoveAxiom> remove = remover.getChanges();
		if( !manipulationBuffering)
			applyChanges((OWLOntologyChange) remove);
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
			OWLSubClassOfAxiom subClAxiom = ontoRef.getFactory().getOWLSubClassOfAxiom( subClass, superClass);
			OWLOntologyChange adding = getRemoveAxiom( subClAxiom, manipulationBuffering);

			if( !manipulationBuffering)
				applyChanges( adding);
			logger.addDebugString( "remove sub class (" + ontoRef.getOWLObjectName( subClass) + ") of super class (" + ontoRef.getOWLObjectName( superClass) + ") in: " + (System.nanoTime() - initialTime) + " [ns]");
			return( adding);
		} catch( org.semanticweb.owlapi.reasoner.InconsistentOntologyException e){
			ontoRef.loggInconsistency();
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
			OWLClass think = ontoRef.getFactory().getOWLThing();
			OWLSubClassOfAxiom subClAxiom = ontoRef.getFactory().getOWLSubClassOfAxiom( cls, think);
			OWLOntologyChange remove = getRemoveAxiom( subClAxiom, manipulationBuffering);
			if( !manipulationBuffering)
				applyChanges( remove);
			logger.addDebugString( "remove sub class (" + ontoRef.getOWLObjectName( cls) + ") in: " + (System.nanoTime() - initialTime) + " [ns]");
			return( remove);
		} catch( org.semanticweb.owlapi.reasoner.InconsistentOntologyException e){
			ontoRef.loggInconsistency();
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
			ontoRef.loggInconsistency();
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
			ontoRef.loggInconsistency();
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
			ontoRef.loggInconsistency();
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
			ontoRef.loggInconsistency();
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
		OWLEntityRenamer renamer = new OWLEntityRenamer( ontoRef.getManager(), ontoRef.getManager().getOntologies());
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
		OWLEntityRenamer renamer = new OWLEntityRenamer( ontoRef.getManager(), ontoRef.getManager().getOntologies());
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
	public OWLOntologyChange setDisjointIndividualName(Set< String> individualNames){
		Set< OWLNamedIndividual> inds = new HashSet< OWLNamedIndividual>();
		for( String i : individualNames)
			inds.add( ontoRef.getOWLIndividual( i));
		return setDisjointIndividuals( inds);
	}
	/**
	 * Returns the changes required to set some individuals disjoint among themselves.
     * Changes will be buffered if {@link #isChangeBuffering()} is {@code true}, else they will be applied immediately.
	 * @param individuals set of individuals to set as disjoint.
	 * @return changes required to set some individual disjoint.
     * Returned object can be ignored while working in buffering mode.
	 */
	public OWLOntologyChange setDisjointIndividuals(Set< OWLNamedIndividual> individuals){
		try{
			long initialTime = System.nanoTime();
			OWLDifferentIndividualsAxiom differentIndAxiom = ontoRef.getFactory().getOWLDifferentIndividualsAxiom( individuals);
			OWLOntologyChange adding = getAddAxiom( differentIndAxiom, manipulationBuffering);
			
			if( !manipulationBuffering)
				applyChanges( adding);
			logger.addDebugString( "make disjoint individuals: " + ontoRef.getOWLObjectName(individuals) + ". in:" + (System.nanoTime() - initialTime) + " [ns]");
			return( adding);
		} catch( org.semanticweb.owlapi.reasoner.InconsistentOntologyException e){
			ontoRef.loggInconsistency();
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
			OWLDifferentIndividualsAxiom differentIndAxiom = ontoRef.getFactory().getOWLDifferentIndividualsAxiom( individuals);
			OWLOntologyChange adding = getRemoveAxiom( differentIndAxiom, manipulationBuffering);
			
			if( !manipulationBuffering)
				applyChanges( adding);
			logger.addDebugString( "make disjoint individuals: " + ontoRef.getOWLObjectName(individuals) + ". in:" + (System.nanoTime() - initialTime) + " [ns]");
			return( adding);
		} catch( org.semanticweb.owlapi.reasoner.InconsistentOntologyException e){
			ontoRef.loggInconsistency();
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
			OWLDisjointClassesAxiom differentIndAxiom = ontoRef.getFactory().getOWLDisjointClassesAxiom( classes);
			OWLOntologyChange adding = getAddAxiom( differentIndAxiom, manipulationBuffering);
			
			if( !manipulationBuffering)
				applyChanges( adding);
			logger.addDebugString( "remove disjoint class: " + ontoRef.getOWLObjectName(classes) + ". in:" + (System.nanoTime() - initialTime) + " [ns]");
			return( adding);
		} catch( org.semanticweb.owlapi.reasoner.InconsistentOntologyException e){
			ontoRef.loggInconsistency();
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
			OWLDisjointClassesAxiom differentIndAxiom = ontoRef.getFactory().getOWLDisjointClassesAxiom( classes);
			OWLOntologyChange adding = getRemoveAxiom( differentIndAxiom, manipulationBuffering);
			
			if( !manipulationBuffering)
				applyChanges( adding);
			logger.addDebugString( "remove disjoint class: " + ontoRef.getOWLObjectName(classes) + ". in:" + (System.nanoTime() - initialTime) + " [ns]");
			return( adding);
		} catch( org.semanticweb.owlapi.reasoner.InconsistentOntologyException e){
			ontoRef.loggInconsistency();
		}
		return( null);
	}


}
