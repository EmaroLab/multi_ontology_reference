package it.emarolab.amor.owlDebugger.OFGUI.allInstancesGUI;

import it.emarolab.amor.owlDebugger.Logger;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

@SuppressWarnings("serial")
public class ClassTableInstance extends JPanel implements MouseListener {
    
    private static JTable table;
    private JFrame frame;
    private JScrollPane scrollPane;
    
    private static int selected;
    
    private static MyTableModel model;

    private static final String[] columnNames = new String[] {    "Class", "Instances", "Log"};

    
    // constructor
    public ClassTableInstance( JFrame jframe) {
        super( new BorderLayout(0, 0));

        this.frame = jframe;

        model = new MyTableModel();
        table = new JTable( model);
        table.setPreferredScrollableViewportSize(new Dimension(500, 70));
                
        //Create the scroll pane and add the table to it.
        scrollPane = new JScrollPane(table);
        
        //Enable tool tips.
        ToolTipManager.sharedInstance().registerComponent( table);
        
        // set up colour manager
        table.setDefaultRenderer( String.class,  new ColorRenderer(true));
               
        //Listen for when the selection changes.
        table.addMouseListener( this);
                
        // Custom cell weight
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
             
        // frame resize listener
        frame.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                double a = e.getComponent().getSize().getWidth() - scrollPane.getVerticalScrollBar().getSize().getWidth();
                setTableDimensions(( a - 15) * 0.3);
            }
        });

        //Add the scroll pane to this panel.
        add( scrollPane, BorderLayout.CENTER);
        
    }

    // set the preferred dimensions of every cell in accord with the type of table
    // and the frame dimension
    public static void setTableDimensions( double tableWidth){
        table.getColumnModel().getColumn( 0)
            .setPreferredWidth( ( int) (0.40 * tableWidth)); // Class
        table.getColumnModel().getColumn( 1)
            .setPreferredWidth( ( int) (0.40 * tableWidth)); // instances
        table.getColumnModel().getColumn( 2)
            .setPreferredWidth( ( int) (0.20 * tableWidth)); // follow
    }

    public static MyTableModel getModel() {
        return model;
    }

    // call the ontology printer and update the textPane
    @Override
    public synchronized void mouseReleased(MouseEvent arg0) {
        // TODO Auto-generated method stub
        int selectionRow = table.getSelectedRow();
        int selectionCol = table.getSelectedColumn();
        if( selectionRow != -1 && selectionCol == 2){
            Logger tmp = Logger.getAllInstances().get( table.getValueAt( selectionRow, 1));
            Boolean boo = (Boolean) table.getValueAt( selectionRow, selectionCol);
            table.setValueAt( !boo, selectionRow, selectionCol);
            tmp.setFlagToFollow( !boo);//.setFlagToFollow( !boo, this);
            update();
            model.fireTableDataChanged();
        }
        table.repaint();
    }

    public static void saveSelection(){
        selected = table.getSelectedRow();
    }

    public static void restoreSelection(){
        if( selected < table.getRowCount() && selected >= 0)
            table.setRowSelectionInterval( selected, selected);
    }

    public JTable getTable() {
        return table;
    }

    private static String[] CN ;
    private static Object[][] D;
    public synchronized static void update(){
        CN = columnNames;
        D = Logger.getTableInfo();
    }

    // table renderer
    // the last must be "follow"
    // only the last column is editable
    public class MyTableModel extends AbstractTableModel {
        //private String[] columnNames =  CN;
        //private Object[][] data = D;
        
        public MyTableModel(){
            update( );
        }
        
        public int getColumnCount() {
            return CN.length; //columnNames.length;
        }

        public int getRowCount() {
            return D.length; //data.length;
        }

        public String getColumnName(int col) {
            return CN[ col];//columnNames[col];
        }

        public Object getValueAt(int row, int col) {
            return D[row][col];
        }

        /*
         * JTable uses this method to determine the default renderer/
         * editor for each cell.  If we didn't implement this method,
         * then the last column would contain text ("true"/"false"),
         * rather than a check box.
         */
        public Class<? extends Object> getColumnClass(int c) {
            return getValueAt(0, c).getClass();
        }

        public boolean isCellEditable(int row, int col) {
            //Note that the data/cell address is constant,
            //no matter where the cell appears onscreen.
            return true; 
        }

        public void setValueAt(Object value, int row, int col) {
            
            fireTableCellUpdated(row, col);
        }
     
    }

    // UNIMPLEMENTED METHODS
    @Override
    public void mouseClicked(MouseEvent e) {}
    @Override
    public void mouseEntered(MouseEvent e) {}
    @Override
    public void mouseExited(MouseEvent e) {}
    @Override
    public void mousePressed(MouseEvent e) {}

}