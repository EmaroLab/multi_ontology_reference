package it.emarolab.amor.owlDebugger.OFGUI;

import it.emarolab.amor.owlDebugger.OFGUI.individualGui.IndividualGuiRunner;
import it.emarolab.amor.owlInterface.OWLReferences;
import it.emarolab.amor.owlInterface.OWLReferencesInterface;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.search.EntitySearcher;

import javax.swing.*;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ClassTree implements TreeSelectionListener {

    private static JTree tree;
    private static JScrollPane treePanel;
    private static boolean initialized = false;
    private static DefaultMutableTreeNode top;

    private static Integer individualGuiID = 0;
    private static OWLReferences ontoRef;
    private TreeRender model;

    // ########## CREATE TREE AND COMPONENTS ##############

    // constructor build up and run tree visualization
    public ClassTree( boolean init){

        ontoRef = ClassExchange.getOntoRef();
        if( ontoRef != null){

            new TreeWorker( this).execute();

            if( ! initialized){ // the frist time build the tree
                EntryInfo ei = new EntryInfo( ClassExchange.defaultRootTree, ClassExchange.classIcon);
                top = new DefaultMutableTreeNode( ei);
                createNodes( top, ClassExchange.getRootClassname());
                EntryInfo.setTopTree(top);

                ClassExchange.addtoTreePanelObj( top);
                // configure a tree that allows one selection at a time.
                ClassExchange.getTreeObj().getSelectionModel().setSelectionMode
                       (TreeSelectionModel.SINGLE_TREE_SELECTION);
                //Enable tool tips.
                ToolTipManager.sharedInstance().registerComponent( ClassExchange.getTreeObj());
                //Set the icon for leaf nodes.
                model = new TreeRender();
                ClassExchange.getTreeObj().setCellRenderer( model);
                //Listen for when the selection changes.
                ClassExchange.getTreeObj().addTreeSelectionListener( this);

                tree = ClassExchange.getTreeObj();
                treePanel = ClassExchange.getTreePanelObj();

                initialized = true;
            }
        }
    }

    public static synchronized void doubleClick(MouseEvent e) {
        //ClassExchange.changeVisibilityProgressBar(true);
        if (e.getClickCount() == 2) {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode)
                    tree.getLastSelectedPathComponent();
            if (node == null) return;

            Object nodeInfo = node.getUserObject();
            final EntryInfo entry = (EntryInfo) nodeInfo;
            if ((entry.getIconNumber() == ClassExchange.individualcon) || (entry.getIconNumber() == ClassExchange.individualInfIcon)) {

                //Thread t = new Thread( new IndividualGuiRunner(  entry.toString(),individualGuiID));
                //t.start();
                new IndividualGuiRunner(entry.toString(), individualGuiID);

                individualGuiID++;
                if (individualGuiID >= Integer.MAX_VALUE)
                    individualGuiID = 0;
            }
            tree.setSelectionPath(null);
        }
        //ClassExchange.changeVisibilityProgressBar(false);
    }

    public static ArrayList<TreePath> find(String str, boolean exact) {
        @SuppressWarnings("unchecked")
        Enumeration<DefaultMutableTreeNode> e = top.depthFirstEnumeration();
        ArrayList<TreePath> paths = new ArrayList<TreePath>();
        while (e.hasMoreElements()) {
            DefaultMutableTreeNode node = e.nextElement();
            if (exact) {
                if (node.toString().equalsIgnoreCase(str)) {
                    paths.add(new TreePath(node.getPath()));
                }
            } else {
                if (node.toString().contains(str)) {
                    paths.add(new TreePath(node.getPath()));
                }
            }
        }
        return (paths);
    }

    // null if doesn't wxist
    // update the selected path
    // return the same path in the n
    public static TreePath isPath(JTree tree, TreePath path) {
        DefaultMutableTreeNode top = (DefaultMutableTreeNode) tree.getModel().getRoot();
        if (top != null) {
            Enumeration<DefaultMutableTreeNode> e = top.depthFirstEnumeration();
            while (e.hasMoreElements()) {
                DefaultMutableTreeNode node = e.nextElement();
                TreePath exploring = new TreePath(node.getPath());
                if (exploring.toString().equals(path.toString())) {
                    return (exploring);
                }
            }
        }
        return (null);
    }

    public static void expandAll(boolean expand_collapse) {
        try {
            for (int i = 1; i < tree.getRowCount(); i++)
                if (expand_collapse)
                    tree.expandRow(i);
                else tree.collapseRow(i);
            treePanel.getViewport().add(ClassExchange.getTreeObj());
        } catch (ArrayIndexOutOfBoundsException e) {
        }
    }

    public static Enumeration<TreePath> saveExpansionState(JTree tree) {
        return tree.getExpandedDescendants(new TreePath(tree.getModel().getRoot()));
    }

    public static void loadExpansionState(JTree tree, Enumeration<TreePath> enumeration) {
        if (enumeration != null) {

            while (enumeration.hasMoreElements()) {
                TreePath treePath = enumeration.nextElement();
                treePath = isPath(tree, treePath);
                if (treePath != null)
                    tree.expandPath(treePath); // not working for leaf nodes
            }
        } else expandAll(true);
    }

    // ########## OPERATE OVER A TREE #############

    public static synchronized JTree getTree() {
        return tree;
    }

    /**
     * @param tree the tree to set
     */
    protected static synchronized void setTree(JTree tree) {
        ClassTree.tree = tree;
    }

    /**
     * @return the treePanel
     */
    protected static synchronized JScrollPane getTreePanel() {
        return treePanel;
    }

    /**
     * @param treePanel the treePanel to set
     */
    protected static synchronized void setTreePanel(JScrollPane treePanel) {
        ClassTree.treePanel = treePanel;
    }

    /**
     * @return the top
     */
    protected synchronized DefaultMutableTreeNode getTop() {
        return top;
    }

    /**
     * @param top the top to set
     */
    protected synchronized void setTop(DefaultMutableTreeNode top) {
        ClassTree.top = top;
    }

    // create the tree recursively
    protected synchronized DefaultMutableTreeNode createNodes(DefaultMutableTreeNode top, String superClass) {
        //call recursively go deeply in the tree
        //while( ontoRef == null);
        synchronized (ontoRef.getOWLReasoner()){
            // support variable to folder creation
            DefaultMutableTreeNode category = null;
            DefaultMutableTreeNode infcategory = null;
            DefaultMutableTreeNode infindividual = null;
            EntryInfo tmpUpdateNode = null;

            // initialize quantity
            OWLClass topClass;
            Set<OWLClass> inferedCl = null;
            Set<OWLClassExpression> notInferedCl;
            // manage not string for owl:Thing
            if( superClass.equals( ClassExchange.Things)){
                topClass = ontoRef.getOWLFactory().getOWLThing().asOWLClass();
                Set<OWLClass> tmpInf = ontoRef.getOWLReasoner().getSubClasses(topClass, false).getFlattened();

                //notInferedCl = topClass.getSubClasses( ontoRef.getOWLOntology());
                Stream<OWLClassExpression> notInferedClStream = EntitySearcher.getSubClasses( topClass, ontoRef.getOWLOntology());
                notInferedCl = notInferedClStream.collect(Collectors.toSet());

                for( OWLClass n :tmpInf)
                    notInferedCl.add( n);

            } else {
                //topClass = ontoRef.getOWLFactory().getOWLClass( superClass, ontoRef.getPrefixFormat());
                topClass = ontoRef.getOWLFactory().getOWLClass( ontoRef.getPrefixFormat(superClass));
                inferedCl = ontoRef.getOWLReasoner().getSubClasses(topClass, false).getFlattened();

                //notInferedCl = topClass.getSubClasses(ontoRef.getOWLOntology());
                Stream<OWLClassExpression> notInferedClStream = EntitySearcher.getSubClasses( topClass, ontoRef.getOWLOntology());
                notInferedCl = notInferedClStream.collect(Collectors.toSet());
            }


            if( ! notInferedCl.isEmpty()){
                // for all the non asserted class
                for (OWLClassExpression infNo : notInferedCl){
                    if( ! infNo.equals( ontoRef.getOWLFactory().getOWLNothing())){
                        //add a new class
                        //tmpUpdateNode = new EntryInfo( ClassExchange.getRenderer().render( infNo), ClassExchange.classIcon);
                        tmpUpdateNode = new EntryInfo( OWLReferencesInterface.getOWLName( infNo), ClassExchange.classIcon);

                        category = new DefaultMutableTreeNode(tmpUpdateNode);
                        top.add( category);

                        // add not inferred individual
                        //Set<OWLIndividual> notInferedIn = infNo.asOWLClass().getIndividuals(ontoRef.getOWLOntology());
                        Stream<OWLIndividual> notInferedInStream = EntitySearcher.getIndividuals( infNo.asOWLClass(), ontoRef.getOWLOntology());
                        Set<OWLIndividual> notInferedIn = notInferedInStream.collect(Collectors.toSet());

                        for( OWLIndividual noInfInd : notInferedIn){
                            //tmpUpdateNode = new EntryInfo( ClassExchange.getRenderer().render( noInfInd), ClassExchange.individualcon);
                            tmpUpdateNode = new EntryInfo( OWLReferencesInterface.getOWLName( noInfInd), ClassExchange.individualcon);

                            category.add( new DefaultMutableTreeNode( tmpUpdateNode));

                        }


                        // coming back up to the root of the tree NN EFFICIENTE add inferred individual
                        boolean init = true;
                        Set<OWLNamedIndividual> inferedIndividual = ontoRef.getOWLReasoner().getInstances(infNo, false).getFlattened();
                        if( ! inferedIndividual .isEmpty())
                        for(OWLIndividual infInd : inferedIndividual){
                            if( ! infInd.equals( ontoRef.getOWLFactory().getOWLAnonymousIndividual())) {
                                if (!notInferedIn.contains( infInd)){
                                    // if assert add notify [ ICON classInfIconn]
                                    if( init){
                                        // if is assert add class notify
                                        tmpUpdateNode = new EntryInfo( ClassExchange.indAssertLabel, ClassExchange.predIndIcon);
                                        infindividual = new DefaultMutableTreeNode( tmpUpdateNode);
                                        init = false;
                                    }
                                    //tmpUpdateNode = new EntryInfo( ClassExchange.getRenderer().render(infInd), ClassExchange.individualInfIcon);
                                    tmpUpdateNode = new EntryInfo( OWLReferencesInterface.getOWLName( infInd), ClassExchange.individualInfIcon);

                                    DefaultMutableTreeNode a = new DefaultMutableTreeNode( tmpUpdateNode);
                                    infindividual.add( a);
                                    category.add( infindividual);
                                }
                            }
                        }
                        /*if( infindividual != null)
                            category.add( infindividual);*/

                        // call this function recorsively
                        //createNodes( category, ClassExchange.getRenderer().render(infNo));
                        createNodes( category, OWLReferencesInterface.getOWLName( infNo));

                    }
                }
            }

            // asserted Class
            boolean init = true;
            if( inferedCl != null)
                for(OWLClass inf : inferedCl){
                    if( ! inf.equals( ontoRef.getOWLFactory().getOWLNothing())) {
                        if (!notInferedCl.contains(inf)) {
                            // if assert add notify [ ICON classInfIconn]
                            if( init){
                                // if is assert add class notify
                                tmpUpdateNode = new EntryInfo( ClassExchange.classAssertLabel, ClassExchange.predClassIcon);
                                infcategory = new DefaultMutableTreeNode( tmpUpdateNode);
                                init = false;
                            }
                            //tmpUpdateNode = new EntryInfo( ClassExchange.getRenderer().render(inf), ClassExchange.classInfIcon);
                            tmpUpdateNode = new EntryInfo( OWLReferencesInterface.getOWLName( inf), ClassExchange.classInfIcon);

                            DefaultMutableTreeNode a = new DefaultMutableTreeNode( tmpUpdateNode);
                            infcategory.add( a);
                        }
                    }
                }
            if( infcategory != null)
                top.add( infcategory);


        }
        return( top);
    }

    // Required by TreeSelectionListener interface.
    // action listener: item selected
    public void valueChanged(TreeSelectionEvent e) {
        // implemented as double click
    }

    // manage icons in the tree
    private class TreeRender extends DefaultTreeCellRenderer {

        private static final long serialVersionUID = 1L;


        public Component getTreeCellRendererComponent(
                            JTree tree,
                            Object value,
                            boolean sel,
                            boolean expanded,
                            boolean leaf,
                            int row,
                            boolean hasFocus) {

            super.getTreeCellRendererComponent(
                            tree, value, sel,
                            expanded, leaf, row,
                            hasFocus);

            synchronized( ClassExchange.getTreeObj()){
                String txt = hasText(value);
                if( txt != null)
                    setText( txt);

                ImageIcon symb = hasIcond(value);
                if ( symb != null)
                     setIcon( symb);
                else {
                     System.err.println("icon missing; using default.");
                     setToolTipText(null); //no tool tip
                }

                if( txt != null && symb != null)
                    hasColor(txt);

                return this;
            }
        }

        protected ImageIcon hasIcond(Object value) {
            DefaultMutableTreeNode node =
                    (DefaultMutableTreeNode)value;
            try{
                EntryInfo nodeInfo = (EntryInfo)(node.getUserObject());
                return( nodeInfo.getIcon());
            }catch(java.lang.ClassCastException e){
                e.printStackTrace();
                return (null);
            }
        }

        protected String hasText(Object value) {
            DefaultMutableTreeNode node =
                    (DefaultMutableTreeNode)value;
            try{
                EntryInfo nodeInfo = (EntryInfo)(node.getUserObject());
                return( nodeInfo.toString());
            }catch(java.lang.ClassCastException e){
                e.printStackTrace();
                return (null);
            }
        }

        protected void hasColor( String txt) {
            if( ClassExchange.isColorMatchSearch()){
                // match all the string
                if( ClassExchange.getAllColorToFollow().keySet().contains( txt)){
                    setForeground( ClassExchange.getAllColorToFollow().get( txt));
                }else{
                    setForeground(ClassExchange.getNullcolor());
                }
            } else {
                // search for string which contains
                for( String stri : ClassExchange.getAllColorToFollow().keySet()){
                    String st = stri.toLowerCase();
                    String s = txt.toLowerCase();
                    if( s.contains( st)){
                        setForeground( ClassExchange.getAllColorToFollow().get( stri));
                        break;
                    } else
                        setForeground(ClassExchange.getNullcolor());
                }
            }
        }
    }
}



