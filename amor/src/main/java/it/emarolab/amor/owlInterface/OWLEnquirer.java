package it.emarolab.amor.owlInterface;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.clarkparsia.pellet.owlapi.PelletReasoner;
import com.clarkparsia.pellet.sparqldl.jena.SparqlDLExecutionFactory;
import org.apache.jena.query.*;
import org.apache.jena.rdf.model.InfModel;
import org.apache.jena.rdf.model.ModelFactory;
import org.mindswap.pellet.KnowledgeBase;
import org.mindswap.pellet.jena.PelletInfGraph;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLDataPropertyExpression;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import org.semanticweb.owlapi.reasoner.InconsistentOntologyException;
import org.semanticweb.owlapi.search.EntitySearcher;

import it.emarolab.amor.owlDebugger.Logger;
import it.emarolab.amor.owlDebugger.Logger.LoggerFlag;

// TODO : bring up to OWLReferences getSubObjectPropertyOf getSubDataPropertyOf and all its case
// TODO : make an abstract class interface to be implemented for all methods (all have the same shape)
public class OWLEnquirer {

	/**
	 * Object used to log information about this class instances.
	 * Logs are activated by flag: {@link LoggerFlag#LOG_OWL_ENQUIRER}
	 */
	private Logger logger = new Logger( this, LoggerFlag.getLogOWLEnquirer());

	/**
	 * Boolean used to query the reasoner sub/super-(class or properties).
	 * If it is {@code false} only hierarchically direct entities will be returned
	 * (all non-reasoned values are returned anyway).
	 * Else, it collects all entities up to the leafs and root of the structure.
	 */
	public static final Boolean DEFAULT_RETURN_COMPLETE_DESCRIPTION = true;
	
	private Boolean returnCompleteDescription;
	/**
	 * Ontology reference to be manipulated given in the constructor.
	 */
	private OWLReferencesInterface ontoRef;

	/**
	 * Constructor which sets {@link #returnCompleteDescription} flag to
	 * default value {@value #DEFAULT_RETURN_COMPLETE_DESCRIPTION}.
	 * @param owlRef the ontology in which perform queries
	 */
	protected OWLEnquirer( OWLReferencesInterface owlRef){
		this.ontoRef = owlRef;
		this.returnCompleteDescription = DEFAULT_RETURN_COMPLETE_DESCRIPTION;
	}
	/**
	 * Constructor to define custom {@link #returnCompleteDescription} value.
	 * @param owlRef the ontology in which perform queries.
	 * @param returnCompleteDescription the value given to {@link #returnCompleteDescription}.
	 */
	protected OWLEnquirer( OWLReferencesInterface owlRef, Boolean returnCompleteDescription){
		this.ontoRef = owlRef;
		this.returnCompleteDescription = returnCompleteDescription;
	}
	
	/**
	 * @return a container of all the objects of the referenced ontology,
	 * set by constructor.
	 */
	protected OWLReferencesInterface getLiOwlLibrary(){
		return ontoRef;
	}

	/**
	 * @return current value of {@link #returnCompleteDescription}.
	 */
	protected Boolean isReturningCompleteDescription(){
		return returnCompleteDescription;
	}
	/**
	 * @param flag value to set for {@link #returnCompleteDescription}.
	 */
	protected void setReturningCompleteDescription(Boolean flag){
		returnCompleteDescription = flag;
	} 
	
