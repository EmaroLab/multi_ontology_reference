package it.emarolab.amor.owlDebugger;

import it.emarolab.amor.owlInterface.OWLReferencesInterface;
import it.emarolab.amor.owlInterface.OWLReferencesInterface.OWLReferencesContainer;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;

import javax.swing.*;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.StringTokenizer;

/**
 * This class helps in file management.
 * 
 * @author Buoncomapgni Luca
 * @version 1.0
 *
 */
public class FileManager {

    // TODO trasformare errori in eccezioni!! (implementato, da testare)
    // TODO aggiungere più formati (e.g CSV), aggiornare lista getFormat e errori sul 'loadFile'...automatizza
    // TODO possibilità di cambiare defaultBasePath
    // TODO riguardare i "/" per generalità con windows....anche su gli altri file (File.separator)
    // TODO fare lettura da file
    // TODO print on file overwrite
    // TODO delete() with different input path
    // TODO rederect System.out in a file

    // to deal with exception. If there will be exception on write other exception on file
    // this will be notify on the previous buffer streaming and the latter exception will
    // be printed on console. Up to another setExceptionPrintOnFile from OntologyManager

    //private File file = null;
    private BufferedWriter writer = null;  // object to write
    private String filePath = null;  // absolute path of the file

    private String defaultBasePath = System.getProperty("user.dir") + "/files/logs/"; // basic path for default usage


    // define the file.format it must be one of the key and it must have delimiters
    private String fileFormat = null;
    // delimiter and key word for different formats used on the method 'printOnFile
    /**
     * Defines the format for a textual file {@literal .txt}
     */
    public static final String TXT_fileFormat = "txt";
    /**
     * Defines the format for a logging file {@literal .log}
     */
    public static final String LOG_fileFormat = "log";
    /**
     * Defines the format for a java class file {@literal .java}
     */
    public static final String JAVA__fileFormat = "java";
    /**
     * Defines the format for an ontological file {@literal .owl}
     */
    public static final String OWL_fileFormat = "owl";
    /**
     * Define the symbol to end a line in the file.
     */
    public static final String ENDOFLINE_symbol = System.getProperty("line.separator");//"\n";

    // input command string to control default class behaviour
    /**
     * If a directory path is define with this name, than the default
     * behaviou will be adopted by the class-
     */
    public static final String DEFAULT_command = "default";


    // _____________________________  LOAD, CREATE, PRINT & CLOES A FILE ____________________________

