package it.emarolab.amor.owlDebugger.OFGUI.allInstancesGUI;

import it.emarolab.amor.owlDebugger.OFGUI.ClassExchange;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.util.Map;

public class ColorRenderer extends JLabel
                            implements TableCellRenderer {
    
    private static final long serialVersionUID = 1L;
    Border unselectedBorder = null;
    Border selectedBorder = null;
    boolean isBordered = true;
    
        
    Map<String, Color> all;

    public ColorRenderer(boolean isBordered) {
        this.isBordered = isBordered;
        setOpaque(true); //MUST do this for background to show up.        
    }

    public Component getTableCellRendererComponent(
                            JTable table, Object text,
                            boolean isSelected, boolean hasFocus,
                            int row, int column) {
    
        if( text instanceof String){
            String str = (String) text;
            setText( str);
            str = str.replace( ClassExchange.nonSameIndividual, "");
            if( ClassExchange.isColorMatchSearch()){
                // match all the string
                if( ClassExchange.getAllColorToFollow().keySet().contains( str)){
                    setForeground( ClassExchange.getAllColorToFollow().get( str));
                }else{
                    setForeground(ClassExchange.getNullcolor());
                }
            } else {
                // search for string which contains
                for( String stri : ClassExchange.getAllColorToFollow().keySet()){
                    String st = stri.toLowerCase();
                    String s = str.toLowerCase();
                    if( s.contains( st)){
                        setForeground( ClassExchange.getAllColorToFollow().get( stri));
                        break;
                    } else
                        setForeground(ClassExchange.getNullcolor());
                }
            }
        }

        // change colour of what are you going to choose
        UIDefaults defaults = javax.swing.UIManager.getDefaults();
        if (isSelected) {
            setBackground( defaults.getColor("List.selectionBackground"));
        } else {
            setBackground( table.getBackground());
        }

        return this;
    }

    
 }
