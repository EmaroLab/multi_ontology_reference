package it.emarolab.amor.owlDebugger;

import it.emarolab.amor.owlInterface.OWLEnquirer;
import it.emarolab.amor.owlInterface.OWLLibrary;
import it.emarolab.amor.owlInterface.OWLManipulator;
import it.emarolab.amor.owlInterface.OWLReferencesInterface;
import it.emarolab.amor.owlInterface.OWLReferencesInterface.OWLReferencesContainer;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/*
 *
 * 
 *   THE DOCUMENTATION IN THE owlDebugger PACKAGE ID OUT OF DATE !!!
 * 
 * 
 * This class is used to manage logging texts
 * organized in base at the class which 
 * are generated them. To do so,  
 * it controls a table of message that are also ordered by time.
 * This class is initialized during system starting up in relation
 * with the ontological individual {@code C_DebuggingConfig}
 * belong to the class {@code Debugger}
 * which must have the following data properties:
 * <pr>
 * {@code C_DebuggingConfig hasDebuggingPrintOnFile "/src/logs/log.txt"^^string}
 * {@code C_DebuggingConfig hasDebuggingPrintOnCOnsole "true"^^boolean}
 * {@code C_DebuggingConfig hasDebuggingOrderPrintRate "5"^^integer}
 * {@code C_DebuggingConfig hasDebuggingRunGui "true"^^boolean}
 * </pr>
 * Please, refer to the documentation of this class for more
 * info about this features. Moreover, all the logs are attached to 
 * a boolean value to decide if notify or not particular logs.
 * This can be reflected into the ontology using an individual,
 * belong to the ontological class {@value ontologyFramework.OFErrorManagement.DebuggingClassFlagData#DEBUGGER_classFlags},
 * to defined this boolean value. For example:
 * <pr>
 * {@code C_OWLLibraryDebugger logsDebuggingData C_trueFlag}
 * where: {@code C_trueFlag hasTypeBoolean "true"^^boolean}
 * </pr>
 * In this case a logger can be created using:
 * {@code OFDebugLogger logger = new OFDebugLogger( OWLLibrary.class, 
 *         DebuggingClassFlagData.getFlag( C_OWLLibraryDebugger));}.
 * Please refer to {@link DebuggingClassFlagData#getFlag(String)} for more info.
 * 
 * @author Buoncomapgni Luca
 * @version 1.0
 *
 */
public class Logger {

    private Object classTofollow;
    private List< DebugItem> debugText;
    private boolean tofollow = true;

    /*
     * The format with time and date are represented. It will be used
     * as: {@code new SimpleDateFormat( DATAFORMAT).
     * format( Calendar.getInstance().getTime());}
     */
    public static final String DATAFORMAT = "dd/MM/yy_HH:mm:ss,SSS";

    private static final int classColumn = 0;
    private static final int instanceColumn = 1;
    private static final int flagColumn = 2;
    private static final int columnNumber = flagColumn + 1;
    private static final String errorString = "[[ !!! ERROR !!! ]]";

    private static String printOnFile = null;
    private static Boolean printOnFileFlag = false;
    private static Boolean printOnConsole = true;
    private static Integer orderPrintingRate = 1;
    private static Boolean startGui = false;
    private static int logCounter = 0;
    /*
     * Key word to used as a file path to define that actually this
     * class should not print on file.
     */
    public static final String NOFILEPRINTING_KeyWord = "none";
    private static FileManager fm;

    /*
     * Defines a new logger attached to a particular class.
     * As an example can be {@code new OFDebugLogger(this)}
     * or {@code new OFDebugLogger(StaticClass.class)} for
     * a generic static class.
     *
     * @param toFollow is the instance of the class from which
     * will send this logs.
     */
    public Logger( Object toFollow){
        initialize( toFollow);
        setFlagToFollow( true);
    }
    /*
     * It does the same work as OFDebugLogger(Object)
     * but it also calls {@link #addDebugString(String)} with
     * the imput parameter.
     *
     * @param toFollow the class which create logs.
     * @param txt a first log.
     */
    public Logger( Object toFollow, String txt){
        initialize( toFollow);
        addDebugString( txt);
        setFlagToFollow( true);
    }
    /*
     * It does the same work as #OFDebugLogger(Object, String)
     * but specify that all the logs given from this class must
     * be notified or not.
     *
     * @param toFollow the class which create logs.
     * @param txt a first log.
     * @param flagTofollow if it is true than the log are notified, no
     * otherwise.
     */
    public Logger( Object toFollow, String txt, boolean flagTofollow){
        initialize( toFollow);
        setFlagToFollow( flagTofollow);
        addDebugString( txt);
    }
    /*
     * It does the same work as {@link #addDebugString(String)} and
     * also specify if the logs must be notified or not.
     *
     * @param toFollow the class which create logs.
     * @param flagTofollow if it is true than the log are notified, no
     * otherwise.
     */
    public Logger( Object toFollow, boolean flagTofollow){
        initialize( toFollow);
        setFlagToFollow( flagTofollow);
    }
    private  synchronized void initialize( Object toFollow){
        this.classTofollow = toFollow;
        cleanDebugText();
        trackedClass();
    }

