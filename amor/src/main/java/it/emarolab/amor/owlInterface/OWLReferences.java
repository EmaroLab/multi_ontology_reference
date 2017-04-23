package it.emarolab.amor.owlInterface;

import it.emarolab.amor.owlDebugger.Logger;
import it.emarolab.amor.owlDebugger.Logger.LoggerFlag;
import it.emarolab.amor.owlInterface.OWLEnquirer.DataPropertyRelations;
import it.emarolab.amor.owlInterface.OWLEnquirer.ObjectPropertyRelations;
import org.apache.jena.query.QuerySolution;
import org.semanticweb.owlapi.model.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

// TODO : serialisation
// TODO : add more reasoners
// TODO : better logging
// TODO : make an abstract class interface to be implemented

/**
 * <div style="text-align:center;"><small>
 * <b>Project</b>:    aMOR <br>
 * <b>File</b>:       it.emarolab.amor.owlInterface.OWLReferences <br>
 * <b>Licence</b>:    GNU GENERAL PUBLIC LICENSE. Version 3, 29 June 2007 <br>
 * <b>Author</b>:     Buoncompagni Luca (luca.buoncompagni@edu.unige.it) <br>
 * <b>affiliation</b>: DIBRIS, EMAROLab, University of Genoa. <br>
 * <b>date</b>:       Feb 10, 2016 <br>
 * </small></div>
 *
 * <p>
 *     This class is the top layer, multi thread safe, interface for:
 *     loading, opening or create and initialise an ontology (see {@link OWLLibrary} and {@link OWLReferencesInterface}).
 *     As well as mainpulating the structure (see {@link OWLManipulator}) and quering the resoner.
 *     (see {@link OWLManipulator}).
 * </p>
 *
 * @version 2.1
 */
public class OWLReferences extends OWLReferencesInterface{

	/**
	 * This object is used to log information about the instances of this class.
	 * The logs can be activated by setting the flag: {@link LoggerFlag#LOG_ONTOLOGY_REFERENCE}
	 */
	private Logger logger = new Logger( this, LoggerFlag.getLogOntologyReference());

	//  [[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[ SUPER CLASS CONSTRUCTORS ]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]
    // ##################################   to ontology enquirer !!!!!!!!!!!!!
    // mutex for assure thread safe behaviour
    private Lock mutexIndividualB2Class = new ReentrantLock();
    private Lock mutexDataPropB2Ind = new ReentrantLock();

    // [[[[[[[[[[[[[[[[[[[[[[   METHODS TO QUERY THE ONTOLOGY   ]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]
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
    private Lock mutexSPARQL = new ReentrantLock();
    private Lock mutexClassDefinition = new ReentrantLock();
    private Lock mutexInverseProperty = new ReentrantLock();
    private Lock mutexBottomType = new ReentrantLock();
    // ##################################   to ontology manipulator !!!!!!!!!!!!!
    // mutex for assure thread safe behaviour
    private Lock mutexReasoner = new ReentrantLock();
    private Lock mutexAddObjPropB2Ind = new ReentrantLock();
    private Lock mutexAddDataPropB2Ind = new ReentrantLock();
    private Lock mutexAddIndB2Class = new ReentrantLock();
    private Lock mutexAddInd = new ReentrantLock();
    private Lock mutexAddClass = new ReentrantLock();
    private Lock mutexAddSubClass = new ReentrantLock();
    private Lock mutexAddSubDataProperty = new ReentrantLock();
    private Lock mutexAddSubObjectProperty = new ReentrantLock();
    private Lock mutexAddRemovingClassDefinition = new ReentrantLock();
    private Lock mutexAddCardinalityData = new ReentrantLock();
    private Lock mutexConvertEquivalentClass = new ReentrantLock();
    private Lock mutexRemoveClass = new ReentrantLock();
    private Lock mutexRemoveSubClass = new ReentrantLock();
    private Lock mutexRemoveSubDataProperty = new ReentrantLock();
    private Lock mutexRemoveSubObjectProperty = new ReentrantLock();
    private Lock mutexRemoveObjPropB2Ind = new ReentrantLock();
    private Lock mutexRemoveDataPropB2Ind = new ReentrantLock();
    private Lock mutexRemoveIndB2Class = new ReentrantLock();
    private Lock mutexRemoveInd = new ReentrantLock();
    private Lock mutexReplaceDataProp = new ReentrantLock();
    private Lock mutexRename = new ReentrantLock();
    private Lock mutexAddDisjointedInd = new ReentrantLock();
    private Lock mutexRemoveDisjointedInd = new ReentrantLock();
    private Lock mutexAddDisjointedCls = new ReentrantLock();
    private Lock mutexRemoveDisjointedCls = new ReentrantLock();

	/**
	 * This constructor just calls the super class constructor: {@link OWLReferencesInterface#OWLReferencesInterface(String, String, String, Boolean, Integer)}
	 * @param referenceName the unique identifier of this ontology references. This is the key with which this instance
	 * is stored in the system map {@link OWLReferencesContainer#allReferences}
	 * @param filePath the file path (or URL) to the ontology.
     * @param ontologyPath the IRI path of the ontology.
     * @param bufferingReasoner {@code true} if the reasoner have to evaluate changes to the ontology as soon as their have been performed.
	 * {@code false} if the reasoner should evaluate all the changes of the ontology only if the method {@link #synchronizeReasoner()} gets called.
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
     * If this parameter is {@code null}, default reasoner value {@link #REASONER_DEFAULT} is used.
	 * The values of this parameter have to be in the range: [{@link OWLLibrary#REASONER_QUALIFIER_PELLET},
     * {@link OWLLibrary#REASONER_QUALIFIER_HERMIT}, {@link OWLLibrary#REASONER_QUALIFIER_SNOROCKET} or {@link OWLLibrary#REASONER_QUALIFIER_FACT}].
     * @param bufferingReasoner {@code true} if the reasoner have to evaluate changes to the ontology as soon as their have been performed.
	 * {@code false} if the reasoner should evaluate all the changes of the ontology only if the method {@link #synchronizeReasoner()} gets called.
	 * @param command specifying if the ontology should be created, loaded from file or from web. Possible value of {@code commands} are:
     * {@link OWLReferencesContainer#COMMAND_CREATE}, {@link OWLReferencesContainer#COMMAND_LOAD_FILE} or
     * {@link OWLReferencesContainer#COMMAND_LOAD_WEB}.
	 */
	protected OWLReferences(String referenceName, String filePath, String ontologyPath, String reasonerFactory, Boolean bufferingReasoner, Integer command) {
		super(referenceName, filePath, ontologyPath, reasonerFactory, bufferingReasoner, command);
	}
	
	/**
     * This method searches for all the individuals in the root class {@link OWLDataFactory#getOWLThing()}.
     * It looks for defined semantic entities as well as for inferred
     * axioms (given by the reasoner). In order to do so it calls:
	 * {@link OWLEnquirer#getIndividualB2Thing()}
	 * @return the set of all the individuals into the root ontology class.
	 */
	public Set<OWLNamedIndividual> getIndividualB2Thing(){
		List< Lock> mutexes = getMutexes( mutexReasoner, mutexIndividualB2Class);
		return new OWLReferencesCaller< Set< OWLNamedIndividual>>(  mutexes, this) {
			@Override
			protected Set< OWLNamedIndividual> performSynchronisedCall() {
				return getEnquirer().getIndividualB2Thing();
			}
		}.call();
	}

	/**
     * This method searches for one individual in the root class {@link OWLDataFactory#getOWLThing()}.
     * It looks for defined semantic entities as well as for inferred
     * axioms (given by the reasoner). In order to do so it calls:
	 * {@link OWLEnquirer#getOnlyIndividualB2Thing()}
	 * @return one individual into the root ontology class.
	 */
	public OWLNamedIndividual getOnlyIndividualB2Thing(){
		List< Lock> mutexes = getMutexes( mutexReasoner, mutexIndividualB2Class);
		return new OWLReferencesCaller< OWLNamedIndividual>(  mutexes, this) {
			@Override
			protected OWLNamedIndividual performSynchronisedCall() {
				return getEnquirer().getOnlyIndividualB2Thing();
			}
		}.call();
	}
	
	/**
     * This method searches for individuals in a specified class.
     * It looks for defined semantic entities as well as for inferred
     * axioms (given by the reasoner). In order to do so it calls:
	 * {@link OWLEnquirer#getIndividualB2Class(String)}
	 * @param className the name of the class from which to retrieve the individuals
	 * @return the set of individuals into the given class.
	 */
	public Set<OWLNamedIndividual> getIndividualB2Class( String className){
		List< Lock> mutexes = getMutexes( mutexReasoner, mutexIndividualB2Class);
		return new OWLReferencesCaller< Set< OWLNamedIndividual>>(  mutexes, this) {
			@Override
			protected Set< OWLNamedIndividual> performSynchronisedCall() {
				return getEnquirer().getIndividualB2Class( className);
			}
		}.call();
	}

	/**
     * This method searches for individuals in a specified class.
     * It looks for defined semantic entities as well as for inferred
     * axioms (given by the reasoner). In order to do so it calls:
	 * {@link OWLEnquirer#getIndividualB2Class(OWLClass)}
	 * @param ontoClass the class from which to retrieve the individuals
	 * @return the set of individuals into the given class.
	 */
    public Set<OWLNamedIndividual> getIndividualB2Class(OWLClass ontoClass) {
        List< Lock> mutexes = getMutexes( mutexReasoner, mutexIndividualB2Class);
		return new OWLReferencesCaller< Set< OWLNamedIndividual>>(  mutexes, this) {
			@Override
			protected Set< OWLNamedIndividual> performSynchronisedCall() {
				return getEnquirer().getIndividualB2Class( ontoClass);
			}
		}.call();
	}

    /**
     * This method searches for individuals in a specified class.
     * It looks for defined semantic entities as well as for inferred
     * axioms (given by the reasoner). In order to do so it calls:
	 * {@link OWLEnquirer#getOnlyIndividualB2Class(String)};
	 * @param className the name of the class from which to retrieve the individuals
	 * @return an individuals into the given class.
	 */
    public OWLNamedIndividual getOnlyIndividualB2Class(String className) {
        List< Lock> mutexes = getMutexes( mutexReasoner, mutexIndividualB2Class);
		return new OWLReferencesCaller< OWLNamedIndividual>(  mutexes, this) {
			@Override
			protected OWLNamedIndividual performSynchronisedCall() {
				return getEnquirer().getOnlyIndividualB2Class( className);
			}
		}.call();
	}

	/**
     * This method searches for individuals in a specified class.
     * It looks for defined semantic entities as well as for inferred
     * axioms (given by the reasoner). In order to do so it calls:
	 * {@link OWLEnquirer#getOnlyIndividualB2Class(OWLClass)};
	 * @param ontoClass the class from which to retrieve the individuals
	 * @return an individuals into the given class.
	 */
	public OWLNamedIndividual getOnlyIndividualB2Class( OWLClass ontoClass){
		List< Lock> mutexes = getMutexes( mutexReasoner, mutexIndividualB2Class);
		return new OWLReferencesCaller< OWLNamedIndividual>(  mutexes, this) {
			@Override
			protected OWLNamedIndividual performSynchronisedCall() {
				return getEnquirer().getOnlyIndividualB2Class(ontoClass);
			}
		}.call();
	}

	/**
     * This method searches for the classes in which an individuals is belonging to.
     * It looks for defined semantic entities as well as for inferred
     * axioms (given by the reasoner). In order to do so it calls:
	 * {@link OWLEnquirer#getIndividualClasses(OWLNamedIndividual)};
	 * @param individual the individual from which query its types.
	 * @return the set of classes in which the given individual is belonging to.
	 */
	public Set< OWLClass> getIndividualClasses( OWLNamedIndividual individual){
		List< Lock> mutexes = getMutexes( mutexReasoner, mutexIndivClasses);
		return new OWLReferencesCaller< Set< OWLClass>>(  mutexes, this) {
			@Override
			protected Set< OWLClass> performSynchronisedCall() {
				return getEnquirer().getIndividualClasses( individual);
			}
		}.call();
	}

	/**
     * This method searches for the classes in which an individuals is belonging to.
     * It looks for defined semantic entities as well as for inferred
     * axioms (given by the reasoner). In order to do so it calls:
	 * {@link OWLEnquirer#getIndividualClasses( String)};
	 * @param individual the individual from which query its types.
	 * @return the set of classes in which the given individual is belonging to.
	 */
	public Set< OWLClass> getIndividualClasses( String individual){
		List< Lock> mutexes = getMutexes( mutexReasoner, mutexIndivClasses);
		return new OWLReferencesCaller< Set< OWLClass>>(  mutexes, this) {
			@Override
			protected Set< OWLClass> performSynchronisedCall() {
				return getEnquirer().getIndividualClasses( individual);
			}
		}.call();
	}

	/**
     * This method searches for a class in which an individuals is belonging to.
     * It looks for defined semantic entities as well as for inferred
     * axioms (given by the reasoner). In order to do so it calls:
	 * {@link OWLEnquirer#getOnlyIndividualClasses(OWLNamedIndividual)};
	 * @param individual the individual from which query its types.
	 * @return the set of classes in which the given individual is belonging to.
	 */
	public OWLClass getOnlyIndividualClasses( OWLNamedIndividual individual){
		List< Lock> mutexes = getMutexes( mutexReasoner, mutexIndivClasses);
		return new OWLReferencesCaller< OWLClass>(  mutexes, this) {
			@Override
			protected OWLClass performSynchronisedCall() {
				return getEnquirer().getOnlyIndividualClasses( individual);
			}
		}.call();
	}

	/**
     * This method searches for a class in which an individuals is belonging to.
     * It looks for defined semantic entities as well as for inferred
     * axioms (given by the reasoner). In order to do so it calls:
	 * {@link OWLEnquirer#getOnlyIndividualClasses( String)};
	 * @param individual the individual from which query its types.
	 * @return the set of classes in which the given individual is belonging to.
	 */
	public OWLClass getOnlyIndividualClasses( String individual){
		List< Lock> mutexes = getMutexes( mutexReasoner, mutexIndivClasses);
		return new OWLReferencesCaller< OWLClass>(  mutexes, this) {
			@Override
			protected OWLClass performSynchronisedCall() {
				return getEnquirer().getOnlyIndividualClasses( individual);
			}
		}.call();
	}

	/**
     * This method searches for the data properties assigned to a specified individual.
     * It looks for defined semantic entities as well as for inferred
     * axioms (given by the reasoner). In order to do so it calls:
	 * {@link OWLEnquirer#getDataPropertyB2Individual(String, String)};
	 * @param individualName the name of the individual from which query its types.
	 * @param propertyName the name of the property to search in the individual properties.
	 * @return the set of values of the specified data property assigned to an individual.
	 */
	public Set<OWLLiteral> getDataPropertyB2Individual( String individualName, String propertyName){
		List< Lock> mutexes = getMutexes( mutexReasoner, mutexDataPropB2Ind);
		return new OWLReferencesCaller< Set< OWLLiteral>>(  mutexes, this) {
			@Override
			protected Set< OWLLiteral> performSynchronisedCall() {
				return getEnquirer().getDataPropertyB2Individual( individualName, propertyName);
			}
		}.call();
	}

	/**
     * This method searches for the data properties assigned to a specified individual.
     * It looks for defined semantic entities as well as for inferred
     * axioms (given by the reasoner). In order to do so it calls:
	 * {@link OWLEnquirer#getDataPropertyB2Individual(OWLNamedIndividual, OWLDataProperty)};
	 * @param individual the individual from which query its types.
	 * @param property the property to search in the individual properties.
	 * @return the set of values of the specified data property assigned to an individual.
	 */
	public Set<OWLLiteral> getDataPropertyB2Individual( OWLNamedIndividual individual, OWLDataProperty property){
		List< Lock> mutexes = getMutexes( mutexReasoner, mutexDataPropB2Ind);
		return new OWLReferencesCaller< Set< OWLLiteral>>(  mutexes, this) {
			@Override
			protected Set< OWLLiteral> performSynchronisedCall() {
				return getEnquirer().getDataPropertyB2Individual( individual, property);
			}
		}.call();
	}

	/**
     * This method searches for a data property assigned to a specified individual.
     * It looks for defined semantic entities as well as for inferred
     * axioms (given by the reasoner). In order to do so it calls:
	 * {@link OWLEnquirer#getOnlyDataPropertyB2Individual(String, String)};
	 * @param individualName the name of the individual from which query its types.
	 * @param propertyName the name of the property to search in the individual properties.
	 * @return a value of the specified data property assigned to an individual.
	 */
	public OWLLiteral getOnlyDataPropertyB2Individual( String individualName, String propertyName){
		List< Lock> mutexes = getMutexes( mutexReasoner, mutexDataPropB2Ind);
		return new OWLReferencesCaller< OWLLiteral>(  mutexes, this) {
			@Override
			protected OWLLiteral performSynchronisedCall() {
				return getEnquirer().getOnlyDataPropertyB2Individual( individualName, propertyName);
			}
		}.call();
	}

	/**
     * This method searches for a data property assigned to a specified individual.
     * It looks for defined semantic entities as well as for inferred
     * axioms (given by the reasoner). In order to do so it calls:
	 * {@link OWLEnquirer#getOnlyDataPropertyB2Individual(OWLNamedIndividual, OWLDataProperty)};
	 * @param individual the individual from which query its types.
	 * @param property the property to search in the individual properties.
	 * @return a value of the specified data property assigned to an individual.
	 */
	public OWLLiteral getOnlyDataPropertyB2Individual( OWLNamedIndividual individual, OWLDataProperty property){
		List< Lock> mutexes = getMutexes( mutexReasoner, mutexDataPropB2Ind);
		return new OWLReferencesCaller< OWLLiteral>(  mutexes, this) {
			@Override
			protected OWLLiteral performSynchronisedCall() {
				return getEnquirer().getOnlyDataPropertyB2Individual( individual, property);
			}
		}.call();
	}

