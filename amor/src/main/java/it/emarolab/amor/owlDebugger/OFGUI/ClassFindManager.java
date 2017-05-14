package it.emarolab.amor.owlDebugger.OFGUI;

import javax.swing.*;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.ArrayList;

public class ClassFindManager {

    private static int cicleFocus;
    static ArrayList<TreePath> allPaths;

    static{
        ComboBoxRenderer renderer = new ComboBoxRenderer();
        ClassExchange.getFindItemObj().setRenderer(renderer);
    }

    public static ArrayList<String> settFindWiev(){
        //cicleFocus = 0;
        allPaths = ClassTree.find( (String) ClassExchange.getFindItemObj().getEditor().getItem(), true);
        ArrayList<String> allFindbox = new ArrayList<String>();
        for( TreePath cl : allPaths){
            if( ClassExchange.getFindItemObj().getEditor().getItem() == null)
                allFindbox.add( cl.toString());
            else if( cl.toString().toLowerCase().contains( ((String) ClassExchange.getFindItemObj().getEditor().getItem()).toLowerCase()))
                allFindbox.add( cl.toString());
        }
        return( allFindbox);
    }

    //simulate enter
    public static void updateComboBox(){
        cicleFocus = ClassExchange.getFindItemObj().getSelectedIndex();
        ClassExchange.getTreeObj().setSelectionPath( allPaths.get( cicleFocus));
        ClassExchange.getTreeObj().scrollPathToVisible( allPaths.get(cicleFocus++));
        if( (cicleFocus >= allPaths.size()) || ( cicleFocus < 0))
            cicleFocus = 0;
        ClassExchange.getFindItemObj().setSelectedIndex( cicleFocus);
    }
    public static void updateComboBox( KeyEvent e){
        ClassExchange.changeVisibilityProgressBar(true);

        ArrayList<String> allFindbox;
        boolean init = false;


        if( e.getKeyCode() == ClassExchange.ENTER){
            if( ! init)
                cicleFocus = ClassExchange.getFindItemObj().getSelectedIndex();
            ClassExchange.getTreeObj().setSelectionPath( allPaths.get( cicleFocus));
            ClassExchange.getTreeObj().scrollPathToVisible( allPaths.get(cicleFocus++));
            if( (cicleFocus >= allPaths.size()) || ( cicleFocus < 0))
                cicleFocus = 0;
            ClassExchange.getFindItemObj().setSelectedIndex( cicleFocus);
        } else if ( (e.getKeyChar() == ClassExchange.ESC)){
            ClassExchange.getFindItemObj().setFocusable(false);
            ClassExchange.getFindItemObj().setFocusable(true);
            init = true;
        }else if( (e.getKeyCode() != ClassExchange.UP) && (e.getKeyCode() != ClassExchange.DOWN) &&
                (e.getKeyCode() != ClassExchange.RIGTH) && (e.getKeyCode() != ClassExchange.LEFT) &&
                (e.getKeyCode() != ClassExchange.CANC) ){
            init = true;
            allFindbox = settFindWiev();

            // store the txt
            Object rootClTextStatus = ClassExchange.getFindItemObj().getEditor().getItem();
            int itemcount = ClassExchange.getFindItemObj().getItemCount();

            if( allFindbox.isEmpty()){
                for( String s: allFindbox){
                    // update the combo list (forall items)
                    for( int i = 0; i < itemcount; i++){
                        String holdIt = (String) ClassExchange.getFindItemObj().getItemAt( i);
                        // remove if it does not compare in the items
                        if( ! s.contains( holdIt)){
                            ClassExchange.getFindItemObj().removeItem( holdIt);
                            itemcount--;
                        }
                        if( ! holdIt.contains( s)){
                            ClassExchange.getFindItemObj().addItem( holdIt);
                            itemcount++;
                        }
                    }
                }
            }      // show all item
            else ClassExchange.getFindItemObj().setModel(new DefaultComboBoxModel( allFindbox.toArray()));

            ClassExchange.getFindItemObj().getEditor().setItem( rootClTextStatus);
            ClassExchange.getFindItemObj().setSelectedItem( rootClTextStatus);
            ClassExchange.getFindItemObj().showPopup();
        }

        ClassExchange.changeVisibilityProgressBar(false);
    }


}


class ComboBoxRenderer extends JLabel implements ListCellRenderer {

    private static final long serialVersionUID = 1L;

    public ComboBoxRenderer() {
        setOpaque(true);
    }

    public Component getListCellRendererComponent(
            JList list,
            Object value,
            int index,
            boolean isSelected,
            boolean cellHasFocus) {


        // change colour of what are you going to choose
        UIDefaults defaults = javax.swing.UIManager.getDefaults();
        if (isSelected) {
            setBackground(defaults.getColor("List.selectionBackground"));
            setForeground(defaults.getColor("List.selectionForeground"));
        } else {
            setBackground(list.getBackground());
            setForeground(list.getForeground());
        }


        setText( hasText( value));
        ImageIcon symb = hasIcond(value);
        if ( symb != null)
             setIcon( symb);
        else {
             System.err.println("icon missing; using default.");
             setToolTipText(null); //no tool tip
        }
        return this;
    }

    protected String hasText(Object value) {
        String path = ( String) value;
        path = path.replaceAll( ClassExchange.indAssertLabel, "");
        path = path.replaceAll( ClassExchange.classAssertLabel, "");
        return( path);
    }

    protected ImageIcon hasIcond(Object value) {
        return( EntryInfo.path2icon( ( String) value));
    }
}
    

