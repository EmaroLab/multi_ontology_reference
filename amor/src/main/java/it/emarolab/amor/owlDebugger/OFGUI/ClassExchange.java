package it.emarolab.amor.owlDebugger.OFGUI;

import it.emarolab.amor.owlDebugger.OFGUI.individualGui.ClassTableIndividual;
import it.emarolab.amor.owlInterface.OWLReferences;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.*;
import java.util.List;

//import uk.ac.manchester.cs.owlapi.dlsyntax.DLSyntaxObjectRenderer;

// collect shered quantity in the gui
public class ClassExchange {

    public static  final String labelTitle = " Class tree. Root : ";
    public static final String allInstancesButtonLabel = "Logs";
    public static final String serialiseFrameworkButtonLabel = "Serialize OF";
    public static final String legendButtonLabel = "legend";
    public static final String ontologyNameLabel = "Ontology Name:";
    public static final String expandeXhekBoxTip = "see all istances";
    public static final String classRootLabel = "Class Root :";
    public static final String findLabel = "Find :";
    public static final String defaultRootClass = "PredefinedOntology";//"rootTime";//JavaInterfaces";
    public static final String defaultSavingPath = System.getProperty("user.dir") + "/files/OntologyGui/";
    public final static String indAssertLabel = "[ asserted individual ]";
    public final static String classAssertLabel = "[ asserted class ]";
    public final static String defaultRootTree = classRootLabel;
    public final static String nonSameIndividual = " ( itself)";
    public static final String DEFAULTSERIALIZATIONPATHLABEL = " default";
    // key code
    public static final char ENTER = 10;
    public static final char MIN = 65;
    public static final char MAX = 90;
    public static final char CANC = 8;
    public static final char ESC = 27;
    public static final char RIGTH = 39;
    public static final char LEFT = 37;
    public static final char UP = 38;
    public static final char DOWN = 40;
    public final static int classIcon = 1;
    public final static int classInfIcon = 2;
    public final static int individualcon = 3;
    public final static int individualInfIcon = 4;
    public final static int predClassIcon = 5;
    public final static int predIndIcon = 6;
    public final static int dataPropIcon = 7;
    public final static int dataPropInfIcon = 8;
    public final static int objPropIcon = 9;
    public final static int objPropInfIcon = 10;
    public static final String Things = "Thing";
    // individual table types
    public static final Integer dataPropertyTable = 1;
    public static final Integer objectPropertyTable = 2;
    public static final Integer classTable = 3;
    public static final Integer sameIndividualTable = 4;
    // constants text in the gui
    private static final String defaultOntoName = "ontoName";
    private static final String expandLabel = "Expand all";
    private static final String frameLabel = "ClassTree";
    private static final String defaultSavingName = "";//"default";
    // icons
    private final static String iconpath = System.getProperty("user.dir") + "/files/icons/";//"images/middle.gif";
    private final static String iconClass = iconpath + "Class.gif";
    public final static ImageIcon imClassIcon = createImageIcon( iconClass);
    private final static String iconClassInf = iconpath + "inferedClass.gif";
    public final static ImageIcon imClassInfIcon = createImageIcon( iconClassInf);
    private final static String iconInd = iconpath + "Individual.gif";
    public final static ImageIcon imIndividualIcon = createImageIcon( iconInd);
    private final static String iconIndInf = iconpath + "inferedIndividual.gif";
    public final static ImageIcon imIndividualInfIcon = createImageIcon( iconIndInf);
    private final static String iconPredClass = iconpath + "predefineInfClass.gif";
    public final static ImageIcon imClassPredIcon = createImageIcon( iconPredClass);
    private final static String iconPredIndi = iconpath + "predefineInfInd.gif";
    public final static ImageIcon imIndividualPredIcon = createImageIcon(iconPredIndi);
    private final static String iconDataProp = iconpath + "dataProp.gif";
    public final static ImageIcon imDataPropIcon = createImageIcon( iconDataProp);
    private final static String iconDataPropInf = iconpath + "inferedDataProp.gif";
    public final static ImageIcon imDataPropInfIcon = createImageIcon( iconDataPropInf);
    private final static String iconObjProp = iconpath + "objProp.gif";
    public final static ImageIcon imObjPropIcon = createImageIcon( iconObjProp);
    private final static String iconObjPropInf = iconpath + "inferedObjProp.gif";
    public final static ImageIcon imObjPropInfIcon = createImageIcon( iconObjPropInf);
    private final static String iconAddColor = iconpath + "addColor.gif";
    public final static ImageIcon imAddColorIcon = createImageIcon( iconAddColor);
    private final static String iconDeleteColor = iconpath + "deleteColor.gif";
    public final static ImageIcon imDeleteColorIcon = createImageIcon( iconDeleteColor);
    private static final Color nullColor = new Color( 0, 0, 0);
    private static final Color alreadySelectedColor = new Color( 255, 255, 255);
    private static final Map<String, Color> StringtoColor = new HashMap<String, Color>();
    private static final List<Runnable> allIndividualFrame = new ArrayList<Runnable>();
    private static final Map<String, JCheckBox> selectedOntoSet = new HashMap<String, JCheckBox>();
    private static final Map<String, JCheckBox> buildedOntoSet = new HashMap<String, JCheckBox>();
    // graphic components
    private static JTextField txtOntoName;
    private static JComboBox cmbClassRoot;
    private static JCheckBox chckbxExpandAll;
    private static JComboBox cmbFind;
    private static JScrollPane treePanel;
    private static JFrame frame;
    private static JTree tree;
    private static JTree holdTree;
    private static JLabel intestLabel;
    private static JProgressBar progressBar;
    private static JFrame brosware_Frame;
    private static JTextField broswarePath_textField;
    private static JButton loadState_btn;
    private static JButton saveState_btn;
    private static JFileChooser fileChooser;
    private static boolean colorMatchSearch = true;
    // ontology variables
    /*private static OWLReasoner reasoner = null;
    private static OWLDataFactory factory = null;
    private static PrefixOWLOntologyFormat pm = null;
    private static OWLOntology ontology = null;
    private static OWLOntologyManager manager;*/
    //private static OWLObjectRenderer renderer;
    private static OWLReferences ontoRef;
    // value from gui
    private static String ontoName;
    private static String rootClassname = defaultRootClass;
    private static String savingPath = defaultSavingPath;
    private static String savingName = defaultSavingName;
    // all the instances of the table usefull in individual tunner??
    private static List< ClassTableIndividual> allDataTable = new ArrayList<ClassTableIndividual>();
    private static List< ClassTableIndividual> allObjTable = new ArrayList<ClassTableIndividual>();
    private static List< ClassTableIndividual> allSameIndTable = new ArrayList<ClassTableIndividual>();
    private static List< ClassTableIndividual> allClassTable = new ArrayList<ClassTableIndividual>();
    private static Map< String, Color> allColorToFollow = new HashMap<String, Color>();
    private static long individualFramPeriod = 2L * 1000;
    private static long allInstancesPeriod = 500L * 1000;
    private static long treePeriod = 1L * 1000;
    private static long SAVINGPERIOD = 170L * 1000;