	/**
     * This method searches for the all the data properties assigned to a specified individual.
     * It looks for defined semantic entities as well as for inferred
     * axioms (given by the reasoner). In order to do so it calls:
	 * {@link OWLEnquirer#getDataPropertyB2Individual(OWLNamedIndividual)}
	 * @param individual the individual from which query its data properties.
	 * @return the set of a container of all the data properties with relative values.
	 */
	public Set<DataPropertyRelations> getDataPropertyB2Individual( OWLNamedIndividual individual){
		List< Lock> mutexes = getMutexes( mutexReasoner, mutexAllDataPropB2Ind);
		return new OWLReferencesCaller< Set<DataPropertyRelations>>(  mutexes, this) {
			@Override
			protected Set<DataPropertyRelations> performSynchronisedCall() {
				return getEnquirer().getDataPropertyB2Individual( individual);
			}
		}.call();
	}

	/**
     * This method searches for the all the data properties assigned to a specified individual.
     * It looks for defined semantic entities as well as for inferred
     * axioms (given by the reasoner). In order to do so it calls:
	 * {@link OWLEnquirer#getDataPropertyB2Individual(String)}
	 * @param individualName the name of the individual from which query its data properties.
	 * @return the set of a container of all the data properties with relative values.
	 */
	public Set<DataPropertyRelations> getDataPropertyB2Individual( String individualName){
		List< Lock> mutexes = getMutexes( mutexReasoner, mutexAllObjPropB2Ind);
		return new OWLReferencesCaller< Set<DataPropertyRelations>>(  mutexes, this) {
			@Override
			protected Set<DataPropertyRelations> performSynchronisedCall() {
				return getEnquirer().getDataPropertyB2Individual( individualName);
			}
		}.call();
	}

	/**
     * This method searches for the object properties assigned to a specified individual.
     * It looks for defined semantic entities as well as for inferred
     * axioms (given by the reasoner). In order to do so it calls:
	 * {@link OWLEnquirer#getObjectPropertyB2Individual(String, String)};
	 * @param individualName the name of the individual from which query its types.
	 * @param propertyName the name of the property to search in the individual properties.
	 * @return the set of values of the specified object property assigned to an individual.
	 */
	public Set<OWLNamedIndividual> getObjectPropertyB2Individual( String individualName, String propertyName){
		List< Lock> mutexes = getMutexes( mutexReasoner, mutexObjPropB2Ind);
		return new OWLReferencesCaller<  Set<OWLNamedIndividual>>(  mutexes, this) {
			@Override
			protected  Set<OWLNamedIndividual> performSynchronisedCall() {
				return getEnquirer().getObjectPropertyB2Individual( individualName, propertyName);
			}
		}.call();
	}

	/**
     * This method searches for the object properties assigned to a specified individual.
     * It looks for defined semantic entities as well as for inferred
     * axioms (given by the reasoner). In order to do so it calls:
	 * {@link OWLEnquirer#getObjectPropertyB2Individual(OWLNamedIndividual, OWLObjectProperty)};
	 * @param individual the individual from which query its types.
	 * @param property the property to search in the individual properties.
	 * @return the set of values of the specified object property assigned to an individual.
	 */
	public Set<OWLNamedIndividual> getObjectPropertyB2Individual( OWLNamedIndividual individual, OWLObjectProperty property){
		List< Lock> mutexes = getMutexes( mutexReasoner, mutexObjPropB2Ind);
		return new OWLReferencesCaller<  Set<OWLNamedIndividual>>(  mutexes, this) {
			@Override
			protected  Set<OWLNamedIndividual> performSynchronisedCall() {
				return getEnquirer().getObjectPropertyB2Individual( individual, property);
			}
		}.call();
	}

	/**
     * This method searches for the object properties assigned to a specified individual.
     * It looks for defined semantic entities as well as for inferred
     * axioms (given by the reasoner). In order to do so it calls:
	 * {@link OWLEnquirer#getOnlyObjectPropertyB2Individual(String, String)};
	 * @param individualName the name of the individual from which query its types.
	 * @param propertyName the name of the property to search in the individual properties.
	 * @return a value of the specified object property assigned to an individual.
	 */
	public OWLNamedIndividual getOnlyObjectPropertyB2Individual( String individualName, String propertyName){
		List< Lock> mutexes = getMutexes( mutexReasoner, mutexObjPropB2Ind);
		return new OWLReferencesCaller< OWLNamedIndividual>(  mutexes, this) {
			@Override
			protected OWLNamedIndividual performSynchronisedCall() {
				return getEnquirer().getOnlyObjectPropertyB2Individual( individualName, propertyName);
			}
		}.call();
	}

	/**
     * This method searches for the object properties assigned to a specified individual.
     * It looks for defined semantic entities as well as for inferred
     * axioms (given by the reasoner). In order to do so it calls:
	 * {@link OWLEnquirer#getOnlyObjectPropertyB2Individual(OWLNamedIndividual, OWLObjectProperty)};
	 * @param individual the individual from which query its types.
	 * @param property the property to search in the individual properties.
	 * @return a value of the specified object property assigned to an individual.
	 */
	public OWLNamedIndividual getOnlyObjectPropertyB2Individual( OWLNamedIndividual individual, OWLObjectProperty property){
		List< Lock> mutexes = getMutexes( mutexReasoner, mutexObjPropB2Ind);
		return new OWLReferencesCaller< OWLNamedIndividual>(  mutexes, this) {
			@Override
			protected OWLNamedIndividual performSynchronisedCall() {
				return getEnquirer().getOnlyObjectPropertyB2Individual( individual, property);
			}
		}.call();
	}

	/**
     * This method searches for the all the object properties assigned to a specified individual.
     * It looks for defined semantic entities as well as for inferred
     * axioms (given by the reasoner). In order to do so it calls:
	 * {@link OWLEnquirer#getObjectPropertyB2Individual(OWLNamedIndividual)}
	 * @param individual the individual from which query its object properties.
	 * @return the set of a container of all the object properties with relative values.
	 */
	public Set<ObjectPropertyRelations> getObjectPropertyB2Individual(OWLNamedIndividual individual){
		List< Lock> mutexes = getMutexes( mutexReasoner, mutexAllObjPropB2Ind);
		return new OWLReferencesCaller< Set<ObjectPropertyRelations>>(  mutexes, this) {
			@Override
			protected Set<ObjectPropertyRelations> performSynchronisedCall() {
				return getEnquirer().getObjectPropertyB2Individual( individual);
			}
		}.call();
	}

	/**
     * This method searches for the all the object properties assigned to a specified individual.
     * It looks for defined semantic entities as well as for inferred
     * axioms (given by the reasoner). In order to do so it calls:
	 * {@link OWLEnquirer#getObjectPropertyB2Individual(String)}
	 * @param individualName the name of the individual from which query its object properties.
	 * @return the set of a container of all the object properties with relative values.
	 */
	public Set<ObjectPropertyRelations> getObjectPropertyB2Individual(String individualName){
		List< Lock> mutexes = getMutexes( mutexReasoner, mutexAllObjPropB2Ind);
		return new OWLReferencesCaller< Set<ObjectPropertyRelations>>(  mutexes, this) {
			@Override
			protected Set<ObjectPropertyRelations> performSynchronisedCall() {
				return getEnquirer().getObjectPropertyB2Individual( individualName);
			}
		}.call();
	}


    // [[[[[[[[[[[[[[[[[[[[   METHODS TO MANIPULATE THE ONTOLOGY   ]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]
    // it uses default ontology manipulator (not buffering)!!!!
    // you can change this by calling: "this.setManipulatorChangeBuffering( true);"
    // but then remember to call "this.applyManipulatorChanges();" to actually perform the ontology changes

	/**
     * This method searches for the sub data properties of a specified property.
     * It looks for defined semantic entities as well as for inferred
     * axioms (given by the reasoner). In order to do so it calls:
	 * {@link OWLEnquirer#getSubDataPropertyOf(String)};
	 * @param propName the name of the data property from which to retrieve its sub-properties.
	 * @return the set of all the data properties that are sub-properties of the specified parameter.
	 */
	public Set<OWLDataProperty> getSubDataPropertyOf( String propName){
		List< Lock> mutexes = getMutexes( mutexReasoner, mutexSubDataProp);
		return new OWLReferencesCaller< Set<OWLDataProperty>>(  mutexes, this) {
			@Override
			protected Set<OWLDataProperty> performSynchronisedCall() {
				return getEnquirer().getSubDataPropertyOf( propName);
			}
		}.call();
	}

    /**
     * This method searches for the sub data properties of a specified property.
     * It looks for defined semantic entities as well as for inferred
     * axioms (given by the reasoner). In order to do so it calls:
	 * {@link OWLEnquirer#getSubDataPropertyOf(OWLDataProperty)};
	 * @param prop the data property from which to retrieve its sub-properties.
	 * @return the set of all the data properties that are sub-properties of the specified parameter.
	 */
	public Set<OWLDataProperty> getSubDataPropertyOf( OWLDataProperty prop){
		List< Lock> mutexes = getMutexes( mutexReasoner, mutexSubDataProp);
		return new OWLReferencesCaller< Set<OWLDataProperty>>(  mutexes, this) {
			@Override
			protected Set<OWLDataProperty> performSynchronisedCall() {
				return getEnquirer().getSubDataPropertyOf( prop);
			}
		}.call();
	}

    /** This method searches for the super data properties of a specified property.
     * It looks for defined semantic entities as well as for inferred
     * axioms (given by the reasoner). In order to do so it calls:
	 * {@link OWLEnquirer#getSuperDataPropertyOf(String)};
	 * @param propName the name of the data property from which to retrieve its super-properties.
	 * @return the set of all the data properties that are super-properties of the specified parameter.
	 */
	public Set<OWLDataProperty> getSuperDataPropertyOf( String propName){
		List< Lock> mutexes = getMutexes( mutexReasoner, mutexSuperDataProp);
		return new OWLReferencesCaller< Set<OWLDataProperty>>(  mutexes, this) {
			@Override
			protected Set<OWLDataProperty> performSynchronisedCall() {
				return getEnquirer().getSuperDataPropertyOf( propName);
			}
		}.call();
	}

    /**
     * This method searches for the super data properties of a specified property.
     * It looks for defined semantic entities as well as for inferred
     * axioms (given by the reasoner). In order to do so it calls:
	 * {@link OWLEnquirer#getSuperDataPropertyOf(OWLDataProperty)};
	 * @param prop the data property from which to retrieve its super-properties.
	 * @return the set of all the data properties that are super-properties of the specified parameter.
	 */
	public Set<OWLDataProperty> getSuperDataPropertyOf( OWLDataProperty prop){
		List< Lock> mutexes = getMutexes( mutexReasoner, mutexSuperDataProp);
		return new OWLReferencesCaller< Set<OWLDataProperty>>(  mutexes, this) {
			@Override
			protected Set<OWLDataProperty> performSynchronisedCall() {
				return getEnquirer().getSuperDataPropertyOf( prop);
			}
		}.call();
	}

    /**
     * This method searches for the sub object properties of a specified property.
     * It looks for defined semantic entities as well as for inferred
     * axioms (given by the reasoner). In order to do so it calls:
	 * {@link OWLEnquirer#getSubObjectPropertyOf(String)};
	 * @param propName the name of the data property from which to retrieve its sub-properties.
	 * @return the set of all the object properties that are sub-properties of the specified parameter.
	 */
	public Set<OWLObjectProperty> getSubObjectPropertyOf( String propName){
		List< Lock> mutexes = getMutexes( mutexReasoner, mutexSubObjProp);
		return new OWLReferencesCaller< Set<OWLObjectProperty>>(  mutexes, this) {
			@Override
			protected Set<OWLObjectProperty> performSynchronisedCall() {
				return getEnquirer().getSubObjectPropertyOf( propName);
			}
		}.call();
	}

    /**
     * This method searches for the sub object properties of a specified property.
     * It looks for defined semantic entities as well as for inferred
     * axioms (given by the reasoner). In order to do so it calls:
	 * {@link OWLEnquirer#getSubObjectPropertyOf(OWLObjectProperty)};
	 * @param prop the data property from which to retrieve its sub-properties.
	 * @return the set of all the object properties that are sub-properties of the specified parameter.
	 */
	public Set<OWLObjectProperty> getSubObjectPropertyOf( OWLObjectProperty prop){
		List< Lock> mutexes = getMutexes( mutexReasoner, mutexSubObjProp);
		return new OWLReferencesCaller< Set<OWLObjectProperty>>(  mutexes, this) {
			@Override
			protected Set<OWLObjectProperty> performSynchronisedCall() {
				return getEnquirer().getSubObjectPropertyOf( prop);
			}
		}.call();
    }

    /** This method searches for the super object properties of a specified property.
     * It looks for defined semantic entities as well as for inferred
     * axioms (given by the reasoner). In order to do so it calls:
	 * {@link OWLEnquirer#getSuperObjectPropertyOf(String)};
	 * @param propName the name of the data property from which to retrieve its super-properties.
	 * @return the set of all the object properties that are super-properties of the specified parameter.
	 */
	public Set<OWLObjectProperty> getSuperObjectPropertyOf( String propName){
		List< Lock> mutexes = getMutexes( mutexReasoner, mutexSuperObjProp);
		return new OWLReferencesCaller< Set<OWLObjectProperty>>(  mutexes, this) {
			@Override
			protected Set<OWLObjectProperty> performSynchronisedCall() {
				return getEnquirer().getSuperObjectPropertyOf( propName);
			}
		}.call();
	}

    /**
     * This method searches for the super object properties of a specified property.
     * It looks for defined semantic entities as well as for inferred
     * axioms (given by the reasoner). In order to do so it calls:
	 * {@link OWLEnquirer#getSuperObjectPropertyOf(OWLObjectProperty)};
	 * @param prop the data property from which to retrieve its super-properties.
	 * @return the set of all the object properties that are super-properties of the specified parameter.
	 */
	public Set<OWLObjectProperty> getSuperObjectPropertyOf( OWLObjectProperty prop){
		List< Lock> mutexes = getMutexes( mutexReasoner, mutexSuperObjProp);
		return new OWLReferencesCaller< Set<OWLObjectProperty>>(  mutexes, this) {
			@Override
			protected Set<OWLObjectProperty> performSynchronisedCall() {
				return getEnquirer().getSuperObjectPropertyOf( prop);
			}
		}.call();
    }

	/**
     * This method searches for the sub classes of a specified class.
     * It looks for defined semantic entities as well as for inferred
     * axioms (given by the reasoner). In order to do so it calls:
	 * {@link OWLEnquirer#getSubClassOf(String)};
	 * @param className the name of the class from which to retrieve its sub-classes.
	 * @return the set of all the classes that are sub-classes of the specified parameter.
	 */
	public Set<OWLClass> getSubClassOf( String className){
		List< Lock> mutexes = getMutexes( mutexReasoner, mutexSubClass);
		return new OWLReferencesCaller< Set<OWLClass>>(  mutexes, this) {
			@Override
			protected Set<OWLClass> performSynchronisedCall() {
				return getEnquirer().getSubClassOf( className);
			}
		}.call();
	}

    /**
     * This method searches for the sub classes of a specified class.
     * It looks for defined semantic entities as well as for inferred
     * axioms (given by the reasoner). In order to do so it calls:
	 * {@link OWLEnquirer#getSubClassOf(OWLClass)};
	 * @param cl the class from which to retrieve its sub-classes.
	 * @return the set of all the classes that are sub-classes of the specified parameter.
	 */
	public Set<OWLClass> getSubClassOf( OWLClass cl){
		List< Lock> mutexes = getMutexes( mutexReasoner, mutexSubClass);
		return new OWLReferencesCaller< Set<OWLClass>>(  mutexes, this) {
			@Override
			protected Set<OWLClass> performSynchronisedCall() {
				return getEnquirer().getSubClassOf( cl);
			}
		}.call();
	}

	/**
     * This method searches for the super classes of a specified class.
     * It looks for defined semantic entities as well as for inferred
     * axioms (given by the reasoner). In order to do so it calls:
	 * {@link OWLEnquirer#getSuperClassOf(String)};
	 * @param className the name of the class from which to retrieve its super-classes.
	 * @return the set of all the classes that are super-classes of the specified parameter.
	 */
	public Set<OWLClass> getSuperClassOf( String className){
		List< Lock> mutexes = getMutexes( mutexReasoner, mutexSuperClass);
		return new OWLReferencesCaller< Set<OWLClass>>(  mutexes, this) {
			@Override
			protected Set<OWLClass> performSynchronisedCall() {
				return getEnquirer().getSuperClassOf( className);
			}
		}.call();
	}

    /**
     * This method searches for the super classes of a specified class.
     * It looks for defined semantic entities as well as for inferred
     * axioms (given by the reasoner). In order to do so it calls:
	 * {@link OWLEnquirer#getSuperClassOf(OWLClass)};
	 * @param cl the class from which to retrieve its super-classes.
	 * @return the set of all the classes that are super-classes of the specified parameter.
	 */
	public Set<OWLClass> getSuperClassOf( OWLClass cl){
		List< Lock> mutexes = getMutexes( mutexReasoner, mutexSuperClass);
		return new OWLReferencesCaller< Set<OWLClass>>(  mutexes, this) {
			@Override
			protected Set<OWLClass> performSynchronisedCall() {
				return getEnquirer().getSuperClassOf( cl);
			}
		}.call();
	}

    /**
     * Returns the set of restrictions of the given class it terms
     * of: &forall; and &exist; quantifier, as well as: minimal, exact and maximal cardinality;
     * with respect to data and object properties.
     * @param cl the class from which get the restriction and cardinality limits.
     * @return the container of all the class restrictions and cardinality, for
     * the given class.
     */
    public Set<OWLEnquirer.ClassRestriction> getClassRestrictions(OWLClass cl){
        List< Lock> mutexes = getMutexes( mutexReasoner, mutexClassDefinition);
        return new OWLReferencesCaller< Set<OWLEnquirer.ClassRestriction>>(  mutexes, this) {
            @Override
            protected Set<OWLEnquirer.ClassRestriction> performSynchronisedCall() {
                return getEnquirer().getClassRestrictions( cl);
            }
        }.call();
    }

