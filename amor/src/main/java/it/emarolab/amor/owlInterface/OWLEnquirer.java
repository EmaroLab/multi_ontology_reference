package it.emarolab.amor.owlInterface;

import com.clarkparsia.pellet.owlapi.PelletReasoner;
import com.clarkparsia.pellet.sparqldl.jena.SparqlDLExecutionFactory;
import it.emarolab.amor.owlDebugger.Logger;
import it.emarolab.amor.owlDebugger.Logger.LoggerFlag;
import org.apache.jena.query.*;
import org.apache.jena.rdf.model.InfModel;
import org.apache.jena.rdf.model.ModelFactory;
import org.mindswap.pellet.KnowledgeBase;
import org.mindswap.pellet.jena.PelletInfGraph;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.InconsistentOntologyException;
import org.semanticweb.owlapi.search.EntitySearcher;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static it.emarolab.amor.owlInterface.OWLManipulator.*;

// TODO : bring up to OWLReferences getSubObjectPropertyOf getSubDataPropertyOf and all its case
// TODO : make an abstract class interface to be implemented for all methods (all have the same shape)

/**
 * This class defines and implement the interface for querying the ontology.
 *
 * <div style="text-align:center;"><small>
 * <b>Project</b>:    aMOR <br>
 * <b>File</b>:       it.emarolab.amor.owlInterface.OWLEnquirer <br>
 * <b>Licence</b>:    GNU GENERAL PUBLIC LICENSE. Version 3, 29 June 2007 <br>
 * <b>Author</b>:     Buoncompagni Luca (luca.buoncompagni@edu.unige.it) <br>
 * <b>affiliation</b>: DIBRIS, EMAROLab, University of Genoa. <br>
 * <b>date</b>:       Feb 10, 2016 <br>
 * </small></div>
 *
 * @version 2.2
 */
public class OWLEnquirer {

    /**
     * The default value for including also inferences on queries response
     * (see: {@link #isIncludingInferences()}).
     */
    public static final Boolean DEFAULT_INCLUDE_INFERENCES = true;
    /**
	 * Boolean used to query the reasoner sub/super-(class or properties).
	 * If it is {@code false} only hierarchically direct entities will be returned
	 * (all non-reasoned values are returned anyway).
	 * Else, it collects all entities up to the leafs and root of the structure
     * (see: {@link #isReturningCompleteDescription()}).
	 */
	public static final Boolean DEFAULT_RETURN_COMPLETE_DESCRIPTION = true;
    /**
     * Object used to log information about this class instances.
     * Logs are activated by flag: {@link LoggerFlag#LOG_OWL_ENQUIRER}
     */
    private Logger logger = new Logger(this, LoggerFlag.getLogOWLEnquirer());
    /**
     * If it is {@code false} only asserted axiom will be considered by the Enquirer.
     * Otherwise, also inferences will be considered.
     */
    private Boolean includesInferences;
    /**
     * If it is {@code true} the call ask to the reasoner, otherwise only asserted knowledge will be queried.
     */
    private Boolean returnsCompleteDescription;
    /**
	 * Ontology reference to be manipulated given in the constructor.
	 */
	private OWLReferencesInterface ontoRef;
	private Set<OWLObjectProperty> allObjectPropertyRecoursive = new HashSet<>();
	private Set<OWLDataProperty> allDataPropertyRecoursive = new HashSet<>();
	
	/**
	 * Constructor which sets {@link #returnsCompleteDescription} flag to
	 * default value {@link #DEFAULT_RETURN_COMPLETE_DESCRIPTION} and
     * {@link #includesInferences} flag to {@link #DEFAULT_INCLUDE_INFERENCES}.
	 * @param owlRef the ontology in which perform queries
	 */
	protected OWLEnquirer( OWLReferencesInterface owlRef){
		this.ontoRef = owlRef;
		this.returnsCompleteDescription = DEFAULT_RETURN_COMPLETE_DESCRIPTION;
		this.includesInferences = DEFAULT_INCLUDE_INFERENCES;
	}

	/**
	 * Constructor to define custom {@link #returnsCompleteDescription} value.
	 * @param owlRef the ontology in which perform queries.
	 * @param returnsCompleteDescription the value given to {@link #returnsCompleteDescription}.
     * @param includesInferences set to {@code false} for consider only asserted axioms.
	 */
	protected OWLEnquirer(OWLReferencesInterface owlRef, Boolean returnsCompleteDescription, Boolean includesInferences){
		this.ontoRef = owlRef;
		this.returnsCompleteDescription = returnsCompleteDescription;
		this.includesInferences = includesInferences;
	}

	/**
	 * @return a container of all the objects of the referenced ontology,
	 * set by constructor.
	 */
	protected OWLReferencesInterface getLiOwlLibrary(){
		return ontoRef;
	}

	/**
	 * @return current value of {@link #returnsCompleteDescription}.
	 */
	protected Boolean isReturningCompleteDescription(){
		return returnsCompleteDescription;
	}

	/**
	 * @param flag value to set for {@link #returnsCompleteDescription}.
	 */
	protected void setReturnCompleteDescription(Boolean flag){
		returnsCompleteDescription = flag;
	}

    /**
	 * It is {@code True} if {@code this} {@link OWLEnquirer} is also appending inferred
	 * axioms to the queries. If it is set to {@code false} it
     * enables/disables the effects of {@link #isIncludingInferences()},
     * since no reasoning processes are involved.<br>
	 * This flag is not considered on {@link #sparql(String, Long)} and
	 * derived methods, which always relies on the reasoner.
	 * @return {@code false} if the queries consider only asserted axioms.
	 */
    public Boolean isIncludingInferences() {
        return includesInferences;
    }

    /**
     * Set to {@code false} if the queries consider only asserted axioms.
     * Set to {@code true} to get also inferred axioms.
     * @param includesInferences the flag to indicate if {@code this.{@link #isIncludingInferences()}}.
     */
    public void setIncludeInferences(boolean includesInferences) {
        this.includesInferences = includesInferences;
    }

    /**
	 * Returns all individual defined in the ontology {@link OWLDataFactory#getOWLThing()}.
	 * It returns {@code null} if no individuals belong to the root class or if such class does not exist.
	 * Results completeness is ensured only if {@link #returnsCompleteDescription} is set to {@code true}.
	 *
	 * @return individuals belonging to the root class of the ontology.
	 */
	public Set<OWLNamedIndividual> getIndividualB2Thing(){
		return( getIndividualB2Class( ontoRef.getOWLFactory().getOWLThing()));
	}

	/**
	 * Returns all individuals belonging to the specified class.
	 * The method takes a string and calls {@link OWLLibrary#getOWLClass(String)},
     * to fetch the corresponding OWL class object {@link #getIndividualB2Class(OWLClass)}.
     * It returns {@code null} if no individual belongs to that class or if such class does not exist.
	 * Results completeness is ensured only if {@link #returnsCompleteDescription} is set to {@code true}.
	 *
	 * @param className name of the class.
	 * @return non-ordered set of individuals belonging to such class.
	 */
	public Set<OWLNamedIndividual> getIndividualB2Class( String className){
		return( getIndividualB2Class( ontoRef.getOWLClass( className)));
	}

	/**
     * Returns all individuals belonging to the specified class.
     * The method takes an OWL class object {@link #getIndividualB2Class(OWLClass)}.
     * It returns {@code null} if no individual belongs to that class or if such class does not exist.
     * Results completeness is ensured only if {@link #returnsCompleteDescription} is set to {@code true}.
	 *
	 * @param ontoClass OWL class.
	 * @return non-ordered set of individuals belonging to such class.
	 */
	public Set<OWLNamedIndividual> getIndividualB2Class( OWLClass ontoClass){
		long initialTime = System.nanoTime();
		Set< OWLNamedIndividual> out = new HashSet< OWLNamedIndividual>();

		//Set<OWLIndividual> set = ontoClass.getIndividuals( ontoRef.getOWLOntology());
		Stream<OWLIndividual> stream = EntitySearcher.getIndividuals( ontoClass, ontoRef.getOWLOntology());
		Set<OWLIndividual> set = stream.collect(Collectors.toSet());

		if( set != null)
			out.addAll( set.stream().map( AsOWLNamedIndividual::asOWLNamedIndividual).collect( Collectors.toList()));

		if(includesInferences) {
            try {
                Stream<OWLNamedIndividual> streamReasoned = ontoRef.getReasoner()
                        .getInstances(ontoClass, ! isReturningCompleteDescription()).entities();
                Set<OWLIndividual> reasoned = streamReasoned.collect(Collectors.toSet());
                if (reasoned != null)
                    out.addAll(reasoned.stream().map(AsOWLNamedIndividual::asOWLNamedIndividual).collect(Collectors.toList()));
            } catch (InconsistentOntologyException e) {
                ontoRef.logInconsistency();
            }
        }
		logger.addDebugString( "Individual belonging to class given in: " + (System.nanoTime() - initialTime) + " [ns]");
		return( out);
	}

