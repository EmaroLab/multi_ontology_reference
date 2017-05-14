package it.emarolab.amor.owlDebugger.OFGUI.individualGui;

import it.emarolab.amor.owlDebugger.OFGUI.ClassExchange;
import it.emarolab.amor.owlInterface.OWLReferences;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.model.parameters.ChangeApplied;
import org.semanticweb.owlapi.util.OWLEntityRemover;

import javax.swing.*;
import javax.swing.GroupLayout.Alignment;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class EditorRunner  implements Runnable{

    private static Set<EditorRun> frames = new HashSet<EditorRun>();
    private String individualName;
    private List<List<String>> editorInfo;
    private int replacing;
    private EditorRun frame;
    private OWLAxiom replacingAxiom;

    public EditorRunner(String individualName, List<List<String>> editorInfo,
            int replacing, OWLAxiom axiom) {
        this.individualName = individualName;
        this.editorInfo = editorInfo;
        this.replacing = replacing;
        this.replacingAxiom = axiom;
        Thread t = new Thread( this);
        t.start();
    }

    public static void disposeAll() {
        for (EditorRun frame : frames) {
            frame.dispose();
        }
    }

    @Override
    public void run() {
        try {
            frame = new EditorRun( individualName, editorInfo, replacing, replacingAxiom);
            javax.swing.SwingUtilities.invokeAndWait( frame);
            frame.setCaller( this);
            frames.add( frame);
        } catch (InvocationTargetException e1) {
            e1.printStackTrace();
        } catch (InterruptedException e1) {
            e1.printStackTrace();
        }
    }

    public void dispose(){
        frame.dispose();
    }
}

@SuppressWarnings("serial")
class EditorRun extends JFrame implements Runnable{

    private static final String REPLACE = "Replace";
    private static final String CONFIRM = "Confirm";
    private final JTextField txtCc = new JTextField();
    private final JComboBox data_typeCmbox = new JComboBox();
    private final JTextField data_hasPropTxt = new JTextField();
    private final JTextField data_indTxt = new JTextField();
    private final JTextField obj_indTxt = new JTextField();
    private final JTextField obj_hasPropTxt = new JTextField();
    private final JTextField obj_valueTxt = new JTextField();
    private final JTextField individual_indTxt = new JTextField();
    private final String buttonLabel;
    private final Boolean replaceFlag;
    private String individualName;
    private EditorRunner caller = null;
    private Boolean doit_replace = true;
    private OWLReferences ontoRef;
//    private static Boolean doit_dataAdd = true;
//    private static Boolean doit_dataRem = true;
//    private static Boolean doit_objAdd = true;
//    private static Boolean doit_objRem = true;
    // set up a frame
    // int replacing: -1 no, 0 dataProp, 1 OnjProp, 2 Ind
    public EditorRun( final String individualname, final List<List<String>> selectedInfo, final int replacing, final OWLAxiom replacingAxiom) {
        this.individualName = individualname;
        setResizable(false);
        setBounds(100, 100, 524, 204);
        JPanel contentPane = new JPanel();
        contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
        setContentPane(contentPane);
        contentPane.setLayout(new BorderLayout(0, 0));

        ontoRef = ClassExchange.getOntoRef();

        if( replacing >= 0){
            replaceFlag = false; // setVisibility
            buttonLabel = CONFIRM;
        } else {
            buttonLabel = REPLACE;
            replaceFlag = true;
        }


        final JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.LEFT);
        contentPane.add(tabbedPane, BorderLayout.CENTER);

        /////////////////////////////////////////
        JPanel data_pane = new JPanel();
        tabbedPane.addTab("Data Prop", null, data_pane, null);