	/**
	 * Returns all individual defined in the ontology {@link OWLDataFactory#getOWLThing()}.
	 * It returns {@code null} if no individuals belong to the root class or if such class does not exist.
	 * Results completeness is ensured only if {@link #returnCompleteDescription} is set to {@code true}.
	 * 
	 * @return individuals belonging to the root class of the ontology.
	 */
	public Set<OWLNamedIndividual> getIndividualB2Thing(){
		return( getIndividualB2Class( ontoRef.getFactory().getOWLThing()));
	}
	/**
	 * Returns all individuals belonging to the specified class.
	 * The method takes a string and calls {@link OWLLibrary#getOWLClass(String)},
     * to fetch the corresponding OWL class object {@link #getIndividualB2Class(OWLClass)}.
     * It returns {@code null} if no individual belongs to that class or if such class does not exist.
	 * Results completeness is ensured only if {@link #returnCompleteDescription} is set to {@code true}.
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
     * Results completeness is ensured only if {@link #returnCompleteDescription} is set to {@code true}.
	 * 
	 * @param ontoClass OWL class.
	 * @return non-ordered set of individuals belonging to such class.
	 */
	public Set<OWLNamedIndividual> getIndividualB2Class( OWLClass ontoClass){
		long initialTime = System.nanoTime();
		Set< OWLNamedIndividual> out = new HashSet< OWLNamedIndividual>();
		
		//Set<OWLIndividual> set = ontoClass.getIndividuals( ontoRef.getOntology());
		Stream<OWLIndividual> stream = EntitySearcher.getIndividuals( ontoClass, ontoRef.getOntology());
		Set<OWLIndividual> set = stream.collect(Collectors.toSet());
		
		if( set != null){
			for( OWLIndividual s : set)
				out.add( s.asOWLNamedIndividual());
		}
		try{
			out.addAll( ontoRef.getReasoner().getInstances( ontoClass, !returnCompleteDescription).getFlattened());
		} catch( InconsistentOntologyException e){
			ontoRef.loggInconsistency();
		}
		logger.addDebugString( "Individual belonging to class given in: " + (System.nanoTime() - initialTime) + " [ns]");
		return( out);
	}
	/**
	 * Returns one individual belonging to the root class {@link OWLDataFactory#getOWLThing()}.
	 * It returns {@code null} if no individual is classified in the class, meaning the ontology is not populated.
	 * Results completeness is ensured only if {@link #returnCompleteDescription} is set to {@code true}.
	 * 
	 * @return an individual belonging to the root class of the ontology.
	 */
	public OWLNamedIndividual getOnlyIndividualB2Thing(){
		return( getOnlyIndividualB2Class( ontoRef.getFactory().getOWLThing()));
	}
	/**
	 * Returns one individual belonging to the specified class. 
	 * Returns {@code null} if no individual are classified in that class,
	 * if such class does not exist or if the Set returned by 
	 * {@code this.getIndividualB2Class( .. )} has {@code size > 1}.
	 * Results completeness is ensured only if {@link #returnCompleteDescription} is set to {@code true}.
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
	 * Results completeness is ensured only if {@link #returnCompleteDescription} is set to {@code true}.
	 * 
	 * @param ontoClass OWLClass object in which to search.
	 * @return an individual belonging to ontoClass.
	 */
	public OWLNamedIndividual getOnlyIndividualB2Class( OWLClass ontoClass){
		Set< OWLNamedIndividual> set = getIndividualB2Class( ontoClass);
		return( (OWLNamedIndividual) ontoRef.getOnlyElement( set));
	}

