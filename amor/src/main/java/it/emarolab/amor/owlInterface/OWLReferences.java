package it.emarolab.amor.owlInterface;

import java.util.List;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLOntologyChange;

import it.emarolab.amor.owlDebugger.Logger;
import it.emarolab.amor.owlDebugger.Logger.LoggerFlag;
import it.emarolab.amor.owlInterface.OWLEnquirer.DataPropertyRelatios;
import it.emarolab.amor.owlInterface.OWLEnquirer.ObjectPropertyRelatios;

import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLLiteral;

// TODO : how it is possible to serialise other things
// TODO : how to add more reasoners
// TODO : add super class manipulator interface
// TODO : better logging
// TODO : make an abstract class interface to be implemented for all methods (all have the same shape)
// TODO : replace with string as input parameter

public class OWLReferences extends OWLReferencesInterface{

	/**
	 * This object is used to log information about the instances of this class.
	 * The logs can be activated by setting the flag: {@link LoggerFlag#LOG_ONTOLOGY_REFERENCE}
	 */
	private Logger logger = new Logger( this, LoggerFlag.getLogOntologyReference());

	//  [[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[ SUPER CLASS CONSTRUCORS ]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]
	/**
	 * This constructor just calls the super class constructor: {@link OWLReferencesInterface#OWLReferencesInterface(String, String, String, Boolean, Integer)}
	 * @param referenceName the unique identifier of this ontology references. This is the key with which this instance
	 * is stored in the system map {@link OWLReferencesContainer#allReferences}
	 * @param filePath the file path (or URL) to the ontology.
	 * @param ontologyPath the IRI path of the ontology. 
	 * @param bufferingReasoner {@code true} if the reasoner have to evaluate changes to the ontology as soon as their have been performed.
	 * {@code false} if the reasoner should evaluate all the changes of the ontology only if the method {@link synchroniseReasoner} gets called. 
	 * @param command specifying if the ontology should be created, loaded from file or from web. Possible value of {@code commands} are:
	 * {@link OWLReferencesContainer#COMMAND_CREATE}, {@link OWLReferencesContainer#COMMAND_LOAD_FILE} or 
	 * {@link OWLReferencesContainer#COMMAND_LOAD_WEB}.
	 */
	protected OWLReferences(String referenceName, String filePath, String ontologyPath, Boolean bufferingReasoner, Integer command) {
		super(referenceName, filePath, ontologyPath, bufferingReasoner, command);
	}

	/**
	 * This constructor just call the super class constructor: {@link OWLReferencesInterface#OWLReferencesInterface(String, String, String, String, Boolean, Integer)}
	 * @param referenceName the unique identifier of this ontology references. This is the key with which this instance
	 * is stored in the system map {@link OWLReferencesContainer#allReferences}
	 * @param filePath the file path (or URL) to the ontology.
	 * @param ontologyPath the IRI path of the ontology.
	 * @param reasonerFactory the reasoner factory qualifier used to instance the reasoner assigned to the ontology refereed by this class. 
	 * If this parameter is {@code null} the default reasoner type is given by the method {@link #getDefaultReasoner(Boolean)}.
	 * The values of this parameter have to be in the range: [{@link OWLLibrary#REASONER_QUALIFIER_PELLET},
	 * {@link OWLLibrary#REASONER_QUALIFIER_HERMIT}, {@link OWLLibrary#REASONER_QUALIFIER_SNOROCKET} or {@link OWLLibrary#REASONER_QUALIFIER_FACT}]. 
	 * @param bufferingReasoner {@code true} if the reasoner have to evaluate changes to the ontology as soon as their have been performed.
	 * {@code false} if the reasoner should evaluate all the changes of the ontology only if the method {@link synchroniseReasoner} gets called. 
	 * @param command specifying if the ontology should be created, loaded from file or from web. Possible value of {@code commands} are:
	 * {@link OWLReferencesContainer#COMMAND_CREATE}, {@link OWLReferencesContainer#COMMAND_LOAD_FILE} or 
	 * {@link OWLReferencesContainer#COMMAND_LOAD_WEB}.
	 */
	protected OWLReferences(String referenceName, String filePath, String ontologyPath, String reasonerFactory, Boolean bufferingReasoner, Integer command) {
		super(referenceName, filePath, ontologyPath, reasonerFactory, bufferingReasoner, command);
	}

	// [[[[[[[[[[[[[[[[[[[[[[   METHODS TO QUEY THE ONTOLOGY   ]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]

	// ##################################   to ontology enquirer !!!!!!!!!!!!!
	// mutex for assure thread safe behaviour 
	private Lock mutexIndividualB2Class = new ReentrantLock();
	private Lock mutexDataPropB2Ind = new ReentrantLock();
	private Lock mutexObjPropB2Ind = new ReentrantLock();
	private Lock mutexSubClass = new ReentrantLock();
	private Lock mutexSuperClass = new ReentrantLock();
	private Lock mutexAllObjPropB2Ind = new ReentrantLock();
	private Lock mutexAllDataPropB2Ind = new ReentrantLock();
	private Lock mutexIndivClasses = new ReentrantLock();
	private Lock mutexSubDataProp = new ReentrantLock();
	private Lock mutexSubObjProp = new ReentrantLock();
	private Lock mutexSuperDataProp = new ReentrantLock();
	private Lock mutexSuperObjProp = new ReentrantLock();
	
	/**
	 * This method search for all the individuals in the root class {@link OWLDataFactory#getOWLThing()}. 
	 * It looks for defined semantic entities as well as for inferred 
	 * axioms (given by the reasoner). In order to do so it calls:
	 * {@link OWLEnquirer#getIndividualB2Thing()}
	 * @return the set of all the individuals into the root ontology class.
	 */
	public Set<OWLNamedIndividual> getIndividualB2Thing(){
		long t = System.nanoTime();
		mutexReasoner.lock();
		mutexIndividualB2Class.lock();
		try{
			Set<OWLNamedIndividual> out = getOWLEnquirer().getIndividualB2Thing();
			long t1 = System.nanoTime();
			loggLockTime( t, t1);
			return out;
		} finally{
			mutexIndividualB2Class.unlock();
			mutexReasoner.unlock();
		}
	}
	/**
	 * This method search for one individual in the root class {@link OWLDataFactory#getOWLThing()}. 
	 * It looks for defined semantic entities as well as for inferred 
	 * axioms (given by the reasoner). In order to do so it calls:
	 * {@link OWLEnquirer#getOnlyIndividualB2Thing()}
	 * @return one individual into the root ontology class.
	 */
	public OWLNamedIndividual getOnlyIndividualB2Thing(){
		long t = System.nanoTime();
		mutexReasoner.lock();
		mutexIndividualB2Class.lock();
		try{
			OWLNamedIndividual out = getOWLEnquirer().getOnlyIndividualB2Thing();
			long t1 = System.nanoTime();
			loggLockTime( t, t1);
			return out;
		} finally{
			mutexIndividualB2Class.unlock();
			mutexReasoner.unlock();
		}
	}
	
	/**
	 * This method search for individuals in a specified class. 
	 * It looks for defined semantic entities as well as for inferred 
	 * axioms (given by the reasoner). In order to do so it calls:
	 * {@link OWLEnquirer#getIndividualB2Class(String)}
	 * @param className the name of the class from which to retrieve the individuals
	 * @return the set of individuals into the given class.
	 */
	public Set<OWLNamedIndividual> getIndividualB2Class( String className){
		long t = System.nanoTime();
		mutexReasoner.lock();
		mutexIndividualB2Class.lock();
		try{
			Set<OWLNamedIndividual> out = getOWLEnquirer().getIndividualB2Class( className);
			long t1 = System.nanoTime();
			loggLockTime( t, t1);
			return out;
		} finally{
			mutexIndividualB2Class.unlock();
			mutexReasoner.unlock();
		}
	}
	/**
	 * This method search for individuals in a specified class. 
	 * It looks for defined semantic entities as well as for inferred 
	 * axioms (given by the reasoner). In order to do so it calls:
	 * {@link OWLEnquirer#getIndividualB2Class(OWLClass)}
	 * @param ontoClass the class from which to retrieve the individuals
	 * @return the set of individuals into the given class.
	 */
	public Set<OWLNamedIndividual> getIndividualB2Class( OWLClass ontoClass){ 
		long t = System.nanoTime();
		mutexReasoner.lock();
		mutexIndividualB2Class.lock();
		try{
			long t1 = System.nanoTime();
			Set<OWLNamedIndividual> out = getOWLEnquirer().getIndividualB2Class( ontoClass);
			loggLockTime( t, t1);
			return out;
		} finally{
			mutexIndividualB2Class.unlock();
			mutexReasoner.unlock();
		}
	}
	/**
	 * This method search for individuals in a specified class. 
	 * It looks for defined semantic entities as well as for inferred 
	 * axioms (given by the reasoner). In order to do so it calls:
	 * {@link OWLEnquirer#getOnlyIndividualB2Class(String)};
	 * @param className the name of the class from which to retrieve the individuals
	 * @return an individuals into the given class.
	 */
	public OWLNamedIndividual getOnlyIndividualB2Class( String className){ 
		long t = System.nanoTime();
		mutexReasoner.lock();
		mutexIndividualB2Class.lock();
		try{
			long t1 = System.nanoTime();
			OWLNamedIndividual out = getOWLEnquirer().getOnlyIndividualB2Class( className);
			loggLockTime( t, t1);
			return  out;
		} finally{
			mutexIndividualB2Class.unlock();
			mutexReasoner.unlock();
		}
	}
	/**
	 * This method search for individuals in a specified class. 
	 * It looks for defined semantic entities as well as for inferred 
	 * axioms (given by the reasoner). In order to do so it calls:
	 * {@link OWLEnquirer#getOnlyIndividualB2Class(OWLClass)};
	 * @param ontoClass the class from which to retrieve the individuals
	 * @return an individuals into the given class.
	 */
	public OWLNamedIndividual getOnlyIndividualB2Class( OWLClass ontoClass){
		long t = System.nanoTime();
		mutexReasoner.lock();
		mutexIndividualB2Class.lock();
		try{
			long t1 = System.nanoTime();
			OWLNamedIndividual out = getOWLEnquirer().getOnlyIndividualB2Class(ontoClass);
			loggLockTime( t, t1);
			return out;
		} finally{
			mutexIndividualB2Class.unlock();
			mutexReasoner.unlock();
		}
	}

