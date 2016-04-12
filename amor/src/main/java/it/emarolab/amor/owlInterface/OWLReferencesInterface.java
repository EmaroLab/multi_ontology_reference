package it.emarolab.amor.owlInterface;

import java.io.File;
import java.io.StringWriter;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.coode.owlapi.manchesterowlsyntax.ManchesterOWLSyntaxOntologyFormat;
import org.semanticweb.owlapi.io.OWLObjectRenderer;
import org.semanticweb.owlapi.io.StreamDocumentTarget;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyChange;
import org.semanticweb.owlapi.model.OWLOntologyFormat;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import org.semanticweb.owlapi.reasoner.OWLReasoner;

import uk.ac.manchester.cs.owlapi.dlsyntax.DLSyntaxObjectRenderer;

import com.clarkparsia.owlapi.explanation.PelletExplanation;
import com.clarkparsia.owlapi.explanation.io.manchester.ManchesterSyntaxExplanationRenderer;

import it.emarolab.amor.owlDebugger.Logger;
import it.emarolab.amor.owlDebugger.Logger.LoggerFlag;

// TODO : serialisation, toString. equals
// TODO : test load from web
// TODO : check different reasoners
// TODO : javadoc
// TODO : method for deserailisation
// TODO : OWLReferencesContainer SHOULD BE HERE!!!!