	/**
	 * Returns one individual belonging to the root class {@link OWLDataFactory#getOWLThing()}.
	 * It returns {@code null} if no individual is classified in the class, meaning the ontology is not populated.
	 * Results completeness is ensured only if {@link #returnsCompleteDescription} is set to {@code true}.
	 *
	 * @return an individual belonging to the root class of the ontology.
	 */
	public OWLNamedIndividual getOnlyIndividualB2Thing(){
		return( getOnlyIndividualB2Class( ontoRef.getOWLFactory().getOWLThing()));
	}

	/**
	 * Returns one individual belonging to the specified class.
	 * Returns {@code null} if no individual are classified in that class,
	 * if such class does not exist or if the Set returned by
	 * {@code this.getIndividualB2Class( .. )} has {@code size > 1}.
	 * Results completeness is ensured only if {@link #returnsCompleteDescription} is set to {@code true}.
	 *
	 * @param className name of the ontological class.
	 * @return an individual belonging to ontoClass.
	 */
	public OWLNamedIndividual getOnlyIndividualB2Class( String className){
		Set<OWLNamedIndividual> set = getIndividualB2Class( ontoRef.getOWLClass( className));
		return( (OWLNamedIndividual) ontoRef.getOnlyElement(set));
	}

	/**
	 * Returns one individual belonging to the specified class.
	 * Returns {@code null} if no individual are classified in that class,
	 * if such class does not exist or if the Set returned by
	 * {@code this.getIndividualB2Class( .. )} has {@code size > 1}.
	 * Results completeness is ensured only if {@link #returnsCompleteDescription} is set to {@code true}.
	 *
	 * @param ontoClass OWLClass object in which to search.
	 * @return an individual belonging to ontoClass.
	 */
	public OWLNamedIndividual getOnlyIndividualB2Class( OWLClass ontoClass){
		Set< OWLNamedIndividual> set = getIndividualB2Class( ontoClass);
		return( (OWLNamedIndividual) ontoRef.getOnlyElement( set));
	}

	/**
	 * Returns the set of classes in which an individual has been classified (except for OWLREA).
	 * Results completeness is ensured only if {@link #returnsCompleteDescription} is set to {@code true}.
	 *
	 * @param individual the instance belonging to the returning classes.
	 * @return set of all classes the individual belongs to.
	 */
	public Set<OWLClass> getIndividualClasses( OWLNamedIndividual individual){
		long initialTime = System.nanoTime();
		Set< OWLClass> out = new HashSet<>();

		//Set< OWLClassExpression> set = individual.getTypes( ontoRef.getOWLOntology());
		Stream<OWLClassExpression> stream = EntitySearcher.getTypes( individual, ontoRef.getOWLOntology());
		Set< OWLClassExpression> set = stream.collect(Collectors.toSet());

		if( set != null)
			out.addAll(set.stream().map(AsOWLClass::asOWLClass).collect(Collectors.toList()));

        if(includesInferences) {
            try {
                Stream<OWLClass> streamReasoned = ontoRef.getReasoner()
                        .getTypes(individual, ! isReturningCompleteDescription()).entities();
                Set<OWLClass> reasoned = streamReasoned.collect(Collectors.toSet());
                if (reasoned != null)
                    out.addAll(reasoned.stream().map(AsOWLClass::asOWLClass).collect(Collectors.toList()));
            } catch ( InconsistentOntologyException e) {
                ontoRef.logInconsistency();
            }
        }
		out.remove( ontoRef.getOWLFactory().getOWLThing());
		logger.addDebugString( "Types of individual given in: " + (System.nanoTime() - initialTime) + " [ns]");
		return out;
	}

	/**
	 * Returns the set of classes in which an individual has been classified.
	 * Results completeness is ensured only if {@link #returnsCompleteDescription} is set to {@code true}.
	 *
	 * @param individualName name of the individual.
	 * @return a set of all classes the individual belongs to.
	 */
	public Set<OWLClass> getIndividualClasses( String individualName){
		OWLNamedIndividual ind = ontoRef.getOWLIndividual( individualName);
		return getIndividualClasses( ind);
	}

	/**
	 * Returns only one class the individual belongs to.
	 *
	 * @param individualName name of the individual.
	 * @return a class.
	 */
	public OWLClass getOnlyIndividualClasses( String individualName){
		Set< OWLClass> set = getIndividualClasses( individualName);
		return( ( OWLClass) ontoRef.getOnlyElement( set));
	}

	/**
	 * Returns one class the individual belongs to.
	 * Calls {@link #getIndividualClasses(OWLNamedIndividual)},
	 * then {@link OWLReferencesInterface#getOnlyElement(Set)}
     * and returns the resulting {@link OWLClass}.
	 *
	 * @param individual ontological individual object.
	 * @return one class in which the input individual is belonging to.
	 */
	public OWLClass getOnlyIndividualClasses( OWLNamedIndividual individual){
		Set< OWLClass> set = getIndividualClasses( individual);
		return( ( OWLClass) ontoRef.getOnlyElement( set));
	}

	/**
	 * Returns the set of literal values of a specific OWL Data Property instance
	 * assigned to an individual. Retrieves an OWL object from a string and calls
	 * {@link #getDataPropertyB2Individual(OWLNamedIndividual, OWLDataProperty)},
	 * then returns the results.
	 *
	 * @param individualName name of the individual the data property belongs to.
	 * @param propertyName data property name.
	 * @return non-ordered set of the data property literal values.
	 */
	public Set<OWLLiteral> getDataPropertyB2Individual( String individualName, String propertyName){
		OWLNamedIndividual ind = ontoRef.getOWLIndividual( individualName);
		OWLDataProperty prop = ontoRef.getOWLDataProperty(propertyName);
		return( getDataPropertyB2Individual( ind, prop));
	}

	/**
     * Returns the set of literal values of a specific OWL Data Property instance
     * assigned to an individual. Returns {@code null} if such data property or
	 * individual do not exist, or if there is no instance of the data property
     * assigned to the individual.
	 *
	 * @param individual individual the data property belongs to.
	 * @param property data property whose values are queried.
	 * @return non-ordered set of the data property literal values.
	 */
	public Set<OWLLiteral> getDataPropertyB2Individual( OWLNamedIndividual individual, OWLDataProperty property){
		long initialTime = System.nanoTime();

		//Set<OWLLiteral>  value = individual.getDataPropertyValues(property, ontoRef.getOWLOntology());
		Stream<OWLLiteral> stream = EntitySearcher.getDataPropertyValues(individual, property, ontoRef.getOWLOntology());
		Set< OWLLiteral> value = stream.collect( Collectors.toSet());

		if (includesInferences) {
			try {
				Set<OWLLiteral> valueInf = ontoRef.getReasoner().getDataPropertyValues(individual, property);
				value.addAll(valueInf);
			} catch (InconsistentOntologyException e) {
				ontoRef.logInconsistency();
			}
		}
		logger.addDebugString( "Data property belonging to individual given in: " + (System.nanoTime() - initialTime) + " [ns]");
		return ( value);
	}

	/**
	 * Returns one literal value of a specific OWL Data Property instance
	 * assigned to an individual. Takes as input the individual and
	 * property names and calls {@link #getDataPropertyB2Individual(OWLNamedIndividual, OWLDataProperty)},
	 * then returns the result as {@link OWLLiteral}.
	 *
	 * @param individualName name of the individual the data property belongs to.
	 * @param propertyName name of the data property whose values are queried.
	 * @return queried literal value.
	 */
	public OWLLiteral getOnlyDataPropertyB2Individual( String individualName, String propertyName){
		Set<OWLLiteral> set = getDataPropertyB2Individual( individualName, propertyName);
		return( (OWLLiteral) ontoRef.getOnlyElement( set));
	}

	/**
     * Returns one literal value of a specific OWL Data Property instance
     * assigned to an individual. This returns {@code null} if such data property or
     * individual do not exist, or if there is no instance of the data property
     * assigned to the individual.
	 *
	 * @param individual individual the data property belongs to.
	 * @param property data property whose values are queried.
	 * @return queried literal value.
	 */
	public OWLLiteral getOnlyDataPropertyB2Individual( OWLNamedIndividual individual, OWLDataProperty property){
		Set<OWLLiteral> set = getDataPropertyB2Individual( individual, property);
		return( (OWLLiteral) ontoRef.getOnlyElement( set));
	}

	/**
     * Returns all value objects of a specific OWL Object Property instance
     * assigned to an individual.
	 *
	 * @param individualName name of an individual.
	 * @param propertyName name of the object property.
	 * @return non-ordered set of the property value entities ({@link OWLNamedIndividual}).
	 */
	public Set<OWLNamedIndividual> getObjectPropertyB2Individual( String individualName, String propertyName){
		OWLNamedIndividual ind = ontoRef.getOWLIndividual( individualName);
		OWLObjectProperty prop = ontoRef.getOWLObjectProperty( propertyName);
		return( getObjectPropertyB2Individual( ind, prop));
	}