    /**
     * Returns the set of restrictions of the given class it terms
     * of: &forall; and &exist; quantifier, as well as: minimal, exact and maximal cardinality;
     * with respect to data and object properties.
     * @param className the name of the class from which get the restriction and cardinality limits.
     * @return the container of all the class restrictions and cardinality, for
     * the given class.
     */
    public Set<OWLEnquirer.ClassRestriction> getClassRestrictions(String className){
        List< Lock> mutexes = getMutexes( mutexReasoner, mutexClassDefinition);
        return new OWLReferencesCaller<Set<OWLEnquirer.ClassRestriction>>(  mutexes, this) {
            @Override
            protected Set<OWLEnquirer.ClassRestriction> performSynchronisedCall() {
                return getEnquirer().getClassRestrictions( className);
            }
        }.call();
    }

    /**
     * This method searches for the inverse properties of the specified property by name.
     * It looks for defined semantic entities as well as for inferred
     * axioms (given by the reasoner). In order to do so it calls:
     * {@link OWLEnquirer#getInverseProperty(String)};
     *
     * @param propertyName the name of the object property from which to retrieve all its inverse properties.
     * @return the set of all the inverse object properties of the given property.
     */
    public Set<OWLObjectProperty> getInverseProperty(String propertyName) {
        List<Lock> mutexes = getMutexes(mutexReasoner, mutexInverseProperty);
        return new OWLReferencesCaller<Set<OWLObjectProperty>>(mutexes, this) {
            @Override
            protected Set<OWLObjectProperty> performSynchronisedCall() {
                return getEnquirer().getInverseProperty(propertyName);
            }
        }.call();
    }

    /**
     * This method searches for the inverse properties of the specified property.
     * It looks for defined semantic entities as well as for inferred
     * axioms (given by the reasoner). In order to do so it calls:
     * {@link OWLEnquirer#getInverseProperty(OWLObjectProperty)} )};
     *
     * @param property the object property from which to retrieve all its inverse properties.
     * @return the set of all the inverse object properties of the given property.
     */
    public Set<OWLObjectProperty> getInverseProperty(OWLObjectProperty property) {
        List<Lock> mutexes = getMutexes(mutexReasoner, mutexInverseProperty);
        return new OWLReferencesCaller<Set<OWLObjectProperty>>(mutexes, this) {
            @Override
            protected Set<OWLObjectProperty> performSynchronisedCall() {
                return getEnquirer().getInverseProperty(property);
            }
        }.call();
    }

    /**
     * This method searches for an inverse property of the specified property by name.
     * It looks for defined semantic entities as well as for inferred
     * axioms (given by the reasoner). In order to do so it calls:
     * {@link OWLEnquirer#getOnlyInverseProperty(String)};
     *
     * @param propertyName the name of the object property from which to retrieve one of its inverse properties.
     * @return an inverse object properties of the given property.
     */
    public OWLObjectProperty getOnlyInverseProperty(String propertyName) {
        List<Lock> mutexes = getMutexes(mutexReasoner, mutexInverseProperty);
        return new OWLReferencesCaller<OWLObjectProperty>(mutexes, this) {
            @Override
            protected OWLObjectProperty performSynchronisedCall() {
                return getEnquirer().getOnlyInverseProperty(propertyName);
            }
        }.call();
    }

    /**
     * This method searches for an inverse property of the specified property.
     * It looks for defined semantic entities as well as for inferred
     * axioms (given by the reasoner). In order to do so it calls:
     * {@link OWLEnquirer#getOnlyInverseProperty(OWLObjectProperty)};
     *
     * @param property the object property from which to retrieve one of its inverse properties.
     * @return an inverse object properties of the given property.
     */
    public OWLObjectProperty getOnlyInverseProperty(OWLObjectProperty property) {
        List<Lock> mutexes = getMutexes(mutexReasoner, mutexInverseProperty);
        return new OWLReferencesCaller<OWLObjectProperty>(mutexes, this) {
            @Override
            protected OWLObjectProperty performSynchronisedCall() {
                return getEnquirer().getOnlyInverseProperty(property);
            }
        }.call();
    }


    /**
     * This methods returns the classes in which the given individual (by name) is classified,
     * that are leafs in the class hierarchy.
     * It relies on {@link OWLEnquirer#getBottomType(String)}.
     * @param individualName the name of the individual to lock for bottom types.
     * @return all the bottom types of the given individual.
     * It returns an empty set if such classes are not found.
     */
    public Set<OWLClass> getBottomType(String individualName){
        List<Lock> mutexes = getMutexes(mutexReasoner, mutexBottomType, mutexSubClass, mutexIndivClasses);
        return new OWLReferencesCaller<Set<OWLClass>>(mutexes, this) {
            @Override
            protected Set<OWLClass> performSynchronisedCall() {
                return getEnquirer().getBottomType( individualName);
            }
        }.call();
    }

    /**
     * This methods returns the classes in which the given individual is classified,
     * that are leafs in the class hierarchy.
     * It relies on {@link OWLEnquirer#getBottomType(OWLNamedIndividual)}.
     * @param individual the individual to lock for bottom types.
     * @return all the bottom types of the given individual.
     * It returns an empty set if such classes are not found.
     */
    public Set<OWLClass> getBottomType(OWLNamedIndividual individual){
        List<Lock> mutexes = getMutexes(mutexReasoner, mutexBottomType, mutexSubClass, mutexIndivClasses);
        return new OWLReferencesCaller<Set<OWLClass>>(mutexes, this) {
            @Override
            protected Set<OWLClass> performSynchronisedCall() {
                return getEnquirer().getBottomType( individual);
            }
        }.call();
    }

    /**
     * This methods returns a class in which the given individual (by name) is classified,
     * that is a leaf in the class hierarchy.
     * It relies on {@link OWLEnquirer#getOnlyBottomType(String)}.
     * @param individualName the name of the individual to lock for a bottom type.
     * @return a bottom type of the given individual.
     * It returns {@code null} if such a class is not found.
     */
    public OWLClass getOnlyBottomType(String individualName){
        List<Lock> mutexes = getMutexes(mutexReasoner, mutexBottomType, mutexSubClass, mutexIndivClasses);
        return new OWLReferencesCaller<OWLClass>(mutexes, this) {
            @Override
            protected OWLClass performSynchronisedCall() {
                return getEnquirer().getOnlyBottomType( individualName);
            }
        }.call();
    }

    /**
     * This methods returns a class in which the given individual is classified,
     * that is a leaf in the class hierarchy.
     * It relies on {@link OWLEnquirer#getOnlyBottomType(OWLNamedIndividual)}.
     * @param individual  the individual to lock for a bottom type.
     * @return a bottom type of the given individual.
     * It returns {@code null} if such a class is not found.
     */
    public OWLClass getOnlyBottomType(OWLNamedIndividual individual){
        List<Lock> mutexes = getMutexes(mutexReasoner, mutexBottomType, mutexSubClass, mutexIndivClasses);
        return new OWLReferencesCaller<OWLClass>(mutexes, this) {
            @Override
            protected OWLClass performSynchronisedCall() {
                return getEnquirer().getOnlyBottomType( individual);
            }
        }.call();
    }


    /**
     * Performs a SPARQL query on the ontology. Returns a list of {@link QuerySolution} or {@code null} if the query fails.
     * Works only with the Pellet reasoner. {@code timeOut} parameter sets the query timeout, no timeout is set if
     * {@code timeOut &lt;=0}. Once timeout is reached, all solutions found up to that point are returned.
     * @param query a string defining the query in SPARQL query syntax.
     * @param timeOut timeout for the query.
     * @return list of solutions.
     */
	public List< QuerySolution> sparql(String query, Long timeOut){
		List< Lock> mutexes = getMutexes( mutexReasoner, mutexSPARQL);
		return new OWLReferencesCaller< List< QuerySolution>>(  mutexes, this) {
			@Override
			protected List< QuerySolution> performSynchronisedCall() {
				return getEnquirer().sparql( query, timeOut);
			}
		}.call();
	}

    /**
     * Performs a SPARQL query on the ontology. Returns a list of {@link QuerySolution} or {@code null} if the query fails.
     * Works only with the Pellet reasoner.
     * @param query a string defining the query in SPARQL query syntax.
     * @return list of solutions.
     */
	public List< QuerySolution> sparql(String query){ // no time out
		List< Lock> mutexes = getMutexes( mutexReasoner, mutexSPARQL);
		return new OWLReferencesCaller< List< QuerySolution>>(  mutexes, this) {
			@Override
			protected List< QuerySolution> performSynchronisedCall() {
				return getEnquirer().sparql(query);
			}
		}.call();
	}

    /**
     * Performs a SPARQL query on the ontology. Returns a list of {@link QuerySolution} or {@code null} if the query fails.
     * Works only with the Pellet reasoner. {@code timeOut} parameter sets the query timeout, no timeout is set if
     * {@code timeOut &lt;= 0}. Once timeout is reached, all solutions found up to that point are returned.
     * @param prefix a string defining the query prefix field in SPARQL query syntax.
     * @param select a string defining the query select field in SPARQL query syntax.
     * @param where a string defining the query where field in SPARQL query syntax.
     * @param timeOut timeout for the query.
     * @return list of solutions.
     */
	public List< QuerySolution> sparql( String prefix, String select, String where, Long timeOut){
		List< Lock> mutexes = getMutexes( mutexReasoner, mutexSPARQL);
		return new OWLReferencesCaller< List< QuerySolution>>(  mutexes, this) {
			@Override
			protected List< QuerySolution> performSynchronisedCall() {
				return getEnquirer().sparql( prefix, select, where, timeOut);
			}
		}.call();
	}

    /**
     * Performs a SPARQL query on the ontology. Returns a list of {@link QuerySolution} or {@code null} if the query fails.
     * Works only with the Pellet reasoner.
     * @param prefix a string defining the query prefix field in SPARQL query syntax.
     * @param select a string defining the query select field in SPARQL query syntax.
     * @param where a string defining the query where field in SPARQL query syntax.
     * @return list of solutions.
     */
	public List< QuerySolution> sparql( String prefix, String select, String where){
		List< Lock> mutexes = getMutexes( mutexReasoner, mutexSPARQL);
		return new OWLReferencesCaller< List< QuerySolution>>(  mutexes, this) {
			@Override
			protected List< QuerySolution> performSynchronisedCall() {
				return getEnquirer().sparql( prefix, select, where);
			}
		}.call();
	}

    /**
     * An utility method that call {@link #sparql(String, Long)} and translates the results to a list of maps among strings.
     * Used to share the results with other code and processes. {@code timeOut} parameter sets the query timeout,
     * no timeout is set if {@code timeOut &lt;= 0}. Once timeout is reached, all solutions found up to that point are returned.
     * @param query a string defining the query in SPARQL query syntax.
     * @param timeOut timeout for the query.
     * @return formatted list of solutions.
     */
	public List< Map< String, String>> sparql2Msg(String query, Long timeOut){
		List< Lock> mutexes = getMutexes( mutexReasoner, mutexSPARQL);
		return new OWLReferencesCaller< List< Map< String, String>>>(  mutexes, this) {
			@Override
			protected List< Map< String, String>> performSynchronisedCall() {
				return getEnquirer().sparqlMsg( query, timeOut);
			}
		}.call();
	}

    /**
     * An utility method that call {@link #sparql(String, Long)} and translates the results to a list of maps among strings.
	 * This call do not apply any time out.
     * Used to share the results with other code and processes.
     * @param query a string defining the query in SPARQL query syntax.
     * @return formatted list of solutions.
     */
	public List< Map< String, String>> sparql2Msg( String query){ // no time out
		List< Lock> mutexes = getMutexes( mutexReasoner, mutexSPARQL);
		return new OWLReferencesCaller< List< Map< String, String>>>(  mutexes, this) {
			@Override
			protected List< Map< String, String>> performSynchronisedCall() {
				return getEnquirer().sparqlMsg(query);
			}
		}.call();
	}

    /**
     * An utility method that call {@link #sparql(String, Long)} and translates the results to a list of maps among strings.
     * Used to share the results with other code and processes. {@code timeOut} parameter sets the query timeout,
     * no timeout is set if {@code timeOut &lt;= 0}. Once timeout is reached, all solutions found up to that point are returned.
     * @param prefix a string defining the query prefix field in SPARQL query syntax.
     * @param select a string defining the query select field in SPARQL query syntax.
     * @param where a string defining the query where field in SPARQL query syntax.
     * @param timeOut timeout for the query.
     * @return list of solutions.
     */
	public List< Map< String, String>> sparql2Msg( String prefix, String select, String where, Long timeOut){
		List< Lock> mutexes = getMutexes( mutexReasoner, mutexSPARQL);
		return new OWLReferencesCaller< List< Map< String, String>>>(  mutexes, this) {
			@Override
			protected List< Map< String, String>> performSynchronisedCall() {
				return getEnquirer().sparqlMsg( prefix, select, where, timeOut);
			}
		}.call();
	}

