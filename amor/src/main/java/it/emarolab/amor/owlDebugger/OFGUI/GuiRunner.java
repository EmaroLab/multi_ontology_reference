package it.emarolab.amor.owlDebugger.OFGUI;

import it.emarolab.amor.owlDebugger.OFGUI.allInstancesGUI.AllinstancesRunner;
import it.emarolab.amor.owlDebugger.OFGUI.allInstancesGUI.LegendRunner;

import javax.swing.*;
import javax.swing.GroupLayout.Alignment;
import javax.swing.LayoutStyle.ComponentPlacement;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;


public class GuiRunner implements Runnable {

    public GuiRunner(){
        new ClassExchange();
    }
    public GuiRunner( String defaultOntoName){
        new ClassExchange(defaultOntoName);
    }
    public GuiRunner( String defaultOntoName, Long treePer, Long indPer, Long instancePer, Long savePer){
        new ClassExchange(defaultOntoName, treePer, indPer, instancePer, savePer);
    }

    @Override
    public void run( ) {
        long initialTime = System.currentTimeMillis();
        while( true){
            try {
                SwingUtilities.invokeLater( new Runner());

                long done = System.currentTimeMillis() - initialTime;
                Thread.sleep( ClassExchange.getTreePeriod() - done);
                initialTime = System.currentTimeMillis();

            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch ( java.lang.IllegalArgumentException e1){
                System.out.println( "Gui runner miss dead-line !!");
            }
        }
    }
}


class Runner implements Runnable {

    static boolean init = false;

    private static boolean openLegend = true;
    private static boolean openLog = true;

    private static final JProgressBar progressBar = new JProgressBar( 0, 100);

    // run the GUI
    public void run( ) {
        if( init)
            ClassExchange.changeVisibilityProgressBar(true);

        if( ! init){
            //create and show GUI
            ClassExchange.getFrameObj().setBounds( 70, 70, 160, 100);
            ClassExchange.getFrameObj().setPreferredSize( new Dimension( 340, 650));

            LoadOntology.updateOntology( init);
        }

        new ClassTree( init);
        ClassExchange.setHoldTreeObj( ClassTree.getTree());

        ClassExchange.getFrameObj().setAlwaysOnTop(false);

        if( ! init){
            ClassRootManager.setRootClass();
            ClassRootManager.updateExpandAll( init);

            ClassExchange.getFrameObj().setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);//JFrame.EXIT_ON_CLOSE);
            setWindows();
            ClassExchange.changeVisibilityProgressBar(true);
            ClassExchange.getFrameObj().setVisible(true);
            init = true;
        }
        //ClassRootManager.updateExpandAll( init);
        ClassExchange.getFrameObj().pack();
        ClassExchange.changeVisibilityProgressBar(false);

    }