	/**
	 * This method search for the classes in which an individuals is belonging to. 
	 * It looks for defined semantic entities as well as for inferred 
	 * axioms (given by the reasoner). In order to do so it calls:
	 * {@link OWLEnquirer#getIndividualClasses(OWLNamedIndividual)};
	 * @param individual the individual from which query its types.
	 * @return the set of classes in which the given individual is belonging to.
	 */
	public Set< OWLClass> getIndividualClasses( OWLNamedIndividual individual){
		long t = System.nanoTime();
		mutexReasoner.lock();
		mutexIndivClasses.lock();
		try{
			long t1 = System.nanoTime();
			Set<OWLClass> out = getOWLEnquirer().getIndividualClasses( individual);
			loggLockTime( t, t1);
			return out;
		} finally{
			mutexIndivClasses.unlock();
			mutexReasoner.unlock();
		}
	}
	/**
	 * This method search for the classes in which an individuals is belonging to. 
	 * It looks for defined semantic entities as well as for inferred 
	 * axioms (given by the reasoner). In order to do so it calls:
	 * {@link OWLEnquirer#getIndividualClasses( String)};
	 * @param individual the individual from which query its types.
	 * @return the set of classes in which the given individual is belonging to.
	 */
	public Set< OWLClass> getIndividualClasses( String individual){
		long t = System.nanoTime();
		mutexReasoner.lock();
		mutexIndivClasses.lock();
		try{
			long t1 = System.nanoTime();
			Set<OWLClass> out = getOWLEnquirer().getIndividualClasses( individual);
			loggLockTime( t, t1);
			return out;
		} finally{
			mutexIndivClasses.unlock();
			mutexReasoner.unlock();
		}
	}
	/**
	 * This method search for a class in which an individuals is belonging to. 
	 * It looks for defined semantic entities as well as for inferred 
	 * axioms (given by the reasoner). In order to do so it calls:
	 * {@link OWLEnquirer#getOnlyIndividualClasses(OWLNamedIndividual)};
	 * @param individual the individual from which query its types.
	 * @return the set of classes in which the given individual is belonging to.
	 */
	public OWLClass getOnlyIndividualClasses( OWLNamedIndividual individual){
		long t = System.nanoTime();
		mutexReasoner.lock();
		mutexIndivClasses.lock();
		try{
			long t1 = System.nanoTime();
			OWLClass out = getOWLEnquirer().getOnlyIndividualClasses( individual);
			loggLockTime( t, t1);
			return out;
		} finally{
			mutexIndivClasses.unlock();
			mutexReasoner.unlock();
		}
	}
	/**
	 * This method search for a class in which an individuals is belonging to. 
	 * It looks for defined semantic entities as well as for inferred 
	 * axioms (given by the reasoner). In order to do so it calls:
	 * {@link OWLEnquirer#getOnlyIndividualClasses( String)};
	 * @param individual the individual from which query its types.
	 * @return the set of classes in which the given individual is belonging to.
	 */
	public OWLClass getOnlyIndividualClasses( String individual){
		long t = System.nanoTime();
		mutexReasoner.lock();
		mutexIndivClasses.lock();
		try{
			long t1 = System.nanoTime();
			OWLClass out = getOWLEnquirer().getOnlyIndividualClasses( individual);
			loggLockTime( t, t1);
			return out;
		} finally{
			mutexIndivClasses.unlock();
			mutexReasoner.unlock();
		}
	}
	
	
	/**
	 * This method search for the data properties assigned to a specified individual. 
	 * It looks for defined semantic entities as well as for inferred 
	 * axioms (given by the reasoner). In order to do so it calls:
	 * {@link OWLEnquirer#getDataPropertyB2Individual(String, String)};
	 * @param individualName the name of the individual from which query its types.
	 * @param propertyName the name of the property to search in the individual properties.
	 * @return the set of values of the specified data property assigned to an individual.
	 */
	public Set<OWLLiteral> getDataPropertyB2Individual( String individualName, String propertyName){
		long t = System.nanoTime();
		mutexReasoner.lock();
		mutexDataPropB2Ind.lock();
		try{
			long t1 = System.nanoTime();
			Set<OWLLiteral> out = getOWLEnquirer().getDataPropertyB2Individual( individualName, propertyName);
			loggLockTime( t, t1);
			return out; 
		} finally{
			mutexDataPropB2Ind.unlock();
			mutexReasoner.unlock();
		}
	}
	/**
	 * This method search for the data properties assigned to a specified individual. 
	 * It looks for defined semantic entities as well as for inferred 
	 * axioms (given by the reasoner). In order to do so it calls:
	 * {@link OWLEnquirer#getDataPropertyB2Individual(OWLNamedIndividual, OWLDataProperty)};
	 * @param individual the individual from which query its types.
	 * @param property the property to search in the individual properties.
	 * @return the set of values of the specified data property assigned to an individual.
	 */
	public Set<OWLLiteral> getDataPropertyB2Individual( OWLNamedIndividual individual, OWLDataProperty property){
		long t = System.nanoTime();
		mutexReasoner.lock();
		mutexDataPropB2Ind.lock();
		try{
			long t1 = System.nanoTime();
			Set<OWLLiteral> out = getOWLEnquirer().getDataPropertyB2Individual( individual, property);
			loggLockTime( t, t1);
			return out;
		} finally{
			mutexDataPropB2Ind.unlock();
			mutexReasoner.unlock();
		}
	}
	/**
	 * This method search for a data property assigned to a specified individual. 
	 * It looks for defined semantic entities as well as for inferred 
	 * axioms (given by the reasoner). In order to do so it calls:
	 * {@link OWLEnquirer#getOnlyDataPropertyB2Individual(String, String)};
	 * @param individualName the name of the individual from which query its types.
	 * @param propertyName the name of the property to search in the individual properties.
	 * @return a value of the specified data property assigned to an individual.
	 */
	public OWLLiteral getOnlyDataPropertyB2Individual( String individualName, String propertyName){
		long t = System.nanoTime();
		mutexReasoner.lock();
		mutexDataPropB2Ind.lock();
		try{
			long t1 = System.nanoTime();
			OWLLiteral out = getOWLEnquirer().getOnlyDataPropertyB2Individual( individualName, propertyName);
			loggLockTime( t, t1);
			return out;
		} finally{
			mutexDataPropB2Ind.unlock();
			mutexReasoner.unlock();
		}
	}
	/**
	 * This method search for a data property assigned to a specified individual. 
	 * It looks for defined semantic entities as well as for inferred 
	 * axioms (given by the reasoner). In order to do so it calls:
	 * {@link OWLEnquirer#getOnlyDataPropertyB2Individual(OWLNamedIndividual, OWLDataProperty)};
	 * @param individual the individual from which query its types.
	 * @param property the property to search in the individual properties.
	 * @return a value of the specified data property assigned to an individual.
	 */
	public OWLLiteral getOnlyDataPropertyB2Individual( OWLNamedIndividual individual, OWLDataProperty property){
		long t = System.nanoTime();
		mutexReasoner.lock();
		mutexDataPropB2Ind.lock();
		try{
			long t1 = System.nanoTime();
			OWLLiteral out = getOWLEnquirer().getOnlyDataPropertyB2Individual( individual, property);
			loggLockTime( t, t1);
			return out;
		} finally{
			mutexDataPropB2Ind.unlock();
			mutexReasoner.unlock();
		}
	}

	/**
	 * This method search for the all the data properties assigned to a specified individual. 
	 * It looks for defined semantic entities as well as for inferred 
	 * axioms (given by the reasoner). In order to do so it calls:
	 * {@link OWLEnquirer#getDataPropertyB2Individual(OWLNamedIndividual)}
	 * @param individual the individual from which query its data properties.
	 * @return the set of a container of all the data properties with relative values.
	 */
	public Set<DataPropertyRelatios> getDataPropertyB2Individual( OWLNamedIndividual individual){
		long t = System.nanoTime();
		mutexReasoner.lock();
		mutexAllDataPropB2Ind.lock();
		try{
			long t1 = System.nanoTime();
			Set<DataPropertyRelatios> out = getOWLEnquirer().getDataPropertyB2Individual( individual);
			loggLockTime( t, t1);
			return out;
		} finally{
			mutexAllDataPropB2Ind.unlock();
			mutexReasoner.unlock();
		}
	}
	/**
	 * This method search for the all the data properties assigned to a specified individual.  
	 * It looks for defined semantic entities as well as for inferred 
	 * axioms (given by the reasoner). In order to do so it calls:
	 * {@link OWLEnquirer#getDataPropertyB2Individual(String)}
	 * @param individualName the name of the individual from which query its data properties.
	 * @return the set of a container of all the data properties with relative values.
	 */
	public Set<DataPropertyRelatios> getDataPropertyB2Individual( String individualName){
		long t = System.nanoTime();
		mutexReasoner.lock();
		mutexAllObjPropB2Ind.lock();
		try{
			long t1 = System.nanoTime();
			Set<DataPropertyRelatios> out = getOWLEnquirer().getDataPropertyB2Individual( individualName);
			loggLockTime( t, t1);
			return out;
		} finally{
			mutexAllObjPropB2Ind.unlock();
			mutexReasoner.unlock();
		}
	}
	
