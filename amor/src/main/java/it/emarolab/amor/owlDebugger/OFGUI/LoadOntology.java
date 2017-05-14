package it.emarolab.amor.owlDebugger.OFGUI;

import it.emarolab.amor.owlDebugger.FileManager;
import it.emarolab.amor.owlInterface.OWLReferences;
import it.emarolab.amor.owlInterface.OWLReferencesInterface.OWLReferencesContainer;

import javax.swing.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;

public class LoadOntology {

    static{
        updateOntology( false);
    }

    public static synchronized void updateOntology( boolean initialized){
        if( initialized)
            ClassExchange.changeVisibilityProgressBar(true);

        // read value and update ClassExchange
        String ontoName = ClassExchange.getOntoNameObj().getText();

        // import Ontology
        OWLReferences ontoRef = (OWLReferences) OWLReferencesContainer.getOWLReferences( ontoName);
        if( ontoRef != null){
            //ClassExchange.setOntoName( ontoName);
            /*ClassExchange.setReasoner( ontoRef.getOWLReasoner());
            ClassExchange.setFactory( ontoRef.getFactory());
            ClassExchange.setPm( ontoRef.getPm());
            ClassExchange.setOntology( ontoRef.getOntology());
            ClassExchange.setManager( ontoRef.getManager());*/
            ClassExchange.setOntoRef( ontoRef);
        } else {
            // show a dialog box
            JOptionPane a = new JOptionPane();
            String message = "No innstance of OntologyManager exists with name : " + ontoName + ".\n No changes will take place.";
            String title = "Unknown ontology name";
            JOptionPane.showMessageDialog( a, message, title, JOptionPane.ERROR_MESSAGE);
        }

        if(initialized)
            ClassExchange.changeVisibilityProgressBar(false);
    }

    // if true print on file the ontology
    // if false save the ontology
    public static synchronized void saveOntology( boolean toOWL){
        OWLReferences ontoRef = ClassExchange.getOntoRef();

        String message;
        String format;
        ArrayList<String> strs;
        if( ! toOWL){
            format = "txt";
            strs = new ArrayList<String>();
            BufferedReader tmp = getOntologyTokens();
            boolean loop =true;
            String line;
            try {
                line = tmp.readLine();
                while( loop){
                    if( line == null)
                        loop = false;
                    else{
                        loop = true;
                        line = tmp.readLine();
                        strs.add( line);
                    }
                }
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                message = "IO Exception " + e.getCause() + System.getProperty("line.separator") +
                        e.getMessage();
            }
        }
        else{
            format = "owl";
            strs = new ArrayList<String>( 1);
            strs.add( ontoRef.getReferenceName());
        }

        String name = ClassExchange.getSavingName();
        boolean nameOk = false;
        if( (name != null) && ( ! name.trim().isEmpty()) &&( ! name.equals( "default")))
                nameOk = true;

        if( nameOk){
            String path = ClassExchange.getSavingPath();
            FileManager fm;
            try{
                if( (path == null) || ( path.trim().isEmpty()) || ( path.equals( ClassExchange.getSavingPath())))
                    fm = new FileManager( "default", name, format, ClassExchange.defaultSavingPath);
                else
                    fm = new FileManager( path, name, format);


                fm.loadFile();
                fm.printOnFile( strs, true);

                message = " saving this ontology configuration as : " + fm.getFileFormat() + " format.\n"
                        + " on the path : " + fm.getFilePath();

                fm.closeFile();

            } catch( java.lang.NullPointerException e){
                message = " NULL Exception" + e.getCause() + System.getProperty("line.separator") +
                        e.getMessage();
                e.printStackTrace();
            }
        }else message = " given name not valid.";

        // show message
        JOptionPane a = new JOptionPane();
        JOptionPane.showMessageDialog(a, message, "saving file", JOptionPane.INFORMATION_MESSAGE);
    }

    // print ontology and return the string
    @Deprecated
    public static synchronized BufferedReader getOntologyTokens(){
    /*    OWLReferences ontoRef = ClassExchange.getOntoRef();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(baos);

        StreamDocumentTarget print = new StreamDocumentTarget( ps);// new SystemOutDocumentTarget();

        try {
            ManchesterOWLSyntaxOntologyFormat manSyntaxFormat = new ManchesterOWLSyntaxOntologyFormat();
            OWLOntologyFormat format =  ontoRef.getManager().getOntologyFormat(ontoRef.getOntology());
            if (format.isPrefixOWLOntologyFormat())
                manSyntaxFormat.copyPrefixesFrom(format.asPrefixOWLOntologyFormat());
            ontoRef.getManager().saveOntology( ontoRef.getOntology(), manSyntaxFormat, print);
        } catch (OWLOntologyStorageException e) {
            e.printStackTrace();
        }

        StringBuilder buffer = new StringBuilder();
        buffer.append( baos.toString());
        BufferedReader br = new BufferedReader(new StringReader(buffer.toString()));

        return( br);
        */
        return null;
    }
}
