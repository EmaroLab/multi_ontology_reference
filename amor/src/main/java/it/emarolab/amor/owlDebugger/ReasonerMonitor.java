package it.emarolab.amor.owlDebugger;

import it.emarolab.amor.owlDebugger.Logger.LoggerFlag;
import org.semanticweb.owlapi.reasoner.ReasonerProgressMonitor;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * This class notifies into console
 * the progress of reasoning classification. 
 * 
 * @author Buoncomapgni Luca
 * @version 1.0
 *
 */
public class ReasonerMonitor implements ReasonerProgressMonitor{

    /**
     * This object is used to log information about the instances of this class.
     * The logs can be activated by setting the flag: {@link LoggerFlag#LOG_REASONER_MONITOR}
     */
    private Logger logger = new Logger( this, LoggerFlag.LOG_REASONER_MONITOR);

    private Long startTime;
    private String startStr, reasonerName;

    public void setReasonerName( String reasonerName){
        this.reasonerName = reasonerName;
    }

    @Override
    public void reasonerTaskStarted(String taskName) {
        startTime = System.nanoTime();
        Date date = Calendar.getInstance().getTime();
        startStr = taskName + " " + reasonerName + " started at: " +  new SimpleDateFormat( Logger.DATAFORMAT).format(date);
    }

    @Override
    public void reasonerTaskStopped() {
        logger.addDebugString( startStr + " and ends now. "+ reasonerName + " [Durarion:" +
                (System.nanoTime() - startTime) + "ns]");
    }

    @Override
    public void reasonerTaskProgressChanged(int value, int max) {
    }

    @Override
    public void reasonerTaskBusy() {
        logger.addDebugString( "WARNING : reasoner task is busy. ");
    }

}