	/**
     * Returns all value objects of a specific OWL Object Property instance
     * assigned to an individual. Returns {@code null}
	 * if the object property or individual do not exist.
	 *
	 * @param individual from which the object property is retrieved.
	 * @param property the object property to lock for.
	 * @return non-ordered set of the property value entities ({@link OWLNamedIndividual}).
	 */
	public Set<OWLNamedIndividual> getObjectPropertyB2Individual( OWLNamedIndividual individual, OWLObjectProperty property){
		long initialTime = System.nanoTime();
		Set< OWLNamedIndividual> out = new HashSet<>();

		Stream< OWLIndividual> stream = EntitySearcher.getObjectPropertyValues(individual, property, ontoRef.getOWLOntology());
		Set< OWLIndividual> set = stream.collect( Collectors.toSet());

		if( set != null){
			out.addAll(set.stream().map(AsOWLNamedIndividual::asOWLNamedIndividual).collect(Collectors.toList()));
		}

        if(includesInferences) {
            try {
                Stream<OWLNamedIndividual> streamReasoned = ontoRef.getReasoner().getObjectPropertyValues(individual, property).entities();
                Set<OWLIndividual> reasoned = streamReasoned.collect(Collectors.toSet());
                if (reasoned != null)
                    out.addAll(reasoned.stream().map(AsOWLNamedIndividual::asOWLNamedIndividual).collect(Collectors.toList()));
            } catch (InconsistentOntologyException e) {
                ontoRef.logInconsistency();
            }
        }
		logger.addDebugString( "Object property belonging to individual given in: " + (System.nanoTime() - initialTime) + " [ns]");
		return( out);
	}

	/**
     * Returns one value object of a specific OWL Object Property instance
     * assigned to an individual, given by name.
     * It actually queries all values and then returns only one.
	 *
	 * @param individualName name of the individual.
	 * @param propertyName name of the object property.
	 * @return property value entity ({@link OWLNamedIndividual}).
	 */
	public OWLNamedIndividual getOnlyObjectPropertyB2Individual( String individualName, String propertyName){
		OWLNamedIndividual ind = ontoRef.getOWLIndividual( individualName);
		OWLObjectProperty prop = ontoRef.getOWLObjectProperty( propertyName);
		Set<OWLNamedIndividual> set = getObjectPropertyB2Individual( ind, prop);
		return( (OWLNamedIndividual) ontoRef.getOnlyElement( set));
	}

	/**
	 * Returns one value object of a specific OWL Object Property instance
     * assigned to an individual. It actually queries all values and then returns only one.
     * Returns {@code null} if the object property or individual do not exist,
     * or {@link OWLReferencesInterface#getOnlyElement(Set)} returns {@code null}.
	 *
	 * @param individual from which the object property is retrieved.
	 * @param property the object property to lock for.
	 * @return property value entity ({@link OWLNamedIndividual}).
	 */
	public OWLNamedIndividual getOnlyObjectPropertyB2Individual( OWLNamedIndividual individual, OWLObjectProperty property){
		Set< OWLNamedIndividual> all = getObjectPropertyB2Individual( individual, property);
		return( (OWLNamedIndividual) ontoRef.getOnlyElement( all));
	}

	/**
	 * Returns all object properties and relative value entities relative to an individual.
	 * Note: this implementation may be not efficient since it iterate over all
	 * the object property of the ontology.
	 *
	 * @param individual the instance from which retrieve its objects properties.
	 * @return all the object properties and value entities of the given individual.
	 */
	public Set<ObjectPropertyRelations> getObjectPropertyB2Individual(OWLNamedIndividual individual){
		Set<ObjectPropertyRelations> out = new HashSet<ObjectPropertyRelations>();
		// get all object prop in the ontology
		OWLObjectProperty topObjProp = ontoRef.getOWLFactory().getOWLTopObjectProperty();
		Set<OWLObjectProperty> allProp = getAllObjectPropertiesRecursive();//getSubObjectPropertyOf(topObjProp);
		for( OWLObjectProperty p : allProp){ // check if a property belongs to this individual
			Set<OWLNamedIndividual> values = getObjectPropertyB2Individual(individual, p);
			if( ! values.isEmpty())
				out.add( new ObjectPropertyRelations(individual, p, values, ontoRef));
		}
		return out;
	}

	private Set<OWLObjectProperty> getAllObjectPropertiesRecursive() {
		allObjectPropertyRecoursive = new HashSet<>();
		getAllObjectPropertiesRecursive(ontoRef.getOWLFactory().getOWLTopObjectProperty());
		return allObjectPropertyRecoursive;
	}

	private void getAllObjectPropertiesRecursive(OWLObjectProperty prop) {
		Set<OWLObjectProperty> props = getSubObjectPropertyOf(prop);
		if (props.isEmpty())
			return;
		else {
            allObjectPropertyRecoursive.addAll(props);
            for (OWLObjectProperty p : props)
				getAllObjectPropertiesRecursive(p);
		}
	}

	/**
     * Returns all object properties and relative value entities relative to an individual.
	 * Note that this implementation may be not efficient since it iterate over all
	 * the object property of the ontology.
	 *
	 * @param individualName name of the individual.
	 * @return all the object properties and value entities of the given individual.
	 */
	public Set<ObjectPropertyRelations> getObjectPropertyB2Individual(String individualName){
		return getObjectPropertyB2Individual(ontoRef.getOWLIndividual(individualName));
	}

	/** Returns all data properties and relative value entities relative to an individual.
     * Note that this implementation may be not efficient since it iterate over all
     * the object property of the ontology.
     *
	 * @param individual the instance from which retrieve its objects properties.
	 * @return all the object properties and value entities of the given individual.
	 */
	public Set< DataPropertyRelations> getDataPropertyB2Individual( OWLNamedIndividual individual){
		Set< DataPropertyRelations> out = new HashSet< DataPropertyRelations>();
		// get all object prop in the ontology
		OWLDataProperty topObjProp = ontoRef.getOWLFactory().getOWLTopDataProperty();
		Set<OWLDataProperty> allProp = getAllDataPropertiesRecursive();//getSubDataPropertyOf(topObjProp);
		for( OWLDataProperty p : allProp){ // check if a property belongs to this individual
			Set<OWLLiteral> values = getDataPropertyB2Individual(individual, p);
			if( ! values.isEmpty())
				out.add( new DataPropertyRelations(individual, p, values, ontoRef));
        }
        return out;
    }

	private Set<OWLDataProperty> getAllDataPropertiesRecursive() {
		allDataPropertyRecoursive = new HashSet<>();
		getAllDataPropertiesRecursive(ontoRef.getOWLFactory().getOWLTopDataProperty());
		return allDataPropertyRecoursive;
	}

	private void getAllDataPropertiesRecursive(OWLDataProperty prop) {
		Set<OWLDataProperty> props = getSubDataPropertyOf(prop);
		if (props.isEmpty())
			return;
		else {
            allDataPropertyRecoursive.addAll(props);
            for (OWLDataProperty p : props)
				getAllDataPropertiesRecursive(p);
		}
	}

    /**
	 * Returns all data properties and relative value entities relative to an individual.
     * Note that this implementation may be not efficient since it iterate over all
     * the object property of the ontology.
     *
	 * @param individualName name of the individual.
	 * @return all the object properties and value entities of the given individual.
	 */
	public Set< DataPropertyRelations> getDataPropertyB2Individual( String individualName){
		return getDataPropertyB2Individual( ontoRef.getOWLIndividual( individualName));
    }

    /**
     * Returns all the sub object properties of a given property.
     * It check in the ontology definition frist and then in the
     * inferred axioms by the reasoner.
     * Also note that the completeness of the results
     * of this methods also depends from the value
	 * of {@link #returnsCompleteDescription}.
	 * @param prop an object property
	 * @return the sub object property of the input parameter ({@code prop})
	 */
	public Set<OWLObjectProperty> getSubObjectPropertyOf( OWLObjectProperty prop){
		long initialTime = System.nanoTime();

		//Set<OWLObjectPropertyExpression> set = prop.getSubProperties( ontoRef.getOWLOntology());//cl.getSubClasses( ontoRef.getOWLOntology());
		Stream<OWLObjectPropertyExpression> stream = EntitySearcher.getSubProperties( prop, ontoRef.getOWLOntology());
		Set<OWLObjectPropertyExpression> set = stream.collect( Collectors.toSet());

		Set<OWLObjectProperty> out = new HashSet<>();
		if( set != null)
			out.addAll(set.stream().map(AsOWLObjectProperty::asOWLObjectProperty).collect(Collectors.toList()));

        if(includesInferences) {
            try {
                Stream<OWLObjectPropertyExpression> streamReasoned = ontoRef.getReasoner()
                        .getSubObjectProperties(prop, ! isReturningCompleteDescription()).entities();
                Set<OWLObjectPropertyExpression> reasoned = streamReasoned.collect(Collectors.toSet());
                if (reasoned != null)
                    out.addAll(reasoned.stream().map(AsOWLObjectProperty::asOWLObjectProperty).collect(Collectors.toList()));
            } catch (InconsistentOntologyException e) {
                ontoRef.logInconsistency();
            }
        }
		logger.addDebugString( "get sub classes of given in: " + (System.nanoTime() - initialTime) + " [ns]");
		return( out);
	}

