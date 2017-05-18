package it.emarolab.amor.owlInterface;

import it.emarolab.amor.owlDebugger.Logger;
import it.emarolab.amor.owlDebugger.Logger.LoggerFlag;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.OWLReasoner;

import java.io.File;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

//import org.coode.owlapi.manchesterowlsyntax.ManchesterOWLSyntaxOntologyFormat;
//import org.semanticweb.owlapi.model.OWLOntologyFormat;
//import uk.ac.manchester.cs.owlapi.dlsyntax.DLSyntaxObjectRenderer;
//import openllet.owlapi.explanation.PelletExplanation;
//import openllet.owlapi.explanation.io.manchester.ManchesterSyntaxExplanationRenderer;

// TODO : serialisation, toString. equals
// TODO : test load from web
// TODO : check different reasoners
// TODO : javadoc
// TODO : method for deserailisation
// TODO : OWLReferencesContainer SHOULD BE HERE!!!!


/**
 * <div style="text-align:center;"><small>
 * <b>Project</b>:    aMOR <br>
 * <b>File</b>:       it.emarolab.amor.owlInterface.OWLReferencesInterface <br>
 * <b>Licence</b>:    GNU GENERAL PUBLIC LICENSE. Version 3, 29 June 2007 <br>
 * <b>Author</b>:     Buoncompagni Luca (luca.buoncompagni@edu.unige.it) <br>
 * <b>affiliation</b>: DIBRIS, EMAROLab, University of Genoa. <br>
 * <b>date</b>:       Feb 10, 2016 <br>
 * </small></div>
 * 
 * <p>
 * This class implements the basic structure provided by the OWL api.<br>
 * It is not recommenced to instantiate directly this class or its extension, 
 * use {@link OWLReferencesContainer} insted.
 * </p>
 * 
 * @see OWLLibrary
 * @see OWLReferences
 * @see OWLReferencesContainer
 * 
 * @version 2.0
 */
public abstract class OWLReferencesInterface extends OWLLibrary{

    // [[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[   CLASS PRIVATE FIELDS   ]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]
    // object used by the OWL api

    private static Logger staicLogger = new Logger(OWLReferencesInterface.class, LoggerFlag.LOG_REFERENCES_INTERFACE);
    private Boolean bufferingReasoner;
    // object used by the OWL References Container
    private String referenceName;
    private int usedCommand;
    private String filePath;
    private String ontologyPath;
    private Boolean consistent = true;
    private ReasonerExplanator reasonerExplanator;
    private OWLManipulator manipulator;

//    private  final static OWLObjectRenderer renderer = new DLSyntaxObjectRenderer(); !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
private OWLEnquirer enquirer;
    /**
     * This object is used to log information about the instances of this class.
     * The logs can be activated by setting the flag {@link LoggerFlag#LOG_REFERENCES_INTERFACE}
     */
    private Logger logger = new Logger( this, LoggerFlag.LOG_REFERENCES_INTERFACE);

    // [[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[   CONSTRUCTORS   ]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]
    /**
     * Create a new references to an ontology using the standard reasoner.
     * @param referenceName the unique identifier for this ontology reference.
     *                      The reference is stored in {@link OWLReferencesContainer#allReferences}.
     * @param filePath the ontology file path (or URL).
     * @param ontologyPath the ontology IRI path.
     * @param bufferingReasoner if {@code true}, the reasoner is triggered only when {@link #synchronizeReasoner()}
     *                          is called. Else, the reasoner is called after every change to the ontology.
     * @param command specify if the reference must be created or loaded (from file or web).
     *                Possible values for{@code commands} are:
     *                {@link OWLReferencesContainer#COMMAND_CREATE}, {@link OWLReferencesContainer#COMMAND_LOAD_FILE} or
     *                {@link OWLReferencesContainer#COMMAND_LOAD_WEB}.
     */
    protected OWLReferencesInterface( String referenceName, String filePath, String ontologyPath, Boolean bufferingReasoner, Integer command){
        super(); // just create OWL library it does not initialise it. Remember to set iriFilePath and iriOntologyPath before to do enything else.
        // track this instance of this class
        if( referenceName != null)
            initializer( referenceName, filePath, ontologyPath, null, true, command);
        else logger.addDebugString( "Cannot initialise an OWL References with null name.", true);
    }
    /**
     * Create a new references to an ontology using the standard reasoner.
     * @param referenceName the unique identifier for this ontology reference.
     *                      The reference is stored in {@link OWLReferencesContainer#allReferences}.
     * @param filePath the ontology file path (or URL).
     * @param ontologyPath the ontology IRI path.
     * @param reasonerFactory the reasoner factory qualifier referring to the reasoner to use.
     *                        Possible values are: [{@link OWLLibrary#REASONER_QUALIFIER_PELLET},
     *                        {@link OWLLibrary#REASONER_QUALIFIER_HERMIT}, {@link OWLLibrary#REASONER_QUALIFIER_SNOROCKET}
     *                        or {@link OWLLibrary#REASONER_QUALIFIER_FACT}].
     * @param bufferingReasoner if {@code true}, the reasoner is triggered only when {@link #synchronizeReasoner()}
     *                          is called. Else, the reasoner is called after every change to the ontology.
     * @param command specify if the reference must be created or loaded (from file or web).
     *                Possible values for{@code commands} are:
     *                {@link OWLReferencesContainer#COMMAND_CREATE}, {@link OWLReferencesContainer#COMMAND_LOAD_FILE} or
     *                {@link OWLReferencesContainer#COMMAND_LOAD_WEB}.
     */
    protected OWLReferencesInterface( String referenceName, String filePath, String ontologyPath, String reasonerFactory, Boolean bufferingReasoner, Integer command) {
        super(); // just create OWL library it does not initialise it. Remember to set iriFilePath and iriOntologyPath before to do enything else.
        // track this instance of this class
        if( referenceName != null)
            initializer( referenceName, filePath, ontologyPath, reasonerFactory, bufferingReasoner, command);
        else logger.addDebugString( "Cannot initialise an OWL References with null name.", true);
    }