        final JButton replaceBnt = new JButton( buttonLabel);
        contentPane.add( replaceBnt, BorderLayout.SOUTH);
        replaceBnt.addMouseListener(new MouseAdapter() {
            // adding data type belong to an individual
            @Override
            public void mouseReleased(MouseEvent e) {
                //if( doit_replace){
                    if( buttonLabel.equals( REPLACE)){
                        OWLAxiom axiom;
                        switch( tabbedPane.getSelectedIndex()){
                        case 0 :    axiom = getDataTypeAxiom();
                            break;
                        case 1 :    axiom = getObjPropAxiom();
                            break;
                        //case 2 :
                            default:
                                axiom = null;
                        }
                        new EditorRunner(individualName,  IndividualRunner.getEditorInfo(), tabbedPane.getSelectedIndex(), axiom);
                    }
                    if( buttonLabel.equals( CONFIRM)){
                        switch( replacing){
                        case 0 :
                            synchronized( ontoRef.getOWLManager()){
                                ChangeApplied changes = ontoRef.getOWLManager().removeAxiom(ontoRef.getOWLOntology(), replacingAxiom);
                                //ontoRef.getOWLManager().applyChanges(changes);
                                changes = ontoRef.getOWLManager().addAxiom(ontoRef.getOWLOntology(), getDataTypeAxiom());
                                //ontoRef.getOWLManager().applyChanges(changes);
                            }
                            break;
                        case 1 :
                            synchronized( ontoRef.getOWLManager()){
                                ChangeApplied changes = ontoRef.getOWLManager().removeAxiom( ontoRef.getOWLOntology(), replacingAxiom);
                                //ontoRef.getOWLManager().applyChanges(changes);
                                changes = ontoRef.getOWLManager().addAxiom(ontoRef.getOWLOntology(), getObjPropAxiom());
                                //ontoRef.getOWLManager().applyChanges(changes);
                            }
                            break;
                        case 2 :

                        }

                        JOptionPane a = new JOptionPane();
                        JOptionPane.showMessageDialog(a, " Property replaced to Individual", "operation successful", JOptionPane.INFORMATION_MESSAGE);
                        caller.dispose();
                    }
                    //doit_replace = false;
                //} else doit_replace = true;
            }
        });

        String tmp;

        if( selectedInfo.get( 0).isEmpty())
            tmp = "";
        else tmp = selectedInfo.get( 0).get( 0);
        data_hasPropTxt.setText( tmp);
        data_hasPropTxt.setColumns(10);


        data_indTxt.setText( individualName);
        data_indTxt.setColumns(10);

        if( selectedInfo.get( 0).isEmpty())
            tmp = "";
        else tmp = selectedInfo.get( 0).get( 1);
        txtCc.setText( tmp);
        txtCc.setColumns(10);

        JLabel data_IndLabel = new JLabel("individual");

        JLabel data_hasPropLabel = new JLabel("hasDataProperty");

        JLabel data_valueLabel = new JLabel("Value");

        JLabel data_typeLabel = new JLabel("Type");


        data_typeCmbox.setModel(new DefaultComboBoxModel(new String[] {"integer", "string", "double", "boolean"}));

        if( selectedInfo.get( 0).isEmpty())
            tmp = "";
        else tmp = selectedInfo.get( 0).get( 2).replace( "xsd:", "");
        data_typeCmbox.setSelectedItem( tmp);

        JButton data_addBnt = new JButton("Add");
        data_addBnt.setEnabled( replaceFlag);
        data_addBnt.addMouseListener(new MouseAdapter() {
            // adding data type belong to an individual
            @Override
            public void mouseReleased(MouseEvent e) {
                //if( doit_dataAdd){
                    ChangeApplied changes = ontoRef.getOWLManager().addAxiom(ontoRef.getOWLOntology(), getDataTypeAxiom() );
                    //ontoRef.getOWLManager().applyChanges(changes);
                    //doit_dataAdd = false;
                    JOptionPane a = new JOptionPane();
                    JOptionPane.showMessageDialog(a, " data Property added to Individual", "operation successful", JOptionPane.INFORMATION_MESSAGE);
                //}else doit_dataAdd = true;
            }
        });


