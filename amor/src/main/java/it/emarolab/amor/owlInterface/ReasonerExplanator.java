package it.emarolab.amor.owlInterface;

import javax.swing.JOptionPane;

import it.emarolab.amor.owlDebugger.Logger;
import it.emarolab.amor.owlDebugger.Logger.LoggerFlag;

/**
* Project: aMOR <br>
* File: .../src/aMOR.owlInterface/OWLLibrary.java <br>
*  
* @author Buoncompagni Luca <br><br>
* DIBRIS emaroLab,<br> 
* University of Genoa. <br>
* Feb 10, 2016 <br>
* License: GPL v2 <br><br>
*  
* <p>
* This class implements the basic interface for reasoner inconsistencies and explanation.
* </p>
* 
* @see 
*
* 
* @version 2.0
*/
abstract public class ReasonerExplanator{
	/**
	 * This is the object that contains all the needed references
	 * to an OWL ontology. Set on constructor.
	 */
	private OWLLibrary owlLibrary;

	/**
	 * This object is used to log informations about the instances of this class.
	 * The logs can be activated by setting the flag: {@link LoggerFlag#LOG_REASONER_EXPLANATION}
	 */
	private Logger logger = new Logger( this, LoggerFlag.getLogReasonerExplanation());

	/**
	 * This constructor just save the instance of the OWL Library
	 * that refers to all the OWL object instantiated in a specific References
	 * @param owlLibrary the object containing OWL instances of a specified References
	 */
	protected ReasonerExplanator( OWLLibrary owlLibrary){
		this.owlLibrary = owlLibrary;
	}

	/**
	 * @return all the explanation that the reasoner can provide when an inconsistency occurs to be printed for debugging.
	 */
	abstract protected String getExplanation();

	/**
	 * This method should print on console (or use {@link #showErrorDialog(String)}) in order to show that an inconsistency occurs.
	 */
	abstract protected void notifyInconsistency();

	/**
	 * This method show a graphical panel by notify the user that an inconsistency occurs.
	 * @param text the text to be notified to the user
	 */
	protected void showErrorDialog( String text){
		JOptionPane panel = new JOptionPane();
		JOptionPane.showMessageDialog( panel, text, "Ontology is INCONSISTENT", JOptionPane.ERROR_MESSAGE);
	}

	/**
	 * @return the instance that points to all the needed objects of an OWL References. 
	 * Initialised on constructor.
	 */
	protected OWLLibrary getOwlLibrary(){
		return owlLibrary;
	}

	/**
	 * @return the logging object associated to this class with the activation
	 * flag: {@link LoggerFlag#LOG_REASONER_EXPLANATION}
	 */
	protected Logger getLogger(){
		return this.logger;
	}
}