	/**
	 * Returns all sub-properties of a given object property fetched by {@link #getSubObjectPropertyOf(OWLObjectProperty)}.
	 * It checks axioms in the ontology first, then reasoner inferred axioms.
     * Results completeness is ensured only if {@link #returnsCompleteDescription} is set to {@code true}.
	 * @param propName the name of the property property.
	 * @return set of sub-properties of {@code propName}.
	 */
	public Set<OWLObjectProperty> getSubObjectPropertyOf( String propName){
		OWLObjectProperty prop = ontoRef.getOWLObjectProperty( propName);
		return getSubObjectPropertyOf( prop);
	}

	/** Returns all the super-properties of a given object property.
     * It checks axioms in the ontology first, then reasoner inferred axioms.
     * Results completeness is ensured only if {@link #returnsCompleteDescription} is set to {@code true}.
	 * @param prop an object property.
	 * @return set of super-properties of {@code propName}.
	 */
	public Set<OWLObjectProperty> getSuperObjectPropertyOf( OWLObjectProperty prop){
		long initialTime = System.nanoTime();

		//Set<OWLObjectPropertyExpression> set = prop.getSuperProperties( ontoRef.getOWLOntology());
		Stream<OWLObjectPropertyExpression> stream = EntitySearcher.getSuperProperties( prop, ontoRef.getOWLOntology());
		Set<OWLObjectPropertyExpression> set = stream.collect( Collectors.toSet());

		Set<OWLObjectProperty> out = new HashSet<>();
		if( set != null)
			out.addAll(set.stream().map(AsOWLObjectProperty::asOWLObjectProperty).collect(Collectors.toList()));

        if(includesInferences) {
            try {
                Stream<OWLObjectPropertyExpression> streamReasoned = ontoRef.getReasoner()
                        .getSuperObjectProperties(prop, ! isReturningCompleteDescription()).entities();
                Set<OWLObjectPropertyExpression> reasoned = streamReasoned.collect(Collectors.toSet());
                if (reasoned != null)
                    out.addAll(reasoned.stream().map(AsOWLObjectProperty::asOWLObjectProperty).collect(Collectors.toList()));

            } catch (InconsistentOntologyException e) {
                ontoRef.logInconsistency();
            }
        }
		logger.addDebugString( "get sub classes of given in: " + (System.nanoTime() - initialTime) + " [ns]");
		return( out);
	}

	/**
	 * Returns all sub-properties of a given object property fetched by {@link #getSuperObjectPropertyOf(OWLObjectProperty)}.
     * It checks axioms in the ontology first, then reasoner inferred axioms.
     * Results completeness is ensured only if {@link #returnsCompleteDescription} is set to {@code true}.
	 * @param propName the object property name.
	 * @return set of super-properties of {@code propName}.
	 */
	public Set<OWLObjectProperty> getSuperObjectPropertyOf( String propName){
		OWLObjectProperty prop = ontoRef.getOWLObjectProperty( propName);
		return getSuperObjectPropertyOf( prop);
	}

    /**
	 * Returns all sub-properties of a given data property.
     * It checks axioms in the ontology first, then reasoner inferred axioms.
     * Results completeness is ensured only if {@link #returnsCompleteDescription} is set to {@code true}.
	 * @param prop a data property.
	 * @return set of sub-properties of {@code prop}.
     */
    public Set<OWLDataProperty> getSubDataPropertyOf(OWLDataProperty prop){
        long initialTime = System.nanoTime();

		//Set<OWLDataPropertyExpression> set = prop.getSubProperties( ontoRef.getOWLOntology());
		Stream<OWLDataPropertyExpression> stream = EntitySearcher.getSubProperties( prop, ontoRef.getOWLOntology());
		Set<OWLDataPropertyExpression> set = stream.collect( Collectors.toSet());
		Set<OWLDataProperty> out = new HashSet<>();
		if( set != null)
			out.addAll(set.stream().map(AsOWLDataProperty::asOWLDataProperty).collect(Collectors.toList()));

        if(includesInferences) {
            try {
                Stream<OWLDataProperty> streamReasoned = ontoRef.getReasoner()
                        .getSubDataProperties(prop, ! isReturningCompleteDescription()).entities();
                Set<OWLDataProperty> reasoned = streamReasoned.collect(Collectors.toSet());
                if (reasoned != null)
                    out.addAll(reasoned.stream().map(AsOWLDataProperty::asOWLDataProperty).collect(Collectors.toList()));
            } catch (InconsistentOntologyException e) {
                ontoRef.logInconsistency();
            }
        }
		logger.addDebugString( "get sub classes of given in: " + (System.nanoTime() - initialTime) + " [ns]");
		return( out);
	}

	/**
	 * Returns all sub-properties of a given data property fetched by {@link #getSubDataPropertyOf(OWLDataProperty)}.
     * It checks axioms in the ontology first, then reasoner inferred axioms.
     * Results completeness is ensured only if {@link #returnsCompleteDescription} is set to {@code true}.
	 * @param propName data property name.
	 * @return set of sub-properties of {@code prop}.
	 */
	public Set<OWLDataProperty> getSubDataPropertyOf( String propName){
		OWLDataProperty prop = ontoRef.getOWLDataProperty( propName);
		return getSubDataPropertyOf( prop);
	}

    /**
	 * Returns all super-properties of a given data property.
     * It checks axioms in the ontology first, then reasoner inferred axioms.
     * Results completeness is ensured only if {@link #returnsCompleteDescription} is set to {@code true}.
	 * @param prop a data property.
	 * @return set of sub-properties of {@code prop}.
     */
    public Set<OWLDataProperty> getSuperDataPropertyOf(OWLDataProperty prop){
        long initialTime = System.nanoTime();

		//Set<OWLDataPropertyExpression> set = prop.getSuperProperties( ontoRef.getOWLOntology());
		Stream<OWLDataPropertyExpression> stream = EntitySearcher.getSuperProperties( prop, ontoRef.getOWLOntology());
		Set<OWLDataPropertyExpression> set = stream.collect( Collectors.toSet());

		Set<OWLDataProperty> out = new HashSet<>();
		if( set != null){
			out.addAll(set.stream().map(AsOWLDataProperty::asOWLDataProperty).collect(Collectors.toList()));
		}
        if(includesInferences) {
            try {
                Stream<OWLDataProperty> streamReasoned = ontoRef.getReasoner()
                        .getSuperDataProperties(prop, ! isReturningCompleteDescription()).entities();
                Set<OWLDataProperty> reasoned = streamReasoned.collect(Collectors.toSet());
                if (reasoned != null)
                    out.addAll(reasoned.stream().map(AsOWLDataProperty::asOWLDataProperty).collect(Collectors.toList()));
            } catch (InconsistentOntologyException e) {
                ontoRef.logInconsistency();
            }
        }
		logger.addDebugString( "get sub classes of given in: " + (System.nanoTime() - initialTime) + " [ns]");
		return(out);
	}

	/**
	 * Returns all super-properties of a given data property fetched by {@link #getSuperDataPropertyOf(OWLDataProperty)}.
     * It checks axioms in the ontology first, then reasoner inferred axioms.
     * Results completeness is ensured only if {@link #returnsCompleteDescription} is set to {@code true}.
	 * @param propName a data property.
	 * @return set of sub-properties of {@code prop}.
	 */
	public Set<OWLDataProperty> getSuperDataPropertyOf( String propName){
		OWLDataProperty prop = ontoRef.getOWLDataProperty( propName);
		return getSuperDataPropertyOf( prop);
	}
	
	/**
	 * Returns all sub-classes of a given class.
	 * Results completeness is ensured only if {@link #returnsCompleteDescription} is set to {@code true}.
     *
	 * @param className name of an OWL class.
	 * @return non-ordered set of sub-classes.
	 */
	public Set<OWLClass> getSubClassOf( String className){
		OWLClass cl = ontoRef.getOWLClass( className);
		return( getSubClassOf(cl));
	}

	/**
	 * Returns all sub-classes of a given class (except for {@link OWLDataFactory#getOWLThing()}).
         * Results completeness is ensured only if {@link #returnsCompleteDescription} is set to {@code true}.
     *
	 * @param cl an OWL class.
	 * @return non-ordered set of sub-classes.
	 */
	public Set<OWLClass> getSubClassOf( OWLClass cl){
		long initialTime = System.nanoTime();

		Stream<OWLClassExpression> stream = EntitySearcher.getSubClasses( cl, ontoRef.getOWLOntology());
		Set<OWLClassExpression> set = stream.collect( Collectors.toSet());

		Set<OWLClass> out = new HashSet<>();
		if( set != null)
			out.addAll( set.stream().map(AsOWLClass::asOWLClass).collect(Collectors.toList()));

        if(includesInferences) {
            try {
                Stream<OWLClass> streamReasoned = ontoRef.getReasoner()
                        .getSubClasses(cl, ! isReturningCompleteDescription()).entities();
                Set<OWLClass> reasoned = streamReasoned.collect(Collectors.toSet());
                if (reasoned != null)
                    out.addAll(reasoned.stream().map(AsOWLClass::asOWLClass).collect(Collectors.toList()));
            } catch (InconsistentOntologyException e) {
                ontoRef.logInconsistency();
            }
        }
		out.remove( ontoRef.getOWLFactory().getOWLThing());
		logger.addDebugString( "get sub classes of given in: " + (System.nanoTime() - initialTime) + " [ns]");
		return(out);
	}