    // constructor, set parameter.
    // if path is equal to default it builds path with folder with prefix: dd-mm-yyyy_hhAM-mm_
    // if fileName is equal to default it builds file with name with prefix: dd-mm-yyyy_hhAM-mm_
    // if format = default => path is the complete absolute path top the file and fileName is unconsidered
    // if ontology is equal to null all the exception will be not considered.
    // exception ID 8 : file format unknown
    /**
     * Create and initialized a file manager. In particular the following rules
     * are true for this method:
     * <pre>
     * if 'path' is equal to {@value #DEFAULT_command} it builds path
     *     with the last folder with prefix: dd-mm-yyyy_hhAM-mm_
     * if 'fileName' is equal to {@value #DEFAULT_command} it builds file
     *     with name with prefix: dd-mm-yyyy_hhAM-mm_
     * if 'format' is equal to {@value #DEFAULT_command} than
     *     'path' is the complete absolute path to the file and
     *     'fileName is unconsidered
     * </pre>
     * This method uses the default base path, which is equal to:
     * {@code System.getProperty("user.dir") + "/files/logs/"}
     *
     * @param path the relative directory where the file is.
     * @param fileName the name of the file.
     * @param format the extension of the file.
     */
    public FileManager( String path, String fileName,  String format){
        initialize( path, fileName, format);
    }
    /**
     * It create a new file manager following the same rules as
     * {@link #FileManager(String, String, String)}.
     * Only different is that this constructor does not use the
     * default base path anymore.
     *
     * @param path the relative directory where the file is.
     * @param fileName the name of the file.
     * @param format the extension of the file.
     * @param basedPath the path which define the relative directory.
     */
    public FileManager( String path, String fileName,  String format, String basedPath){
        this.defaultBasePath = basedPath;
        initialize( path, fileName, format);
    }
    private void initialize( String path, String fileName,  String format){

        if ( format.equals( DEFAULT_command)){
            filePath = path; // path = /.../directory/../name.txt
            fileFormat = getFormatFromAbsolutePath( path);
            if ( ( ! fileFormat.equals( TXT_fileFormat)) && ( ! fileFormat.equals( LOG_fileFormat)) && ( ! fileFormat.equals( JAVA__fileFormat)))
                System.out.println("Exception");//new OntologyManager().notifyException( exceptionID, "IOjavaException, error in loading file : " + filePath + ". File format :" + fileFormat + " unknow.\nPossible formats are : " + getPossibleFormat()[0] + " " + getPossibleFormat()[1] + " " + getPossibleFormat()[2] );
        } else {
            if ( path.equals( DEFAULT_command)){
                new TimeManager();
                String timeHour =  TimeManager.dataHourString( false);
                filePath = defaultBasePath + timeHour;
                new File( filePath).mkdirs();
                if( fileName.equals( DEFAULT_command)) {
                        filePath += "/" + timeHour + "." + format;
                } else { filePath += "/" + timeHour + fileName + "." + format; }
            } else { filePath = path + fileName + "." + format; }
            fileFormat = format;
        }
    }
    // open a file given a directory path and returns a writer object to that file
    // if the file doe not exist it will be created.
    // returns true if the procedure ends without errors, otherwise false
    // exception ID 9 : IOjavaException on loading file
    /**
     * After class construction, when paths are setted a file
     * must be open before use it. If the file does not
     * exist this method will create it.
     *
     * @return true if the file has been loaded or false if not.
     */
    public boolean loadFile(){
        //int exceptionID = 9;
        boolean succesfull = true;

        File file = new File( filePath);
        // if file does not exists, then create it
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                System.out.println("Exception");//new OntologyManager().notifyException( exceptionID, "IOjavaException : error in creating file : " + filePath +"\njava message : " + e.getMessage().toString());
                succesfull = false;
            }
        }

        FileWriter fw = null;
        try {
            fw = new FileWriter( file.getAbsoluteFile(), true);
            writer = new BufferedWriter(fw);
        } catch (IOException e) {
            new ShowError( 0, new String[] { "IOjavaException", "error in creating a writer object on file : " + filePath, e.getCause().toString(), e.getMessage().toString()});
            succesfull = false;
        }
        return( succesfull);
    }

    // close the file and
    // returns true if the procedure ends without errors, otherwise false
    // exception ID 10 : IOjavaException on closing file
    /**
     * After manipulation the file mast be close to properly
     * create it and make it readable by text editors.
     *
     * @return true if the file has been closed, false otherwise
     */
    public boolean closeFile(){
        //int exceptionID = 10;
        boolean succesful = true;

        try {
            writer.close();
            // fw.close
        } catch (IOException e) {
            System.out.println("Exception");//new OntologyManager().notifyException( exceptionID, "IOjavaException : error in closing file : " + filePath + "\njava message: " + e.getMessage().toString());
            succesful = false;
        }
        return( succesful);
    }

    // write a string in the file in different formats
    // returns true if the procedure ends without errors, otherwise false
    // ATTENTION : if not delete a file befaure it will append strings to the existing file
    // exception ID 11 : IOjavaException while printing on file
    /**
     * Write all the strings into a file with a new line as separator.
     * If the flag is true than the file is cleaned first, otherwise
     * the text will be appended to the already existing file, if any.
     * It automatically open, print and then close the file.
     *
     * @param strs strings to be written into the file
     * @param overwrite flag. If false those strings will be appended
     * to the file, otherwise the file will be cleaned first.
     * @return true if all the strings have been printed in the file,
     * false otherwise.
     */
    public boolean printOnFile( List<String> strs, boolean overwrite){
        if( overwrite){
            if( writer != null){
                this.closeFile();
                this.deleteFile();
                this.loadFile();
            }
        }
        return( printOnFile( strs));
    }
    /**
     * Append all the string into the file, which should be
     * already opened.
     *
     * @param strs strings to be written into the file
     * @return true if all the strings have been printed in the file,
     * false otherwise.
     */
    public boolean printOnFile( List<String> strs){
        //int exceptionID = 11;
        boolean succesful = true;
        String toPrint = "";
        // prepare string to print whit the right format
        if( fileFormat.equals( TXT_fileFormat))
            toPrint = inTxtFormat( strs);
        else if( fileFormat.equals( LOG_fileFormat))
            toPrint = inLogFormat( strs);
        else if( fileFormat.equals( JAVA__fileFormat))
            toPrint = inJavaFormat( strs);
        else if( fileFormat.equals( OWL_fileFormat))
            if( inOWLFormat( strs.get( 0) , new File( filePath)));
                succesful = true;

        // write that string on the file, modify: getPossibleFormat, constructor(x2), and create the method to call here
        if( (toPrint != null))
            if( ! toPrint.trim().isEmpty())
                try {
                    writer.write( toPrint);
                } catch (IOException e) {
                    System.out.println("Exception");//new OntologyManager().notifyException( exceptionID, "IOjavaException : error in file writing the string : " + toPrint + "\njava message: " + e.getMessage().toString());
                    succesful = false;
                }
        return( succesful);
    }

    // delete the file given a directory path
    // exception ID 16 : a not existing file cannot be deleted
    /**
     * Delete the file.
     * @return true if the operation ends successfully, false otherwise.
     */
    public boolean deleteFile( ){
        boolean success;
        File file = new File( filePath);
        if( file.delete())
            success = true;
        else{ // IOJavaException{
            System.out.println("Exception");//new OntologyManager().notifyException( 16, "fail on deleting in path : " + filePath);
            success = false;
        }
        return( success);
    }

    /*
    // envoce an exception
    private void notifyException( int exceptionID, String hint){
        if( ! exceptionSkip){
            ExceptionType eT = null;
            try{
                eT = exceptionHashMap.get( exceptionID);
            }
            catch( NullPointerException e){
                new ShowError( 0, new String[] { "ONTOLOGY SETTING ERROR", " the class Exception must have an appropiate individual with ID " + exceptionID});
            }
            eT.notifier( hint);
        }
    }*/

    // _____________________________  GET METHODS ____________________________

    // returns the path for the file
    /**
     * @return the directory to the file.
     */
    public String getFilePath(){ return( filePath); }
    // returns the writer object to that file
    /**
     * @return the object to write into the file.
     */
    public BufferedWriter getFileWriter(){ return( writer); }
    // returns file format as a string
    /**
     * @return the extension of the file.
     */
    public String getFileFormat(){ return( fileFormat); }

    // get and set base path for default loading
    /**
     * @return the base directory path used to define relatively all
     * the other paths.
     */
    public String getDefaultBasePath(){ return( defaultBasePath); }


    /**
     * @return all the possible extension that can be used by this manager.
     */
    public String[] getPossibleFormat(){ return( new String[] { TXT_fileFormat, LOG_fileFormat, JAVA__fileFormat}); }


    // get file format from absolute path.
    // ATTENTION: the path must contain a format (eg /p/a/t/h/name.txt) and no more symbols '.'
    // exception ID 12 : bad set absolute path. the file must contain an explicit extension and no more '.' symbols
    /**
     * It assume to have path which contains only one symbol equal to
     * "{@literal .}" and based in this it retrieve the extension of a file
     * from its coplete path.
     *
     * @param absolutePath full directory path to file
     * @return the format of the file
     */
    public String getFormatFromAbsolutePath( String absolutePath){
        //int exceptionID = 12;
        StringTokenizer st = new StringTokenizer( absolutePath, ".");
        String format = null;
        while ( st.hasMoreElements())
            format = (String) st.nextElement(); // get the last string after the '.'
        if( format.equals( null)){
            System.out.println("Exception");//new OntologyManager().notifyException( exceptionID, "IOjavaException : error in loading file : " + filePath + " the file must contain an explicit extension and no more symbols '.'\n possible formats are : " + getPossibleFormat());
            format = null; //???????????????????????????????
        }
        return( format);
    }



    // _____________________________  FORMATTING INTO A STRING TO PRINT ____________________________

    // concatenate all the components of strs with new line as delimiter
    private String inTxtFormat( List<String> strs){
        String toPrint = "";
        for( int i = 0; i < strs.size(); i++)
            toPrint += strs.get( i) + ENDOFLINE_symbol;
        return( toPrint);
    }

    // as a inTxtFormat but add a line on top with every is called with : dd-mm-yyyy_hhAM-mm_
    private String inLogFormat( List<String> strs){
        new TimeManager();
        String toPrint = TimeManager.dataHourString( true) + ENDOFLINE_symbol;
        toPrint += inTxtFormat( strs);
        return( toPrint);
    }

    private String inJavaFormat( List<String> strs){
        return( inTxtFormat( strs)); //already formatted by CreateClassFile class
    }

    // save ontology to file
    private Boolean inOWLFormat( String ontoName, File file){
        OWLReferencesInterface ontoRef = OWLReferencesContainer.getOWLReferences( ontoName);
        //OntologyManager ontoM = InvokerManager.getOntologyManagerInstance( ontoName);
        try {
            ontoRef.getOWLManager().saveOntology( ontoRef.getOWLOntology(), IRI.create(file.toURI()));
            return true;
        } catch (OWLOntologyStorageException e) {
            return false;
        }
    }
}



