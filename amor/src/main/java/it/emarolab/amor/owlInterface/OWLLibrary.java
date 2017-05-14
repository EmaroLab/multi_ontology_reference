package it.emarolab.amor.owlInterface;

import it.emarolab.amor.owlDebugger.Logger;
import it.emarolab.amor.owlDebugger.Logger.LoggerFlag;
import it.emarolab.amor.owlDebugger.ReasonerMonitor;
import it.emarolab.amor.owlInterface.OWLReferencesInterface.OWLReferencesContainer;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerConfiguration;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import org.semanticweb.owlapi.reasoner.SimpleConfiguration;
import org.semanticweb.owlapi.util.SimpleIRIMapper;
import org.semanticweb.owlapi.vocab.OWL2Datatype;

//import org.semanticweb.owlapi.vocab.PrefixOWLOntologyFormat;

/**
 * <div style="text-align:center;"><small>
 * <b>Project</b>:    aMOR <br>
 * <b>File</b>:       it.emarolab.amor.owlInterface.OWLLibrary <br>
 * <b>Licence</b>:    GNU GENERAL PUBLIC LICENSE. Version 3, 29 June 2007 <br>
 * <b>Author</b>:     Buoncompagni Luca (luca.buoncompagni@edu.unige.it) <br>
 * <b>affiliation</b>: DIBRIS, EMAROLab, University of Genoa. <br>
 * <b>date</b>:       Feb 10, 2016 <br>
 * </small></div>
 *
 * <p>
 * This class is the lowest interface level with the OWL API.
 * It contains all the basic structures to manipulate ontologies
 * but does not provide any initialization procedure.<br>
 * Initialization of this class should be done by calling (in this order):
 * {@link #setIriFilePath(IRI)}, {@link #setIriOntologyPath(IRI)}, {@link #setManager()},
 * {@link #createOntology()} or {@link #loadOntologyFromFile()}, {@link #setFactory()},
 * {@link #setPrefixFormat()} and {@link #setOWLReasoner(String, boolean, String)}.<br>
 * WARNING: It is recommended not to interact directly twith this class,
 * use {@link OWLReferences} instead.<br>
 * </p>
 *
 * @version 2.0
 */

public class OWLLibrary {

    // [[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[   CONSTANTS   ]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]
    /**
     * Full qualifier of the Pellet reasoner Factory. String to be called by
     * Java reflection to instantiate the Pellet Reasoner.
     * In particular it is: {@value #REASONER_QUALIFIER_PELLET}.
     */
    public static final String REASONER_QUALIFIER_PELLET = "com.clarkparsia.pellet.owlapi.PelletReasonerFactory"; // "com.clarkparsia.pellet.owlapiv3.PelletReasonerFactory";
    /**
     * Full qualifier of the Snorocket reasoner Factory. String to be called by
     * Java reflection to instantiate the Snorocket Reasoner.
     * In particular it is: {@value #REASONER_QUALIFIER_SNOROCKET}.
     */
    public static final String REASONER_QUALIFIER_SNOROCKET = "au.csiro.snorocket.owlapi3.SnorocketReasonerFactory";
    /**
     * Full qualifier of the HERMIT reasoner Factory. String to be called by
     * Java reflection to instantiate the HERMIT Reasoner.
     * In particular it is: {@value #REASONER_QUALIFIER_HERMIT}.
     */
    public static final String REASONER_QUALIFIER_HERMIT = "org.semanticweb.HermiT.ReasonerFactory"; // "org.semanticweb.HermiT.Reasoner$ReasonerFactory";
    /**
     * Full qualifier of the FACT reasoner Factory. String to be called by
     * Java reflection to instantiate the FACT Reasoner.
     * In particular it is: {@value #REASONER_QUALIFIER_FACT}.
     */
    public static final String REASONER_QUALIFIER_FACT = "uk.ac.manchester.cs.factplusplus.owlapiv3.FaCTPlusPlusReasonerFactory";

    /**
     * Generic identifier for the default reasoner {@value #REASONER_DEFAULT}.
     */
    public static final String REASONER_DEFAULT = REASONER_QUALIFIER_PELLET; //"aMOR.default.reasoner";

    // [[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[   PRIVATE CLASS FIELDS   ]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]
    /**
     * This object is used to log information about the OWL references managed by this container class.
     * Activated by setting the flag: {@link LoggerFlag#LOG_OWL_LIBRARY}
     */
    private static Logger logger = new Logger( OWLReferencesContainer.class, LoggerFlag.getLogOwlLibrary());

