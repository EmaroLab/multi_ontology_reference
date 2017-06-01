package it.emarolab.amor.owlInterface;

import com.clarkparsia.pellet.owlapi.PelletReasoner;
import com.clarkparsia.pellet.sparqldl.jena.SparqlDLExecutionFactory;
import it.emarolab.amor.owlDebugger.Logger;
import it.emarolab.amor.owlDebugger.Logger.LoggerFlag;
import it.emarolab.amor.owlInterface.SemanticRestriction.*;
import org.apache.jena.query.*;
import org.apache.jena.rdf.model.InfModel;
import org.apache.jena.rdf.model.ModelFactory;
import org.mindswap.pellet.KnowledgeBase;
import org.mindswap.pellet.jena.PelletInfGraph;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.InconsistentOntologyException;
import org.semanticweb.owlapi.reasoner.Node;
import org.semanticweb.owlapi.search.EntitySearcher;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;


// TODO : make an abstract class interface to be implemented for all methods (all have the same shape)

/**
 * <div style="text-align:center;"><small>
 * <b>Project</b>:    aMOR <br>
 * <b>File</b>:       it.emarolab.amor.owlInterface.OWLEnquirer <br>
 * <b>Licence</b>:    GNU GENERAL PUBLIC LICENSE. Version 3, 29 June 2007 <br>
 * <b>Author</b>:     Buoncompagni Luca (luca.buoncompagni@edu.unige.it) <br>
 * <b>affiliation</b>: DIBRIS, EMAROLab, University of Genoa. <br>
 * <b>date</b>:       Feb 10, 2016 <br>
 * </small></div>
 *
 * <p>
 *     This class defines and implement the interface for querying the ontology.
 * </p>
 *
 * @version 2.1
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
     * If it is true the call ask to the reasoner, otherwise only asserted knowledge will be queried.
     */
    private Boolean returnsCompleteDescription, includesInferences;
    /**
     * Ontology reference to be manipulated given in the constructor.
     */
    private OWLReferencesInterface ontoRef;

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
    protected OWLReferencesInterface getOwlLibrary(){
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
     * Set to {@code true} to get also inferred axioms
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
                Stream<OWLNamedIndividual> streamReasoned = ontoRef.getOWLReasoner()
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
                Stream<OWLClass> streamReasoned = ontoRef.getOWLReasoner()
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

        //Set<OWLLiteral>  value = individual.getDataPropertyValues(property, ontoRef.getOWLOntology());
        Stream<OWLLiteral> stream = EntitySearcher.getDataPropertyValues(individual, property, ontoRef.getOWLOntology());
        Set< OWLLiteral> value = stream.collect( Collectors.toSet());

        if(includesInferences) {
            try {
                Set<OWLLiteral> valueInf = ontoRef.getOWLReasoner().getDataPropertyValues(individual, property);
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
                Stream<OWLNamedIndividual> streamReasoned = ontoRef.getOWLReasoner().getObjectPropertyValues(individual, property).entities();
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
        Set<OWLObjectProperty> allProp = getSubObjectPropertyOf(topObjProp);
        for( OWLObjectProperty p : allProp){ // check if a property belongs to this individual
            Set<OWLNamedIndividual> values = getObjectPropertyB2Individual(individual, p);
            if( ! values.isEmpty())
                out.add( new ObjectPropertyRelations(individual, p, values));
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
        Set<OWLDataProperty> allProp = getSubDataPropertyOf(topObjProp);
        for( OWLDataProperty p : allProp){ // check if a property belongs to this individual
            Set<OWLLiteral> values = getDataPropertyB2Individual(individual, p);
            if( ! values.isEmpty())
                out.add( new DataPropertyRelations(individual, p, values));
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
                Stream<OWLObjectPropertyExpression> streamReasoned = ontoRef.getOWLReasoner()
                        .getSubObjectProperties(prop, ! isReturningCompleteDescription()).entities();
                Set<OWLObjectPropertyExpression> reasoned = streamReasoned.collect(Collectors.toSet());
                if (reasoned != null)
                    out.addAll(reasoned.stream().map(AsOWLObjectProperty::asOWLObjectProperty).collect(Collectors.toList()));
            } catch (InconsistentOntologyException e) {
                ontoRef.logInconsistency();
            }
        }
        out.remove( ontoRef.getOWLFactory().getOWLBottomObjectProperty());
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
                Stream<OWLObjectPropertyExpression> streamReasoned = ontoRef.getOWLReasoner()
                        .getSuperObjectProperties(prop, ! isReturningCompleteDescription()).entities();
                Set<OWLObjectPropertyExpression> reasoned = streamReasoned.collect(Collectors.toSet());
                if (reasoned != null)
                    out.addAll(reasoned.stream().map(AsOWLObjectProperty::asOWLObjectProperty).collect(Collectors.toList()));

            } catch (InconsistentOntologyException e) {
                ontoRef.logInconsistency();
            }
        }
        out.remove( ontoRef.getOWLFactory().getOWLTopObjectProperty());
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
                Stream<OWLDataProperty> streamReasoned = ontoRef.getOWLReasoner()
                        .getSubDataProperties(prop, ! isReturningCompleteDescription()).entities();
                Set<OWLDataProperty> reasoned = streamReasoned.collect(Collectors.toSet());
                if (reasoned != null)
                    out.addAll(reasoned.stream().map(AsOWLDataProperty::asOWLDataProperty).collect(Collectors.toList()));
            } catch (InconsistentOntologyException e) {
                ontoRef.logInconsistency();
            }
        }
        out.remove( ontoRef.getOWLFactory().getOWLBottomDataProperty());
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
                Stream<OWLDataProperty> streamReasoned = ontoRef.getOWLReasoner()
                        .getSuperDataProperties(prop, ! isReturningCompleteDescription()).entities();
                Set<OWLDataProperty> reasoned = streamReasoned.collect(Collectors.toSet());
                if (reasoned != null)
                    out.addAll(reasoned.stream().map(AsOWLDataProperty::asOWLDataProperty).collect(Collectors.toList()));
            } catch (InconsistentOntologyException e) {
                ontoRef.logInconsistency();
            }
        }
        out.remove( ontoRef.getOWLFactory().getOWLTopDataProperty());
        logger.addDebugString( "get sub classes of given in: " + (System.nanoTime() - initialTime) + " [ns]");
        return( out);
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
                Stream<OWLClass> streamReasoned = ontoRef.getOWLReasoner()
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
                Stream<OWLClass> streamReasoned = ontoRef.getOWLReasoner()
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
     * Returns all the restrictions that are defining a given class.
     * @param cl an OWL class.
     * @return non-ordered set of all restrictions that are defining the class.
     */
    public Set<ApplyingRestriction> getRestriction(OWLClass cl){
        try{
            Set< ApplyingRestriction> out = new HashSet<>();
            Stream< OWLClassAxiom> axiomStream = ontoRef.getOWLOntology().axioms( cl);
            for (OWLClassAxiom ax :  (Iterable<OWLClassAxiom>) axiomStream::iterator) {
                Stream<OWLClassExpression> nestedClassStream = ax.nestedClassExpressions();
                for( OWLClassExpression e : (Iterable<OWLClassExpression>) nestedClassStream::iterator) {
                    if ( e.getClassExpressionType() == ClassExpressionType.OBJECT_MIN_CARDINALITY)
                        out.add( new ClassRestrictedOnMinObject( cl, (OWLObjectMinCardinality) e));
                    else if ( e.getClassExpressionType() == ClassExpressionType.OBJECT_MAX_CARDINALITY)
                        out.add( new ClassRestrictedOnMaxObject( cl, (OWLObjectMaxCardinality) e));
                    else if ( e.getClassExpressionType() == ClassExpressionType.OBJECT_EXACT_CARDINALITY)
                        out.add( new ClassRestrictedOnExactObject( cl, (OWLObjectExactCardinality) e));
                    else if ( e.getClassExpressionType() == ClassExpressionType.OBJECT_ALL_VALUES_FROM)
                        out.add( new ClassRestrictedOnAllObject( cl, (OWLObjectAllValuesFrom) e));
                    else if ( e.getClassExpressionType() == ClassExpressionType.OBJECT_SOME_VALUES_FROM)
                        out.add( new ClassRestrictedOnSomeObject( cl, (OWLObjectSomeValuesFrom) e));
                    else if ( e.getClassExpressionType() == ClassExpressionType.DATA_MIN_CARDINALITY)
                        out.add( new ClassRestrictedOnMinData( cl, (OWLDataMinCardinality) e));
                    else if ( e.getClassExpressionType() == ClassExpressionType.DATA_MAX_CARDINALITY)
                        out.add( new ClassRestrictedOnMaxData( cl, (OWLDataMaxCardinality) e));
                    else if ( e.getClassExpressionType() == ClassExpressionType.DATA_EXACT_CARDINALITY)
                        out.add( new ClassRestrictedOnExactData( cl, (OWLDataExactCardinality) e));
                    else if ( e.getClassExpressionType() == ClassExpressionType.DATA_ALL_VALUES_FROM)
                        out.add( new ClassRestrictedOnAllData( cl, (OWLDataAllValuesFrom) e));
                    else if ( e.getClassExpressionType() == ClassExpressionType.DATA_SOME_VALUES_FROM)
                        out.add( new ClassRestrictedOnSomeData( cl, (OWLDataSomeValuesFrom) e));
                    else if ( e.getClassExpressionType() == ClassExpressionType.OWL_CLASS)
                        out.add( new ClassRestrictedOnClass( cl, e.asOWLClass()));
                }
            }

            // reason about other equivalent classes
            if( isIncludingInferences()) {
                Stream<OWLClass> streamReasoned = ontoRef.getOWLReasoner().getEquivalentClasses(cl).entities();
                for (OWLClass a : (Iterable<OWLClass>) streamReasoned::iterator)
                    if ( ! a.isOWLThing())
                        out.add(new ClassRestrictedOnClass(cl, a.asOWLClass()));
            }
            return out;
        } catch( org.semanticweb.owlapi.reasoner.InconsistentOntologyException e){
            ontoRef.logInconsistency();
            return null;
        }
    }

    /**
     * Returns all the restrictions that are defining a data property domain.
     * @param property a data property.
     * @return non-ordered set of all restrictions that are in the given property domain.
     */
    public Set< ApplyingRestriction> getDomainRestriction( OWLDataProperty property){
        try{
            Set< ApplyingRestriction> out = new HashSet<>();
            Stream<OWLDataPropertyDomainAxiom> axiomStream = ontoRef.getOWLOntology().dataPropertyDomainAxioms(property);
            for (OWLDataPropertyDomainAxiom ax :  (Iterable<OWLDataPropertyDomainAxiom>) axiomStream::iterator) {
                Stream<OWLClassExpression> nestedClassStream = ax.getDomain().nestedClassExpressions();
                for( OWLClassExpression e : (Iterable<OWLClassExpression>) nestedClassStream::iterator) {
                    if ( e.getClassExpressionType() == ClassExpressionType.OBJECT_MIN_CARDINALITY)
                        out.add(new DataDomainRestrictedOnMinObject(property, (OWLObjectMinCardinality) e));
                    else if ( e.getClassExpressionType() == ClassExpressionType.OBJECT_MAX_CARDINALITY)
                        out.add(new DataDomainRestrictedOnMaxObject(property, (OWLObjectMaxCardinality) e));
                    else if ( e.getClassExpressionType() == ClassExpressionType.OBJECT_EXACT_CARDINALITY)
                        out.add(new DataDomainRestrictedOnExactObject(property, (OWLObjectExactCardinality) e));
                    else if ( e.getClassExpressionType() == ClassExpressionType.OBJECT_ALL_VALUES_FROM)
                        out.add(new DataDomainRestrictedOnAllObject(property, (OWLObjectAllValuesFrom) e));
                    else if ( e.getClassExpressionType() == ClassExpressionType.OBJECT_SOME_VALUES_FROM)
                        out.add(new DataDomainRestrictedOnSomeObject(property, (OWLObjectSomeValuesFrom) e));
                    else if ( e.getClassExpressionType() == ClassExpressionType.DATA_MIN_CARDINALITY)
                        out.add(new DataDomainRestrictedOnMinData(property, (OWLDataMinCardinality) e));
                    else if ( e.getClassExpressionType() == ClassExpressionType.DATA_MAX_CARDINALITY)
                        out.add(new DataDomainRestrictedOnMaxData(property, (OWLDataMaxCardinality) e));
                    else if ( e.getClassExpressionType() == ClassExpressionType.DATA_EXACT_CARDINALITY)
                        out.add(new DataDomainRestrictedOnExactData(property, (OWLDataExactCardinality) e));
                    else if ( e.getClassExpressionType() == ClassExpressionType.DATA_ALL_VALUES_FROM)
                        out.add(new DataDomainRestrictedOnAllData(property, (OWLDataAllValuesFrom) e));
                    else if ( e.getClassExpressionType() == ClassExpressionType.DATA_SOME_VALUES_FROM)
                        out.add(new DataDomainRestrictedOnSomeData(property, (OWLDataSomeValuesFrom) e));
                    else if ( e.getClassExpressionType() == ClassExpressionType.OWL_CLASS)
                        out.add(new DataDomainRestrictedOnClass(property, e.asOWLClass()));
                }
            }

            // reason about
            if( isIncludingInferences()) {
                Stream<Node<OWLClass>> reasoned = ontoRef.getOWLReasoner().getDataPropertyDomains(property, isReturningCompleteDescription()).nodes(); // add flag!!!!
                for (Node<OWLClass> ax : (Iterable<Node<OWLClass>>) reasoned::iterator)
                    for (OWLClass a : (Iterable<OWLClass>) () -> ax.entities().iterator())
                        if ( ! a.isOWLThing())
                            out.add(new DataDomainRestrictedOnClass(property, a.asOWLClass()));
            }
            return out;
        } catch( org.semanticweb.owlapi.reasoner.InconsistentOntologyException e){
            ontoRef.logInconsistency();
            return null;
        }
    }
    /**
     * Returns all the restrictions that are defining a data property range.
     * @param property a data property.
     * @return non-ordered set of all restrictions that are in the given property range.
     */
    public Set<ApplyingRestriction> getRangeRestriction( OWLDataProperty property){
        try {
            Set< ApplyingRestriction> out = new HashSet<>();
            Stream<OWLDataPropertyRangeAxiom> axiomStream = ontoRef.getOWLOntology().dataPropertyRangeAxioms(property);
            for (OWLDataPropertyRangeAxiom ax :  (Iterable<OWLDataPropertyRangeAxiom>) axiomStream::iterator)
                out.add( new DataRangeRestricted( property, ax.getRange()));

            // owl api does not support reasoning on this

            return out;
        } catch( org.semanticweb.owlapi.reasoner.InconsistentOntologyException e){
            ontoRef.logInconsistency();
            return null;
        }
    }

    /**
     * Returns all the restrictions that are defining an object property domain.
     * @param property an object property.
     * @return non-ordered set of all restrictions that are in the given property domain.
     */
    public Set<ApplyingRestriction> getDomainRestriction( OWLObjectProperty property){
        try{
            Set< ApplyingRestriction> out = new HashSet<>();
            Stream<OWLObjectPropertyDomainAxiom> axiomStream = ontoRef.getOWLOntology().objectPropertyDomainAxioms(property);
            for (OWLObjectPropertyDomainAxiom ax :  (Iterable<OWLObjectPropertyDomainAxiom>) axiomStream::iterator) {
                Stream<OWLClassExpression> nestedClassStream = ax.getDomain().nestedClassExpressions();
                for( OWLClassExpression e : (Iterable<OWLClassExpression>) nestedClassStream::iterator) {
                    if ( e.getClassExpressionType() == ClassExpressionType.OBJECT_MIN_CARDINALITY)
                        out.add(new ObjectDomainRestrictedOnMinObject(property, (OWLObjectMinCardinality) e));
                    else if ( e.getClassExpressionType() == ClassExpressionType.OBJECT_MAX_CARDINALITY)
                        out.add(new ObjectDomainRestrictedOnMaxObject(property, (OWLObjectMaxCardinality) e));
                    else if ( e.getClassExpressionType() == ClassExpressionType.OBJECT_EXACT_CARDINALITY)
                        out.add(new ObjectDomainRestrictedOnExactObject(property, (OWLObjectExactCardinality) e));
                    else if ( e.getClassExpressionType() == ClassExpressionType.OBJECT_ALL_VALUES_FROM)
                        out.add(new ObjectDomainRestrictedOnAllObject(property, (OWLObjectAllValuesFrom) e));
                    else if ( e.getClassExpressionType() == ClassExpressionType.OBJECT_SOME_VALUES_FROM)
                        out.add(new ObjectDomainRestrictedOnSomeObject(property, (OWLObjectSomeValuesFrom) e));
                    else if ( e.getClassExpressionType() == ClassExpressionType.DATA_MIN_CARDINALITY)
                        out.add(new ObjectDomainRestrictedOnMinData(property, (OWLDataMinCardinality) e));
                    else if ( e.getClassExpressionType() == ClassExpressionType.DATA_MAX_CARDINALITY)
                        out.add(new ObjectDomainRestrictedOnMaxData(property, (OWLDataMaxCardinality) e));
                    else if ( e.getClassExpressionType() == ClassExpressionType.DATA_EXACT_CARDINALITY)
                        out.add(new ObjectDomainRestrictedOnExactData(property, (OWLDataExactCardinality) e));
                    else if ( e.getClassExpressionType() == ClassExpressionType.DATA_ALL_VALUES_FROM)
                        out.add(new ObjectDomainRestrictedOnAllData(property, (OWLDataAllValuesFrom) e));
                    else if ( e.getClassExpressionType() == ClassExpressionType.DATA_SOME_VALUES_FROM)
                        out.add(new ObjectDomainRestrictedOnSomeData(property, (OWLDataSomeValuesFrom) e));
                    else if ( e.getClassExpressionType() == ClassExpressionType.OWL_CLASS)
                        out.add(new ObjectDomainRestrictedOnClass(property, e.asOWLClass()));
                }
            }

            // reason about
            if( isIncludingInferences()) {
                Stream<Node<OWLClass>> reasoned = ontoRef.getOWLReasoner().getObjectPropertyDomains(property, isReturningCompleteDescription()).nodes(); // add flag!!!!
                for (Node<OWLClass> ax : (Iterable<Node<OWLClass>>) reasoned::iterator)
                    for (OWLClass a : (Iterable<OWLClass>) () -> ax.entities().iterator())
                        if ( ! a.isOWLThing())
                            out.add(new ObjectDomainRestrictedOnClass(property, a.asOWLClass()));
            }
            return out;
        } catch( org.semanticweb.owlapi.reasoner.InconsistentOntologyException e){
            ontoRef.logInconsistency();
            return null;
        }
    }
    /**
     * Returns all the restrictions that are defining a data property range.
     * @param property a data property.
     * @return non-ordered set of all restrictions that are in the given property range.
     */
    public Set<ApplyingRestriction> getRangeRestriction( OWLObjectProperty property){
        try{
            Set< ApplyingRestriction> out = new HashSet<>();
            Stream<OWLObjectPropertyRangeAxiom> axiomStream = ontoRef.getOWLOntology().objectPropertyRangeAxioms(property);
            for (OWLObjectPropertyRangeAxiom ax :  (Iterable<OWLObjectPropertyRangeAxiom>) axiomStream::iterator) {
                Stream<OWLClassExpression> nestedClassStream = ax.getRange().nestedClassExpressions();
                for( OWLClassExpression e : (Iterable<OWLClassExpression>) nestedClassStream::iterator) {
                    if ( e.getClassExpressionType() == ClassExpressionType.OBJECT_MIN_CARDINALITY)
                        out.add(new ObjectRangeRestrictedOnMinObject(property, (OWLObjectMinCardinality) e));
                    else if ( e.getClassExpressionType() == ClassExpressionType.OBJECT_MAX_CARDINALITY)
                        out.add(new ObjectRangeRestrictedOnMaxObject(property, (OWLObjectMaxCardinality) e));
                    else if ( e.getClassExpressionType() == ClassExpressionType.OBJECT_EXACT_CARDINALITY)
                        out.add(new ObjectRangeRestrictedOnExactObject(property, (OWLObjectExactCardinality) e));
                    else if ( e.getClassExpressionType() == ClassExpressionType.OBJECT_ALL_VALUES_FROM)
                        out.add(new ObjectRangeRestrictedOnAllObject(property, (OWLObjectAllValuesFrom) e));
                    else if ( e.getClassExpressionType() == ClassExpressionType.OBJECT_SOME_VALUES_FROM)
                        out.add(new ObjectRangeRestrictedOnSomeObject(property, (OWLObjectSomeValuesFrom) e));
                    else if ( e.getClassExpressionType() == ClassExpressionType.DATA_MIN_CARDINALITY)
                        out.add(new ObjectRangeRestrictedOnMinData(property, (OWLDataMinCardinality) e));
                    else if ( e.getClassExpressionType() == ClassExpressionType.DATA_MAX_CARDINALITY)
                        out.add(new ObjectRangeRestrictedOnMaxData(property, (OWLDataMaxCardinality) e));
                    else if ( e.getClassExpressionType() == ClassExpressionType.DATA_EXACT_CARDINALITY)
                        out.add(new ObjectRangeRestrictedOnExactData(property, (OWLDataExactCardinality) e));
                    else if ( e.getClassExpressionType() == ClassExpressionType.DATA_ALL_VALUES_FROM)
                        out.add(new ObjectRangeRestrictedOnAllData(property, (OWLDataAllValuesFrom) e));
                    else if ( e.getClassExpressionType() == ClassExpressionType.DATA_SOME_VALUES_FROM)
                        out.add(new ObjectRangeRestrictedOnSomeData(property, (OWLDataSomeValuesFrom) e));
                    else if ( e.getClassExpressionType() == ClassExpressionType.OWL_CLASS)
                        out.add(new ObjectRangeRestrictedOnClass(property, e.asOWLClass()));
                }
            }

            // reason about
            if( isIncludingInferences()) {
                Stream<Node<OWLClass>> reasoned = ontoRef.getOWLReasoner().getObjectPropertyRanges(property, isReturningCompleteDescription()).nodes(); // add flag!!!!
                for (Node<OWLClass> ax : (Iterable<Node<OWLClass>>) reasoned::iterator)
                    for (OWLClass a : (Iterable<OWLClass>) () -> ax.entities().iterator())
                        if ( ! a.isOWLThing())
                            out.add(new ObjectRangeRestrictedOnClass(property, a.asOWLClass()));
            }
            return out;
        } catch( org.semanticweb.owlapi.reasoner.InconsistentOntologyException e){
            getOwlLibrary().logInconsistency();
            return null;
        }
    }

    /**
     * Returns all the disjoint class of the given class given by name.
     *
     * @param className the name of the class to search for disjointed classes.
     * @return all the disjointed classes.
     */
    public Set<OWLClass> getDisjointClasses( String className){
        return getDisjointClasses( ontoRef.getOWLClass( className));
    }
    /**
     * Returns all the disjoint class of the given class.
     *
     * @param cl the OWL class to search for disjointed classes.
     * @return all the disjointed classes.
     */
    public Set<OWLClass> getDisjointClasses( OWLClass cl){
        long initialTime = System.nanoTime();
        Set<OWLClass> classes = new HashSet<>();

        Stream<OWLClassExpression> stream = EntitySearcher.getDisjointClasses( cl, ontoRef.getOWLOntology());
        Set<OWLClassExpression> set = stream.collect( Collectors.toSet());

        if( set != null)
            classes.addAll(set.stream().map(AsOWLClass::asOWLClass).collect(Collectors.toList()));

        if( isIncludingInferences()) {
            try {
                Stream<OWLClass> streamReasoned = ontoRef.getOWLReasoner().getDisjointClasses(cl).entities();
                Set<OWLClass> reasoned = streamReasoned.collect(Collectors.toSet());
                if (reasoned != null)
                    classes.addAll(reasoned.stream().map(AsOWLClass::asOWLClass).collect(Collectors.toList()));
            } catch (InconsistentOntologyException e) {
                ontoRef.logInconsistency();
            }
        }
        classes.remove( ontoRef.getOWLFactory().getOWLThing());
        logger.addDebugString( "get disjoint classes given in: " + (System.nanoTime() - initialTime) + " [ns]");
        return classes;
    }

    /**
     * Returns all the different individuals of the given one, given by name.
     *
     * @param individualName the name of the individual to search for different instances.
     * @return all the disjointed individuals.
     */
    public Set<OWLNamedIndividual> getDisjointIndividuals(String individualName){
        return getDisjointIndividuals( ontoRef.getOWLIndividual( individualName));
    }
    /**
     * Returns all the different individuals of the given one.
     *
     * @param individual the OWL individual to search for different instances.
     * @return all the disjointed individuals.
     */
    public Set<OWLNamedIndividual> getDisjointIndividuals(OWLNamedIndividual individual){
        long initialTime = System.nanoTime();
        Set<OWLNamedIndividual> individuals = new HashSet<>();

        Stream<OWLIndividual> stream = EntitySearcher.getDifferentIndividuals(individual, ontoRef.getOWLOntology());
        Set<OWLIndividual> set = stream.collect(Collectors.toSet());

        if( set != null)
            individuals.addAll(set.stream().map(AsOWLNamedIndividual::asOWLNamedIndividual).collect(Collectors.toList()));

        if( isIncludingInferences()) {
            try {
                Stream<OWLNamedIndividual> streamReasoned = ontoRef.getOWLReasoner().getDifferentIndividuals(individual).entities();
                Set<OWLNamedIndividual> reasoned = streamReasoned.collect(Collectors.toSet());
                if (reasoned != null)
                    individuals.addAll(reasoned.stream().map(AsOWLNamedIndividual::asOWLNamedIndividual).collect(Collectors.toList()));
            } catch (InconsistentOntologyException e) {
                ontoRef.logInconsistency();
            }
        }
        logger.addDebugString( "get disjoint individuals given in: " + (System.nanoTime() - initialTime) + " [ns]");
        return individuals;
    }

    /**
     * Returns all the disjointed data property of the given one, given by name.
     *
     * @param propertyName the name of the data property to search for different data properties.
     * @return all the disjointed data properties.
     */
    public Set<OWLDataProperty> getDisjointDataProperties(String propertyName){
        return getDisjointDataProperties( ontoRef.getOWLDataProperty( propertyName));
    }
    /**
     * Returns all the disjointed data property of the given one.
     *
     * @param property the OWL data property to search for different data properties.
     * @return all the disjointed data properties.
     */
    public Set<OWLDataProperty> getDisjointDataProperties(OWLDataProperty property){
        long initialTime = System.nanoTime();
        Set<OWLDataProperty> properties = new HashSet<>();

        Stream<OWLDataProperty> stream = EntitySearcher.getDisjointProperties(property, ontoRef.getOWLOntology());
        Set<OWLDataProperty> set = stream.collect(Collectors.toSet());

        if( set != null)
            properties.addAll(set.stream().map(AsOWLDataProperty::asOWLDataProperty).collect(Collectors.toList()));

        if( isIncludingInferences()) {
            try {
                Stream<OWLDataProperty> streamReasoned = ontoRef.getOWLReasoner().getDisjointDataProperties(property).entities();
                Set<OWLDataProperty> reasoned = streamReasoned.collect(Collectors.toSet());
                if (reasoned != null)
                    properties.addAll(reasoned.stream().map(AsOWLDataProperty::asOWLDataProperty).collect(Collectors.toList()));
            } catch (InconsistentOntologyException e) {
                ontoRef.logInconsistency();
            }
        }
        logger.addDebugString( "get disjoint data property given in: " + (System.nanoTime() - initialTime) + " [ns]");
        return properties;
    }

    /**
     * Returns all the disjointed object property of the given one, given by name.
     *
     * @param propertyName the name of the object property to search for different object properties.
     * @return all the disjointed object properties.
     */
    public Set<OWLObjectProperty> getDisjointObjectProperties(String propertyName){
        return getDisjointObjectProperties( ontoRef.getOWLObjectProperty( propertyName));
    }
    /**
     * Returns all the disjointed object property of the given one.
     *
     * @param property the OWL object property to search for different object properties.
     * @return all the disjointed object properties.
     */
    public Set<OWLObjectProperty> getDisjointObjectProperties(OWLObjectProperty property){
        long initialTime = System.nanoTime();
        Set<OWLObjectProperty> properties = new HashSet<>();

        Stream<OWLObjectProperty> stream = EntitySearcher.getDisjointProperties(property, ontoRef.getOWLOntology());
        Set<OWLObjectProperty> set = stream.collect(Collectors.toSet());

        if( set != null)
            properties.addAll(set.stream().map(AsOWLObjectProperty::asOWLObjectProperty).collect(Collectors.toList()));

        if( isIncludingInferences()) {
            try {
                Stream<OWLObjectPropertyExpression> streamReasoned = ontoRef.getOWLReasoner().getDisjointObjectProperties(property).entities();
                Set<OWLObjectPropertyExpression> reasoned = streamReasoned.collect(Collectors.toSet());
                if (reasoned != null)
                    properties.addAll(reasoned.stream().map(AsOWLObjectProperty::asOWLObjectProperty).collect(Collectors.toList()));
            } catch (InconsistentOntologyException e) {
                ontoRef.logInconsistency();
            }
        }
        logger.addDebugString( "get disjoint object property given in: " + (System.nanoTime() - initialTime) + " [ns]");
        return properties;
    }


    /**
     * Returns all the equivalent individuals of the given one, given by name.
     *
     * @param individualName the name of the individual to search for equivalent instances.
     * @return all the equivalent individuals.
     */
    public Set<OWLNamedIndividual> getEquivalentIndividuals(String individualName){
        return getEquivalentIndividuals( ontoRef.getOWLIndividual( individualName));
    }
    /**
     * Returns all the equivalent individuals of the given one.
     *
     * @param individual the OWL individual to search for equivalent instances.
     * @return all the equivalent individuals.
     */
    public Set<OWLNamedIndividual> getEquivalentIndividuals(OWLNamedIndividual individual){
        long initialTime = System.nanoTime();
        Set<OWLNamedIndividual> individuals = new HashSet<>();

        Stream<OWLIndividual> stream = EntitySearcher.getSameIndividuals(individual, ontoRef.getOWLOntology());
        Set<OWLIndividual> set = stream.collect(Collectors.toSet());

        if( set != null)
            individuals.addAll(set.stream().map(AsOWLNamedIndividual::asOWLNamedIndividual).collect(Collectors.toList()));

        if( isIncludingInferences()) {
            try {
                Stream<OWLNamedIndividual> streamReasoned = ontoRef.getOWLReasoner().getSameIndividuals(individual).entities();
                Set<OWLNamedIndividual> reasoned = streamReasoned.collect(Collectors.toSet());
                if (reasoned != null)
                    individuals.addAll(reasoned.stream().map(AsOWLNamedIndividual::asOWLNamedIndividual).collect(Collectors.toList()));
            } catch (InconsistentOntologyException e) {
                ontoRef.logInconsistency();
            }
        }
        logger.addDebugString( "get equivalent individuals given in: " + (System.nanoTime() - initialTime) + " [ns]");
        return individuals;
    }

    /**
     * Returns all the equivalent class of the given class given by name.
     *
     * @param className the name of the class to search for equivalent classes.
     * @return all the equivalent classes.
     */
    public Set<OWLClass> getEquivalentClasses( String className){
        return getEquivalentClasses( ontoRef.getOWLClass( className));
    }
    /**
     * Returns all the equivalent class of the given class.
     *
     * @param cl the OWL class to search for equivalent classes.
     * @return all the equivalent classes.
     */
    public Set<OWLClass> getEquivalentClasses(OWLClass cl){
        long initialTime = System.nanoTime();
        Set<OWLClass> classes = new HashSet<>();

        Stream<OWLClassExpression> stream = EntitySearcher.getEquivalentClasses( cl, ontoRef.getOWLOntology());
        //Set<OWLClassExpression> set = stream.collect( Collectors.toSet());

        //if( set != null)
            stream.forEach( (c) -> {
                if (c.isOWLClass())
                    classes.add(c.asOWLClass());
            });
        //classes.addAll(set.stream().map(AsOWLClass::asOWLClass).collect(Collectors.toList()));

        if( isIncludingInferences()) {
            try {
                Stream<OWLClass> streamReasoned = ontoRef.getOWLReasoner().getEquivalentClasses(cl).entities();
                Set<OWLClass> reasoned = streamReasoned.collect(Collectors.toSet());
                if (reasoned != null)
                    classes.addAll(reasoned.stream().map(AsOWLClass::asOWLClass).collect(Collectors.toList()));
            } catch (InconsistentOntologyException e) {
                ontoRef.logInconsistency();
            }
        }
        classes.remove( ontoRef.getOWLFactory().getOWLThing());
        logger.addDebugString( "get equivalent classes given in: " + (System.nanoTime() - initialTime) + " [ns]");
        return classes;
    }

    /**
     * Returns all the equivalent data property of the given one, given by name.
     *
     * @param propertyName the name of the data property to search for different data properties.
     * @return all the equivalent data properties.
     */
    public Set<OWLDataProperty> getEquivalentDataProperties(String propertyName){
        return getEquivalentDataProperties( ontoRef.getOWLDataProperty( propertyName));
    }
    /**
     * Returns all the equivalent data property of the given one.
     *
     * @param property the OWL data property to search for different data properties.
     * @return all the equivalent data properties.
     */
    public Set<OWLDataProperty> getEquivalentDataProperties(OWLDataProperty property){
        long initialTime = System.nanoTime();
        Set<OWLDataProperty> properties = new HashSet<>();

        Stream<OWLDataPropertyExpression> stream = EntitySearcher.getEquivalentProperties(property, ontoRef.getOWLOntology());
        Set<OWLDataPropertyExpression> set = stream.collect(Collectors.toSet());

        if( set != null)
            properties.addAll(set.stream().map(AsOWLDataProperty::asOWLDataProperty).collect(Collectors.toList()));

        if( isIncludingInferences()) {
            try {
                Stream<OWLDataProperty> streamReasoned = ontoRef.getOWLReasoner().getEquivalentDataProperties(property).entities();
                Set<OWLDataProperty> reasoned = streamReasoned.collect(Collectors.toSet());
                if (reasoned != null)
                    properties.addAll(reasoned.stream().map(AsOWLDataProperty::asOWLDataProperty).collect(Collectors.toList()));
            } catch (InconsistentOntologyException e) {
                ontoRef.logInconsistency();
            }
        }
        logger.addDebugString( "get equivalent data property given in: " + (System.nanoTime() - initialTime) + " [ns]");
        return properties;
    }

    /**
     * Returns all the equivalent object property of the given one, given by name.
     *
     * @param propertyName the name of the object property to search for different object properties.
     * @return all the equivalent object properties.
     */
    public Set<OWLObjectProperty> getEquivalentObjectProperties(String propertyName){
        return getEquivalentObjectProperties( ontoRef.getOWLObjectProperty( propertyName));
    }
    /**
     * Returns all the equivalent object property of the given one.
     *
     * @param property the OWL object property to search for different object properties.
     * @return all the equivalent object properties.
     */
    public Set<OWLObjectProperty> getEquivalentObjectProperties(OWLObjectProperty property){
        long initialTime = System.nanoTime();
        Set<OWLObjectProperty> properties = new HashSet<>();

        Stream<OWLObjectPropertyExpression> stream = EntitySearcher.getEquivalentProperties(property, ontoRef.getOWLOntology());
        Set<OWLObjectPropertyExpression> set = stream.collect(Collectors.toSet());

        if( set != null)
            properties.addAll(set.stream().map(AsOWLObjectProperty::asOWLObjectProperty).collect(Collectors.toList()));

        if( isIncludingInferences()) {
            try {
                Stream<OWLObjectPropertyExpression> streamReasoned = ontoRef.getOWLReasoner().getEquivalentObjectProperties(property).entities();
                Set<OWLObjectPropertyExpression> reasoned = streamReasoned.collect(Collectors.toSet());
                if (reasoned != null)
                    properties.addAll(reasoned.stream().map(AsOWLObjectProperty::asOWLObjectProperty).collect(Collectors.toList()));
            } catch (InconsistentOntologyException e) {
                ontoRef.logInconsistency();
            }
        }
        logger.addDebugString( "get equivalent object property given in: " + (System.nanoTime() - initialTime) + " [ns]");
        return properties;
    }

    /**
     * Returns all the inverse object properties of a given property.
     * @param propertyName the name of the property from which retrieve its inverses.
     * @return the inverse properties of the given property.
     */
    public Set<OWLObjectProperty> getInverseProperty(String propertyName) {
        return getInverseProperty(ontoRef.getOWLObjectProperty(propertyName));
    }
    /**
     * Returns all the inverse object properties of a given property.
     * @param property the property from which retrieve its inverses.
     * @return the inverse properties of the given property.
     */
    public Set<OWLObjectProperty> getInverseProperty(OWLObjectProperty property) {
        final Set<OWLObjectProperty> prInverse = new HashSet<>();
        Stream<OWLInverseObjectPropertiesAxiom> st = ontoRef.getOWLOntology().inverseObjectPropertyAxioms(property);
        st.forEach((e) -> {
            if ( e.getSecondProperty().equals( property))
                prInverse.add( e.getFirstProperty().asOWLObjectProperty());
            else prInverse.add( e.getSecondProperty().asOWLObjectProperty());
        });

        if (includesInferences) {
            Stream<OWLObjectPropertyExpression> reasoned = ontoRef.getOWLReasoner().getInverseObjectProperties(property).entities();
            reasoned.forEach(e -> prInverse.add(e.asOWLObjectProperty()));
        }
        return prInverse;
    }

    /**
     * Returns an inverse object property of a given property.
     * If no inverse property are fount it returns the given property.
     *
     * @param propertyName the name of the property from which retrieve an inverse.
     * @return an inverse property of the given property.
     */
    public OWLObjectProperty getOnlyInverseProperty(String propertyName) {
        return getOnlyInverseProperty(ontoRef.getOWLObjectProperty(propertyName));
    }
    /**
     * Returns an inverse object property of a given property.
     * If no inverse property are fount it returns the given property.
     *
     * @param property the property from which retrieve an inverse.
     * @return an inverse property of the given property.
     */
    public OWLObjectProperty getOnlyInverseProperty(OWLObjectProperty property) {
        return ((OWLObjectProperty) ontoRef.getOnlyElement(getInverseProperty(property)));
    }

    /**
     * Returns only the classes, in which the given individual (by name) is classified,
     * that are leafs in the class tree.
     * @param individualName the name of the individual for which find the bottom types.
     * @return the classes in which the individual in classified that are a bottom type in the class hierarchy.
     * It returns an empty set if no such a classes are found.
     */
    public Set<OWLClass> getBottomType(String individualName){
        return getBottomType( ontoRef.getOWLIndividual( individualName));
    }
    /**
     * Returns only the classes, in which the given individual is classified,
     * that are leafs in the class tree.
     * @param individual the individual for which find the bottom types.
     * @return the classes in which the individual in classified that are a bottom type in the class hierarchy.
     * It returns an empty set if no such a classes are found.
     */
    public Set<OWLClass> getBottomType(OWLNamedIndividual individual){
        Set<OWLClass> types = getIndividualClasses(individual);
        Set<OWLClass> out = new HashSet<>();
        for ( OWLClass cl : types) {
            Set<OWLClass> subCl = getSubClassOf(cl);
            if ( subCl.isEmpty())
                out.add( cl);
            if ( subCl.size() == 1)
                for ( OWLClass sub : subCl)
                    if ( sub.isOWLNothing())
                        out.add( cl);
        }
        return out;
    }

    /**
     * Returns only one class, in which the given individual (by name) is classified,
     * that is a leaf in the class tree.
     * @param individualName the name of the individual for which find a bottom type.
     * @return a class in which the individual in classified that is a bottom type in the class hierarchy.
     * It returns {@code null} if no such a classes are found.
     */
    public OWLClass getOnlyBottomType(String individualName){
        return getOnlyBottomType( ontoRef.getOWLIndividual( individualName));
    }
    /**
     * Returns only one class, in which the given individual is classified,
     * that is a leaf in the class tree.
     * @param individual the individual for which find a bottom type.
     * @return a class in which the individual in classified that is a bottom type in the class hierarchy.
     * It returns {@code null} if no such a classes are found.
     */
    public OWLClass getOnlyBottomType(OWLNamedIndividual individual){
        Set<OWLClass> types = getIndividualClasses(individual);
        for ( OWLClass cl : types) {
            Set<OWLClass> subCl = getSubClassOf(cl);
            if ( subCl.isEmpty())
                return cl;
            if ( subCl.size() == 1)
                for ( OWLClass sub : subCl)
                    if ( sub.isOWLNothing())
                        return cl;
        }
        return null;
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
            KnowledgeBase kb = ((PelletReasoner) ontoRef.getOWLReasoner()).getKB();
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

}