	/**
	 * This method search for the object properties assigned to a specified individual. 
	 * It looks for defined semantic entities as well as for inferred 
	 * axioms (given by the reasoner). In order to do so it calls:
	 * {@link OWLEnquirer#getObjectPropertyB2Individual(String, String)};
	 * @param individualName the name of the individual from which query its types.
	 * @param propertyName the name of the property to search in the individual properties.
	 * @return the set of values of the specified object property assigned to an individual.
	 */
	public Set<OWLNamedIndividual> getObjectPropertyB2Individual( String individualName, String propertyName){
		long t = System.nanoTime();
		mutexReasoner.lock();
		mutexObjPropB2Ind.lock();
		try{
			long t1 = System.nanoTime();
			Set<OWLNamedIndividual> out = getOWLEnquirer().getObjectPropertyB2Individual( individualName, propertyName);
			loggLockTime( t, t1);
			return out;
		} finally{
			mutexObjPropB2Ind.unlock();
			mutexReasoner.unlock();
		}
	}
	/**
	 * This method search for the object properties assigned to a specified individual. 
	 * It looks for defined semantic entities as well as for inferred 
	 * axioms (given by the reasoner). In order to do so it calls:
	 * {@link OWLEnquirer#getObjectPropertyB2Individual(OWLNamedIndividual, OWLObjectProperty)};
	 * @param individual the individual from which query its types.
	 * @param property the property to search in the individual properties.
	 * @return the set of values of the specified object property assigned to an individual.
	 */
	public Set<OWLNamedIndividual> getObjectPropertyB2Individual( OWLNamedIndividual individual, OWLObjectProperty property){
		long t = System.nanoTime();
		mutexReasoner.lock();
		mutexObjPropB2Ind.lock();
		try{
			long t1 = System.nanoTime();
			Set<OWLNamedIndividual> out = getOWLEnquirer().getObjectPropertyB2Individual( individual, property);
			loggLockTime( t, t1);
			return out;
		} finally{
			mutexObjPropB2Ind.unlock();
			mutexReasoner.unlock();
		}
	}
	/**
	 * This method search for the object properties assigned to a specified individual. 
	 * It looks for defined semantic entities as well as for inferred 
	 * axioms (given by the reasoner). In order to do so it calls:
	 * {@link OWLEnquirer#getOnlyObjectPropertyB2Individual(String, String)};
	 * @param individualName the name of the individual from which query its types.
	 * @param propertyName the name of the property to search in the individual properties.
	 * @return a value of the specified object property assigned to an individual.
	 */
	public OWLNamedIndividual getOnlyObjectPropertyB2Individual( String individualName, String propertyName){
		long t = System.nanoTime();
		mutexReasoner.lock();
		mutexObjPropB2Ind.lock();
		try{
			long t1 = System.nanoTime();
			OWLNamedIndividual out = getOWLEnquirer().getOnlyObjectPropertyB2Individual( individualName, propertyName);
			loggLockTime( t, t1);
			return out;
		} finally{
			mutexObjPropB2Ind.unlock();
			mutexReasoner.unlock();
		}
	}
	/**
	 * This method search for the object properties assigned to a specified individual. 
	 * It looks for defined semantic entities as well as for inferred 
	 * axioms (given by the reasoner). In order to do so it calls:
	 * {@link OWLEnquirer#getOnlyObjectPropertyB2Individual(OWLNamedIndividual, OWLObjectProperty)};
	 * @param individual the individual from which query its types.
	 * @param property the property to search in the individual properties.
	 * @return a value of the specified object property assigned to an individual.
	 */
	public OWLNamedIndividual getOnlyObjectPropertyB2Individual( OWLNamedIndividual individual, OWLObjectProperty property){
		long t = System.nanoTime();
		mutexReasoner.lock();
		mutexObjPropB2Ind.lock();
		try{
			long t1 = System.nanoTime();
			OWLNamedIndividual out = getOWLEnquirer().getOnlyObjectPropertyB2Individual( individual, property);
			loggLockTime( t, t1);
			return out;
		} finally{
			mutexObjPropB2Ind.unlock();
			mutexReasoner.unlock();
		}
	}
	
	/**
	 * This method search for the all the object properties assigned to a specified individual. 
	 * It looks for defined semantic entities as well as for inferred 
	 * axioms (given by the reasoner). In order to do so it calls:
	 * {@link OWLEnquirer#getObjectPropertyB2Individual(OWLNamedIndividual)}
	 * @param individual the individual from which query its object properties.
	 * @return the set of a container of all the object properties with relative values.
	 */
	public Set<ObjectPropertyRelatios> getObjectPropertyB2Individual( OWLNamedIndividual individual){
		long t = System.nanoTime();
		mutexReasoner.lock();
		mutexAllObjPropB2Ind.lock();
		try{
			long t1 = System.nanoTime();
			Set<ObjectPropertyRelatios> out = getOWLEnquirer().getObjectPropertyB2Individual( individual);
			loggLockTime( t, t1);
			return out;
		} finally{
			mutexAllObjPropB2Ind.unlock();
			mutexReasoner.unlock();
		}
	}
	/**
	 * This method search for the all the object properties assigned to a specified individual. 
	 * It looks for defined semantic entities as well as for inferred 
	 * axioms (given by the reasoner). In order to do so it calls:
	 * {@link OWLEnquirer#getObjectPropertyB2Individual(String)}
	 * @param individualName the name of the individual from which query its object properties.
	 * @return the set of a container of all the object properties with relative values.
	 */
	public Set< ObjectPropertyRelatios> getObjectPropertyB2Individual( String individualName){
		long t = System.nanoTime();
		mutexReasoner.lock();
		mutexAllObjPropB2Ind.lock();
		try{
			long t1 = System.nanoTime();
			Set<ObjectPropertyRelatios> out = getOWLEnquirer().getObjectPropertyB2Individual( individualName);
			loggLockTime( t, t1);
			return out;
		} finally{
			mutexAllObjPropB2Ind.unlock();
			mutexReasoner.unlock();
		}
	}

	/**
	 * This method search for the sub data properties of a specified property. 
	 * It looks for defined semantic entities as well as for inferred 
	 * axioms (given by the reasoner). In order to do so it calls:
	 * {@link OWLEnquirer#getSubDataPropertyOf(String)};
	 * @param propName the name of the data property from which to retrieve its sub-properties.
	 * @return the set of all the data properties that are sub-properties of the specified parameter.
	 */
	public Set<OWLDataProperty> getSubDataPropertyOf( String propName){
		long t = System.nanoTime();
		mutexReasoner.lock();
		mutexSubDataProp.lock();
		try{
			long t2 = System.nanoTime();
			Set<OWLDataProperty> out = getOWLEnquirer().getSubDataPropertyOf( propName);
			loggLockTime( t, t2);
			return out;
		} finally{
			mutexSubDataProp.unlock();
			mutexReasoner.unlock();
		}
	}
	/**
	 * This method search for the sub data properties of a specified property. 
	 * It looks for defined semantic entities as well as for inferred 
	 * axioms (given by the reasoner). In order to do so it calls:
	 * {@link OWLEnquirer#getSubDataPropertyOf(OWLDataProperty)};
	 * @param prop the data property from which to retrieve its sub-properties.
	 * @return the set of all the data properties that are sub-properties of the specified parameter.
	 */
	public Set<OWLDataProperty> getSubDataPropertyOf( OWLDataProperty prop){
		long t = System.nanoTime();
		mutexReasoner.lock();
		mutexSubDataProp.lock();
		try{
			long t2 = System.nanoTime();
			Set<OWLDataProperty> out = getOWLEnquirer().getSubDataPropertyOf( prop);
			loggLockTime( t, t2);
			return out;
		} finally{
			mutexSubDataProp.unlock();
			mutexReasoner.unlock();
		}
	}

	/** This method search for the super data properties of a specified property. 
	 * It looks for defined semantic entities as well as for inferred 
	 * axioms (given by the reasoner). In order to do so it calls:
	 * {@link OWLEnquirer#getSuperDataPropertyOf(String)};
	 * @param propName the name of the data property from which to retrieve its super-properties.
	 * @return the set of all the data properties that are super-properties of the specified parameter.
	 */
	public Set<OWLDataProperty> getSuperDataPropertyOf( String propName){
		long t = System.nanoTime();
		mutexReasoner.lock();
		mutexSuperDataProp.lock();
		try{
			long t2 = System.nanoTime();
			Set<OWLDataProperty> out = getOWLEnquirer().getSuperDataPropertyOf( propName);
			loggLockTime( t, t2);
			return out;
		} finally{
			mutexSuperDataProp.unlock();
			mutexReasoner.unlock();
		}
	}
	/**
	 * This method search for the super data properties of a specified property. 
	 * It looks for defined semantic entities as well as for inferred 
	 * axioms (given by the reasoner). In order to do so it calls:
	 * {@link OWLEnquirer#getSuperDataPropertyOf(OWLDataProperty)};
	 * @param prop the data property from which to retrieve its super-properties.
	 * @return the set of all the data properties that are super-properties of the specified parameter.
	 */
	public Set<OWLDataProperty> getSuperDataPropertyOf( OWLDataProperty prop){
		long t = System.nanoTime();
		mutexReasoner.lock();
		mutexSuperDataProp.lock();
		try{
			long t2 = System.nanoTime();
			Set<OWLDataProperty> out = getOWLEnquirer().getSuperDataPropertyOf( prop);
			loggLockTime( t, t2);
			return out;
		} finally{
			mutexSuperDataProp.unlock();
			mutexReasoner.unlock();
		}
	}
	
