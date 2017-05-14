package it.emarolab.amor.owlDebugger.OFGUI;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

public class EntryInfo {

    public String name;
    public int icon;

    private static DefaultMutableTreeNode topTree;

    public EntryInfo(String nameEntry, int iconEntry) {
        name = nameEntry;
        icon = iconEntry;
    }

    public int getIconNumber(){
        return icon;
    }

    public ImageIcon getIcon(){
        if ( icon == ClassExchange.classIcon) {
            return( ClassExchange.imClassIcon);
        } else if ( icon == ClassExchange.classInfIcon) {
            return( ClassExchange.imClassInfIcon);
        } else if ( icon == ClassExchange.individualcon) {
            return( ClassExchange.imIndividualIcon);
        } else if ( icon == ClassExchange.individualInfIcon) {
            return( ClassExchange.imIndividualInfIcon);
        } else if ( icon == ClassExchange.predClassIcon){
            return( ClassExchange.imClassPredIcon);
        } else if ( icon == ClassExchange.predIndIcon){
            return( ClassExchange.imIndividualPredIcon);
        } else return( null);
    }

    public String toString() {
        return name;
    }


    public static void setTopTree( DefaultMutableTreeNode top){
        topTree = top;
    }

    public static DefaultMutableTreeNode getTopTree(){
        return topTree;
    }

    public static ImageIcon path2icon( String path){
        Map<String, ImageIcon> imagePath = new HashMap< String, ImageIcon>();
        @SuppressWarnings("unchecked")
        Enumeration<DefaultMutableTreeNode> e = topTree.depthFirstEnumeration();
        while (e.hasMoreElements()) {
            DefaultMutableTreeNode node = e.nextElement();
            try{
                EntryInfo nodeInfo = (EntryInfo)(node.getUserObject());
                imagePath.put( new TreePath(node.getPath()).toString(), nodeInfo.getIcon());
            }catch(java.lang.ClassCastException ez){
                ez.printStackTrace();
            }
        }
        return( imagePath.get( path));
    }


}