    /**
     * It gets the name of an ontological object from its IRI path.
     * It returns {@code null} if the input parameter is {@code null}.
     *
     * @param obj the object for which get the ontological name
     * @return the name of the ontological object given as input parameter.
     */
    public synchronized static String getOWLName(OWLObject obj) {
        /*if( o != null)
            return renderer.render( o));
            */
        if (obj != null) {
            String tmp = obj.toString();
            // ex: <http://www.co-ode.org/ontologies/pizza/pizza.owl#America>
            // ex: http://www.w3.org/2001/XMLSchema#boolean
            int start = tmp.lastIndexOf("#");
            int end = tmp.lastIndexOf(">");
            if ( end <= 0)
                end = tmp.length();
            if (start >= 0 & end >= 0)
                return tmp.substring(start + 1, end);
            else if (tmp.contains("http") & tmp.contains("://")) {
                int s = tmp.lastIndexOf("/");
                if (s >= tmp.length()) {
                    tmp = tmp.substring(0, tmp.length() - 2);
                    s = tmp.lastIndexOf("/");
                }
                int e = tmp.lastIndexOf(">");
                if (s >= 0 & e >= 0)
                    return tmp.substring(s + 1, e);
            } else {
                // ex: "1"^^xsd:integer
                String s = tmp;
                start = s.indexOf("\"");
                if (start >= 0) {
                    s = s.substring(start + 1);
                    end = s.indexOf("\"");
                    if (end >= 0)
                        return s.substring(0, end);
                }
            }
            return obj.toString(); // if cannot parse return complete name
        }
        staicLogger.addDebugString("Cannot get the OWL name of a null OWL object", true);
        return null;
    }

    /**
     * It gets the name of a set of ontological objects from its IRI path.
     * It returns {@code null} if the input parameter is {@code null}.
     *
     * @param objects the set of objects for which get the ontological name
     * @return the name of the ontological objects given as input parameters.
     */
    public synchronized static Set<String> getOWLName(Set<?> objects) {
        Set<String> out = new HashSet<String>();
        for (Object o : objects) {
            if (o instanceof OWLObject) {
                OWLObject owlObj = (OWLObject) o;
                out.add(getOWLName(owlObj));
            } else staicLogger.addDebugString("Cannot get the name of a non OWL object. Given entity: " + o, true);
        }
        return out;
    }

    /**
     * This method implements the common initialisation procedure called by
     * {@link OWLReferencesInterface(String, String, String, Boolean, Integer)} and
     * {@link OWLReferencesInterface(String, String, String, OWLReasoner, Boolean, Integer)}.
     * It stores the reference created in {@link OWLReferencesContainer#addInstance(OWLReferencesInterface)}
     * to create a static map of the system.
     * @param referenceName the unique identifier for this ontology reference.
     *                      The reference is stored in {@link OWLReferencesContainer#allReferences}.
     * @param filePath the ontology file path (or URL).
     * @param ontologyPath the ontology IRI path.
     * @param reasonerFactory the reasoner factory qualifier referring to the reasoner to use.
     *                        Possible values are: [{@link OWLLibrary#REASONER_QUALIFIER_PELLET},
     *                        {@link OWLLibrary#REASONER_QUALIFIER_HERMIT}, {@link OWLLibrary#REASONER_QUALIFIER_SNOROCKET}
     *                        or {@link OWLLibrary#REASONER_QUALIFIER_FACT}].
     * @param bufferingReasoner if {@code true}, the reasoner is triggered only when {@link #synchronizeReasoner()}
     *                          is called. Else, the reasoner is called after every change to the ontology.
     * @param command specify if the reference must be created or loaded (from file or web).
     *                Possible values for{@code commands} are:
     *                {@link OWLReferencesContainer#COMMAND_CREATE}, {@link OWLReferencesContainer#COMMAND_LOAD_FILE} or
     *                {@link OWLReferencesContainer#COMMAND_LOAD_WEB}.
     */
    private synchronized void initializer(String referenceName, String filePath, String ontologyPath, String reasonerFactory, Boolean bufferingReasoner, Integer command){
        this.referenceName = referenceName; // set the unique identifier of this object
        if(  OWLReferencesContainer.addInstance( this)){ // add this class to the static map
            long initialTime = System.nanoTime();
            // set internal variables
            this.filePath = filePath;
            this.ontologyPath = ontologyPath;
            this.setIriOntologyPath( IRI.create( ontologyPath));
            this.setPrefixFormat();
            this.usedCommand = command;
            // take an ontology opening action by considering its vale
            switch( command){
            case 0: //OWLReferencesContainer.COMMAND_CREATE
                this.setIriFilePath( IRI.create( filePath));
                this.setManager(); // creates and sets the filed that you can retrieve from getOWLManager();
                this.createOntology(); // creates and set the field taht you can retrieve from getOWLOntology();
                break;
            case 1: //OWLReferencesContainer.COMMAND_LOAD_FILE
                this.setIriFilePath( IRI.create( new File( filePath)));
                this.setManager(); // creates and sets the filed that you can retrieve from getOWLManager();
                this.loadOntologyFromFile(); // creates and set the field taht you can retrieve from getOWLOntology();
                break;
            case 2: //OWLReferencesContainer.COMMAND_LOAD_WEB
                this.setIriFilePath( IRI.create( ontologyPath)); // in this case the file path should be a WEB URL
                this.setManager(); // creates and sets the filed that you can retrieve from getOWLManager();
                this.loadOntologyFromWeb(); // creates and set the field taht you can retrieve from getOWLOntology();
                this.setIriFilePath( IRI.create( filePath));
                break;
            default : logger.addDebugString( "Cannot initialise OWL References with the given command: " + command, true);
            }
            // now that the manager and the ontology is initialise, create other owl api objects
            this.setFactory(); // creates and sets the field that you can retrieve from getOWLFactory();
            //this.setPrefixFormat(); // creates and sets the field that you can retrieve from getPrefixForamt();

            if (reasonerFactory == null || reasonerFactory.equals( OWLLibrary.REASONER_DEFAULT))
                setDefaultReasoner( bufferingReasoner); // actually it Initialise  pellet as reasoner
            else {
                this.setOWLReasoner( reasonerFactory, bufferingReasoner, referenceName);
                //this.resonerFactory = reasonerFactory; // used for serialisation
                if( reasonerFactory.equals( OWLLibrary.REASONER_QUALIFIER_PELLET))
                    setPelletReasonerExplanator();
            }
            this.bufferingReasoner = bufferingReasoner;
            this.manipulator = new OWLManipulator( this); // use default apply change flag value
            this.enquirer = new OWLEnquirer( this);
            logger.addDebugString( "new OWL References initialised in: " + (System.nanoTime() - initialTime) + " [ns] for the Object " + this);
        }else logger.addDebugString( "Cannot initialise an OWL References with a name already available in the map", true);
    }

    /**
     * Method called when the {@code reasonerFactory} is {@code null} or not given.
     * It creates a new instance of the reasoner and assign it to the reference.
     * By default, it creates an instance of the Pellet reasoner {@link OWLLibrary#setPelletReasoner(boolean, String)}.<br>
     * @param buffering if {@code true}, the reasoner is triggered only when {@link #synchronizeReasoner()}
     *                  is called. Else, the reasoner is called after every change to the ontology.
     */
    protected void setDefaultReasoner( Boolean buffering){
        this.setPelletReasoner( buffering, referenceName);
        //resonerFactory = OWLLibrary.REASONER_QUALIFIER_PELLET; // used for serialisation
        setPelletReasonerExplanator();
    }