	/**
	 * This method search for the sub object properties of a specified property. 
	 * It looks for defined semantic entities as well as for inferred 
	 * axioms (given by the reasoner). In order to do so it calls:
	 * {@link OWLEnquirer#getSubObjectPropertyOf(String)};
	 * @param propName the name of the data property from which to retrieve its sub-properties.
	 * @return the set of all the object properties that are sub-properties of the specified parameter.
	 */
	public Set<OWLObjectProperty> getSubObjectPropertyOf( String propName){
		long t = System.nanoTime();
		mutexReasoner.lock();
		mutexSubObjProp.lock();
		try{
			long t2 = System.nanoTime();
			Set<OWLObjectProperty> out = getOWLEnquirer().getSubObjectPropertyOf( propName);
			loggLockTime( t, t2);
			return out;
		} finally{
			mutexSubObjProp.unlock();
			mutexReasoner.unlock();
		}
	}
	/**
	 * This method search for the sub object properties of a specified property. 
	 * It looks for defined semantic entities as well as for inferred 
	 * axioms (given by the reasoner). In order to do so it calls:
	 * {@link OWLEnquirer#getSubObjectPropertyOf(OWLObjectProperty)};
	 * @param prop the data property from which to retrieve its sub-properties.
	 * @return the set of all the object properties that are sub-properties of the specified parameter.
	 */
	public Set<OWLObjectProperty> getSubObjectPropertyOf( OWLObjectProperty prop){
		long t = System.nanoTime();
		mutexReasoner.lock();
		mutexSubObjProp.lock();
		try{
			long t2 = System.nanoTime();
			Set<OWLObjectProperty> out = getOWLEnquirer().getSubObjectPropertyOf( prop);
			loggLockTime( t, t2);
			return out;
		} finally{
			mutexSubObjProp.unlock();
			mutexReasoner.unlock();
		}
	}	
	
	/** This method search for the super object properties of a specified property. 
	 * It looks for defined semantic entities as well as for inferred 
	 * axioms (given by the reasoner). In order to do so it calls:
	 * {@link OWLEnquirer#getSuperObjectPropertyOf(String)};
	 * @param propName the name of the data property from which to retrieve its super-properties.
	 * @return the set of all the object properties that are super-properties of the specified parameter.
	 */
	public Set<OWLObjectProperty> getSuperObjectPropertyOf( String propName){
		long t = System.nanoTime();
		mutexReasoner.lock();
		mutexSuperObjProp.lock();
		try{
			long t2 = System.nanoTime();
			Set<OWLObjectProperty> out = getOWLEnquirer().getSuperObjectPropertyOf( propName);
			loggLockTime( t, t2);
			return out;
		} finally{
			mutexSuperObjProp.unlock();
			mutexReasoner.unlock();
		}
	}
	/**
	 * This method search for the super object properties of a specified property. 
	 * It looks for defined semantic entities as well as for inferred 
	 * axioms (given by the reasoner). In order to do so it calls:
	 * {@link OWLEnquirer#getSuperObjectPropertyOf(OWLObjectProperty)};
	 * @param prop the data property from which to retrieve its super-properties.
	 * @return the set of all the object properties that are super-properties of the specified parameter.
	 */
	public Set<OWLObjectProperty> getSuperObjectPropertyOf( OWLObjectProperty prop){
		long t = System.nanoTime();
		mutexReasoner.lock();
		mutexSuperObjProp.lock();
		try{
			long t2 = System.nanoTime();
			Set<OWLObjectProperty> out = getOWLEnquirer().getSuperObjectPropertyOf( prop);
			loggLockTime( t, t2);
			return out;
		} finally{
			mutexSuperObjProp.unlock();
			mutexReasoner.unlock();
		}
	}	
		
	/**
	 * This method search for the sub classes of a specified class. 
	 * It looks for defined semantic entities as well as for inferred 
	 * axioms (given by the reasoner). In order to do so it calls:
	 * {@link OWLEnquirer#getSubClassOf(String)};
	 * @param className the name of the class from which to retrieve its sub-classes.
	 * @return the set of all the classes that are sub-classes of the specified parameter.
	 */
	public Set<OWLClass> getSubClassOf( String className){
		long t = System.nanoTime();
		mutexReasoner.lock();
		mutexSubClass.lock();
		try{
			long t2 = System.nanoTime();
			Set<OWLClass> out = getOWLEnquirer().getSubClassOf( className);
			loggLockTime( t, t2);
			return out;
		} finally{
			mutexSubClass.unlock();
			mutexReasoner.unlock();
		}
	}
	/**
	 * This method search for the sub classes of a specified class. 
	 * It looks for defined semantic entities as well as for inferred 
	 * axioms (given by the reasoner). In order to do so it calls:
	 * {@link OWLEnquirer#getSubClassOf(OWLClass)};
	 * @param class the class from which to retrieve its sub-classes.
	 * @return the set of all the classes that are sub-classes of the specified parameter.
	 */
	public Set<OWLClass> getSubClassOf( OWLClass cl){
		long t = System.nanoTime(); 
		mutexReasoner.lock();
		mutexSubClass.lock();
		try{
			long t1 = System.nanoTime(); 
			Set<OWLClass> out = getOWLEnquirer().getSubClassOf( cl);
			loggLockTime( t, t1);
			return out;
		} finally{
			mutexSubClass.unlock();
			mutexReasoner.unlock();
		}	
	}

	/**
	 * This method search for the super classes of a specified class. 
	 * It looks for defined semantic entities as well as for inferred 
	 * axioms (given by the reasoner). In order to do so it calls:
	 * {@link OWLEnquirer#getSuperClassOf(String)};
	 * @param className the name of the class from which to retrieve its super-classes.
	 * @return the set of all the classes that are super-classes of the specified parameter.
	 */
	public Set<OWLClass> getSuperClassOf( String className){
		long t = System.nanoTime();
		mutexReasoner.lock();
		mutexSuperClass.lock();
		try{
			long t1 = System.nanoTime();
			Set<OWLClass> out = getOWLEnquirer().getSuperClassOf( className);
			loggLockTime( t, t1);
			return out;
		} finally{
			mutexSuperClass.unlock();
			mutexReasoner.unlock();
		}	
	}
	/**
	 * This method search for the super classes of a specified class. 
	 * It looks for defined semantic entities as well as for inferred 
	 * axioms (given by the reasoner). In order to do so it calls:
	 * {@link OWLEnquirer#getSuperClassOf(OWLClass)};
	 * @param class the class from which to retrieve its super-classes.
	 * @return the set of all the classes that are super-classes of the specified parameter.
	 */
	public Set<OWLClass> getSuperClassOf( OWLClass cl){
		long t = System.nanoTime();
		mutexReasoner.lock();
		mutexSuperClass.lock();
		try{
			long t1 = System.nanoTime();
			Set<OWLClass> out = getOWLEnquirer().getSuperClassOf( cl);
			loggLockTime( t, t1);
			return out;
		} finally{
			mutexSuperClass.unlock();
			mutexReasoner.unlock();
		}	
	}

	
	
	// [[[[[[[[[[[[[[[[[[[[   METHODS TO MANIPULATE THE ONTOLOGY   ]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]
	// it uses default ontology manipulator (not buffering)!!!!
	// you can change this by calling: "this.setManipulatorChangeBuffering( true);"
	// but then remember to call "this.applyManipulatorChanges();" to actually perform the ontology changes

	// ##################################   to ontology manipulator !!!!!!!!!!!!!
	// mutex for assure thread safe behaviour 
	private Lock mutexReasoner = new ReentrantLock();
	private Lock mutexAddObjPropB2Ind = new ReentrantLock();
	private Lock mutexAddDataPropB2Ind = new ReentrantLock();
	private Lock mutexAddIndB2Class = new ReentrantLock();
	private Lock mutexAddInd = new ReentrantLock();
	private Lock mutexAddClass = new ReentrantLock();
	private Lock mutexAddSubClass = new ReentrantLock();
	private Lock mutexRemoveClass = new ReentrantLock();
	private Lock mutexRemoveSubClass = new ReentrantLock();
	private Lock mutexRemoveObjPropB2Ind = new ReentrantLock();
	private Lock mutexRemoveDataPropB2Ind = new ReentrantLock();
	private Lock mutexRemoveIndB2Class = new ReentrantLock();
	private Lock mutexRemoveInd = new ReentrantLock();
	private Lock mutexReplaceDataProp = new ReentrantLock();
	private Lock mutexRename = new ReentrantLock();
	private Lock mutexAddDisjoinedInd = new ReentrantLock();
	private Lock mutexRemoveDisjoinedInd = new ReentrantLock();
	private Lock mutexAddDisjoinedCls = new ReentrantLock();
	private Lock mutexRemoveDisjoinedCls = new ReentrantLock();

