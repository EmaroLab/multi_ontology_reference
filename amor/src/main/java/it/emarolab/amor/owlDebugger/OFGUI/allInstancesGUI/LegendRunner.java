package it.emarolab.amor.owlDebugger.OFGUI.allInstancesGUI;

import it.emarolab.amor.owlDebugger.OFGUI.ClassExchange;
import it.emarolab.amor.owlDebugger.OFGUI.ClassTree;
import it.emarolab.amor.owlDebugger.OFGUI.individualGui.ClassTableIndividual;

import javax.swing.*;
import javax.swing.GroupLayout.Alignment;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.util.List;
import java.util.Map;


public class LegendRunner extends JFrame implements ActionListener {

    private static final long serialVersionUID = 1L;
    private JPanel contentPane;
    private JTextField textField;
    private JColorChooser colorChooser;
    private JDialog dialog;
    private JButton btnPIU;
    private JPanel colorPane;
    private JLabel lblColors;
    private JCheckBox checkBox;
    private static final int colorCursorNumber = 12;
    protected static final String EDIT = "edit";
    private static final String labelsColorsTitle = "COLORS";

    private JSpinner treeFreq = new JSpinner();
    private JSpinner logFrq = new JSpinner();
    private JSpinner saveFrq = new JSpinner();
    private JSpinner indiFreq = new JSpinner();