    /*
     * Add a log to this logger. Basically it will
     * call {@code addDebugString( text, false);}
     *
     * @param text a log
     */
    public  synchronized void addDebugString(String text){
        addDebugString( text, false);
    }
    public synchronized void addDebugString(String text, boolean isError){
        try{
            if( tofollow){
                if( isError){
                    text = errorString + text;
                }
                DebugItem tmp = new DebugItem( getTime(), text, getNamedClass() + getNamedInstance(), isError);
                debugText.add( tmp);
                logCounter++;
                if( logCounter == orderPrintingRate){
                    List< List< Object>> logs = getLogInfo();
                    if( printOnConsole)
                        printLogOnConsole( logs);
                    if( printOnFileFlag)
                        printLogOnFile( logs);
                    logCounter = 0;
                }
            }
        } catch(Exception e){
            e.printStackTrace();
        }
    }

    /*
     * Set if the log created by this logger must be notified or
     * not.
     *
     * @param flag if it true than the log related to this logger
     * will be notified, no otherwise.
     */
    public  void setFlagToFollow( boolean flag){
        this.tofollow = flag;
    }

    /*
     * @return Return true if the logs produced by this logger are notified
     * by this class, otherwise it will return false.
     */
    public  boolean getFlagToFollow(){
        return( tofollow);
    }

    /*
     * @return The class that are producing this logs.
     */
    public  Object getFollowedClass(){
        return( classTofollow);
    }

    /*
     * It manipulate the returning value of {@link #getFollowedClass()}
     * to retrieve its name of the instance to print.
     * Basically if the name contains the symbol "@" than it will
     * return the chars the follow it otherwise it will return
     * all the name.
     *
     * @return the instance name from its class definition.
     */
    public  String getNamedInstance(){
        try{
            String ret = "";
            if( (classTofollow == null))
                ret = "";//null
            else if(  classTofollow.toString().contains( "@"))
                ret = classTofollow.toString().substring( classTofollow.toString().indexOf( "@"));
            else{
                ret = classTofollow.toString().substring( classTofollow.toString().lastIndexOf(".") + 1);
                ret = " " + ret;
            }
            return( ret);
        } catch( Exception e){
            e.printStackTrace();
            return(null);
        }
    }

    /*
     * @return the {@code getSimpleName()} applied to the class given
     * during class creation for this logger.
     */
    public  String getNamedClass(){
        // if you want also the package return: classTofollow.getClass().getCanonicalName()
        if( classTofollow == null)
            return( " null");
        return( classTofollow.getClass().getSimpleName());
    }

    /*
     * @return all the logs for this logger.
     */
    public  List< DebugItem> getDebugText(){
        return( debugText);
    }

    /*
     * It will clean up all the logs of this logger.
     */
    public  void cleanDebugText(){
        debugText = Collections.synchronizedList(new ArrayList<DebugItem>());//new ArrayList< DebugItem>();
    }

    /*
     * Calls {@code this.finalize()} to remove this logger from the
     * logging mechanism.
     */
    public  void removeDebug(){
        finalize();
    }

    /*
     * @return the actual time formatted by {@value #DATAFORMAT}.
     */
    private static String getTime(){
        Date date = Calendar.getInstance().getTime();
        return( new SimpleDateFormat( DATAFORMAT).format(date));
    }