    /**
     * Sets the default reasoner explanator object for pellet.
     * It is called in {@link #initializer(String, String, String, String, Boolean, Integer)}
     * and {@link #setDefaultReasoner(Boolean)}.
     */
    private void setPelletReasonerExplanator(){
        this.setReasonerExplanator( new PelletReasonerExplanation( this));
    }

    /**
     * @param reasonerExplanator the reasoner Explanator implementation to set.
     */
    protected synchronized void setReasonerExplanator(    ReasonerExplanator reasonerExplanator) {
        this.reasonerExplanator = reasonerExplanator;
    }

    /**
     * Calls {@link OWLReferencesContainer#removeInstance(OWLReferencesInterface)}
     * to remove to remove the reference from the static system map when this object gets finalized.
     * @see java.lang.Object#finalize()
     */
    @Override
    public synchronized void finalize() throws Throwable {
        OWLReferencesContainer.removeInstance( this);
        super.finalize();
    }

    /**
     * Modify the current reasoner buffering mode.
     * @param bufferize if {@code true}, the reasoner is triggered only when {@link #synchronizeReasoner()}
     *                  is called. Else, the reasoner is called after every change to the ontology.
     */
    public synchronized void setOWLManipulatorBuffering( Boolean bufferize){
        manipulator.setManipulationBuffering(bufferize);
    }

    // [[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[   GETTERS   ]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]

    /**
     * Modify the current enquirer reasoning setting.
     * @param flag set to {@code false} if the query should consider only asserted axioms.
     *             Set to {@code true} for considering also reasoned axioms.
     */
    public synchronized void setOWLEnquirerIncludesInferences(Boolean flag){
        this.enquirer.setIncludeInferences( flag);
    }

    /**
     * @return the referenceName in {@link OWLReferencesContainer} referring to this instance.
     * The reference name is initialized by the constructor and  can be used to retrieve a pointer
     * to an instantiated ontology. This name should be considered as an ontology unique qualifier.
     */
    public synchronized String getReferenceName() {
        return referenceName;
    }

    /**
     * @return the directory path (or the URL) to the file that contains the ontology.
     */
    public synchronized String getFilePath() {
        return filePath;
    }

    /**
     * @return the ontology semantic path (IRI).
     */
    public synchronized String getOntologyPath() {
        return ontologyPath;
    }

    /**
     * @return the mode this is instance has been initialized. Possible values are:
     * [{@link OWLReferencesContainer#COMMAND_CREATE}, {@link OWLReferencesContainer#COMMAND_LOAD_FILE},
     * {@link OWLReferencesContainer#COMMAND_LOAD_WEB}].
     */
    public synchronized int getUsedCommand() {
        return usedCommand;
    }

    /**
     * @return the value of the reasoner buffering flag.
     * If {@code true}, the reasoner is triggered only when {@link #synchronizeReasoner()}
     * is called. Else, the reasoner is called after every change to the ontology.
     */
    public synchronized Boolean useBufferingReasoner(){
        return bufferingReasoner;
    }

    /**
     * @return explanator object used to return the causes of an inconsistency.
     * It is only implemented for Pellet and it is automatically initialized if the default reasoner is used.
     */
    public synchronized ReasonerExplanator getReasonerExplainer(){
        return reasonerExplanator;
    }

    /**
     * @return the object that applies manipulation to the ontology (not thread safe).
     * This object should not be used by a user. Use {@link OWLReferences} instead.
     */
    protected synchronized OWLManipulator getManipulator(){
        return this.manipulator;
    }

    /**
     * This method calls {@link OWLManipulator#applyChanges()}.
     * it caused all buffered manipulations to be immediately applied.
     */
    public synchronized void applyOWLManipulatorChanges(){
        manipulator.applyChanges();
    }

    /**
     * Applies a single axiom (expressed as an ontology change) to the ontology by calling {@link OWLManipulator#applyChanges(OWLOntologyChange)}.
     * @param addAxiom the axiom to be applied into the ontology.
     */
    public synchronized void applyOWLManipulatorChanges(OWLOntologyChange addAxiom){
        manipulator.applyChanges(addAxiom);
    }

    /**
     * Applies a list of axioms (expressed as ontology changes) to the ontology by calling {@link OWLManipulator#applyChanges(List)}.
     * @param addAxiom the list of axioms to be applied into the ontology.
     */
    public synchronized void applyOWLManipulatorChanges(List<OWLOntologyChange> addAxiom){
        manipulator.applyChanges(addAxiom);
    }

    /**
     * Applies an axiom to the ontology. First, it gets the necessary changes using
     * {@link OWLManipulator#applyChanges(List)}, then calls {@link OWLManipulator#getAddAxiom(OWLAxiom)} to apply them.
     * Depending on {@link OWLManipulator#isChangeBuffering()} flag, those changes would be
     * added (or not) to the internal manipulator buffer.
     * @param addAxiom the axiom to be added and applied to the ontology.
     */
    public synchronized void applyOWLManipulatorChangesAddAxiom( OWLAxiom addAxiom){
        manipulator.applyChanges( manipulator.getAddAxiom( addAxiom));
    }

    /**
     * Removes an axiom to the ontology. First, it gets the necessary changes using
     * {@link OWLManipulator#applyChanges(List)}, then calls {@link OWLManipulator#getAddAxiom(OWLAxiom)} to apply them.
     * Depending on {@link OWLManipulator#isChangeBuffering()} flag, those changes would be
     * added (or not) to the internal manipulator buffer.
     * @param removeAxiom the axiom to be removed and applied to the ontology.
     */
    public synchronized void applyOWLManipulatorChangesRemoveAxiom( OWLAxiom removeAxiom){
        manipulator.applyChanges( manipulator.getRemoveAxiom( removeAxiom));
    }

    /**
     * @return the object used to query the ontology (not thread safe).
     * This object should not be used from an aMOR user. Use {@link OWLReferences} instead.
     */
    protected synchronized OWLEnquirer getEnquirer(){
        return this.enquirer;
    }

    /**
     * @return {@code true} if the enquirer is set to return all the sup/super
     * properties up to the leaf/root of the structure.
     * Else, it returns only the first direct assertion.
     */
    public synchronized Boolean getOWLEnquirerCompletenessFlag(){
        return this.enquirer.isReturningCompleteDescription();
    }


    // [[[[[[[[[[[[[[[[[[[[[[   METHODS TO CALL REASONING   ]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]

    /**
     * Modify the current enquirer completeness setting.
     *
     * @param flag set to {@code true} if the enquirer should return all the sub/super
     *             properties up to the leaves/root of the structure.
     *             Else, it returns only the first direct assertion.
     */
    public synchronized void setOWLEnquirerCompletenessFlag(Boolean flag) {
        this.enquirer.setReturnCompleteDescription( flag);
    }