    private static Boolean exportAssertion_flag = true;
    private static Boolean runScheduler_flag = true;
    private static Set<String> chosenLoadingPaths;

    public ClassExchange(){
        ontoName = defaultOntoName;
        initialise();
    }
    public ClassExchange(String customOntoName){
        ontoName = customOntoName;
        initialise();
    }
    public ClassExchange(String customOntoName, Long treePer, Long indPer, Long instancePer, Long savePer){
        ontoName = customOntoName;
        treePeriod = treePer;
        individualFramPeriod = indPer;
        allInstancesPeriod = instancePer;
        SAVINGPERIOD = savePer;
        initialise();
    }

    // ############### GRAPHIC COMPONENT ###############
    public static synchronized JScrollPane getTreePanelObj(){
        return( treePanel);
    }

    public static synchronized void addtoTreePanelObj( DefaultMutableTreeNode top){
        tree = new JTree(top);
        treePanel = new JScrollPane( tree);

        tree.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                ClassTree.doubleClick( e);
            }
        });
    }

    public static void setNewTreeObj( DefaultMutableTreeNode top){
        // retrive from status
        TreePath selected = tree.getSelectionPath();
        Enumeration<TreePath> state = ClassTree.saveExpansionState( holdTree);
        int h =treePanel.getHorizontalScrollBar().getValue();
        int v = treePanel.getVerticalScrollBar().getValue();
        treePanel.getViewport().remove(tree);

        // update new tree //this collapse all
        ((DefaultTreeModel)holdTree.getModel()).setRoot(top);

        // new status
        holdTree = tree;

        // set back the expansion
        ClassTree.loadExpansionState( tree, state); // doesn't work on leaf ????

        // set back the selection
        if( selected != null){
            // return the same path in the new tree or null otherwise
            TreePath treePath = ClassTree.isPath( tree, selected);
            if( treePath != null){
               tree.setSelectionPath( treePath);
            }
        }

        treePanel.getViewport().add(tree);
        treePanel.getHorizontalScrollBar().setValue(h);
        treePanel.getVerticalScrollBar().setValue(v);

        tree.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                ClassTree.doubleClick( e);
            }
        });
    }

    public static synchronized void refreshTreePanelObj( JTree jtree){
        treePanel.getViewport().add( jtree);
    }

    public static synchronized JTextField getOntoNameObj(){
        return( txtOntoName);
    }

    public static synchronized JComboBox getClassRootObj(){
        return( cmbClassRoot);
    }

    public static synchronized JComboBox getFindItemObj(){
        return( cmbFind);
    }

    public static synchronized JCheckBox getExpandAllObj(){
        return( chckbxExpandAll);
    }

    public static synchronized JFrame getFrameObj(){
        return(frame);
    }

    public static synchronized JTree getHoldTreeObj() {
        return holdTree;
    }

    public static synchronized void setHoldTreeObj(JTree holdTree) {
        ClassExchange.holdTree = holdTree;
    }

    public static synchronized JTree getTreeObj(){
        return(tree);
    }

    public static synchronized JLabel getIntestLabelObj(){
        return( intestLabel);
    }

    public static JProgressBar getProgressBar() {
        return( ClassExchange.progressBar);
    }

    public static synchronized void setProgressBar( JProgressBar progressbar) {
        ClassExchange.progressBar = progressbar;
        ClassExchange.progressBar.setIndeterminate( true);
    }

    public static synchronized void changeVisibilityProgressBar( boolean visible) {
        /*if( progressBar.isIndeterminate() != visible){
            if( visible){
                progressBar.setIndeterminate(true);
            }else progressBar.setIndeterminate(false);
        }*/
        //progressBar.setVisible( visible);


    }

    public static synchronized List<Runnable> getAllIndividualFrame() {
        return allIndividualFrame;
    }

    public static synchronized  void addAllIndividualFrame( Runnable IndividualFrame) {
        allIndividualFrame.add(IndividualFrame);
    }

    public static synchronized  void removeAllIndividualFrame( Runnable IndividualFrame) {
        allIndividualFrame.remove( IndividualFrame);
    }

    public static synchronized  void removeAllIndividualFrame( int idx) {
        allIndividualFrame.remove( idx);
    }

    public static synchronized Map<String, Color> getAllColorToFollow() {
        return allColorToFollow;
    }

    public static synchronized void setAllColorToFollow(Map<String, Color> allColorToFollow) {
        ClassExchange.allColorToFollow = allColorToFollow;
    }

    // ############### ONTOLOGY VARIABLE ###############
    /*
     * @return the ontoRef
     */
    public static synchronized OWLReferences getOntoRef() {
        return ontoRef;
    }

    /*
     * @param ontoRef the ontoRef to set
     */
    public static synchronized void setOntoRef(OWLReferences ontoRef) {
        ClassExchange.ontoRef = ontoRef;
    }

    public static synchronized String getOntoName() {
        return (ontoName);
    }

    /*public synchronized static OWLReasoner getOWLReasoner() {
        return ontoRef.getOWLReasoner();//reasoner;
    }
//    public synchronized static void setOWLReasoner(OWLReasoner reasoner) {
//        ClassExchange.reasoner = reasoner;
//    }

    public synchronized static OWLOntologyManager getOWLManager() {
        return ontoRef.getOWLManager();//manager;
    }
//    public synchronized static void setManager(OWLOntologyManager owlOntologyManager) {
//        ClassExchange.manager = owlOntologyManager;
//    }

    public synchronized static OWLDataFactory getOWLFactory() {
        return ontoRef.getOWLFactory();//factory;
    }
//    public synchronized static void setFactory(OWLDataFactory factory) {
//        ClassExchange.factory = factory;
//    }

    public synchronized static PrefixOWLOntologyFormat getPm() {
        return ontoRef.getPm();//pm;
    }
//    public synchronized static void setPm(PrefixOWLOntologyFormat pm) {
//        ClassExchange.pm = pm;
//    }

    public synchronized static OWLOntology getOWLOntology() {
        return ontoRef.getOWLOntology();//ontology;
    }
//    public synchronized static void setOntology(OWLOntology ontology) {
//        ClassExchange.ontology = ontology;
//    }
    */
    public synchronized static void setOntoName(String ontoName) {
        ClassExchange.ontoName = ontoName;
    }

    public static synchronized Color getNullcolor() {
        return nullColor;
    }
    /*public static synchronized OWLObjectRenderer getRenderer() {
        return renderer;
    }*/

    public static synchronized Color getAlreadyselectedcolor() {
        return alreadySelectedColor;
    }

    public static boolean isColorMatchSearch() {
        synchronized (tree) {
            return colorMatchSearch;
        }
    }

    /*public synchronized static Color getNewColor() {
        return newColor;
    }
    public synchronized static void setNewColor(Color newColor) {
        ClassExchange.newColor = newColor;
    }

    public static synchronized void setNewcolorcoordinate( int row, int column) {
        newColorCoordinate[ 0] = row;
        newColorCoordinate[ 1] = column;
    }

    public static synchronized Integer[] getNewcolorcoordinate() {
        return newColorCoordinate;
    }*/

    public static synchronized void setColorMatchSearch(boolean colorMatchSearch) {
        ClassExchange.colorMatchSearch = colorMatchSearch;
    }

    public static synchronized JButton getLoadState_btn() {
        return loadState_btn;
    }

    public static synchronized void setLoadState_btn(JButton loadState_btn) {
        ClassExchange.loadState_btn = loadState_btn;
    }

    // ############### INFO FROM GUI ###############
    public static synchronized String getRootClassname() {
        return rootClassname;
    }

    public static synchronized void setRootClassname(String rootClassName) {
        rootClassname = rootClassName;
        intestLabel.setText( labelTitle + rootClassname);
    }

    public static synchronized String getSavingPath() {
        return savingPath;
    }

    public static synchronized void setSavingPath(String savingPath) {
        ClassExchange.savingPath = savingPath;
    }

    public static synchronized String getSavingName() {
        return savingName;
    }

    public static synchronized void setSavingName(String savingName) {
        ClassExchange.savingName = savingName;
    }

    public static synchronized  List<ClassTableIndividual> getAllDataTable(){
        return( allDataTable);
    }

    public static synchronized  void addtoDataTable( ClassTableIndividual ct){
        allDataTable.add( ct);
    }

    public static synchronized  void removeFromDataTable( ClassTableIndividual ct){
        allDataTable.remove( ct);
    }

    public static synchronized  List<ClassTableIndividual> getAllObjTable(){
        return( allObjTable);
    }

    public static synchronized  void addtoObjTable( ClassTableIndividual ct){
        allObjTable.add( ct);
    }

    public static synchronized  void removeFromObjTable( ClassTableIndividual ct){
        allObjTable.remove( ct);
    }

    public static synchronized  List<ClassTableIndividual> getAllSameIndTable(){
        return( allSameIndTable);
    }

    public static synchronized  void addtoSameIndTable( ClassTableIndividual ct){
        allSameIndTable.add( ct);
    }

    public static synchronized  void removeFromSameIndTable( ClassTableIndividual ct){
        allSameIndTable.remove( ct);
    }

    public static synchronized  List<ClassTableIndividual> getAllClassTable(){
        return( allClassTable);
    }

    public static synchronized  void addtoClassTable( ClassTableIndividual ct){
        allClassTable.add( ct);
    }

    public static synchronized void removeFromClassTable( ClassTableIndividual ct){
        allClassTable.remove( ct);
    }

    // ############### AUXILIARE METHOD ###############
    private static synchronized ImageIcon createImageIcon(String path) {
        return new ImageIcon(path);
    }

    public static synchronized Map<String, Color> getStringtoColor() {
        return StringtoColor;
    }

    public static synchronized void addStringtoColor( String tofollow, Color color){
        StringtoColor.put( tofollow, color);
    }

    public static synchronized void removeStringtoColor( String tofollow){
        StringtoColor.remove( tofollow);
    }

    public static synchronized void removeStringtoColor( Color color){
        StringtoColor.remove( color);
    }

    public static synchronized void addToSelectedOntoSet( String toadd, JCheckBox value){
        selectedOntoSet.put( toadd, value);
    }

    public static synchronized void removeFromSelectedOntoSet( String toRemove){
        selectedOntoSet.remove( toRemove);
    }

    public static synchronized void removeFromSelectedOntoSet(Set<String> toRemove) {
        selectedOntoSet.remove( toRemove);
    }

    public static synchronized void clearSelectedOntoSet( ){
        selectedOntoSet.clear();
    }

    public static synchronized Map<String, JCheckBox> getSelectedOntoSet(){
        return( selectedOntoSet);
    }

    public static synchronized Set<String> getTrueSelectedOntoSet(){
//        List<String> sortedKeys=new ArrayList<String>(selectedOntoSet.keySet());
//        Collections.sort(sortedKeys);
        Set<String> out = new HashSet<String>();
        for( String s : selectedOntoSet.keySet()){
             JCheckBox box = selectedOntoSet.get( s);
             if( box.isSelected())
                 out.add( s);
        }

        return( out);
    }

    public static synchronized void addToBuildedOntoSet( String toadd, JCheckBox value){
        buildedOntoSet.put( toadd, value);
    }

    public static synchronized void removeFromBuildedOntoSet( String toRemove){
        buildedOntoSet.remove( toRemove);
    }

    public static synchronized void removeFromBuildedOntoSet(Set<String> toRemove) {
        buildedOntoSet.remove( toRemove);
    }

    public static synchronized void clearBuildedOntoSet( ){
        buildedOntoSet.clear();
    }

    public static synchronized Map<String, JCheckBox> getBuildedOntoSet(){
        return( buildedOntoSet);
    }

    public static synchronized Set<String> getTrueBuildedOntoSet(){
        Set<String> out = new HashSet<String>();
        for( String s : buildedOntoSet.keySet()){
             JCheckBox box = buildedOntoSet.get( s);
             if( box.isSelected())
                 out.add( s);
        }
        return( out);
    }

    /*
     * @return the runScheduler_flag
     */
    public static synchronized Boolean getRunSchedulerFlag() {
        return runScheduler_flag;
    }

    /*
     * @param runScheduler_flag the runScheduler_flag to set
     */
    public static synchronized void setRunSchedulerFlag(Boolean runScheduler_flag) {
        ClassExchange.runScheduler_flag = runScheduler_flag;
    }

    /*
     * @return the exportAssertion_flag
     */
    public static synchronized Boolean getExportAssertionFlag() {
        return exportAssertion_flag;
    }

    /*
     * @param exportAssertion_flag the exportAssertion_flag to set
     */
    public static synchronized void setExportAssertionFlag(Boolean exportAssertion_flag) {
        ClassExchange.exportAssertion_flag = exportAssertion_flag;
    }

    /*
     * @return the saveState_btn
     */
    public static synchronized JButton getSaveState_btn() {
        return saveState_btn;
    }
    /*
     * @param saveState_btn the saveState_btn to set
     */
    public static synchronized void setSaveState_btn(JButton saveState_btn) {
        ClassExchange.saveState_btn = saveState_btn;
    }

    /*
     * @return the brosware_Frame
     */
    public static synchronized JFrame getBroswareFrame() {
        return brosware_Frame;
    }

    /*
     * @param brosware_Frame the brosware_Frame to set
     */
    public static synchronized void setBroswareFrame(JFrame brosware_Frame) {
        ClassExchange.brosware_Frame = brosware_Frame;
    }

    /*
     * @return the broswarePath_textField
     */
    public static synchronized JTextField getBroswarePathtextField() {
        return broswarePath_textField;
    }

    /*
     * @param broswarePathtextField the broswarePath_textField to set
     */
    public static synchronized void setBroswarePathtextField(JTextField broswarePathtextField) {
        broswarePath_textField = broswarePathtextField;
    }

    /*
     * @return the chosenLoadingPaths
     */
    public static Set<String> getChosenLoadingPaths() {
        return chosenLoadingPaths;
    }
    /*
     * @param paths
     */
    public static synchronized void setChosenLoadingPaths( Set<String> paths) {
        chosenLoadingPaths = paths;
    }

    /*
     * @return the fileChooser
     */
    public static synchronized JFileChooser getFileChooser() {
        return fileChooser;
    }

    /*
     * @param fileChooser the fileChooser to set
     */
    public static synchronized void setFileChooser(JFileChooser fileChooser) {
        ClassExchange.fileChooser = fileChooser;
    }

    /*
     * @return the individualFramPeriod
     */
    public static synchronized long getIndividualFramPeriod() {
        return individualFramPeriod;
    }

    /*
     * @param individualFramPeriod the individualFramPeriod to set
     */
    public static synchronized void setIndividualFramPeriod(long individualFramPeriod) {
        ClassExchange.individualFramPeriod = individualFramPeriod;
    }

    /*
     * @return the allInstancesPeriod
     */
    public static synchronized long getAllInstancesPeriod() {
        return allInstancesPeriod;
    }

    /*
     * @param allInstancesPeriod the allInstancesPeriod to set
     */
    public static synchronized void setAllInstancesPeriod(long allInstancesPeriod) {
        ClassExchange.allInstancesPeriod = allInstancesPeriod;
    }

    /*
     * @return the treePeriod
     */
    public static synchronized long getTreePeriod() {
        return treePeriod;
    }

    /*
     * @param treePeriod the treePeriod to set
     */
    public static synchronized void setTreePeriod(long treePeriod) {
        ClassExchange.treePeriod = treePeriod;
    }

    /*
     * @return the sAVINGPERIOD
     */
    public static synchronized long getSAVINGPERIOD() {
        return SAVINGPERIOD;
    }

    /*
     * @param sAVINGPERIOD the sAVINGPERIOD to set
     */
    public static synchronized void setSAVINGPERIOD(long sAVINGPERIOD) {
        SAVINGPERIOD = sAVINGPERIOD;
    }

    //static{
    private void initialise() {
        //renderer = new DLSyntaxObjectRenderer();
        frame = new MyFrame(frameLabel);//new JFrame( frameLabel);
        txtOntoName = new JTextField(ontoName);
        cmbClassRoot = new JComboBox();
        cmbFind = new JComboBox();
        chckbxExpandAll = new JCheckBox(expandLabel);
        treePanel = new JScrollPane();
        intestLabel = new JLabel(labelTitle + rootClassname);
        broswarePath_textField = new JTextField();
        loadState_btn = new JButton("Load Framework State");
        saveState_btn = new JButton("Save Framework State");
    }
}