    private static void setWindows() {

        buildBottomMenu();

        // build option panel
        JPanel panel = new JPanel();
        ClassExchange.getFrameObj().getContentPane().add(panel, BorderLayout.NORTH);
        ClassExchange.getFrameObj().addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                ClassExchange.getFrameObj().setPreferredSize( e.getComponent().getSize());
            }
        });

        JButton lblOntologyName = new JButton(ClassExchange.ontologyNameLabel);
        lblOntologyName.setFont(new Font("Dialog", Font.BOLD, 10));

        lblOntologyName.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                // load ontology variable and set ClassExchange
                LoadOntology.updateOntology( init);
            }
        });

        // care about update ontology from name
        ClassExchange.getOntoNameObj().addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {

                // load ontology variable and set ClassExchange
                if( e.getKeyCode() == ClassExchange.ENTER)
                    LoadOntology.updateOntology( init);
                //add hint
                ClassExchange.getOntoNameObj().setToolTipText(ClassExchange.expandeXhekBoxTip);

            }
        });
        ClassExchange.getOntoNameObj().setFont(new Font("Dialog", Font.PLAIN, 10));
        ClassExchange.getOntoNameObj().setColumns(10);

        JButton lblNewLabel = new JButton(ClassExchange.classRootLabel);
        lblNewLabel.setFont(new Font("Dialog", Font.BOLD, 10));
        lblNewLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                ClassRootManager.updateComboBox();
            }
        });


        // care about root class (combobox)
        ClassExchange.getClassRootObj().setFocusable(true);
        ClassExchange.getClassRootObj().requestFocusInWindow();
        ClassExchange.getClassRootObj().setEditable(true);
        ClassExchange.getClassRootObj().setFont(new Font("Dialog", Font.BOLD, 10));
        ArrayList<String>  allClbox = ClassRootManager.settRootWiev();
        ClassExchange.getClassRootObj().setModel(new DefaultComboBoxModel( allClbox.toArray()));

        JButton lblSearch = new JButton( ClassExchange.findLabel);
        lblSearch.setFont(new Font("Dialog", Font.BOLD, 10));
        lblSearch.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                ClassFindManager.updateComboBox();
            }
        });



        // care about find combobox
        ClassExchange.getFindItemObj().setFocusable(true);
        ClassExchange.getFindItemObj().setEditable(true);
        ArrayList<String>  allFindbox = ClassFindManager.settFindWiev();
        ClassExchange.getFindItemObj().setModel(new DefaultComboBoxModel( allFindbox.toArray()));

        ClassExchange.getFindItemObj().setFont(new Font("Dialog", Font.BOLD, 10));

        initializeOptionPanel( panel, lblSearch, lblOntologyName, lblNewLabel);

        JPanel panel_1 = new JPanel();
        ClassExchange.getFrameObj().getContentPane().add(panel_1, BorderLayout.CENTER);

        addProgressBar();


        // care about expand all check box
        ClassExchange.getExpandAllObj().addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                ClassRootManager.updateExpandAll( init);
            }
        });

        initailizeTreePanel( panel_1);

    }

    private static void buildBottomMenu( ){

        JMenuBar menuBar = new JMenuBar();
        menuBar.setFont(new Font("Dialog", Font.BOLD, 10));
        ClassExchange.getFrameObj().setJMenuBar(menuBar);

        JButton btnAllInstances = new JButton( ClassExchange.allInstancesButtonLabel);
        btnAllInstances.setFont(new Font("Dialog", Font.BOLD, 10));
        menuBar.add(btnAllInstances);

        btnAllInstances.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                if( openLog){
                    openLog = false;
                    EventQueue.invokeLater(new Runnable() {
                        public void run() {
                            try {
                                AllinstancesRunner frame = new AllinstancesRunner();
                                frame.setVisible(true);
                                frame.addWindowListener(new WindowAdapter() {
                                    @Override
                                    public void windowClosing(WindowEvent e) {
                                        openLog = true;
                                    }
                                });
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    });
                }
            }
        });

        JButton btnLegend = new JButton(ClassExchange.legendButtonLabel);
        btnLegend.setFont(new Font("Dialog", Font.BOLD, 10));
        menuBar.add(btnLegend);

        btnLegend.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                if( openLegend){
                    openLegend = false;
                    EventQueue.invokeLater(new Runnable() {
                        public void run() {
                            try {
                                LegendRunner frame = new LegendRunner();
                                frame.setVisible(true);
                                frame.addWindowListener(new WindowAdapter() {
                                    @Override
                                    public void windowClosing(WindowEvent e) {
                                        openLegend = true;
                                    }
                                });
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    });
                }
            }
        });

        JButton btnSave = new JButton( ClassExchange.serialiseFrameworkButtonLabel);
        btnSave.setFont(new Font("Dialog", Font.BOLD, 10));
        menuBar.add(btnSave);
        btnSave.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                //Thread t = new Thread( new FrameworkSerializator());
                //t.start();
            }
        });
    }

    private static void initailizeTreePanel( JPanel panel_1){
        ClassExchange.getExpandAllObj().setSelected(true);
        GroupLayout gl_panel_1 = new GroupLayout(panel_1);
        gl_panel_1.setHorizontalGroup(
            gl_panel_1.createParallelGroup(Alignment.LEADING)
                .addGroup(Alignment.TRAILING, gl_panel_1.createSequentialGroup()
                    .addGroup(gl_panel_1.createParallelGroup(Alignment.TRAILING)
                        .addGroup(gl_panel_1.createSequentialGroup()
                            .addGap(28)
                            .addComponent(ClassExchange.getIntestLabelObj())
                            .addPreferredGap(ComponentPlacement.RELATED, 165, Short.MAX_VALUE)
                            .addComponent(ClassExchange.getExpandAllObj()))
                        .addGroup(gl_panel_1.createSequentialGroup()
                            .addContainerGap()
                            .addComponent(ClassExchange.getTreePanelObj(), GroupLayout.DEFAULT_SIZE, 348, Short.MAX_VALUE)))
                    .addContainerGap())
        );
        gl_panel_1.setVerticalGroup(
            gl_panel_1.createParallelGroup(Alignment.LEADING)
                .addGroup(gl_panel_1.createSequentialGroup()
                    .addContainerGap()
                    .addGroup(gl_panel_1.createParallelGroup(Alignment.TRAILING)
                        .addComponent(ClassExchange.getIntestLabelObj())
                        .addComponent(ClassExchange.getExpandAllObj()))
                    .addPreferredGap(ComponentPlacement.RELATED)
                    .addComponent(ClassExchange.getTreePanelObj(), GroupLayout.DEFAULT_SIZE, 404, Short.MAX_VALUE)
                    .addContainerGap())
        );
        panel_1.setLayout(gl_panel_1);
    }

    private static void initializeOptionPanel( JComponent panel, JComponent lblSearch, JComponent lblOntologyName, JComponent lblNewLabel){
        GroupLayout gl_panel = new GroupLayout(panel);
        gl_panel.setHorizontalGroup(
            gl_panel.createParallelGroup(Alignment.LEADING)
                .addGroup(gl_panel.createSequentialGroup()
                    .addGap(25)
                    .addGroup(gl_panel.createParallelGroup(Alignment.LEADING)
                        .addGroup(gl_panel.createSequentialGroup()
                            .addGroup(gl_panel.createParallelGroup(Alignment.LEADING)
                                .addGroup(gl_panel.createSequentialGroup()
                                    .addComponent(lblSearch)
                                    .addPreferredGap(ComponentPlacement.RELATED))
                                .addGroup(Alignment.TRAILING, gl_panel.createSequentialGroup()
                                    .addPreferredGap(ComponentPlacement.RELATED)))
                            .addComponent(ClassExchange.getFindItemObj(), 0, 287, Short.MAX_VALUE))
                        .addGroup(gl_panel.createSequentialGroup()
                            .addComponent(lblOntologyName)
                            .addPreferredGap(ComponentPlacement.RELATED)
                            .addComponent(ClassExchange.getOntoNameObj(), GroupLayout.DEFAULT_SIZE, 226, Short.MAX_VALUE))
                        .addGroup(gl_panel.createSequentialGroup()
                            .addComponent(lblNewLabel)
                            .addGap(51)
                            .addComponent(ClassExchange.getClassRootObj(), 0, 212, Short.MAX_VALUE)))
                    .addGap(18))
        );
        gl_panel.setVerticalGroup(
            gl_panel.createParallelGroup(Alignment.TRAILING)
                .addGroup(gl_panel.createSequentialGroup()
                    .addContainerGap()
                    .addGroup(gl_panel.createParallelGroup(Alignment.LEADING)
                        .addGroup(gl_panel.createSequentialGroup()
                            .addGroup(gl_panel.createParallelGroup(Alignment.BASELINE)
                                .addComponent(lblOntologyName)
                                .addComponent(ClassExchange.getOntoNameObj(), GroupLayout.PREFERRED_SIZE, 19, GroupLayout.PREFERRED_SIZE))
                            .addPreferredGap(ComponentPlacement.RELATED)
                            .addGroup(gl_panel.createParallelGroup(Alignment.BASELINE)
                                .addComponent(lblNewLabel)
                                .addComponent(ClassExchange.getClassRootObj(), GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                            .addPreferredGap(ComponentPlacement.RELATED)
                            .addGroup(gl_panel.createParallelGroup(Alignment.BASELINE)
                                .addComponent(lblSearch)
                                .addComponent(ClassExchange.getFindItemObj(), GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)))

        )));
        panel.setLayout(gl_panel);

        //addProgressBar();
    }

    private static void addProgressBar(){

        progressBar.setIndeterminate( true);
        progressBar.setValue(0);
        progressBar.setStringPainted( false);
        progressBar.setVisible( true);
        ClassExchange.setProgressBar( progressBar);
        ClassExchange.getFrameObj().getContentPane().add(
                ClassExchange.getProgressBar(), BorderLayout.SOUTH);
    }

}