    /**
     * @return {@code false} if the enquirer is set to query only the asserted axioms.
     * {@code True}, if also inferred axiom are returned byb the enquirer.
     */
    public synchronized Boolean getOWLEnquirerReasoningFlag(){
        return this.enquirer.isIncludingInferences();
    }

    /**
     * If the Ontology is consistent, it will synchronise a buffering reasoner
     * calling {@code reasoner.flush()}; if the reasoner buffering is set to false,
     * then this method has no effects. <br>
     * If an inconsistency error occurs, it will print on console an explanation of the
     * error. Such an explanation interface can be implemented with the {@link ReasonerExplanator} interface. <br>
     * Note that if the ontology is inconsistent then all the methods in this class may return {@code null}.
     * WARNING: manipulation buffer is always flushed before synchronizing the reasoner by {@link OWLManipulator#applyChanges()}.
     * You can synchronize the reasoner manually (non-buffering mode) by using {@link #checkConsistent()}
     */
    public synchronized void synchronizeReasoner(){
        if( this.isConsistent()){
            try{
                Long initialTime = System.nanoTime();
                this.getManipulator().applyChanges(); // be sure to empty the buffer (if any)
                this.callReasoning( initialTime);
                if( !this.checkConsistent())
                    this.logInconsistency();
            } catch( org.semanticweb.owlapi.reasoner.InconsistentOntologyException e){
                this.logInconsistency();
            }
        } else {
            this.checkConsistent();
        }
    }

     /**
     * Applies some changes, then synchronize the reasoner.
     * If the Ontology is consistent, it will synchronise a buffering reasoner
     * calling {@code reasoner.flush()}; if the reasoner buffering is set to false,
     * then this method has no effects. <br>
     * If an inconsistency error occurs, it will print on console an explanation of the
     * error. Such an explanation interface can be implemented with the {@link ReasonerExplanator} interface. <br>
     * Note that if the ontology is inconsistent then all the methods in this class may return {@code null}.
     * WARNING: manipulation buffer is always flushed before synchronizing the reasoner by {@link OWLManipulator#applyChanges()}.
     * You can synchronize the reasoner manually (non-buffering mode) by using {@link #checkConsistent()}
      * @param changesBuffer the buffer of ontology changes to be applied before reasoning.
     */
    public synchronized void synchronizeReasoner(List<OWLOntologyChange> changesBuffer){
        if( this.isConsistent()){
            try{
                Long initialTime = System.nanoTime();
                this.getManipulator().applyChanges( changesBuffer);
                this.callReasoning( initialTime);
                if( !this.checkConsistent())
                    this.logInconsistency();
            } catch( org.semanticweb.owlapi.reasoner.InconsistentOntologyException e){
                this.logInconsistency();
            }
        } else {
            this.checkConsistent();
        }
    }

    /**
     * @return the consistency state flag for this OWL Reference.
     */
    public synchronized boolean isConsistent() {
        return consistent;
    }

    /**
     * Call the reasoner to check ontology consistency and synchronizes the consistency state flag of this instance.
     * @return the consistency state after the reasoner is synchronized .
     */
    protected synchronized boolean checkConsistent() {
        consistent = this.getOWLReasoner().isConsistent();
        return consistent;
    }

    // [[[[[[[[[[[[[[[[[[[[[[   METHODS TO PARSE ONTOLOGY NAMES   ]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]

    /**
     * This method check if the {@link #reasonerExplanator} is initialised.
     * If yes, it calls {@link ReasonerExplanator#notifyInconsistency()}.
     * Otherwise, it logs to console that no Reasoner Explanator implementation is initialized.
     */
    protected synchronized void logInconsistency(){
        if( reasonerExplanator != null)
            reasonerExplanator.notifyInconsistency();
        else logger.addDebugString( "The ontology is not consistent but the system does not provide any ReasonerExplanator implementation.", true);
    }

    /**
     * Utility method to get an object from a set which is supposed to have only one element.
     * Returns {@code null} {@code if( set.size() == 0)}.
     * If the set is non-empty, returns the first value, which can change since the sets are non ordered.
     * Example: you need to get the value of an object property but you know from the design of your ontology
     * that an individual can be assigned only one value for that specific property. If more than one are assigned,
     * a random one is returned.
     * @param set a generic set of objects.
     * @return an element of the set.
     */
    public synchronized Object getOnlyElement(Set< ?> set){
        if( set != null){
            for( Object i : set){
                return( i);
            }
        }
        logger.addDebugString("Get only elements cannot work with an null or empty set.", true);
        return( null);
    }

    /**
     * It gets the name of an ontological object from its IRI path.
     * It returns {@code null} if the input parameter is {@code null}.
     * This is just a non-static interface to the method {@link #getOWLName(OWLObject)}.
     * @param obj the object for which to get the ontological name
     * @return the name of the ontological object given as input parameter.
     */
    public synchronized String getOWLObjectName( OWLObject obj){
        return getOWLName( obj);
    }

    /**
     * It gets the name of a set of ontological objects from its IRI path.
     * It returns {@code null} if the input parameter is {@code null}.
     * This is just a not static interface to the method {@link #getOWLName(Set)}.
     * @param objects the set of objects for which get the ontological name
     * @return the name of the ontological objects given as input parameters.
     */
    public synchronized Set<String> getOWLObjectName( Set< ?> objects){
        return getOWLName( objects);
    }

    /**
     * Save the referenced ontology by using the path stored on {@link #getIriFilePath()}.
     */
    public synchronized void saveOntology(){
        try {
            File file = new File( this.getIriFilePath().toString());
            this.getOWLManager().saveOntology( this.getOWLOntology(), IRI.create( file.toURI()));
            logger.addDebugString( "Ontology References: " + this + " saved on path: " + this.getIriFilePath().toString());
        } catch (OWLOntologyStorageException e) {
            e.printStackTrace();
            logger.addDebugString( "Error on saving the ontology: " + this + " on path: " + this.getIriFilePath(), true);
        }
    }


    // [[[[[[[[[[[[[[[[[[[[[[   METHODS TO SAVE (print) ONTOLOGY   ]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]

    /**
     * Save the referenced ontology by using the path given as input argument.
     * @param filePath the directory path in which save the ontology.
     */
    public synchronized void saveOntology( String filePath){
        try {
            File file = new File( filePath);
            this.getOWLManager().saveOntology( this.getOWLOntology(), IRI.create( file.toURI()));
            logger.addDebugString( "Ontology References: " + this + " saved on path: " + filePath);
        } catch (OWLOntologyStorageException e) {
            e.printStackTrace();
            logger.addDebugString( "Error on saving the ontology: " + this + " on path: " + this.getIriFilePath(), true);
        }
    }