	// ------------------------------------------------------------   methods for ADDING entities
	/**
	 * This method calls {@link OWLManipulator#addObjectPropertyB2Individual(OWLNamedIndividual, OWLObjectProperty, OWLNamedIndividual)}
	 * in order to add an object property to an individual.
	 * @param ind the individual to set
	 * @param prop the object property to add to the individual
	 * @param value the value of the data property
	 * @return the changes to be done in order to add an object property to an individual. (see {@link OWLManipulator} for more info)
	 */
	public OWLOntologyChange addObjectPropertyB2Individual( OWLNamedIndividual ind, OWLObjectProperty prop,  OWLNamedIndividual value){
		long t = System.nanoTime();
		mutexReasoner.lock();
		mutexAddObjPropB2Ind.lock();
		try{
			long t1 = System.nanoTime();
			OWLOntologyChange out = getOWLManipulator().addObjectPropertyB2Individual( ind, prop, value);
			loggLockTime( t, t1);
			return out;
		} finally{
			mutexAddObjPropB2Ind.unlock();
			mutexReasoner.unlock();
		}
	}
	/**
	 * This method calls {@link OWLManipulator#addObjectPropertyB2Individual(String, String, String)}
	 * in order to add an object property to an individual.
	 * @param individualName the name of the individual to set
	 * @param propName the name of the object property to add to the individual
	 * @param valueName the name of the value of the data property
	 * @return the changes to be done in order to add an object property to an individual. (see {@link OWLManipulator} for more info)
	 */
	public OWLOntologyChange addObjectPropertyB2Individual( String individualName, String propName, String valueName){
		long t = System.nanoTime();
		mutexReasoner.lock();
		mutexAddObjPropB2Ind.lock();
		try{
			long t1 = System.nanoTime();
			OWLOntologyChange out = getOWLManipulator().addObjectPropertyB2Individual( individualName, propName, valueName);
			loggLockTime( t, t1);
			return out;
		} finally{
			mutexAddObjPropB2Ind.unlock();
			mutexReasoner.unlock();
		}
	}


	/**
	 * This method calls {@link OWLManipulator#addDataPropertyB2Individual(OWLNamedIndividual, OWLDataProperty, OWLLiteral)}
	 * in order to add a data property to an individual.
	 * @param ind the individual to set
	 * @param prop the data property to add to the individual
	 * @param value the value of the data property
	 * @return the changes to be done in order to add an data property to an individual. (see {@link OWLManipulator} for more info)
	 */
	public OWLOntologyChange addDataPropertyB2Individual(OWLNamedIndividual ind,  OWLDataProperty prop, OWLLiteral value){
		long t = System.nanoTime();
		mutexReasoner.lock();
		mutexAddDataPropB2Ind.lock();
		try{
			long t1 = System.nanoTime();
			OWLOntologyChange out = getOWLManipulator().addDataPropertyB2Individual(ind, prop, value);
			loggLockTime( t, t1);
			return out;
		} finally{
			mutexAddDataPropB2Ind.unlock();
			mutexReasoner.unlock();
		}
	}
	/**
	 * This method calls {@link OWLManipulator#addDataPropertyB2Individual(String, String, Object)}
	 * in order to add a data property to an individual.
	 * @param individualName the name of the individual to set
	 * @param propertyName the name of the data property to add to the individual
	 * @param value the name of the value of the data property
	 * @return the changes to be done in order to add an data property to an individual. (see {@link OWLManipulator} for more info)
	 */
	public OWLOntologyChange addDataPropertyB2Individual( String individualName, String propertyName, Object value){
		long t = System.nanoTime();
		mutexReasoner.lock();
		mutexAddDataPropB2Ind.lock();
		try{
			long t1 = System.nanoTime();
			OWLOntologyChange out = getOWLManipulator().addDataPropertyB2Individual( individualName, propertyName, value);
			loggLockTime( t, t1);
			return out;
		} finally{
			mutexAddDataPropB2Ind.unlock();
			mutexReasoner.unlock();
		}
	}

	/**
	 * This method calls {@link OWLManipulator#addIndividual(OWLNamedIndividual)}
	 * in order to add a an individual into an ontology.
	 * @param ind the individual to be set to be belonging to the {@code OWLThing} class.
	 * @return the changes to be done in order to add an individual into a class. (see {@link OWLManipulator} for more info)
	 */
	public OWLOntologyChange addIndividual(OWLNamedIndividual ind){
		long t = System.nanoTime();
		mutexReasoner.lock();
		mutexAddInd.lock();
		try{
			long t1 = System.nanoTime();
			OWLOntologyChange out = getOWLManipulator().addIndividual(ind);
			loggLockTime( t, t1);
			return out;
		} finally{
			mutexAddInd.unlock();
			mutexReasoner.unlock();
		}
	}
	/**
	 * This method calls {@link OWLManipulator#addIndividual(String)}
	 * in order to add a an individual into an ontology.
	 * @param individualName the name of the individual to be set to be belonging to the {@code OWLThing} class.
	 * @return the changes to be done in order to add an individual into a class. (see {@link OWLManipulator} for more info)
	 */
	public OWLOntologyChange addIndividual(String individualName){
		long t = System.nanoTime();
		mutexReasoner.lock();
		mutexAddInd.lock();
		try{
			long t1 = System.nanoTime();
			OWLOntologyChange out = getOWLManipulator().addIndividual( individualName);
			loggLockTime( t, t1);
			return out;
		} finally{
			mutexAddInd.unlock();
			mutexReasoner.unlock();
		}
	}
	
	/**
	 * This method calls {@link OWLManipulator#addIndividualB2Class(OWLNamedIndividual, OWLClass)}
	 * in order to add a an individual into a class.
	 * @param ind the individual to be set to be belonging to the {@code cls} class.
	 * @param cls the class in which the individual will be belonging to.
	 * @return the changes to be done in order to add an individual into a class. (see {@link OWLManipulator} for more info)
	 */
	public OWLOntologyChange addIndividualB2Class(OWLNamedIndividual ind, OWLClass cls){
		long t = System.nanoTime();
		mutexReasoner.lock();
		mutexAddIndB2Class.lock();
		try{
			long t1 = System.nanoTime();
			OWLOntologyChange out = getOWLManipulator().addIndividualB2Class(ind, cls);
			loggLockTime( t, t1);
			return out;
		} finally{
			mutexAddIndB2Class.unlock();
			mutexReasoner.unlock();
		}
	}
	/**
	 * This method calls {@link OWLManipulator#addIndividualB2Class(String, String)}
	 * in order to add a an individual into a class.
	 * @param individualName the name of the individual to be set to be belonging to the {@code cls} class.
	 * @param className the name of the class in which the individual will be belonging to.
	 * @return the changes to be done in order to add an individual into a class. (see {@link OWLManipulator} for more info)
	 */
	public OWLOntologyChange addIndividualB2Class(String individualName, String className){
		long t = System.nanoTime();
		mutexReasoner.lock();
		mutexAddIndB2Class.lock();
		try{
			long t1 = System.nanoTime();
			OWLOntologyChange out = getOWLManipulator().addIndividualB2Class(individualName, className);
			loggLockTime( t, t1);
			return out;
		} finally{
			mutexAddIndB2Class.unlock();
			mutexReasoner.unlock();
		}
	}

	/**
	 * This method calls {@link OWLManipulator#addClass(String)}
	 * in order to add a class into the ontology.
	 * @param className the name of the class to be added into the ontology.
	 * @return the changes to be done in order to add a class into the ontology. (see {@link OWLManipulator} for more info)
	 */
	public OWLOntologyChange addClass( String className){
		long t = System.nanoTime();
		mutexReasoner.lock();
		mutexAddClass.lock();
		try{
			long t1 = System.nanoTime();
			OWLOntologyChange out = getOWLManipulator().addClass( className);
			loggLockTime( t, t1);
			return out;
		} finally{
			mutexAddClass.unlock();
			mutexReasoner.unlock();
		}
	}
	/**
	 * This method calls {@link OWLManipulator#addClass(OWLClass)}
	 * in order to add a class into the ontology.
	 * @param cls the class to be added into the ontology.
	 * @return the changes to be done in order to add a class into the ontology. (see {@link OWLManipulator} for more info)
	 */
	public OWLOntologyChange addClass( OWLClass cls){
		long t = System.nanoTime();
		mutexReasoner.lock();
		mutexAddClass.lock();
		try{
			long t1 = System.nanoTime();
			OWLOntologyChange out = getOWLManipulator().addClass( cls);
			loggLockTime( t, t1);
			return out;
		} finally{
			mutexAddClass.unlock();
			mutexReasoner.unlock();
		}
	}
	
	/**
	 * This method calls {@link OWLManipulator#addSubClassOf(String, String)}
	 * in order to add a class into the ontology as a sub class of a specified entity.
	 * @param superClassName the name of the class in which add the sub class.
	 * @param subClassName the name of the class to add as sub class of the specified class.
	 * @return the changes to be done in order to add a class by specifying its super class. (see {@link OWLManipulator} for more info)
	 */
	public OWLOntologyChange addSubClassOf( String superClassName, String subClassName){
		long t = System.nanoTime();
		mutexReasoner.lock();
		mutexAddSubClass.lock();
		try{
			long t1 = System.nanoTime();
			OWLOntologyChange out = getOWLManipulator().addSubClassOf( superClassName, subClassName);
			loggLockTime( t, t1);
			return out;
		} finally{
			mutexAddSubClass.unlock();
			mutexReasoner.unlock();
		}
	}
	/**
	 * This method calls {@link OWLManipulator#addSubClassOf(OWLClass, OWLClass)}
	 * in order to add a class into the ontology as a sub class of a specified entity.
	 * @param superClass the class in which add the sub class.
	 * @param subClass the class to add as sub class of the specified class.
	 * @return the changes to be done in order to add a class by specifying its super class. (see {@link OWLManipulator} for more info)
	 */
	public OWLOntologyChange addSubClassOf( OWLClass supClass, OWLClass subClass){
		long t = System.nanoTime();
		mutexReasoner.lock();
		mutexAddSubClass.lock();
		try{
			long t1 = System.nanoTime();
			OWLOntologyChange out = getOWLManipulator().addSubClassOf( supClass, subClass);
			loggLockTime( t, t1);
			return out;
		} finally{
			mutexAddSubClass.unlock();
			mutexReasoner.unlock();
		}
	}
	