    /**
     * This is the OWL ontology Manager (base API object) to be used in an OWL Reference.
     */
    private OWLOntologyManager manager;
    /**
     * This is the OWL ontology Data Factory (base API object) to be used in an OWL Reference.
     */
    private OWLDataFactory factory;
    /**
     * This is the OWL Ontology (base API object) to be used in an OWL Reference.
     */
    private OWLOntology ontology;

    // This is the OWL ontology Prefix Format (base API object) to be used in an OWL References
    //private PrefixOWLOntologyFormat pm; DEPRECATED WITH OWL API 5
    private String prefix;

    /**
     * This is the OWL Reasoner (base API object) used by the ontology referenced by an OWLReference.
     */
    private OWLReasoner reasoner;
    /**
     * This is the IRI file path. It points to a local directory, if the References are created or loaded from file,
     * or the URL path, if the References is load from WEB.
     */
    private IRI iriFilePath;
    /**
     * This is the semantic IRI path associated to the Ontology.
     */
    private IRI iriOntologyPath;

    // [[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[   CONSTRUCTOR   ]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]
    /**
     * This constructor force this class to be used only inside the {@code aMor.owlInterface} package.
     * This object is internally used to manage {@link OWLReferences} and should not be used for other purposes.
     * If you want to interact with the ontology use the {@link OWLReferences} instead.<br>
     * This method does not provide any initialisation procedure.
     * Please note that before you use any other methods of this class, you must call {@link #setIriFilePath(IRI)}
     * and {@link #setIriOntologyPath(IRI)} methods.
     * For a complete initialisation of this object see the methods listed in the {@literal seeAlso} section.
     * @see #setIriFilePath(IRI)
     * @see #setIriOntologyPath(IRI)
     * @see #setManager()
     * @see #createOntology()
     * @see #setFactory()
     * @see #setPrefixFormat()
     * @see #setOWLReasoner(String, boolean, String)
     */
    protected OWLLibrary() {}


    // [[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[   FIELD SETTERS   ]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]
    // those two must be called before doing anything else !!!!!!!!!.

    /**
     * Creates a new OWL Ontology Manager and sets it to the relative field (see {@link #getOWLManager()}).<br>
     * This method requires that the following values are not {@code null}:
     * {@link #getIriFilePath()} and {@link #getIriOntologyPath()}.<br>
     */
    protected synchronized void setManager(){
        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
        IRI iriOnto = getIriOntologyPath();
        IRI iriFile = getIriFilePath();
        if( iriOnto != null){
            if( iriFile != null){
                SimpleIRIMapper mapper = new SimpleIRIMapper( iriOnto, iriFile);
                manager.getIRIMappers().add(mapper); // load from file
                this.manager = manager;
                logger.addDebugString( "Ontology OWL manager created for References: " + this);
                return;
            }
        }
        logger.addDebugString( "Cannot create ontology manager for References: " + this, true);
        this.manager = null;
    }

    /**
     * Creates and sets a new empty ontology in accordance with {@link OWLReferencesInterface#getIriOntologyPath()}.<br>
     * It sets the field to {@code null} if the ontology path associated to the parameter is not correct.
     * Also, This method requires that the values:
     * {@link #getIriOntologyPath()} and {@link #getOWLManager()} are not {@code null} nor badly formatted.
     * If this happens, the ontology object associated to that class instance will be {@code null} too.
     */
    protected synchronized void createOntology(){
        try {
            IRI iri = this.getIriOntologyPath();
            if( iri != null){
                OWLOntology out = this.getOWLManager().createOntology( iri);
                logger.addDebugString( "New ontology created for reference: " + this);
                this.ontology = out;
                return;
            }else logger.addDebugString( "Cannot create new ontology for Reference: " + this, true);
        } catch (OWLOntologyCreationException e) {
            e.printStackTrace();
            logger.addDebugString( "Cannot create new ontology for References: " + this, true);
        }
        this.ontology = null;
    }

    // [[[[[[[[[[[[[[[[[[[[[[[[   FIELD SETTERS (CLASS INITIALIZERS)   ]]]]]]]]]]]]]]]]]]]]]]]]]]
    // those methods use tha bove set fields to create and initialise References to an ontology.