    /**
     * It prints the ontology on the java console using Manchester syntax.
     */
    public void printOntologyOnConsole() {
        /*try{
            long initialTime = System.nanoTime();
            ManchesterOWLSyntaxOntologyFormat manSyntaxFormat = new ManchesterOWLSyntaxOntologyFormat();
            OWLOntologyManager man = this.getOWLManager();
            OWLOntology ont = this.getOWLOntology();
            OWLOntologyFormat format = man.getOntologyFormat(ont);
            if (format.isPrefixOWLOntologyFormat())
                manSyntaxFormat.copyPrefixesFrom(format.asPrefixOWLOntologyFormat());
            logger.addDebugString("---------  PRINTING ONTOLOGY --------");
            man.saveOntology( ont, manSyntaxFormat, new StreamDocumentTarget( System.out));
            logger.addDebugString( "ontology printed in console in: " + (System.nanoTime() - initialTime) + " [ns]");
        } catch( OWLOntologyStorageException e){
            e.printStackTrace();
        } catch( org.semanticweb.owlapi.reasoner.InconsistentOntologyException e){
            this.logInconsistency();
        }*/
        System.err.println("The ontology printing on console has not been ported to owl 5 yet !!!");
    }

    /**
     * Stores the reasoner and buffering mode, no further actions are taken by this method.
     *
     * @param refInterface the OWL reference that can {@link #getOWLReasoner()} and {@link #bufferingReasoner} to set.
     */
    public void setReasoner(OWLReferencesInterface refInterface) {
        this.setOWLReasoner(refInterface.getOWLReasoner());
        this.bufferingReasoner = refInterface.useBufferingReasoner();
    }

    /**
     * @return (verbose) print all the fields of this object
     */
    public String toStringAll() {
        return "OWLReferencesInterface [getReferenceName()="
                + getReferenceName() + ", getFilePath()=" + getFilePath()
                + ", getOntologyPath()=" + getOntologyPath()
                + ", getUsedCommand()=" + getUsedCommand()
                + ", useBufferingReasoner()=" + useBufferingReasoner()
                + ", getReasonerExplainer()=" + getReasonerExplainer()
                + ", isConsistent()=" + isConsistent() + " " + super.toString();
    }

    @Override
    public String toString() {
        return "OWLReferencesInterface \"" + getReferenceName() + "\"";
    }

    /**
     * <div style="text-align:center;"><small>
     * <b>Project</b>:    aMOR <br>
     * <b>File</b>:       it.emarolab.amor.owlInterface.OWLReferencesInterface <br>
     * <b>Licence</b>:    GNU GENERAL PUBLIC LICENSE. Version 3, 29 June 2007 <br>
     * <b>Author</b>:     Buoncompagni Luca (luca.buoncompagni@edu.unige.it) <br>
     * <b>affiliation</b>: DIBRIS, EMAROLab, University of Genoa. <br>
     * <b>date</b>:       Feb 10, 2016 <br>
     * </small></div>
     *
     * <p>
     * This class implements methods to instantiate or retrieve a reference to an ontology.<br>
     * This is done by keeping track of all instances of the extending classes
     * of {@link OWLReferencesInterface} in a static map ({@link #allReferences}).
     * The database maps each instance with respect to a name ({@code ontoName})
     * given when the reference was initialized as an unique ontology qualifier.
     * </p>
     *
     *
     * @version 2.0
     */
    public static class OWLReferencesContainer{

        // [[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[   CLASS CONSTANTS   ]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]
        /**
         * Command value that specifies that the new ontology references has to point to a new ontology.
         * In particular, its value is: {@link  #COMMAND_CREATE}.
         */
        static public final Integer COMMAND_CREATE = 0;
        /**
         * Command value that specifies that the new ontology references has to point to an ontology file.
         * In particular, its value is: {@link  #COMMAND_LOAD_FILE}.
         */
        static public final Integer COMMAND_LOAD_FILE = 1;
        /**
         * Command value that specifies that the new ontology references has to point to an ontology stored in the web.
         * In particular, its value is: {@link  #COMMAND_LOAD_WEB}.
         */
        static public final Integer COMMAND_LOAD_WEB = 2;


        // [[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[   CLASS PRIVATE FIELDS   ]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]
        /**
         * This static map contains all the OWL references instantiated by the system and stored in this class.
         * In particular, this map is an instance of {@link ConcurrentHashMap}, please see its specification
         * for synchronisation on a thread safe system.
         */
        private static Map<String, OWLReferencesInterface> allReferences = new ConcurrentHashMap<String, OWLReferencesInterface>();

        /**
         * This object is used to log information about the owl references managed by this container class.
         * The logs can be activated by setting the flag {@link LoggerFlag#LOG_REFERENCES_CONTAINER}.
         */
        private static Logger logger = new Logger( OWLReferencesContainer.class, LoggerFlag.LOG_REFERENCES_CONTAINER);


        // [[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[   METHODS TO MANAGE THE MAP   ]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]

        /**
         * This method adds a reference instance to the internal map {@link #allReferences}.
         * This procedure is automatically managed by using the instantiating procedure implemented by this class.
         * @param instance the new OWL reference to be add to the internal map.
         * @return {@code false} if the map already contains an object with name {@link OWLReferencesInterface#getReferenceName()}
         * and no action was taken. {@code true} otherwise.
         */
        private static Boolean addInstance(OWLReferencesInterface instance){
            String refName = instance.getReferenceName();
            if( ! isInstance( refName)){
                allReferences.putIfAbsent( refName, instance);
                return( true);
            }
            logger.addDebugString( "Exception: cannot create another Ontology with referencing name: " + refName, true);
            return( false);
        }

        /**
         * This method remove an instance from the internal map {@link #allReferences}.
         * This is automatically done when {@link OWLReferencesInterface#finalize()} method is called.
         * @param instance the OWL reference to be removed from the internal map.
         * @return {@code false} if the map does not contain an object with name {@link OWLReferencesInterface#getReferenceName()}
         * and no action was taken. {@code true} otherwise.
         */
        private static Boolean removeInstance(OWLReferencesInterface instance){
            String refName = instance.getReferenceName();
            if( isInstance( refName)){
                allReferences.remove( refName);
                return( true);
            }
            logger.addDebugString( "Exception: cannot remove an Ontology with referencing name: " + refName, true);
            return (false);
        }

        /**
         * @param instance the OWL interface object to check.
         * @return {@code true} if the internal map {@link #allReferences}
         * contains this instance. {@code false} otherwise.
         */
        public static Boolean isInstance(OWLReferencesInterface instance){
            return isInstance( instance.getReferenceName());
        }

