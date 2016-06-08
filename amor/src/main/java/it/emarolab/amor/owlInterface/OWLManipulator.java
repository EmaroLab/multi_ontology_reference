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
import org.semanticweb.owlapi.model.OWLDataFactory;
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
 * This class implements the basic operations that can be done
 * in order to manipulate an ontology.
 * This class should not be used directly from an aMOR user
 * since it does not provide any thread saving mechanisms.
 * Use {@link OWLReferences} instead.<br>
 * 
 * This class is also used in order to implement an internal buffer of ontological changes
 * that can be applied to the ontology at every manipulation or at demand 
 * by using the {@link #applyChanges()} method and clean the buffer.<br>
 * 
 * Moreover, consider that this class uses the convention: {@literal B2} which stands
 * for {@link belong to}. Last but not the least, node that the methods for
 * manipulation can be used with the ontological entity simple name (without the complete semantic IRI). 
 * This allow to refer generically to an entity that, if it is already available in
 * the ontology, will be used. Otherwise a new entity with the specified 
 * features will be created. 
 * </p>
 * 
 * @see 
 *
 * 
 * @version 2.0
 */
public class OWLManipulator{

	/**
	 * This object is used to log informations about the instances of this class.
	 * The logs can be activated by setting the flag: {@link LoggerFlag#LOG_OWL_MANIPULATOR}
	 */
	private Logger logger = new Logger( this, LoggerFlag.getLogOWLManipulator());

	/**
	 * The default value of the {@link #changeBuffering} field.
	 * It is given when not specified in the constructor, namely in {@link #OWLManipulator(OWLReferencesInterface)}
	 */
	public static Boolean DEFAULT_CHANGE_BUFFERING = false;
	
	// [[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[   CONSTRUCTOR   ]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]
	/**
	 * fully initialise this class by initialise the value
	 * given from {@link #getLiOwlLibrary()} and by setting 
	 * the value of {@link #isChangeBuffering()}
	 * @param owlRef the references to the ontology to be manipulated.
	 * @param changeBuffering set to {@code false} to apply the changes automatically as soon as they
	 * are performed. Otherwise, set to {@code true} to buffering it into an internal list. In this last
	 * case in order to actually apply the changes into the ontology you should call manually {@link #applyChanges()}.
	 */
	protected OWLManipulator( OWLReferencesInterface owlRef, Boolean changeBuffering){
		this.ontoRef = owlRef;
		this.changeBuffering = changeBuffering;
	}
	/**
	 * fully initialise this class by initialise the value
	 * given from {@link #getLiOwlLibrary()} and by setting 
	 * the value of {@link #isChangeBuffering()} to {@code false}.
	 * @param owlRef the references to the ontology to be manipulated.
	 */
	protected OWLManipulator( OWLReferencesInterface owlRef){
		this.ontoRef = owlRef;
		this.changeBuffering = DEFAULT_CHANGE_BUFFERING;
	}

	// [[[[[[[[[[[[[[[[[[[[[[[[[[[[[   CHANGE BUFFERING FLAG MANAGMENT   ]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]
	/**
	 * The flag to decide if the changes should be applied into the ontology as soon as they are perfomed ({@code false});
	 * or if buffering them ({@code true}). In this last
	 * case in order to actually apply the changes into the ontology you should call manually {@link #applyChanges()}. 
	 */
	private Boolean changeBuffering;
	/**
	 * @return the changeBuffering flag to specify if the changes are applied into the ontology as soon as they are
	 * performed ({@code true}); or if buffering them ({@code true}). In this last
	 * case in order to actually apply the changes into the ontology you should call manually {@link #applyChanges()}.
	 */
	public synchronized Boolean isChangeBuffering() {
		return changeBuffering;
	}
	/**
	 * @param changeBuffering the changeBuffering to set in order to specify if the changes are applied into the ontology as soon as they are
	 * performed ({@code true}); or if buffering them ({@code true}). In this last
	 * case in order to actually apply the changes into the ontology you should call manually {@link #applyChanges()}.
	 */
	public synchronized void setChangeBuffering(Boolean changeBuffering) {
		this.changeBuffering = changeBuffering;
	}


	// [[[[[[[[[[[[[[[[[[[[[[[[[[[[   OWL REFERENCE POINTER MANGMENT  ]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]
	/**
	 * The basic Reference to an ontology.
	 */
	private OWLReferencesInterface ontoRef;
	/**
	 * @return a container of all the objects of the refereed ontology,
	 * set on constructor.
	 */
	protected OWLReferencesInterface getLiOwlLibrary(){
		return ontoRef;
	}


	// [[[[[[[[[[[[[[[[[[[[   METHODS TO COLLECT AND APPLY ONTOLOGY CHANGES   ]]]]]]]]]]]]]]]]]]]]]]]]]]
	// build into Ontology
	// if bufferize = true it save the axiom in the internal states and
	// apply the changes when is called {@link #applyChanges( OWLReferences)}.
	// if false it apply the changes immediately
	/**
	 * This is a vector of ontological changes applied by this class. 
	 * If the change are bufferising you can call {@link OWLManipulator#applyChanges(OWLReferences)} 
	 * in order to actually manipulate the ontology and clear this list.
	 * Otherwise, if the changes are not bufferising this is done automatically.
	 */
	private final List< OWLOntologyChange> changeList = new ArrayList< OWLOntologyChange>();

	/**
	 * It returns a list of ontology changes to be done to build a 
	 * given axiom into the ontology. Indeed it calls:
	 * {@link #getAddAxiom(OWLAxiom, boolean)} with the 
	 * flag value always set to {@link #changeBuffering}.
	 * @param axiom to describe relationships between ontological entities. 
	 * @return the ordered set of changes to be done in order to make the axiom verified in the ontology.
	 */
	public synchronized OWLOntologyChange getAddAxiom( OWLAxiom axiom){
		return( getAddAxiom( axiom, changeBuffering));
	}
	/**
	 * It returns a list of ontology changes to be done to build a 
	 * given axiom into the ontology. If the flag {@code addToChangeList} is {@code true}
	 * then those changes will be stored inside an internal buffer ({@link #changeList})
	 * and the returning value can be discarder. Otherwise, you need to manage
	 * the returning value manually.
	 * @param axiom to describe relationships between ontological entities.
	 * @param addToChangeList flag to decide if add them into the internal buffer of changes or not.
	 * @return the ordered set of changes to be done in order to make the axiom verified in the ontology.
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
	 * It returns a list of ontology changes to be done to remove a 
	 * given axiom from the ontology. Indeed it calls:
	 * {@link #getRemoveAxiom(OWLAxiom, boolean, OWLReferences)} with the 
	 * flag value always set to {@link #changeBuffering}.
	 * @param axiom to describe relationships between ontological entities. 
	 * @return the ordered set of changes to be done in order to make the axiom not verified in the ontology.
	 */
	public synchronized OWLOntologyChange getRemoveAxiom( OWLAxiom axiom){
		return( getRemoveAxiom( axiom, changeBuffering));
	}
	/**
	 * It returns a list of ontology changes to be done to remove a 
	 * given axiom from the ontology. If the flag {@code addToChangeList} is {@code true}
	 * then those changes will be stored inside an internal buffer ({@link #changeList})
	 * and the returning value can be discarder. Otherwise, you need to manage
	 * the returning value manually.
	 * @param axiom to describe relationships between ontological entities.
	 * @param addToChangeList flag to decide if add them into the internal buffer of changes or not.
	 * @return the order set of changes to remove a given axiom.
	 */
	public synchronized OWLOntologyChange getRemoveAxiom( OWLAxiom axiom, boolean addToChangeList){
		//OWLOntologyChange addAxiom = new AddAxiom( ontoRef.getOntology(), axiom);
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
	 * It applies all the changes (computed from axioms) stored in the internal buffer ({@link #changeList}) 
	 * into the ontology in order to actual perform structure manipulation. 
	 * After its work, this method will clean up this buffer.
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
	 * It applies, into the ontology, only the change given as input parameter.
	 * It basically does the same work as {@link #applyChanges()} but by using
	 * the input parameter instead of the internal buffer of changes.
	 * @param addAxiom a change to be applied in the ontology
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
	 * It applies, into the ontology, only the change given as input parameter.
	 * It basically does the same work as {@link #applyChanges()} but by using
	 * the input parameter instead of the internal buffer of changes.
	 * @param addAxiom a list of changes to be applied in the ontology
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
	 * Returns a list of changes to be applied into the ontology to
	 * add a new object property (with its value) into an individual.
	 * The changes provided by this call are applied immediately to 
	 * the ontology or buffered with respect to the value of {@link #isChangeBuffering()}.
	 * @param ind individual that have to have a new object property.
	 * @param prop object property to be added.
	 * @param value individual which is the value of the given object property.
	 * @return the changes to be done into the refereed ontology to add this specific object property. 
	 * You may want to do not consider the returning value and call {@link #applyChanges()} if this operation not buffered.
	 */
	public OWLOntologyChange addObjectPropertyB2Individual( OWLNamedIndividual ind, OWLObjectProperty prop, OWLNamedIndividual value){
		long initialTime = System.nanoTime();
		try{
			OWLAxiom propertyAssertion = ontoRef.getFactory().getOWLObjectPropertyAssertionAxiom( prop, ind, value);
			OWLOntologyChange add = getAddAxiom( propertyAssertion, changeBuffering);
			if( ! changeBuffering)
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
	 * Returns a list of changes to be applied into the ontology to
	 * add a new object property (with its value) into an individual.
	 * The changes provided by this call are applied immediately to 
	 * the ontology or buffered with respect to the value of {@link #isChangeBuffering()}.
	 * Indeed it retrieve the ontological object from name and then calls: 
	 * {@link #addObjectPropertyB2Individual(OWLNamedIndividual, OWLObjectProperty, OWLNamedIndividual)}
	 * @param individualName the name of an ontological individual that have to have a new object property
	 * @param propName name of the object property inside the ontology refereed by ontoRef.
	 * @param valueName individual name inside te refereed ontology to be the value of the given object property
	 * @return  the changes to be done into the refereed ontology to add this specific object property. 
	 * You may want to do not consider the returning value and call {@link #applyChanges()} if this operation not buffered.
	 */
	public OWLOntologyChange addObjectPropertyB2Individual( String individualName, String propName, String valueName){
		OWLNamedIndividual indiv = ontoRef.getOWLIndividual( individualName);
		OWLObjectProperty prop = ontoRef.getOWLObjectProperty( propName);
		OWLNamedIndividual val = ontoRef.getOWLIndividual( valueName);
		return( addObjectPropertyB2Individual( indiv, prop, val));
	}

	/**
	 * Returns a list of changes to be applied into the ontology to
	 * add a new data property (with its value) into an individual.
	 * The changes provided by this call are applied immediately to 
	 * the ontology or buffered with respect to the value of {@link #isChangeBuffering()}.
	 * @param ind individual that have to have a new data property.
	 * @param prop data property to be added.
	 * @param value literal which is the value of the given data property.
	 * @return  the changes to be done into the refereed ontology to add this specific data property. 
	 * You may want to do not consider the returning value and call {@link #applyChanges()} if this operation not buffered.
	 */
	public OWLOntologyChange addDataPropertyB2Individual(OWLNamedIndividual ind, OWLDataProperty prop, OWLLiteral value) {
		try{
			long initialTime = System.nanoTime();
			OWLAxiom newAxiom = ontoRef.getFactory().getOWLDataPropertyAssertionAxiom( prop, ind, value);
			OWLOntologyChange add = getAddAxiom( newAxiom, changeBuffering);
			if( ! changeBuffering)
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
	 * Returns a list of changes to be applied into the ontology to
	 * add a new data property (with its value) into an individual.
	 * The changes provided by this call are applied immediately to 
	 * the ontology or buffered with respect to the value of {@link #isChangeBuffering()}.
	 * Indeed it retrieve the ontological object from name and than it calls: 
	 * {@link #addDataPropertyB2Individual(OWLNamedIndividual, OWLDataProperty, OWLLiteral)}
	 * @param individualName the name of an ontological individual that have to have a new data property
	 * @param propertyName name of the data property inside the ontology refereed by ontoRef.
	 * @param value literal to be added as the value of a data property.
	 * @return  the changes to be done into the refereed ontology to add this specific data property. 
	 * You may want to do not consider the returning value and call {@link #applyChanges()} if this operation not buffered.
	 */
	public OWLOntologyChange addDataPropertyB2Individual( String individualName, String propertyName, Object value) {
		OWLNamedIndividual indiv = ontoRef.getOWLIndividual( individualName);
		OWLDataProperty prop = ontoRef.getOWLDataProperty( propertyName);
		OWLLiteral lit = ontoRef.getOWLLiteral( value, null);
		return( addDataPropertyB2Individual( indiv, prop, lit));
	}

	/**
	 * Returns the ontological changes to be applied to put an 
	 * individual inside an ontological class.
	 * The changes provided by this call are applied immediately to 
	 * the ontology or buffered with respect to the value of {@link #isChangeBuffering()}.
	 * @param ind individual to add into an ontological class
	 * @param cls ontological class that than will contend this individual
	 * @return  the changes to be done into the refereed ontology to add an individual to be belonging to a specific class. 
	 * You may want to do not consider the returning value and call {@link #applyChanges()} if this operation not buffered.
	 */
	public OWLOntologyChange addIndividualB2Class(OWLNamedIndividual ind, OWLClass cls) {
		long initialTime = System.nanoTime();
		try{
			OWLAxiom newAxiom = ontoRef.getFactory().getOWLClassAssertionAxiom( cls, ind);
			OWLOntologyChange add = getAddAxiom( newAxiom, changeBuffering);
			if( ! changeBuffering)
				applyChanges(add);
			logger.addDebugString( "add individual (" + ontoRef.getOWLObjectName( ind) + ") belong to class (" + ontoRef.getOWLObjectName( cls) + ") in: " + (System.nanoTime() - initialTime) + " [ns]");
			return( add);
		} catch( org.semanticweb.owlapi.reasoner.InconsistentOntologyException e){
			ontoRef.loggInconsistency();
			return( null);
		}

	}
	/**
	 * Returns a list of changes to be applied into the ontology to
	 * set an individual to belonging to a class.
	 * The changes provided by this call are applied immediately to 
	 * the ontology or buffered with respect to the value of {@link #isChangeBuffering()}.
	 * Indeed it retrieve the ontological object from name inside the referring ontology
	 * and than it calls: {@link #addIndividualB2Class(OWLNamedIndividual, OWLClass)}
	 * @param individualName the name of an ontological individual that have to be belonging to a given class.
	 * @param className the name of an ontological class that will contains the input individual parameter.
	 * @param bufferize flag to buffering changes internally to this class.
	 * @param buffering flag to buffering changes inside an internal buffer or apply them directly (by using {@code false}).
	 * @return  the changes to be done into the refereed ontology to add an individual to be belonging to a specific class. 
	 * You may want to do not consider the returning value and call {@link #applyChanges()} if this operation not buffered.
	 */
	public OWLOntologyChange addIndividualB2Class(String individualName, String className) {
		OWLNamedIndividual indiv = ontoRef.getOWLIndividual( individualName);
		OWLClass cl = ontoRef.getOWLClass( className);
		return( addIndividualB2Class( indiv, cl));
	}

	/**
	 * Returns the ontological changes to be applied to put an 
	 * individual inside the ontology (as a child of the top class {@code OWLThing}).
	 * The changes provided by this call are applied immediately to 
	 * the ontology or buffered with respect to the value of {@link #isChangeBuffering()}.
	 * Indeed it calls {@link #addIndividual(OWLNamedIndividual)} where the input parameter
	 * is given from the result of the method {@link OWLLibrary#getOWLIndividual(String)}
	 * @param individualName the name of the individual to be added into the ontology.
	 * @return  the changes to be done into the refereed ontology to add an individual. 
	 * You may want to do not consider the returning value and call {@link #applyChanges()} if this operation not buffered.
	 */
	public OWLOntologyChange addIndividual( String individualName){
		return addIndividual( ontoRef.getOWLIndividual(individualName));
	}
	/**
	 * Returns the ontological changes to be applied to put an 
	 * individual inside the ontology (as a child of the top class {@code OWLThing}).
	 * The changes provided by this call are applied immediately to 
	 * the ontology or buffered with respect to the value of {@link #isChangeBuffering()}.
	 * Indeed it calls {@link #addIndividualB2Class(OWLNamedIndividual, OWLClass)} where the input parameter
	 * Referring to the class is given from the result of the method {@link OWLDataFactory#getOWLThing()}
	 * @param ind the individual to be added into the ontology.
	 * @return  the changes to be done into the refereed ontology to add an individual. 
	 * You may want to do not consider the returning value and call {@link #applyChanges()} if this operation not buffered.
	 */
	public OWLOntologyChange addIndividual( OWLNamedIndividual ind){
		OWLClass things = ontoRef.getFactory().getOWLThing();
		return addIndividualB2Class( ind, things);		
	}

	/**
	 * Returns the ontological changes to be applied to set 
	 * the super class of another class.
	 * The changes provided by this call are applied immediately to 
	 * the ontology or buffered with respect to the value of {@link #isChangeBuffering()}.
	 * @param superClassName the name of the ontological super class
	 * @param subClassName the name of the ontological sub class
	 * @return  the changes to be done into the refereed ontology to add a class as a sub class of another. 
	 * You may want to do not consider the returning value and call {@link #applyChanges()} if this operation not buffered.
	 */
	public OWLOntologyChange addSubClassOf( OWLClass superClass, OWLClass subClass){
		try{
			long initialTime = System.nanoTime();
			OWLSubClassOfAxiom subClAxiom = ontoRef.getFactory().getOWLSubClassOfAxiom( subClass, superClass);
			OWLOntologyChange adding = getAddAxiom( subClAxiom, changeBuffering);
			if( ! changeBuffering)
				applyChanges( adding);
			logger.addDebugString( "set sub class (" + ontoRef.getOWLObjectName( subClass) + ") of super class (" + ontoRef.getOWLObjectName( superClass) + ") in: " + (System.nanoTime() - initialTime) + " [ns]");
			return( adding);
		} catch( org.semanticweb.owlapi.reasoner.InconsistentOntologyException e){
			ontoRef.loggInconsistency();
			return( null);
		}
	}
	/**
	 * Returns the ontological changes to be applied to set 
	 * the super class of another class.
	 * The changes provided by this call are applied immediately to 
	 * the ontology or buffered with respect to the value of {@link #isChangeBuffering()}.
	 * Indeed it retrieve the ontological object from name inside the referring ontology
	 * and than it calls: {@link #addSubClassOf(OWLClass, OWLClass)}
	 * @param superClassName the name of the ontological super class
	 * @param subClassName the name of the ontological sub class
	 * @return  the changes to be done into the refereed ontology to add a class as a sub class of another. 
	 * You may want to do not consider the returning value and call {@link #applyChanges()} if this operation not buffered.
	 */
	public OWLOntologyChange addSubClassOf( String superClassName, String subClassName){
		OWLClass sup = ontoRef.getOWLClass( superClassName);
		OWLClass sub = ontoRef.getOWLClass( subClassName);
		return( addSubClassOf( sup, sub));
	}

	/**
	 * Returns the ontological changes to be applied in order to add a class
	 * to the ontology.
	 * The changes provided by this call are applied immediately to 
	 * the ontology or buffered with respect to the value of {@link #isChangeBuffering()}.
	 * Indeed it sets the input class to be a sub class of {@code OWLThink} by
	 * using {@link #addSubClassOf(OWLClass, OWLClass)}.
	 * @param cls the class to be added to the ontology.
	 * @return  the changes to be done into the refereed ontology to add a new class. 
	 * You may want to do not consider the returning value and call {@link #applyChanges()} if this operation not buffered.
	 */
	public OWLOntologyChange addClass( OWLClass cls){
		OWLClass think = ontoRef.getFactory().getOWLThing();
		return addSubClassOf(think, cls);
	}
	/**
	 * Returns the ontological changes to be applied in order to add a class
	 * to the ontology.
	 * The changes provided by this call are applied immediately to 
	 * the ontology or buffered with respect to the value of {@link #isChangeBuffering()}.
	 * Indeed it sets the input class to be a sub class of {@code OWLThink} by
	 * using {@link #addSubClassOf(OWLClass, OWLClass)}.
	 * @param className the name of the class to be added to the ontology.
	 * @return  the changes to be done into the refereed ontology to add a new class. 
	 * You may want to do not consider the returning value and call {@link #applyChanges()} if this operation not buffered.
	 */
	public OWLOntologyChange addClass( String className){
		return addClass( ontoRef.getOWLClass( className));
	}

	
	
	// ---------------------------   methods for remove entities to the ontology
	/**
	 * Returns a list of changes to be applied into the ontology to
	 * remove an object property (with its value) from an individual.
	 * The changes provided by this call are applied immediately to 
	 * the ontology or buffered with respect to the value of {@link #isChangeBuffering()}.
	 * @param ind individual from which remove a given object property.
	 * @param prop object property to be removed.
	 * @param value individual which is the value of the given object property.
	 * @return the changes to be done into the refereed ontology to remove an object property from an individual. 
	 * You may want to do not consider the returning value and call {@link #applyChanges()} if this operation not buffered.
	 */
	public OWLOntologyChange removeObjectPropertyB2Individual( OWLNamedIndividual ind, OWLObjectProperty prop, OWLNamedIndividual value){
		try{
			long initialTime = System.nanoTime();
			OWLAxiom propertyAssertion = ontoRef.getFactory().getOWLObjectPropertyAssertionAxiom( prop, ind, value);
			OWLOntologyChange remove = getRemoveAxiom( propertyAssertion, changeBuffering);
			if( ! changeBuffering)
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
	 * Returns a list of changes to be applied into the ontology to
	 * remove a given object property (with its value) from an individual.
	 * The changes provided by this call are applied immediately to 
	 * the ontology or buffered with respect to the value of {@link #isChangeBuffering()}. 
	 * Indeed it retrieve the ontological object from name and than it calls: 
	 * {@link #removeObjectPropertyB2Individual(OWLNamedIndividual, OWLObjectProperty, OWLNamedIndividual)}
	 * @param individualName the name of an ontological individual from which remove the object property
	 * @param propName name of the object property inside the ontology refereed by ontoRef.
	 * @param valueName individual name inside the refereed ontology to be the value of the given object property
	 * @return the changes to be done into the refereed ontology to remove an object property from an individual. 
	 * You may want to do not consider the returning value and call {@link #applyChanges()} if this operation not buffered.
	 */
	public OWLOntologyChange removeObjectPropertyB2Individual( String individualName, String propName, String valueName){
		OWLNamedIndividual indiv = ontoRef.getOWLIndividual( individualName);
		OWLObjectProperty prop = ontoRef.getOWLObjectProperty( propName);
		OWLNamedIndividual val = ontoRef.getOWLIndividual( valueName);
		return( removeObjectPropertyB2Individual( indiv, prop, val));
	}

	/**
	 * Returns a list of changes to be applied into the ontology to
	 * remove a data property (with its value) from an individual.
	 * The changes provided by this call are applied immediately to 
	 * the ontology or buffered with respect to the value of {@link #isChangeBuffering()}. 
	 * @param ind individual from which remove the given data property.
	 * @param prop data property to be removed.
	 * @param value literal which is the value of the given data property.
	 * @return the changes to be done into the refereed ontology to remove this specific data property from an individual. 
	 * You may want to do not consider the returning value and call {@link #applyChanges()} if this operation not buffered.
	 */
	public OWLOntologyChange removeDataPropertyB2Individual(OWLNamedIndividual ind, OWLDataProperty prop, OWLLiteral value) {
		long initialTime = System.nanoTime();
		try{
			OWLAxiom newAxiom = ontoRef.getFactory().getOWLDataPropertyAssertionAxiom( prop, ind, value);
			OWLOntologyChange remove = getRemoveAxiom( newAxiom, changeBuffering);
			if( ! changeBuffering)
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
	 * Returns a list of changes to be applied into the ontology to
	 * remove a data property (with its value) from an individual.
	 * The changes provided by this call are applied immediately to 
	 * the ontology or buffered with respect to the value of {@link #isChangeBuffering()}.
	 * Indeed it retrieve the ontological object from name and than it calls: 
	 * {@link #removeDataPropertyB2Individual(OWLNamedIndividual, OWLDataProperty, OWLLiteral)}
	 * @param individualName the name of an ontological individual from which remove the data property
	 * @param propertyName name of the data property inside the ontology refereed by ontoRef.
	 * @param value literal to be removed as the value of a data property.
	 * @return the changes to be done into the refereed ontology to remove this specific data property from an individual. 
	 * You may want to do not consider the returning value and call {@link #applyChanges()} if this operation not buffered.
	 */
	public OWLOntologyChange removeDataPropertyB2Individual( String individualName, String propertyName, Object value) {
		OWLNamedIndividual indiv = ontoRef.getOWLIndividual( individualName);
		OWLDataProperty prop = ontoRef.getOWLDataProperty( propertyName);
		OWLLiteral lit = ontoRef.getOWLLiteral( value, null);
		return( removeDataPropertyB2Individual( indiv, prop, lit));
	}

	/**
	 * Returns the ontological changes to be applied to remove an 
	 * individual from an ontological class.
	 * The changes provided by this call are applied immediately to 
	 * the ontology or buffered with respect to the value of {@link #isChangeBuffering()}.
	 * @param ind individual to remove from an ontological class
	 * @param cls ontological class that than was contend this individual
	 * @return the changes to be done into the refereed ontology to set an individual to not be anymore belonging to a specific class. 
	 * You may want to do not consider the returning value and call {@link #applyChanges()} if this operation not buffered.
	 */
	public OWLOntologyChange removeIndividualB2Class(OWLNamedIndividual ind, OWLClass cls) {
		long initialTime = System.nanoTime();
		try{
			OWLAxiom newAxiom = ontoRef.getFactory().getOWLClassAssertionAxiom( cls, ind);
			OWLOntologyChange remove = getRemoveAxiom( newAxiom, changeBuffering);
			if( ! changeBuffering)
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
	 * Returns a list of changes to be applied into the ontology to
	 * remove an individual to belonging to a class.
	 * The changes provided by this call are applied immediately to 
	 * the ontology or buffered with respect to the value of {@link #isChangeBuffering()}.
	 * Indeed it retrieve the ontological object from name inside the referring ontology
	 * and than it calls: 
	 * {@link #removeIndividualB2Class(OWLNamedIndividual, OWLClass)}
	 * @param individualName the name of an ontological individual that have not to be belonging to a given class.
	 * @param className the name of an ontological class that will no more contains the input individual parameter.
	 * @return the changes to be done into the refereed ontology to set an individual to do not belong to a class anymore. 
	 * You may want to do not consider the returning value and call {@link #applyChanges()} if this operation not buffered.
	 */
	public OWLOntologyChange removeIndividualB2Class(String individualName, String className) {
		OWLNamedIndividual indiv = ontoRef.getOWLIndividual( individualName);
		OWLClass cl = ontoRef.getOWLClass( className);
		return( removeIndividualB2Class( indiv, cl));
	}

	/**
	 * Returns the changes to be applied into the referring ontology for 
	 * removing an individual.
	 * The changes provided by this call are applied immediately to 
	 * the ontology or buffered with respect to the value of {@link #isChangeBuffering()}.
	 * @param individual to be removed from the ontology.
	 * @return the changes to be done into the refereed ontology to remove the given individual. 
	 * You may want to do not consider the returning value and call {@link #applyChanges()} if this operation not buffered.
	 */
	public List<OWLOntologyChange> removeIndividual( OWLNamedIndividual individual){
		OWLEntityRemover remover = new OWLEntityRemover( ontoRef.getManager(), Collections.singleton( ontoRef.getOntology()));
		individual.accept(remover);
		long initialTime = System.nanoTime();
		List<OWLOntologyChange> remove = remover.getChanges();
		if( ! changeBuffering)
			applyChanges(remove);
		else changeList.addAll( remove);
		logger.addDebugString( "remove individual (" + ontoRef.getOWLObjectName( individual) + ") in: " + (System.nanoTime() - initialTime) + " [ns]");
		return( remove);
	}
	/**
	 * Returns the changes to be applied into the referring ontology for removing
	 * a set of individuals.
	 * The changes provided by this call are applied immediately to 
	 * the ontology or buffered with respect to the value of {@link #isChangeBuffering()}.
	 * Indeed for all individuals in the set it calls {@link #removeIndividual(OWLNamedIndividual)}.
	 * @param individuals set of individuals to be removed.
	 * @return the changes to be done into the refereed ontology to remove the given set of individuals. 
	 * You may want to do not consider the returning value and call {@link #applyChanges()} if this operation not buffered.
	 */
	public List<OWLOntologyChange> removeIndividual( Set< OWLNamedIndividual> individuals){
		List<OWLOntologyChange> changes = new ArrayList<OWLOntologyChange>();
		for( OWLNamedIndividual i : individuals)
			changes.addAll( removeIndividual( i));
		return( changes);
	}
	/**
	 * Returns the changes to be applied into the referring ontology for 
	 * removing an individual.
	 * The changes provided by this call are applied immediately to 
	 * the ontology or buffered with respect to the value of {@link #isChangeBuffering()}.
	 * @param indName the name of the individual to be removed from the ontology.
	 * @return the changes to be done into the refereed ontology to remove the given individual. 
	 * You may want to do not consider the returning value and call {@link #applyChanges()} if this operation not buffered.
	 */
	public List<OWLOntologyChange> removeIndividual( String indName){
		return removeIndividual( ontoRef.getOWLIndividual( indName));
	}

	/**
	 * Returns the ontological changes to be applied to remove the asserting 
	 * of a class to be a sub class of another class.
	 * The changes provided by this call are applied immediately to 
	 * the ontology or buffered with respect to the value of {@link #isChangeBuffering()}.
	 * @param superClassName the name of the ontological super class
	 * @param subClassName the name of the ontological sub class in which remove the sub classing asserting.
	 * @return  the changes to be done into the refereed ontology to remove the sub classing assertion within the two inputs. 
	 * You may want to do not consider the returning value and call {@link #applyChanges()} if this operation not buffered.
	 */
	public OWLOntologyChange removeSubClassOf( OWLClass superClass, OWLClass subClass){
		try{
			long initialTime = System.nanoTime();
			OWLSubClassOfAxiom subClAxiom = ontoRef.getFactory().getOWLSubClassOfAxiom( subClass, superClass);
			OWLOntologyChange adding = getRemoveAxiom( subClAxiom, changeBuffering);

			if( ! changeBuffering)
				applyChanges( adding);
			logger.addDebugString( "remove sub class (" + ontoRef.getOWLObjectName( subClass) + ") of super class (" + ontoRef.getOWLObjectName( superClass) + ") in: " + (System.nanoTime() - initialTime) + " [ns]");
			return( adding);
		} catch( org.semanticweb.owlapi.reasoner.InconsistentOntologyException e){
			ontoRef.loggInconsistency();
		}
		return( null);
	}
	/**
	 * Returns the ontological changes to be applied to remove the asserting 
	 * of a class to be a sub class of another class.
	 * The changes provided by this call are applied immediately to 
	 * the ontology or buffered with respect to the value of {@link #isChangeBuffering()}.
	 * Indeed it retrieve the ontological object from name inside the referring ontology
	 * and than it calls: {@link #removeSubClassOf(OWLClass, OWLClass)}
	 * @param superClass of the ontological super class
	 * @param subClass the ontological sub class in which remove the sub classing asserting.
	 * @return the changes to be done into the refereed ontology to remove the sub classing assertion within the two inputs. 
	 * You may want to do not consider the returning value and call {@link #applyChanges()} if this operation not buffered.
	 */
	public OWLOntologyChange removeSubClassOf( String superClassName, String subClassName){
		OWLClass sup = ontoRef.getOWLClass( superClassName);
		OWLClass sub = ontoRef.getOWLClass( subClassName);
		return( removeSubClassOf( sup, sub));
	}

	/**
	 * Returns the ontological changes to be applied in order to remove a class
	 * to the ontology.
	 * The changes provided by this call are applied immediately to 
	 * the ontology or buffered with respect to the value of {@link #isChangeBuffering()}.
	 * Indeed it sets the input class to be a sub class of {@code OWLThink} by
	 * using {@link #addSubClassOf(OWLClass, OWLClass)}.
	 * @param cls the class to be added to the ontology.
	 * @return  the changes to be done into the refereed ontology to remove the given class. 
	 * You may want to do not consider the returning value and call {@link #applyChanges()} if this operation not buffered.
	 */
	public OWLOntologyChange removeClass( OWLClass cls){
		try{
			long initialTime = System.nanoTime();
			OWLClass think = ontoRef.getFactory().getOWLThing();
			OWLSubClassOfAxiom subClAxiom = ontoRef.getFactory().getOWLSubClassOfAxiom( cls, think);
			OWLOntologyChange remove = getRemoveAxiom( subClAxiom, changeBuffering);
			if( ! changeBuffering)
				applyChanges( remove);
			logger.addDebugString( "remove sub class (" + ontoRef.getOWLObjectName( cls) + ") in: " + (System.nanoTime() - initialTime) + " [ns]");
			return( remove);
		} catch( org.semanticweb.owlapi.reasoner.InconsistentOntologyException e){
			ontoRef.loggInconsistency();
			return( null);
		}
	}
	/**
	 * Returns the ontological changes to be applied in order to remove a class
	 * to the ontology.
	 * The changes provided by this call are applied immediately to 
	 * the ontology or buffered with respect to the value of {@link #isChangeBuffering()}.
	 * Indeed it sets the input class to be a sub class of {@code OWLThink} by
	 * using {@link #addSubClassOf(OWLClass, OWLClass)}.
	 * @param className the name of the class to be added to the ontology.
	 * @return  the changes to be done into the refereed ontology to remove the given class. 
	 * You may want to do not consider the returning value and call {@link #applyChanges()} if this operation not buffered.
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
	 * You may want to do not consider the returning value and call {@link #applyChanges()} if this operation not buffered.
	 
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
	 * Atomically (with respect to reasoner update) replacing of a data property.
	 * Indeed, it will remove the possible data property with a given value
	 * using {@link #removeDataPropertyB2Individual(OWLNamedIndividual, OWLDataProperty, OWLLiteral)}.
	 * Than, it add the new value calling {@link #addDataPropertyB2Individual(OWLNamedIndividual, OWLDataProperty, OWLLiteral)}.
	 * Refer to those methods and to {@link #isChangeBuffering()} for more information.
	 * @param ind individual for which a data property will be replaced.
	 * @param prop property to replace
	 * @param oldValue value to remove
	 * @param newValue new value to add
	 * @return  the changes to be done into the refereed ontology to replace the value of a data property value attached into an individual. 
	 * You may want to do not consider the returning value and call {@link #applyChanges()} if this operation not buffered.
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
	 * Atomically (with respect to reasoner update) replacing of a object property.
	 * Indeed, it will remove the possible object property with a given values
	 * using {@link #removeObjectPropertyB2Individual(OWLNamedIndividual, OWLObjectProperty, OWLNamedIndividual)}.
	 * Than, it add the new value calling {@link #addObjectPropertyB2Individual(OWLNamedIndividual, OWLObjectProperty, OWLNamedIndividual)}.
	 * Refer to those methods and to {@link #isChangeBuffering()} for more information.
	 * @param ind individual for which a object property will be replaced.
	 * @param prop property to replace
	 * @param oldValue set of old values to remove
	 * @param newValue new value to add
	 * @return  the changes to be done into the refereed ontology to replace the value of an object property value attached into an individual. 
	 * You may want to do not consider the returning value and call {@link #applyChanges()} if this operation not buffered.
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
	 * Atomically (with respect to reasoner update) replacing of individual
	 * type. Which means to remove an individual from a class and add it to
	 * belong to another class.
	 * Indeed, it will remove the possible type with a given values
	 * using {@link #removeIndividualB2Class(OWLNamedIndividual, OWLClass)}.
	 * Than, it add the new value calling {@link #addIndividualB2Class(OWLNamedIndividual, OWLClass)}.
	 * Refer to those methods and to {@link #isChangeBuffering()} for more information.
	 * @param ind individual to change its classification.
	 * @param oldValue old class in which the individual is belonging to
	 * @param newValue new class in which the individual will belonging to
	 * @return  the changes to be done into the refereed ontology to replace the value of an object property value attached into an individual. 
	 * You may want to do not consider the returning value and call {@link #applyChanges()} if this operation not buffered.
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
	 * It returns the changes that must be done into the ontology to rename 
	 * an entity. The changes provided by this call are applied immediately to 
	 * the ontology or buffered with respect to the value of {@link #isChangeBuffering()}.
	 * @param entity ontological object to rename
	 * @param newIRI new name as ontological IRI path
	 * @return the changes to be applied into the ontology to rename an entity with a new IRI.
	 * You may want to do not consider the returning value and call {@link #applyChanges()} if this operation not buffered.
	 */
	public List<OWLOntologyChange> renameEntity( OWLEntity entity, IRI newIRI){
		long initialTime = System.nanoTime();
		String oldName = ontoRef.getOWLObjectName( entity);
		OWLEntityRenamer renamer = new OWLEntityRenamer( ontoRef.getManager(), ontoRef.getManager().getOntologies());
		List<OWLOntologyChange> changes = renamer.changeIRI( entity, newIRI);
		if( ! changeBuffering)
			applyChanges( changes);
		else changeList.addAll( changes);
		logger.addDebugString( "rename entity from (" + oldName + ") to (" + ontoRef.getOWLObjectName( newIRI) + ") in: " + (System.nanoTime() - initialTime) + " [ns]");
		return( changes);
	}
	/**
	 * It returns the changes that must be done into the ontology to rename 
	 * an entity. The changes provided by this call are applied immediately to 
	 * the ontology or buffered with respect to the value of {@link #isChangeBuffering()}.
	 * @param entity ontological object to rename
	 * @param newName the new name as ontological IRI path
	 * @return the changes to be applied into the ontology to rename an entity with a new IRI.
	 * You may want to do not consider the returning value and call {@link #applyChanges()} if this operation not buffered.
	 */
	public List<OWLOntologyChange> renameEntity( OWLEntity entity, String newName){
		long initialTime = System.nanoTime();
		String oldName = ontoRef.getOWLObjectName( entity);
		OWLEntityRenamer renamer = new OWLEntityRenamer( ontoRef.getManager(), ontoRef.getManager().getOntologies());
		IRI newIRI = IRI.create( ontoRef.getIriOntologyPath() + "#" + newName);
		List<OWLOntologyChange> changes = renamer.changeIRI( entity, newIRI);
		if( ! changeBuffering)
			applyChanges( changes);
		else changeList.addAll( changes);
		logger.addDebugString( "rename entity from (" + oldName + ") to (" + newName + ") in: " + (System.nanoTime() - initialTime) + " [ns]");
		return( changes);
	}


	// ---------------------------   methods for make disjointed individual and class
	/**
	 * returns the changes to be done in the ontology to make the input individuals disjointed.
	 * Check the {@link #applyChanges()} mechanism to apply this changes automatically and do not
	 * manipulate the returning value.<br>
	 * Indeed this method calls {@link #makeDisjointIndividuals(Set)}.
	 * @param individualNames the names of the individuals to make disjointed.
	 * @return the change to make in the ontology to apply the disjointed axiom.
	 */
	public OWLOntologyChange makeDisjointIndividualName( Set< String> individualNames){
		Set< OWLNamedIndividual> inds = new HashSet< OWLNamedIndividual>();
		for( String i : individualNames)
			inds.add( ontoRef.getOWLIndividual( i));
		return makeDisjointIndividuals( inds);
	}
	/**
	 * returns the changes to be done in the ontology to make the input individuals disjointed.
	 * Check the {@link #applyChanges()} mechanism to apply this changes automatically and do not
	 * manipulate the returning value.<br>
	 * @param individuals the set of individuals to make disjointed
	 * @return the change to make in the ontology to apply the disjointed axiom. 
	 */
	public OWLOntologyChange makeDisjointIndividuals( Set< OWLNamedIndividual> individuals){
		try{
			long initialTime = System.nanoTime();
			OWLDifferentIndividualsAxiom differentIndAxiom = ontoRef.getFactory().getOWLDifferentIndividualsAxiom( individuals);
			OWLOntologyChange adding = getAddAxiom( differentIndAxiom, changeBuffering);
			
			if( ! changeBuffering)
				applyChanges( adding);
			logger.addDebugString( "make disjoint individuals: " + ontoRef.getOWLObjectName(individuals) + ". in:" + (System.nanoTime() - initialTime) + " [ns]");
			return( adding);
		} catch( org.semanticweb.owlapi.reasoner.InconsistentOntologyException e){
			ontoRef.loggInconsistency();
		}
		return( null);
	}
	
	/**
	 * returns the changes to be done in the ontology to remove the eventual fact that the given individuals are disjointed.
	 * Check the {@link #applyChanges()} mechanism to apply this changes automatically and do not
	 * manipulate the returning value.<br>
	 * Indeed this method calls {@link #removeDisjointIndividuals(Set)}.
	 * @param individualNames the names of the individuals to make not disjointed anymore.
	 * @return the change to make in the ontology to remove disjointed axiom.
	 */
	public OWLOntologyChange removeDisjointIndividualName( Set< String> individualNames){
		Set< OWLNamedIndividual> inds = new HashSet< OWLNamedIndividual>();
		for( String i : individualNames)
			inds.add( ontoRef.getOWLIndividual( i));
		return removeDisjointIndividuals( inds);
	}
	/**
	 * returns the changes to be done in the ontology to remove the eventual fact that the given individuals are disjointed.
	 * Check the {@link #applyChanges()} mechanism to apply this changes automatically and do not
	 * manipulate the returning value.<br>
	 * @param individuals the set of individuals to make not disjointed anymore.
	 * @return the change to make in the ontology to remove disjointed axiom. 
	 */
	public OWLOntologyChange removeDisjointIndividuals( Set< OWLNamedIndividual> individuals){
		try{
			long initialTime = System.nanoTime();
			OWLDifferentIndividualsAxiom differentIndAxiom = ontoRef.getFactory().getOWLDifferentIndividualsAxiom( individuals);
			OWLOntologyChange adding = getRemoveAxiom( differentIndAxiom, changeBuffering);
			
			if( ! changeBuffering)
				applyChanges( adding);
			logger.addDebugString( "make disjoint individuals: " + ontoRef.getOWLObjectName(individuals) + ". in:" + (System.nanoTime() - initialTime) + " [ns]");
			return( adding);
		} catch( org.semanticweb.owlapi.reasoner.InconsistentOntologyException e){
			ontoRef.loggInconsistency();
		}
		return( null);
	}

	/**
	 * returns the changes to be done in the ontology to make the input classes disjointed.
	 * Check the {@link #applyChanges()} mechanism to apply this changes automatically and do not
	 * manipulate the returning value.<br>
	 * Indeed this method calls {@link #makeDisjointClasses(Set)}.
	 * @param classesName the names of the classes to make disjointed.
	 * @return the change to make in the ontology to apply the disjointed axiom.
	 */
	public OWLOntologyChange makeDisjointClassName( Set< String> classesName){
		Set< OWLClass> inds = new HashSet< OWLClass>();
		for( String i : classesName)
			inds.add( ontoRef.getOWLClass( i));
		return makeDisjointClasses( inds);
	}
	/**
	 * returns the changes to be done in the ontology to make the input classes disjointed.
	 * Check the {@link #applyChanges()} mechanism to apply this changes automatically and do not
	 * manipulate the returning value.<br>
	 * @param classes the set of class to make disjointed
	 * @return the change to make in the ontology to apply the disjointed axiom. 
	 */
	public OWLOntologyChange makeDisjointClasses( Set< OWLClass> classes){
		try{
			long initialTime = System.nanoTime();
			OWLDisjointClassesAxiom differentIndAxiom = ontoRef.getFactory().getOWLDisjointClassesAxiom( classes);
			OWLOntologyChange adding = getAddAxiom( differentIndAxiom, changeBuffering);
			
			if( ! changeBuffering)
				applyChanges( adding);
			logger.addDebugString( "remove disjoint class: " + ontoRef.getOWLObjectName(classes) + ". in:" + (System.nanoTime() - initialTime) + " [ns]");
			return( adding);
		} catch( org.semanticweb.owlapi.reasoner.InconsistentOntologyException e){
			ontoRef.loggInconsistency();
		}
		return( null);
	}
	
	/**
	 * returns the changes to be done in the ontology to remove the eventual fact that the given classes are disjointed.
	 * Check the {@link #applyChanges()} mechanism to apply this changes automatically and do not
	 * manipulate the returning value.<br>
	 * Indeed this method calls {@link #removeDisjointClasses(Set)}.
	 * @param classesName the names of the classes to make not disjointed anymore.
	 * @return the change to make in the ontology to remove disjointed axiom.
	 */
	public OWLOntologyChange removeDisjointClassName( Set< String> classesName){
		Set< OWLClass> inds = new HashSet< OWLClass>();
		for( String i : classesName)
			inds.add( ontoRef.getOWLClass( i));
		return removeDisjointClasses( inds);
	}
	/**
	 * returns the changes to be done in the ontology to remove the eventual fact that the given classes are disjointed.
	 * Check the {@link #applyChanges()} mechanism to apply this changes automatically and do not
	 * manipulate the returning value.<br>
	 * @param classes the set of classes to make not disjointed anymore.
	 * @return the change to make in the ontology to remove disjointed axiom. 
	 */
	public OWLOntologyChange removeDisjointClasses( Set< OWLClass> classes){
		try{
			long initialTime = System.nanoTime();
			OWLDisjointClassesAxiom differentIndAxiom = ontoRef.getFactory().getOWLDisjointClassesAxiom( classes);
			OWLOntologyChange adding = getRemoveAxiom( differentIndAxiom, changeBuffering);
			
			if( ! changeBuffering)
				applyChanges( adding);
			logger.addDebugString( "remove disjoint class: " + ontoRef.getOWLObjectName(classes) + ". in:" + (System.nanoTime() - initialTime) + " [ns]");
			return( adding);
		} catch( org.semanticweb.owlapi.reasoner.InconsistentOntologyException e){
			ontoRef.loggInconsistency();
		}
		return( null);
	}


}
