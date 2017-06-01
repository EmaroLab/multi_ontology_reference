package it.emarolab.amor.owlDebugger.OFGUI.allInstancesGUI;

import it.emarolab.amor.owlDebugger.OFGUI.ClassExchange;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.HashSet;
import java.util.Set;

@SuppressWarnings("serial")
public class Browser extends JFrame {

    private JPanel contentPane;

    /**
     * Launch the application.
     */
    public static void openBroswer() {
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                try {
                    Browser frame = new Browser();
                    ClassExchange.setBroswareFrame( frame);
                    frame.setVisible(true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * Create the frame.
     */
    public Browser() {
        setBounds(100, 100, 459, 312);
        contentPane = new JPanel();
        contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
        contentPane.setLayout(new BorderLayout(0, 0));
        setContentPane(contentPane);

        JFileChooser fileChooser = ClassExchange.getFileChooser();
        fileChooser = new MyFileChooser();
        fileChooser.setFileSelectionMode( JFileChooser.FILES_AND_DIRECTORIES );
        fileChooser.setMultiSelectionEnabled( true);
        fileChooser.setCurrentDirectory(
                fileChooser.getFileSystemView().getParentDirectory(
                    new File( System.getProperty("user.dir"))));  
        contentPane.add(fileChooser, BorderLayout.CENTER);

        this.addWindowListener(new WindowAdapter() {
            @Override
            public synchronized void windowClosing(WindowEvent e) {
                ClassExchange.getBroswareFrame().dispose();
                ClassExchange.setBroswareFrame( null);
            }
        });
        this.setTitle( "OFSystem State");
    }

}


@SuppressWarnings("serial")
class MyFileChooser extends JFileChooser {

    @Override
    public void approveSelection() {
        if (getSelectedFile().isFile()) {
            ClassExchange.getLoadState_btn().setEnabled( true);
            ClassExchange.getSaveState_btn().setEnabled( false);

            File[] files = this.getSelectedFiles();
            String basePath = null;
            Set< String> paths = new HashSet<String>();
            for( int i = 0; i < files.length; i++){
                paths.add( files[i].getAbsolutePath());
                basePath = files[i].getPath();
            }
            ClassExchange.getBroswarePathtextField().setText( basePath);
            ClassExchange.setChosenLoadingPaths( paths);
            closeDialog();
        } else {
            if( this.getSelectedFiles().length == 1){
                ClassExchange.getLoadState_btn().setEnabled( false);
                ClassExchange.getSaveState_btn().setEnabled( true);

                String path = this.getSelectedFile().getAbsolutePath();
                ClassExchange.getBroswarePathtextField( ).setText( path);
                closeDialog();
            }
        }
    }

    @Override
    public void cancelSelection() {
        closeDialog();
    }
    
    private void closeDialog(){
        ClassExchange.getBroswareFrame().dispose();
        ClassExchange.setBroswareFrame( null);
    }
    
}