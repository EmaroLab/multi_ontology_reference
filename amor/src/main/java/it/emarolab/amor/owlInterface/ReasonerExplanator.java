package it.emarolab.amor.owlInterface;

import it.emarolab.amor.owlDebugger.Logger;
import it.emarolab.amor.owlDebugger.Logger.LoggerFlag;

import javax.swing.*;

/**
 *This class implements the basic interface for reasoner inconsistencies and explanation.
 *
 * <div style="text-align:center;"><small>
 * <b>Project</b>:    aMOR <br>
 * <b>File</b>:       it.emarolab.amor.owlInterface.ReasonerExplanator <br>
 * <b>Licence</b>:    GNU GENERAL PUBLIC LICENSE. Version 3, 29 June 2007 <br>
 * <b>Author</b>:     Buoncompagni Luca (luca.buoncompagni@edu.unige.it) <br>
 * <b>affiliation</b>: DIBRIS, EMAROLab, University of Genoa. <br>
 * <b>date</b>:       Feb 10, 2016 <br>
 * </small></div>
 * @version 1.0
*/
abstract public class ReasonerExplanator{
    /**
     * This is the object that contains all the required references
     * to an OWL ontology. Set on constructor.
     */
    private OWLLibrary owlLibrary;

    /**
     * This object is used to log information about the instances of this class.
     * The logs can be activated by setting the flag {@link LoggerFlag#LOG_REASONER_EXPLANATION}
     */
    private Logger logger = new Logger( this, LoggerFlag.getLogReasonerExplanation());

    /**
     * Constructor.
     * @param owlLibrary an object collecting all OWL entities belonging to an OWL reference
     */
    protected ReasonerExplanator( OWLLibrary owlLibrary){
        this.owlLibrary = owlLibrary;
    }

    /**
     * @return all the explanation that the reasoner can provide when an inconsistency occurs. Can be printed for debugging.
     */
    abstract protected String getExplanation();

    /**
     * This method should print on console (or use {@link #showErrorDialog(String)}) in order to show that an inconsistency occurred.
     */
    abstract protected void notifyInconsistency();

    /**
     * This method show a graphical panel to notify the user that an inconsistency occurred.
     * @param text the text to be notified to the user
     */
    protected void showErrorDialog( String text){
        JOptionPane panel = new JOptionPane();
        JOptionPane.showMessageDialog( panel, text, "Ontology is INCONSISTENT", JOptionPane.ERROR_MESSAGE);
    }

    /**
     * @return the instance that points to all the objects of an OWL Reference.
     * Initialised on constructor.
     */
    protected OWLLibrary getOwlLibrary(){
        return owlLibrary;
    }

    /**
     * @return the logging object associated to this class.
     * Logging can be activated by the flag {@link LoggerFlag#LOG_REASONER_EXPLANATION}.
     */
    protected Logger getLogger(){
        return this.logger;
    }
}
