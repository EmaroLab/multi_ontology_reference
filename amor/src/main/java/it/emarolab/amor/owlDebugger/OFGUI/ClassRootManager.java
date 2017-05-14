package it.emarolab.amor.owlDebugger.OFGUI;

import it.emarolab.amor.owlInterface.OWLReferences;
import it.emarolab.amor.owlInterface.OWLReferencesInterface;
import org.semanticweb.owlapi.model.OWLClass;

import javax.swing.*;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Set;


public class ClassRootManager {

    static{
        setRootClass();
    }

    public static void setRootClass(){
        // update the name, also in the intestation label
        String in = (String) ClassExchange.getClassRootObj().getSelectedItem();
        if( in != null)
            ClassExchange.setRootClassname( in);
    }

    public static ArrayList<String> settRootWiev(){
        OWLReferences ontoRef = ClassExchange.getOntoRef();
        // update root class combo box items
        Set<OWLClass> allClass = ontoRef.getOWLOntology().getClassesInSignature();
        String tmp;
        ArrayList<String> allClbox = new ArrayList<String>();
        allClbox.add( ClassExchange.Things); // care about OWLThing
        for( OWLClass cl : allClass){

            //tmp = ClassExchange.getRenderer().render( cl);
            tmp = OWLReferencesInterface.getOWLName( cl);

            if( ! cl.equals( ontoRef.getOWLFactory().getOWLThing())){
                if( ClassExchange.getClassRootObj().getEditor().getItem() == null)
                    allClbox.add( tmp);
                else if( tmp.toLowerCase().contains( ((String) ClassExchange.getClassRootObj().getEditor().getItem()).toLowerCase()))
                    allClbox.add( tmp);

            } //else System.out.println( "founf OWLThing ");
        }
        return( allClbox);
    }
    
    // simulate enter
    public static void updateComboBox(){
        synchronized( ClassExchange.getTreeObj()){
            setRootClass(); // update root Class
            new ClassTree( true);
            updateExpandAll( true);
            ClassExchange.getClassRootObj().setFocusable(false);
            ClassExchange.getClassRootObj().setFocusable(true);
        }
    }
    
    public static void updateComboBox( KeyEvent e){

        ClassExchange.changeVisibilityProgressBar(true);

        ArrayList<String> allClbox = null;
        if( e.getKeyCode() == ClassExchange.ENTER){
            setRootClass(); // update root Class
            new ClassTree( true);
            updateExpandAll( true);
            ClassExchange.getClassRootObj().setFocusable(false);
            ClassExchange.getClassRootObj().setFocusable(true);
        } else if ( (e.getKeyChar() == ClassExchange.ESC)){
            ClassExchange.getClassRootObj().setFocusable(false);
            ClassExchange.getClassRootObj().setFocusable(true);
        }else if( (e.getKeyCode() != ClassExchange.UP) && (e.getKeyCode() != ClassExchange.DOWN) &&
                (e.getKeyCode() != ClassExchange.RIGTH) && (e.getKeyCode() != ClassExchange.LEFT) &&
                (e.getKeyCode() != ClassExchange.CANC) )
            allClbox = settRootWiev();

        // store the txt
        Object rootClTextStatus = ClassExchange.getClassRootObj().getEditor().getItem();
        int itemcount = ClassExchange.getClassRootObj().getItemCount();

        if( allClbox != null){
            if( allClbox.isEmpty()){
                for( String s: allClbox){
                    // update the combo list (for all items)
                    for( int i = 0; i < itemcount; i++){
                        String holdIt = (String) ClassExchange.getClassRootObj().getItemAt( i);
                        // remove if it does not compare in the items
                        if( ! s.contains( holdIt)){
                            ClassExchange.getClassRootObj().removeItem( holdIt);
                            itemcount--;
                        }
                        if( ! holdIt.contains( s)){
                            ClassExchange.getClassRootObj().addItem( holdIt);
                            itemcount++;
                        }
                    }
                }
            }      // show all item
            else
                ClassExchange.getClassRootObj().setModel(new DefaultComboBoxModel( allClbox.toArray()));
        }

        ClassExchange.getClassRootObj().getEditor().setItem( rootClTextStatus);
        ClassExchange.getClassRootObj().setSelectedItem( rootClTextStatus);
        ClassExchange.getClassRootObj().showPopup();

        ClassExchange.changeVisibilityProgressBar(false);
    }

    public static void updateExpandAll( boolean initialized){
        if( initialized)
            ClassExchange.changeVisibilityProgressBar(true);

        boolean sel = ClassExchange.getExpandAllObj().isSelected();
        ClassTree.expandAll( sel);

        if( initialized)
            ClassExchange.changeVisibilityProgressBar(false);
    }
}
