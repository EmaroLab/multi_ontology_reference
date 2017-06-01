package it.emarolab.amor.owlDebugger.OFGUI.individualGui;

import it.emarolab.amor.owlDebugger.OFGUI.ClassExchange;
import it.emarolab.amor.owlDebugger.OFGUI.LoadOntology;

import javax.swing.*;
import javax.swing.GroupLayout.Alignment;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;

public class IndividualGuiRunner implements Runnable{//extends TimerTask {

    static int count = 0;
    static boolean initialized = false;

    static List<Integer> allFrameID = new ArrayList<Integer>();
    static List<String> allName = new ArrayList<String>();

    private String individualname;
    private Integer frameID;
    private Thread thisThread;

    public IndividualGuiRunner(final String individualname, final Integer frameID) {
        this.individualname = individualname;
        this.frameID = frameID;
        thisThread = new Thread( this);
        thisThread.start();
    }
    public synchronized Thread getThisThread() {
        return thisThread;
    }

    public synchronized static List<Integer> getAllframeid() {
        return allFrameID;
    }

    public synchronized static void removetoAlframeid(int index) {
        allFrameID.remove( allFrameID.indexOf(index));
    }

    public synchronized static void addtoAlframeid(int value) {
        allFrameID.add( value);
    }

    public synchronized static void removefromAllName(String value) {
        allName.remove( value);
    }

    private boolean interrupt = false;
    public void setInterrupt( boolean flag){
        interrupt = flag;
    }


    @Override
    public synchronized void run() {
        long initialTime = System.currentTimeMillis();

        while( !interrupt){// true){
            try {
                if( ! allName.contains( individualname)){
                    if ( !allFrameID.contains(frameID)) {
                        if( !IndividualRunner.getJustclosed().contains( frameID)){
                            IndividualRunner a = new IndividualRunner(individualname, frameID);
                            javax.swing.SwingUtilities.invokeLater( a);
                            a.setRunnable( a , this);
                            ClassExchange.addAllIndividualFrame( a);

                            allName.add( individualname);
                        }
                    }
                }

                for (Runnable tr : ClassExchange.getAllIndividualFrame())
                    javax.swing.SwingUtilities.invokeLater( tr);

                long done = System.currentTimeMillis() - initialTime;
                Thread.sleep( ClassExchange.getIndividualFramPeriod() - done);
                initialTime = System.currentTimeMillis();
            } catch (InterruptedException e) {
                break;
            } catch ( java.lang.IllegalArgumentException e1){
                System.out.println( "Gui runner miss dead-line !!");
                break;
            }
        }
    }

}




class IndividualRunner implements Runnable {



    private static final String saveTxt = "save as txt file";
    private static final String saveOwl = "save as OWL file";
    private static final String editBtnLabel = "edit";
    private static final String axiomSummaryLabel = "Axiom Proprieties (Summary)";

    private int frameId;
    private String individualName;

    private JTextArea textarea = new JTextArea();
    private JFrame frame;
    private final JCheckBox savecheck = new JCheckBox();
    private static final boolean savechekInitialState = true;
    private static final List< Integer> justClosed = new ArrayList<Integer>();

    private static ClassTableIndividual dataTable;
    private static ClassTableIndividual objTable;
    private static ClassTableIndividual sameIndTable;
    private static ClassTableIndividual classTable;

    private Runnable thisInstance = null;
    private IndividualGuiRunner calledInstances;

    public IndividualRunner(String individualname, Integer frameID) {
        this.individualName = individualname;
        this.frameId = frameID;
    }

    public synchronized void setRunnable(Runnable runnable, IndividualGuiRunner individualGuiRunner) {
        thisInstance = runnable;
        calledInstances = individualGuiRunner;
    }

    public synchronized static List< List< String>> getEditorInfo(){
        List< List< String>> editorInfo = new ArrayList< List< String>>();
        int loop = 0;

        while( loop >= 0){
            JTable table = null;
            switch( loop){
            case 0 : table = dataTable.getTable();
                break;
            case 1 : table = objTable.getTable();
                break;
            case 2 : table = sameIndTable.getTable();
                break;
            case 3 : table = classTable.getTable();
            }
            if( loop + 1 > 3)
                loop = -1; // exit
            else loop++;


            int selRow = table.getSelectedRow();
            List< String> tmp = new ArrayList< String>();
            if( selRow >= 0){
                // for all the column skipping the logo
                for( int i = 1; i < table.getColumnCount(); i++)
                    tmp.add( (String) table.getValueAt( selRow, i));
            }
            editorInfo.add( tmp);


        }

        return( editorInfo);
    }

    private JPanel panel;
    private JSplitPane splitPane;
    private JPanel panel_1;
    private JSplitPane splitPane_1 ;
    private JPanel panel_4;
    private JSplitPane splitPane_2;
    private JPanel panel_3;