	// ------------------------------------------------------------   methods for REMOVING entities
	/**
	 * This method calls {@link OWLManipulator#removeObjectPropertyB2Individual(OWLNamedIndividual, OWLObjectProperty, OWLNamedIndividual)}
	 * in order to remove an object property from an individual.
	 * @param ind the individual which has the property to remove.
	 * @param prop the property to remove.
	 * @param value the value of the property to remove.
	 * @return the changes to be done in order to remove an object property from an individual. (see {@link OWLManipulator} for more info)
	 */
	public OWLOntologyChange removeObjectPropertyB2Individual( OWLNamedIndividual ind, OWLObjectProperty prop, OWLNamedIndividual value){
		long t = System.nanoTime();
		mutexReasoner.lock();
		mutexRemoveObjPropB2Ind.lock();
		try{
			long t1 = System.nanoTime();
			OWLOntologyChange out = getOWLManipulator().removeObjectPropertyB2Individual( ind, prop,value);
			loggLockTime( t, t1);
			return out;
		} finally{
			mutexRemoveObjPropB2Ind.unlock();
			mutexReasoner.unlock();
		}
	}
	/**
	 * This method calls {@link OWLManipulator#removeObjectPropertyB2Individual(String, String, String)}
	 * in order to remove an object property from an individual.
	 * @param individualName the name of the individual which has the property to remove.
	 * @param propName the name of the property to remove.
	 * @param valueName the name of the value of the property to remove.
	 * @return the changes to be done in order to remove an object property from an individual. (see {@link OWLManipulator} for more info)
	 */
	public OWLOntologyChange removeObjectPropertyB2Individual( String individualName, String propName, String valueName){
		long t = System.nanoTime();
		mutexReasoner.lock();
		mutexRemoveObjPropB2Ind.lock();
		try{
			long t1 = System.nanoTime();
			OWLOntologyChange out = getOWLManipulator().removeObjectPropertyB2Individual( individualName, propName,valueName);
			loggLockTime( t, t1);
			return out;
		} finally{
			mutexRemoveObjPropB2Ind.unlock();
			mutexReasoner.unlock();
		}
	}

	/**
	 * This method calls {@link OWLManipulator#removeDataPropertyB2Individual(OWLNamedIndividual, OWLDataProperty, OWLLiteral)}
	 * in order to remove a data property from an individual.
	 * @param ind the individual which has the property to remove.
	 * @param prop the property to remove.
	 * @param value the value of the property to remove.
	 * @return the changes to be done in order to remove a data property from an individual. (see {@link OWLManipulator} for more info)
	 */
	public OWLOntologyChange removeDataPropertyB2Individual(OWLNamedIndividual ind, OWLDataProperty prop, OWLLiteral value){
		long t = System.nanoTime();
		mutexReasoner.lock();
		mutexRemoveDataPropB2Ind.lock();
		try{
			long t1 = System.nanoTime();
			OWLOntologyChange out = getOWLManipulator().removeDataPropertyB2Individual( ind, prop, value);
			loggLockTime( t, t1);
			return out;
		} finally{
			mutexRemoveDataPropB2Ind.unlock();
			mutexReasoner.unlock();
		}
	}
	/**
	 * This method calls {@link OWLManipulator#removeDataPropertyB2Individual(String, String, Object)}
	 * in order to remove a data property from an individual.
	 * @param individualName the name of the individual which has the property to remove.
	 * @param propertyName the name of the property to remove.
	 * @param value the name of the value of the property to remove.
	 * @return the changes to be done in order to remove a data property from an individual. (see {@link OWLManipulator} for more info)
	 */
	public OWLOntologyChange removeDataPropertyB2Individual( String individualName, String propertyName, Object value){
		long t = System.nanoTime();
		mutexReasoner.lock();
		mutexRemoveDataPropB2Ind.lock();
		try{
			long t1 = System.nanoTime();
			OWLOntologyChange out = getOWLManipulator().removeDataPropertyB2Individual( individualName, propertyName, value);
			loggLockTime( t, t1);
			return out;
		} finally{
			mutexRemoveDataPropB2Ind.unlock();
			mutexReasoner.unlock();
		}
	}


	/**
	 * This method calls {@link OWLManipulator#removeIndividualB2Class(OWLNamedIndividual, OWLClass)}
	 * in order to remove the assertion of an individual to be classified into a class.
	 * @param ind the individual which has to be removed from a class.
	 * @param cls the class from which the individual should be removed.
	 * @return the changes to be done in order to remove an individual from a class. (see {@link OWLManipulator} for more info)
	 */
	public OWLOntologyChange removeIndividualB2Class(OWLNamedIndividual ind, OWLClass cls){
		long t = System.nanoTime();
		mutexReasoner.lock();
		mutexRemoveIndB2Class.lock();
		try{
			long t1 = System.nanoTime();
			OWLOntologyChange out = getOWLManipulator().removeIndividualB2Class(ind, cls);
			loggLockTime( t, t1);
			return out;
		} finally{
			mutexRemoveIndB2Class.unlock();
			mutexReasoner.unlock();
		}
	}
	/**
	 * This method calls {@link OWLManipulator#removeIndividualB2Class(String, String)}
	 * in order to remove the assertion of an individual to be classified into a class.
	 * @param individualName the name of the individual which has to be removed from a class.
	 * @param className the name of the class from which the individual should be removed.
	 * @return the changes to be done in order to remove an individual from a class. (see {@link OWLManipulator} for more info)
	 */
	public OWLOntologyChange removeIndividualB2Class(String individualName, String className){
		long t = System.nanoTime();
		mutexReasoner.lock();
		mutexRemoveIndB2Class.lock();
		try{
			long t1 = System.nanoTime();
			OWLOntologyChange out = getOWLManipulator().removeIndividualB2Class( individualName, className);
			loggLockTime( t, t1);
			return out;
		} finally{
			mutexRemoveIndB2Class.unlock();
			mutexReasoner.unlock();
		}
	}

	/**
	 * This method calls {@link OWLManipulator#removeIndividual(OWLNamedIndividual)}
	 * in order to remove an individual from the ontology.
	 * @param ind the individual to be removed.
	 * @return the changes to be done in order to remove an individual from the ontology. (see {@link OWLManipulator} for more info)
	 */
	public List<OWLOntologyChange> removeIndividual( OWLNamedIndividual individual){ 
		long t = System.nanoTime();
		mutexReasoner.lock();
		mutexRemoveInd.lock();
		try{
			long t1 = System.nanoTime();
			List<OWLOntologyChange> out = getOWLManipulator().removeIndividual(individual);
			loggLockTime( t, t1);
			return out;
		} finally{
			mutexRemoveInd.unlock();
			mutexReasoner.unlock();
		}
	}
	/**
	 * This method calls {@link OWLManipulator#removeIndividual(OWLNamedIndividual)}
	 * in order to remove an individual from the ontology.
	 * @param indName the name of the individual to be removed.
	 * @return the changes to be done in order to remove an individual from the ontology. (see {@link OWLManipulator} for more info)
	 */
	public List<OWLOntologyChange> removeIndividual( String indName){ 
		long t = System.nanoTime();
		mutexReasoner.lock();
		mutexRemoveInd.lock();
		try{
			long t1 = System.nanoTime();
			List<OWLOntologyChange> out = getOWLManipulator().removeIndividual( indName);
			loggLockTime( t, t1);
			return out;
		} finally{
			mutexRemoveInd.unlock();
			mutexReasoner.unlock();
		}
	}
	/**
	 * This method calls {@link OWLManipulator#removeIndividual(Set)}
	 * in order to remove an individual from the ontology.
	 * @param individuals the set of individuals to be removed.
	 * @return the changes to be done in order to remove the set of individuals from the ontology. (see {@link OWLManipulator} for more info)
	 */
	public List<OWLOntologyChange> removeIndividual( Set< OWLNamedIndividual> individuals){ 
		long t = System.nanoTime();
		mutexReasoner.lock();
		mutexRemoveInd.lock();
		try{
			long t1 = System.nanoTime();
			List<OWLOntologyChange> out = getOWLManipulator().removeIndividual(individuals);
			loggLockTime( t, t1);
			return out;
		} finally{
			mutexRemoveInd.unlock();
			mutexReasoner.unlock();
		}
	}

	/** This method calls {@link OWLManipulator#removeClass(String)}
	 * in order to remove a class into the ontology.
	 * @param className the name of the class to be removed from the ontology.
	 * @return the changes to be done in order to remove a class from the ontology. (see {@link OWLManipulator} for more info)
	 */
	public OWLOntologyChange removeClass( String className){
		long t = System.nanoTime();
		mutexReasoner.lock();
		mutexRemoveClass.lock();
		try{
			long t1 = System.nanoTime();
			OWLOntologyChange out = getOWLManipulator().removeClass( className);
			loggLockTime( t, t1);
			return out;
		} finally{
			mutexRemoveClass.unlock();
			mutexReasoner.unlock();
		}
	}
	/**
	 * This method calls {@link OWLManipulator#removeClass(OWLClass)}
	 * in order to remove a class into the ontology.
	 * @param cls the class to be removed into the ontology.
	 * @return the changes to be done in order to remove a class from the ontology an individual into a class. (see {@link OWLManipulator} for more info)
	 */
	public OWLOntologyChange removeClass( OWLClass cls){
		long t = System.nanoTime();
		mutexReasoner.lock();
		mutexRemoveClass.lock();
		try{
			long t1 = System.nanoTime();
			OWLOntologyChange out = getOWLManipulator().removeClass( cls);
			loggLockTime( t, t1);
			return out;
		} finally{
			mutexRemoveClass.unlock();
			mutexReasoner.unlock();
		}
	}
	