/**
 * This class is used by the FileManager to manage the time r
 * representation.
 * 
 * @author Buoncomapgni Luca
 * @version 1.0
 *
 */
class TimeManager {

    private static Date date;
    private static String year, month, day,    hour, minute, ms;

    // return an up to date string as:
    //     dd-mm-yyyy_hhAM-mm_    if addMilliSec is false
    //     dd-mm-yyyy_hhAM-mm-ms_    if addMilliSec is true
    /**
     * @param addMilliSec if true add milliseconds into the string,
     * no otherwise.
     * @return the actual tyme as a string formatted as: {@literal d-M-y_h-m-s-ms}.
     */
    public static String dataHourString( boolean addMilliSec){
        refreshTimeStrings();
        if ( addMilliSec)
            return(  day + "-" + month + "-" + year + "_" + hour + "-" + minute + "-" + ms + "_");
        return(  day + "-" + month + "-" + year + "_" + hour + "-" + minute + "_");
    }

    // refresh value of time to stream out as string
    private static void refreshTimeStrings(){
        date = Calendar.getInstance().getTime();
        year = new SimpleDateFormat("yyyy").format(date);
        month = new SimpleDateFormat("MM").format(date);
        day = new SimpleDateFormat("dd").format(date);
        hour =  new SimpleDateFormat("HH").format(date);
        minute = new SimpleDateFormat("mm").format(date);
        ms = new SimpleDateFormat("ss.SSS").format(date);
    }

}