    // store the instances of 'this' for retrieve it by name in another file by using invokerManager.getAllInstancesDebugInstance
    private static Map<String, Logger> allInstances;
    static  {
        allInstances = new ConcurrentHashMap<String, Logger>();
    }
    public synchronized void finalize()  {
        allInstances.values().remove( this);
    }
    private synchronized void trackedClass()  {
        allInstances.put( getNamedInstance(), this);
    }
    /*
     * @return a map which contains all the created instances of this class
     * related to a key equal to the result of {@link #getNamedClass()}.
     */
    public static synchronized  ConcurrentHashMap<String, Logger> getAllInstances(){
        return( ( ConcurrentHashMap<String, Logger> )allInstances );
    }
    /*
     * print to console the map which is returning by {@link #getAllInstances()}.
     */
    public static void printActiveIstances(){ // return the number of activated instances
        int count = 0;
        for (Map.Entry<String, Logger> entry : allInstances.entrySet())
            System.out.println( count++ + " : " + entry);
    }


    /*
     * It goes across all the instance of this class and return a table with
     * all the logs create by all the logger ordered by time. In particular,
     * the column of this table are: time stamp, class name and textual log.
     *
     * @return a table with all the logs created by all the instances of
     * this class.
     */
    public  static List<List<Object>> getLogInfo(){
        // buld all the logs
        List< DebugItem> allLogs = new ArrayList< DebugItem>();

        try{
            synchronized( allInstances){
                for( String i : allInstances.keySet()){
                    if( allInstances.get(i).getFlagToFollow()){
                        allLogs.addAll( allInstances.get( i).getDebugText());
                        allInstances.get( i).cleanDebugText();
                    }
                }
            }

            // sorth all logs
            Collections.sort( allLogs);

            List< List< Object>> allLog = new ArrayList< List< Object>>();
            for( DebugItem di : allLogs){
                List< Object> tmp = new ArrayList<Object>();
                tmp.add( di.getTime());
                tmp.add( di.getClassName());
                tmp.add( di.getText());
                allLog.add( tmp);
            }
            return( allLog);
        }catch( Exception ex){
            ex.printStackTrace();
            return( null);
        }
    }

    /*
     * @return an object to be used as JTable manipolating the returning
     * value of {@link #getLogInfo()}.
     */
    public  static Object[][] getTableInfo(){
        synchronized( allInstances){
            Object[][] info = new Object[ allInstances.size()][ columnNumber];
            int row = 0;
            for( String i : allInstances.keySet()){
                info[ row][ classColumn] = (Object) allInstances.get( i).getNamedClass();
                info[ row][ instanceColumn] = (Object) allInstances.get( i).getNamedInstance();
                info[ row++][ flagColumn] = (Object) allInstances.get( i).getFlagToFollow();
            }
            return( info);
        }
    }

    /*
     * Print on console all the logs info contained in the input parameter.
     * It uses a parsing mechanism in accord with the table returned by:
     * {@link #getLogInfo()}.
     *
     * @param logData all the long formatted as a table.
     */
    public synchronized static void printLogOnConsole( List< List< Object>> logData){
        for( List< Object> o : logData)
            System.out.println( parseLog( o));
    }

    /*
     * Print on file all the logs info contained in the input parameter.
     * It uses a parsing mechanism in accord with the table returned by:
     * {@link #getLogInfo()}.
     *
     * @param logData all the long formatted as a table.
     */
    public synchronized static void printLogOnFile( List< List< Object>> logData){
        try{
            List< String> logs = new ArrayList< String>();
            fm.loadFile();
            for( List< Object> o : logData)
                logs.add( parseLog( o));
            fm.printOnFile( logs);
            fm.closeFile();
        }catch( Exception ex){
            ex.printStackTrace();
        }
    }

    private static String parseLog( List< Object> o){
        try{
            return( " " + o.get( 0) + " -> " + o.get(1) + " : " + o.get(2));
        }catch(Exception e){
            return( errorString + " exception on class OFDebugLogger");
        }
    }