	/**
	 * This method calls {@link OWLManipulator#removeSubClassOf(String, String)}
	 * in order to remove a class assertion to be a sub class of a specified entity.
	 * @param superClassName the name of the class in which remove the sub class.
	 * @param subClassName the name of the class to remove as sub class of the specified class.
	 * @return the changes to be done in order to remove a sub class assertion by specifying its super class. (see {@link OWLManipulator} for more info)
	 */
	public OWLOntologyChange removeSubClassOf( String superClassName, String subClassName){
		long t = System.nanoTime();
		mutexReasoner.lock();
		mutexRemoveSubClass.lock();
		try{
			long t1 = System.nanoTime();
			OWLOntologyChange out = getOWLManipulator().removeSubClassOf( superClassName, subClassName);
			loggLockTime( t, t1);
			return out;
		} finally{
			mutexRemoveSubClass.unlock();
			mutexReasoner.unlock();
		}
	}
	/**
	 * This method calls {@link OWLManipulator#removeSubClassOf(OWLClass, OWLClass)}
	 * in order to remove a class assertion to be a sub class of a specified entity.
	 * @param superClass the class in which remove the sub class.
	 * @param subClass the class to remove as sub class of the specified class.
	 * @return the changes to be done in order to remove a sub class assertion by specifying its super class. (see {@link OWLManipulator} for more info)
	 */
	public OWLOntologyChange removeSubClassOf( OWLClass supClass, OWLClass subClass){
		long t = System.nanoTime();
		mutexReasoner.lock();
		mutexRemoveSubClass.lock();
		try{
			long t1 = System.nanoTime();
			OWLOntologyChange out = getOWLManipulator().removeSubClassOf( supClass, subClass);
			loggLockTime( t, t1);
			return out;
		} finally{
			mutexRemoveSubClass.unlock();
			mutexReasoner.unlock();
		}
	}
	

	// ------------------------------------------------------------   methods for REPLACE entities
	/*
	 * This method calls {@link OWLManipulator#replaceDataPropertyB2Individual(OWLNamedIndividual, OWLDataProperty, Set, OWLLiteral)}
	 * in order to replace a data property of an individual with a new value.
	 * @param ind the individual from which replace the property
	 * @param prop the property to be replaces
	 * @param oldValue the set of old value of the property to be removed
	 * @param newValue the new value of the property to be added
	 * @return the changes to be done in order to replace a data property value attached to an individual. (see {@link OWLManipulator} for more info) 
	 
	public List<OWLOntologyChange> replaceDataProperty( OWLNamedIndividual ind, OWLDataProperty prop, Set< OWLLiteral> oldValue, OWLLiteral newValue){
		long t = System.nanoTime();
		mutexReasoner.lock();
		mutexReplaceDataProp.lock();
		try{
			long t1 = System.nanoTime();
			List<OWLOntologyChange> out = getOWLManipulator().replaceDataPropertyB2Individual( ind, prop, oldValue, newValue);
			loggLockTime( t, t1);
			return out;
		} finally{
			mutexReplaceDataProp.unlock();
			mutexReasoner.unlock();
		}
	}*/
	/**
	 * This method calls {@link OWLManipulator#replaceDataPropertyB2Individual(OWLNamedIndividual, OWLDataProperty, OWLLiteral, OWLLiteral)}
	 * in order to replace a data property of an individual with a new value.
	 * @param ind the individual from which replace the property
	 * @param prop the property to be replaces
	 * @param oldValue the old value of the property to be removed
	 * @param newValue the new value of the property to be added
	 * @return the changes to be done in order to replace a data property value attached to an individual. (see {@link OWLManipulator} for more info) 
	 */
	public List<OWLOntologyChange> replaceDataProperty( OWLNamedIndividual ind, OWLDataProperty prop, OWLLiteral oldValue, OWLLiteral newValue){
		long t = System.nanoTime();
		mutexReasoner.lock();
		mutexReplaceDataProp.lock();
		try{
			long t1 = System.nanoTime();
			List<OWLOntologyChange> out = getOWLManipulator().replaceDataPropertyB2Individual( ind, prop, oldValue, newValue);
			loggLockTime( t, t1);
			return out;
		} finally{
			mutexReplaceDataProp.unlock();
			mutexReasoner.unlock();
		}
	}


	/**
	 * This method calls {@link OWLManipulator#replaceObjectProperty(OWLNamedIndividual, OWLObjectProperty, OWLNamedIndividual, OWLNamedIndividual)}
	 * in order to replace an object property of an individual with a new value.
	 * @param ind the individual from which replace the property
	 * @param prop the property to be replaces
	 * @param oldValue the old value of the property to be removed
	 * @param newValue the new value of the property to be added
	 * @return the changes to be done in order to replace a object property value attached to an individual. (see {@link OWLManipulator} for more info) 
	 */
	public List<OWLOntologyChange> replaceObjectProperty( OWLNamedIndividual ind, OWLObjectProperty prop, OWLNamedIndividual oldValue, OWLNamedIndividual newValue){
		long t = System.nanoTime();
		mutexReasoner.lock();
		try{
			long t1 = System.nanoTime();
			List<OWLOntologyChange> out = getOWLManipulator().replaceObjectProperty(ind, prop, oldValue, newValue);
			loggLockTime( t, t1);
			return out;
		} finally{
			mutexReasoner.unlock();
		}
	}
	/**
	 * This method calls {@link OWLManipulator#replaceIndividualClass(OWLNamedIndividual, OWLClass, OWLClass)}
	 * in order to move an individual from a class to another.
	 * @param ind the individual from which replace the property
	 * @param oldValue the old class in which the individual would not be belonging to anymore.
	 * @param newValue the new class in which the individual will be belonging to.
	 * @return the changes to be done in order to replace a object property value attached to an individual. (see {@link OWLManipulator} for more info) 
	 */
	public List<OWLOntologyChange> replaceIndividualClass( OWLNamedIndividual ind,	OWLClass oldValue, OWLClass newValue){
		mutexReasoner.lock();
		long t = System.nanoTime();
		try{
			long t1 = System.nanoTime();
			List<OWLOntologyChange> out = getOWLManipulator().replaceIndividualClass(ind, oldValue, newValue);
			loggLockTime( t, t1);
			return out;
		} finally{
			mutexReasoner.unlock();
		}
	}


	/**
	 * This method calls {@link OWLManipulator#renameEntity(OWLEntity, IRI)}
	 * in order to rename an ontological entity with a new IRI.
	 * @param entity the ontological entity to be renamed.
	 * @param newIRI the new name for the ontological entity.
	 * @return the changes to be done in order to rename an ontological entity. (see {@link OWLManipulator} for more info)
	 */
	public List< OWLOntologyChange> renameEntity( OWLEntity entity, IRI newIRI){
		long t = System.nanoTime();
		mutexReasoner.lock();
		mutexRename.lock();
		try{
			long t1 = System.nanoTime();
			List<OWLOntologyChange> out = getOWLManipulator().renameEntity(entity, newIRI);
			loggLockTime( t, t1);
			return out;
		} finally{
			mutexRename.unlock();
			mutexReasoner.unlock();
		}
	}
	/**
	 * This method calls {@link OWLManipulator#renameEntity(String, String)}
	 * in order to rename an ontological entity with a new Name base on the {@link OWLLibrary#getIriOntologyPath()}.
	 * @param entity the ontological entity to be renamed.
	 * @param newName the new name for the ontological entity to be combined with the semantic ontology IRI.
	 * @return the changes to be done in order to rename an ontological entity. (see {@link OWLManipulator} for more info)
	 */
	public List< OWLOntologyChange> renameEntity( OWLEntity entity, String newName){
		long t = System.nanoTime();
		mutexReasoner.lock();
		mutexRename.lock();
		try{
			long t1 = System.nanoTime();
			List<OWLOntologyChange> out = getOWLManipulator().renameEntity(entity, newName);
			loggLockTime( t, t1);
			return out;
		} finally{
			mutexRename.unlock();
			mutexReasoner.unlock();
		}
	}
	
	// ------------------------------------------------------------   methods for DISJOINT individuals
	/**
	 * This method calls {@link OWLManipulator#makeDisjointIndividualName(Set)}
	 * in order to create (buffers and/or add) the disjoint axiom with respect to the individuals
	 * specified as input though their name. 
	 * @param individualNames the set of names of individuals to make disjointed.
	 * @return the changes to be done in order to add the disjoint individual axiom for all the inputs. (see {@link OWLManipulator} for more info)
	 */
	public OWLOntologyChange makeDisjointIndividualName( Set< String> individualNames){
		long t = System.nanoTime();
		mutexReasoner.lock();
		mutexAddDisjoinedInd.lock();
		try{
			long t1 = System.nanoTime();
			OWLOntologyChange out = getOWLManipulator().makeDisjointIndividualName( individualNames);
			loggLockTime( t, t1);
			return out;
		} finally{
			mutexAddDisjoinedInd.unlock();
			mutexReasoner.unlock();
		}
	}
	/**
	 * This method calls {@link OWLManipulator#makeDisjointIndividuals(Set)}
	 * in order to create (buffers and/or add) the disjoint axiom with respect to the 
	 * individuals set specified as input. 
	 * @param individuals the set of individuals to make disjointed.
	 * @return the changes to be done in order to add the disjoint individual axiom for all the inputs. (see {@link OWLManipulator} for more info)
	 */
	public OWLOntologyChange makeDisjointIndividuals( Set< OWLNamedIndividual> individuals){
		long t = System.nanoTime();
		mutexReasoner.lock();
		mutexAddDisjoinedInd.lock();
		try{
			long t1 = System.nanoTime();
			OWLOntologyChange out = getOWLManipulator().makeDisjointIndividuals( individuals);
			loggLockTime( t, t1);
			return out;
		} finally{
			mutexAddDisjoinedInd.unlock();
			mutexReasoner.unlock();
		}
	}
	