//Then it close the program 
//arg[0] -> title of the dialog box
//arg[1]...arg[n]  -> errors divided by 'separator'
//backStep are the backward step on the steak to show method name and line number
//if arg = default => does not show nothing but you can call the other methods
/**
* This class is used to show a dialog box with informations about 
* the occurred error; than it closes the program. 
* 
* @author Buoncomapgni Luca
* @version 1.0
*
*/
class ShowError {

    private final String separator = ". \n";

    /**
     * Show error in a dialog box.
     *
     * @param backStep number of compilation trace steps to reproduce in
     * the error notification.
     * @param arg is a vector where: arg[0] is the title of the dialog
     * box while the other components are string to be notified.
     */
    public ShowError ( int backStep, String[] arg){

        if( !( arg[0].equals( "default") && arg.length == 1)){
            // build a string as a following of array 'arg' element
            String error = "";
            for( int i = 1; i < arg.length; i++)
                error += separator + arg[i];

            error += "\n\n      COMPILING INFO : \n";
            String[] compilingInfo = catchCaller( 3);
            String[] compilingInfoPlus = catchCaller( 4 + backStep);
            error += " package.class  : " + compilingInfo[ 0] + separator;
            error += " method  : " + compilingInfo[ 1] + " line number  : " + compilingInfo[ 2] + separator;
            error += "  called by : " + compilingInfoPlus[0] + "." + compilingInfoPlus[ 1] + "()   line:" + compilingInfoPlus[ 2] + separator;

            // show a dialog box
            JOptionPane a = new JOptionPane();
            JOptionPane.showMessageDialog(a, error, arg[0], JOptionPane.ERROR_MESSAGE);
            System.exit(0);
        }
    }


    /**
     * Retrieve and propose useful information for error recognition.
     *
     * @param backCallStepNumber number of compilation trace steps to reproduce in
     * the error notification.
     * @return return the name of package, class and method from which notifier has been called
     * return: str[0] = package.class, str[1] = method, str[2] = line number.
     */
    public String[] catchCaller( int backCallStepNumber){
        String[] ret = new String[ 3];
        StackTraceElement[] stacktrace = Thread.currentThread().getStackTrace();
        try{
            StackTraceElement e = stacktrace[ backCallStepNumber]; // [0] -> java, [1] -> catchCaller, [2] -> who call catchCaller, [3] -> who call who call catchCaller .....
            ret[ 0] = e.getClassName();
            ret[ 1] = e.getMethodName();
            Integer tmp = e.getLineNumber();
            ret[ 2] = tmp.toString();
            return( ret);
        }
        catch( java.lang.ArrayIndexOutOfBoundsException e){
            return( new String[] {" index out of : Thread.currentThread().getStackTrace() ",
                    " index out of : Thread.currentThread().getStackTrace() ",
                    " index out of : Thread.currentThread().getStackTrace() "});
        }
    }
}