    // run the GUI
    public synchronized void run() {
        ClassExchange.changeVisibilityProgressBar(true);

        boolean initialized;
        if (!IndividualGuiRunner.getAllframeid().contains(frameId)){
            initialized = false;
            IndividualGuiRunner.addtoAlframeid(frameId);
        }else initialized = true;

        if( ! initialized){
            // create and show GUI
            frame = new JFrame(individualName);
            frame.setBounds(100, 100, 100, 120);
            frame.setPreferredSize(new Dimension(440, 500));
            setWindows();
        } else{
            try{
                dataTable.saveSelection();
                dataTable.update();
                dataTable.getModel().fireTableDataChanged();
                dataTable.restoreSelection();

                objTable.saveSelection();
                objTable.update();
                objTable.getModel().fireTableDataChanged();
                objTable.restoreSelection();

                classTable.saveSelection();
                classTable.update();
                classTable.getModel().fireTableDataChanged();
                classTable.restoreSelection();

                sameIndTable.saveSelection();
                sameIndTable.update();
                sameIndTable.getModel().fireTableDataChanged();
                sameIndTable.restoreSelection();
            } catch ( java.lang.NullPointerException e){
                closeWindows();
            }
        }

        if( ! initialized){
            frame.pack();
            frame.setVisible(true);

            frame.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent e) {
                    closeWindows();
                }

            });
        }

        ClassExchange.changeVisibilityProgressBar(false);
    }

    protected synchronized void closeWindows() {

        // meccessario?? non testatp ... salva le istanze delle
        // tabelle
        ClassExchange.removeFromDataTable(dataTable);
        ClassExchange.removeFromObjTable(objTable);
        ClassExchange.removeFromSameIndTable(sameIndTable);
        ClassExchange.removeFromClassTable(classTable);

        // remove following quntity
        FollowingManager fm = FollowingManager.getAllInstances()
                .get(frameId);
        if (fm != null) {
            fm.removeFrameFollowing();
        }

        // remove this id
        IndividualGuiRunner.removetoAlframeid( frameId);

        // remove this name
        IndividualGuiRunner.removefromAllName(individualName);

        // remove this thread
        ClassExchange.removeAllIndividualFrame(thisInstance);

        justClosed.add( frameId);

        calledInstances.setInterrupt( true);
    }

    protected synchronized void setWindows() {

        JScrollPane scrollPane = new JScrollPane();
        GroupLayout groupLayout = new GroupLayout(frame.getContentPane());
        groupLayout.setHorizontalGroup(groupLayout.createParallelGroup(
                Alignment.LEADING).addGroup(
                groupLayout
                        .createSequentialGroup()
                        .addContainerGap()
                        .addComponent(scrollPane, GroupLayout.DEFAULT_SIZE,
                                454, Short.MAX_VALUE).addGap(17)));
        groupLayout.setVerticalGroup(groupLayout.createParallelGroup(
                Alignment.TRAILING).addGroup(
                groupLayout
                        .createSequentialGroup()
                        .addContainerGap()
                        .addComponent(scrollPane, GroupLayout.DEFAULT_SIZE,
                                473, Short.MAX_VALUE).addGap(3)));

        JSplitPane splitPane0 = new JSplitPane();
        splitPane0.setOneTouchExpandable(true);
        splitPane0.setOrientation(JSplitPane.VERTICAL_SPLIT);
        scrollPane.setViewportView(splitPane0);
        splitPane0.setPreferredSize(frame.getSize());

        splitPane = new JSplitPane();
        splitPane.setOneTouchExpandable(true);
        splitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
        splitPane0.setLeftComponent(splitPane);

        panel = new JPanel();
        dataTable = buildTable(panel, splitPane, true, false,
                ClassExchange.dataPropertyTable);

        splitPane_1 = new JSplitPane();
        splitPane_1.setToolTipText("");
        splitPane_1.setOrientation(JSplitPane.VERTICAL_SPLIT);
        splitPane_1.setOneTouchExpandable(true);
        splitPane.setRightComponent(splitPane_1);

        panel_1 = new JPanel();
        splitPane_1.setLeftComponent(panel_1);
        objTable = buildTable(panel_1, splitPane_1, true, false,
                ClassExchange.objectPropertyTable);

        JPanel panel_2 = new JPanel();
        splitPane_1.setRightComponent(panel_2);
        panel_2.setLayout(new BorderLayout(0, 0));

        JScrollPane scrollPane_1 = new JScrollPane();
        panel_2.add(scrollPane_1, BorderLayout.CENTER);

        splitPane_2 = new JSplitPane();
        Dimension d = frame.getSize();
        d.setSize(d.getWidth() / 2, d.getHeight() / 2);
        splitPane_2.setPreferredSize(d);
        splitPane_2.setResizeWeight(.5d);
        splitPane_2.setOneTouchExpandable(true);
        scrollPane_1.setViewportView(splitPane_2);

        panel_3 = new JPanel();
        panel_3.setLayout(new BorderLayout(0, 0));

        panel_4 = new JPanel();
        panel_4.setLayout(new BorderLayout(0, 0));

        sameIndTable = buildTable(panel_4, splitPane_2, false, true,
                ClassExchange.classTable);
        classTable = buildTable(panel_3, splitPane_2, true, true,
                ClassExchange.sameIndividualTable);

        JPanel panel_5 = new JPanel();
        splitPane0.setRightComponent(panel_5);
        panel_5.setLayout(new BorderLayout(0, 0));

        JScrollPane scrollPane_2 = new JScrollPane();
        panel_5.add(scrollPane_2, BorderLayout.CENTER);

        JPanel panel_6 = new JPanel();
        // panel_6.setLayout(new BorderLayout(2, 0));
        panel_5.add(panel_6, BorderLayout.NORTH);

        JLabel label_1 = new JLabel(axiomSummaryLabel);
        panel_6.add(label_1);

        JButton btnEdit = new JButton(editBtnLabel);
        btnEdit.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                new EditorRunner(individualName,  IndividualRunner.getEditorInfo(), -1, null);
            }
        });
        panel_6.add(btnEdit);

        JPanel panel_7 = new JPanel();
        panel_7.setLayout(new BorderLayout(0, 0));
        panel_5.add(panel_7, BorderLayout.SOUTH);

        String uff = "                                   ";
        final JTextField saveName = new JTextField(uff);
        saveName.setSelectionStart(0);
        saveName.setSelectionEnd(uff.length() - 1);
        saveName.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                saveName.selectAll();
            }
        });

        // saveName.setSize( new Dimension( 15,
        // (int)saveName.getSize().getHeight()));
        JPanel panel_8 = new JPanel();
        saveName.setVisible(true);
        saveName.setScrollOffset(uff.length() - 1);
        panel_8.add(saveName, BorderLayout.SOUTH);

        saveName.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                ClassExchange.changeVisibilityProgressBar(true);
                ClassExchange.setSavingName(saveName.getText().trim());
                ClassExchange.changeVisibilityProgressBar(false);
            }
        });

        final JButton btnPrint = new JButton(saveTxt);
        panel_8.add(btnPrint);

        savecheck.setSelected(!savechekInitialState);
        panel_8.add(savecheck);

        panel_7.add(panel_8, BorderLayout.NORTH);

        savecheck.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                ClassExchange.changeVisibilityProgressBar(true);
                if (savecheck.isSelected())
                    btnPrint.setText(saveOwl);
                else
                    btnPrint.setText(saveTxt);
                // saveName.setPreferredSize( new Dimension( 150,
                // (int)saveName.getSize().getHeight()));
                ClassExchange.changeVisibilityProgressBar(false);
            }
        });

        btnPrint.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                ClassExchange.changeVisibilityProgressBar(true);
                LoadOntology.saveOntology(savecheck.isSelected());
                ClassExchange.changeVisibilityProgressBar(false);
            }
        });

        scrollPane_2.setViewportView(textarea);

        // just run to set the textField
        savecheck.setSelected(!savecheck.isSelected());
    }

    // crate a table, returning the object and add it to the list of all the
    // open tables
    private synchronized ClassTableIndividual buildTable(JPanel panel, JSplitPane splitPane,
            boolean topSplit, boolean vertical, int tableType) {

        if (topSplit)
            splitPane.setLeftComponent(panel);
        else
            splitPane.setRightComponent(panel);
        panel.setLayout(new BorderLayout(0, 0));

        JPanel panel_1 = new JPanel();
        panel.add(panel_1, BorderLayout.NORTH);
        ClassTableIndividual table = new ClassTableIndividual(individualName, tableType, textarea,
                frame, frameId);

        if (table.getTableType() == ClassExchange.dataPropertyTable) {
            ClassExchange.addtoDataTable(table);
        } else if (table.getTableType() == ClassExchange.objectPropertyTable) {
            ClassExchange.addtoObjTable(table);
        } else if (table.getTableType() == ClassExchange.classTable) {
            ClassExchange.addtoClassTable( table);
        } else if (table.getTableType() == ClassExchange.sameIndividualTable) {
            ClassExchange.addtoSameIndTable(table);
        }

        JPanel tablePane = table;
        panel.add(tablePane, BorderLayout.CENTER);

        return (table);
    }

    public synchronized static List<Integer> getJustclosed() {
        List< Integer> clone = new ArrayList< Integer>();
        for( Integer i : justClosed)
            clone.add( i);
        justClosed.clear();
        return clone;
    }
}