	/**
	 * Returns the set of classes in which an individual has been classified.
	 * Results completeness is ensured only if {@link #returnCompleteDescription} is set to {@code true}.
	 * 
	 * @param individual
	 * @return set of all classes the individual belongs to.
	 */
	public Set< OWLClass> getIndividualClasses( OWLNamedIndividual individual){
		long initialTime = System.nanoTime();
		Set< OWLClass> out = new HashSet< OWLClass>();
		
		//Set< OWLClassExpression> set = individual.getTypes( ontoRef.getOntology());
		Stream<OWLClassExpression> stream = EntitySearcher.getTypes( individual, ontoRef.getOntology());
		Set< OWLClassExpression> set = stream.collect(Collectors.toSet());
		
		if( set != null){
			for( OWLClassExpression s : set)
				out.add( s.asOWLClass());
		}
		try{
			out.addAll( ontoRef.getReasoner().getTypes( individual, !returnCompleteDescription).getFlattened());
		} catch( org.semanticweb.owlapi.reasoner.InconsistentOntologyException e){
			ontoRef.loggInconsistency();
		}
		logger.addDebugString( "Types of individual given in: " + (System.nanoTime() - initialTime) + " [ns]");
		return out;
	}
	/**
	 * Returns the set of classes in which an individual has been classified.
	 * Results completeness is ensured only if {@link #returnCompleteDescription} is set to {@code true}.
	 *
	 * @param individualName name of the individual.
	 * @return a set of all classes the individual belongs to.
	 */
	public Set< OWLClass> getIndividualClasses( String individualName){
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
		OWLDataProperty prop = ontoRef.getOWLDataProperty( propertyName); 
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
		
		//Set<OWLLiteral>  value = individual.getDataPropertyValues(property, ontoRef.getOntology());
		Stream<OWLLiteral> stream = EntitySearcher.getDataPropertyValues(individual, property, ontoRef.getOntology());
		Set< OWLLiteral> value = stream.collect( Collectors.toSet());
		
		try{
			Set<OWLLiteral> valueInf = ontoRef.getReasoner().getDataPropertyValues( individual, property);
			value.addAll( valueInf);
		} catch( InconsistentOntologyException e){
			ontoRef.loggInconsistency();
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
	 * @param individual
	 * @param property
	 * @return non-ordered set of the property value entities ({@link OWLNamedIndividual}).
	 */
	public Set<OWLNamedIndividual> getObjectPropertyB2Individual( OWLNamedIndividual individual, OWLObjectProperty property){
		long initialTime = System.nanoTime();
		Set< OWLNamedIndividual> out = new HashSet< OWLNamedIndividual>();
		
		//Set< OWLIndividual> set = individual.getObjectPropertyValues(property, ontoRef.getOntology());
		Stream< OWLIndividual> stream = EntitySearcher.getObjectPropertyValues(individual, property, ontoRef.getOntology());
		Set< OWLIndividual> set = stream.collect( Collectors.toSet());
		
		if( set != null){
			for( OWLIndividual i : set)
				out.add( i.asOWLNamedIndividual());
		}
		try{
			Set<OWLNamedIndividual> reasoned = ontoRef.getReasoner().getObjectPropertyValues( individual, property).getFlattened();
			out.addAll( reasoned);
		} catch( InconsistentOntologyException e){
			ontoRef.loggInconsistency();
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
	 * @param individual
	 * @param property
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
	 * @param individual
	 * @return all the object properties and value entities of the given individual.
	 */
	public Set<ObjectPropertyRelations> getObjectPropertyB2Individual(OWLNamedIndividual individual){
		Set<ObjectPropertyRelations> out = new HashSet<ObjectPropertyRelations>();
		// get all object prop in the ontology
		OWLObjectProperty topObjProp = ontoRef.getFactory().getOWLTopObjectProperty();
		Set<OWLObjectProperty> allProp = getSubObjectPropertyOf(topObjProp);
		for( OWLObjectProperty p : allProp){ // check if a property belongs to this individual
			Set<OWLNamedIndividual> values = getObjectPropertyB2Individual(individual, p);
			if( ! values.isEmpty())
				out.add( new ObjectPropertyRelations(individual, p, values, ontoRef));
		}
		return out;	
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
		return getObjectPropertyB2Individual( ontoRef.getOWLIndividual( individualName));	
	}	
	/**
	 * Class used to contain an object property relation associated to an individuals and its value entities.
	 * A {@link ObjectPropertyRelations} object is returned by
	 * {@link OWLEnquirer#getObjectPropertyB2Individual(OWLNamedIndividual)}
	 * and {@link OWLEnquirer#getObjectPropertyB2Individual(String)}.
	 */
	public class ObjectPropertyRelations {
		private OWLObjectProperty prop;
		private OWLNamedIndividual ind;
		private Set< OWLNamedIndividual> value;
		private String propName, indName;
		private Set<String> valueName;
		
		public ObjectPropertyRelations(OWLNamedIndividual ind, OWLObjectProperty prop, Set<OWLNamedIndividual> value,
                                       OWLReferencesInterface ontoRef) {
			this.prop = prop;
			this.propName = ontoRef.getOWLObjectName( prop);
			this.ind = ind;
			this.indName = ontoRef.getOWLObjectName( ind);
			this.value = value;
			this.valueName = ontoRef.getOWLObjectName( value);
		}
		
		public OWLObjectProperty getProperty() {
			return prop;
		}
		public OWLNamedIndividual getIndividuals() {
			return ind;
		}
		public Set< OWLNamedIndividual> getValues() {
			return value;
		}
		
		public String getPropertyName() {
			return propName;
		}
		public String getIndividualsName() {
			return indName;
		}
		public Set< String> getValuesName() {
			return valueName;
		}
		public String toString(){
			return "\"" + getIndividualsName() + "." + getPropertyName() + "( " + getValuesName() + ")"; 
		}
	}
		
	/** Returns all data properties and relative value entities relative to an individual.
	 * Note that this implementation may be not efficient since it iterate over all 
	 * the object property of the ontology.
	 * 
	 * @param individual
	 * @return all the object properties and value entities of the given individual.
	 */
	public Set< DataPropertyRelations> getDataPropertyB2Individual( OWLNamedIndividual individual){
		Set< DataPropertyRelations> out = new HashSet< DataPropertyRelations>();
		// get all object prop in the ontology
		OWLDataProperty topObjProp = ontoRef.getFactory().getOWLTopDataProperty();
		Set<OWLDataProperty> allProp = getSubDataPropertyOf(topObjProp);
		for( OWLDataProperty p : allProp){ // check if a property belongs to this individual
			Set<OWLLiteral> values = getDataPropertyB2Individual(individual, p);
			if( ! values.isEmpty())
				out.add( new DataPropertyRelations(individual, p, values, ontoRef));
		}
		return out;	
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
	 * Class used to contain a data property relation  associated to an individuals and its literal values.
	 * A {@link DataPropertyRelations} object is returned by
     * {@link OWLEnquirer#getObjectPropertyB2Individual(OWLNamedIndividual)}
	 * and {@link OWLEnquirer#getObjectPropertyB2Individual(String)}.
	 */
	public class DataPropertyRelations{
		private OWLDataProperty prop;
		private OWLNamedIndividual ind;
		private Set< OWLLiteral> value;
		private String propName, indName;
		private Set<String> valueName;
		
		public DataPropertyRelations( OWLNamedIndividual ind, OWLDataProperty prop, Set<OWLLiteral> value, OWLReferencesInterface ontoRef) {
			this.prop = prop;
			this.propName = ontoRef.getOWLObjectName( prop);
			this.ind = ind;
			this.indName = ontoRef.getOWLObjectName( ind);
			this.value = value;
			this.valueName = ontoRef.getOWLObjectName( value);
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
		public Set< String> getValuesName() {
			return valueName;
		}
		public String toString(){
			return "\"" + getIndividualName() + "." + getPropertyName() + "( " + getValuesName() + ")";
		}
	}


	/**
	 * Returns all the sub object properties of a given property. 
	 * It check in the ontology definition frist and then in the 
	 * inferred axioms by the reasoner.
	 * Also note that the completeness of the results 
	 * of this methods also depends from the value
	 * of {@link #returnCompleteDescription}.
	 * @param prop an object property
	 * @return the sub object property of the input parameter ({@code prop})
	 */
	public Set<OWLObjectProperty> getSubObjectPropertyOf( OWLObjectProperty prop){
		long initialTime = System.nanoTime();
		
		//Set<OWLObjectPropertyExpression> set = prop.getSubProperties( ontoRef.getOntology());//cl.getSubClasses( ontoRef.getOntology());
		Stream<OWLObjectPropertyExpression> stream = EntitySearcher.getSubProperties( prop, ontoRef.getOntology());
		Set<OWLObjectPropertyExpression> set = stream.collect( Collectors.toSet());
		
		Set<OWLObjectProperty> out = new HashSet< OWLObjectProperty>();
		if( set != null){
			for( OWLObjectPropertyExpression s : set)
				out.add( s.asOWLObjectProperty());
		}
		try{
			Set<OWLObjectPropertyExpression> inferred = ontoRef.getReasoner().getSubObjectProperties(prop, !returnCompleteDescription).getFlattened();
			for( OWLObjectPropertyExpression e : inferred)
				out.add( e.asOWLObjectProperty());
		} catch( InconsistentOntologyException e){
			ontoRef.loggInconsistency();
		}
		logger.addDebugString( "get sub classes of given in: " + (System.nanoTime() - initialTime) + " [ns]");
		return( out);
	}
	/**
	 * Returns all sub-properties of a given object property fetched by {@link #getSubObjectPropertyOf(OWLObjectProperty)}.
	 * It checks axioms in the ontology first, then reasoner inferred axioms.
     * Results completeness is ensured only if {@link #returnCompleteDescription} is set to {@code true}.
	 * @param propName the name of the property property.
	 * @return set of sub-properties of {@code propName}.
	 */
	public Set<OWLObjectProperty> getSubObjectPropertyOf( String propName){
		OWLObjectProperty prop = ontoRef.getOWLObjectProperty( propName);
		return getSubObjectPropertyOf( prop);
	}
	
	/** Returns all the super-properties of a given object property.
     * It checks axioms in the ontology first, then reasoner inferred axioms.
     * Results completeness is ensured only if {@link #returnCompleteDescription} is set to {@code true}.
	 * @param prop an object property.
	 * @return set of super-properties of {@code propName}.
	 */
	public Set<OWLObjectProperty> getSuperObjectPropertyOf( OWLObjectProperty prop){
		long initialTime = System.nanoTime();
		
		//Set<OWLObjectPropertyExpression> set = prop.getSuperProperties( ontoRef.getOntology());
		Stream<OWLObjectPropertyExpression> stream = EntitySearcher.getSuperProperties( prop, ontoRef.getOntology());
		Set<OWLObjectPropertyExpression> set = stream.collect( Collectors.toSet());
		
		Set<OWLObjectProperty> out = new HashSet< OWLObjectProperty>();
		if( set != null){
			for( OWLObjectPropertyExpression s : set)
				out.add( s.asOWLObjectProperty());
		}
		try{
			Set<OWLObjectPropertyExpression> inferred = ontoRef.getReasoner().getSuperObjectProperties(prop, !returnCompleteDescription).getFlattened();
			for( OWLObjectPropertyExpression e : inferred)
				out.add( e.asOWLObjectProperty());
		} catch( InconsistentOntologyException e){
			ontoRef.loggInconsistency();
		}
		logger.addDebugString( "get sub classes of given in: " + (System.nanoTime() - initialTime) + " [ns]");
		return( out);
	}
	/**
	 * Returns all sub-properties of a given object property fetched by {@link #getSuperObjectPropertyOf(OWLObjectProperty)}.
     * It checks axioms in the ontology first, then reasoner inferred axioms.
     * Results completeness is ensured only if {@link #returnCompleteDescription} is set to {@code true}.
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
     * Results completeness is ensured only if {@link #returnCompleteDescription} is set to {@code true}.
	 * @param prop a data property.
	 * @return set of sub-properties of {@code prop}.
	 */
	public Set<OWLDataProperty> getSubDataPropertyOf( OWLDataProperty prop){ 
		long initialTime = System.nanoTime();
		
		//Set<OWLDataPropertyExpression> set = prop.getSubProperties( ontoRef.getOntology());
		Stream<OWLDataPropertyExpression> stream = EntitySearcher.getSubProperties( prop, ontoRef.getOntology());
		Set<OWLDataPropertyExpression> set = stream.collect( Collectors.toSet());
		
		Set<OWLDataProperty> out = new HashSet< OWLDataProperty>();
		if( set != null){
			for( OWLDataPropertyExpression s : set)
				out.add( s.asOWLDataProperty());
		}
		try{
			Set<OWLDataProperty> inferred = ontoRef.getReasoner().getSubDataProperties(prop, !returnCompleteDescription).getFlattened();
			out.addAll( inferred);
		} catch( InconsistentOntologyException e){
			ontoRef.loggInconsistency();
		}
		logger.addDebugString( "get sub classes of given in: " + (System.nanoTime() - initialTime) + " [ns]");
		return( out);
	}
	/**
	 * Returns all sub-properties of a given data property fetched by {@link #getSubDataPropertyOf(OWLDataProperty)}.
     * It checks axioms in the ontology first, then reasoner inferred axioms.
     * Results completeness is ensured only if {@link #returnCompleteDescription} is set to {@code true}.
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
     * Results completeness is ensured only if {@link #returnCompleteDescription} is set to {@code true}.
	 * @param prop a data property.
	 * @return set of sub-properties of {@code prop}.
	 */
	public Set<OWLDataProperty> getSuperDataPropertyOf( OWLDataProperty prop){ 
		long initialTime = System.nanoTime();
		
		//Set<OWLDataPropertyExpression> set = prop.getSuperProperties( ontoRef.getOntology());
		Stream<OWLDataPropertyExpression> stream = EntitySearcher.getSuperProperties( prop, ontoRef.getOntology());
		Set<OWLDataPropertyExpression> set = stream.collect( Collectors.toSet());
		
		Set<OWLDataProperty> out = new HashSet< OWLDataProperty>();
		if( set != null){
			for( OWLDataPropertyExpression s : set)
				out.add( s.asOWLDataProperty());
		}
		try{
			Set<OWLDataProperty> infered = ontoRef.getReasoner().getSuperDataProperties(prop, !returnCompleteDescription).getFlattened();
			out.addAll( infered);
		} catch( InconsistentOntologyException e){
			ontoRef.loggInconsistency();
		}
		logger.addDebugString( "get sub classes of given in: " + (System.nanoTime() - initialTime) + " [ns]");
		return( out);
	}
	/**
	 * Returns all super-properties of a given data property fetched by {@link #getSuperDataPropertyOf(OWLDataProperty)}.
     * It checks axioms in the ontology first, then reasoner inferred axioms.
     * Results completeness is ensured only if {@link #returnCompleteDescription} is set to {@code true}.
	 * @param propName a data property.
	 * @return set of sub-properties of {@code prop}.
	 */
	public Set<OWLDataProperty> getSuperDataPropertyOf( String propName){
		OWLDataProperty prop = ontoRef.getOWLDataProperty( propName);
		return getSuperDataPropertyOf( prop);
	}
	
	
	/**
	 * Returns all sub-classes of a given class.
	 * Results completeness is ensured only if {@link #returnCompleteDescription} is set to {@code true}.
	 * 
	 * @param className name of an OWL class.
	 * @return non-ordered set of sub-classes.
	 */
	public Set<OWLClass> getSubClassOf( String className){
		OWLClass cl = ontoRef.getOWLClass( className);
		return( getSubClassOf( cl));
	}
	/**
	 * Returns all sub-classes of a given class.
     * Results completeness is ensured only if {@link #returnCompleteDescription} is set to {@code true}.
	 * 
	 * @param cl an OWL class.
	 * @return non-ordered set of sub-classes.
	 */
	public Set<OWLClass> getSubClassOf( OWLClass cl){
		long initialTime = System.nanoTime();
		
		//Set<OWLClassExpression> set = cl.getSubClasses( ontoRef.getOntology());
		Stream<OWLClassExpression> stream = EntitySearcher.getSubClasses( cl, ontoRef.getOntology());
		Set<OWLClassExpression> set = stream.collect( Collectors.toSet());
		
		Set<OWLClass> out = new HashSet< OWLClass>();
		if( set != null){
			for( OWLClassExpression s : set)
				out.add( s.asOWLClass());
		}
		try{
			out.addAll( ontoRef.getReasoner().getSubClasses( cl, !returnCompleteDescription).getFlattened());
		} catch( InconsistentOntologyException e){
			ontoRef.loggInconsistency();
		}
		logger.addDebugString( "get sub classes of given in: " + (System.nanoTime() - initialTime) + " [ns]");
		return( out);
	}

	/**
	 * Returns all super-classes of a given class.
     * Results completeness is ensured only if {@link #returnCompleteDescription} is set to {@code true}.
	 * 
	 * @param className name of an OWL class.
	 * @return non-ordered set of sub-classes.
	 */
	public Set<OWLClass> getSuperClassOf( String className){
		OWLClass cl = ontoRef.getOWLClass( className);
		return( getSuperClassOf( cl));
	}
	/**
	 * Returns all super-classes of a given class.
     * Results completeness is ensured only if {@link #returnCompleteDescription} is set to {@code true}.
	 * 
	 * @param cl an OWL class.
	 * @return non-ordered set of sub-classes.
	 */
	public Set<OWLClass> getSuperClassOf( OWLClass cl){
		long initialTime = System.nanoTime();
		Set<OWLClass> classes = new HashSet< OWLClass>();
		
		//Set< OWLClassExpression> set = cl.getSuperClasses( ontoRef.getOntology());
		Stream<OWLClassExpression> stream = EntitySearcher.getSuperClasses( cl, ontoRef.getOntology());
		Set<OWLClassExpression> set = stream.collect( Collectors.toSet());
		
		if( set != null){ 
			for( OWLClassExpression j : set)
				classes.add( j.asOWLClass());
		}
		try{
			classes.addAll( ontoRef.getReasoner().getSuperClasses( cl, !returnCompleteDescription).getFlattened());
		} catch( InconsistentOntologyException e){
			ontoRef.loggInconsistency();
		}
		logger.addDebugString( "get super classes of given in: " + (System.nanoTime() - initialTime) + " [ns]");
		return( classes);
	}


	// works only for pellet
	// set time out to null or < 0 to do not apply any timing out

    /**
     * Performs a SPARQL query on the ontology. Returns a list of {@link QuerySolution} or {@code null} if the query fails.
     * Works only with the Pellet reasoner. {@code timeOut} parameter sets the query timeout, no timeout is set if
     * {@code timeOut} <= 0. Once timeout is reached, all solutions found up to that point are returned.
     * @param query a string defining the query in SPARQL query syntax.
     * @param timeOut timeout for the query.
     * @return list of solutions.
     */
	public List< QuerySolution> sparql( String query, Long timeOut){
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
		} catch ( QueryCancelledException e){
            logger.addDebugString("SPARQL timed out !!");
			return null;
		}
	}
    /**
     * Performs a SPARQL query on the ontology. Returns a list of {@link QuerySolution} or {@code null} if the query fails.
     * Works only with the Pellet reasoner.
     * @param query a string defining the query in SPARQL query syntax.
     * @return list of solutions.
     */
	public List< QuerySolution> sparql( String query){ // no time out
		return sparql( query, null);
	}
    /**
     * Performs a SPARQL query on the ontology. Returns a list of {@link QuerySolution} or {@code null} if the query fails.
     * Works only with the Pellet reasoner. {@code timeOut} parameter sets the query timeout, no timeout is set if
     * {@code timeOut} <= 0. Once timeout is reached, all solutions found up to that point are returned.
     * @param prefix a string defining the query prefix field in SPARQL query syntax.
     * @param select a string defining the query select field in SPARQL query syntax.
     * @param where a string defining the query where field in SPARQL query syntax.
     * @param timeOut timeout for the query.
     * @return list of solutions.
     */
	public List< QuerySolution> sparql( String prefix, String select, String where, Long timeOut){
		return sparql( prefix + select + where, timeOut);
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
		return sparql( prefix + select + where);
	}
    /**
     * An utility method that call {@link #sparql(String, Long)} and translates the results to a list of maps among strings.
     * Used to share the results with other code and processes. {@code timeOut} parameter sets the query timeout,
     * no timeout is set if {@code timeOut} <= 0. Once timeout is reached, all solutions found up to that point are returned.
     * @param query a string defining the query in SPARQL query syntax.
     * @param timeOut timeout for the query.
     * @return formatted list of solutions.
     */
	public List< Map< String, String>> sparqlMsg(String query, Long timeOut){
		return sparql2Msg( sparql( query, timeOut));
	}
    /**
     * An utility method that call {@link #sparql(String, Long)} and translates the results to a list of maps among strings.
     * Used to share the results with other code and processes.
     * @param query a string defining the query in SPARQL query syntax.
     * @return formatted list of solutions.
     */
	public List< Map< String, String>> sparqlMsg( String query){ // no time out
		return sparql2Msg( sparql( query, null));
	}
    /**
     * An utility method that call {@link #sparql(String, Long)} and translates the results to a list of maps among strings.
     * Used to share the results with other code and processes. {@code timeOut} parameter sets the query timeout,
     * no timeout is set if {@code timeOut} <= 0. Once timeout is reached, all solutions found up to that point are returned.
     * @param prefix a string defining the query prefix field in SPARQL query syntax.
     * @param select a string defining the query select field in SPARQL query syntax.
     * @param where a string defining the query where field in SPARQL query syntax.
     * @param timeOut timeout for the query.
     * @return list of solutions.
     */
	public List< Map< String, String>> sparqlMsg( String prefix, String select, String where, Long timeOut){
		return sparql2Msg( sparql( prefix + select + where, timeOut));
	}
    /**
     * An utility method that call {@link #sparql(String, Long)} and translates the results to a list of maps among strings.
     * Used to share the results with other code and processes.
     * @param prefix a string defining the query prefix field in SPARQL query syntax.
     * @param select a string defining the query select field in SPARQL query syntax.
     * @param where a string defining the query where field in SPARQL query syntax.
     * @return list of solutions.
     */
    public List< Map< String, String>> sparqlMsg( String prefix, String select, String where){
		return sparql2Msg( sparql( prefix + select + where));
	}

    /**
     * Formats a list of {@link QuerySolution} into a list of maps among strings.
     * @param results list of {@link QuerySolution}.
     * @return formatted results.
     */
    private List< Map< String, String>> sparql2Msg( List< QuerySolution> results) {
        List< Map< String, String>> out = new ArrayList();
		if( results != null) // timeout
			for( QuerySolution q : results){
				Iterator<String> names = q.varNames();
				Map<String, String> item = new HashMap<>();
				while( names.hasNext()){
					String n = names.next();
					item.put( n, q.get( n).toString());
				}
				out.add( item);
			}
        return out;
    }
}