        /**
         * @param referenceName the reference name to the OWL reference object to check.
         * @return {@code true} if the internal map {@link #allReferences}
         * contains a matching instance. {@code false} otherwise.
         */
        public static Boolean isInstance(String referenceName){
            return allReferences.containsKey( referenceName);
        }

        /**
         * Return a particular OWL reference, given its name.
         *
         * @param referenceName the reference name of a particular OWL ontology references instance.
         * @return the instance carrying the specified reference name name.
         *         Returns {@code null} if the map does not contains an object a matching {@code referenceName}.
         */
        public static OWLReferencesInterface getOWLReferences(String referenceName){
            return( allReferences.get( referenceName));
        }

        /**
         * @return all reference names stored in ({@link #allReferences}).
         */
        public static Set< String> getOWLReferencesKeys(){
            return allReferences.keySet();
        }

        /**
         * @return all the reference instances stored in ({@link #allReferences}).
         */
        public static Collection< OWLReferencesInterface> getOWLReferencesValues(){
            return allReferences.values();
        }

        // [[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[   METHODS TO CREATE ONTOLOGY REFERENCES   ]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]

        /**
         * Creates a new OWL References by calling {@link OWLReferences#OWLReferences(String, String, String, Boolean, Integer)}
         * @param referenceName the unique identifier of this ontology references to store in {@link OWLReferencesContainer#allReferences}.
         * @param filePath the file path (or URL) to the ontology.
         * @param ontologyPath the IRI path of the ontology.
         * @param bufferingReasoner if {@code true}, the reasoner is triggered only when {@link #synchronizeReasoner()}
         *                          is called. Else, the reasoner is called after every change to the ontology.
         * @param command specifying if the ontology should be created, loaded from file or from web. Possible values are:
         *                {@link OWLReferencesContainer#COMMAND_CREATE}, {@link OWLReferencesContainer#COMMAND_LOAD_FILE} or
         *                {@link OWLReferencesContainer#COMMAND_LOAD_WEB}.
         * @return a fully initialised OWL Reference object.
         */
        public static OWLReferences newOWLReferences(String referenceName, String filePath, String ontologyPath, Boolean bufferingReasoner, Integer command){
            return new OWLReferences( referenceName, filePath, ontologyPath, bufferingReasoner, command);
        }

        /**
         * Creates a new OWL References by calling {@link OWLReferences#OWLReferences(String, String, String, String, Boolean, Integer)}
         * @param referenceName the unique identifier of this ontology reference to store in {@link OWLReferencesContainer#allReferences}.
         * @param filePath the file path (or URL) to the ontology.
         * @param ontologyPath the IRI path of the ontology.
         * @param reasonerFactory the reasoner factory qualifier referring to the reasoner to use.
         *                        Possible values are: [{@link OWLLibrary#REASONER_QUALIFIER_PELLET},
         *                        {@link OWLLibrary#REASONER_QUALIFIER_HERMIT}, {@link OWLLibrary#REASONER_QUALIFIER_SNOROCKET}
         *                        or {@link OWLLibrary#REASONER_QUALIFIER_FACT}].
         * @param bufferingReasoner if {@code true}, the reasoner is triggered only when {@link #synchronizeReasoner()}
         *                          is called. Else, the reasoner is called after every change to the ontology.
         * @param command specifying if the ontology should be created, loaded from file or from web. Possible value of {@code commands} are:
         *                {@link OWLReferencesContainer#COMMAND_CREATE}, {@link OWLReferencesContainer#COMMAND_LOAD_FILE} or
         *                {@link OWLReferencesContainer#COMMAND_LOAD_WEB}.
         * @return a fully initialised OWL Reference object.
         */
        public static OWLReferences newOWLReferences(String referenceName, String filePath, String ontologyPath, String reasonerFactory, Boolean bufferingReasoner, Integer command){
            return new OWLReferences(referenceName, filePath, ontologyPath, reasonerFactory, bufferingReasoner, command);
        }


        /**
         * Create a new References from a new empty ontology with the default reasoner ({@link OWLReferencesInterface#setDefaultReasoner(Boolean)}).
         * @param referenceName the unique identifier of this ontology reference to store in {@link OWLReferencesContainer#allReferences}.
         * @param filePath the path to save the newly generated ontology.
         * @param ontologyPath the semantic IRI path of the ontology to be created
         * @param bufferingReasoner if {@code true}, the reasoner is triggered only when {@link #synchronizeReasoner()}
         *                          is called. Else, the reasoner is called after every change to the ontology.
         * @return a fully initialised OWL Reference object.
         */
        public static OWLReferences newOWLReferencesCreated(String referenceName, String filePath, String ontologyPath, Boolean bufferingReasoner){
            return new OWLReferences(referenceName, filePath, ontologyPath, bufferingReasoner, COMMAND_CREATE);
        }

        /**
         * Create a new References to an new empty ontology with the Pellet reasoner.
         * @param referenceName the unique identifier of this ontology reference to store in {@link OWLReferencesContainer#allReferences}.
         * @param filePath the path to save the newly generated ontology.
         * @param ontologyPath the semantic IRI path of the ontology to be created
         * @param bufferingReasoner if {@code true}, the reasoner is triggered only when {@link #synchronizeReasoner()}
         *                          is called. Else, the reasoner is called after every change to the ontology.
         * @return a fully initialised OWL Reference object.
         */
        public static OWLReferences newOWLReferencesCreatedWithPellet(String referenceName, String filePath, String ontologyPath, Boolean bufferingReasoner){
            return new OWLReferences(referenceName, filePath, ontologyPath, OWLLibrary.REASONER_QUALIFIER_PELLET, bufferingReasoner, COMMAND_CREATE);
        }

        /**
         * Create a new References to an new empty ontology with the Hermit reasoner.
         * @param referenceName the unique identifier of this ontology reference to store in {@link OWLReferencesContainer#allReferences}.
         * @param filePath the path to save the newly generated ontology.
         * @param ontologyPath the semantic IRI path of the ontology to be created
         * @param bufferingReasoner if {@code true}, the reasoner is triggered only when {@link #synchronizeReasoner()}
         *                          is called. Else, the reasoner is called after every change to the ontology.
         * @return a fully initialised OWL Reference object.
         */
        public static OWLReferences newOWLReferencesCreatedWithHermit(String referenceName, String filePath, String ontologyPath, Boolean bufferingReasoner){
            return new OWLReferences(referenceName, filePath, ontologyPath, OWLLibrary.REASONER_QUALIFIER_HERMIT, bufferingReasoner, COMMAND_CREATE);
        }