    /**
     * It loads an ontology from file in accord with the given parameter.<br>
     * Ontology IRI path: {@link #getIriOntologyPath()} must not be {@code null} nor badly formatted.
     * If this happen, the ontology field will be set to {@code null}.
     */
    protected synchronized void loadOntologyFromFile(){
        try{
            IRI iri = this.getIriOntologyPath();
            if( iri != null){
                OWLOntology out = this.getOWLManager().loadOntology( iri);
                logger.addDebugString( "Ontology loaded from file  for References: " + this);
                this.ontology = out;
                return;
            }else logger.addDebugString( "Cannot load ontology from file for the References: " + this, true);
        } catch(  OWLOntologyCreationException e){
            e.printStackTrace();
            logger.addDebugString( "Cannot load ontology from file for the References: " + this, true);
        }
        this.ontology = null;
    }

    /**
     * It loads an ontology from WEB in accordance with the given parameter.<br>
     * Ontology IRI path: {@link #getIriOntologyPath()} must not be {@code null} nor badly formatted.
     * Returns null if these conditions are not met.
     */
    protected synchronized void loadOntologyFromWeb(){
        try{
            IRI iri = this.getIriFilePath();//.getIriOntologyPath();
            if( iri != null){
                OWLOntology out = this.getOWLManager().loadOntologyFromOntologyDocument( iri);
                logger.addDebugString( "Ontology loaded from web for References: " + this);
                this.ontology = out;
                return;
            }else logger.addDebugString( "Cannot load ontology from web for the References: " + this, true);
        } catch(  OWLOntologyCreationException e){
            e.printStackTrace();
            logger.addDebugString( "Cannot load ontology from web for the References: " + this, true);
        }
        this.ontology = null;
    }

    /**
     * Creates and sets the OWLDataFactory.<br>
     * It requires that the {@link OWLReferencesInterface#getOWLManager()} is not {@code null}.
     */
    protected synchronized void setFactory(){
        OWLDataFactory out = this.getOWLManager().getOWLDataFactory();
        logger.addDebugString( "Create a OWL Data Factory for References: " + this);
        this.factory = out;
    }

    /**
     * Creates and sets a prefix manager to be attached to the given ontology references,
     * in order to simplify IRI definitions and usage.<br>
     * Fields: {@link #getOWLManager()}, {@link #getOWLOntology()}
     * and {@link #getIriOntologyPath()}, must not be {@code null} nor badly formatted.
     */
    protected synchronized void setPrefixFormat() {
        /*PrefixOWLOntologyFormat pm = (PrefixOWLOntologyFormat) this.getOWLManager().getOntologyFormat( this.getOWLOntology());
        pm.setDefaultPrefix( this.getIriOntologyPath() + "#");
        logger.addDebugString( "Create a new prefix manager for References: " + this);
        this.pm = pm;*/

        this.prefix = this.getIriOntologyPath() + "#";
        logger.addDebugString( "Create a new prefix manager for References: " + this);
    }