    /**
     * An utility method that call {@link #sparql(String, Long)} and translates the results to a list of maps among strings.
     * Used to share the results with other code and processes.
     * @param prefix a string defining the query prefix field in SPARQL query syntax.
     * @param select a string defining the query select field in SPARQL query syntax.
     * @param where a string defining the query where field in SPARQL query syntax.
     * @return list of solutions.
     */
	public List< Map< String, String>> sparql2Msg(String prefix, String select, String where){
		List< Lock> mutexes = getMutexes( mutexReasoner, mutexSPARQL);
		return new OWLReferencesCaller< List< Map< String, String>>>(  mutexes, this) {
			@Override
			protected List< Map< String, String>> performSynchronisedCall() {
				return getEnquirer().sparqlMsg( prefix, select, where);
			}
		}.call();
	}

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
		List< Lock> mutexes = getMutexes( mutexReasoner, mutexAddObjPropB2Ind);
		return new OWLReferencesCaller< OWLOntologyChange>(  mutexes, this) {
			@Override
			protected OWLOntologyChange performSynchronisedCall() {
				return getManipulator().addObjectPropertyB2Individual( ind, prop, value);
			}
		}.call();
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
		List< Lock> mutexes = getMutexes( mutexReasoner, mutexAddObjPropB2Ind);
		return new OWLReferencesCaller< OWLOntologyChange>(  mutexes, this) {
			@Override
			protected OWLOntologyChange performSynchronisedCall() {
				return getManipulator().addObjectPropertyB2Individual( individualName, propName, valueName);
			}
		}.call();
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
		List< Lock> mutexes = getMutexes( mutexReasoner, mutexAddDataPropB2Ind);
		return new OWLReferencesCaller< OWLOntologyChange>(  mutexes, this) {
			@Override
			protected OWLOntologyChange performSynchronisedCall() {
				return getManipulator().addDataPropertyB2Individual(ind, prop, value);
			}
		}.call();
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
		List< Lock> mutexes = getMutexes( mutexReasoner, mutexAddDataPropB2Ind);
		return new OWLReferencesCaller< OWLOntologyChange>(  mutexes, this) {
			@Override
			protected OWLOntologyChange performSynchronisedCall() {
				return getManipulator().addDataPropertyB2Individual( individualName, propertyName, value);
			}
		}.call();
	}

	/**
	 * This method calls {@link OWLManipulator#addIndividual(OWLNamedIndividual)}
	 * in order to add a an individual into an ontology.
	 * @param ind the individual to be set to be belonging to the {@code OWLThing} class.
	 * @return the changes to be done in order to add an individual into a class. (see {@link OWLManipulator} for more info)
	 */
	public OWLOntologyChange addIndividual(OWLNamedIndividual ind){
		List< Lock> mutexes = getMutexes( mutexReasoner, mutexAddInd);
		return new OWLReferencesCaller< OWLOntologyChange>(  mutexes, this) {
			@Override
			protected OWLOntologyChange performSynchronisedCall() {
				return getManipulator().addIndividual(ind);
			}
		}.call();
	}
	/**
	 * This method calls {@link OWLManipulator#addIndividual(String)}
	 * in order to add a an individual into an ontology.
	 * @param individualName the name of the individual to be set to be belonging to the {@code OWLThing} class.
	 * @return the changes to be done in order to add an individual into a class. (see {@link OWLManipulator} for more info)
	 */
	public OWLOntologyChange addIndividual(String individualName){
		List< Lock> mutexes = getMutexes( mutexReasoner, mutexAddInd);
		return new OWLReferencesCaller< OWLOntologyChange>(  mutexes, this) {
			@Override
			protected OWLOntologyChange performSynchronisedCall() {
				return getManipulator().addIndividual( individualName);
			}
		}.call();
	}
	
	/**
	 * This method calls {@link OWLManipulator#addIndividualB2Class(OWLNamedIndividual, OWLClass)}
	 * in order to add a an individual into a class.
	 * @param ind the individual to be set to be belonging to the {@code cls} class.
	 * @param cls the class in which the individual will be belonging to.
	 * @return the changes to be done in order to add an individual into a class. (see {@link OWLManipulator} for more info)
	 */
	public OWLOntologyChange addIndividualB2Class(OWLNamedIndividual ind, OWLClass cls){
		List< Lock> mutexes = getMutexes( mutexReasoner, mutexAddIndB2Class);
		return new OWLReferencesCaller< OWLOntologyChange>(  mutexes, this) {
			@Override
			protected OWLOntologyChange performSynchronisedCall() {
				return getManipulator().addIndividualB2Class(ind, cls);
			}
		}.call();
	}
	/**
	 * This method calls {@link OWLManipulator#addIndividualB2Class(String, String)}
	 * in order to add a an individual into a class.
	 * @param individualName the name of the individual to be set to be belonging to the {@code cls} class.
	 * @param className the name of the class in which the individual will be belonging to.
	 * @return the changes to be done in order to add an individual into a class. (see {@link OWLManipulator} for more info)
	 */
	public OWLOntologyChange addIndividualB2Class(String individualName, String className){
		List< Lock> mutexes = getMutexes( mutexReasoner, mutexAddIndB2Class);
		return new OWLReferencesCaller< OWLOntologyChange>(  mutexes, this) {
			@Override
			protected OWLOntologyChange performSynchronisedCall() {
				return getManipulator().addIndividualB2Class(individualName, className);
			}
		}.call();
	}

	/**
	 * This method calls {@link OWLManipulator#addClass(String)}
	 * in order to add a class into the ontology.
	 * @param className the name of the class to be added into the ontology.
	 * @return the changes to be done in order to add a class into the ontology. (see {@link OWLManipulator} for more info)
	 */
	public OWLOntologyChange addClass( String className){
		List< Lock> mutexes = getMutexes( mutexReasoner, mutexAddClass);
		return new OWLReferencesCaller< OWLOntologyChange>(  mutexes, this) {
			@Override
			protected OWLOntologyChange performSynchronisedCall() {
				return getManipulator().addClass( className);
			}
		}.call();
	}
	/**
	 * This method calls {@link OWLManipulator#addClass(OWLClass)}
	 * in order to add a class into the ontology.
	 * @param cls the class to be added into the ontology.
	 * @return the changes to be done in order to add a class into the ontology. (see {@link OWLManipulator} for more info)
	 */
	public OWLOntologyChange addClass( OWLClass cls){
		List< Lock> mutexes = getMutexes( mutexReasoner, mutexAddClass);
		return new OWLReferencesCaller< OWLOntologyChange>(  mutexes, this) {
			@Override
			protected OWLOntologyChange performSynchronisedCall() {
				return getManipulator().addClass( cls);
			}
		}.call();
	}
	
	/**
	 * This method calls {@link OWLManipulator#addSubClassOf(String, String)}
	 * in order to add a class into the ontology as a sub class of a specified entity.
	 * @param superClassName the name of the class in which add the sub class.
	 * @param subClassName the name of the class to add as sub class of the specified class.
	 * @return the changes to be done in order to add a class by specifying its super class. (see {@link OWLManipulator} for more info)
	 */
	public OWLOntologyChange addSubClassOf( String superClassName, String subClassName){
		List< Lock> mutexes = getMutexes( mutexReasoner, mutexAddSubClass);
		return new OWLReferencesCaller< OWLOntologyChange>(  mutexes, this) {
			@Override
			protected OWLOntologyChange performSynchronisedCall() {
				return getManipulator().addSubClassOf( superClassName, subClassName);
			}
		}.call();
	}
	/**
	 * This method calls {@link OWLManipulator#addSubClassOf(OWLClass, OWLClass)}
	 * in order to add a class into the ontology as a sub class of a specified entity.
	 * @param superClass the class in which add the sub class.
	 * @param subClass the class to add as sub class of the specified class.
	 * @return the changes to be done in order to add a class by specifying its super class. (see {@link OWLManipulator} for more info)
	 */
	public OWLOntologyChange addSubClassOf( OWLClass superClass, OWLClass subClass){
		List< Lock> mutexes = getMutexes( mutexReasoner, mutexAddSubClass);
		return new OWLReferencesCaller< OWLOntologyChange>(  mutexes, this) {
			@Override
			protected OWLOntologyChange performSynchronisedCall() {
				return getManipulator().addSubClassOf( superClass, subClass);
			}
		}.call();
	}

	/**
	 * This method calls {@link OWLManipulator#addSubDataPropertyOf(String, String)}
	 * in order to add a data property into the ontology as a sub property of a specified entity.
	 * @param superPropertyName the name of the super data property.
	 * @param subPropertyName the name of the sub data property.
	 * @return the changes to be done in order to add a data property by specifying its super property. (see {@link OWLManipulator} for more info)
	 */
	public OWLOntologyChange addSubDataPropertyOf( String superPropertyName, String subPropertyName){
		List< Lock> mutexes = getMutexes( mutexReasoner, mutexAddSubDataProperty);
		return new OWLReferencesCaller< OWLOntologyChange>(  mutexes, this) {
			@Override
			protected OWLOntologyChange performSynchronisedCall() {
				return getManipulator().addSubDataPropertyOf( superPropertyName, subPropertyName);
			}
		}.call();
	}
	/**
	 * This method calls {@link OWLManipulator#addSubDataPropertyOf(OWLDataProperty, OWLDataProperty)}
	 * in order to add a data property into the ontology as a sub property of a specified entity.
	 * @param superProperty the super data property.
	 * @param subProperty the sub data property.
	 * @return the changes to be done in order to add a data property by specifying its super property. (see {@link OWLManipulator} for more info)
	 */
	public OWLOntologyChange addSubDataPropertyOf( OWLDataProperty superProperty, OWLDataProperty subProperty){
		List< Lock> mutexes = getMutexes( mutexReasoner, mutexAddSubDataProperty);
		return new OWLReferencesCaller< OWLOntologyChange>(  mutexes, this) {
			@Override
			protected OWLOntologyChange performSynchronisedCall() {
				return getManipulator().addSubDataPropertyOf( superProperty, subProperty);
			}
		}.call();
	}

	/**
	 * This method calls {@link OWLManipulator#addSubObjectPropertyOf(String, String)}
	 * in order to add an object property into the ontology as a sub property of a specified entity.
	 * @param superPropertyName the name of the super object property.
	 * @param subPropertyName the name of the sub object property.
	 * @return the changes to be done in order to add a data property by specifying its super property. (see {@link OWLManipulator} for more info)
	 */
	public OWLOntologyChange addSubObjectPropertyOf( String superPropertyName, String subPropertyName){
		List< Lock> mutexes = getMutexes( mutexReasoner, mutexAddSubObjectProperty);
		return new OWLReferencesCaller< OWLOntologyChange>(  mutexes, this) {
			@Override
			protected OWLOntologyChange performSynchronisedCall() {
				return getManipulator().addSubObjectPropertyOf( superPropertyName, subPropertyName);
			}
		}.call();
	}
	/**
	 * This method calls {@link OWLManipulator#addSubObjectPropertyOf(OWLObjectProperty, OWLObjectProperty)}
	 * in order to add an object property into the ontology as a sub property of a specified entity.
	 * @param superProperty the super object property.
	 * @param subProperty the sub object property.
	 * @return the changes to be done in order to add an object property by specifying its super property. (see {@link OWLManipulator} for more info)
	 */
	public OWLOntologyChange addSubObjectPropertyOf( OWLObjectProperty superProperty, OWLObjectProperty subProperty){
		List< Lock> mutexes = getMutexes( mutexReasoner, mutexAddSubObjectProperty);
		return new OWLReferencesCaller< OWLOntologyChange>(  mutexes, this) {
			@Override
			protected OWLOntologyChange performSynchronisedCall() {
				return getManipulator().addSubObjectPropertyOf( superProperty, subProperty);
			}
		}.call();
	}

    /**
     * Returns the changes to make a class be a sub class of an object property in existence with a class value.
     * In symbols: {@code C &sub; p(&exist; V)}, where: {@code C} is the class, {@code p} the object property
     * and {@code V}, the class value.
     * Changes will be buffered if {@link OWLManipulator#isChangeBuffering()} is {@code true}, else they will be applied immediately.
     * @param cl the class object of the sub-setting relation ({@code C}).
     * @param property the property of the sub-setting relation ({@code p}).
     * @param value the value of the sub-setting relation ({@code V}).
     * @return the changes to be applied in order to make a class being a sub-set of an existential property.
     */
	public OWLOntologyChange addSomeObjectClassExpression(OWLClass cl, OWLObjectProperty property, OWLClass value){
		List< Lock> mutexes = getMutexes( mutexReasoner, mutexAddRemovingClassDefinition);
		return new OWLReferencesCaller< OWLOntologyChange>(  mutexes, this) {
			@Override
			protected OWLOntologyChange performSynchronisedCall() {
				return getManipulator().addSomeObjectClassExpression( cl, property, value);
			}
		}.call();
	}
    /**
     * Returns the changes to make a class be a sub class of an object property in existence with a class value.
     * In symbols: {@code C &sub; p(&exist; V)}, where: {@code C} is the class, {@code p} the object property
     * and {@code V}, the class value.
     * Changes will be buffered if {@link OWLManipulator#isChangeBuffering()} is {@code true}, else they will be applied immediately.
     * @param className the name of the class object of the sub-setting relation ({@code C}).
     * @param propertyName the name of the property of the sub-setting relation ({@code p}).
     * @param valueName the name the value of the sub-setting relation ({@code V}).
     * @return the changes to be applied in order to make a class being a sub-set of an existential property.
     */
	public OWLOntologyChange addSomeObjectClassExpression(String className, String propertyName, String valueName){
		List< Lock> mutexes = getMutexes( mutexReasoner, mutexAddRemovingClassDefinition);
		return new OWLReferencesCaller< OWLOntologyChange>(  mutexes, this) {
			@Override
			protected OWLOntologyChange performSynchronisedCall() {
				return getManipulator().addSomeObjectClassExpression( className, propertyName, valueName);
			}
		}.call();
	}
    /**
     * Returns the changes to make a class be a sub class of an object property universally identifying a class value.
     * In symbols: {@code C &sub; p(&forall; V)}, where: {@code C} is the class, {@code p} the object property
     * and {@code V}, the class value.
     * Changes will be buffered if {@link OWLManipulator#isChangeBuffering()} is {@code true}, else they will be applied immediately.
     * @param cl the class object of the sub-setting relation ({@code C}).
     * @param property the property of the sub-setting relation ({@code p}).
     * @param value the value of the sub-setting relation ({@code V}).
     * @return the changes to be applied in order to make a class being a sub-set of an universal property.
     */
	public OWLOntologyChange addOnlyObjectClassExpression(OWLClass cl, OWLObjectProperty property, OWLClass value){
		List< Lock> mutexes = getMutexes( mutexReasoner, mutexAddRemovingClassDefinition);
		return new OWLReferencesCaller< OWLOntologyChange>(  mutexes, this) {
			@Override
			protected OWLOntologyChange performSynchronisedCall() {
				return getManipulator().addOnlyObjectClassExpression( cl, property, value);
			}
		}.call();
	}
    /**
     * Returns the changes to make a class be a sub class of an object property universally identifying a class value.
     * In symbols: {@code C &sub; p(&forall; V)}, where: {@code C} is the class, {@code p} the object property
     * and {@code V}, the class value.
     * Changes will be buffered if {@link OWLManipulator#isChangeBuffering()} is {@code true}, else they will be applied immediately.
     * @param className the name of the class object of the sub-setting relation ({@code C}).
     * @param propertyName the name of the property of the sub-setting relation ({@code p}).
     * @param valueName the name the value of the sub-setting relation ({@code V}).
     * @return the changes to be applied in order to make a class being a sub-set of an universal property.
     */
	public OWLOntologyChange addOnlyObjectClassExpression(String className, String propertyName, String valueName){
		List< Lock> mutexes = getMutexes( mutexReasoner, mutexAddRemovingClassDefinition);
		return new OWLReferencesCaller< OWLOntologyChange>(  mutexes, this) {
			@Override
			protected OWLOntologyChange performSynchronisedCall() {
				return getManipulator().addOnlyObjectClassExpression( className, propertyName, valueName);
			}
		}.call();
	}

    /**
     * Returns the changes to make a class be a sub class of a data property in existence with a data type value.
     * In symbols: {@code C &sub; p(&exist; D)}, where: {@code C} is the class, {@code p} the data property
     * and {@code D}, the type of data (supported {@link String}, {@link Integer}, {@link Double},
     * {@link Float} and {@link Long}).
     * Changes will be buffered if {@link OWLManipulator#isChangeBuffering()} is {@code true}, else they will be applied immediately.
     * @param cl the class object of the sub-setting relation ({@code C}).
     * @param property the property of the sub-setting relation ({@code p}).
     * @param type the Class representing a supported data type for the sub-setting relation ({@code D}).
     * @return the changes to be applied in order to make a class being a sub-set of an existential property.
     */
	public OWLOntologyChange addSomeDataClassExpression(OWLClass cl, OWLDataProperty property, Class type){
		List< Lock> mutexes = getMutexes( mutexReasoner, mutexAddRemovingClassDefinition);
		return new OWLReferencesCaller< OWLOntologyChange>(  mutexes, this) {
			@Override
			protected OWLOntologyChange performSynchronisedCall() {
				return getManipulator().addSomeDataClassExpression( cl, property, type);
			}
		}.call();
	}
    /**
     * Returns the changes to make a class be a sub class of a data property in existence with a data type value.
     * In symbols: {@code C &sub; p(&exist; D)}, where: {@code C} is the class, {@code p} the data property
     * and {@code D}, the type of data (supported {@link String}, {@link Integer}, {@link Double},
     * {@link Float} and {@link Long}).
     * Changes will be buffered if {@link OWLManipulator#isChangeBuffering()} is {@code true}, else they will be applied immediately.
     * @param className the name of the class object of the sub-setting relation ({@code C}).
     * @param propertyName the name of the property of the sub-setting relation ({@code p}).
     * @param type the Class representing a supported data type for the sub-setting relation ({@code D}).
     * @return the changes to be applied in order to make a class being a sub-set of an existential property.
     */
	public OWLOntologyChange addSomeDataClassExpression(String className, String propertyName, Class type){
		List< Lock> mutexes = getMutexes( mutexReasoner, mutexAddRemovingClassDefinition);
		return new OWLReferencesCaller< OWLOntologyChange>(  mutexes, this) {
			@Override
			protected OWLOntologyChange performSynchronisedCall() {
				return getManipulator().addSomeDataClassExpression( className, propertyName, type);
			}
		}.call();
	}
    /**
     * Returns the changes to make a class be a sub class of a data property universally identified by a data type value.
     * In symbols: {@code C &sub; p(&forall; D)}, where: {@code C} is the class, {@code p} the data property
     * and {@code D}, the type of data (supported {@link String}, {@link Integer}, {@link Double},
     * {@link Float} and {@link Long}).
     * Changes will be buffered if {@link OWLManipulator#isChangeBuffering()} is {@code true}, else they will be applied immediately.
     * @param cl the class object of the sub-setting relation ({@code C}).
     * @param property the property of the sub-setting relation ({@code p}).
     * @param type the Class representing a supported data type for the sub-setting relation ({@code D}).
     * @return the changes to be applied in order to make a class being a sub-set of an universal property.
     */
	public OWLOntologyChange addOnlyDataClassExpression(OWLClass cl, OWLDataProperty property, Class type){
		List< Lock> mutexes = getMutexes( mutexReasoner, mutexAddRemovingClassDefinition);
		return new OWLReferencesCaller< OWLOntologyChange>(  mutexes, this) {
			@Override
			protected OWLOntologyChange performSynchronisedCall() {
				return getManipulator().addOnlyDataClassExpression( cl, property, type);
			}
		}.call();
	}
    /**
     * Returns the changes to make a class be a sub class of a data property universally identified by a data type value.
     * In symbols: {@code C &sub; p(&forall; D)}, where: {@code C} is the class, {@code p} the data property
     * and {@code D}, the type of data (supported {@link String}, {@link Integer}, {@link Double},
     * {@link Float} and {@link Long}).
     * Changes will be buffered if {@link OWLManipulator#isChangeBuffering()} is {@code true}, else they will be applied immediately.
     * @param className the name of the class object of the sub-setting relation ({@code C}).
     * @param propertyName the name of the property of the sub-setting relation ({@code p}).
     * @param type the Class representing a supported data type for the sub-setting relation ({@code D}).
     * @return the changes to be applied in order to make a class being a sub-set of an universal property.
     */
	public OWLOntologyChange addOnlyDataClassExpression(String className, String propertyName, Class type){
		List< Lock> mutexes = getMutexes( mutexReasoner, mutexAddRemovingClassDefinition);
		return new OWLReferencesCaller< OWLOntologyChange>(  mutexes, this) {
			@Override
			protected OWLOntologyChange performSynchronisedCall() {
				return getManipulator().addOnlyDataClassExpression( className, propertyName, type);
			}
		}.call();
	}

    /**
     * Returns the changes to make a class be a sub class of an object property expression
     * minimally identified by a given cardinality class restriction.
     * In symbols: {@code C &sub; p(&lt;<sub>d</sub> V)}, where: {@code C} is the class, {@code p} the object property,
     * {@code V} is the class value and {@code d}, the cardinality.
     * Changes will be buffered if {@link OWLManipulator#isChangeBuffering()} is {@code true}, else they will be applied immediately.
     * @param cl the class object of the sub-setting relation ({@code C}).
     * @param property the property of the sub-setting relation ({@code p}).
     * @param cardinality the cardinality of the minimal relation ({@code d}).
     * @param value the value of the sub-setting relation ({@code V}).
     * @return the changes to be applied in order to make a class being a sub-set of a minimum number of properties
     * restricted to a class.
     */	
	public OWLOntologyChange addMinObjectClassExpression(OWLClass cl, OWLObjectProperty property, int cardinality, OWLClass value){
		List< Lock> mutexes = getMutexes( mutexReasoner, mutexAddRemovingClassDefinition);
		return new OWLReferencesCaller< OWLOntologyChange>(  mutexes, this) {
			@Override
			protected OWLOntologyChange performSynchronisedCall() {
				return getManipulator().addMinObjectClassExpression( cl, property, cardinality, value);
			}
		}.call();
	}
    /**
     * Returns the changes to make a class be a sub class of an object property  expression
     * minimally identified by a given cardinality class restriction.
     * In symbols: {@code C &sub; p(&lt;<sub>d</sub> V)}, where: {@code C} is the class, {@code p} the object property,
     * {@code V} is the class value and {@code d}, the cardinality.
     * Changes will be buffered if {@link OWLManipulator#isChangeBuffering()} is {@code true}, else they will be applied immediately.
     * @param className the name of the class object of the sub-setting relation ({@code C}).
     * @param propertyName the name of the property of the sub-setting relation ({@code p}).
     * @param cardinality the cardinality of the minimal relation ({@code d}).
     * @param valueName the name of the value of the sub-setting relation ({@code V}).
     * @return the changes to be applied in order to make a class being a sub-set of a minimum number of properties
     * restricted to a class.
     */
	public OWLOntologyChange addMinObjectClassExpression(String className, String propertyName, int cardinality, String valueName){
		List< Lock> mutexes = getMutexes( mutexReasoner, mutexAddRemovingClassDefinition);
		return new OWLReferencesCaller< OWLOntologyChange>(  mutexes, this) {
			@Override
			protected OWLOntologyChange performSynchronisedCall() {
				return getManipulator().addMinObjectClassExpression( className, propertyName, cardinality, valueName);
			}
		}.call();
	}
    /**
     * Returns the changes to make a class be a sub class of an object property  expression
     * maximally identified by a given cardinality class restriction.
     * In symbols: {@code C &sub; p(&gt;<sub>d</sub> V)}, where: {@code C} is the class, {@code p} the object property,
     * {@code V} is the class value and {@code d}, the cardinality.
     * Changes will be buffered if {@link OWLManipulator#isChangeBuffering()} is {@code true}, else they will be applied immediately.
     * @param cl the class object of the sub-setting relation ({@code C}).
     * @param property the property of the sub-setting relation ({@code p}).
     * @param cardinality the cardinality of the minimal relation ({@code d}).
     * @param value the value of the sub-setting relation ({@code V}).
     * @return the changes to be applied in order to make a class being a sub-set of a maximum number of properties
     * restricted to a class.
     */
	public OWLOntologyChange addMaxObjectClassExpression(OWLClass cl, OWLObjectProperty property, int cardinality, OWLClass value){
		List< Lock> mutexes = getMutexes( mutexReasoner, mutexAddRemovingClassDefinition);
		return new OWLReferencesCaller< OWLOntologyChange>(  mutexes, this) {
			@Override
			protected OWLOntologyChange performSynchronisedCall() {
				return getManipulator().addMaxObjectClassExpression( cl, property, cardinality, value);
			}
		}.call();
	}
    /**
     * Returns the changes to make a class be a sub class of an object property expression
     * maximally identified by a given cardinality class restriction.
     * In symbols: {@code C &sub; p(&gt;<sub>d</sub> V)}, where: {@code C} is the class, {@code p} the object property,
     * {@code V} is the class value and {@code d}, the cardinality.
     * Changes will be buffered if {@link OWLManipulator#isChangeBuffering()} is {@code true}, else they will be applied immediately.
     * @param className the name of the class object of the sub-setting relation ({@code C}).
     * @param propertyName the name of the property of the sub-setting relation ({@code p}).
     * @param cardinality the cardinality of the minimal relation ({@code d}).
     * @param valueName the name of the value of the sub-setting relation ({@code V}).
     * @return the changes to be applied in order to make a class being a sub-set of a maximum number of properties
     * restricted to a class.
     */
	public OWLOntologyChange addMaxObjectClassExpression( String className, String propertyName, int cardinality, String valueName){
		List< Lock> mutexes = getMutexes( mutexReasoner, mutexAddRemovingClassDefinition);
		return new OWLReferencesCaller< OWLOntologyChange>(  mutexes, this) {
			@Override
			protected OWLOntologyChange performSynchronisedCall() {
				return getManipulator().addMaxObjectClassExpression( className, propertyName, cardinality, valueName);
			}
		}.call();
	}
    /**
     * Returns the changes to make a class be a sub class of an object property expression
     * exactly identified by a given cardinality class restriction.
     * In symbols: {@code C &sub; p(=<sub>d</sub> V)}, where: {@code C} is the class, {@code p} the object property,
     * {@code V} is the class value and {@code d}, the cardinality.
     * Changes will be buffered if {@link OWLManipulator#isChangeBuffering()} is {@code true}, else they will be applied immediately.
     * @param cl the class object of the sub-setting relation ({@code C}).
     * @param property the property of the sub-setting relation ({@code p}).
     * @param cardinality the cardinality of the minimal relation ({@code d}).
     * @param value the value of the sub-setting relation ({@code V}).
     * @return the changes to be applied in order to make a class being a sub-set of a exact number of properties
     * restricted to a class.
     */
	public OWLOntologyChange addExactObjectClassExpression(OWLClass cl, OWLObjectProperty property, int cardinality, OWLClass value){
		List< Lock> mutexes = getMutexes( mutexReasoner, mutexAddRemovingClassDefinition);
		return new OWLReferencesCaller< OWLOntologyChange>(  mutexes, this) {
			@Override
			protected OWLOntologyChange performSynchronisedCall() {
				return getManipulator().addExactObjectClassExpression( cl, property, cardinality, value);
			}
		}.call();
	}
    /**
     * Returns the changes to make a class be a sub class of an object property expression
     * exactly identified by a given cardinality class restriction.
     * In symbols: {@code C &sub; p(=<sub>d</sub> V)}, where: {@code C} is the class, {@code p} the object property,
     * {@code V} is the class value and {@code d}, the cardinality.
     * Changes will be buffered if {@link OWLManipulator#isChangeBuffering()} is {@code true}, else they will be applied immediately.
     * @param className the name of the class object of the sub-setting relation ({@code C}).
     * @param propertyName the name of the property of the sub-setting relation ({@code p}).
     * @param cardinality the cardinality of the minimal relation ({@code d}).
     * @param valueName the name of the value of the sub-setting relation ({@code V}).
     * @return the changes to be applied in order to make a class being a sub-set of a exact number of properties
     * restricted to a class.
     */
	public OWLOntologyChange addExactObjectClassExpression( String className, String propertyName, int cardinality, String valueName){
		List< Lock> mutexes = getMutexes( mutexReasoner, mutexAddRemovingClassDefinition);
		return new OWLReferencesCaller< OWLOntologyChange>(  mutexes, this) {
			@Override
			protected OWLOntologyChange performSynchronisedCall() {
				return getManipulator().addExactObjectClassExpression( className, propertyName, cardinality, valueName);
			}
		}.call();
	}

    /**
     * Returns the changes to make a class be a sub class of a data property expression
     * minimally identified by a given cardinality class restriction.
     * In symbols: {@code C &sub; p(&lt;<sub>d</sub> D)}, where: {@code C} is the class, {@code p} the object property,
     * {@code V} is the class value, {@code d} the cardinality and {@code D}, the type of data (supported {@link String}, {@link Integer}, {@link Double},
     * {@link Float} and {@link Long}).
     * Changes will be buffered if {@link OWLManipulator#isChangeBuffering()} is {@code true}, else they will be applied immediately.
     * @param cl the class object of the sub-setting relation ({@code C}).
     * @param property the property of the sub-setting relation ({@code p}).
     * @param cardinality the cardinality of the minimal relation ({@code d}).
     * @param type the Class representing a supported data type for the sub-setting relation ({@code D}).
     * @return the changes to be applied in order to make a class being a sub-set of a minimum number of
     * properties restricted to a data type.
     */
	public OWLOntologyChange addMinDataClassExpression(OWLClass cl, OWLDataProperty property, int cardinality, Class type){
		List< Lock> mutexes = getMutexes( mutexReasoner, mutexAddCardinalityData);
		return new OWLReferencesCaller< OWLOntologyChange>(  mutexes, this) {
			@Override
			protected OWLOntologyChange performSynchronisedCall() {
				return getManipulator().addMinDataClassExpression( cl, property, cardinality, type);
			}
		}.call();
	}
    /**
     * Returns the changes to make a class be a sub class of a data property expression
     * minimally identified by a given cardinality class restriction.
     * In symbols: {@code C &sub; p(&lt;<sub>d</sub> D)}, where: {@code C} is the class, {@code p} the object property,
     * {@code V} is the class value, {@code d} the cardinality and {@code D}, the type of data (supported {@link String}, {@link Integer}, {@link Double},
     * {@link Float} and {@link Long}).
     * Changes will be buffered if {@link OWLManipulator#isChangeBuffering()} is {@code true}, else they will be applied immediately.
     * @param className the name of the class object of the sub-setting relation ({@code C}).
     * @param propertyName the name of the property of the sub-setting relation ({@code p}).
     * @param cardinality the cardinality of the minimal relation ({@code d}).
     * @param type the Class representing a supported data type for the sub-setting relation ({@code D}).
     * @return the changes to be applied in order to make a class being a sub-set of a minimum number of
     * properties restricted to a data type.
     */
	public OWLOntologyChange addMinDataClassExpression( String className, String propertyName, int cardinality, Class type){
		List< Lock> mutexes = getMutexes( mutexReasoner, mutexAddCardinalityData);
		return new OWLReferencesCaller< OWLOntologyChange>(  mutexes, this) {
			@Override
			protected OWLOntologyChange performSynchronisedCall() {
				return getManipulator().addMinDataClassExpression( className, propertyName, cardinality, type);
			}
		}.call();
	}
    /**
     * Returns the changes to make a class be a sub class of a data property expression
     * maximally identified by a given cardinality class restriction.
     * In symbols: {@code C &sub; p(&gt;<sub>d</sub> D)}, where: {@code C} is the class, {@code p} the object property,
     * {@code V} is the class value, {@code d} the cardinality and {@code D}, the type of data (supported {@link String}, {@link Integer}, {@link Double},
     * {@link Float} and {@link Long}).
     * Changes will be buffered if {@link OWLManipulator#isChangeBuffering()} is {@code true}, else they will be applied immediately.
     * @param cl the class object of the sub-setting relation ({@code C}).
     * @param property the property of the sub-setting relation ({@code p}).
     * @param cardinality the cardinality of the minimal relation ({@code d}).
     * @param type the Class representing a supported data type for the sub-setting relation ({@code D}).
     * @return the changes to be applied in order to make a class being a sub-set of a maximum number of
     * properties restricted to a data type.
     */
	public OWLOntologyChange addMaxDataClassExpression(OWLClass cl, OWLDataProperty property, int cardinality, Class type){
		List< Lock> mutexes = getMutexes( mutexReasoner, mutexAddCardinalityData);
		return new OWLReferencesCaller< OWLOntologyChange>(  mutexes, this) {
			@Override
			protected OWLOntologyChange performSynchronisedCall() {
				return getManipulator().addMaxDataClassExpression( cl, property, cardinality, type);
			}
		}.call();
	}
    /**
     * Returns the changes to make a class be a sub class of a data property expression
     * maximally identified by a given cardinality class restriction.
     * In symbols: {@code C &sub; p(&gt;<sub>d</sub> D)}, where: {@code C} is the class, {@code p} the object property,
     * {@code V} is the class value, {@code d} the cardinality and {@code D}, the type of data (supported {@link String}, {@link Integer}, {@link Double},
     * {@link Float} and {@link Long}).
     * Changes will be buffered if {@link OWLManipulator#isChangeBuffering()} is {@code true}, else they will be applied immediately.
     * @param className the name of the class object of the sub-setting relation ({@code C}).
     * @param propertyName the name of the property of the sub-setting relation ({@code p}).
     * @param cardinality the cardinality of the minimal relation ({@code d}).
     * @param type the Class representing a supported data type for the sub-setting relation ({@code D}).
     * @return the changes to be applied in order to make a class being a sub-set of a maximum number of
     * properties restricted to a data type.
     */
	public OWLOntologyChange addMaximalDataClassExpression( String className, String propertyName, int cardinality, Class type){
		List< Lock> mutexes = getMutexes( mutexReasoner, mutexAddCardinalityData);
		return new OWLReferencesCaller< OWLOntologyChange>(  mutexes, this) {
			@Override
			protected OWLOntologyChange performSynchronisedCall() {
				return getManipulator().addMaxDataClassExpression( className, propertyName, cardinality, type);
			}
		}.call();
	}
    /**
     * Returns the changes to make a class be a sub class of a data property expression
     * exactly identified by a given cardinality class restriction.
     * In symbols: {@code C &sub; p(=<sub>d</sub> D)}, where: {@code C} is the class, {@code p} the object property,
     * {@code V} is the class value, {@code d} the cardinality and {@code D}, the type of data (supported {@link String}, {@link Integer}, {@link Double},
     * {@link Float} and {@link Long}).
     * Changes will be buffered if {@link OWLManipulator#isChangeBuffering()} is {@code true}, else they will be applied immediately.
     * @param cl the class object of the sub-setting relation ({@code C}).
     * @param property the property of the sub-setting relation ({@code p}).
     * @param cardinality the cardinality of the minimal relation ({@code d}).
     * @param type the Class representing a supported data type for the sub-setting relation ({@code D}).
     * @return the changes to be applied in order to make a class being a sub-set of an exact number of
     * properties restricted to a data type.
     */
	public OWLOntologyChange addExactDataClassExpression(OWLClass cl, OWLDataProperty property, int cardinality, Class type){
		List< Lock> mutexes = getMutexes( mutexReasoner, mutexAddCardinalityData);
		return new OWLReferencesCaller< OWLOntologyChange>(  mutexes, this) {
			@Override
			protected OWLOntologyChange performSynchronisedCall() {
				return getManipulator().addExactDataClassExpression( cl, property, cardinality, type);
			}
		}.call();
	}
    /**
     * Returns the changes to make a class be a sub class of a data property expression
     * exactly identified by a given cardinality class restriction.
     * In symbols: {@code C &sub; p(=<sub>d</sub> D)}, where: {@code C} is the class, {@code p} the object property,
     * {@code V} is the class value, {@code d} the cardinality and {@code D}, the type of data (supported {@link String}, {@link Integer}, {@link Double},
     * {@link Float} and {@link Long}).
     * Changes will be buffered if {@link OWLManipulator#isChangeBuffering()} is {@code true}, else they will be applied immediately.
     * @param className the name of the class object of the sub-setting relation ({@code C}).
     * @param propertyName the name of the property of the sub-setting relation ({@code p}).
     * @param cardinality the cardinality of the minimal relation ({@code d}).
     * @param type the Class representing a supported data type for the sub-setting relation ({@code D}).
     * @return the changes to be applied in order to make a class being a sub-set of an exact number of
     * properties restricted to a data type.
     */
	public OWLOntologyChange addExactDataClassExpression( String className, String propertyName, int cardinality, Class type){
		List< Lock> mutexes = getMutexes( mutexReasoner, mutexAddCardinalityData);
		return new OWLReferencesCaller< OWLOntologyChange>(  mutexes, this) {
			@Override
			protected OWLOntologyChange performSynchronisedCall() {
				return getManipulator().addExactDataClassExpression( className, propertyName, cardinality, type);
			}
		}.call();
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
        List< Lock> mutexes = getMutexes( mutexReasoner, mutexConvertEquivalentClass);
        return new OWLReferencesCaller< List< OWLOntologyChange>>(  mutexes, this) {
            @Override
            protected List< OWLOntologyChange> performSynchronisedCall() {
                return getManipulator().convertSuperClassesToEquivalentClass( cl);
            }
        }.call();
    }
    /**
     * Given a class {@code C}, it uses {@link org.semanticweb.owlapi.change.ConvertEquivalentClassesToSuperClasses}
     * to convert all the sub class axioms of {@code C} into a conjunctions of expressions
     * in the definition of the class itself.
     * @param className the name of the class to be converted from sub classing to equivalent expression.
     * @return the changes to be applied in order to make all the sub axioms of a class being
     * the conjunction of its equivalent expression.
     */
    public List<OWLOntologyChange> convertSuperClassesToEquivalentClass( String className){
        List< Lock> mutexes = getMutexes( mutexReasoner, mutexConvertEquivalentClass);
        return new OWLReferencesCaller< List< OWLOntologyChange>>(  mutexes, this) {
            @Override
            protected List< OWLOntologyChange> performSynchronisedCall() {
                return getManipulator().convertSuperClassesToEquivalentClass( className);
            }
        }.call();
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
		List< Lock> mutexes = getMutexes( mutexReasoner, mutexRemoveObjPropB2Ind);
		return new OWLReferencesCaller< OWLOntologyChange>(  mutexes, this) {
			@Override
			protected OWLOntologyChange performSynchronisedCall() {
				return getManipulator().removeObjectPropertyB2Individual( ind, prop,value);
			}
		}.call();
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
		List< Lock> mutexes = getMutexes( mutexReasoner, mutexRemoveObjPropB2Ind);
		return new OWLReferencesCaller< OWLOntologyChange>(  mutexes, this) {
			@Override
			protected OWLOntologyChange performSynchronisedCall() {
				return getManipulator().removeObjectPropertyB2Individual( individualName, propName,valueName);
			}
		}.call();
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
		List< Lock> mutexes = getMutexes( mutexReasoner, mutexRemoveDataPropB2Ind);
		return new OWLReferencesCaller< OWLOntologyChange>(  mutexes, this) {
			@Override
			protected OWLOntologyChange performSynchronisedCall() {
				return getManipulator().removeDataPropertyB2Individual( ind, prop, value);
			}
		}.call();
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
		List< Lock> mutexes = getMutexes( mutexReasoner, mutexRemoveDataPropB2Ind);
		return new OWLReferencesCaller< OWLOntologyChange>(  mutexes, this) {
			@Override
			protected OWLOntologyChange performSynchronisedCall() {
				return getManipulator().removeDataPropertyB2Individual( individualName, propertyName, value);
			}
		}.call();
	}


	/**
	 * This method calls {@link OWLManipulator#removeIndividualB2Class(OWLNamedIndividual, OWLClass)}
	 * in order to remove the assertion of an individual to be classified into a class.
	 * @param ind the individual which has to be removed from a class.
	 * @param cls the class from which the individual should be removed.
	 * @return the changes to be done in order to remove an individual from a class. (see {@link OWLManipulator} for more info)
	 */
	public OWLOntologyChange removeIndividualB2Class(OWLNamedIndividual ind, OWLClass cls){
		List< Lock> mutexes = getMutexes( mutexReasoner, mutexRemoveIndB2Class);
		return new OWLReferencesCaller< OWLOntologyChange>(  mutexes, this) {
			@Override
			protected OWLOntologyChange performSynchronisedCall() {
				return getManipulator().removeIndividualB2Class(ind, cls);
			}
		}.call();
	}
	/**
	 * This method calls {@link OWLManipulator#removeIndividualB2Class(String, String)}
	 * in order to remove the assertion of an individual to be classified into a class.
	 * @param individualName the name of the individual which has to be removed from a class.
	 * @param className the name of the class from which the individual should be removed.
	 * @return the changes to be done in order to remove an individual from a class. (see {@link OWLManipulator} for more info)
	 */
	public OWLOntologyChange removeIndividualB2Class(String individualName, String className){
		List< Lock> mutexes = getMutexes( mutexReasoner, mutexRemoveIndB2Class);
		return new OWLReferencesCaller< OWLOntologyChange>(  mutexes, this) {
			@Override
			protected OWLOntologyChange performSynchronisedCall() {
				return getManipulator().removeIndividualB2Class( individualName, className);
			}
		}.call();
	}

	/**
	 * This method calls {@link OWLManipulator#removeIndividual(OWLNamedIndividual)}
	 * in order to remove an individual from the ontology.
	 * @param individual the individual to be removed.
	 * @return the changes to be done in order to remove an individual from the ontology. (see {@link OWLManipulator} for more info)
	 */
	public List<RemoveAxiom> removeIndividual( OWLNamedIndividual individual){ 
		List< Lock> mutexes = getMutexes( mutexReasoner, mutexRemoveInd);
		return new OWLReferencesCaller< List< RemoveAxiom>>(  mutexes, this) {
			@Override
			protected List<RemoveAxiom> performSynchronisedCall() {
				return getManipulator().removeIndividual(individual);
			}
		}.call();
	}
	/**
	 * This method calls {@link OWLManipulator#removeIndividual(OWLNamedIndividual)}
	 * in order to remove an individual from the ontology.
	 * @param indName the name of the individual to be removed.
	 * @return the changes to be done in order to remove an individual from the ontology. (see {@link OWLManipulator} for more info)
	 */
	public List<RemoveAxiom> removeIndividual( String indName){ 
		List< Lock> mutexes = getMutexes( mutexReasoner, mutexRemoveInd);
		return new OWLReferencesCaller< List< RemoveAxiom>>(  mutexes, this) {
			@Override
			protected List< RemoveAxiom> performSynchronisedCall() {
				return getManipulator().removeIndividual( indName);
			}
		}.call();
	}
	/**
	 * This method calls {@link OWLManipulator#removeIndividual(Set)}
	 * in order to remove an individual from the ontology.
	 * @param individuals the set of individuals to be removed.
	 * @return the changes to be done in order to remove the set of individuals from the ontology. (see {@link OWLManipulator} for more info)
	 */
	public List<OWLOntologyChange> removeIndividual( Set< OWLNamedIndividual> individuals){ 
		List< Lock> mutexes = getMutexes( mutexReasoner, mutexRemoveInd);
		return new OWLReferencesCaller< List< OWLOntologyChange>>(  mutexes, this) {
			@Override
			protected List< OWLOntologyChange> performSynchronisedCall() {
				return getManipulator().removeIndividual(individuals);
			}
		}.call();
	}

	/** This method calls {@link OWLManipulator#removeClass(String)}
	 * in order to remove a class into the ontology.
	 * @param className the name of the class to be removed from the ontology.
	 * @return the changes to be done in order to remove a class from the ontology. (see {@link OWLManipulator} for more info)
	 */
	public OWLOntologyChange removeClass( String className){
		List< Lock> mutexes = getMutexes( mutexReasoner, mutexRemoveClass);
		return new OWLReferencesCaller< OWLOntologyChange>(  mutexes, this) {
			@Override
			protected OWLOntologyChange performSynchronisedCall() {
				return getManipulator().removeClass( className);
			}
		}.call();
	}
	/**
	 * This method calls {@link OWLManipulator#removeClass(OWLClass)}
	 * in order to remove a class into the ontology.
	 * @param cls the class to be removed into the ontology.
	 * @return the changes to be done in order to remove a class from the ontology an individual into a class. (see {@link OWLManipulator} for more info)
	 */
	public OWLOntologyChange removeClass( OWLClass cls){
		List< Lock> mutexes = getMutexes( mutexReasoner, mutexRemoveClass);
		return new OWLReferencesCaller< OWLOntologyChange>(  mutexes, this) {
			@Override
			protected OWLOntologyChange performSynchronisedCall() {
				return getManipulator().removeClass( cls);
			}
		}.call();
	}
	
	/**
	 * This method calls {@link OWLManipulator#removeSubClassOf(String, String)}
	 * in order to remove a class assertion to be a sub class of a specified entity.
	 * @param superClassName the name of the class in which remove the sub class.
	 * @param subClassName the name of the class to remove as sub class of the specified class.
	 * @return the changes to be done in order to remove a sub class assertion by specifying its super class. (see {@link OWLManipulator} for more info)
	 */
	public OWLOntologyChange removeSubClassOf( String superClassName, String subClassName){
		List< Lock> mutexes = getMutexes( mutexReasoner, mutexRemoveSubClass);
		return new OWLReferencesCaller< OWLOntologyChange>(  mutexes, this) {
			@Override
			protected OWLOntologyChange performSynchronisedCall() {
				return getManipulator().removeSubClassOf( superClassName, subClassName);
			}
		}.call();
	}
	/**
	 * This method calls {@link OWLManipulator#removeSubClassOf(OWLClass, OWLClass)}
	 * in order to remove a class assertion to be a sub class of a specified entity.
	 * @param superClass the class in which remove the sub class.
	 * @param subClass the class to remove as sub class of the specified class.
	 * @return the changes to be done in order to remove a sub class assertion by specifying its super class. (see {@link OWLManipulator} for more info)
	 */
	public OWLOntologyChange removeSubClassOf( OWLClass superClass, OWLClass subClass){
		List< Lock> mutexes = getMutexes( mutexReasoner, mutexRemoveSubClass);
		return new OWLReferencesCaller< OWLOntologyChange>(  mutexes, this) {
			@Override
			protected OWLOntologyChange performSynchronisedCall() {
				return getManipulator().removeSubClassOf( superClass, subClass);
			}
		}.call();
	}


	/**
	 * This method calls {@link OWLManipulator#removeSubDataPropertyOf(String, String)}
	 * in order to remove a data property assertion to be a sub-property of a specified entity.
	 * @param superPropertyName the name of the data property in which remove the sub property.
	 * @param subPropertyName the name of the data property to remove as sub property of the specified class.
	 * @return the changes to be done in order to remove a sub data property assertion by specifying its super property. (see {@link OWLManipulator} for more info)
	 */
	public OWLOntologyChange removeSubDataPropertyOf( String superPropertyName, String subPropertyName){
		List< Lock> mutexes = getMutexes( mutexReasoner, mutexRemoveSubDataProperty);
		return new OWLReferencesCaller< OWLOntologyChange>(  mutexes, this) {
			@Override
			protected OWLOntologyChange performSynchronisedCall() {
				return getManipulator().removeSubDataPropertyOf( superPropertyName, subPropertyName);
			}
		}.call();
	}
	/**
	 * This method calls {@link OWLManipulator#removeSubDataPropertyOf(OWLDataProperty, OWLDataProperty)}
	 * in order to remove a data property assertion to be a sub property of a specified entity.
	 * @param superProperty the data property in which remove the sub property.
	 * @param subProperty the data property to remove as sub property of the specified class.
	 * @return the changes to be done in order to remove a sub data property assertion by specifying its super property. (see {@link OWLManipulator} for more info)
	 */
	public OWLOntologyChange removeSubDataPropertyOf( OWLDataProperty superProperty, OWLDataProperty subProperty){
		List< Lock> mutexes = getMutexes( mutexReasoner, mutexRemoveSubDataProperty);
		return new OWLReferencesCaller< OWLOntologyChange>(  mutexes, this) {
			@Override
			protected OWLOntologyChange performSynchronisedCall() {
				return getManipulator().removeSubDataPropertyOf( superProperty, subProperty);
			}
		}.call();
	}

	/**
	 * This method calls {@link OWLManipulator#removeSubObjectPropertyOf(String, String)}
	 * in order to remove an object property assertion to be a sub property of a specified entity.
	 * @param superPropertyName the name of the object property in which remove the sub property.
	 * @param subPropertyName the name of the object property to remove as sub property of the specified property.
	 * @return the changes to be done in order to remove a sub object property assertion by specifying its super property. (see {@link OWLManipulator} for more info)
	 */
	public OWLOntologyChange removeSubObjectPropertyOf( String superPropertyName, String subPropertyName){
		List< Lock> mutexes = getMutexes( mutexReasoner, mutexRemoveSubObjectProperty);
		return new OWLReferencesCaller< OWLOntologyChange>(  mutexes, this) {
			@Override
			protected OWLOntologyChange performSynchronisedCall() {
				return getManipulator().removeSubObjectPropertyOf( superPropertyName, subPropertyName);
			}
		}.call();
	}
	/**
	 * This method calls {@link OWLManipulator#removeSubObjectPropertyOf(OWLObjectProperty, OWLObjectProperty)}
	 * in order to remove a object property assertion to be a sub property of a specified entity.
	 * @param superProperty the object property in which remove the sub property.
	 * @param subProperty the class to remove as sub object property of the specified property.
	 * @return the changes to be done in order to remove a sub object property assertion by specifying its super property. (see {@link OWLManipulator} for more info)
	 */
	public OWLOntologyChange removeSubObjectPropertyOf( OWLObjectProperty superProperty, OWLObjectProperty subProperty){
		List< Lock> mutexes = getMutexes( mutexReasoner, mutexRemoveSubObjectProperty);
		return new OWLReferencesCaller< OWLOntologyChange>(  mutexes, this) {
			@Override
			protected OWLOntologyChange performSynchronisedCall() {
				return getManipulator().removeSubObjectPropertyOf( superProperty, subProperty);
			}
		}.call();
	}

    // depending on the same mutex as adding min/max object class expression
    /**
     * Returns the changes to make a class no more being a sub class of an
     * object property in existence with a class value.
     * In symbols, it will be no more true that: {@code C &sub; p(&exist; V)},
     * where: {@code C} is the class, {@code p} the object property
     * and {@code V}, the class value.
     * Changes will be buffered if {@link OWLManipulator#isChangeBuffering()} is {@code true}, else they will be applied immediately.
     * @param cl the class object of the sub-setting relation ({@code C}).
     * @param property the property of the sub-setting relation ({@code p}).
     * @param value the value of the sub-setting relation ({@code V}).
     * @return the changes to be applied in order to remove the fact that a class
     * is a sub-set of an existential property.
     */
	public OWLOntologyChange removeSomeObjectClassExpression(OWLClass cl, OWLObjectProperty property, OWLClass value){
		List< Lock> mutexes = getMutexes( mutexReasoner, mutexAddRemovingClassDefinition);
		return new OWLReferencesCaller< OWLOntologyChange>(  mutexes, this) {
			@Override
			protected OWLOntologyChange performSynchronisedCall() {
				return getManipulator().removeSomeObjectClassExpression( cl, property, value);
			}
		}.call();
	}
    /**
     * Returns the changes to make a class no more being a sub class of an
     * object property in existence with a class value.
     * In symbols, it will be no more true that: {@code C &sub; p(&exist; V)},
     * where: {@code C} is the class, {@code p} the object property
     * and {@code V}, the class value.
     * Changes will be buffered if {@link OWLManipulator#isChangeBuffering()} is {@code true}, else they will be applied immediately.
     * @param className the name of the class object of the sub-setting relation ({@code C}).
     * @param propertyName the name of the property of the sub-setting relation ({@code p}).
     * @param valueName the name of the value of the sub-setting relation ({@code V}).
     * @return the changes to be applied in order to remove the fact that a class
     * is a sub-set of an existential property.
     */
	public OWLOntologyChange removeSomeObjectClassExpression(String className, String propertyName, String valueName){
		List< Lock> mutexes = getMutexes( mutexReasoner, mutexAddRemovingClassDefinition);
		return new OWLReferencesCaller< OWLOntologyChange>(  mutexes, this) {
			@Override
			protected OWLOntologyChange performSynchronisedCall() {
				return getManipulator().removeSomeObjectClassExpression( className, propertyName, valueName);
			}
		}.call();
	}
    /**
     * Returns the changes to make a class no more being a sub class of an
     * object property universally identified with a class value.
     * In symbols, it will be no more true that: {@code C &sub; p(&forall; V)},
     * where: {@code C} is the class, {@code p} the object property
     * and {@code V}, the class value.
     * Changes will be buffered if {@link OWLManipulator#isChangeBuffering()} is {@code true}, else they will be applied immediately.
     * @param cl the class object of the sub-setting relation ({@code C}).
     * @param property the property of the sub-setting relation ({@code p}).
     * @param value the value of the sub-setting relation ({@code V}).
     * @return the changes to be applied in order to remove the fact that a class
     * is a sub-set of an universal property.
     */
	public OWLOntologyChange removeOnlyObjectClassExpression(OWLClass cl, OWLObjectProperty property, OWLClass value){
		List< Lock> mutexes = getMutexes( mutexReasoner, mutexAddRemovingClassDefinition);
		return new OWLReferencesCaller< OWLOntologyChange>(  mutexes, this) {
			@Override
			protected OWLOntologyChange performSynchronisedCall() {
				return getManipulator().removeOnlyObjectClassExpression( cl, property, value);
			}
		}.call();
	}
    /**
     * Returns the changes to make a class no more being a sub class of an
     * object property universally identified with a class value.
     * In symbols, it will be no more true that: {@code C &sub; p(&forall; V)},
     * where: {@code C} is the class, {@code p} the object property
     * and {@code V}, the class value.
     * Changes will be buffered if {@link OWLManipulator#isChangeBuffering()} is {@code true}, else they will be applied immediately.
     * @param className the name of the class object of the sub-setting relation ({@code C}).
     * @param propertyName the name of the property of the sub-setting relation ({@code p}).
     * @param valueName the name of the value of the sub-setting relation ({@code V}).
     * @return the changes to be applied in order to remove the fact that a class
     * is a sub-set of an universal property.
     */
	public OWLOntologyChange removeOnlyObjectClassExpression(String className, String propertyName, String valueName){
		List< Lock> mutexes = getMutexes( mutexReasoner, mutexAddRemovingClassDefinition);
		return new OWLReferencesCaller< OWLOntologyChange>(  mutexes, this) {
			@Override
			protected OWLOntologyChange performSynchronisedCall() {
				return getManipulator().removeOnlyObjectClassExpression( className, propertyName, valueName);
			}
		}.call();
	}

	// depending on the same mutex as adding min/max object class expression
    /**
     * Returns the changes to make a class not being a sub class of a data property,
     * in existence with a data type value, anymore.
     * In symbols, it will be not long true that: {@code C &sub; p(&exist; D)},
     * where: {@code C} is the class, {@code p} the data property
     * and {@code D}, the type of data (supported {@link String}, {@link Integer}, {@link Double},
     * {@link Float} and {@link Long}).
     * Changes will be buffered if {@link OWLManipulator#isChangeBuffering()} is {@code true}, else they will be applied immediately.
     * @param cl the class object of the sub-setting relation ({@code C}).
     * @param property the property of the sub-setting relation ({@code p}).
     * @param type the Class representing a supported data type for the sub-setting relation ({@code D}).
     * @return the changes to be applied in order to remove the fact that
     * a class is a sub-set of an existential property.
     */
    public OWLOntologyChange removeSomeDataClassExpression(OWLClass cl, OWLDataProperty property, Class type){
		List< Lock> mutexes = getMutexes( mutexReasoner, mutexAddRemovingClassDefinition);
		return new OWLReferencesCaller< OWLOntologyChange>(  mutexes, this) {
			@Override
			protected OWLOntologyChange performSynchronisedCall() {
				return getManipulator().removeSomeDataClassExpression( cl, property, type);
			}
		}.call();
	}
    /**
     * Returns the changes to make a class not being a sub class of a data property,
     * in existence with a data type value, anymore.
     * In symbols, it will be not long true that: {@code C &sub; p(&exist; D)},
     * where: {@code C} is the class, {@code p} the data property
     * and {@code D}, the type of data (supported {@link String}, {@link Integer}, {@link Double},
     * {@link Float} and {@link Long}).
     * Changes will be buffered if {@link OWLManipulator#isChangeBuffering()} is {@code true}, else they will be applied immediately.
     * @param className the name of the class object of the sub-setting relation ({@code C}).
     * @param propertyName the name of the property of the sub-setting relation ({@code p}).
     * @param type the Class representing a supported data type for the sub-setting relation ({@code D}).
     * @return the changes to be applied in order to remove the fact that
     * a class is a sub-set of an existential property.
     */
	public OWLOntologyChange removeSomeDataClassExpression(String className, String propertyName, Class type){
		List< Lock> mutexes = getMutexes( mutexReasoner, mutexAddRemovingClassDefinition);
		return new OWLReferencesCaller< OWLOntologyChange>(  mutexes, this) {
			@Override
			protected OWLOntologyChange performSynchronisedCall() {
				return getManipulator().removeSomeDataClassExpression( className, propertyName, type);
			}
		}.call();
	}
    /**
     * Returns the changes to make a class not being a sub class of a data property,
     * universally qualified by a data type value, anymore.
     * In symbols, it will be not long true that: {@code C &sub; p(&forall; D)},
     * where: {@code C} is the class, {@code p} the data property
     * and {@code D}, the type of data (supported {@link String}, {@link Integer}, {@link Double},
     * {@link Float} and {@link Long}).
     * Changes will be buffered if {@link OWLManipulator#isChangeBuffering()} is {@code true}, else they will be applied immediately.
     * @param cl the class object of the sub-setting relation ({@code C}).
     * @param property the property of the sub-setting relation ({@code p}).
     * @param type the Class representing a supported data type for the sub-setting relation ({@code D}).
     * @return the changes to be applied in order to remove the fact that
     * a class is a sub-set of an universal property.
     */
	public OWLOntologyChange removeOnlyDataClassExpression(OWLClass cl, OWLDataProperty property, Class type){
		List< Lock> mutexes = getMutexes( mutexReasoner, mutexAddRemovingClassDefinition);
		return new OWLReferencesCaller< OWLOntologyChange>(  mutexes, this) {
			@Override
			protected OWLOntologyChange performSynchronisedCall() {
				return getManipulator().removeOnlyDataClassExpression( cl, property, type);
			}
		}.call();
	}
    /**
     * Returns the changes to make a class not being a sub class of a data property,
     * universally qualified by a data type value, anymore.
     * In symbols, it will be not long true that: {@code C &sub; p(&forall; D)},
     * where: {@code C} is the class, {@code p} the data property
     * and {@code D}, the type of data (supported {@link String}, {@link Integer}, {@link Double},
     * {@link Float} and {@link Long}).
     * Changes will be buffered if {@link OWLManipulator#isChangeBuffering()} is {@code true}, else they will be applied immediately.
     * @param className the name of the class object of the sub-setting relation ({@code C}).
     * @param propertyName the name of the property of the sub-setting relation ({@code p}).
     * @param type the Class representing a supported data type for the sub-setting relation ({@code D}).
     * @return the changes to be applied in order to remove the fact that
     * a class is a sub-set of an universal property.
     */
	public OWLOntologyChange removeOnlyDataClassExpression(String className, String propertyName, Class type){
		List< Lock> mutexes = getMutexes( mutexReasoner, mutexAddRemovingClassDefinition);
		return new OWLReferencesCaller< OWLOntologyChange>(  mutexes, this) {
			@Override
			protected OWLOntologyChange performSynchronisedCall() {
				return getManipulator().removeOnlyDataClassExpression( className, propertyName, type);
			}
		}.call();
	}

	// depending on the same mutex as adding min/max object class expression
    /**
     * Returns the changes to make a class not being a sub class of an object property expression,
     * minimally identified by a given cardinality class restriction, anymore.
     * In symbols, it will be no more true that:: {@code C &sub; p(&lt;<sub>d</sub> V)},
     * where: {@code C} is the class, {@code p} the object property,
     * {@code V} is the class value and {@code d}, the cardinality.
     * Changes will be buffered if {@link OWLManipulator#isChangeBuffering()} is {@code true}, else they will be applied immediately.
     * @param cl the class object of the sub-setting relation ({@code C}).
     * @param property the property of the sub-setting relation ({@code p}).
     * @param cardinality the cardinality of the minimal relation ({@code d}).
     * @param value the value of the sub-setting relation ({@code V}).
     * @return the changes to be applied in order to remove tha fact that
     * a class is a sub-set of a minimum number of properties
     * restricted to a class.
     */
	public OWLOntologyChange removeMinObjectClassExpression(OWLClass cl, OWLObjectProperty property, int cardinality, OWLClass value){
		List< Lock> mutexes = getMutexes( mutexReasoner, mutexAddRemovingClassDefinition);
		return new OWLReferencesCaller< OWLOntologyChange>(  mutexes, this) {
			@Override
			protected OWLOntologyChange performSynchronisedCall() {
				return getManipulator().removeMinObjectClassExpression( cl, property, cardinality, value);
			}
		}.call();
	}
    /**
     * Returns the changes to make a class not being a sub class of an object property expression,
     * minimally identified by a given cardinality class restriction, anymore.
     * In symbols, it will be no more true that:: {@code C &sub; p(&lt;<sub>d</sub> V)},
     * where: {@code C} is the class, {@code p} the object property,
     * {@code V} is the class value and {@code d}, the cardinality.
     * Changes will be buffered if {@link OWLManipulator#isChangeBuffering()} is {@code true}, else they will be applied immediately.
     * @param className the name of the class object of the sub-setting relation ({@code C}).
     * @param propertyName the name of the property of the sub-setting relation ({@code p}).
     * @param cardinality the cardinality of the minimal relation ({@code d}).
     * @param valueName the name of the value of the sub-setting relation ({@code V}).
     * @return the changes to be applied in order to remove tha fact that
     * a class is a sub-set of a minimum number of properties
     * restricted to a class.
     */
	public OWLOntologyChange removeMinObjectClassExpression(String className, String propertyName, int cardinality, String valueName){
		List< Lock> mutexes = getMutexes( mutexReasoner, mutexAddRemovingClassDefinition);
		return new OWLReferencesCaller< OWLOntologyChange>(  mutexes, this) {
			@Override
			protected OWLOntologyChange performSynchronisedCall() {
				return getManipulator().removeMinObjectClassExpression( className, propertyName, cardinality, valueName);
			}
		}.call();
	}
    /**
     * Returns the changes to make a class not being a sub class of an object property expression,
     * maximally identified by a given cardinality class restriction, anymore.
     * In symbols, it will be no more true that:: {@code C &sub; p(&gt;<sub>d</sub> V)},
     * where: {@code C} is the class, {@code p} the object property,
     * {@code V} is the class value and {@code d}, the cardinality.
     * Changes will be buffered if {@link OWLManipulator#isChangeBuffering()} is {@code true}, else they will be applied immediately.
     * @param cl the class object of the sub-setting relation ({@code C}).
     * @param property the property of the sub-setting relation ({@code p}).
     * @param cardinality the cardinality of the minimal relation ({@code d}).
     * @param value the value of the sub-setting relation ({@code V}).
     * @return the changes to be applied in order to remove tha fact that
     * a class is a sub-set of a maximum number of properties
     * restricted to a class.
     */
	public OWLOntologyChange removeMaxObjectClassExpression(OWLClass cl, OWLObjectProperty property, int cardinality, OWLClass value){
		List< Lock> mutexes = getMutexes( mutexReasoner, mutexAddRemovingClassDefinition);
		return new OWLReferencesCaller< OWLOntologyChange>(  mutexes, this) {
			@Override
			protected OWLOntologyChange performSynchronisedCall() {
				return getManipulator().removeMaxObjectClassExpression( cl, property, cardinality, value);
			}
		}.call();
	}
    /**
     * Returns the changes to make a class not being a sub class of an object property expression,
     * maximally identified by a given cardinality class restriction, anymore.
     * In symbols, it will be no more true that:: {@code C &sub; p(&gt;<sub>d</sub> V)},
     * where: {@code C} is the class, {@code p} the object property,
     * {@code V} is the class value and {@code d}, the cardinality.
     * Changes will be buffered if {@link OWLManipulator#isChangeBuffering()} is {@code true}, else they will be applied immediately.
     * @param className the name of the class object of the sub-setting relation ({@code C}).
     * @param propertyName the name of the property of the sub-setting relation ({@code p}).
     * @param cardinality the cardinality of the minimal relation ({@code d}).
     * @param valueName the name of the value of the sub-setting relation ({@code V}).
     * @return the changes to be applied in order to remove tha fact that
     * a class is a sub-set of a maximum number of properties
     * restricted to a class.
     */
	public OWLOntologyChange removeMaxObjectClassExpression( String className, String propertyName, int cardinality, String valueName){
		List< Lock> mutexes = getMutexes( mutexReasoner, mutexAddRemovingClassDefinition);
		return new OWLReferencesCaller< OWLOntologyChange>(  mutexes, this) {
			@Override
			protected OWLOntologyChange performSynchronisedCall() {
				return getManipulator().removeMaxObjectClassExpression( className, propertyName, cardinality, valueName);
			}
		}.call();
	}
    /**
     * Returns the changes to make a class not being a sub class of an object property expression,
     * exactly identified by a given cardinality class restriction, anymore.
     * In symbols, it will be no more true that:: {@code C &sub; p(=<sub>d</sub> V)},
     * where: {@code C} is the class, {@code p} the object property,
     * {@code V} is the class value and {@code d}, the cardinality.
     * Changes will be buffered if {@link OWLManipulator#isChangeBuffering()} is {@code true}, else they will be applied immediately.
     * @param cl the class object of the sub-setting relation ({@code C}).
     * @param property the property of the sub-setting relation ({@code p}).
     * @param cardinality the cardinality of the minimal relation ({@code d}).
     * @param value the value of the sub-setting relation ({@code V}).
     * @return the changes to be applied in order to remove tha fact that
     * a class is a sub-set of a exact number of properties
     * restricted to a class.
     */
	public OWLOntologyChange removeExactObjectClassExpression(OWLClass cl, OWLObjectProperty property, int cardinality, OWLClass value){
		List< Lock> mutexes = getMutexes( mutexReasoner, mutexAddRemovingClassDefinition);
		return new OWLReferencesCaller< OWLOntologyChange>(  mutexes, this) {
			@Override
			protected OWLOntologyChange performSynchronisedCall() {
				return getManipulator().removeExactObjectClassExpression( cl, property, cardinality, value);
			}
		}.call();
	}
    /**
     * Returns the changes to make a class not being a sub class of an object property expression,
     * exactly identified by a given cardinality class restriction, anymore.
     * In symbols, it will be no more true that:: {@code C &sub; p(=<sub>d</sub> V)},
     * where: {@code C} is the class, {@code p} the object property,
     * {@code V} is the class value and {@code d}, the cardinality.
     * Changes will be buffered if {@link OWLManipulator#isChangeBuffering()} is {@code true}, else they will be applied immediately.
     * @param className the nme of the class object of the sub-setting relation ({@code C}).
     * @param propertyName the name of the property of the sub-setting relation ({@code p}).
     * @param cardinality the cardinality of the minimal relation ({@code d}).
     * @param valueName the name of the value of the sub-setting relation ({@code V}).
     * @return the changes to be applied in order to remove tha fact that
     * a class is a sub-set of a exact number of properties
     * restricted to a class.
     */
	public OWLOntologyChange removeExactObjectClassExpression( String className, String propertyName, int cardinality, String valueName){
		List< Lock> mutexes = getMutexes( mutexReasoner, mutexAddRemovingClassDefinition);
		return new OWLReferencesCaller< OWLOntologyChange>(  mutexes, this) {
			@Override
			protected OWLOntologyChange performSynchronisedCall() {
				return getManipulator().removeExactObjectClassExpression( className, propertyName, cardinality, valueName);
			}
		}.call();
	}

	// depending on the same mutex as adding min/max data class expression
    /**
     * Returns the changes to make a class not being a sub class of a data property expression,
     * minimally identified by a given cardinality class restriction, anymore.
     * In symbols, it will not true that: {@code C &sub; p(&lt;<sub>d</sub> D)},
     * where: {@code C} is the class, {@code p} the object property,
     * {@code V} is the class value, {@code d} the cardinality and {@code D}, the type of data (supported {@link String}, {@link Integer}, {@link Double},
     * {@link Float} and {@link Long}).
     * Changes will be buffered if {@link OWLManipulator#isChangeBuffering()} is {@code true}, else they will be applied immediately.
     * @param cl the class object of the sub-setting relation ({@code C}).
     * @param property the property of the sub-setting relation ({@code p}).
     * @param cardinality the cardinality of the minimal relation ({@code d}).
     * @param type the Class representing a supported data type for the sub-setting relation ({@code D}).
     * @return the changes to be applied in order to remove the fact that a class
     * is sub-set of a minimum number of properties restricted to a data type.
     */
	public OWLOntologyChange removeMinDataClassExpression(OWLClass cl, OWLDataProperty property, int cardinality, Class type){
		List< Lock> mutexes = getMutexes( mutexReasoner, mutexAddCardinalityData);
		return new OWLReferencesCaller< OWLOntologyChange>(  mutexes, this) {
			@Override
			protected OWLOntologyChange performSynchronisedCall() {
				return getManipulator().removeMinDataClassExpression( cl, property, cardinality, type);
			}
		}.call();
	}
    /**
     * Returns the changes to make a class not being a sub class of a data property expression,
     * minimally identified by a given cardinality class restriction, anymore.
     * In symbols, it will not true that: {@code C &sub; p(&lt;<sub>d</sub> D)},
     * where: {@code C} is the class, {@code p} the object property,
     * {@code V} is the class value, {@code d} the cardinality and {@code D}, the type of data (supported {@link String}, {@link Integer}, {@link Double},
     * {@link Float} and {@link Long}).
     * Changes will be buffered if {@link OWLManipulator#isChangeBuffering()} is {@code true}, else they will be applied immediately.
     * @param className the name of the class object of the sub-setting relation ({@code C}).
     * @param propertyName the name of the property of the sub-setting relation ({@code p}).
     * @param cardinality the cardinality of the minimal relation ({@code d}).
     * @param type the Class representing a supported data type for the sub-setting relation ({@code D}).
     * @return the changes to be applied in order to remove the fact that a class
     * is sub-set of a minimum number of properties restricted to a data type.
     */
	public OWLOntologyChange removeMinDataClassExpression( String className, String propertyName, int cardinality, Class type){
		List< Lock> mutexes = getMutexes( mutexReasoner, mutexAddCardinalityData);
		return new OWLReferencesCaller< OWLOntologyChange>(  mutexes, this) {
			@Override
			protected OWLOntologyChange performSynchronisedCall() {
				return getManipulator().removeMinDataClassExpression( className, propertyName, cardinality, type);
			}
		}.call();
	}
    /**
     * Returns the changes to make a class not being a sub class of a data property expression,
     * maximally identified by a given cardinality class restriction, anymore.
     * In symbols, it will not true that: {@code C &sub; p(&gt;<sub>d</sub> D)},
     * where: {@code C} is the class, {@code p} the object property,
     * {@code V} is the class value, {@code d} the cardinality and {@code D}, the type of data (supported {@link String}, {@link Integer}, {@link Double},
     * {@link Float} and {@link Long}).
     * Changes will be buffered if {@link OWLManipulator#isChangeBuffering()} is {@code true}, else they will be applied immediately.
     * @param cl the class object of the sub-setting relation ({@code C}).
     * @param property the property of the sub-setting relation ({@code p}).
     * @param cardinality the cardinality of the minimal relation ({@code d}).
     * @param type the Class representing a supported data type for the sub-setting relation ({@code D}).
     * @return the changes to be applied in order to remove the fact that a class
     * is sub-set of a maximum number of properties restricted to a data type.
     */
	public OWLOntologyChange removeMaxDataClassExpression(OWLClass cl, OWLDataProperty property, int cardinality, Class type){
		List< Lock> mutexes = getMutexes( mutexReasoner, mutexAddCardinalityData);
		return new OWLReferencesCaller< OWLOntologyChange>(  mutexes, this) {
			@Override
			protected OWLOntologyChange performSynchronisedCall() {
				return getManipulator().removeMaxDataClassExpression( cl, property, cardinality, type);
			}
		}.call();
	}
    /**
     * Returns the changes to make a class not being a sub class of a data property expression,
     * maximally identified by a given cardinality class restriction, anymore.
     * In symbols, it will not true that: {@code C &sub; p(&gt;<sub>d</sub> D)},
     * where: {@code C} is the class, {@code p} the object property,
     * {@code V} is the class value, {@code d} the cardinality and {@code D}, the type of data (supported {@link String}, {@link Integer}, {@link Double},
     * {@link Float} and {@link Long}).
     * Changes will be buffered if {@link OWLManipulator#isChangeBuffering()} is {@code true}, else they will be applied immediately.
     * @param className the name of the class object of the sub-setting relation ({@code C}).
     * @param propertyName the name of the property of the sub-setting relation ({@code p}).
     * @param cardinality the cardinality of the minimal relation ({@code d}).
     * @param type the Class representing a supported data type for the sub-setting relation ({@code D}).
     * @return the changes to be applied in order to remove the fact that a class
     * is sub-set of a maximum number of properties restricted to a data type.
     */
	public OWLOntologyChange removeMaximalDataClassExpression( String className, String propertyName, int cardinality, Class type){
		List< Lock> mutexes = getMutexes( mutexReasoner, mutexAddCardinalityData);
		return new OWLReferencesCaller< OWLOntologyChange>(  mutexes, this) {
			@Override
			protected OWLOntologyChange performSynchronisedCall() {
				return getManipulator().removeMaxDataClassExpression( className, propertyName, cardinality, type);
			}
		}.call();
	}
    /**
     * Returns the changes to make a class not being a sub class of a data property expression,
     * exactly identified by a given cardinality class restriction, anymore.
     * In symbols, it will not true that: {@code C &sub; p(=<sub>d</sub> D)},
     * where: {@code C} is the class, {@code p} the object property,
     * {@code V} is the class value, {@code d} the cardinality and {@code D}, the type of data (supported {@link String}, {@link Integer}, {@link Double},
     * {@link Float} and {@link Long}).
     * Changes will be buffered if {@link OWLManipulator#isChangeBuffering()} is {@code true}, else they will be applied immediately.
     * @param cl the class object of the sub-setting relation ({@code C}).
     * @param property the property of the sub-setting relation ({@code p}).
     * @param cardinality the cardinality of the minimal relation ({@code d}).
     * @param type the Class representing a supported data type for the sub-setting relation ({@code D}).
     * @return the changes to be applied in order to remove the fact that a class
     * is sub-set of a exact number of properties restricted to a data type.
     */
	public OWLOntologyChange removeExactClassExpression(OWLClass cl, OWLDataProperty property, int cardinality, Class type){
		List< Lock> mutexes = getMutexes( mutexReasoner, mutexAddCardinalityData);
		return new OWLReferencesCaller< OWLOntologyChange>(  mutexes, this) {
			@Override
			protected OWLOntologyChange performSynchronisedCall() {
				return getManipulator().removeExactClassExpression( cl, property, cardinality, type);
			}
		}.call();
	}
    /**
     * Returns the changes to make a class not being a sub class of a data property expression,
     * exactly identified by a given cardinality class restriction, anymore.
     * In symbols, it will not true that: {@code C &sub; p(=<sub>d</sub> D)},
     * where: {@code C} is the class, {@code p} the object property,
     * {@code V} is the class value, {@code d} the cardinality and {@code D}, the type of data (supported {@link String}, {@link Integer}, {@link Double},
     * {@link Float} and {@link Long}).
     * Changes will be buffered if {@link OWLManipulator#isChangeBuffering()} is {@code true}, else they will be applied immediately.
     * @param className the name of the class object of the sub-setting relation ({@code C}).
     * @param propertyName the name of the property of the sub-setting relation ({@code p}).
     * @param cardinality the cardinality of the minimal relation ({@code d}).
     * @param type the Class representing a supported data type for the sub-setting relation ({@code D}).
     * @return the changes to be applied in order to remove the fact that a class
     * is sub-set of a exact number of properties restricted to a data type.
     */
	public OWLOntologyChange removeExactDataClassExpression( String className, String propertyName, int cardinality, Class type){
		List< Lock> mutexes = getMutexes( mutexReasoner, mutexAddCardinalityData);
		return new OWLReferencesCaller< OWLOntologyChange>(  mutexes, this) {
			@Override
			protected OWLOntologyChange performSynchronisedCall() {
				return getManipulator().removeExactDataClassExpression( className, propertyName, cardinality, type);
			}
		}.call();
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
			List<OWLOntologyChange> out = getManipulator().replaceDataPropertyB2Individual( ind, prop, oldValue, newValue);
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
		List< Lock> mutexes = getMutexes( mutexReasoner, mutexReplaceDataProp);
		return new OWLReferencesCaller< List<OWLOntologyChange>>(  mutexes, this) {
			@Override
			protected List<OWLOntologyChange> performSynchronisedCall() {
				return getManipulator().replaceDataPropertyB2Individual( ind, prop, oldValue, newValue);
			}
		}.call();
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
		List< Lock> mutexes = getMutexes( mutexReasoner);
		return new OWLReferencesCaller< List<OWLOntologyChange>>(  mutexes, this) {
			@Override
			protected List<OWLOntologyChange> performSynchronisedCall() {
				return getManipulator().replaceObjectProperty(ind, prop, oldValue, newValue);
			}
		}.call();
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
		List< Lock> mutexes = getMutexes( mutexReasoner);
		return new OWLReferencesCaller< List<OWLOntologyChange>>(  mutexes, this) {
			@Override
			protected List<OWLOntologyChange> performSynchronisedCall() {
				return getManipulator().replaceIndividualClass(ind, oldValue, newValue);
			}
		}.call();
	}


	/**
	 * This method calls {@link OWLManipulator#renameEntity(OWLEntity, IRI)}
	 * in order to rename an ontological entity with a new IRI.
	 * @param entity the ontological entity to be renamed.
	 * @param newIRI the new name for the ontological entity.
	 * @return the changes to be done in order to rename an ontological entity. (see {@link OWLManipulator} for more info)
	 */
	public List< OWLOntologyChange> renameEntity( OWLEntity entity, IRI newIRI){
		List< Lock> mutexes = getMutexes( mutexReasoner, mutexRename);
		return new OWLReferencesCaller< List<OWLOntologyChange>>(  mutexes, this) {
			@Override
			protected List<OWLOntologyChange> performSynchronisedCall() {
				return getManipulator().renameEntity(entity, newIRI);
			}
		}.call();
	}
	/**
	 * This method calls {@link OWLManipulator#renameEntity(OWLEntity entity, String)}
	 * in order to rename an ontological entity with a new Name base on the {@link OWLLibrary#getIriOntologyPath()}.
	 * @param entity the ontological entity to be renamed.
	 * @param newName the new name for the ontological entity to be combined with the semantic ontology IRI.
	 * @return the changes to be done in order to rename an ontological entity. (see {@link OWLManipulator} for more info)
	 */
	public List< OWLOntologyChange> renameEntity( OWLEntity entity, String newName){
		List< Lock> mutexes = getMutexes( mutexReasoner, mutexRename);
		return new OWLReferencesCaller< List<OWLOntologyChange>>(  mutexes, this) {
			@Override
			protected List<OWLOntologyChange> performSynchronisedCall() {
				return getManipulator().renameEntity(entity, newName);
			}
		}.call();
	}
	
	// ------------------------------------------------------------   methods for DISJOINT individuals
	/**
	 * This method calls {@link OWLManipulator#setDisjointIndividualName(Set)}
	 * in order to create (buffers and/or add) the disjoint axiom with respect to the individuals
	 * specified as input though their name. 
	 * @param individualNames the set of names of individuals to make disjointed.
	 * @return the changes to be done in order to add the disjoint individual axiom for all the inputs. (see {@link OWLManipulator} for more info)
	 */
	public OWLOntologyChange makeDisjointIndividualName( Set< String> individualNames){
		List< Lock> mutexes = getMutexes( mutexReasoner, mutexAddDisjointedInd);
		return new OWLReferencesCaller< OWLOntologyChange>(  mutexes, this) {
			@Override
			protected OWLOntologyChange performSynchronisedCall() {
				return getManipulator().setDisjointIndividualName( individualNames);
			}
		}.call();
	}
	/**
	 * This method calls {@link OWLManipulator#setDisjointIndividuals(Set)}
	 * in order to create (buffers and/or add) the disjoint axiom with respect to the 
	 * individuals set specified as input. 
	 * @param individuals the set of individuals to make disjointed.
	 * @return the changes to be done in order to add the disjoint individual axiom for all the inputs. (see {@link OWLManipulator} for more info)
	 */
	public OWLOntologyChange makeDisjointIndividuals( Set< OWLNamedIndividual> individuals){
		List< Lock> mutexes = getMutexes( mutexReasoner, mutexAddDisjointedInd);
		return new OWLReferencesCaller< OWLOntologyChange>(  mutexes, this) {
			@Override
			protected OWLOntologyChange performSynchronisedCall() {
				return getManipulator().setDisjointIndividuals( individuals);
			}
		}.call();
	}
	
	/**
	 * This method calls {@link OWLManipulator#removeDisjointIndividualName(Set)}
	 * in order to create (buffers and/or remove) the disjoint axioms (if their exists) with respect to the individuals
	 * specified as input though their name. 
	 * @param individualNames the set of names of individuals to make not disjointed anymore.
	 * @return the changes to be done in order to remove the disjoint individual axiom for all the inputs. (see {@link OWLManipulator} for more info)
	 */
	public OWLOntologyChange removeDisjointIndividualName( Set< String> individualNames){
		List< Lock> mutexes = getMutexes( mutexReasoner, mutexRemoveDisjointedInd);
		return new OWLReferencesCaller< OWLOntologyChange>(  mutexes, this) {
			@Override
			protected OWLOntologyChange performSynchronisedCall() {
				return  getManipulator().removeDisjointIndividualName( individualNames);
			}
		}.call();
	}
	/**
	 * This method calls {@link OWLManipulator#removeDisjointIndividuals(Set)}
	 * in order to create (buffers and/or remove) the disjoint axiom (if their exists) with respect to the 
	 * individuals set specified as input. 
	 * @param individuals the set of individuals to make not disjointed anymore.
	 * @return the changes to be done in order to remove the disjoint individual axiom for all the inputs. (see {@link OWLManipulator} for more info)
	 */
	public OWLOntologyChange removeDisjointIndividuals( Set< OWLNamedIndividual> individuals){
		List< Lock> mutexes = getMutexes( mutexReasoner, mutexRemoveDisjointedInd);
		return new OWLReferencesCaller< OWLOntologyChange>(  mutexes, this) {
			@Override
			protected OWLOntologyChange performSynchronisedCall() {
				return  getManipulator().removeDisjointIndividuals( individuals);
			}
		}.call();
	}

	/**
	 * This method calls {@link OWLManipulator#makeDisjointClassName(Set)}
	 * in order to create (buffers and/or add) the disjoint axiom with respect to the classes
	 * specified as input though their name. 
	 * @param classesName the set of names of class to make disjointed.
	 * @return the changes to be done in order to add the disjoint classes axiom for all the inputs. (see {@link OWLManipulator} for more info)
	 */
	public OWLOntologyChange makeDisjointClassName( Set< String> classesName){
		List< Lock> mutexes = getMutexes( mutexReasoner, mutexAddDisjointedCls);
		return new OWLReferencesCaller< OWLOntologyChange>(  mutexes, this) {
			@Override
			protected OWLOntologyChange performSynchronisedCall() {
				return getManipulator().makeDisjointClassName( classesName);
			}
		}.call();		
	}
	/**
	 * This method calls {@link OWLManipulator#makeDisjointClasses(Set)}
	 * in order to create (buffers and/or add) the disjoint axiom with respect to the 
	 * classes set specified as input. 
	 * @param classes the set of classes to make disjointed.
	 * @return the changes to be done in order to add the disjoint classes axiom for all the inputs. (see {@link OWLManipulator} for more info)
	 */
	public OWLOntologyChange makeDisjointClasses( Set< OWLClass> classes){
		List< Lock> mutexes = getMutexes( mutexReasoner, mutexAddDisjointedCls);
		return new OWLReferencesCaller< OWLOntologyChange>(  mutexes, this) {
			@Override
			protected OWLOntologyChange performSynchronisedCall() {
				return getManipulator().makeDisjointClasses( classes);
			}
		}.call();
	}
	
	/**
	 * This method calls {@link OWLManipulator#removeDisjointClassName(Set)}
	 * in order to create (buffers and/or remove) the disjoint axioms (if their exists) with respect to the classes
	 * specified as input though their name. 
	 * @param classesName the set of names of class to make not disjointed anymore.
	 * @return the changes to be done in order to remove the disjoint class axiom for all the inputs. (see {@link OWLManipulator} for more info)
	 */
	public OWLOntologyChange removeDisjointClassName( Set< String> classesName){
		List< Lock> mutexes = getMutexes( mutexReasoner, mutexRemoveDisjointedCls);
		return new OWLReferencesCaller< OWLOntologyChange>(  mutexes, this) {
			@Override
			protected OWLOntologyChange performSynchronisedCall() {
				return getManipulator().removeDisjointClassName( classesName);
			}
		}.call();
	}
	/**
	 * This method calls {@link OWLManipulator#removeDisjointClasses(Set)}
	 * in order to create (buffers and/or remove) the disjoint axiom (if their exists) with respect to the 
	 * classes set specified as input. 
	 * @param classes the set of class to make not disjointed anymore.
	 * @return the changes to be done in order to remove the disjoint class axiom for all the inputs. (see {@link OWLManipulator} for more info)
	 */	
	public OWLOntologyChange removeDisjointClasses( Set< OWLClass> classes){
		List< Lock> mutexes = getMutexes( mutexReasoner, mutexRemoveDisjointedCls);
		return new OWLReferencesCaller< OWLOntologyChange>(  mutexes, this) {
			@Override
			protected OWLOntologyChange performSynchronisedCall() {
				return getManipulator().removeDisjointClasses( classes);
			}
		}.call();
	}



	// [[[[[[[[[[[[[[[[[[[[[[   METHOD TO CALL REASONING (thread safe)   ]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]
	/** 
	 * This method just synchronises the call to the reasoner (performed by {@link OWLReferences}) with respect to 
	 * {@link #mutexReasoner} by making this call thread safe with respect to 
	 * the manipulations of (performed by {@link OWLManipulator}) and queries
	 * (performed by {@link OWLEnquirer}). 
	 * @see OWLReferencesInterface#synchronizeReasoner()
	 */
	@Override
	public synchronized void synchronizeReasoner() {
		mutexReasoner.lock();
		try{
			super.synchronizeReasoner();
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
	 * asserted relation is done by: {@link InferredAxiomExporter#exportOntology(OWLReferences)}
	 * and my be an expensive procedure.
	 * @param exportInf if {@code true} export all reasoner inferences in the returned ontology References.
	 * Otherwise it just call {@link #saveOntology()}
	 */
	public synchronized void saveOntology( boolean exportInf) {
		OWLReferences ontoRef = this;
		try {
			if( exportInf)
				ontoRef = InferredAxiomExporter.exportOntology( ontoRef);
			ontoRef.saveOntology();
		} catch( org.semanticweb.owlapi.reasoner.InconsistentOntologyException e){
			this.logInconsistency();
		}
	}


	/**
	 * It will save an ontology into a file. The files path is 
	 * given as input parameter, and this method does not update: 
	 * {@code ontoRef.getIriFilePath();}. Note that this procedure
	 * may replace an already existing file. The exporting of the 
	 * asserted relation is done by: {@link InferredAxiomExporter#exportOntology(OWLReferences)}
	 * and my be an expensive procedure.
	 * @param exportInf if {@code true} export all reasoner inferences in the returned ontology References.
	 * Otherwise it just call {@link #saveOntology(String)}
	 * @param filePath directory in which save the ontology.
	 */
    public synchronized void saveOntology(boolean exportInf, String filePath) {
        OWLReferences ontoRef = this;
        try {
            if( exportInf)
                ontoRef = InferredAxiomExporter.exportOntology( ontoRef);
            ontoRef.saveOntology( filePath);
        } catch( org.semanticweb.owlapi.reasoner.InconsistentOntologyException e){
            this.logInconsistency();
        }
    }

    // method to easy get object to initialise OWLReferencesCall
    private List<Lock> getMutexes(Lock mutex) {
        List<Lock> mutexes = new ArrayList<>();
        mutexes.add(mutex);
        return mutexes;
    }

    private List<Lock> getMutexes(Lock mutex1, Lock mutex2) {
        List<Lock> mutexes = new ArrayList<>();
        mutexes.add(mutex1);
        mutexes.add(mutex2);
        return mutexes;
    }

    private List<Lock> getMutexes(Lock mutex1, Lock mutex2, Lock mutex3) {
        List<Lock> mutexes = new ArrayList<>();
        mutexes.add(mutex1);
        mutexes.add(mutex2);
        mutexes.add(mutex3);
        return mutexes;
    }

    private List<Lock> getMutexes(Lock mutex1, Lock mutex2, Lock mutex3, Lock mutex4) {
        List<Lock> mutexes = new ArrayList<>();
        mutexes.add(mutex1);
        mutexes.add(mutex2);
        mutexes.add(mutex3);
        mutexes.add(mutex4);
        return mutexes;
    }

	// [[[[[[[[[[[[[[[[[[[[[[              INTERNAL CLASS               ]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]
	// use to manage logging and mutex for all the call to the OWLManipulator and OWLEnquirer
	abstract protected class OWLReferencesCaller< T>{
		//// constant
		public final Long NANOSEC_2_SEC = 1000000000L;
		public final Float MIN_LOGGING_THRESHOULD = 0.000000050F; // in seconds

		//// fields
		private List< Lock> mutexes;
		private Long synchronisatedInitialTime, workInitialTime;
		private OWLReferences ontoRef;
		private Float minLoggingThreshould;

		//// constructor
		public OWLReferencesCaller( List< Lock> mutexes, OWLReferences ontoRef){
			this.initialise(mutexes, ontoRef, MIN_LOGGING_THRESHOULD);
		}
		public OWLReferencesCaller( List< Lock> mutexes, OWLReferences ontoRef, Float minLoggingThreshould){
			this.initialise(mutexes, ontoRef, minLoggingThreshould);
		}
		private void initialise( List< Lock> mutexes, OWLReferences ontoRef, Float minLoggingThreshould){
			// initialise fields
			this.mutexes = mutexes;
			this.ontoRef = ontoRef;
			this.minLoggingThreshould = minLoggingThreshould;
		}

		//// working methods called in constructor
		public T call(){
			T t;
			setSynchronisatedInitialTime();
			if( mutexes != null){
					lockMutex();
				try{
					t = doSynchronisedWork();
				} finally {
					unlockMutex();
				}
			} else t = doSynchronisedWork();
			return t;
		}
		protected void lockMutex(){
			for( int i = 0; i < getMutexes().size(); i++)
				getMutexes().get( i).lock();
		}
		abstract T performSynchronisedCall(); //// main call function
		protected T doSynchronisedWork(){
			setWorkInitialTime();
			T out = performSynchronisedCall();
			loggLockTime( getSynchronisatedInitialTime(), getWorkInitialTime());
            return out;
		}
		protected void unlockMutex(){
			for( int i = getMutexes().size() - 1; i >= 0; i--)
				getMutexes().get( i).unlock();
		}

		// for logging time (how much has been waiting for the mutex) and (how much has been waiting to do the manipulation)
		private void loggLockTime( long initialTime, long unlockingTime){
			Float time = Float.valueOf( unlockingTime - initialTime) / NANOSEC_2_SEC; // in seconds
			Float time2 = Float.valueOf( System.nanoTime() - unlockingTime) / NANOSEC_2_SEC;
			if( time >= minLoggingThreshould || time2 >= minLoggingThreshould){
				StackTraceElement[] trace = Thread.currentThread().getStackTrace();
				String method = "", caller = "";
				if( trace.length >= 3){
					method = trace[2].getMethodName();
					caller = trace[ trace.length - 1].getMethodName();
				}
				logger.addDebugString( getOntoRef().getReferenceName() + " locked on " + method + " for " + time + " [sec] (top stack trace caller: " + caller + ")");
				logger.addDebugString( getOntoRef().getReferenceName() + " spent " + time2 + "[sec] waiting in OWLLibrary");
			}
		}

		//// getters
		protected List<Lock> getMutexes() {
			return mutexes;
		}
		protected Long getSynchronisatedInitialTime() {
			return synchronisatedInitialTime;
		}
		protected Long getWorkInitialTime() {
			return workInitialTime;
		}
		protected OWLReferences getOntoRef() {
			return ontoRef;
		}

		protected Float getMinLoggingThreshould() {
			return minLoggingThreshould;
        }

        protected void setMinLoggingThreshould(Float minLoggingThreshold) {
            this.minLoggingThreshould = minLoggingThreshold;
        }

		//// setters
		protected void setSynchronisatedInitialTime() {
			this.synchronisatedInitialTime = System.nanoTime();
		}

		protected void setWorkInitialTime() {
			this.workInitialTime = System.nanoTime();
		}
	}
}