        /**
         * Create a new References to an new empty ontology with the Fact reasoner.
         * @param referenceName the unique identifier of this ontology reference to store in {@link OWLReferencesContainer#allReferences}.
         * @param filePath the path to save the newly generated ontology.
         * @param ontologyPath the semantic IRI path of the ontology to be created
         * @param bufferingReasoner if {@code true}, the reasoner is triggered only when {@link #synchronizeReasoner()}
         *                          is called. Else, the reasoner is called after every change to the ontology.
         * @return a fully initialised OWL Reference object.
         */
        public static OWLReferences newOWLReferencesCreatedWithFact(String referenceName, String filePath, String ontologyPath, Boolean bufferingReasoner){
            return new OWLReferences(referenceName, filePath, ontologyPath, OWLLibrary.REASONER_QUALIFIER_FACT, bufferingReasoner, COMMAND_CREATE);
        }

        /**
         * Create a new References to an new empty ontology with the Snorocket reasoner.
         * @param referenceName the unique identifier of this ontology reference to store in {@link OWLReferencesContainer#allReferences}.
         * @param filePath the path to save the newly generated ontology.
         * @param ontologyPath the semantic IRI path of the ontology to be created
         * @param bufferingReasoner if {@code true}, the reasoner is triggered only when {@link #synchronizeReasoner()}
         *                          is called. Else, the reasoner is called after every change to the ontology.
         * @return a fully initialised OWL Reference object.
         */
        public static OWLReferences newOWLReferencesCreatedWithSnorocket(String referenceName, String filePath, String ontologyPath, Boolean bufferingReasoner){
            return new OWLReferences(referenceName, filePath, ontologyPath, OWLLibrary.REASONER_QUALIFIER_SNOROCKET, bufferingReasoner, COMMAND_CREATE);
        }


        /**
         * Create a new References to an ontology loaded from file with the default reasoner ({@link OWLReferences#setDefaultReasoner(Boolean)}).
         * @param referenceName the unique identifier of this ontology reference to store in {@link OWLReferencesContainer#allReferences}.
         * @param filePath path to the file to load (used by default for saving too).
         * @param ontologyPath the semantic IRI path of the ontology to be created
         * @param bufferingReasoner if {@code true}, the reasoner is triggered only when {@link #synchronizeReasoner()}
         *                          is called. Else, the reasoner is called after every change to the ontology.
         * @return a fully initialised OWL Reference object.
         */
        public static OWLReferences newOWLReferenceFromFile(String referenceName, String filePath, String ontologyPath, Boolean bufferingReasoner){
            return new OWLReferences(referenceName, filePath, ontologyPath, bufferingReasoner, COMMAND_LOAD_FILE);
        }

        /**
         * Create a new References to an ontology loaded from file with the Pellet reasoner.
         * @param referenceName the unique identifier of this ontology reference to store in {@link OWLReferencesContainer#allReferences}.
         * @param filePath path to the file to load (used by default for saving too).
         * @param ontologyPath the semantic IRI path of the ontology to be created
         * @param bufferingReasoner if {@code true}, the reasoner is triggered only when {@link #synchronizeReasoner()}
         *                          is called. Else, the reasoner is called after every change to the ontology.
         * @return a fully initialised OWL Reference object.
         */
        public static OWLReferences newOWLReferenceFromFileWithPellet(String referenceName, String filePath, String ontologyPath, Boolean bufferingReasoner){
            return new OWLReferences( referenceName, filePath, ontologyPath, OWLLibrary.REASONER_QUALIFIER_PELLET, bufferingReasoner, COMMAND_LOAD_FILE);
        }

        /**
         * Create a new References to an ontology loaded from file with the Hermit reasoner.
         * @param referenceName the unique identifier of this ontology reference to store in {@link OWLReferencesContainer#allReferences}.
         * @param filePath path to the file to load (used by default for saving too).
         * @param ontologyPath the semantic IRI path of the ontology to be created
         * @param bufferingReasoner if {@code true}, the reasoner is triggered only when {@link #synchronizeReasoner()}
         *                          is called. Else, the reasoner is called after every change to the ontology.
         * @return a fully initialised OWL Reference object.
         */
        public static OWLReferences newOWLReferenceFromFileWithHermit(String referenceName, String filePath, String ontologyPath, Boolean bufferingReasoner){
            return new OWLReferences( referenceName, filePath, ontologyPath, OWLLibrary.REASONER_QUALIFIER_HERMIT, bufferingReasoner, COMMAND_LOAD_FILE);
        }

        /**
         * Create a new References to an ontology loaded from file with the Fact reasoner.
         * @param referenceName the unique identifier of this ontology reference to store in {@link OWLReferencesContainer#allReferences}.
         * @param filePath path to the file to load (used by default for saving too).
         * @param ontologyPath the semantic IRI path of the ontology to be created
         * @param bufferingReasoner if {@code true}, the reasoner is triggered only when {@link #synchronizeReasoner()}
         *                          is called. Else, the reasoner is called after every change to the ontology.
         * @return a fully initialised OWL Reference object.
         */
        public static OWLReferences newOWLReferenceFromFileWithFact(String referenceName, String filePath, String ontologyPath, Boolean bufferingReasoner){
            return new OWLReferences( referenceName, filePath, ontologyPath, OWLLibrary.REASONER_QUALIFIER_FACT, bufferingReasoner, COMMAND_LOAD_FILE);
        }

        /**
         * Create a new References to an ontology loaded from file with the Snorocket reasoner.
         * @param referenceName the unique identifier of this ontology reference to store in {@link OWLReferencesContainer#allReferences}.
         * @param filePath path to the file to load (used by default for saving too).
         * @param ontologyPath the semantic IRI path of the ontology to be created
         * @param bufferingReasoner if {@code true}, the reasoner is triggered only when {@link #synchronizeReasoner()}
         *                          is called. Else, the reasoner is called after every change to the ontology.
         * @return a fully initialised OWL Reference object.
         */
        public static OWLReferences newOWLReferenceFromFileWithSnorocket(String referenceName, String filePath, String ontologyPath, Boolean bufferingReasoner){
            return new OWLReferences( referenceName, filePath, ontologyPath, OWLLibrary.REASONER_QUALIFIER_SNOROCKET, bufferingReasoner, COMMAND_LOAD_FILE);
        }


        /**
         * Create a new References to an ontology loaded from web with the default reasoner ({@link OWLReferences#setDefaultReasoner(Boolean)}).
         * @param referenceName the unique identifier of this ontology reference to store in {@link OWLReferencesContainer#allReferences}.
         * @param filePath the URL from which to load the ontology.
         * @param ontologyPath the semantic IRI path of the ontology to be created
         * @param bufferingReasoner if {@code true}, the reasoner is triggered only when {@link #synchronizeReasoner()}
         *                          is called. Else, the reasoner is called after every change to the ontology.
         * @return a fully initialised OWL Reference object.
         */
        public static OWLReferences newOWLReferenceFromWeb(String referenceName, String filePath, String ontologyPath, Boolean bufferingReasoner){
            return new OWLReferences(referenceName, filePath, ontologyPath, bufferingReasoner, COMMAND_LOAD_WEB);
        }