    /*
     * @return the directory path to the file if this class is printing
     * to file. If it is not printing this method will return:
     * {@value #NOFILEPRINTING_KeyWord}.
     */
    public static String getPrintOnFile() {
        return printOnFile;
    }
    /*
     * Set to print longs on file. This will not be done if
     * the input parameter is equal to {@value #NOFILEPRINTING_KeyWord}.
     * Moreover it uses {@link FileManager} to use files and if the input
     * parameter is equal to {@link FileManager#DEFAULT_command} than it
     * will be initialized as:
     * {#code new FileManager(FileManager.defaultComand, FileManager.defaultComand, FileManager.keyTxt);}.
     * Otherwise it is possible to give, as input parameter
     * a relative path to a file that should be augmented with
     * available logs.
     *
     * @param printOnFile the directory path to the file or the instruction
     * to do not print over file or the default file definition of {@link FileManager}.
     */
    public  static void setPrintOnFile(String printOnFile) {
        Logger.printOnFile = printOnFile;
        if( printOnFile.equals( NOFILEPRINTING_KeyWord))
            printOnFileFlag = false;
        else{
            String path = null;
            String name = null;
            if( printOnFile.equals( FileManager.DEFAULT_command))
                fm = new FileManager( FileManager.DEFAULT_command, FileManager.DEFAULT_command, FileManager.TXT_fileFormat);
            else{
                path = printOnFile.substring( 0, printOnFile.lastIndexOf( System.getProperty( "file.separator"))).trim();
                name = printOnFile.substring( path.length(), printOnFile.lastIndexOf( "."));
                fm = new FileManager( path, name, FileManager.TXT_fileFormat);
            }
            printOnFileFlag = true;
        }
    }
    /*
     * @return true if the class is printing on console, false otherwise
     */
    public static Boolean getPrintOnConsole() {
        return printOnConsole;
    }
    /*
     * @param printOnConsole the printOnConsole to set. If it is true
     * than all the logs will be printed on console, if it is false no.
     */
    public static void setPrintOnConsole(Boolean printOnConsole) {
        Logger.printOnConsole = printOnConsole;
    }
    /*
     * A parameter as been introduced to find
     * a correct trade off between ordering with respect to
     * time logs without having a big list to manage before to
     * print and than clean them.
     *
     * @return the number which defines after how many longs this class
     * has to build the relate table, eventually notifing it and then
     * clearing it.
     */
    public static Integer getOrderPrintingRate() {
        return orderPrintingRate;
    }
    /*
     * A parameter as been introduced to find
     * a correct trade off between ordering with respect to
     * time logs without having a big list to manage before to
     * print and than clean them.
     *
     * @param orderPrintingRate set the number of logs that are
     * saved in the table before to notify logs and clear the resources.
     */
    public  static void setOrderPrintingRate(Integer orderPrintingRate) {
        Logger.orderPrintingRate = orderPrintingRate;
    }

    /*
     * @return return true if the debugging GUI starts when the program starts.
     */
    public  static Boolean getStartGui() {
        return startGui;
    }
    /*
     * @param startGui set to true if the debugging GUI should start when
     * the program start. No starting property otherwise.
     */
    public static  void setStartGui(Boolean startGui) {
        Logger.startGui = startGui;
    }

    public static void flush(){
        List< List< Object>> logs = getLogInfo();
        if( printOnConsole)
            printLogOnConsole( logs);
        if( printOnFileFlag)
            printLogOnFile( logs);
        logCounter = 0;
    }

    /*
     * This class is used to defines logs that are
     * short by time. Those will be elments of the
     * log table builded by this implementation.
     *
     * @author Buoncomapgni Luca
     * @version 1.0
     *
     */
    class DebugItem implements Comparable< DebugItem>{

        private final String text;
        private final String time;
        private final String className;
        private final Boolean isError;

        /*
         * Create a log instance.
         *
         * @param actualtime time where the log takes place.
         * @param actualtext the text attached to the log.
         * @param name the name of the class that are producing the log.
         * @param redPrinting if it is true than the log is considered to
         * be an error. If it is false it will be just an information.
         */
        public DebugItem( String actualtime, String actualtext, String name, boolean redPrinting){
            text = actualtext;
            time = actualtime;
            className = name;
            isError = redPrinting;
        }

        /*
         * @return the name of the class that are producing this log
         */
        public  String getClassName() {
            return className;
        }

        /*
         * @return the text of the log.
         */
        public  String getText() {
            return text;
        }

        /*
         * @return the time when this log takes place,
         */
        public  String getTime() {
            return time;
        }

        /*
         * @return true if the log is considered to be an error,
         * false otherwise.
         */
        protected  Boolean getIsError() {
            return isError;
        }