    /**
	 * Returns all super-classes of a given class.
         * Results completeness is ensured only if {@link #returnsCompleteDescription} is set to {@code true}.
     *
	 * @param className name of an OWL class.
	 * @return non-ordered set of sub-classes.
	 */
	public Set<OWLClass> getSuperClassOf( String className){
		OWLClass cl = ontoRef.getOWLClass( className);
		return( getSuperClassOf(cl));
	}

	/**
	 * Returns all super-classes of a given class (except for {@link OWLDataFactory#getOWLThing()}).
         * Results completeness is ensured only if {@link #returnsCompleteDescription} is set to {@code true}.
     *
	 * @param cl an OWL class.
	 * @return non-ordered set of sub-classes.
	 */
	public Set<OWLClass> getSuperClassOf( OWLClass cl){
		long initialTime = System.nanoTime();
		Set<OWLClass> classes = new HashSet< OWLClass>();

		//Set< OWLClassExpression> set = cl.getSuperClasses( ontoRef.getOWLOntology());
		Stream<OWLClassExpression> stream = EntitySearcher.getSuperClasses( cl, ontoRef.getOWLOntology());
		Set<OWLClassExpression> set = stream.collect( Collectors.toSet());

		if( set != null)
			classes.addAll(set.stream().map(AsOWLClass::asOWLClass).collect(Collectors.toList()));

        if( isIncludingInferences()) {
            try {
                Stream<OWLClass> streamReasoned = ontoRef.getReasoner()
                        .getSuperClasses(cl, ! isReturningCompleteDescription()).entities();
                Set<OWLClass> reasoned = streamReasoned.collect(Collectors.toSet());
                if (reasoned != null)
                    classes.addAll(reasoned.stream().map(AsOWLClass::asOWLClass).collect(Collectors.toList()));
            } catch (InconsistentOntologyException e) {
                ontoRef.logInconsistency();
            }
        }
		classes.remove( ontoRef.getOWLFactory().getOWLThing());
		logger.addDebugString( "get super classes of given in: " + (System.nanoTime() - initialTime) + " [ns]");
		return( classes);
	}

    /**
     * Returns the set of restrictions of the given class it terms
     * of: &forall; and &exist; quantifier, as well as: minimal, exact and maximal cardinality;
     * with respect to data and object properties.
     * @param cl the class from which get the restriction and cardinality limits.
     * @return the container of all the class restrictions and cardinality, for
     * the given class.
     */
	public Set<ClassRestriction> getClassRestrictions(OWLClass cl){
		Set<ClassRestriction> out = new HashSet();
		Stream<OWLClassAxiom> axiomStream = ontoRef.getOWLOntology().axioms( cl);
		for (OWLClassAxiom ax :  (Iterable<OWLClassAxiom>) () -> axiomStream.iterator()) {
			Stream<OWLClassExpression> nestedClassStream = ax.nestedClassExpressions();
			for( OWLClassExpression e : (Iterable<OWLClassExpression>) () ->  nestedClassStream.iterator()) {
				if ( e.getClassExpressionType() == ClassExpressionType.OBJECT_MIN_CARDINALITY) {
					OWLObjectMinCardinality r = (OWLObjectMinCardinality) e;
                    ClassRestriction cr = new ClassRestriction( cl, (OWLObjectProperty) r.getProperty());
                    cr.setObjectMinRestriction( r.getCardinality(), (OWLClass) r.getFiller());
					out.add( cr);
					logger.addDebugString( "getting class definition: " + cr);
				}
				if ( e.getClassExpressionType() == ClassExpressionType.OBJECT_MAX_CARDINALITY) {
                    OWLObjectMaxCardinality r = (OWLObjectMaxCardinality) e;
                    ClassRestriction cr = new ClassRestriction( cl, (OWLObjectProperty) r.getProperty());
                    cr.setObjectMaxRestriction( r.getCardinality(), (OWLClass) r.getFiller());
                    out.add( cr);
                    logger.addDebugString( "getting class definition: " + cr);
				}
				if ( e.getClassExpressionType() == ClassExpressionType.OBJECT_EXACT_CARDINALITY) {
                    OWLObjectExactCardinality r = (OWLObjectExactCardinality) e;
                    ClassRestriction cr = new ClassRestriction( cl, (OWLObjectProperty) r.getProperty());
                    cr.setObjectExactRestriction( r.getCardinality(), (OWLClass) r.getFiller());
                    out.add( cr);
                    logger.addDebugString( "getting class definition: " + cr);
				}
				if ( e.getClassExpressionType() == ClassExpressionType.OBJECT_ALL_VALUES_FROM) {
                    OWLObjectAllValuesFrom r = (OWLObjectAllValuesFrom) e;
                    ClassRestriction cr = new ClassRestriction( cl, (OWLObjectProperty) r.getProperty());
                    cr.setObjectOnlyRestriction( (OWLClass) r.getFiller());
                    out.add( cr);
                    logger.addDebugString( "getting class definition: " + cr);
				}
				if ( e.getClassExpressionType() == ClassExpressionType.OBJECT_SOME_VALUES_FROM) {
                    OWLObjectSomeValuesFrom r = (OWLObjectSomeValuesFrom) e;
                    ClassRestriction cr = new ClassRestriction( cl, (OWLObjectProperty) r.getProperty());
                    cr.setObjectSomeRestriction( (OWLClass) r.getFiller());
                    out.add( cr);
                    logger.addDebugString( "getting class definition: " + cr);
				}

				if ( e.getClassExpressionType() == ClassExpressionType.DATA_MIN_CARDINALITY) {
                    OWLDataMinCardinality r = (OWLDataMinCardinality) e;
                    ClassRestriction cr = new ClassRestriction( cl, (OWLDataProperty) r.getProperty());
                     cr.setDataMinRestriction( r.getCardinality(), r.getFiller());
                    out.add( cr);
                    logger.addDebugString( "getting class definition: " + cr);
				}
				if ( e.getClassExpressionType() == ClassExpressionType.DATA_MAX_CARDINALITY) {
                    OWLDataMaxCardinality r = (OWLDataMaxCardinality) e;
                    ClassRestriction cr = new ClassRestriction( cl, (OWLDataProperty) r.getProperty());
                    cr.setDataMaxRestriction( r.getCardinality(), r.getFiller());
                    out.add( cr);
                    logger.addDebugString( "getting class definition: " + cr);
				}
				if ( e.getClassExpressionType() == ClassExpressionType.DATA_EXACT_CARDINALITY) {
                    OWLDataExactCardinality r = (OWLDataExactCardinality) e;
                    ClassRestriction cr = new ClassRestriction( cl, (OWLDataProperty) r.getProperty());
                    cr.setDataExactRestriction( r.getCardinality(), r.getFiller());
                    out.add( cr);
                    logger.addDebugString( "getting class definition: " + cr);
				}
				if ( e.getClassExpressionType() == ClassExpressionType.DATA_ALL_VALUES_FROM) {
                    OWLDataAllValuesFrom r = (OWLDataAllValuesFrom) e;
                    ClassRestriction cr = new ClassRestriction( cl, (OWLDataProperty) r.getProperty());
                    cr.setDataOnlyRestriction( r.getFiller());
                    out.add( cr);
                    logger.addDebugString( "getting class definition: " + cr);
				}
				if ( e.getClassExpressionType() == ClassExpressionType.DATA_SOME_VALUES_FROM) {
                    OWLDataSomeValuesFrom r = (OWLDataSomeValuesFrom) e;
                    ClassRestriction cr = new ClassRestriction( cl, (OWLDataProperty) r.getProperty());
                    cr.setDataSomeRestriction( r.getFiller());
                    out.add( cr);
                    logger.addDebugString( "getting class definition: " + cr);
				}

			}
		}
		return out;
	}

    /**
     * Returns the set of restrictions of the given class it terms
     * of: &forall; and &exist; quantifier, as well as: minimal, exact and maximal cardinality;
     * with respect to data and object properties.
     * @param className the name of the class from which get the restriction and cardinality limits.
     * @return the container of all the class restrictions and cardinality, for
     * the given class.
     */
    public Set<ClassRestriction> getClassRestrictions(String className){
        return getClassRestrictions( ontoRef.getOWLClass( className));
    }