        /**
         * Create a new References to an ontology loaded from web with the Pellet reasoner.
         * @param referenceName the unique identifier of the created References.
         * @param filePath the URL path for load the ontology.
         * @param ontologyPath the semantic IRI path of the ontology to be created
         * @param bufferingReasoner if {@code true}, the reasoner is triggered only when {@link #synchronizeReasoner()}
         *                          is called. Else, the reasoner is called after every change to the ontology.
         * @return a fully initialised OWL Reference object.
         */
        public static OWLReferences newOWLReferenceFromWebWithPellet(String referenceName, String filePath, String ontologyPath, Boolean bufferingReasoner){
            return new OWLReferences( referenceName, filePath, ontologyPath, OWLLibrary.REASONER_QUALIFIER_PELLET, bufferingReasoner, COMMAND_LOAD_WEB);
        }

        /**
         * Create a new References to an ontology loaded from web with the Hermit reasoner.
         * @param referenceName the unique identifier of the created References.
         * @param filePath the URL path for load the ontology.
         * @param ontologyPath the semantic IRI path of the ontology to be created
         * @param bufferingReasoner if {@code true}, the reasoner is triggered only when {@link #synchronizeReasoner()}
         *                          is called. Else, the reasoner is called after every change to the ontology.
         * @return a fully initialised OWL Reference object.
         */
        public static OWLReferences newOWLReferenceFromWebWithHermit(String referenceName, String filePath, String ontologyPath, Boolean bufferingReasoner){
            return new OWLReferences( referenceName, filePath, ontologyPath, OWLLibrary.REASONER_QUALIFIER_HERMIT, bufferingReasoner, COMMAND_LOAD_WEB);
        }

        /**
         * Create a new References to an ontology loaded from web with the Fact reasoner.
         * @param referenceName the unique identifier of the created References.
         * @param filePath the URL path for load the ontology.
         * @param ontologyPath the semantic IRI path of the ontology to be created
         * @param bufferingReasoner if {@code true}, the reasoner is triggered only when {@link #synchronizeReasoner()}
         *                          is called. Else, the reasoner is called after every change to the ontology.
         * @return a fully initialised OWL Reference object.
         */
        public static OWLReferences newOWLReferenceFromWebWithFact(String referenceName, String filePath, String ontologyPath, Boolean bufferingReasoner){
            return new OWLReferences( referenceName, filePath, ontologyPath, OWLLibrary.REASONER_QUALIFIER_FACT, bufferingReasoner, COMMAND_LOAD_WEB);
        }

        /**
         * Create a new References to an ontology loaded from web with the Snorocket reasoner.
         * @param referenceName the unique identifier of the created References.
         * @param filePath the URL path for load the ontology.
         * @param ontologyPath the semantic IRI path of the ontology to be created
         * @param bufferingReasoner if {@code true}, the reasoner is triggered only when {@link #synchronizeReasoner()}
         *                          is called. Else, the reasoner is called after every change to the ontology.
         * @return a fully initialised OWL Reference object.
         */
        public static OWLReferences newOWLReferenceFromWebWithSnorocket(String referenceName, String filePath, String ontologyPath, Boolean bufferingReasoner){
            return new OWLReferences( referenceName, filePath, ontologyPath,  OWLLibrary.REASONER_QUALIFIER_SNOROCKET, bufferingReasoner, COMMAND_LOAD_WEB);
        }

        public static String to_string(){
            String out = "aMUR system instantiates though the class " + OWLReferencesContainer.class.getCanonicalName() + " OWL References to:" + System.getProperty( "line.separator");
            for( OWLReferencesInterface ref : allReferences.values())
                out += "\t" + ref.toString() + System.getProperty("line.separator");
            return out + ".";
        }
    }

    /**
     * Project: aMOR <br>
     * File: .../src/aMOR.owlInterface/OWLReferencesInterface.java <br>
     *
     * @author Buoncompagni Luca <br><br>
     * DIBRIS emaroLab,<br>
     * University of Genoa. <br>
     * Feb 10, 2016 <br>
     * License: GPL v2 <br><br>
     *
     * <p>
     * This class implements {@link ReasonerExplanator#ReasonerExplanator(OWLLibrary)}
     * only for the Pellet reasoner.
     * </p>
     *
     * @version 2.0
     */

    public class PelletReasonerExplanation extends ReasonerExplanator {
        /**
         * The References to the ontology whose inconsistencies should be explained
         */
        private OWLReferencesInterface ontoRef;

        /**
         * This method instantiate this class without initializing
         * {@link #getOwlLibrary()}. Anyway, it saves this object in
         * a more general instance of {@link OWLReferencesInterface}
         *
         * @param ontoRef the OWL References to the ontology whose inconsistencies should be explained.
         */
        protected PelletReasonerExplanation(OWLReferencesInterface ontoRef) {
            super(null);
            this.ontoRef = ontoRef;
        }

        /**
         * It uses Manchester syntax to explain possible inconsistencies.
         *
         * @return an inconsistency explanation as a string of text.
         * @see ReasonerExplanator#getExplanation()
         */
        @Override
        protected String getExplanation() {
            /*
            // should throw org.semanticweb.owlapi.reasoner.InconsistentOntologyException
            PelletExplanation.setup();
            logger.addDebugString("%%%%%%%%%%%%%%%%%%%%%%  INCONSISTENCY  " + ontoRef.getReferenceName() + " %%%%%%%%%%%%%%%%%%%%%%%%%%");
            try {
                // The renderer is used to pretty print explanation
                ManchesterSyntaxExplanationRenderer renderers = new ManchesterSyntaxExplanationRenderer();
                // The writer used for the explanation rendered
                StringWriter out = new StringWriter();
                renderers.startRendering( out );
                // Create an explanation generator
                PelletExplanation expGen = new PelletExplanation( ontoRef.getOWLOntology(), false);//pelletReasoners );
                Set<Set<org.semanticweb.owlapi.model.OWLAxiom>> explanation = expGen.getInconsistencyExplanations();

                renderers.render( explanation );
                renderers.endRendering();
                return( ontoRef + "is consistent? " + ontoRef.isConsistent() + " explanation: " + out.toString());
            }catch( Exception e){
                return(  ontoRef + "is not consistent. No message was give from the reasoner, an error occurs during explanation retrieves. " + e.getCause());
            }
            */
            return ("pellet explanation to be migrated");
        }

        @Override
        protected void notifyInconsistency() {
            String explanation = getExplanation();
            this.getLogger().addDebugString(explanation, true);
        }
    }
}