    /**
     * It creates and sets a Reasoner instance.
     * The type of the reasoner is defined by the reasoner name factory, which could be:
     * {@link #REASONER_QUALIFIER_PELLET}, {@link #REASONER_QUALIFIER_SNOROCKET},
     * {@link #REASONER_QUALIFIER_HERMIT} or {@link #REASONER_QUALIFIER_FACT}.<br>
     * If the buffering flag is {@code true} then the reasoner will update its
     * state only if {@link OWLReferencesInterface#synchronizeReasoner()} is called.
     * Otherwise, the reasoner will synchronise itself after every change in the ontology.
     * The system will return {@code null} if a java reflection error occurs while instancing the
     * class defined by the parameter {@code reasonerFactoryName}.
     * Field {@link #getOWLOntology()} has not to be {@code null}.
     * @param reasonerFactoryName full java qualifier of the reasoner factory class to be initialised.
     * @param buffering if {@code true} the reasoner buffers changes.
     * @param loggingName for debugging. Evocative name given to the {@link ReasonerMonitor} assigned to this reasoner.
     */
    protected synchronized void setOWLReasoner(String reasonerFactoryName, boolean buffering, String loggingName){
        long initialTime = System.nanoTime();
        try {
            OWLReasonerFactory reasonerFactory = (OWLReasonerFactory) Class.forName(reasonerFactoryName).newInstance();
            OWLReasoner reasoner;
            ReasonerMonitor progressMonitor = new ReasonerMonitor();
            progressMonitor.setReasonerName( loggingName);//reasonerFactoryName.substring(
            //reasonerFactoryName.lastIndexOf(".") + 1 ).replace( "ReasonerFactory", ""));
            OWLReasonerConfiguration config = new SimpleConfiguration( progressMonitor);
            if( buffering){
                reasoner = reasonerFactory.createReasoner( this.getOWLOntology(), config);
            }else{
                reasoner = reasonerFactory.createNonBufferingReasoner( this.getOWLOntology(), config);
                try{
                    this.getOWLManager().addOntologyChangeListener( (OWLOntologyChangeListener) reasoner );
                } catch( Exception e){
                    e.printStackTrace();
                    logger.addDebugString( "Impossible add ontology change listener for non buffering reasoner", true);
                }
            }
            logger.addDebugString( "Reasoner (" + reasonerFactoryName + ") created in: " + (System.nanoTime() - initialTime) + " [ns] for the References: " + this);
            this.reasoner = reasoner;
            return;
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        logger.addDebugString( "Error on creating Reasoner (" + reasonerFactoryName + ") for the References: " + this , true);
        this.reasoner = null;
    }

    /**
     * Creates and sets an instance of the Pellet reasoner by calling
     * {@link #setOWLReasoner(String, boolean, String)} with input parameter set to:
     * {@link #REASONER_QUALIFIER_PELLET}, {@code buffering} and {@code loggingName} respectively.
     * @param buffering if {@code true} the reasoner must be synchronized manually. Else, sync is automatic.
     * @param loggingName for debugging. Evocative name given to the {@link ReasonerMonitor} assigned to this reasoner.
     */
    protected synchronized void setPelletReasoner( boolean buffering, String loggingName){
        setOWLReasoner( REASONER_QUALIFIER_PELLET, buffering, loggingName);
    }

    /**
     * Creates and sets an instance of the Snorocket reasoner by calling
     * {@link #setOWLReasoner(String, boolean, String)} with input parameter set to:
     * {@link #REASONER_QUALIFIER_SNOROCKET}, {@code buffering} and {@code loggingName} respectively.
     * @param buffering if {@code true} the reasoner must be synchronized manually. Else, sync is automatic.
     * @param loggingName for debugging. Evocative name given to the {@link ReasonerMonitor} assigned to this reasoner.
     */
    protected synchronized void setSnorocketReasoner(boolean buffering, String loggingName){
        setOWLReasoner( REASONER_QUALIFIER_SNOROCKET, buffering, loggingName);
    }
    // methods to create the reasoner from java reflection

    /**
     * Creates and sets an instance of the Hermit reasoner by calling
     * {@link #setOWLReasoner(String, boolean, String)} with input parameter set to:
     * {@link #REASONER_QUALIFIER_HERMIT}, {@code buffering} and {@code loggingName} respectively.
     * @param buffering if {@code true} the reasoner must be synchronized manually. Else, sync is automatic.
     * @param loggingName for debugging. Evocative name given to the {@link ReasonerMonitor} assigned to this reasoner.
     */
    protected synchronized void setHermitReasoner(boolean buffering, String loggingName){
        setOWLReasoner( REASONER_QUALIFIER_HERMIT, buffering, loggingName);
    }

    /**
     * Creates and sets an instance of the Fact reasoner by calling
     * {@link #setOWLReasoner(String, boolean, String)} with input parameter set to:
     * {@link #REASONER_QUALIFIER_FACT}, {@code buffering} and {@code loggingName} respectively.
     * @param buffering if {@code true} the reasoner must be synchronized manually. Else, sync is automatic.
     * @param loggingName for debugging. Evocative name given to the {@link ReasonerMonitor} assigned to this reasoner.
     */
    protected synchronized void setFactReasoner(boolean buffering, String loggingName){
        setOWLReasoner( REASONER_QUALIFIER_FACT, buffering, loggingName);
    }

    /**
     * @return the OWL Ontology Manager associated to this reference instance.
     */
    public synchronized OWLOntologyManager getOWLManager() {
        return manager;
    }

    /**
     * @return the OWL Data Factory associated to this reference instance.
     */
    public synchronized OWLDataFactory getOWLFactory() {
        return factory;
    }

    /**
     * @return the OWL Ontology associated to this reference instance.
     */
    public synchronized OWLOntology getOWLOntology() {
        return ontology;
    }


    // [[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[   FIELD GETTERS   ]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]

    //@return the OWL Ontology prefix which depends from the ontology IRI.
    //This is initialised using OWL API during constructor.
    public synchronized String getPrefix() {
        return prefix;
    }

    public synchronized IRI getPrefixFormat( String name) {
        return IRI.create( prefix + name);
    }

    /**
     * @return the OWL reasoner.
     */
    public synchronized OWLReasoner getOWLReasoner() {
        return reasoner;
    }

    /**
     * Set the reasoner that should be correctly initialised.
     *
     * @param reasoner the instance to set.
     */
    protected void setOWLReasoner(OWLReasoner reasoner) {
        this.reasoner = reasoner;
    }

    /**
     * @return the file path (or URL) formatted as an IRI address.
     */
    public synchronized IRI getIriFilePath() {
        return iriFilePath;
    }

    /**
     * @param iriFilePath the ontology IRI path.
     *                    IRI is supposed to be set before using other methods of this class.
     */
    protected synchronized void setIriFilePath(IRI iriFilePath) {
        this.iriFilePath = iriFilePath;
    }

    /**
     * @return the ontology semantic path formatted as an IRI address.
     */
    public synchronized IRI getIriOntologyPath() {
        return iriOntologyPath;
    }

    /**
     * @param iriOntologyPath the semantic IRI path of the ontology.
     *                        semantic IRI is supposed to be set before using other methods of this class.
     */
    protected synchronized void setIriOntologyPath(IRI iriOntologyPath) {
        this.iriOntologyPath = iriOntologyPath;
    }


    // [[[[[[[[[[[[[[[[[[[[[[[[[[[[[   BASIC OWL OBJECT GETTERS   ]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]

    /**
     * Returns an Object which represents an ontological class
     * with a given name and specific IRI paths. If the entity
     * already exists in the ontology, then the object will refer to it,
     * otherwise the method will create a new ontological entity
     * (not automatically added to the ontology).
     * @param className name of the ontological class.
     * @return the OWL class with the given name and IRI paths, in accordance with the OWLReference.
     */
    public OWLClass getOWLClass( String className) {
        long initialTime = System.nanoTime();

        //OWLClass classObj = this.getOWLFactory().getOWLClass(className, this.pm);
        OWLClass classObj = this.getOWLFactory().getOWLClass( getPrefixFormat( className));

        logger.addDebugString( "OWLClass given in: " + (System.nanoTime() - initialTime) + " [ns]");
        return (classObj);
    }
    /**
     * Returns an Object which represents an ontological individual
     * with a given name and specific IRI paths.
     * If the entity already exists in the ontology, then the object will refer to it,
     * otherwise the method will create a new ontological entity
     * (not automatically added to the ontology).
     * @param individualName name of the ontological individual.
     * @return the OWL individual with the given name and IRI paths, in accordance to the OWLReference.
     */
    public OWLNamedIndividual getOWLIndividual( String individualName){
        long initialTime = System.nanoTime();

        //OWLNamedIndividual individualObj = this.getOWLFactory().getOWLNamedIndividual( ":" + individualName, this.pm);
        OWLNamedIndividual individualObj = this.getOWLFactory().getOWLNamedIndividual( getPrefixFormat( individualName));

        logger.addDebugString( "OWLNamedIndividual given in: " + (System.nanoTime() - initialTime) + " [ns]");
        return (individualObj);
    }
    /**
     * Returns an Object which represents an ontological data property
     * with a given name and specific IRI paths. If the entity
     * already exists in the ontology then the object will refer to it,
     * otherwise the method will create a new ontological entity
     * (not automatically added to the ontology).
     * @param dataPropertyName  name of the ontological data property.
     * @return the OWL data property with the given name and IRI paths, in accordance with the OWLReference
     */
    public OWLDataProperty getOWLDataProperty(String dataPropertyName) {
        long initialTime = System.nanoTime();

        //OWLDataProperty property = this.getOWLFactory().getOWLDataProperty( ":" + dataPropertyName, this.pm);
        OWLDataProperty property = this.getOWLFactory().getOWLDataProperty(  getPrefixFormat( dataPropertyName));

        logger.addDebugString( "OWLDataProperty given in: " + (System.nanoTime() - initialTime) + " [ns]");
        return (property);
    }
    /**
     * Returns an Object which represents an ontological object property
     * with a given name and specific IRI paths. If the entity
     * already exists in the ontology then the object will refer to it,
     * otherwise the method will create a new ontological entity
     * (not automatically added to the ontology).
     * @param objPropertyName name of the ontological object property.
     * @return the OWL object property with the given name and IRI paths, in accordance with the OWLReference
     */
    public OWLObjectProperty getOWLObjectProperty( String objPropertyName){
        long initialTime = System.nanoTime();

        //OWLObjectProperty property = this.getOWLFactory().getOWLObjectProperty( ":" + objPropertyName, this.pm());
        OWLObjectProperty property = this.getOWLFactory().getOWLObjectProperty(  getPrefixFormat( objPropertyName));

        logger.addDebugString( "OWLObjectProperty given in: " + (System.nanoTime() - initialTime) + " [ns]");
        return (property);
    }
    /**
     * Returns an Object which represents an ontological literal
     * with a given value and specific IRI paths.
     * @param value defines the value of the ontological literal to get.
     * @return the OWL literal with the given value, type and IRI paths, in accordance with the OWLReference.
     */
    public OWLLiteral getOWLLiteral( Object value){
        return( getOWLLiteral( value, null));
    }
    /**
     * Given an Object value, this method returns the OWLLiteral in accordance with the
     * actual type of {@code value}. The parameter Type can be {@code null} (i.e., not specified) if value is of type:
     * String, Integer, Boolean, Float, Long or OWLLiteral; otherwise it must be specified explicitly.
     * For more specific data type this methods require to give as input the right OWLDataType parameter.
     * WARNING: it will return {@code null} if the data type of the parameter value is unknown.
     * @param value value of queried literal.
     * @param type OWL data type of queried literal.
     * @return the OWL literal object with the given value, type and IRI paths, in accordance with the OWLReference.
     */
    public OWLLiteral getOWLLiteral( Object value, OWLDatatype type){
        long initialTime = System.nanoTime();
        OWLLiteral liter = null;
        if( value instanceof String)
            liter = getOWLFactory().getOWLLiteral( (String) value);
        else if( value instanceof Integer)
            liter = getOWLFactory().getOWLLiteral( (Integer) value);
        else if( value instanceof Boolean)
            liter = getOWLFactory().getOWLLiteral( (Boolean) value);
        else if( value instanceof Float)
            liter = getOWLFactory().getOWLLiteral( (Float) value);
        else if( value instanceof Double) {
            Float tmp = ((Double) value).floatValue();
            liter = getOWLFactory().getOWLLiteral(tmp);
        }else if( value instanceof Long)
            liter = getOWLFactory().getOWLLiteral( String.valueOf( value), getOWLFactory().getOWLDatatype( OWL2Datatype.XSD_LONG.getIRI()));
        else if( value instanceof OWLLiteral)
            liter = (OWLLiteral) value;
        else if( type != null)
            liter = getOWLFactory().getOWLLiteral( String.valueOf( value), type);
        else logger.addDebugString( "EXCEPTION: type for literal not known", true);
        logger.addDebugString( "OWLLitteral given in: " + (System.nanoTime() - initialTime) + " [ns]");
        return (liter);
    }


    // [[[[[[[[[[[[[[[[[[[[[[[[[[[[   METHODS USED FOR REASONING   ]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]

    /**
     * Performs reasoning on the Ontology when reasoner is set to buffering mode.
     * It does not have any effects on non-buffering reasoners.
     * WARNING: it does not consider pending changes on the manipulation buffer ({@link OWLManipulator}).
     * @param initialTime_ns is only used for logging purposes in case of reasoning pre-processing.
     * @param initialTime_ns represents the initial time used to compute the reasoning time[ns] to be logged.
     */
    protected void callReasoning( Long initialTime_ns){
        if( initialTime_ns == null)
            initialTime_ns =  System.nanoTime();
        this.reasoner.flush();
        Long finalTime = System.nanoTime();
        logger.addDebugString( "Synchronising... reasoner.flush() for ontology named: " +
                ". Reasoning Time: " + ( finalTime - initialTime_ns) + " [ns]" + " over ontology: " + this.getOWLOntology());
    }
    /**
     * Performs reasoning on the Ontology when reasoner is set to buffering mode.
     * It does not have any effects on non-buffering reasoners.
     * WARNING: it does not consider pending changes on the manipulation buffer ({@link OWLManipulator}).
     */
    protected void callReasoning(){
        this.callReasoning( System.nanoTime());
    }

    @Override
    public String toString() {
        return "OWLLibrary [getOWLManager()=" + getOWLManager() + ", getOWLFactory()="
                + getOWLFactory() + ", getOWLOntology()=" + getOWLOntology()
                + ", getPrefix()=" + getPrefix()
                + ", getOWLReasoner()=" + getOWLReasoner() + ", getIriFilePath()="
                + getIriFilePath() + ", getIriOntologyPath()="
                + getIriOntologyPath() + "]";
    }

}


