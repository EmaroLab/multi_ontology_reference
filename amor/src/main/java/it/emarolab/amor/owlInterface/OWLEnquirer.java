package it.emarolab.amor.owlInterface;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.clarkparsia.pellet.owlapi.PelletReasoner;
import com.clarkparsia.pellet.owlapi.PelletReasonerFactory;
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
	 * @param owlRef the ontology in which perform queries
	 * @param returnCompleteDescription the value given to {@link #returnCompleteDescription}
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
		logger.addDebugString( "Individual belong to class given in: " + (System.nanoTime() - initialTime) + " [ns]");
		return( out);
	}
	/**
	 * Returns one individual belonging to the root class {@link OWLDataFactory#getOWLThing()}.
	 * It returns {@code null} if no individual are classified in that class, so if no individuals are introduced into the ontology.
	 * Finally note that the completeness of the results 
	 * of this methods also depends from the value
	 * of {@link #returnCompleteDescription}.
	 * 
	 * @return an individual belong to the root class of the ontology.
	 */
	public OWLNamedIndividual getOnlyIndividualB2Thing(){
		return( getOnlyIndividualB2Class( ontoRef.getFactory().getOWLThing()));
	}
	/**
	 * It returns one ontological individual which are defined in the 
	 * refereed ontology and which are belonging to the class with name 
	 * defined by the parameter. Indeed this method will call {@link #getOWLClass(String)},
	 * to get the actual OWL class Object and than it use it to call
	 * {@link #getIndividualB2Class(OWLClass)}. Than,
	 * using {@link #getOnlyElement(Set)} it will return one
	 * individual that are belonging to the class. It returns null if no individual are
	 * classified in that class, if such class does not exist in 
	 * the refereed ontology or if the Set returned by 
	 * {@code this.getIndividualB2Class( .. )} has {@code size > 1}.
	 * Finally note that the completeness of the results 
	 * of this methods also depends from the value
	 * of {@link #returnCompleteDescription}.
	 * 
	 * @param className name of the ontological class
	 * @return an individual belong to such class.
	 */
	public OWLNamedIndividual getOnlyIndividualB2Class( String className){
		Set<OWLNamedIndividual> set = getIndividualB2Class( ontoRef.getOWLClass( className));
		return( (OWLNamedIndividual) ontoRef.getOnlyElement(set));
	}
	/**
	 * It returns an ontological individual which are defined in the 
	 * refereed ontology and which are belonging to the class 
	 * defined by the parameter. It returns null if no individual are
	 * classified in it, if such class does not 
	 * exist  or if there are more than one
	 * individual classified in that class 
	 * (since it uses {@link #getOnlyElement(Set)}). 
	 * Finally note that the completeness of the results 
	 * of this methods also depends from the value
	 * of {@link #returnCompleteDescription}.
	 * 
	 * @param ontoClass OWL class for which the individual are asked.
	 * @return an individual belong to such class.
	 */
	public OWLNamedIndividual getOnlyIndividualB2Class( OWLClass ontoClass){
		Set< OWLNamedIndividual> set = getIndividualB2Class( ontoClass);
		return( (OWLNamedIndividual) ontoRef.getOnlyElement( set));
	}

	/**
	 * It returns the set of classes in which an individual has been
	 * classified.
	 * Finally note that the completeness of the results 
	 * of this methods also depends from the value
	 * of {@link #returnCompleteDescription}.
	 * 
	 * @param individual ontological individual object
	 * @return a not ordered set of all the classes where the 
	 * individual is belonging to.
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
		logger.addDebugString( "Types of insdividual given in: " + (System.nanoTime() - initialTime) + " [ns]");
		return out;
	}
	/**
	 * It returns the set of classes in which an individual has been
	 * classified.
	 * Finally note that the completeness of the results 
	 * of this methods also depends from the value
	 * of {@link #returnCompleteDescription}.
	 * 
	 * @param individual ontological individual object.
	 * @return a not ordered set of all the classes where the 
	 * individual is belonging to.
	 */
	public Set< OWLClass> getIndividualClasses( String individualName){
		OWLNamedIndividual ind = ontoRef.getOWLIndividual( individualName);
		return getIndividualClasses( ind);
	}
	/**
	 * Returns one calass in which the individual is belonging to.
	 * Indeed this method calls {@link #getIndividualClasses(String)}
	 * and its returning value retrieved trhough with {@link #getOnlyElement(Set)}.
	 * 
	 * @param individualName ontological individual object.
	 * @return one class inwhic the input individual is belonging to.
	 */
	public OWLClass getOnlyIndividualClasses( String individualName){
		Set< OWLClass> set = getIndividualClasses( individualName);
		return( ( OWLClass) ontoRef.getOnlyElement( set));
	}
	/**
	 * Returns one calass in which the individual is belonging to.
	 * Indeed this method calls {@link #getIndividualClasses(OWLNamedIndividual)}
	 * and its returning value retrieved trhough with {@link #getOnlyElement(Set)}.
	 * 
	 * @param individualName ontological individual object.
	 * @return one class inwhic the input individual is belonging to.
	 */
	public OWLClass getOnlyIndividualClasses( OWLNamedIndividual individual){
		Set< OWLClass> set = getIndividualClasses( individual);
		return( ( OWLClass) ontoRef.getOnlyElement( set));
	}
	
	/**
	 * Returns the set of literal value relate to an OWL Data Property 
	 * which has a specific name and which is assign to a given individual. 
	 * Indeed it retrieves OWL object from strings and calls: 
	 * {@link #getDataPropertyB2Individual(OWLNamedIndividual, OWLDataProperty)}.
	 * Than its returning value is propagated.
	 * 
	 * @param individualName name to the ontological individual belonging to the referring ontology
	 * @param propertyName data property name applied to the ontological individual belonging to the referring ontology
	 * @return a not ordered set of literal value of such property applied to a given individual
	 */
	public Set<OWLLiteral> getDataPropertyB2Individual( String individualName, String propertyName){
		OWLNamedIndividual ind = ontoRef.getOWLIndividual( individualName);
		OWLDataProperty prop = ontoRef.getOWLDataProperty( propertyName); 
		return( getDataPropertyB2Individual( ind, prop));
	}
	/**
	 * Returns the set of literal value relate to an OWL Data Property 
	 * and assigned to a given individual. It returns null if such data property or
	 * individual does not exist. Also if the individual has not such a
	 * property.
	 * 
	 * @param individual the OWL individual belonging to the referring ontology
	 * @param property the OWL data property applied to the ontological individual belonging to the refering ontology
	 * @return a not ordered set of literal value of such property applied to a given individual
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
		logger.addDebugString( "get data property belong to individual given in: " + (System.nanoTime() - initialTime) + " [ns]");
		return ( value);
	}
	/**
	 * Returns one literal value attached to a given individual
	 * through a specific data property. Here both, individual and property, are given
	 * by name, than the system calls {@link #getOnlyDataPropertyB2Individual(String, String)}
	 * and its returning value is used with {@link #getOnlyElement(Set)}.
	 * 
	 * @param individualName name to the ontological individual belonging to the referring ontology
	 * @param propertyName data property name applied to the ontological individual belonging to the referring ontology
	 * @return a literal value of such property applied to a given individual
	 */
	public OWLLiteral getOnlyDataPropertyB2Individual( String individualName, String propertyName){
		Set<OWLLiteral> set = getDataPropertyB2Individual( individualName, propertyName);
		return( (OWLLiteral) ontoRef.getOnlyElement( set));
	}
	/**
	 * Returns one literal value attached to a given OWL individual 
	 * through an OWL data property. This returns null if {@link #getDataPropertyB2Individual(OWLNamedIndividual, OWLDataProperty)}
	 * or {@link #getOnlyElement(Set)} return null.
	 * 
	 * @param individual the OWL individual belonging to the referring ontology
	 * @param property the OWL data property applied to the ontological individual belonging to the referring ontology
	 * @return a literal value of such property applied to a given individual
	 */
	public OWLLiteral getOnlyDataPropertyB2Individual( OWLNamedIndividual individual, OWLDataProperty property){
		Set<OWLLiteral> set = getDataPropertyB2Individual( individual, property);
		return( (OWLLiteral) ontoRef.getOnlyElement( set));
	}


	/**
	 * Returns all the values (individuals) to an Object property, given by name,
	 * linked to an individual, given by name as well. Indeed it retrieve the OWL
	 * Objects by name using {@link #getOWLObjectProperty(String)}
	 * and {@link #getOWLIndividual(String)}. 
	 * Than it calls {@link #getObjectPropertyB2Individual(OWLNamedIndividual, OWLObjectProperty)}
	 * propagating its returning value.
	 * 
	 * @param individualName the name of an ontological individual 
	 * @param propertyName the name of an ontological object property
	 * @return a not ordered set of all the values (OWLNamedIndividual) that
	 * the individual has w.r.t. such object property. 
	 */
	public Set<OWLNamedIndividual> getObjectPropertyB2Individual( String individualName, String propertyName){
		OWLNamedIndividual ind = ontoRef.getOWLIndividual( individualName);
		OWLObjectProperty prop = ontoRef.getOWLObjectProperty( propertyName);
		return( getObjectPropertyB2Individual( ind, prop));
	}
	/**
	 * Returns all the values (individuals) to an Object property, given by name,
	 * linked to an individual, given by name as well. It will return null
	 * if such object property or individual does not exist.
	 * 
	 * @param individual an OWL individual
	 * @param property an OWL object property
	 * @return a not ordered set of all the values (OWLNamedIndividual) that
	 * the individual has w.r.t. such object property.
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
		logger.addDebugString( "get object property belong to individual given in: " + (System.nanoTime() - initialTime) + " [ns]");
		return( out);
	}
	/**
	 * Returns a value (individual) to an Object property, given by name,
	 * linked to an individual, given by name as well. Indeed it retrueve the OWL
	 * Objects by name using {@link #getOWLObjectProperty(String, OWLReferences)}
	 * and {@link #getOWLIndividual(String, OWLReferences)}. 
	 * Than it calls {@link #getObjectPropertyB2Individual(OWLNamedIndividual, OWLObjectProperty, OWLReferences)}
	 * and its returning value is used to call 
	 * {@link #getOnlyElement(Set)} which define the actual returning
	 * value of this method.
	 * 
	 * @param individualName the name of an ontological individual 
	 * @param propertyName the name of an ontological object property
	 * @param ontoRef reference to an OWL ontology.
	 * @return a value (OWLNamedIndividual) that
	 * the individual has w.r.t. such object property. 
	 */
	public OWLNamedIndividual getOnlyObjectPropertyB2Individual( String individualName, String propertyName){
		OWLNamedIndividual ind = ontoRef.getOWLIndividual( individualName);
		OWLObjectProperty prop = ontoRef.getOWLObjectProperty( propertyName);
		Set<OWLNamedIndividual> set = getObjectPropertyB2Individual( ind, prop);
		return( (OWLNamedIndividual) ontoRef.getOnlyElement( set));
	}
	/**
	 * Returns a value (individual) to an Object property, given by name,
	 * linked to an individual, given by name as well. It will return null
	 * if such object property or individual does not exist. 
	 * Finally it can return null if {@link #getOnlyElement(Set)} returns
	 * null.
	 * 
	 * @param individual an OWL individual
	 * @param property an OWL object property
	 * @return a value (OWLNamedIndividual) that
	 * the individual has w.r.t. such object property.
	 */
	public OWLNamedIndividual getOnlyObjectPropertyB2Individual( OWLNamedIndividual individual, OWLObjectProperty property){
		Set< OWLNamedIndividual> all = getObjectPropertyB2Individual( individual, property);
		return( (OWLNamedIndividual) ontoRef.getOnlyElement( all));
	}

	/**
	 * Returns all the object properties (with relative values) that are
	 * belonging to an individual.
	 * Note that this implementation may be not efficient since it iterate over all 
	 * the object property of the ontology.
	 * 
	 * @param individual an OWL individual
	 * @return all the object properties, and values, of the given individual
	 */
	public Set< ObjectPropertyRelatios> getObjectPropertyB2Individual( OWLNamedIndividual individual){
		Set< ObjectPropertyRelatios> out = new HashSet< ObjectPropertyRelatios>();
		// get all object prop in the ontology
		OWLObjectProperty topObjProp = ontoRef.getFactory().getOWLTopObjectProperty();
		Set<OWLObjectProperty> allProp = getSubObjectPropertyOf(topObjProp);
		for( OWLObjectProperty p : allProp){ // check if a property belongs to this individual
			Set<OWLNamedIndividual> values = getObjectPropertyB2Individual(individual, p);
			if( ! values.isEmpty())
				out.add( new ObjectPropertyRelatios(individual, p, values, ontoRef));
		}
		return out;	
	}
	/**
	 * Returns all the object properties (with relative values) that are
	 * belonging to an individual.
	 * Note that this implementation may be not efficient since it iterate over all 
	 * the object property of the ontology.
	 * Indeed, it calls {@link #getObjectPropertyB2Individual(OWLNamedIndividual)}.
	 * 
	 * 
	 * @param individual an OWL individual
	 * @return all the object properties, and values, of the given individual
	 */
	public Set< ObjectPropertyRelatios> getObjectPropertyB2Individual( String individualName){
		return getObjectPropertyB2Individual( ontoRef.getOWLIndividual( individualName));	
	}	
	/**
	 * This class is used to contains an object property relation and values (individuals)
	 * associated do a particular individuals.
	 * This class is computed through {@link OWLEnquirer#getObjectPropertyB2Individual(OWLNamedIndividual)}
	 * or {@link OWLEnquirer#getObjectPropertyB2Individual(String)}.
	 */
	public class ObjectPropertyRelatios{
		private OWLObjectProperty prop;
		private OWLNamedIndividual ind;
		private Set< OWLNamedIndividual> value;
		private String propName, indName;
		private Set<String> valueName;
		
		public ObjectPropertyRelatios( OWLNamedIndividual ind, OWLObjectProperty prop, Set<OWLNamedIndividual> value,
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
		
	/** Returns all the data property (with relative values) that are
	 * belonging to an individual.
	 * Note that this implementation may be not efficient since it iterate over all 
	 * the object property of the ontology.
	 * 
	 * @param individual an OWL individual
	 * @return all the object properties, and values, of the given individual
	 */
	public Set< DataPropertyRelatios> getDataPropertyB2Individual( OWLNamedIndividual individual){
		Set< DataPropertyRelatios> out = new HashSet< DataPropertyRelatios>();
		// get all object prop in the ontology
		OWLDataProperty topObjProp = ontoRef.getFactory().getOWLTopDataProperty();
		Set<OWLDataProperty> allProp = getSubDataPropertyOf(topObjProp);
		for( OWLDataProperty p : allProp){ // check if a property belongs to this individual
			Set<OWLLiteral> values = getDataPropertyB2Individual(individual, p);
			if( ! values.isEmpty())
				out.add( new DataPropertyRelatios(individual, p, values, ontoRef));
		}
		return out;	
	}
	/**
	 * Returns all the data property (with relative values) that are
	 * belonging to an individual.
	 * Note that this implementation may be not efficient since it iterate over all 
	 * the object property of the ontology.
	 * Indeed, it calls {@link #getObjectPropertyB2Individual(OWLNamedIndividual)}.
	 * 
	 * @param individual an OWL individual
	 * @return all the object properties, and values, of the given individual
	 */
	public Set< DataPropertyRelatios> getDataPropertyB2Individual( String individualName){
		return getDataPropertyB2Individual( ontoRef.getOWLIndividual( individualName));
	}
	/**
	 * This class is used to contains an data property relation and values (literals)
	 * associated do a particular individuals.
	 * This class is computed through {@link OWLEnquirer#getObjectPropertyB2Individual(OWLNamedIndividual)}
	 * or {@link OWLEnquirer#getObjectPropertyB2Individual(String)}.
	 */
	public class DataPropertyRelatios{
		private OWLDataProperty prop;
		private OWLNamedIndividual ind;
		private Set< OWLLiteral> value;
		private String propName, indName;
		private Set<String> valueName;
		
		public DataPropertyRelatios( OWLNamedIndividual ind, OWLDataProperty prop, Set<OWLLiteral> value, OWLReferencesInterface ontoRef) {
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
		
		public String getProperyName() {
			return propName;
		}
		public String getIndividualName() {
			return indName;
		}
		public Set< String> getValuesName() {
			return valueName;
		}
		public String toString(){
			return "\"" + getIndividualName() + "." + getProperyName() + "( " + getValuesName() + ")"; 
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
			Set<OWLObjectPropertyExpression> infered = ontoRef.getReasoner().getSubObjectProperties(prop, !returnCompleteDescription).getFlattened();
			for( OWLObjectPropertyExpression e : infered)
				out.add( e.asOWLObjectProperty());
		} catch( InconsistentOntologyException e){
			ontoRef.loggInconsistency();
		}
		logger.addDebugString( "get sub classes of given in: " + (System.nanoTime() - initialTime) + " [ns]");
		return( out);
	}
	/**
	 * Returns all the sub object properties of a given property
	 * by reling on the method {@link #getSubObjectPropertyOf(OWLObjectProperty)}. 
	 * It check in the ontology definition frist and then in the 
	 * infered axioms by the reasoner.
	 * Also note that the completeness of the results 
	 * of this methods also depends from the value
	 * of {@link #returnCompleteDescription}.
	 * @param prop an object property
	 * @return the sub object property of the input parameter ({@code prop})
	 */
	public Set<OWLObjectProperty> getSubObjectPropertyOf( String propName){
		OWLObjectProperty prop = ontoRef.getOWLObjectProperty( propName);
		return getSubObjectPropertyOf( prop);
	}
	
	/** Returns all the super object properties of a given property. 
	 * It check in the ontology definition frist and then in the 
	 * infered axioms by the reasoner.
	 * Also note that the completeness of the results 
	 * of this methods also depends from the value
	 * of {@link #returnCompleteDescription}.
	 * @param prop an object property
	 * @return the super object property of the input parameter ({@code prop})
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
			Set<OWLObjectPropertyExpression> infered = ontoRef.getReasoner().getSuperObjectProperties(prop, !returnCompleteDescription).getFlattened();
			for( OWLObjectPropertyExpression e : infered)
				out.add( e.asOWLObjectProperty());
		} catch( InconsistentOntologyException e){
			ontoRef.loggInconsistency();
		}
		logger.addDebugString( "get sub classes of given in: " + (System.nanoTime() - initialTime) + " [ns]");
		return( out);
	}
	/**
	 * Returns all the super object properties of a given property
	 * by reling on the method {@link #getSuperObjectPropertyOf(OWLObjectProperty)}. 
	 * It check in the ontology definition frist and then in the 
	 * infered axioms by the reasoner.
	 * Also note that the completeness of the results 
	 * of this methods also depends from the value
	 * of {@link #returnCompleteDescription}.
	 * @param prop an object property
	 * @return the super object property of the input parameter ({@code prop})
	 */
	public Set<OWLObjectProperty> getSuperObjectPropertyOf( String propName){
		OWLObjectProperty prop = ontoRef.getOWLObjectProperty( propName);
		return getSuperObjectPropertyOf( prop);
	}
		
	/**
	 * Returns all the sub data properties of a given property. 
	 * It check in the ontology definition frist and then in the 
	 * infered axioms by the reasoner.
	 * Also note that the completeness of the results 
	 * of this methods also depends from the value
	 * of {@link #returnCompleteDescription}.
	 * @param prop an data property
	 * @return the sub data property of the input parameter ({@code prop})
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
			Set<OWLDataProperty> infered = ontoRef.getReasoner().getSubDataProperties(prop, !returnCompleteDescription).getFlattened();
			out.addAll( infered);
		} catch( InconsistentOntologyException e){
			ontoRef.loggInconsistency();
		}
		logger.addDebugString( "get sub classes of given in: " + (System.nanoTime() - initialTime) + " [ns]");
		return( out);
	}
	/**
	 * Returns all the sub data properties of a given property
	 * by reling on the method {@link #getSubDataPropertyOf(OWLObjectProperty)}. 
	 * It check in the ontology definition frist and then in the 
	 * infered axioms by the reasoner.
	 * Also note that the completeness of the results 
	 * of this methods also depends from the value
	 * of {@link #returnCompleteDescription}.
	 * @param prop an data property
	 * @return the sub data property of the input parameter ({@code prop})
	 */
	public Set<OWLDataProperty> getSubDataPropertyOf( String propName){
		OWLDataProperty prop = ontoRef.getOWLDataProperty( propName);
		return getSubDataPropertyOf( prop);
	}
	
	/**
	 * Returns all the super data properties of a given property. 
	 * It check in the ontology definition frist and then in the 
	 * infered axioms by the reasoner.
	 * Also note that the completeness of the results 
	 * of this methods also depends from the value
	 * of {@link #returnCompleteDescription}.
	 * @param prop an data property
	 * @return the super data property of the input parameter ({@code prop})
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
	 * Returns all the super data properties of a given property
	 * by reling on the method {@link #getSuperDataPropertyOf(OWLObjectProperty)}. 
	 * It check in the ontology definition frist and then in the 
	 * infered axioms by the reasoner.
	 * Also note that the completeness of the results 
	 * of this methods also depends from the value
	 * of {@link #returnCompleteDescription}.
	 * @param prop an data property
	 * @return the super data property of the input parameter ({@code prop})
	 */
	public Set<OWLDataProperty> getSuperDataPropertyOf( String propName){
		OWLDataProperty prop = ontoRef.getOWLDataProperty( propName);
		return getSuperDataPropertyOf( prop);
	}
	
	
	/**
	 * Returns all the classes that are sub classes of the given parameter.
	 * Here class is defined by name, so this method uses: 
	 * {@link #getOWLClass(String)} to get an OWLClass and than
	 * it calls {@link #getSubClassOf(OWLClass)}
	 * propagating its returning value. 
	 * Finally note that the completeness of the results 
	 * of this methods also depends from the value
	 * of {@link #returnCompleteDescription}.
	 * 
	 * @param className name of the ontological class to find sub classes
	 * @return a not order set of all the sub classes of cl parameter.
	 */
	public Set<OWLClass> getSubClassOf( String className){
		OWLClass cl = ontoRef.getOWLClass( className);
		return( getSubClassOf( cl));
	}
	/**
	 * Returns all the classes that are sub classes of the given class parameter.
	 * It returns null if no sub classes are defined in the ontology.
	 * Finally note that the completeness of the results 
	 * of this methods also depends from the value
	 * of {@link #returnCompleteDescription}.
	 * 
	 * @param cl OWL class to find sub classes
	 * @return a not order set of all the sub-classes of cl parameter.
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
	 * Returns all the classes that are super classes of the given parameter.
	 * Here class is defined by name, so this method uses: 
	 * {@link #getOWLClass(String)} to get an OWLClass and than
	 * it calls {@link #getSuperClassOf(OWLClass)}
	 * propagating its returning value. 
	 * Finally note that the completeness of the results 
	 * of this methods also depends from the value
	 * of {@link #returnCompleteDescription}.
	 * 
	 * @param className name of the ontological class to find super classes
	 * @return a not order set of all the super classes of cl parameter.
	 */
	public Set<OWLClass> getSuperClassOf( String className){
		OWLClass cl = ontoRef.getOWLClass( className);
		return( getSuperClassOf( cl));
	}
	/**
	 * Returns all the classes that are super classes of the given class parameter.
	 * It returns null if no super classes are defined in the ontology.
	 * Finally note that the completeness of the results 
	 * of this methods also depends from the value
	 * of {@link #returnCompleteDescription}.
	 * 
	 * @param cl OWL class to find super classes
	 * @return a not order set of all the super classes of cl parameter.
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
			ResultSet result = qe.execSelect(); // TODO: it can do mutch more.... (ask query, evaluate constants, etc.)
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
	public List< QuerySolution> sparql( String query){ // no time out
		return sparql( query, null);
	}
	public List< QuerySolution> sparql( String prefix, String select, String where, Long timeOut){
		return sparql( prefix + select + where, timeOut);
	}
	public List< QuerySolution> sparql( String prefix, String select, String where){
		return sparql( prefix + select + where);
	}

	public List< Map< String, String>> sparqlMsg(String query, Long timeOut){
		return sparql2Msg( sparql( query, timeOut));
	}
	public List< Map< String, String>> sparqlMsg( String query){ // no time out
		return sparql2Msg( sparql( query, null));
	}
	public List< Map< String, String>> sparqlMsg( String prefix, String select, String where, Long timeOut){
		return sparql2Msg( sparql( prefix + select + where, timeOut));
	}
    public List< Map< String, String>> sparqlMsg( String prefix, String select, String where){
		return sparql2Msg( sparql( prefix + select + where));
	}
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