    public LegendRunner() {
        setResizable(false);
        setTitle("Legend & Colors");
        setBounds(500, 120, 431, 641);//setBounds(500, 120, 408, 441);
        contentPane = new JPanel();
        contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
        setContentPane(contentPane);

        JPanel panel = new JPanel();

        colorPane = new JPanel();
        colorPane.setLayout(new BoxLayout(colorPane, BoxLayout.Y_AXIS));
        JScrollPane scrollPane = new JScrollPane( colorPane);

        JLabel lblSymbols = new JLabel("SYMBOLS");

        btnPIU = new JButton("");
        btnPIU.setActionCommand(EDIT);
        btnPIU.setIcon( ClassExchange.imAddColorIcon);
        btnPIU.addActionListener(this);
        btnPIU.setBorderPainted(false);
        //Set up the dialog that the button brings up.
        colorChooser = new JColorChooser();
        dialog = JColorChooser.createDialog( btnPIU,
                "Pick a Color",
                true,  //modal
                colorChooser,
                this,  //OK button handler
                null); //no CANCEL button handler

        JButton btnMENO = new JButton();
        btnMENO.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                String txt = textField.getText();
                // check if the string is empty
                if( ( txt == null) || ( txt.trim().isEmpty()))
                    showError( " impossible to remuve an empty String");
                else{
                    Map<String, Color> map = ClassExchange.getAllColorToFollow();
                    // check if it is choosen
                    if( ! map.containsKey( txt))
                        showError( " impossible to remove if is not already chosen");
                    else{
                        map.remove( txt);
                        ClassExchange.setAllColorToFollow(map);
                        showAllItem();
                        fireTableChange();
                    }
                }
            }
        });
        btnMENO.setIcon( ClassExchange.imDeleteColorIcon);

        textField = new JTextField();
        textField.setColumns(10);

        lblColors = new JLabel( labelsColorsTitle + " (match)   ");

        JSeparator separator = new JSeparator();
        separator.setOrientation(SwingConstants.VERTICAL);

        checkBox = new JCheckBox("");
        checkBox.setSelected( ClassExchange.isColorMatchSearch());
        checkBox.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                if ( checkBox.isSelected())
                    lblColors.setText( labelsColorsTitle + " (match)   ");
                else
                    lblColors.setText( labelsColorsTitle + " (contains)");
                ClassExchange.setColorMatchSearch( checkBox.isSelected());
            }
        });

        GroupLayout gl_contentPane = new GroupLayout(contentPane);
        gl_contentPane.setHorizontalGroup(
                gl_contentPane.createParallelGroup(Alignment.LEADING)
                .addGroup(gl_contentPane.createSequentialGroup()
                        .addGroup(gl_contentPane.createParallelGroup(Alignment.LEADING)
                                .addGroup(gl_contentPane.createSequentialGroup()
                                        .addGap(65)
                                        .addComponent(lblSymbols))
                                        .addGroup(gl_contentPane.createSequentialGroup()
                                                .addContainerGap()
                                                .addComponent(panel, GroupLayout.PREFERRED_SIZE, 189, GroupLayout.PREFERRED_SIZE)))
                                                .addPreferredGap(ComponentPlacement.UNRELATED)
                                                .addComponent(separator, GroupLayout.PREFERRED_SIZE, 5, GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(ComponentPlacement.RELATED)
                                                .addGroup(gl_contentPane.createParallelGroup(Alignment.LEADING)
                                                        .addGroup(gl_contentPane.createSequentialGroup()
                                                                .addGroup(gl_contentPane.createParallelGroup(Alignment.LEADING)
                                                                        .addGroup(gl_contentPane.createSequentialGroup()
                                                                                .addGap(12)
                                                                                .addComponent(btnPIU)
                                                                                .addPreferredGap(ComponentPlacement.RELATED, 34, Short.MAX_VALUE)
                                                                                .addComponent(btnMENO, GroupLayout.PREFERRED_SIZE, 44, GroupLayout.PREFERRED_SIZE)
                                                                                .addPreferredGap(ComponentPlacement.UNRELATED)
                                                                                .addComponent(checkBox))
                                                                                .addGroup(gl_contentPane.createParallelGroup(Alignment.TRAILING, false)
                                                                                        .addComponent(lblColors, Alignment.LEADING, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                                                        .addComponent(textField, Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 145, Short.MAX_VALUE)))
                                                                                        .addPreferredGap(ComponentPlacement.RELATED))
                                                                                        .addComponent(scrollPane, GroupLayout.DEFAULT_SIZE, 153, Short.MAX_VALUE))
                                                                                        .addGap(19))
                );
        gl_contentPane.setVerticalGroup(
                gl_contentPane.createParallelGroup(Alignment.LEADING)
                .addGroup(gl_contentPane.createSequentialGroup()
                        .addGroup(gl_contentPane.createParallelGroup(Alignment.LEADING)
                                .addGroup(gl_contentPane.createSequentialGroup()
                                        .addContainerGap()
                                        .addComponent(separator, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                                        .addGroup(gl_contentPane.createSequentialGroup()
                                                .addComponent(lblSymbols)
                                                .addPreferredGap(ComponentPlacement.RELATED)
                                                .addComponent(panel, GroupLayout.PREFERRED_SIZE, 369, GroupLayout.PREFERRED_SIZE))
                                                .addGroup(gl_contentPane.createSequentialGroup()
                                                        .addComponent(lblColors)
                                                        .addPreferredGap(ComponentPlacement.RELATED)
                                                        .addComponent(textField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                                        .addPreferredGap(ComponentPlacement.RELATED)
                                                        .addGroup(gl_contentPane.createParallelGroup(Alignment.LEADING)
                                                                .addGroup(gl_contentPane.createParallelGroup(Alignment.BASELINE)
                                                                        .addComponent(btnPIU)
                                                                        .addComponent(btnMENO))
                                                                        .addComponent(checkBox))
                                                                        .addPreferredGap(ComponentPlacement.UNRELATED)
                                                                        .addComponent(scrollPane, GroupLayout.PREFERRED_SIZE, 307, GroupLayout.PREFERRED_SIZE)))
                                                                        .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                );

        JPanel frequanyPanel = new JPanel();

        JSeparator separator_3 = new JSeparator();
        GroupLayout gl_contentPane1 = new GroupLayout(contentPane);
        gl_contentPane1.setHorizontalGroup(
                gl_contentPane1.createParallelGroup(Alignment.LEADING)
                .addGroup(gl_contentPane1.createSequentialGroup()
                        .addGroup(gl_contentPane1.createParallelGroup(Alignment.LEADING)
                                .addGroup(gl_contentPane1.createSequentialGroup()
                                        .addGap(65)
                                        .addComponent(lblSymbols))
                                        .addGroup(gl_contentPane1.createSequentialGroup()
                                                .addContainerGap()
                                                .addComponent(panel, GroupLayout.PREFERRED_SIZE, 189, GroupLayout.PREFERRED_SIZE)))
                                                .addPreferredGap(ComponentPlacement.UNRELATED)
                                                .addComponent(separator, GroupLayout.PREFERRED_SIZE, 5, GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(ComponentPlacement.RELATED)
                                                .addGroup(gl_contentPane1.createParallelGroup(Alignment.LEADING, false)
                                                        .addComponent(scrollPane, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                                        .addGroup(gl_contentPane1.createSequentialGroup()
                                                                .addGap(12)
                                                                .addComponent(btnPIU)
                                                                .addGap(38)
                                                                .addComponent(btnMENO, GroupLayout.PREFERRED_SIZE, 44, GroupLayout.PREFERRED_SIZE))
                                                                .addGroup(gl_contentPane1.createSequentialGroup()
                                                                        .addGroup(gl_contentPane1.createParallelGroup(Alignment.TRAILING, false)
                                                                                .addComponent(lblColors, Alignment.LEADING, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                                                .addComponent(textField, Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 145, Short.MAX_VALUE))
                                                                                .addPreferredGap(ComponentPlacement.UNRELATED)
                                                                                .addComponent(checkBox)))
                                                                                .addContainerGap(9, Short.MAX_VALUE))
                                                                                .addGroup(gl_contentPane1.createSequentialGroup()
                                                                                        .addContainerGap()
                                                                                        .addComponent(separator_3, GroupLayout.PREFERRED_SIZE, 760, Short.MAX_VALUE)
                                                                                        .addGap(10))
                                                                                        .addGroup(gl_contentPane1.createSequentialGroup()
                                                                                                .addContainerGap()
                                                                                                .addComponent(frequanyPanel, GroupLayout.PREFERRED_SIZE, 376, GroupLayout.PREFERRED_SIZE)
                                                                                                .addContainerGap(31, Short.MAX_VALUE))
                );
        gl_contentPane1.setVerticalGroup(
                gl_contentPane1.createParallelGroup(Alignment.LEADING)
                .addGroup(gl_contentPane1.createSequentialGroup()
                        .addGroup(gl_contentPane1.createParallelGroup(Alignment.LEADING)
                                .addGroup(gl_contentPane1.createSequentialGroup()
                                        .addContainerGap()
                                        .addComponent(separator, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                                        .addGroup(gl_contentPane1.createSequentialGroup()
                                                .addComponent(lblSymbols)
                                                .addPreferredGap(ComponentPlacement.RELATED)
                                                .addComponent(panel, GroupLayout.PREFERRED_SIZE, 369, GroupLayout.PREFERRED_SIZE))
                                                .addGroup(gl_contentPane1.createSequentialGroup()
                                                        .addComponent(lblColors)
                                                        .addPreferredGap(ComponentPlacement.RELATED)
                                                        .addGroup(gl_contentPane1.createParallelGroup(Alignment.LEADING)
                                                                .addGroup(gl_contentPane1.createSequentialGroup()
                                                                        .addComponent(textField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                                                        .addPreferredGap(ComponentPlacement.RELATED)
                                                                        .addGroup(gl_contentPane1.createParallelGroup(Alignment.LEADING)
                                                                                .addComponent(btnPIU)
                                                                                .addComponent(btnMENO)))
                                                                                .addComponent(checkBox))
                                                                                .addGap(19)
                                                                                .addComponent(scrollPane, GroupLayout.PREFERRED_SIZE, 307, GroupLayout.PREFERRED_SIZE)))
                                                                                .addPreferredGap(ComponentPlacement.RELATED)
                                                                                .addComponent(separator_3, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                                                                .addPreferredGap(ComponentPlacement.UNRELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                                                .addComponent(frequanyPanel, GroupLayout.PREFERRED_SIZE, 192, GroupLayout.PREFERRED_SIZE)
                                                                                .addGap(53))
                );
        frequanyPanel.setLayout(new BorderLayout(0, 0));

        JLabel lblNewLabel_1 = new JLabel("                UPDATING GUI FREQUENCIES [ms]");
        frequanyPanel.add(lblNewLabel_1, BorderLayout.NORTH);

        JPanel panel_1 = new JPanel();
        frequanyPanel.add(panel_1, BorderLayout.SOUTH);

        JButton btnSetFrequencies = new JButton("set Frequencies");
        btnSetFrequencies.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                ClassExchange.setTreePeriod( (Long) treeFreq.getValue());
                ClassExchange.setAllInstancesPeriod( (Long)  logFrq.getValue());
                ClassExchange.setSAVINGPERIOD( (Long) saveFrq.getValue());
                ClassExchange.setIndividualFramPeriod( (Long) indiFreq.getValue());
            }
        });

        JButton btnReset = new JButton("reset");
        btnReset.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                treeFreq.setValue( ClassExchange.getTreePeriod());
                logFrq.setValue( ClassExchange.getAllInstancesPeriod());
                saveFrq.setValue( ClassExchange.getSAVINGPERIOD( ));
                indiFreq.setValue( ClassExchange.getIndividualFramPeriod());
            }
        });
        GroupLayout gl_panel_1 = new GroupLayout(panel_1);
        gl_panel_1.setHorizontalGroup(
                gl_panel_1.createParallelGroup(Alignment.LEADING)
                .addGroup(gl_panel_1.createSequentialGroup()
                        .addGap(33)
                        .addComponent(btnSetFrequencies, GroupLayout.PREFERRED_SIZE, 158, GroupLayout.PREFERRED_SIZE)
                        .addGap(37)
                        .addComponent(btnReset)
                        .addContainerGap(76, Short.MAX_VALUE))
                );
        gl_panel_1.setVerticalGroup(
                gl_panel_1.createParallelGroup(Alignment.LEADING)
                .addGroup(gl_panel_1.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(gl_panel_1.createParallelGroup(Alignment.BASELINE)
                                .addComponent(btnSetFrequencies)
                                .addComponent(btnReset))
                                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                );
        panel_1.setLayout(gl_panel_1);

        JPanel panel_2 = new JPanel();
        frequanyPanel.add(panel_2, BorderLayout.CENTER);

        JLabel lblNewLabel_2 = new JLabel("Tree frequency");

        JLabel lblIndividualTablesFrequency = new JLabel("Individual tables frequency");

        JLabel lblLogTablesFrequency = new JLabel("Log tables frequency");

        JLabel lblSerializableListsFrequency = new JLabel("Serializable lists frequency");

        treeFreq.setModel(new SpinnerNumberModel(new Long( ClassExchange.getTreePeriod()), new Long(10), null, new Long(20)));
        logFrq.setModel(new SpinnerNumberModel(new Long( ClassExchange.getAllInstancesPeriod()), new Long(10), null, new Long(20)));
        saveFrq.setModel(new SpinnerNumberModel(new Long( ClassExchange.getSAVINGPERIOD()), new Long(10), null, new Long(20)));
        indiFreq.setModel(new SpinnerNumberModel(new Long( ClassExchange.getIndividualFramPeriod()), new Long(10), null, new Long(20)));

        GroupLayout gl_panel_2 = new GroupLayout(panel_2);
        gl_panel_2.setHorizontalGroup(
                gl_panel_2.createParallelGroup(Alignment.LEADING)
                .addGroup(gl_panel_2.createSequentialGroup()
                        .addGap(22)
                        .addGroup(gl_panel_2.createParallelGroup(Alignment.LEADING)
                                .addGroup(gl_panel_2.createSequentialGroup()
                                        .addComponent(lblSerializableListsFrequency, GroupLayout.PREFERRED_SIZE, 162, GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(ComponentPlacement.UNRELATED)
                                        .addComponent(saveFrq, GroupLayout.PREFERRED_SIZE, 116, GroupLayout.PREFERRED_SIZE))
                                        .addGroup(gl_panel_2.createSequentialGroup()
                                                .addComponent(lblLogTablesFrequency, GroupLayout.PREFERRED_SIZE, 162, GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(ComponentPlacement.UNRELATED)
                                                .addComponent(logFrq, GroupLayout.PREFERRED_SIZE, 116, GroupLayout.PREFERRED_SIZE))
                                                .addGroup(gl_panel_2.createParallelGroup(Alignment.LEADING, false)
                                                        .addGroup(gl_panel_2.createSequentialGroup()
                                                                .addComponent(lblNewLabel_2)
                                                                .addPreferredGap(ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                                .addComponent(treeFreq, GroupLayout.PREFERRED_SIZE, 116, GroupLayout.PREFERRED_SIZE))
                                                                .addGroup(gl_panel_2.createSequentialGroup()
                                                                        .addComponent(lblIndividualTablesFrequency, GroupLayout.PREFERRED_SIZE, 162, GroupLayout.PREFERRED_SIZE)
                                                                        .addPreferredGap(ComponentPlacement.UNRELATED)
                                                                        .addComponent(indiFreq, GroupLayout.PREFERRED_SIZE, 116, GroupLayout.PREFERRED_SIZE))))
                                                                        .addContainerGap(50, Short.MAX_VALUE))
                );
        gl_panel_2.setVerticalGroup(
                gl_panel_2.createParallelGroup(Alignment.LEADING)
                .addGroup(gl_panel_2.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(gl_panel_2.createParallelGroup(Alignment.BASELINE)
                                .addComponent(lblNewLabel_2)
                                .addComponent(treeFreq, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(ComponentPlacement.RELATED)
                                .addGroup(gl_panel_2.createParallelGroup(Alignment.TRAILING)
                                        .addComponent(lblIndividualTablesFrequency)
                                        .addComponent(indiFreq, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                                        .addPreferredGap(ComponentPlacement.RELATED)
                                        .addGroup(gl_panel_2.createParallelGroup(Alignment.TRAILING)
                                                .addComponent(lblLogTablesFrequency)
                                                .addComponent(logFrq, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                                                .addPreferredGap(ComponentPlacement.RELATED)
                                                .addGroup(gl_panel_2.createParallelGroup(Alignment.TRAILING)
                                                        .addComponent(lblSerializableListsFrequency)
                                                        .addComponent(saveFrq, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                                                        .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                );
        panel_2.setLayout(gl_panel_2);


        JLabel lblDefinedEntities = new JLabel("Defined Entities:");

        JSeparator separator_1 = new JSeparator();

        JLabel lblAssertedEntities = new JLabel("Asserted Entities:");

        JLabel lblClass = new JLabel(" Class");
        lblClass.setIcon( ClassExchange.imClassIcon);

        JLabel lblDataProperty = new JLabel(" Data Property");
        lblDataProperty.setIcon( ClassExchange.imDataPropIcon);

        JLabel lblObjectProperty = new JLabel(" Object Property");
        lblObjectProperty.setIcon( ClassExchange.imObjPropIcon);

        JLabel lblIndividual = new JLabel(" Individual");
        lblIndividual.setIcon( ClassExchange.imIndividualIcon);

        JLabel label = new JLabel(" Class");
        label.setIcon( ClassExchange.imClassInfIcon);

        JLabel label_1 = new JLabel(" Data Property");
        label_1.setIcon( ClassExchange.imDataPropInfIcon);

        JLabel label_2 = new JLabel(" Object Property");
        label_2.setIcon( ClassExchange.imObjPropInfIcon);

        JLabel label_3 = new JLabel(" Individual");
        label_3.setIcon( ClassExchange.imIndividualInfIcon);

        JSeparator separator_2 = new JSeparator();

        JLabel lblVisualAggregation = new JLabel("Visual Aggregation");

        JLabel label_4 = new JLabel(" asserted sub-Classes");
        label_4.setIcon( ClassExchange.imClassPredIcon);

        JLabel lblAssertedIndividuals = new JLabel(" asserted individuals");
        lblAssertedIndividuals.setIcon( ClassExchange.imIndividualPredIcon);

        JLabel lblBelongToClass = new JLabel("-belong to class");

        GroupLayout gl_panel = new GroupLayout(panel);
        gl_panel.setHorizontalGroup(
                gl_panel.createParallelGroup(Alignment.TRAILING)
                .addComponent(separator_1, GroupLayout.DEFAULT_SIZE, 201, Short.MAX_VALUE)
                .addGroup(gl_panel.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(gl_panel.createParallelGroup(Alignment.LEADING)
                                .addGroup(gl_panel.createSequentialGroup()
                                        .addGap(12)
                                        .addGroup(gl_panel.createParallelGroup(Alignment.LEADING)
                                                .addComponent(lblObjectProperty, GroupLayout.DEFAULT_SIZE, 165, Short.MAX_VALUE)
                                                .addComponent(lblClass, GroupLayout.DEFAULT_SIZE, 165, Short.MAX_VALUE)
                                                .addComponent(lblDataProperty, GroupLayout.DEFAULT_SIZE, 165, Short.MAX_VALUE)
                                                .addComponent(lblIndividual, GroupLayout.DEFAULT_SIZE, 165, Short.MAX_VALUE)))
                                                .addComponent(lblDefinedEntities))
                                                .addContainerGap())
                                                .addGroup(gl_panel.createSequentialGroup()
                                                        .addContainerGap()
                                                        .addGroup(gl_panel.createParallelGroup(Alignment.LEADING)
                                                                .addComponent(lblAssertedEntities, GroupLayout.DEFAULT_SIZE, 177, Short.MAX_VALUE)
                                                                .addGroup(gl_panel.createSequentialGroup()
                                                                        .addGap(12)
                                                                        .addGroup(gl_panel.createParallelGroup(Alignment.LEADING)
                                                                                .addComponent(label, GroupLayout.DEFAULT_SIZE, 165, Short.MAX_VALUE)
                                                                                .addComponent(label_1, GroupLayout.DEFAULT_SIZE, 165, Short.MAX_VALUE)
                                                                                .addComponent(label_2, GroupLayout.DEFAULT_SIZE, 165, Short.MAX_VALUE)
                                                                                .addComponent(label_3, GroupLayout.DEFAULT_SIZE, 165, Short.MAX_VALUE))))
                                                                                .addContainerGap())
                                                                                .addGroup(gl_panel.createSequentialGroup()
                                                                                        .addComponent(separator_2, GroupLayout.PREFERRED_SIZE, 189, GroupLayout.PREFERRED_SIZE)
                                                                                        .addContainerGap())
                                                                                        .addGroup(gl_panel.createSequentialGroup()
                                                                                                .addContainerGap()
                                                                                                .addComponent(lblVisualAggregation, GroupLayout.PREFERRED_SIZE, 177, GroupLayout.PREFERRED_SIZE)
                                                                                                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                                                                                .addGroup(gl_panel.createSequentialGroup()
                                                                                                        .addContainerGap(24, Short.MAX_VALUE)
                                                                                                        .addComponent(label_4, GroupLayout.PREFERRED_SIZE, 165, GroupLayout.PREFERRED_SIZE)
                                                                                                        .addContainerGap())
                                                                                                        .addGroup(gl_panel.createSequentialGroup()
                                                                                                                .addContainerGap(24, Short.MAX_VALUE)
                                                                                                                .addComponent(lblBelongToClass, GroupLayout.PREFERRED_SIZE, 165, GroupLayout.PREFERRED_SIZE)
                                                                                                                .addContainerGap())
                                                                                                                .addGroup(gl_panel.createSequentialGroup()
                                                                                                                        .addContainerGap()
                                                                                                                        .addComponent(lblAssertedIndividuals, GroupLayout.PREFERRED_SIZE, 177, GroupLayout.PREFERRED_SIZE))
                );
        gl_panel.setVerticalGroup(
                gl_panel.createParallelGroup(Alignment.LEADING)
                .addGroup(gl_panel.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(lblDefinedEntities)
                        .addPreferredGap(ComponentPlacement.RELATED)
                        .addComponent(lblClass, GroupLayout.PREFERRED_SIZE, 15, GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(ComponentPlacement.RELATED)
                        .addComponent(lblDataProperty, GroupLayout.PREFERRED_SIZE, 15, GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(ComponentPlacement.RELATED)
                        .addComponent(lblObjectProperty, GroupLayout.PREFERRED_SIZE, 15, GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(ComponentPlacement.RELATED)
                        .addComponent(lblIndividual)
                        .addGap(24)
                        .addComponent(separator_1, GroupLayout.PREFERRED_SIZE, 11, GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(ComponentPlacement.RELATED)
                        .addComponent(lblAssertedEntities)
                        .addPreferredGap(ComponentPlacement.RELATED)
                        .addComponent(label, GroupLayout.PREFERRED_SIZE, 15, GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(ComponentPlacement.RELATED)
                        .addComponent(label_1, GroupLayout.PREFERRED_SIZE, 15, GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(ComponentPlacement.RELATED)
                        .addComponent(label_2, GroupLayout.PREFERRED_SIZE, 15, GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(ComponentPlacement.RELATED)
                        .addComponent(label_3)
                        .addGap(18)
                        .addComponent(separator_2, GroupLayout.PREFERRED_SIZE, 11, GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(ComponentPlacement.RELATED)
                        .addComponent(lblVisualAggregation)
                        .addPreferredGap(ComponentPlacement.RELATED)
                        .addComponent(label_4)
                        .addPreferredGap(ComponentPlacement.RELATED)
                        .addComponent(lblAssertedIndividuals)
                        .addPreferredGap(ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(lblBelongToClass)
                        .addContainerGap())
                );
        panel.setLayout(gl_panel);
        contentPane.setLayout(gl_contentPane1);
    }

    private void showAllItem(){
        colorPane.removeAll();
        for( String key : ClassExchange.getAllColorToFollow().keySet())
            colorPane.add( showNewItem( key, ClassExchange.getAllColorToFollow().get(key)));
        validate();
        repaint();
    }

    private JPanel showNewItem( String toFollow, Color color){

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));

        if( toFollow.length() > colorCursorNumber ){
            toFollow = toFollow.substring( 0, colorCursorNumber - 3);
            toFollow += "... ";
        } else {
            for( int i = toFollow.length() - 1; i < colorCursorNumber; i++)
                toFollow += " ";
        }
        toFollow = "  " + toFollow;
        JLabel lblNewLabel = new JLabel( toFollow);
        lblNewLabel.setToolTipText("RGB value: " + color.getRed() + ", "
                + color.getGreen() + ", " + color.getBlue());
        lblNewLabel.setFont(new Font("Monospaced", Font.ITALIC, 12));
        panel.add(lblNewLabel);

        final JButton btnNewButton = new JButton(" ");
        btnNewButton.setEnabled( true);
        btnNewButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                Map<String, Color> map = ClassExchange.getAllColorToFollow();
                for( String s : map.keySet())
                    if( map.get( s).equals( btnNewButton.getBackground()))
                        textField.setText( s);

            }
        });
        btnNewButton.setBackground( color);
        btnNewButton.setToolTipText("RGB value: " + color.getRed() + ", "
                + color.getGreen() + ", " + color.getBlue());
        panel.add(btnNewButton);

        return( panel);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String txt = textField.getText();
        Map<String, Color> map = ClassExchange.getAllColorToFollow();
        if (EDIT.equals(e.getActionCommand())) {
            // check if the text field is empty
            if( ( txt == null) || ( txt.trim().isEmpty()))
                showError(" empty text field. Impossible to follow an empty name");
            else{
                // check if the name has already been chosen
                if( map.containsKey( txt))
                    showError( " name already chosen");
                else{
                    dialog.setVisible(true);
                }
            }
        } else {
            //User pressed dialog's "OK" button.
            Color currentColor = colorChooser.getColor();

            // check color already used
            if( map.containsValue( currentColor))
                showError( " the color is already used");
            else{
                map.put(txt, currentColor);
                ClassExchange.setAllColorToFollow( map);
                showAllItem();
                fireTableChange();
            }
        }
    }

    private void fireTableChange(){
        List<ClassTableIndividual> data = ClassExchange.getAllDataTable();
        for( ClassTableIndividual ct : data)
            ct.getModel().fireTableDataChanged();
        data = ClassExchange.getAllObjTable();
        for( ClassTableIndividual ct : data)
            ct.getModel().fireTableDataChanged();
        data = ClassExchange.getAllSameIndTable();
        for( ClassTableIndividual ct : data)
            ct.getModel().fireTableDataChanged();
        data = ClassExchange.getAllClassTable();
        for( ClassTableIndividual ct : data)
            ct.getModel().fireTableDataChanged();
        new ClassTree( true);
    }

    private void showError( String err){
        JOptionPane a = new JOptionPane();
        JOptionPane.showMessageDialog(a, err, "Bad input", JOptionPane.ERROR_MESSAGE);
    }
}