    /**
     * Performs a SPARQL query on the ontology. Returns a list of {@link QuerySolution} or {@code null} if the query fails.
     * Works only with the Pellet reasoner. {@code timeOut} parameter sets the query timeout, no timeout is set if
     * {@code timeOut &lt;=0}. Once timeout is reached, all solutions found up to that point are returned.
     *
     * @param query   a string defining the query in SPARQL query syntax.
     * @param timeOut timeout for the query.
     * @return list of solutions.
     */
    public List<QuerySolution> sparql(String query, Long timeOut) {
        try {
            // get objects
            KnowledgeBase kb = ((PelletReasoner) ontoRef.getReasoner()).getKB();
            PelletInfGraph graph = new org.mindswap.pellet.jena.PelletReasoner().bind(kb);
            InfModel model = ModelFactory.createInfModel(graph);
            // make the query
            QueryExecution qe = SparqlDLExecutionFactory.create(QueryFactory.create(query), model);
            String queryLog = qe.getQuery().toString() + System.getProperty("line.separator") + "[TimeOut:";
            if (timeOut != null) { // apply time out
                if (timeOut > 0) {
                    qe.setTimeout(timeOut); // TODO: it does not seems to work with pellet SELECT queries
                    queryLog += timeOut + "ms].";
                } else queryLog += "NONE].";
            } else queryLog += "NONE].";
            ResultSet result = qe.execSelect(); // TODO: it can do much more.... (ask query, evaluate constants, etc.)
            // store the results
            List<QuerySolution> solutions = new ArrayList<>();
            while (result.hasNext()) {
                QuerySolution r = result.next();
                solutions.add(r);
            }
            logger.addDebugString("SPARQL query:" + System.getProperty("line.separator") + queryLog + System.getProperty("line.separator") + ResultSetFormatter.asText(result));
            return solutions;
        } catch (QueryCancelledException e) {
            logger.addDebugString("SPARQL timed out !!");
            return null;
        }
    }

    /**
     * Performs a SPARQL query on the ontology. Returns a list of {@link QuerySolution} or {@code null} if the query fails.
     * Works only with the Pellet reasoner.
     *
     * @param query a string defining the query in SPARQL query syntax.
     * @return list of solutions.
     */
    public List<QuerySolution> sparql(String query) { // no time out
        return sparql(query, null);
    }

    /**
     * Performs a SPARQL query on the ontology. Returns a list of {@link QuerySolution} or {@code null} if the query fails.
     * Works only with the Pellet reasoner. {@code timeOut} parameter sets the query timeout, no timeout is set if
     * {@code timeOut &lt;= 0}. Once timeout is reached, all solutions found up to that point are returned.
     *
     * @param prefix  a string defining the query prefix field in SPARQL query syntax.
     * @param select  a string defining the query select field in SPARQL query syntax.
     * @param where   a string defining the query where field in SPARQL query syntax.
     * @param timeOut timeout for the query.
     * @return list of solutions.
     */
    public List<QuerySolution> sparql(String prefix, String select, String where, Long timeOut) {
        return sparql(prefix + select + where, timeOut);
    }

    // works only for pellet
    // set time out to null or < 0 to do not apply any timing out

    /**
     * Performs a SPARQL query on the ontology. Returns a list of {@link QuerySolution} or {@code null} if the query fails.
     * Works only with the Pellet reasoner.
     *
     * @param prefix a string defining the query prefix field in SPARQL query syntax.
     * @param select a string defining the query select field in SPARQL query syntax.
     * @param where  a string defining the query where field in SPARQL query syntax.
     * @return list of solutions.
     */
    public List<QuerySolution> sparql(String prefix, String select, String where) {
        return sparql(prefix + select + where);
    }

    /**
     * An utility method that call {@link #sparql(String, Long)} and translates the results to a list of maps among strings.
     * Used to share the results with other code and processes. {@code timeOut} parameter sets the query timeout,
     * no timeout is set if {@code timeOut &lt;= 0}. Once timeout is reached, all solutions found up to that point are returned.
     *
     * @param query   a string defining the query in SPARQL query syntax.
     * @param timeOut timeout for the query.
     * @return formatted list of solutions.
     */
    public List<Map<String, String>> sparqlMsg(String query, Long timeOut) {
        return sparql2Msg(sparql(query, timeOut));
    }

    /**
     * An utility method that call {@link #sparql(String, Long)} and translates the results to a list of maps among strings.
     * This call do not apply any time out.
     * Used to share the results with other code and processes.
     *
     * @param query a string defining the query in SPARQL query syntax.
     * @return formatted list of solutions.
     */
    public List<Map<String, String>> sparqlMsg(String query) { // no time out
        return sparql2Msg(sparql(query, null));
    }

    /**
     * An utility method that call {@link #sparql(String, Long)} and translates the results to a list of maps among strings.
     * Used to share the results with other code and processes. {@code timeOut} parameter sets the query timeout,
     * no timeout is set if {@code timeOut &lt;= 0}. Once timeout is reached, all solutions found up to that point are returned.
     *
     * @param prefix  a string defining the query prefix field in SPARQL query syntax.
     * @param select  a string defining the query select field in SPARQL query syntax.
     * @param where   a string defining the query where field in SPARQL query syntax.
     * @param timeOut timeout for the query.
     * @return list of solutions.
     */
    public List<Map<String, String>> sparqlMsg(String prefix, String select, String where, Long timeOut) {
        return sparql2Msg(sparql(prefix + select + where, timeOut));
    }

    /**
     * An utility method that call {@link #sparql(String, Long)} and translates the results to a list of maps among strings.
     * Used to share the results with other code and processes.
     *
     * @param prefix a string defining the query prefix field in SPARQL query syntax.
     * @param select a string defining the query select field in SPARQL query syntax.
     * @param where  a string defining the query where field in SPARQL query syntax.
     * @return list of solutions.
     */
    public List<Map<String, String>> sparqlMsg(String prefix, String select, String where) {
        return sparql2Msg(sparql(prefix + select + where));
    }

    /**
     * Formats a list of {@link QuerySolution} into a list of maps among strings.
     *
     * @param results list of {@link QuerySolution}.
     * @return formatted results.
     */
    private List<Map<String, String>> sparql2Msg(List<QuerySolution> results) {
        List<Map<String, String>> out = new ArrayList();
        if (results != null) // timeout
            for (QuerySolution q : results) {
                Iterator<String> names = q.varNames();
                Map<String, String> item = new HashMap<>();
                while (names.hasNext()) {
                    String n = names.next();
                    item.put(n, q.get(n).toString());
                }
                out.add(item);
            }
        return out;
    }

    /**
     * Class used to contain an object property relation associated to an individuals and its value entities.
     * A {@link ObjectPropertyRelations} object is returned by
     * {@link OWLEnquirer#getObjectPropertyB2Individual(OWLNamedIndividual)}
     * and {@link OWLEnquirer#getObjectPropertyB2Individual(String)}.
     *
     * <div style="text-align:center;"><small>
     * <b>Project</b>:    aMOR <br>
     * <b>File</b>:       it.emarolab.amor.owlInterface.OWLEnquirer <br>
     * <b>Licence</b>:    GNU GENERAL PUBLIC LICENSE. Version 3, 29 June 2007 <br>
     * <b>Author</b>:     Buoncompagni Luca (luca.buoncompagni@edu.unige.it) <br>
     * <b>affiliation</b>: DIBRIS, EMAROLab, University of Genoa. <br>
     * <b>date</b>:       Feb 10, 2016 <br>
     * </small></div>
     */
    public class ObjectPropertyRelations {
        private OWLObjectProperty prop;
        private OWLNamedIndividual ind;
        private Set<OWLNamedIndividual> value;
        private String propName, indName;
        private Set<String> valueName;

        public ObjectPropertyRelations(OWLNamedIndividual ind, OWLObjectProperty prop, Set<OWLNamedIndividual> value,
                                       OWLReferencesInterface ontoRef) {
            this.prop = prop;
            this.propName = ontoRef.getOWLObjectName(prop);
            this.ind = ind;
            this.indName = ontoRef.getOWLObjectName(ind);
            this.value = value;
            this.valueName = ontoRef.getOWLObjectName(value);
        }

        public OWLObjectProperty getProperty() {
            return prop;
        }

        public OWLNamedIndividual getIndividuals() {
            return ind;
        }

        public Set<OWLNamedIndividual> getValues() {
            return value;
        }

        public String getPropertyName() {
            return propName;
        }

        public String getIndividualsName() {
            return indName;
        }

        public Set<String> getValuesName() {
            return valueName;
        }

        public String toString() {
            return "\"" + getIndividualsName() + "." + getPropertyName() + "( " + getValuesName() + ")";
        }
    }

    /**
     * Class used to contain a data property relation  associated to an individuals and its literal values.
     * A {@link DataPropertyRelations} object is returned by
     * {@link OWLEnquirer#getObjectPropertyB2Individual(OWLNamedIndividual)}
     * and {@link OWLEnquirer#getObjectPropertyB2Individual(String)}.
     *
     * <div style="text-align:center;"><small>
     * <b>Project</b>:    aMOR <br>
     * <b>File</b>:       it.emarolab.amor.owlInterface.OWLEnquirer <br>
     * <b>Licence</b>:    GNU GENERAL PUBLIC LICENSE. Version 3, 29 June 2007 <br>
     * <b>Author</b>:     Buoncompagni Luca (luca.buoncompagni@edu.unige.it) <br>
     * <b>affiliation</b>: DIBRIS, EMAROLab, University of Genoa. <br>
     * <b>date</b>:       Feb 10, 2016 <br>
     * </small></div>
     */
    public class DataPropertyRelations {
        private OWLDataProperty prop;
        private OWLNamedIndividual ind;
        private Set<OWLLiteral> value;
        private String propName, indName;
        private Set<String> valueName;