        JButton data_removeBnt = new JButton("Remove");
        data_removeBnt.setEnabled( replaceFlag);
        data_removeBnt.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                //if( doit_dataRem){
                    ChangeApplied changes = ontoRef.getOWLManager().removeAxiom(ontoRef.getOWLOntology(), getDataTypeAxiom());
                    //ontoRef.getOWLManager().applyChanges(changes);
                //ClassExchange.getOWLReasoner().flush();
                ///doit_dataRem = false;
                    JOptionPane a = new JOptionPane();
                    JOptionPane.showMessageDialog(a, " data Property removed from Individual", "operation successful", JOptionPane.INFORMATION_MESSAGE);
                //} else doit_dataRem = true;
            }
        });
        GroupLayout gl_data_pane = new GroupLayout(data_pane);
        gl_data_pane.setHorizontalGroup(
            gl_data_pane.createParallelGroup(Alignment.LEADING)
                .addGroup(gl_data_pane.createSequentialGroup()
                    .addGroup(gl_data_pane.createParallelGroup(Alignment.LEADING)
                        .addGroup(gl_data_pane.createSequentialGroup()
                            .addGap(36)
                            .addComponent(data_IndLabel)
                            .addPreferredGap(ComponentPlacement.RELATED, 35, Short.MAX_VALUE)
                            .addComponent(data_hasPropLabel))
                        .addGroup(Alignment.TRAILING, gl_data_pane.createSequentialGroup()
                            .addGroup(gl_data_pane.createParallelGroup(Alignment.LEADING)
                                .addGroup(gl_data_pane.createSequentialGroup()
                                    .addGap(15)
                                    .addComponent(data_addBnt, GroupLayout.PREFERRED_SIZE, 91, GroupLayout.PREFERRED_SIZE))
                                .addGroup(gl_data_pane.createSequentialGroup()
                                    .addContainerGap()
                                    .addComponent(data_indTxt, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)))
                            .addGap(18)
                            .addGroup(gl_data_pane.createParallelGroup(Alignment.TRAILING)
                                .addGroup(gl_data_pane.createSequentialGroup()
                                    .addComponent(data_removeBnt, GroupLayout.PREFERRED_SIZE, 91, GroupLayout.PREFERRED_SIZE)
                                    .addGap(26))
                                .addComponent(data_hasPropTxt, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))))
                    .addGroup(gl_data_pane.createParallelGroup(Alignment.LEADING)
                        .addGroup(gl_data_pane.createParallelGroup(Alignment.LEADING)
                            .addGroup(gl_data_pane.createSequentialGroup()
                                .addGroup(gl_data_pane.createParallelGroup(Alignment.LEADING)
                                    .addGroup(gl_data_pane.createSequentialGroup()
                                        .addGap(49)
                                        .addComponent(data_valueLabel))
                                    .addGroup(gl_data_pane.createSequentialGroup()
                                        .addPreferredGap(ComponentPlacement.RELATED)
                                        .addComponent(data_typeCmbox, 0, 120, Short.MAX_VALUE)))
                                .addGap(20))
                            .addGroup(gl_data_pane.createSequentialGroup()
                                .addGap(54)
                                .addComponent(data_typeLabel)
                                .addContainerGap()))
                        .addGroup(gl_data_pane.createSequentialGroup()
                            .addGap(18)
                            .addComponent(txtCc, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                            .addContainerGap())))
        );
        gl_data_pane.setVerticalGroup(
            gl_data_pane.createParallelGroup(Alignment.LEADING)
                .addGroup(gl_data_pane.createSequentialGroup()
                    .addContainerGap()
                    .addGroup(gl_data_pane.createParallelGroup(Alignment.BASELINE)
                        .addComponent(data_IndLabel)
                        .addComponent(data_hasPropLabel)
                        .addComponent(data_valueLabel))
                    .addGap(19)
                    .addGroup(gl_data_pane.createParallelGroup(Alignment.BASELINE)
                        .addComponent(txtCc, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                        .addComponent(data_hasPropTxt, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                        .addComponent(data_indTxt, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                    .addPreferredGap(ComponentPlacement.RELATED)
                    .addComponent(data_typeLabel)
                    .addGap(11)
                    .addGroup(gl_data_pane.createParallelGroup(Alignment.TRAILING)
                        .addComponent(data_typeCmbox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                        .addGroup(gl_data_pane.createParallelGroup(Alignment.BASELINE)
                            .addComponent(data_removeBnt)
                            .addComponent(data_addBnt)))
                    .addContainerGap(33, Short.MAX_VALUE))
        );
        data_pane.setLayout(gl_data_pane);

        /////////////////////////////////////////////////////////////
        JPanel obj_pane = new JPanel();
        tabbedPane.addTab("Object Prop", null, obj_pane, null);

        JLabel data_valueTxt = new JLabel("individual");

        JLabel obj_hasPropLabel = new JLabel("hasObjectProperty");

        JButton obj_addBnt = new JButton("Add");
        obj_addBnt.setEnabled( replaceFlag);
        obj_addBnt.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                //if( doit_objAdd){
                    ChangeApplied changes = ontoRef.getOWLManager().addAxiom(ontoRef.getOWLOntology(), getObjPropAxiom());
                    //ontoRef.getOWLManager().applyChanges(changes);
                    ///doit_objAdd = false;
                    JOptionPane a = new JOptionPane();
                    JOptionPane.showMessageDialog(a, " object Property added to Individual", "operation successful", JOptionPane.INFORMATION_MESSAGE);
                //} else doit_objAdd = true;
            }
        });


        obj_indTxt.setText( individualName);
        obj_indTxt.setColumns(10);

        JButton obj_removeBnt = new JButton("Remove");
        obj_removeBnt.setEnabled(replaceFlag);
        obj_removeBnt.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                //if( doit_objRem){
                    ChangeApplied changes = ontoRef.getOWLManager().removeAxiom( ontoRef.getOWLOntology(), getObjPropAxiom());
                    //ontoRef.getOWLManager().applyChanges(changes);

                //doit_objRem = false;
                    JOptionPane a = new JOptionPane();
                    JOptionPane.showMessageDialog(a, " data Property removed from Individual", "operation successful", JOptionPane.INFORMATION_MESSAGE);
                //} else doit_objRem = true;
            }
        });


        if( selectedInfo.get( 1).isEmpty())
            tmp = "";
        else tmp = selectedInfo.get( 1).get( 0);
        obj_hasPropTxt.setText( tmp);
        obj_hasPropTxt.setColumns(10);

        JLabel obj_valueLabel = new JLabel("Value");

        if( selectedInfo.get( 1).isEmpty())
            tmp = "";
        else tmp = selectedInfo.get( 1).get( 1);
        obj_valueTxt.setText( tmp);

        obj_valueTxt.setColumns(10);
        GroupLayout gl_obj_pane = new GroupLayout(obj_pane);
        gl_obj_pane.setHorizontalGroup(
            gl_obj_pane.createParallelGroup(Alignment.LEADING)
                .addGroup(gl_obj_pane.createSequentialGroup()
                    .addContainerGap()
                    .addGroup(gl_obj_pane.createParallelGroup(Alignment.LEADING)
                        .addGroup(gl_obj_pane.createSequentialGroup()
                            .addGroup(gl_obj_pane.createParallelGroup(Alignment.LEADING)
                                .addGroup(gl_obj_pane.createSequentialGroup()
                                    .addGap(30)
                                    .addComponent(data_valueTxt, GroupLayout.PREFERRED_SIZE, 68, GroupLayout.PREFERRED_SIZE)
                                    .addGap(29)
                                    .addComponent(obj_hasPropLabel, GroupLayout.PREFERRED_SIZE, 122, GroupLayout.PREFERRED_SIZE)
                                    .addGap(49)
                                    .addComponent(obj_valueLabel, GroupLayout.PREFERRED_SIZE, 40, GroupLayout.PREFERRED_SIZE))
                                .addGroup(gl_obj_pane.createSequentialGroup()
                                    .addComponent(obj_indTxt, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                    .addGap(21)
                                    .addComponent(obj_hasPropTxt, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                    .addGap(18)
                                    .addComponent(obj_valueTxt, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)))
                            .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addGroup(Alignment.TRAILING, gl_obj_pane.createSequentialGroup()
                            .addComponent(obj_addBnt, GroupLayout.PREFERRED_SIZE, 91, GroupLayout.PREFERRED_SIZE)
                            .addGap(32)
                            .addComponent(obj_removeBnt, GroupLayout.PREFERRED_SIZE, 91, GroupLayout.PREFERRED_SIZE)
                            .addGap(84))))
        );
        gl_obj_pane.setVerticalGroup(
            gl_obj_pane.createParallelGroup(Alignment.LEADING)
                .addGroup(gl_obj_pane.createSequentialGroup()
                    .addContainerGap()
                    .addGroup(gl_obj_pane.createParallelGroup(Alignment.LEADING)
                        .addComponent(data_valueTxt)
                        .addComponent(obj_hasPropLabel)
                        .addComponent(obj_valueLabel))
                    .addGap(19)
                    .addGroup(gl_obj_pane.createParallelGroup(Alignment.LEADING)
                        .addComponent(obj_indTxt, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                        .addComponent(obj_hasPropTxt, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                        .addComponent(obj_valueTxt, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                    .addGap(32)
                    .addGroup(gl_obj_pane.createParallelGroup(Alignment.BASELINE)
                        .addComponent(obj_removeBnt)
                        .addComponent(obj_addBnt))
                    .addContainerGap(33, Short.MAX_VALUE))
        );
        obj_pane.setLayout(gl_obj_pane);

        JPanel ind_pane = new JPanel();
        tabbedPane.addTab("Individual", null, ind_pane, null);

        individual_indTxt.setText( individualName);
        individual_indTxt.setColumns(10);

        JLabel individual_IndLabel = new JLabel("individual");

        JButton individual_addBnt = new JButton("Add");
        individual_addBnt.setEnabled( false); //replaceFlag
        individual_addBnt.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                /*String individlualName = individual_indTxt.getText();
                OWLReferences ontoRef = OWLReferences.getOWLReferences( ClassExchange.getOntoName());
                OWLNamedIndividual ind = OWLLibrary.getOWLIndividual( individualName, ontoRef);*/
                // check double firing "doit" flag
            }
        });


        JButton individual_removeBnt = new JButton("Remove");
        individual_removeBnt.setEnabled(false); //replaceFlag
        individual_removeBnt.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                removeIndividual();
                //ClassExchange.getOWLReasoner().flush();
            }
        });
        GroupLayout gl_ind_pane = new GroupLayout(ind_pane);
        gl_ind_pane.setHorizontalGroup(
            gl_ind_pane.createParallelGroup(Alignment.LEADING)
                .addGroup(gl_ind_pane.createSequentialGroup()
                    .addGap(105)
                    .addComponent(individual_addBnt, GroupLayout.PREFERRED_SIZE, 91, GroupLayout.PREFERRED_SIZE)
                    .addGap(64)
                    .addComponent(individual_removeBnt, GroupLayout.PREFERRED_SIZE, 91, GroupLayout.PREFERRED_SIZE)
                    .addGap(50))
                .addGroup(Alignment.TRAILING, gl_ind_pane.createSequentialGroup()
                    .addContainerGap(180, Short.MAX_VALUE)
                    .addComponent(individual_IndLabel)
                    .addGap(153))
                .addGroup(Alignment.TRAILING, gl_ind_pane.createSequentialGroup()
                    .addContainerGap(155, Short.MAX_VALUE)
                    .addComponent(individual_indTxt, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addGap(132))
        );
        gl_ind_pane.setVerticalGroup(
            gl_ind_pane.createParallelGroup(Alignment.LEADING)
                .addGroup(gl_ind_pane.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(individual_IndLabel)
                    .addGap(18)
                    .addComponent(individual_indTxt, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addGap(33)
                    .addGroup(gl_ind_pane.createParallelGroup(Alignment.BASELINE)
                        .addComponent(individual_removeBnt)
                        .addComponent(individual_addBnt))
                    .addContainerGap(33, Short.MAX_VALUE))
        );
        ind_pane.setLayout(gl_ind_pane);


        if( replacing >= 0)
            tabbedPane.setSelectedIndex( replacing);

    }

    public void setCaller(EditorRunner r) {
        caller = r;
    }

    @Override
    public void run() {
        // TODO Auto-generated method stub
        try {
            EditorRun frame = this;//new EditorRunner( individualName);
            frame.setVisible(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private synchronized OWLObjectPropertyAssertionAxiom getObjPropAxiom(){

        String individualname = obj_indTxt.getText();
        String propertyName = obj_hasPropTxt.getText();
        String propertyValue = obj_valueTxt.getText();

        OWLNamedIndividual individual = ontoRef.getOWLFactory().getOWLNamedIndividual( ontoRef.getPrefixFormat( individualname));
        OWLNamedIndividual value = ontoRef.getOWLFactory().getOWLNamedIndividual( ontoRef.getPrefixFormat( propertyValue));
        OWLObjectProperty hasProperty = ontoRef.getOWLFactory().getOWLObjectProperty( ontoRef.getPrefixFormat( propertyName));
        OWLObjectPropertyAssertionAxiom propertyAssertion = ontoRef.getOWLFactory().getOWLObjectPropertyAssertionAxiom( hasProperty, individual, value);

        return( propertyAssertion);
    }

    private OWLAxiom getDataTypeAxiom(){

        String propertyName = data_hasPropTxt.getText();
        OWLDataProperty isMappedBy = ontoRef.getOWLFactory().getOWLDataProperty( ontoRef.getPrefixFormat( propertyName));

        String individualname = data_indTxt.getText();
        OWLNamedIndividual individual = ontoRef.getOWLFactory().getOWLNamedIndividual( ontoRef.getPrefixFormat( individualname));

        String value = txtCc.getText();
        OWLLiteral isMappedByDataTypeValue = null;
        if( data_typeCmbox.getSelectedItem().equals( "integer"))
            isMappedByDataTypeValue =  ontoRef.getOWLFactory()
                .getOWLLiteral( Integer.valueOf( value.trim()));
        else if( data_typeCmbox.getSelectedItem().equals( "string"))
            isMappedByDataTypeValue =  ontoRef.getOWLFactory()
            .getOWLLiteral( String.valueOf( value));//.trim());
        else if( data_typeCmbox.getSelectedItem().equals( "double"))
            isMappedByDataTypeValue =  ontoRef.getOWLFactory()
            .getOWLLiteral( Double.valueOf( value.trim()));
        else if( data_typeCmbox.getSelectedItem().equals( "boolean"))
            isMappedByDataTypeValue =  ontoRef.getOWLFactory()
                .getOWLLiteral( Boolean.valueOf( value.trim()));
        else System.out.println( " Please select a type for the DataProperty");


        OWLAxiom newAxiom = ontoRef.getOWLFactory().getOWLDataPropertyAssertionAxiom(
                isMappedBy, individual, isMappedByDataTypeValue);

        return( newAxiom);

    }

    private synchronized void removeIndividual(){
        OWLNamedIndividual individual = ontoRef.getOWLFactory().getOWLNamedIndividual( ontoRef.getPrefixFormat( individualName));
        OWLEntityRemover remover = new OWLEntityRemover( Collections.singleton( ontoRef.getOWLOntology()));

        individual.accept( remover);
        ontoRef.getOWLManager().applyChanges(remover.getChanges());
        remover.reset();
    }

}