class TreeWorker extends SwingWorker< DefaultMutableTreeNode, String> {

    ClassTree caller;
    DefaultMutableTreeNode top;
    boolean excecute = false;

    public TreeWorker( ClassTree caller){
        this.caller = caller;
        if( ! excecute)
            this.execute();
    }

    @Override
    protected synchronized DefaultMutableTreeNode doInBackground() throws Exception {
        ClassExchange.setNewTreeObj( caller.getTop());
        excecute = true;
        ClassExchange.getProgressBar().setVisible( excecute);//.changeVisibilityProgressBar( true);

        EntryInfo ei  = new EntryInfo( ClassExchange.getRootClassname(), ClassExchange.classIcon);

        top = new DefaultMutableTreeNode( ei);
        top = caller.createNodes( top, ClassExchange.getRootClassname());
        ClassExchange.setNewTreeObj( top);
        ClassExchange.getTreeObj().expandPath(new TreePath(top.getPath()));

        excecute = false;
        ClassExchange.getProgressBar().setVisible( excecute);
        return top;
    }

    @Override
    protected synchronized void done(){
        synchronized( ClassTree.getTree()){
            if( top != null){
                caller.setTop( top);
                EntryInfo.setTopTree(top);

                ClassTree.setTree( ClassExchange.getTreeObj());
                ClassTree.setTreePanel( ClassExchange.getTreePanelObj());

                //ClassExchange.setNewTreeObj( top);
            }
        }
    }
}