        public DataPropertyRelations(OWLNamedIndividual ind, OWLDataProperty prop, Set<OWLLiteral> value, OWLReferencesInterface ontoRef) {
            this.prop = prop;
            this.propName = ontoRef.getOWLObjectName(prop);
            this.ind = ind;
            this.indName = ontoRef.getOWLObjectName(ind);
            this.value = value;
            this.valueName = ontoRef.getOWLObjectName(value);
        }

        public OWLDataProperty getProperty() {
            return prop;
        }

        public OWLNamedIndividual getIndividual() {
            return ind;
        }

        public Set<OWLLiteral> getValues() {
            return value;
        }

        public String getPropertyName() {
            return propName;
        }

        public String getIndividualName() {
            return indName;
        }

        public Set<String> getValuesName() {
            return valueName;
        }

        public String toString() {
            return "\"" + getIndividualName() + "." + getPropertyName() + "( " + getValuesName() + ")";
        }
    }

    /**
     * This is a container for quantify class restrictions.
     * It is produced fom {@link #getClassRestrictions(OWLClass)} and
     * describes {@link OWLClassExpression} of the given class, if its type is one of:
     * {@link ClassExpressionType#OBJECT_SOME_VALUES_FROM},  {@link ClassExpressionType#DATA_SOME_VALUES_FROM} (&exist;),
     * {@link ClassExpressionType#OBJECT_ALL_VALUES_FROM}, {@link ClassExpressionType#DATA_ALL_VALUES_FROM} (&forall;),
     * {@link ClassExpressionType#OBJECT_MIN_CARDINALITY}, {@link ClassExpressionType#DATA_MIN_CARDINALITY},
     * {@link ClassExpressionType#OBJECT_MAX_CARDINALITY}, {@link ClassExpressionType#DATA_MAX_CARDINALITY},
     * {@link ClassExpressionType#OBJECT_EXACT_CARDINALITY}, {@link ClassExpressionType#DATA_EXACT_CARDINALITY}.
     * Where the getters of this containers depends from one of those types, assigned through setters.
     *
     * <div style="text-align:center;"><small>
     * <b>Project</b>:    aMOR <br>
     * <b>File</b>:       it.emarolab.amor.owlInterface.OWLEnquirer <br>
     * <b>Licence</b>:    GNU GENERAL PUBLIC LICENSE. Version 3, 29 June 2007 <br>
     * <b>Author</b>:     Buoncompagni Luca (luca.buoncompagni@edu.unige.it) <br>
     * <b>affiliation</b>: DIBRIS, EMAROLab, University of Genoa. <br>
     * <b>date</b>:       Feb 10, 2016 <br>
     * </small></div>
     */
    public class ClassRestriction{
		private OWLClass definitionOf;
		private OWLProperty property;
		private Boolean isDataProperty;
		private int expressioneType, cardinality;
		private OWLClass object;
		private OWLDataRange data;

        /**
         * Create a class restriction for a data property and set its
         * domain to be an {@link OWLDataRange} (see {@link #getDataTypeRestriction()}).
         * @param subject the class restricted by this axiom.
         * @param property the data property that restricts the class
         */
		public ClassRestriction(OWLClass subject, OWLDataProperty property) {
			this.definitionOf = subject;
			this.property = property;
			this.isDataProperty = true;
		}
        /**
         * Create a class restriction for an object property and set its
         * domain to be an {@link OWLClass} (see {@link #getObjectRestriction()}).
         * @param subject the class restricted by this axiom.
         * @param property the object property that restricts the class
         */
		public ClassRestriction(OWLClass subject, OWLObjectProperty property) {
            this.definitionOf = subject;
            this.property = property;
            this.isDataProperty = false;
		}

        /**
         * Initialise to describe universal data property restriction over
         * a data type (supported: {@link String}, {@link Double}, {@link Float}, {@link Integer} and {@link Long}).
         * In symbols: {@code class &forall; hasDataProperty dataType}.
         * @param dataType the range of data of the universal restriction.
         */
		protected void setDataOnlyRestriction( OWLDataRange dataType){
			if( isDataProperty) {
				this.expressioneType = RESTRICTION_ONLY;
				this.data = dataType;
			} else logger.addDebugString("Cannot set a 'only' data restriction over an object property", true);
		}
        /**
         * Initialise to describe existential data property restriction over
         * a data type (supported: {@link String}, {@link Double}, {@link Float}, {@link Integer} and {@link Long}).
         * In symbols: {@code class &exists; hasDataProperty dataType}.
         * @param dataType the range of data of the existential restriction.
         */
		protected void setDataSomeRestriction( OWLDataRange dataType){
			if( isDataProperty) {
				this.expressioneType = RESTRICTION_SOME;
				this.data = dataType;
			} else logger.addDebugString("Cannot set a 'some' data restriction over an object property", true);
		}
        /**
         * Initialise to describe a data property with a minimal cardinality restriction over
         * a data type (supported: {@link String}, {@link Double}, {@link Float}, {@link Integer} and {@link Long}).
         * In symbols: {@code class &lt;<sub>d</sub> hasDataProperty dataType}.
         * @param cardinality the cardinality of the restriction {@code d}.
         * @param dataType the range of data of the minimal cardinality restriction.
         */
		protected void setDataMinRestriction( int cardinality, OWLDataRange dataType){
			if( isDataProperty) {
				this.expressioneType = RESTRICTION_MIN;
				this.cardinality = cardinality;
				this.data = dataType;
            } else logger.addDebugString("Cannot set a 'min' data restriction over an object property", true);
		}
        /**
         * Initialise to describe a data property with a maximal cardinality restriction over
         * a data type (supported: {@link String}, {@link Double}, {@link Float}, {@link Integer} and {@link Long}).
         * In symbols: {@code class &gt;<sub>d</sub> hasDataProperty dataType}.
         * @param cardinality the cardinality of the restriction {@code d}.
         * @param dataType the range of data of the maximal cardinality restriction.
         */
		protected void setDataMaxRestriction(int cardinality, OWLDataRange dataType){
			if( isDataProperty) {
				this.expressioneType = RESTRICTION_MAX;
				this.cardinality = cardinality;
				this.data = dataType;
            } else logger.addDebugString("Cannot set a 'max' data restriction over an object property", true);
		}
        /**
         * Initialise to describe a data property with an exact cardinality restriction over
         * a data type (supported: {@link String}, {@link Double}, {@link Float}, {@link Integer} and {@link Long}).
         * In symbols: {@code class =<sub>d</sub> hasDataProperty dataType}.
         * @param cardinality the cardinality of the restriction {@code d}.
         * @param dataType the range of data of the exact cardinality restriction.
         */
		protected void setDataExactRestriction( int cardinality, OWLDataRange dataType){
			if( isDataProperty) {
				this.expressioneType = RESTRICTION_EXACT;
				this.cardinality = cardinality;
				this.data = dataType;
			} else logger.addDebugString("Cannot set a 'exact' data restriction over an object property", true);
		}

        /**
         * Initialise to describe universal object property restriction over
         * a {@link OWLClass}.
         * In symbols: {@code classSubject &forall; hasObjectProperty classObject}.
         * @param object the class domain of the universal property restriction.
         */
        protected void setObjectOnlyRestriction( OWLClass object){
			if( ! isDataProperty) {
				this.expressioneType = RESTRICTION_ONLY;
				this.object = object;
            } else logger.addDebugString("Cannot set a 'only' object restriction over a data property", true);
		}
        /**
         * Initialise to describe existential object property restriction over
         * a {@link OWLClass}.
         * In symbols: {@code classSubject &exists; hasObjectProperty classObject}.
         * @param object the class domain of the existential property restriction.
         */
		protected void setObjectSomeRestriction( OWLClass object){
			if( ! isDataProperty) {
				this.expressioneType = RESTRICTION_SOME;
				this.object = object;
			} else logger.addDebugString( "Cannot set a 'some' object restriction over a data property", true);
		}
        /**
         * Initialise to describe an object property with a minimal cardinality restriction over
         * a {@link OWLClass}.
         * In symbols: {@code classSubject &lt;<sub>d</sub> hasObjectProperty objectClass}.
         * @param cardinality the cardinality of the restriction {@code d}.
         * @param object the class domain of the minimal cardinality restriction over the property.
         */
		protected void setObjectMinRestriction(int cardinality, OWLClass object){
			if( ! isDataProperty) {
				this.expressioneType = RESTRICTION_MIN;
				this.cardinality = cardinality;
				this.object = object;
			} else logger.addDebugString( "Cannot set an 'min' object restriction over a data property", true);
		}
        /**
         * Initialise to describe an object property with a maximal cardinality restriction over
         * a {@link OWLClass}.
         * In symbols: {@code classSubject &gt;<sub>d</sub> hasObjectProperty objectClass}.
         * @param cardinality the cardinality of the restriction {@code d}.
         * @param object the class domain of the maximal cardinality restriction over the property.
         */
		protected void setObjectMaxRestriction(int cardinality, OWLClass object){
			if( ! isDataProperty) {
				this.expressioneType = RESTRICTION_MAX;
				this.cardinality = cardinality;
				this.object = object;
			} else logger.addDebugString( "Cannot set an 'max' object restriction over a data property", true);
		}
        /**
         * Initialise to describe an object property with a exact cardinality restriction over
         * a {@link OWLClass}.
         * In symbols: {@code classSubject =<sub>d</sub> hasObjectProperty objectClass}.
         * @param cardinality the cardinality of the restriction {@code d}.
         * @param object the class domain of the exact cardinality restriction over the property.
         */
        protected void setObjectExactRestriction(int cardinality, OWLClass object){
			if( ! isDataProperty) {
				this.expressioneType = RESTRICTION_EXACT;
				this.cardinality = cardinality;
				this.object = object;
			} else logger.addDebugString( "Cannot set an 'exact' object restriction over a data property", true);
		}