/**
 * Project: OWLHelper <br>
 * File: .../src/owlInterface/OWLLibrary.java <br>
 *  
 * @author Buoncompagni Luca <br><br>
 * DIBRIS emaroLab,<br> 
 * University of Genoa. <br>
 * Feb 9, 2016 <br>
 * License: GPL v2 <br><br>
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

	private Boolean bufferingReasoner;
	// object used by the OWL References Container
	private String referenceName;

	private int usedCommand;
	private String filePath;
	private String ontologyPath;

	private Boolean consistent = true;

	private ReasonerExplanator reasonerExplanator;

	private OWLManipulator manipulator;
	private OWLEnquirer enquirer;

	private  final static OWLObjectRenderer renderer = new DLSyntaxObjectRenderer();

	/**
	 * This object is used to log informations about the instances of this class.
	 * The logs can be activated by setting the flag: {@link LoggerFlag#LOG_REFERENCES_INTERFACE}
	 */
	private Logger logger = new Logger( this, LoggerFlag.LOG_REFERENCES_INTERFACE);
	
	private static Logger staicLogger = new Logger( OWLReferencesInterface.class, LoggerFlag.LOG_REFERENCES_INTERFACE);

	// [[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[   CONSTRUCTORS   ]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]
	/**
	 * Create a new references to an ontology using the standard reasoner (given by {@link #getDefaultReasoner(Boolean)}).
	 * @param referenceName the unique identifier of this ontology references. This is the key with which this instance
	 * is stored in the system map {@link OWLReferencesContainer#allReferences}
	 * @param filePath the file path (or URL) to the ontology.
	 * @param ontologyPath the IRI path of the ontology. 
	 * @param bufferingReasoner {@code true} if the reasoner have to evaluate changes to the ontology as soon as their have been performed.
	 * {@code false} if the reasoner should evaluated all the changes of the ontology only if the method {@link #synchroniseReasoner()} gets called. 
	 * @param command specifying if the ontology should be created, loaded from file or from web. Possible value of {@code commands} are:
	 * {@link OWLReferencesContainer#COMMAND_CREATE}, {@link OWLReferencesContainer#COMMAND_LOAD_FILE} or 
	 * {@link OWLReferencesContainer#COMMAND_LOAD_WEB}.
	 */
	protected OWLReferencesInterface( String referenceName, String filePath, String ontologyPath, Boolean bufferingReasoner, Integer command){
		super(); // just create OWL library it does not initialise it. Remember to set iriFilePath and iriOntologyPath before to do enything else.
		// track this instance of this class
		if( referenceName != null)
			initialiser( referenceName, filePath, ontologyPath, null, true, command);
		else logger.addDebugString( "Cannot initialise an OWL References with null name.", true);
	}
	/**
	 * Create a new references to an ontology with the given specifications.
	 * @param referenceName the unique identifier of this ontology references. This is the key with which this instance
	 * is stored in the system map {@link OWLReferencesContainer#allReferences}
	 * @param filePath the file path (or URL) to the ontology.
	 * @param ontologyPath the IRI path of the ontology.
	 * @param reasonerFactory the reasoner factory qualifier used to instance the reasoner assigned to the ontology refereed by this class. 
	 * If this parameter is {@code null} the default reasoner type is given by the method {@link #getDefaultReasoner(Boolean)}.
	 * The values of this parameter have to be in the range: [{@link OWLLibrary#REASONER_QUALIFIER_PELLET},
	 * {@link OWLLibrary#REASONER_QUALIFIER_HERMIT}, {@link OWLLibrary#REASONER_QUALIFIER_SNOROCKET} or {@link OWLLibrary#REASONER_QUALIFIER_FACT}]. 
	 * @param bufferingReasoner {@code true} if the reasoner have to evaluate changes to the ontology as soon as their have been performed.
	 * {@code false} if the reasoner should evaluated all the changes of the ontology only if the method {@link #synchroniseReasoner()} gets called. 
	 * @param command specifying if the ontology should be created, loaded from file or from web. Possible value of {@code commands} are:
	 * {@link OWLReferencesContainer#COMMAND_CREATE}, {@link OWLReferencesContainer#COMMAND_LOAD_FILE} or 
	 * {@link OWLReferencesContainer#COMMAND_LOAD_WEB}.
	 */
	protected OWLReferencesInterface( String referenceName, String filePath, String ontologyPath, String reasonerFactory, Boolean bufferingReasoner, Integer command) {
		super(); // just create OWL library it does not initialise it. Remember to set iriFilePath and iriOntologyPath before to do enything else.
		// track this instance of this class
		if( referenceName != null)
			initialiser( referenceName, filePath, ontologyPath, reasonerFactory, bufferingReasoner, command);
		else logger.addDebugString( "Cannot initialise an OWL References with null name.", true);
	}

	/**
	 * This method implements the common initialisation procedure called by all the constructors:
	 * {@link #OWLReferencesInterface(OWLReferencesSerializable)}, {@link #OWLReferencesInterface(String, String, String, int)} and
	 * {@link #OWLReferencesInterface(String, String, String, OWLReasoner, Boolean, int)}. 
	 * Last but not the least it calls {@link OWLReferencesContainer#addInstance(OWLReferencesInterface)} in order
	 * to add this References to the complete static map of the system. 
	 * @param referenceName the unique identifier of this ontology references. This is the key with which this instance
	 * is stored in the system map {@link OWLReferencesContainer#allReferences}
	 * @param filePath the file path (or URL) to the ontology.
	 * @param ontologyPath the IRI path of the ontology.
	 * @param reasonerFactory the reasoner factory qualifier used to instance the reasoner assigned to the ontology refereed by this class. 
	 * If this parameter is {@code null} the default reasoner type is given by the method {@link #getDefaultReasoner(Boolean)}.
	 * The values of this parameter have to be in the range: [{@link OWLLibrary#REASONER_QUALIFIER_PELLET},
	 * {@link OWLLibrary#REASONER_QUALIFIER_HERMIT}, {@link OWLLibrary#REASONER_QUALIFIER_SNOROCKET} or {@link OWLLibrary#REASONER_QUALIFIER_FACT}]. 
	 * @param bufferingReasoner {@code true} if the reasoner have to evaluate changes to the ontology as soon as their have been performed.
	 * {@code false} if the reasoner should evaluated all the changes of the ontology only if the method {@link #synchroniseReasoner()} gets called.
	 * @param command specifying if the ontology should be created, loaded from file or from web. Possible value of {@code commands} are:
	 * {@link OWLReferencesContainer#COMMAND_CREATE}, {@link OWLReferencesContainer#COMMAND_LOAD_FILE} or 
	 * {@link OWLReferencesContainer#COMMAND_LOAD_WEB}.
	 */
	private synchronized void initialiser( String referenceName, String filePath, String ontologyPath, String reasonerFactory, Boolean bufferingReasoner, Integer command){
		this.referenceName = referenceName; // set the unique identifier of this object
		if(  OWLReferencesContainer.addInstance( this)){ // add this class to the static map
			long initialTime = System.nanoTime();
			// set internal variables
			this.filePath = filePath; 
			this.ontologyPath = ontologyPath;
			this.setIriOntologyPath( IRI.create( ontologyPath));
			this.usedCommand = command;
			// take an ontology opening action by considering its vale
			switch( command){
			case 0: //OWLReferencesContainer.COMMAND_CREATE
				this.setIriFilePath( IRI.create( filePath));
				this.setManager(); // creates and sets the filed that you can retrieve from getManager(); 
				this.createOntology(); // creates and set the field taht you can retrieve from getOntology();
				break;
			case 1: //OWLReferencesContainer.COMMAND_LOAD_FILE
				this.setIriFilePath( IRI.create( new File( filePath)));
				this.setManager(); // creates and sets the filed that you can retrieve from getManager();
				this.loadOntologyFromFile(); // creates and set the field taht you can retrieve from getOntology();
				break;
			case 2: //OWLReferencesContainer.COMMAND_LOAD_WEB
				this.setIriFilePath( IRI.create( filePath)); // in this case the file path should be a WEB URL
				this.setManager(); // creates and sets the filed that you can retrieve from getManager();
				this.loadOntologyFromWeb(); // creates and set the field taht you can retrieve from getOntology();
				break;
			default : logger.addDebugString( "Cannot initialise OWL References with the given command: " + command, true);
			}
			// now that the manager and the ontology is initialise, create other owl api objects
			this.setFactory(); // creates and sets the field that you can retrieve from getFactory();
			this.setPrefixFormat(); // creates and sets the field that you can retrieve from getPrefixForamt();

			if( reasonerFactory == null || reasonerFactory.equals( OWLLibrary.REASONER_DEFAULT)) 
				setDefaultReasoner( bufferingReasoner); // actually it Initialise  pellet as reasoner
			else {
				this.setReasoner( reasonerFactory, bufferingReasoner, referenceName);
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
	 * This method is called when the {@code reasonerFactory} is null or not given on constructor.
	 * It creates a new instance of the reasoner and set it to this References.
	 * By default, it calls the Pellet reasoner through {@link OWLLibrary#createPelletReasoner(OWLReferencesInterface, boolean)}.<br>
	 * Last but not the least, consider that this is the only calls that initialise the Pellet explanator object.
	 * @param buffering set to {@code true} if the changes on the ontology should be buffered and
	 * the effects will be applied by calling {@link #factory. Set to {@code false} if the reasoner has to
	 * consider the changes on the ontology as soon as they are performed. 
	 */
	protected void setDefaultReasoner( Boolean buffering){ 
		this.setPelletReasoner( buffering, referenceName);
		//resonerFactory = OWLLibrary.REASONER_QUALIFIER_PELLET; // used for serialisation
		setPelletReasonerExplanator();
	}
	
	/**
	 * This method set the default reasoner explanator object for pellet.
	 * To be called in {@link #initialiser(String, String, String, String, Boolean, Integer)}
	 * and {@link #setDefaultReasoner(Boolean)}.
	 */
	private void setPelletReasonerExplanator(){
		this.setReasonerExplanator( new PelletReasonerExplanation( this));
	}

	/**
	 * @param reasonerExplanator the reasoner Explanator implementation to set.
	 */
	protected synchronized void setReasonerExplanator(	ReasonerExplanator reasonerExplanator) {
		this.reasonerExplanator = reasonerExplanator;
	}

	/**
	 * This method calls {@link OWLReferencesContainer#removeInstance(OWLReferencesInterface)}
	 * in order to remove it from the complete static system map when this object gets finalised.
	 * @see java.lang.Object#finalize()
	 */
	@Override
	public synchronized void finalize() throws Throwable {
		OWLReferencesContainer.removeInstance( this);
		super.finalize();
	}

	// [[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[   GETTERS   ]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]
	/**
	 * @return the referenceName of this instance used by {@link OWLReferencesContainer}.
	 * This is initialised during constructor and it can be used to retrieve a pointer to an  
	 * already instantiated ontology reference. This name should be considered as an ontology
	 * unique qualifier.
	 */
	public synchronized String getReferenceName() {
		return referenceName;
	}
	/**
	 * @return the directory path (or the URL) to the file that contains the ontology
	 * given on constructor.
	 */
	public synchronized String getFilePath() {
		return filePath;
	}
	/**
	 * @return the ontology semantic path given on constructor.
	 */
	public synchronized String getOntologyPath() {
		return ontologyPath;
	}
	/**
	 * @return the command used on constructor. It range of values are: 
	 * [{@link OWLReferencesContainer#COMMAND_CREATE}, {@link OWLReferencesContainer#COMMAND_LOAD_FILE}, 
	 * {@link OWLReferencesContainer#COMMAND_LOAD_WEB}].
	 */
	public synchronized int getUsedCommand() {
		return usedCommand;
	}
	/**
	 * @return the value of the reasoner buffering flag used on constructor.
	 */
	public synchronized Boolean useBufferingReasoner(){ 
		return bufferingReasoner;
	}	
	/**
	 * @return the object that can explain the inconsistency of the reasoner.
	 * It is only implemented for Pellet and it is initialised only if the default reasoner is used.
	 */
	public synchronized ReasonerExplanator getReasonerExplanator(){
		return reasonerExplanator;
	}
	
	/**
	 * @return the object able to manipulate the ontology with not thread save implementation.
	 * This object should not be used from an aMOR user. Use {@link OWLReferences} instead.
	 */
	protected synchronized OWLManipulator getOWLManipulator(){
		return this.manipulator;
	}
	/**
	 * @param willBufferise the flag to set in the {@link OWLManipulator#setChangeBuffering(Boolean)}
	 * in order to change the buffering (or not) behaviour for apply changes into the ontology.
	 */
	public synchronized void setOWLManipulatorBuffering( Boolean willBufferise){
		manipulator.setChangeBuffering( willBufferise);
	}
	/**
	 * This method calls {@link OWLManipulator#applyChanges()}. This will apply all the
	 * changes stored in a buffering manipulator.
	 */
	public synchronized void applyOWLManipulatorChanges(){
		manipulator.applyChanges();
	}
	/**
	 * This method calls {@link OWLManipulator#applyChanges(OWLOntologyChange)}.
	 * @param addAxiom the axiom to be applied into the ontology
	 */
	public synchronized void applyOWLManipulatorChanges(OWLOntologyChange addAxiom){
		manipulator.applyChanges(addAxiom);
	}
	/**
	 * This method calls {@link OWLManipulator#applyChanges(List)}.
	 * @param addAxiom the list of axioms to be applied into the ontology
	 */
	public synchronized void applyOWLManipulatorChanges(List<OWLOntologyChange> addAxiom){
		manipulator.applyChanges(addAxiom);
	}
	
	/**
	 * This method calls {@link OWLManipulator#applyChanges(List)} with the input
	 * parameter computed with {@link OWLManipulator#getAddAxiom(OWLAxiom)}.
	 * @param addAxiom the axiom to be added and applyed to the ontology.
	 * Dependign from {@link OWLManipulator#isChangeBuffering()} flag those changes would be
	 * added (or not) to the interal manipulator buffer.
	 */
	public synchronized void applyOWLManipulatorChangesAddAxiom( OWLAxiom addAxiom){
		manipulator.applyChanges( manipulator.getAddAxiom( addAxiom));
	}
	/**
	 * This method calls {@link OWLManipulator#applyChanges(List)} with the input
	 * parameter computed with {@link OWLManipulator#getRemoveAxiom(OWLAxiom)}.
	 * @param addAxiom the axiom to be removed and applyed to the ontology.
	 * Dependign from {@link OWLManipulator#isChangeBuffering()} flag those changes would be
	 * added (or not) to the interal manipulator buffer.
	 */
	public synchronized void applyOWLManipulatorChangesRemoveAxiom( OWLAxiom removeAxiom){
		manipulator.applyChanges( manipulator.getRemoveAxiom( removeAxiom));
	}
	
	
	/**
	 * @return the object able to query the ontology with not thread save implementation.
	 * This object should not be used from an aMOR user. Use {@link OWLReferences} instead.
	 */
	protected synchronized OWLEnquirer getOWLEnquirer(){
		return this.enquirer;
	}
	/**
	 * @return {@code true} if the enquirer would return all the sup/super
	 * properties up to the leaf/root of the structure.
	 * {@code false} it would return only the frist direct assertion.
	 */
	public synchronized Boolean getOWLEnquirerCompletenessFlag(){
		return this.enquirer.isReturningCompleteDescription();
	}
	/**
	 * @param flag set to {@code true} if the enquirer should return all the sup/super
	 * properties up to the leaf/root of the structure.
	 * {@code false} it would return only the frist direct assertion.
	 */
	public synchronized void setOWLEnquirerCompletenessFlag( Boolean flag){
		this.enquirer.setReturningCompleteDescrription( flag);
	}
	

	// [[[[[[[[[[[[[[[[[[[[[[   METHODS TO CALL REASONING   ]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]
	/**
	 * If the Ontology is consistent it will synchronise a buffering reasoner
	 * calling {@code reasoner.flush()}; if the reasoner has a false buffering 
	 * flag, than this method has no effects. If an inconsistency error 
	 * occurs than this method will print over console an explanation of the 
	 * error. Such an explanation interface can be implemented with the {@link ReasonerExplanator} interface.
	 * Note that if the ontology is inconsistent than all the methods
	 * in this class may return a null value.
	 * Moreover, consider that this methods calls {@link OWLManipulator#applyChanges()}
	 * in order to synchronise the buffer before to reason.
	 * Last but not the least note that for a buffering reasoner you can use
	 * manually the method {@link #checkConsistent()}
	 */
	public synchronized void synchroniseReasoner(){
		if( this.isConsistent()){
			try{
				Long initialTime = System.nanoTime(); 
				this.getOWLManipulator().applyChanges(); // be sure to empty the buffer (if any)
				this.callReasoning( initialTime);
				if( !this.checkConsistent())
					this.loggInconsistency();
			} catch( org.semanticweb.owlapi.reasoner.InconsistentOntologyException e){
				this.loggInconsistency();
			}
		} else {
			this.checkConsistent();
		}
	}
	/**
	 * If the Ontology is consistent it will synchronise a buffering reasoner
	 * calling {@code reasoner.flush()}; if the reasoner has a false buffering 
	 * flag, than this method has no effects. If an inconsistency error 
	 * occurs than this method will print over console an explanation of the 
	 * error. Such an explanation interface can be implemented with the {@link ReasonerExplanator} interface.
	 * Note that if the ontology is inconsistent than all the methods
	 * in this class may return a null value.
	 * Moreover, consider that this methods calls {@link OWLManipulator#applyChanges( List)}
	 * in order to use a custom buffer of ontology changes before to reason (see {@link OWLManipulator}).
	 * Last but not the least note that for a buffering reasoner you can use
	 * manually the method {@link #checkConsistent()}
	 * @param changesBuffer the ontological changes to add and reason about.
	 */
	public synchronized void synchroniseReasoner( List< OWLOntologyChange> changesBuffer){
		if( this.isConsistent()){
			try{
				Long initialTime = System.nanoTime(); 
				this.getOWLManipulator().applyChanges( changesBuffer);
				this.callReasoning( initialTime);
				if( !this.checkConsistent())
					this.loggInconsistency();
			} catch( org.semanticweb.owlapi.reasoner.InconsistentOntologyException e){
				this.loggInconsistency();
			}
		} else {
			this.checkConsistent();
		}
	}
	/**
	 * @return the consistency flag for this OWL References
	 */
	public synchronized boolean isConsistent() {
		return consistent;
	}
	/**
	 * call the reasoner to check ontology consistency and synchronises the internal flag of this class.
	 * Finally it returns such a flag.
	 * It return the results of {@link #isConsistent()} after the check.
	 */
	protected synchronized boolean checkConsistent() {
		consistent = this.getReasoner().isConsistent();
		return consistent;
	}

	/**
	 * This method check if the {@link #reasonerExplanator} is initialised. And if yes calls {@link OWLLibrary.ReasonerExplanation#notifyInconsistency()}.
	 * Otherwise, it log an error on the console by saying that an inconsistency occurs.
	 */
	protected synchronized void loggInconsistency(){
		if( reasonerExplanator != null)
			reasonerExplanator.notifyInconsistency();
		else logger.addDebugString( "The ontology is not consistent but the system does not provide any ReasonerExplanation implementation.", true);
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
	 * This class implements {@link OWLLibrary.ReasonerExplanation}
	 * only for the Pellet reasoner.
	 * </p>
	 * 
	 * @see 
	 *
	 * 
	 * @version 2.0
	 */
	public class PelletReasonerExplanation extends ReasonerExplanator{
		/**
		 * The References to the ontology from which explain inconsistency
		 */
		private OWLReferencesInterface ontoRef;

		/**
		 * This method instantiate this class without initialise
		 * {@link #getOwlLibrary()}. Anyway, it saves in this object
		 * a more general instance of {@link OWLReferencesInterface}
		 * @param ontoRef the OWL References to the ontology to be used
		 * for explain the consistency.
		 */
		protected PelletReasonerExplanation(OWLReferencesInterface ontoRef) {
			super( null); 
			this.ontoRef = ontoRef;
		} 

		/**
		 * It uses Manchester syntax to explain possible inconsistencies.
		 * @return an inconsistency explanation as a string of text.
		 * @see aMOR.owlInterface.OWLLibrary.ReasonerExplanation#getExplanation()
		 */
		@Override
		protected String getExplanation() {
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
				PelletExplanation expGen = new PelletExplanation( ontoRef.getOntology(), false);//pelletReasoners );
				Set<Set<org.semanticweb.owlapi.model.OWLAxiom>> explanation = expGen.getInconsistencyExplanations();

				renderers.render( explanation );
				renderers.endRendering();
				return( ontoRef + "is consistent? " + ontoRef.isConsistent() + " explanation: " + out.toString());
			}catch( Exception e){
				return(  ontoRef + "is not consistent. No message was give from the reasoner, an error occurs during explanation retrieves. " + e.getCause());
			}
		}

		@Override
		protected void notifyInconsistency() {
			String explanation = getExplanation();
			this.getLogger().addDebugString( explanation, true);
		} 
	}

	// [[[[[[[[[[[[[[[[[[[[[[   METHODS TO PARSE ONTOLOGY NAMES   ]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]
	/**
	 * Its purposes is to be used when an entity of the ontology can
	 * have only one element by semantic specifications. In particular, this method
	 * returns {@code null} {@code if( set.size() == 0)}. Otherwise it will iterate over 
	 * the set and return just the first value. Note that a set does not
	 * guarantee that its order is always the same.
	 * @param set a generic set of object
	 * @return an element of the set
	 */
	public synchronized Object getOnlyElement( Set< ?> set){
		if( set != null){
			for( Object i : set){
				return( i);
			}
		} 
		logger.addDebugString("get only elements cannot work with an null or empty set", true);
		return( null); 
	}
	/**
	 * It uses a render defined as {@code OWLObjectRenderer renderer = new DLSyntaxObjectRenderer();}
	 * to get the name of an ontological object from its IRI path. 
	 * It returns null if the input parameter is {@code null}.
	 * This is jsut a not static interface to the method {@link #getOWLName(OWLObject)}.
	 * @param o the object for which get the ontological name 
	 * @return the name of the ontological object given as input parameter.
	 */
	public synchronized String getOWLObjectName( OWLObject o){
		return getOWLName( o);
	}
	/**
	 * It uses a render defined as {@code OWLObjectRenderer renderer = new DLSyntaxObjectRenderer();}
	 * to get the name of an ontological object from its IRI path. 
	 * It returns null if the input parameter is {@code null}.
	 * @param o the object for which get the ontological name 
	 * @return the name of the ontological object given as input parameter.
	 */
	public synchronized static String getOWLName( OWLObject o){
		if( o != null)
			return( renderer.render( o));
		staicLogger.addDebugString( "Cannot get the OWL name of a null OWL object", true);
		return( null);
	}
	/**
	 * It uses a render defined as {@code OWLObjectRenderer renderer = new DLSyntaxObjectRenderer();}
	 * to get the name of a set of ontological objects from its IRI path. 
	 * It returns null if the input parameter is {@code null}.
	 *  This is jsut a not static interface to the method {@link #getOWLName(Set)}.
	 * @param objects the set of objects for which get the ontological name 
	 * @return the name of the ontological objects given as input parameters.
	 */
	public synchronized Set<String> getOWLObjectName( Set< ?> objects){
		return getOWLName( objects);
	}
	/**
	 * It uses a render defined as {@code OWLObjectRenderer renderer = new DLSyntaxObjectRenderer();}
	 * to get the name of a set of ontological objects from its IRI path. 
	 * It returns null if the input parameter is {@code null}.
	 * @param objects the set of objects for which get the ontological name 
	 * @return the name of the ontological objects given as input parameters.
	 */
	public synchronized static Set<String> getOWLName( Set< ?> objects){
		Set< String> out = new HashSet< String>();
		for( Object o : objects){
			if( o instanceof OWLObject){
				OWLObject owlObj = ( OWLObject) o;
				out.add( getOWLName( owlObj));
			}else staicLogger.addDebugString( "Cannot get the name of a non OWL object. Given entity: " + o, true);
		}
		return out;
	}


	// [[[[[[[[[[[[[[[[[[[[[[   METHODS TO SAVE (print) ONTOLOGY   ]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]
	/**
	 * Save the refereed Ontology by using the path stored on {@link #getIriFilePath()}.
	 */
	public synchronized void saveOntology(){
		try {
			File file = new File( this.getIriFilePath().toString());
			this.getManager().saveOntology( this.getOntology(), IRI.create( file.toURI()));
			logger.addDebugString( "Ontology References: " + this + " saved on path: " + this.getIriFilePath().toString());
		} catch (OWLOntologyStorageException e) {
			e.printStackTrace();
			logger.addDebugString( "Error on saving the ontology: " + this + " on path: " + this.getIriFilePath(), true);
		}
	}
	/**
	 * Save the refereed Ontology by using the path given as input argument.
	 * @param filePath the directory path in which save the ontology
	 */
	public synchronized void saveOntology( String filePath){
		try {
			File file = new File( filePath);
			this.getManager().saveOntology( this.getOntology(), IRI.create( file.toURI()));
			logger.addDebugString( "Ontology References: " + this + " saved on path: " + filePath);
		} catch (OWLOntologyStorageException e) {
			e.printStackTrace();
			logger.addDebugString( "Error on saving the ontology: " + this + " on path: " + this.getIriFilePath(), true);
		}
	}
	/**
	 * It prints the ontology on the java console using Manchester formatting. 
	 */
	public void printOntonolyOnConsole() {
		try{
			long initialTime = System.nanoTime();
			ManchesterOWLSyntaxOntologyFormat manSyntaxFormat = new ManchesterOWLSyntaxOntologyFormat();
			OWLOntologyManager man = this.getManager();
			OWLOntology ont = this.getOntology();
			OWLOntologyFormat format = man.getOntologyFormat(ont);
			if (format.isPrefixOWLOntologyFormat())
				manSyntaxFormat.copyPrefixesFrom(format.asPrefixOWLOntologyFormat());
			logger.addDebugString("---------  PRINTING ONTOLOGY --------");
			man.saveOntology( ont, manSyntaxFormat, new StreamDocumentTarget( System.out));
			logger.addDebugString( "ontology printed in console in: " + (System.nanoTime() - initialTime) + " [ns]");
		} catch( OWLOntologyStorageException e){
			e.printStackTrace();
		} catch( org.semanticweb.owlapi.reasoner.InconsistentOntologyException e){
			this.loggInconsistency();
		}
	}


	/**
	 * Project: OWLHelper <br>
	 * File: .../src/owlInterface/OWLLibrary.java <br>
	 *  
	 * @author Buoncompagni Luca <br><br>
	 * DIBRIS emaroLab,<br> 
	 * University of Genoa. <br>
	 * Feb 9, 2016 <br>
	 * License: GPL v2 <br><br>
	 *  
	 * <p>
	 * This class implements method to instantiate or retrieve a reference to an ontology.<br>
	 * In particular, this is done by keeping track of all the created instances of the extending class
	 * of {@link OWLReferencesInterface} in a static map ({@link #allReferences}). 
	 * Which links each instance with respect to a name {@code ontoName} given on construction 
	 * as an unique ontology qualifier. 
	 * </p>
	 * 
	 * @see 
	 *
	 * 
	 * @version 2.0
	 */
	public static class OWLReferencesContainer{
		
		// [[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[   CLASS CONSTANTS   ]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]
		/**
		 * Command value that specifies that the new ontology references has to point to a new ontology.
		 * In particular, its value is: {@value #COMMAND_CREATE}.  
		 */
		static public final Integer COMMAND_CREATE = 0;
		/**
		 * Command value that specifies that the new ontology references has to point to an ontology file.
		 * In particular, its value is: {@value #COMMAND_LOAD_FILE}.
		 */
		static public final Integer COMMAND_LOAD_FILE = 1;
		/**
		 * Command value that specifies that the new ontology references has to point to an ontology stored in the web.
		 * In particular, its value is: {@value #COMMAND_LOAD_WEB}.
		 */
		static public final Integer COMMAND_LOAD_WEB = 2;



		// [[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[   CLASS PRIVATE FIELDS   ]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]
		/**
		 * This static map contains all the OWL references instantiate by the system through this class. 
		 * More in particular, this map is an instance of {@link ConcurrentHashMap}, please see its specification
		 * for synchronisation on a thread save system.  
		 */
		private static Map<String, OWLReferencesInterface> allReferences = new ConcurrentHashMap<String, OWLReferencesInterface>();

		/**
		 * This object is used to log informations about the owl references managed by this container class.
		 * The logs can be activated by setting the flag: {@link LoggerFlag#LOG_REFERENCES_CONTAINER}
		 */
		private static Logger logger = new Logger( OWLReferencesContainer.class, LoggerFlag.LOG_REFERENCES_CONTAINER);



		// [[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[   METHODS TO MANAGE THE MAP   ]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]
		/**
		 * This method adds an instance to the internal map {@link #allReferences}. 
		 * This procedure is automatically managed by using the creating procedure implemented by this class.
		 * @param instance the new OWL reference to be add to the internal map
		 * @return {@code false} if the map already contains an object with name {@link OWLReferencesInterface#getReferenceName()}.
		 * This implies that the {@code instance} is not added to the map. {@code true} otherwise.
		 */
		private static Boolean addInstance( OWLReferencesInterface instance){
			String refName = instance.getReferenceName();
			if( ! isInstance( refName)){
				allReferences.putIfAbsent( refName, instance);
				return( true);
			}
			logger.addDebugString( "Exception: cannot create another Ontology with referencing name : " + refName, true);
			return( false);
		}	
		/**
		 * This method remove an instance to the internal map {@link #allReferences}.
		 * This procedure is automatically managed by using the {@link OWLReferencesInterface#finalize()} method.
		 * @param instance the OWL reference to be removed from the internal map
		 * @return {@code false} if the map does not contain an object with name {@link OWLReferencesInterface#getReferenceName()}.
		 * This implies that the {@code instance} is not removed from the map. {@code true} otherwise.
		 */
		private static Boolean removeInstance( OWLReferencesInterface instance){
			String refName = instance.getReferenceName();
			if( isInstance( refName)){
				allReferences.remove( refName);
				return( true);
			}
			logger.addDebugString( "Exception: cannot remve an Ontology with referencing name : " + refName, true);
			return( false);
		}

		/**
		 * @param instance the OWL interfacing object to check.
		 * @return {@code true} if the internal map {@link #allReferences} 
		 * contains this instance. It will return {@code false} otherwise.
		 */
		public static Boolean isInstance( OWLReferencesInterface instance){
			return isInstance( instance.getReferenceName());
		}
		/**
		 * @param referenceName the referencing name to the OWL interfacing object to check.
		 * @return {@code true} if the internal map {@link #allReferences} 
		 * contains this instance. It will return {@code false} otherwise.
		 */
		public static Boolean isInstance( String referenceName){
			return allReferences.containsKey( referenceName);
		}
		/**
		 * Return a particular OWL references, given its name {@link OWLReferencesInterface#getReferenceName()}. 
		 * Basically this is done by just calling: 
		 * {@code return( this.getAllInstances().get(referenceName))}.
		 * 
		 * @param referenceName the referring name to a particular instance of OWL ontology references.
		 * @return the instance of this class attached to a particular name. 
		 * {@code null} if the map does not contains an object with the key equal to the {@code referenceName}.
		 */
		public static OWLReferencesInterface getOWLReferences( String referenceName){
			return( allReferences.get( referenceName));
		}
		/**
		 * @return the key set of the internal map that contains all the ontology references instances ({@link #allReferences}).
		 * This is done by just calling: {@link Map#keySet()}
		 */
		public static Set< String> getOWLReferencesKeys(){
			return allReferences.keySet();
		}
		/**
		 * @return the value set of the internal map that contains all the ontology references instances ({@link #allReferences}).
		 * This is done by just calling: {@link Map#values()}
		 */
		public static Collection< OWLReferencesInterface> getOWLReferencesValues(){
			return allReferences.values();
		}

		// [[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[   METHODS TO CREATE ONTOLOGY REFERENCES   ]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]

		/**
		 * Creates a new OWL References by calling {@link OWLReferences#OWLReferences(String, String, String, Boolean, Integer)}
		 * @param referenceName the unique identifier of this ontology references. This is the key with which this instance
		 * is stored in the system map {@link OWLReferencesContainer#allReferences}
		 * @param filePath the file path (or URL) to the ontology.
		 * @param ontologyPath the IRI path of the ontology. 
		 * @param bufferingReasoner {@code true} if the reasoner have to evaluate changes to the ontology as soon as their have been performed.
		 * {@code false} if the reasoner should evaluated all the changes of the ontology only if the method {@link #synchroniseReasoner()} gets called. 
		 * @param command specifying if the ontology should be created, loaded from file or from web. Possible value of {@code commands} are:
		 * {@link OWLReferencesContainer#COMMAND_CREATE}, {@link OWLReferencesContainer#COMMAND_LOAD_FILE} or 
		 * {@link OWLReferencesContainer#COMMAND_LOAD_WEB}.
		 * @return the created and fully initialised OWL References object
		 */
		public static OWLReferences newOWLReferences( String referenceName, String filePath, String ontologyPath, Boolean bufferingReasoner, Integer command){
			return new OWLReferences( referenceName, filePath, ontologyPath, bufferingReasoner, command);
		}


		/**
		 * Creates a new OWL References by calling {@link OWLReferences#OWLReferences(String, String, String, String, Boolean, Integer)}
		 * @param referenceName the unique identifier of this ontology references. This is the key with which this instance
		 * is stored in the system map {@link OWLReferencesContainer#allReferences}
		 * @param filePath the file path (or URL) to the ontology.
		 * @param ontologyPath the IRI path of the ontology.
		 * @param reasonerFactory the reasoner factory qualifier used to instance the reasoner assigned to the ontology refereed by this class. 
		 * If this parameter is {@code null} the default reasoner type is given by the method {@link #getDefaultReasoner(Boolean)}.
		 * The values of this parameter have to be in the range: [{@link OWLLibrary#REASONER_QUALIFIER_PELLET},
		 * {@link OWLLibrary#REASONER_QUALIFIER_HERMIT}, {@link OWLLibrary#REASONER_QUALIFIER_SNOROCKET} or {@link OWLLibrary#REASONER_QUALIFIER_FACT}]. 
		 * @param bufferingReasoner {@code true} if the reasoner have to evaluate changes to the ontology as soon as their have been performed.
		 * {@code false} if the reasoner should evaluated all the changes of the ontology only if the method {@link #synchroniseReasoner()} gets called. 
		 * @param command specifying if the ontology should be created, loaded from file or from web. Possible value of {@code commands} are:
		 * {@link OWLReferencesContainer#COMMAND_CREATE}, {@link OWLReferencesContainer#COMMAND_LOAD_FILE} or 
		 * {@link OWLReferencesContainer#COMMAND_LOAD_WEB}.
		 * @return the created and fully initialised OWL References object
		 */
		public static OWLReferences newOWLReferences( String referenceName, String filePath, String ontologyPath, String reasonerFactory, Boolean bufferingReasoner, Integer command){
			return new OWLReferences(referenceName, filePath, ontologyPath, reasonerFactory, bufferingReasoner, command);
		}


		/**
		 * Create a new References to an new empty ontology with the default reasoner ({@link OWLReferencesInterface#setDefaultReasoner(Boolean)}). 
		 * @param referenceName the unique identifier of the created References.
		 * @param filePath the directory path to the file used (by default) for saving the ontology
		 * @param ontologyPath the semantic IRI path of the ontology to be created
		 * @param bufferingReasoner {@code true} if the reasoner have to evaluate changes to the ontology as soon as their have been performed.
		 * {@code false} if the reasoner should evaluated all the changes of the ontology only if the method {@link OWLReferencesInterface#synchroniseReasoner()} gets called.
		 * @return the created and fully initialised OWL References object
		 */
		public static OWLReferences newOWLReferencesCreated( String referenceName, String filePath, String ontologyPath, Boolean bufferingReasoner){
			return new OWLReferences( referenceName, filePath, ontologyPath, bufferingReasoner, COMMAND_CREATE);
		}
		/**
		 * Create a new References to an new empty ontology with the Pellet reasoner. 
		 * @param referenceName the unique identifier of the created References.
		 * @param filePath the directory path to the file used (by default) for saving the ontology
		 * @param ontologyPath the semantic IRI path of the ontology to be created
		 * @param bufferingReasoner {@code true} if the reasoner have to evaluate changes to the ontology as soon as their have been performed.
		 * {@code false} if the reasoner should evaluated all the changes of the ontology only if the method {@link OWLReferencesInterface#synchroniseReasoner()} gets called.
		 * @return the created and fully initialised OWL References object
		 */
		public static OWLReferences newOWLReferencesCreatedWithPellet( String referenceName, String filePath, String ontologyPath, Boolean bufferingReasoner){
			return new OWLReferences(referenceName, filePath, ontologyPath, OWLLibrary.REASONER_QUALIFIER_PELLET, bufferingReasoner, COMMAND_CREATE);
		}
		/**
		 * Create a new References to an new empty ontology with the Hermit reasoner. 
		 * @param referenceName the unique identifier of the created References.
		 * @param filePath the directory path to the file used (by default) for saving the ontology
		 * @param ontologyPath the semantic IRI path of the ontology to be created
		 * @param bufferingReasoner {@code true} if the reasoner have to evaluate changes to the ontology as soon as their have been performed.
		 * {@code false} if the reasoner should evaluated all the changes of the ontology only if the method {@link OWLReferencesInterface#synchroniseReasoner()} gets called.
		 * @return the created and fully initialised OWL References object
		 */
		public static OWLReferences newOWLReferencesCreatedWithHermit( String referenceName, String filePath, String ontologyPath, Boolean bufferingReasoner){
			return new OWLReferences(referenceName, filePath, ontologyPath, OWLLibrary.REASONER_QUALIFIER_HERMIT, bufferingReasoner, COMMAND_CREATE);
		}
		/**
		 * Create a new References to an new empty ontology with the Fact reasoner. 
		 * @param referenceName the unique identifier of the created References.
		 * @param filePath the directory path to the file used (by default) for saving the ontology
		 * @param ontologyPath the semantic IRI path of the ontology to be created
		 * @param bufferingReasoner {@code true} if the reasoner have to evaluate changes to the ontology as soon as their have been performed.
		 * {@code false} if the reasoner should evaluated all the changes of the ontology only if the method {@link OWLReferencesInterface#synchroniseReasoner()} gets called.
		 * @return the created and fully initialised OWL References object
		 */
		public static OWLReferences newOWLReferencesCreatedWithFact( String referenceName, String filePath, String ontologyPath, Boolean bufferingReasoner){
			return new OWLReferences(referenceName, filePath, ontologyPath, OWLLibrary.REASONER_QUALIFIER_FACT, bufferingReasoner, COMMAND_CREATE);
		}
		/**
		 * Create a new References to an new empty ontology with the Snorocket reasoner. 
		 * @param referenceName the unique identifier of the created References.
		 * @param filePath the directory path to the file used (by default) for saving the ontology
		 * @param ontologyPath the semantic IRI path of the ontology to be created
		 * @param bufferingReasoner {@code true} if the reasoner have to evaluate changes to the ontology as soon as their have been performed.
		 * {@code false} if the reasoner should evaluated all the changes of the ontology only if the method {@link OWLReferencesInterface#synchroniseReasoner()} gets called.
		 * @return the created and fully initialised OWL References object
		 */
		public static OWLReferences newOWLReferencesCreatedWithSnorocket( String referenceName, String filePath, String ontologyPath, Boolean bufferingReasoner){
			return new OWLReferences(referenceName, filePath, ontologyPath, OWLLibrary.REASONER_QUALIFIER_SNOROCKET, bufferingReasoner, COMMAND_CREATE);
		}


		/**
		 * Create a new References to an ontology loaded from file with the default reasoner ({@link OWLReferences#setDefaultReasoner(Boolean)}). 
		 * @param referenceName the unique identifier of the created References.
		 * @param filePath the directory path for load the ontology. (Used also as a default for saving).
		 * @param ontologyPath the semantic IRI path of the ontology to be created
		 * @param bufferingReasoner {@code true} if the reasoner have to evaluate changes to the ontology as soon as their have been performed.
		 * {@code false} if the reasoner should evaluated all the changes of the ontology only if the method {@link OWLReferencesInterface#synchroniseReasoner()} gets called.
		 * @return the created and fully initialised OWL References object
		 */
		public static OWLReferences newOWLReferenceFromFile( String referenceName, String filePath, String ontologyPath, Boolean bufferingReasoner){
			return new OWLReferences( referenceName, filePath, ontologyPath, bufferingReasoner, COMMAND_LOAD_FILE);
		}
		/**
		 * Create a new References to an ontology loaded from file with the Pellet reasoner. 
		 * @param referenceName the unique identifier of the created References.
		 * @param filePath the directory path for load the ontology. (Used also as a default for saving).
		 * @param ontologyPath the semantic IRI path of the ontology to be created
		 * @param bufferingReasoner {@code true} if the reasoner have to evaluate changes to the ontology as soon as their have been performed.
		 * {@code false} if the reasoner should evaluated all the changes of the ontology only if the method {@link OWLReferencesInterface#synchroniseReasoner()} gets called.
		 * @return the created and fully initialised OWL References object
		 */
		public static OWLReferences newOWLReferenceFromFileWithPellet( String referenceName, String filePath, String ontologyPath, Boolean bufferingReasoner){
			return new OWLReferences( referenceName, filePath, ontologyPath,  OWLLibrary.REASONER_QUALIFIER_PELLET, bufferingReasoner, COMMAND_LOAD_FILE);
		}
		/**
		 * Create a new References to an ontology loaded from file with the Hermit reasoner. 
		 * @param referenceName the unique identifier of the created References.
		 * @param filePath the directory path for load the ontology. (Used also as a default for saving).
		 * @param ontologyPath the semantic IRI path of the ontology to be created
		 * @param bufferingReasoner {@code true} if the reasoner have to evaluate changes to the ontology as soon as their have been performed.
		 * {@code false} if the reasoner should evaluated all the changes of the ontology only if the method {@link OWLReferencesInterface#synchroniseReasoner()} gets called.
		 * @return the created and fully initialised OWL References object
		 */
		public static OWLReferences newOWLReferenceFromFileWithHermit( String referenceName, String filePath, String ontologyPath, Boolean bufferingReasoner){
			return new OWLReferences( referenceName, filePath, ontologyPath,  OWLLibrary.REASONER_QUALIFIER_HERMIT, bufferingReasoner, COMMAND_LOAD_FILE);
		}
		/**
		 * Create a new References to an ontology loaded from file with the Fact reasoner. 
		 * @param referenceName the unique identifier of the created References.
		 * @param filePath the directory path for load the ontology. (Used also as a default for saving).
		 * @param ontologyPath the semantic IRI path of the ontology to be created
		 * @param bufferingReasoner {@code true} if the reasoner have to evaluate changes to the ontology as soon as their have been performed.
		 * {@code false} if the reasoner should evaluated all the changes of the ontology only if the method {@link OWLReferencesInterface#synchroniseReasoner()} gets called.
		 * @return the created and fully initialised OWL References object
		 */
		public static OWLReferences newOWLReferenceFromFileWithFact( String referenceName, String filePath, String ontologyPath, Boolean bufferingReasoner){
			return new OWLReferences( referenceName, filePath, ontologyPath,  OWLLibrary.REASONER_QUALIFIER_FACT, bufferingReasoner, COMMAND_LOAD_FILE);
		}		
		/**
		 * Create a new References to an ontology loaded from file with the Snorocket reasoner. 
		 * @param referenceName the unique identifier of the created References.
		 * @param filePath the directory path for load the ontology. (Used also as a default for saving).
		 * @param ontologyPath the semantic IRI path of the ontology to be created
		 * @param bufferingReasoner {@code true} if the reasoner have to evaluate changes to the ontology as soon as their have been performed.
		 * {@code false} if the reasoner should evaluated all the changes of the ontology only if the method {@link OWLReferencesInterface#synchroniseReasoner()} gets called.
		 * @return the created and fully initialised OWL References object
		 */
		public static OWLReferences newOWLReferenceFromFileWithSnorocket( String referenceName, String filePath, String ontologyPath, Boolean bufferingReasoner){
			return new OWLReferences( referenceName, filePath, ontologyPath,  OWLLibrary.REASONER_QUALIFIER_SNOROCKET, bufferingReasoner, COMMAND_LOAD_FILE);
		}		


		/**
		 * Create a new References to an ontology loaded from web with the default reasoner ({@link OWLReferences#setDefaultReasoner(Boolean)}). 
		 * @param referenceName the unique identifier of the created References.
		 * @param filePath the URL path for load the ontology.
		 * @param ontologyPath the semantic IRI path of the ontology to be created
		 * @param bufferingReasoner {@code true} if the reasoner have to evaluate changes to the ontology as soon as their have been performed.
		 * {@code false} if the reasoner should evaluated all the changes of the ontology only if the method {@link OWLReferencesInterface#synchroniseReasoner()} gets called.
		 * @return the created and fully initialised OWL References object.
		 */
		public static OWLReferences newOWLReferenceFromWeb( String referenceName, String filePath, String ontologyPath, Boolean bufferingReasoner){
			return new OWLReferences( referenceName, filePath, ontologyPath, bufferingReasoner, COMMAND_LOAD_WEB);
		}
		/**
		 * Create a new References to an ontology loaded from web with the Pellet reasoner. 
		 * @param referenceName the unique identifier of the created References.
		 * @param filePath the URL path for load the ontology.
		 * @param ontologyPath the semantic IRI path of the ontology to be created
		 * @param bufferingReasoner {@code true} if the reasoner have to evaluate changes to the ontology as soon as their have been performed.
		 * {@code false} if the reasoner should evaluated all the changes of the ontology only if the method {@link OWLReferencesInterface#synchroniseReasoner()} gets called.
		 * @return the created and fully initialised OWL References object.
		 */
		public static OWLReferences newOWLReferenceFromWebWithPellet( String referenceName, String filePath, String ontologyPath, Boolean bufferingReasoner){
			return new OWLReferences( referenceName, filePath, ontologyPath,  OWLLibrary.REASONER_QUALIFIER_PELLET, bufferingReasoner, COMMAND_LOAD_WEB);
		}
		/**
		 * Create a new References to an ontology loaded from web with the Hermit reasoner. 
		 * @param referenceName the unique identifier of the created References.
		 * @param filePath the URL path for load the ontology.
		 * @param ontologyPath the semantic IRI path of the ontology to be created
		 * @param bufferingReasoner {@code true} if the reasoner have to evaluate changes to the ontology as soon as their have been performed.
		 * {@code false} if the reasoner should evaluated all the changes of the ontology only if the method {@link OWLReferencesInterface#synchroniseReasoner()} gets called.
		 * @return the created and fully initialised OWL References object.
		 */
		public static OWLReferences newOWLReferenceFromWebWithHermit( String referenceName, String filePath, String ontologyPath, Boolean bufferingReasoner){
			return new OWLReferences( referenceName, filePath, ontologyPath,  OWLLibrary.REASONER_QUALIFIER_HERMIT, bufferingReasoner, COMMAND_LOAD_WEB);
		}
		/**
		 * Create a new References to an ontology loaded from web with the Fact reasoner. 
		 * @param referenceName the unique identifier of the created References.
		 * @param filePath the URL path for load the ontology.
		 * @param ontologyPath the semantic IRI path of the ontology to be created
		 * @param bufferingReasoner {@code true} if the reasoner have to evaluate changes to the ontology as soon as their have been performed.
		 * {@code false} if the reasoner should evaluated all the changes of the ontology only if the method {@link OWLReferencesInterface#synchroniseReasoner()} gets called.
		 * @return the created and fully initialised OWL References object.
		 */
		public static OWLReferences newOWLReferenceFromWebWithFact( String referenceName, String filePath, String ontologyPath, Boolean bufferingReasoner){
			return new OWLReferences( referenceName, filePath, ontologyPath,  OWLLibrary.REASONER_QUALIFIER_FACT, bufferingReasoner, COMMAND_LOAD_WEB);
		}
		/**
		 * Create a new References to an ontology loaded from web with the Snorocket reasoner. 
		 * @param referenceName the unique identifier of the created References.
		 * @param filePath the URL path for load the ontology.
		 * @param ontologyPath the semantic IRI path of the ontology to be created
		 * @param bufferingReasoner {@code true} if the reasoner have to evaluate changes to the ontology as soon as their have been performed.
		 * {@code false} if the reasoner should evaluated all the changes of the ontology only if the method {@link OWLReferencesInterface#synchroniseReasoner()} gets called.
		 * @return the created and fully initialised OWL References object.
		 */
		public static OWLReferences newOWLReferenceFromWebWithSnorocket( String referenceName, String filePath, String ontologyPath, Boolean bufferingReasoner){
			return new OWLReferences( referenceName, filePath, ontologyPath,  OWLLibrary.REASONER_QUALIFIER_SNOROCKET, bufferingReasoner, COMMAND_LOAD_WEB);
		}

		public static String to_string(){
			String out = "aMUR system instantiates though the class " + OWLReferencesContainer.class.getCanonicalName() + " OWL References to:" + System.getProperty( "line.separator");
			for( OWLReferencesInterface ref : allReferences.values())
				out +=  "\t" + ref.toString() + System.getProperty( "line.separator");
			return out + ".";
		}
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
				+ ", getReasonerExplanator()=" + getReasonerExplanator()
				+ ", isConsistent()=" + isConsistent() + " " + super.toString();
	}

	@Override
	public String toString() {
		return "OWLReferencesInterface \"" + getReferenceName() + "\"";
	}
}