        @Override
        public  int compareTo( DebugItem other) {
            Date tOther = null;
            Date tThis = null;
            try {
                tOther = new SimpleDateFormat( DATAFORMAT).parse( other.getTime());
                tThis = new SimpleDateFormat( DATAFORMAT).parse( this.getTime());
            } catch (ParseException e) {
                e.printStackTrace();
            }

            return( tThis.compareTo(tOther));
        }
    }


    /**
     * This class contains defualts methods for enabling and disabling logging.
     * Generically, each class of this architecture has a {@link Logger} attached to it.
     * Use this static implementation to manipulate their flags.
     */
    public static class LoggerFlag{
        /**
         * This flag enables or disables logs provided by {@link OWLReferencesContainer}.
         * Actual value: {@link #LOG_REFERENCES_CONTAINER}.
         */
        public static Boolean LOG_REFERENCES_CONTAINER = true;

        /**
         * This flag enables or disables logs provided by {@link OWLReferencesInterface}
         * Actual value: {@link #LOG_REFERENCES_INTERFACE}.
         */
        public static Boolean LOG_REFERENCES_INTERFACE = true;

        /**
         * This flag enables or disables logs provided by {@link OWLLibrary}
         * Actual value: {@link #LOG_OWL_LIBRARY}.
         */
        protected static Boolean LOG_OWL_LIBRARY = true;

        /**
         * This flag enables or disables logs provided by {@link ReasonerMonitor}
         * Actual value: {@link #LOG_REASONER_MONITOR}.
         */
        protected static Boolean LOG_REASONER_MONITOR = true;

        /**
         * This flag enables or disables logs provided by {@link it.emarolab.amor.owlInterface.ReasonerExplanator}
         * Actual value: {@link #LOG_REASONER_EXPLANATION}.
         */
        protected static Boolean LOG_REASONER_EXPLANATION = true;

        /**
         * This flag enables or disables logs provided by {@link OWLManipulator}
         * Actual value: {@link #LOG_OWL_MANIPULATOR}.
         */
        protected static Boolean LOG_OWL_MANIPULATOR = true;

        /**
         * This flag enables or disables logs provided by {@link it.emarolab.amor.owlInterface.OWLReferences}
         * Actual value: {@link #LOG_ONTOLOGY_REFERENCE}.
         */
        protected static Boolean LOG_ONTOLOGY_REFERENCE = true;

        /**
         * This flag enables or disables logs provided by {@link OWLEnquirer}
         * Actual value: {@link #LOG_OWL_ENQUIRER}.
         */
        protected static Boolean LOG_OWL_ENQUIRER = true;

        /**
         * This flag enables or disables logs provided by {@link it.emarolab.amor.owlInterface.InferredAxiomExporter}
         * Actual value: {@link #LOG_ONTOLOGY_EXPORTER}.
         */
        protected static Boolean LOG_ONTOLOGY_EXPORTER = true;


        //  [[[[[[[[[[[[[[[[[[[[[[[[[[[[[    GETTERS   ]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]
        /**
         * @return the {@link #LOG_REFERENCES_CONTAINER}
         */
        public static synchronized Boolean getLogReferencesContainer() {
            return LOG_REFERENCES_CONTAINER;
        }
        /**
         * @return the LOG_REFERENCES_INTERFACE
         */
        public static synchronized Boolean getLogReferencesInterface() {
            return LOG_REFERENCES_INTERFACE;
        }
        /**
         * @return the {@link #LOG_OWL_LIBRARY}
         */
        public static synchronized Boolean getLogOwlLibrary() {
            return LOG_OWL_LIBRARY;
        }
        /**
         * @return the {@link #LOG_REASONER_MONITOR}
         */
        public static synchronized Boolean getLogReasonerMonitor() {
            return LOG_REASONER_MONITOR;
        }
        /**
         * @return the {@link #LOG_REASONER_EXPLANATION}
         */
        public static synchronized Boolean getLogReasonerExplanation() {
            return LOG_REASONER_EXPLANATION;
        }
        /**
         * @return the {@link #LOG_OWL_MANIPULATOR}
         */
        public static synchronized Boolean getLogOWLManipulator() {
            return LOG_OWL_MANIPULATOR;
        }
        /**
         * @return the {@link #LOG_ONTOLOGY_REFERENCE}
         */
        public static synchronized Boolean getLogOntologyReference() {
            return LOG_ONTOLOGY_REFERENCE;
        }
        /**
         * @return the {@link #LOG_OWL_ENQUIRER}
         */
        public static synchronized Boolean getLogOWLEnquirer() {
            return LOG_OWL_ENQUIRER;
        }
        /**
         * @return the {@link #LOG_ONTOLOGY_EXPORTER}
         */
        public static synchronized Boolean getLogOntologyExporter() {
            return LOG_ONTOLOGY_EXPORTER;
        }