	/**
	 * This method calls {@link OWLManipulator#removeDisjointIndividualName(Set)}
	 * in order to create (buffers and/or remove) the disjoint axioms (if their exists) with respect to the individuals
	 * specified as input though their name. 
	 * @param individualNames the set of names of individuals to make not disjointed anymore.
	 * @return the changes to be done in order to remove the disjoint individual axiom for all the inputs. (see {@link OWLManipulator} for more info)
	 */
	public OWLOntologyChange removeDisjointIndividualName( Set< String> individualNames){
		long t = System.nanoTime();
		mutexReasoner.lock();
		mutexRemoveDisjoinedInd.lock();
		try{
			long t1 = System.nanoTime();
			OWLOntologyChange out = getOWLManipulator().removeDisjointIndividualName( individualNames);
			loggLockTime( t, t1);
			return out;
		} finally{
			mutexRemoveDisjoinedInd.unlock();
			mutexReasoner.unlock();
		}
	}
	/**
	 * This method calls {@link OWLManipulator#removeDisjointIndividuals(Set)}
	 * in order to create (buffers and/or remove) the disjoint axiom (if their exists) with respect to the 
	 * individuals set specified as input. 
	 * @param individuals the set of individuals to make not disjointed anymore.
	 * @return the changes to be done in order to remove the disjoint individual axiom for all the inputs. (see {@link OWLManipulator} for more info)
	 */
	public OWLOntologyChange removeDisjointIndividuals( Set< OWLNamedIndividual> individuals){
		long t = System.nanoTime();
		mutexReasoner.lock();
		mutexRemoveDisjoinedInd.lock();
		try{
			long t1 = System.nanoTime();
			OWLOntologyChange out = getOWLManipulator().removeDisjointIndividuals( individuals);
			loggLockTime( t, t1);
			return out;
		} finally{
			mutexRemoveDisjoinedInd.unlock();
			mutexReasoner.unlock();
		}	
	}

	/**
	 * This method calls {@link OWLManipulator#makeDisjointClassName(Set)}
	 * in order to create (buffers and/or add) the disjoint axiom with respect to the classes
	 * specified as input though their name. 
	 * @param classesName the set of names of class to make disjointed.
	 * @return the changes to be done in order to add the disjoint classes axiom for all the inputs. (see {@link OWLManipulator} for more info)
	 */
	public OWLOntologyChange makeDisjointClassName( Set< String> classesName){
		long t = System.nanoTime();
		mutexReasoner.lock();
		mutexAddDisjoinedCls.lock();
		try{
			long t1 = System.nanoTime();
			OWLOntologyChange out = getOWLManipulator().makeDisjointClassName( classesName);
			loggLockTime( t, t1);
			return out;
		} finally{
			mutexAddDisjoinedCls.unlock();
			mutexReasoner.unlock();
		}	
	}
	/**
	 * This method calls {@link OWLManipulator#makeDisjointClasses(Set)}
	 * in order to create (buffers and/or add) the disjoint axiom with respect to the 
	 * classes set specified as input. 
	 * @param classes the set of classes to make disjointed.
	 * @return the changes to be done in order to add the disjoint classes axiom for all the inputs. (see {@link OWLManipulator} for more info)
	 */
	public OWLOntologyChange makeDisjointClasses( Set< OWLClass> classes){
		long t = System.nanoTime();
		mutexReasoner.lock();
		mutexAddDisjoinedCls.lock();
		try{
			long t1 = System.nanoTime();
			OWLOntologyChange out = getOWLManipulator().makeDisjointClasses( classes);
			loggLockTime( t, t1);
			return out;
		} finally{
			mutexAddDisjoinedCls.unlock();
			mutexReasoner.unlock();
		}	
	}
	
	/**
	 * This method calls {@link OWLManipulator#removeDisjointClassName(Set)}
	 * in order to create (buffers and/or remove) the disjoint axioms (if their exists) with respect to the classes
	 * specified as input though their name. 
	 * @param classesName the set of names of class to make not disjointed anymore.
	 * @return the changes to be done in order to remove the disjoint class axiom for all the inputs. (see {@link OWLManipulator} for more info)
	 */
	public OWLOntologyChange removeDisjointClassName( Set< String> classesName){
		long t = System.nanoTime();
		mutexReasoner.lock();
		mutexRemoveDisjoinedCls.lock();
		try{
			long t1 = System.nanoTime();
			OWLOntologyChange out = getOWLManipulator().removeDisjointClassName( classesName);
			loggLockTime( t, t1);
			return out;
		} finally{
			mutexRemoveDisjoinedCls.unlock();
			mutexReasoner.unlock();
		}	
	}
	/**
	 * This method calls {@link OWLManipulator#removeDisjointClasses(Set)}
	 * in order to create (buffers and/or remove) the disjoint axiom (if their exists) with respect to the 
	 * classes set specified as input. 
	 * @param classes the set of class to make not disjointed anymore.
	 * @return the changes to be done in order to remove the disjoint class axiom for all the inputs. (see {@link OWLManipulator} for more info)
	 */	
	public OWLOntologyChange removeDisjointClasses( Set< OWLClass> classes){
		long t = System.nanoTime();
		mutexReasoner.lock();
		mutexRemoveDisjoinedCls.lock();
		try{
			long t1 = System.nanoTime();
			OWLOntologyChange out = getOWLManipulator().removeDisjointClasses( classes);
			loggLockTime( t, t1);
			return out;
		} finally{
			mutexRemoveDisjoinedCls.unlock();
			mutexReasoner.unlock();
		}	
	}

	
	// for logging time (how much has been waiting for the mutex) and (how much has been waiting to do the manipulation)
	private void loggLockTime( long initialTime, long unlockingTime){
		Double time = (unlockingTime - initialTime) / 1000000D;
		Double time2 = (System.nanoTime() - unlockingTime) / 1000000D;
		//if( time > 400 || time2 > 400){
			StackTraceElement[] trace = Thread.currentThread().getStackTrace();
			String method = "", caller = "";
			if( trace.length >= 3){
				method = trace[2].getMethodName();
				caller = trace[ trace.length - 1].getMethodName();
			}
			logger.addDebugString( this.getReferenceName() + " locked on " + method + " for " + time + " [ns] (top stack trace caller: " + caller + ")");
			logger.addDebugString( this.getReferenceName() + " spent " + time2 + "[ns] waiting in OWLLibrary");
		//}
	}

	// [[[[[[[[[[[[[[[[[[[[[[   METHOD TO CALL REASONING (thread safe)   ]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]
	/** 
	 * This method just synchronises the call to the reasoner (performed by {@link OWLReferences}) with respect to 
	 * {@link #mutexReasoner} by making this call thread safe with respect to 
	 * the manipulations of (performed by {@link OWLManipulator}) and queries
	 * (performed by {@link OWLEnquirer}). 
	 * @see aMOR.owlInterface.OWLReferencesInterface#synchroniseReasoner()
	 */
	@Override
	public synchronized void synchroniseReasoner() {
		mutexReasoner.lock();
		try{
			super.synchroniseReasoner();
		} finally{
			mutexReasoner.unlock();
		}
	}
	
	
	// [[[[[[[[[[[[[[[[[[[[[[   METHODS TO SAVE (exportable) ONTOLOGY   ]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]
	/**
	 * It will save an ontology into a file. The files path is 
	 * retrieved from the OWLReferences class using: 
	 * {@code ontoRef.getIriFilePath();}. Note that this procedure
	 * may replace an already existing file. The exporting of the 
	 * asserted relation is done by: {@link InferedAxiomExporter#exportOntology(OWLReferences)}
	 * and my be an expensive procedure.
	 * @param exportInf if {@code true} export all reasoner inferences in the returned ontology References.
	 * Otherwise it just call {@link #saveOntology()}
	 */
	public synchronized void saveOntology( boolean exportInf) {
		OWLReferences ontoRef = this;
		try {
			if( exportInf)
				ontoRef = InferedAxiomExporter.exportOntology( ontoRef);
			ontoRef.saveOntology();
		} catch( org.semanticweb.owlapi.reasoner.InconsistentOntologyException e){
			this.loggInconsistency();
		}
	}


	/**
	 * It will save an ontology into a file. The files path is 
	 * given as input parameter, and this method does not update: 
	 * {@code ontoRef.getIriFilePath();}. Note that this procedure
	 * may replace an already existing file. The exporting of the 
	 * asserted relation is done by: {@link InferedAxiomExporter#exportOntology(OWLReferences)}
	 * and my be an expensive procedure.
	 * @param exportInf if {@code true} export all reasoner inferences in the returned ontology References.
	 * Otherwise it just call {@link #saveOntology(String)}
	 * @param filePath directory in which save the ontology.
	 */
	public synchronized void saveOntology( boolean exportInf, String filePath) {
		OWLReferences ontoRef = this;
		try {
			if( exportInf)
				ontoRef = InferedAxiomExporter.exportOntology( ontoRef);
			ontoRef.saveOntology( filePath);
		} catch( org.semanticweb.owlapi.reasoner.InconsistentOntologyException e){
			this.loggInconsistency();
		}
	}
}