        /**
         * It returns {@code null} if {@code ( ! {@link #isDataProperty} )}.
         * Otherwise, it returns the data type of the data property restriction.
         * @return the data type of the data property restriction.
         */
		public OWLDataRange getDataTypeRestriction(){
			if( isDataProperty)
				return data;
			logger.addDebugString( "a class restriction based on an object property does not have data restrictions.");
			return null;
		}

        /**
         * It returns {@code null} if {@code ( {@link #isDataProperty} )}.
         * Otherwise, it returns the OWL class of the data property restriction.
         * @return the class of the data property restriction.
         */
		public OWLClass getObjectRestriction(){
			if( ! isDataProperty)
				return object;
			logger.addDebugString( "a class restriction based on a data property does not have object restrictions.");
			return null;
		}

        /**
         * Returns {@code null} if the expression type is RESTRICTION_ONLY or RESTRICTION_SOME.
         * Otherwise, it return the cardinality of the data property restriction.
         * @return the cardinality of the data propriety restriction.
         */
		public Integer getCardinality(){
			if( expressioneType >= RESTRICTION_MIN)
				return cardinality;
			logger.addDebugString( getExpressionTypeName() + " does not have a cardinality.", true);
			return null;
		}

        /**
         * @return the property of this restriction, defining the
         * given class (see {@link #isDefinitionOf()}).
         */
		public OWLProperty getProperty() {
			return property;
		}

        /**
         * @return {@link #getProperty()}, casted as {@link OWLDataProperty},
         * if {@code this} {@link #isDataProperty} is true.
         * {@code Null} otherwise.
         */
		public OWLDataProperty getDataProperty() {
			if( isDataProperty)
				return (OWLDataProperty) property;
			logger.addDebugString( "cannot assign data to object property over: " + ontoRef.getOWLObjectName( property), true);
			return null;
		}
        /**
         * @return {@link #getProperty()}, casted as {@link OWLObjectProperty},
         * if {@code this} {@link #isDataProperty} is false.
         * {@code Null} otherwise.
         */
		public OWLObjectProperty getObjectProperty() {
			if( ! isDataProperty)
				return (OWLObjectProperty) property;
			logger.addDebugString( "cannot assign data to object property over: " + ontoRef.getOWLObjectName( property), true);
			return null;
		}

        /**
         * Return the subject of the class restriction.
         * It is the same class given as input to {@link OWLEnquirer#getClassRestrictions(OWLClass)}
         * and produce this container. In the basic set given by {@link OWLEnquirer#getClassRestrictions(OWLClass)}
         * this field is always the same.
         * @return the class that is defined also by this property restriction.
         */
		public OWLClass isDefinitionOf() {
			return definitionOf;
		}

        /**
         * It is used to discriminate this instance for being:
         * {@link OWLManipulator#RESTRICTION_SOME}, {@link OWLManipulator#RESTRICTION_ONLY},
         * {@link OWLManipulator#RESTRICTION_MIN}, {@link OWLManipulator#RESTRICTION_EXACT}
         * or {@link OWLManipulator#RESTRICTION_MAX}.
         * @return the identifier of the expression type.
         */
		public int getExpressiontType() {
			return expressioneType;
		}

        /**
         * @return a string identifying the actual {@link #getExpressiontType()}.
         */
		public String getExpressionTypeName(){
			if( isSomeRestriction())
				return "<some>";
			if( isOnlyRestriction())
				return "<only>";
			if( isMinRestriction())
				return "<min>";
			if( isExactRestriction())
				return "<exact>";
			if( isMaxRestriction())
				return "<max>";
			return "<null>"; // should not happen
		}

        /**
         * @return {@code true} if the {@link #getExpressiontType()} is {@link OWLManipulator#RESTRICTION_SOME}
         */
		public boolean isSomeRestriction(){
			return expressioneType == RESTRICTION_SOME;
		}
        /**
         * @return {@code true} if the {@link #getExpressiontType()} is {@link OWLManipulator#RESTRICTION_ONLY}
         */
		public boolean isOnlyRestriction(){
			return expressioneType == RESTRICTION_ONLY;
		}
        /**
         * @return {@code true} if the {@link #getExpressiontType()} is {@link OWLManipulator#RESTRICTION_MIN}
         */
		public boolean isMinRestriction(){
			return expressioneType == RESTRICTION_MIN;
		}
        /**
         * @return {@code true} if the {@link #getExpressiontType()} is {@link OWLManipulator#RESTRICTION_EXACT}
         */
		public boolean isExactRestriction(){
			return expressioneType == RESTRICTION_EXACT;
		}
        /**
         * @return {@code true} if the {@link #getExpressiontType()} is {@link OWLManipulator#RESTRICTION_MAX}
         */
		public boolean isMaxRestriction(){
			return expressioneType == RESTRICTION_MAX;
		}

        /**
         * @return {@code true} if this object has been instantiated
         * with {@link #ClassRestriction(OWLClass, OWLDataProperty)}.
         * {@code False} otherwise.
         */
		public boolean restrictsOverDataProperty(){
			return isDataProperty;
		}
        /**
         * @return {@code true} if this object has been instantiated
         * with {@link #ClassRestriction(OWLClass, OWLObjectProperty)}.
         * {@code False} otherwise.
         */
		public boolean restrictsOverObjectProperty(){
			return ! isDataProperty;
		}

        /**
         * @return a short name for {@link #isDefinitionOf()}.
         */
		public String getDefinitionOfName(){
			return ontoRef.getOWLObjectName( definitionOf);
		}

        /**
         * @return a short name for {@link #getProperty()}.
         */
		public String getPropertyName(){
			return ontoRef.getOWLObjectName( property);
		}

        /**
         * @return a short name for {@link #getDataTypeRestriction()}
         * if .{@link #isDataProperty}. Otherwise, a short name for
         * {@link #getObjectRestriction()}.
         */
		public String getObjectName(){
			if( isDataProperty) {
			    try{
			        return data.toString().substring( data.toString().lastIndexOf('#') + 1, data.toString().length());
                } catch (Exception e) {
                    return data + "";
                }
            }
			return ontoRef.getOWLObjectName( object);
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (!(o instanceof ClassRestriction)) return false;

            ClassRestriction that = (ClassRestriction) o;

			if (expressioneType != that.expressioneType) return false;
			if (cardinality != that.cardinality) return false;
			if (definitionOf != null ? !definitionOf.equals(that.definitionOf) : that.definitionOf != null)
				return false;
			if (property != null ? !property.equals(that.property) : that.property != null) return false;
			if (isDataProperty != null ? !isDataProperty.equals(that.isDataProperty) : that.isDataProperty != null)
				return false;
			if (object != null ? !object.equals(that.object) : that.object != null) return false;
			return data != null ? data.equals(that.data) : that.data == null;
		}
		@Override
		public int hashCode() {
			int result = definitionOf != null ? definitionOf.hashCode() : 0;
			result = 31 * result + (property != null ? property.hashCode() : 0);
			result = 31 * result + (isDataProperty != null ? isDataProperty.hashCode() : 0);
			result = 31 * result + expressioneType;
			result = 31 * result + cardinality;
			result = 31 * result + (object != null ? object.hashCode() : 0);
			result = 31 * result + (data != null ? data.hashCode() : 0);
			return result;
		}

		@Override
		public String toString() {
			String out = "Class definition: " + getDefinitionOfName() + " " + getPropertyName();
			if( isDataProperty)
			    out += " data::";
			else out += " object::";
			out += " " + getExpressionTypeName();
            if( expressioneType >= RESTRICTION_MIN)
				out += " " + getCardinality();
			out += " " + getObjectName();
			return out;
		}
	}
}