        //  [[[[[[[[[[[[[[[[[[[[[[[[[[[[[    SETTERS   ]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]
        /**
         * @param logReferencesContainer the {@link #LOG_REFERENCES_CONTAINER} to set
         */
        public static synchronized void setLogReferencesContainer( Boolean logReferencesContainer) {
            LOG_REFERENCES_CONTAINER = logReferencesContainer;
        }
        /**
         * @param logReferencesInterface the LOG_REFERENCES_INTERFACE to set
         */
        public static synchronized void setLogReferencesInterface( Boolean logReferencesInterface) {
            LOG_REFERENCES_INTERFACE = logReferencesInterface;
        }
        /**
         * @param logOwlLibrary the {@link #LOG_OWL_LIBRARY} to set
         */
        public static synchronized void setLogOwlLibrary( Boolean logOwlLibrary) {
            LOG_OWL_LIBRARY = logOwlLibrary;
        }
        /**
         * @param logReasonerMonitor the {@link #LOG_REASONER_MONITOR} to set
         */
        public static synchronized void setLogReasonerMonitor( Boolean logReasonerMonitor) {
            LOG_REASONER_MONITOR = logReasonerMonitor;
        }
        /**
         * @param logReasonerExplanation the {@link #LOG_REASONER_EXPLANATION} to set
         */
        public static synchronized void setLogReasonerExplanation( Boolean logReasonerExplanation) {
            LOG_REASONER_EXPLANATION = logReasonerExplanation;
        }
        /**
         * @param logOwlManipulator the {@link #LOG_OWL_MANIPULATOR} to set
         */
        public static synchronized void setLogOwlManipulator(Boolean logOwlManipulator) {
            LOG_OWL_MANIPULATOR = logOwlManipulator;
        }
        /**
         * @param logOntologyReference the {@link #LOG_ONTOLOGY_REFERENCE} to set
         */
        public static synchronized void setLogOntologyReference( Boolean logOntologyReference) {
            LOG_ONTOLOGY_REFERENCE = logOntologyReference;
        }
        /**
         * @param logOwlEnquirer the {@link #LOG_OWL_ENQUIRER} to set
         */
        public static synchronized void setLogOwlEnquirer(Boolean logOwlEnquirer) {
            LOG_OWL_ENQUIRER = logOwlEnquirer;
        }
        /**
         * @param logOntologyExporter the {@link #LOG_ONTOLOGY_EXPORTER} to set
         */
        public static synchronized void setLogOntologyExporter(    Boolean logOntologyExporter) {
            LOG_ONTOLOGY_EXPORTER = logOntologyExporter;
        }

        /**
         * This methods sets all flags contained in this class to {@code true}.
         */
        public static synchronized void setAllLoggingFlags(){
            LOG_REFERENCES_CONTAINER = true;
            LOG_REFERENCES_INTERFACE = true;
            LOG_OWL_LIBRARY = true;
            LOG_REASONER_MONITOR = true;
            LOG_REASONER_EXPLANATION = true;
            LOG_OWL_MANIPULATOR = true;
            LOG_ONTOLOGY_REFERENCE = true;
            LOG_OWL_ENQUIRER = true;
            LOG_ONTOLOGY_EXPORTER = true;
        }
        /**
         * This methods sets all flags contained in this class to {@code false}.
         */
        public static synchronized void resetAllLoggingFlags(){
            LOG_REFERENCES_CONTAINER = false;
            LOG_REFERENCES_INTERFACE = false;
            LOG_OWL_LIBRARY = false;
            LOG_REASONER_MONITOR = false;
            LOG_REASONER_EXPLANATION = false;
            LOG_OWL_MANIPULATOR = false;
            LOG_ONTOLOGY_REFERENCE = false;
            LOG_OWL_ENQUIRER = false;
            LOG_ONTOLOGY_EXPORTER = false;
        }
    }
}