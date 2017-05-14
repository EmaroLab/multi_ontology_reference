package it.emarolab.amor.owlDebugger.OFGUI.allInstancesGUI;

import it.emarolab.amor.owlDebugger.Logger;
import it.emarolab.amor.owlDebugger.OFGUI.ClassExchange;
import it.emarolab.amor.owlDebugger.OFGUI.allInstancesGUI.ClassTableInstance.MyTableModel;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.concurrent.ConcurrentHashMap;


@SuppressWarnings("serial")
public class AllinstancesRunner extends JFrame {

    public AllinstancesRunner() {
        JPanel contentPane;

        setBounds(400, 100, 900, 250);
        contentPane = new JPanel();
        contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
        contentPane.setLayout(new BorderLayout(0, 0));
        setContentPane(contentPane);

        JSplitPane splitPane = new JSplitPane();
        splitPane.setOneTouchExpandable(true);
        splitPane.setResizeWeight(0.2);
        contentPane.add(splitPane, BorderLayout.CENTER);

        JPanel table = new ClassTableInstance( this);
        splitPane.setLeftComponent( table);
        splitPane.getLeftComponent().addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                double a = e.getComponent().getSize().getWidth() - 15;
                ClassTableInstance.setTableDimensions( a);
            }
        });
        splitPane.getRightComponent().addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                double a = e.getComponent().getSize().getWidth() - 15;
                ClassTableLog.setTableDimensions( a);
            }
        });

        TextUpdater t = new TextUpdater( splitPane, this);
        t.start();

        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                TextUpdater.stopLopping();
                ConcurrentHashMap<String, Logger> allLog;
                synchronized( allLog = Logger.getAllInstances()){
                for( String id : allLog.keySet())
                    allLog.get( id).cleanDebugText();
                ClassTableLog.clear();
                }
            }
        });

    }
}


class TextUpdater extends Thread {

    private JSplitPane pane;
    private JFrame frame;

    private static boolean loopFlag;

    public TextUpdater( JSplitPane splitPane, JFrame jframe){
        pane = splitPane;
        frame = jframe;
        loopFlag = true;
    }

    @Override
    public void run(){
        long initialTime = System.currentTimeMillis();
        JPanel table = new ClassTableLog( frame);
        ClassTableLog a = (ClassTableLog) table;
        pane.setRightComponent( table);

        while( loopFlag) {
            a.saveSelection();
            a.update();
            a.getModel().fireTableDataChanged();
            a.restoreSelection();

            ClassTableInstance.saveSelection();
            ClassTableInstance.update();
            MyTableModel model = ClassTableInstance.getModel();
            if( model != null){
                model.fireTableDataChanged();
                ClassTableInstance.restoreSelection();
            }

            // runs periodically
            try {
                long done = System.currentTimeMillis() - initialTime;
                Thread.sleep( ClassExchange.getAllInstancesPeriod() - done);
                initialTime = System.currentTimeMillis();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public static void stopLopping(){
        loopFlag = false;
    }
}

