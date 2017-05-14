package it.emarolab.amor.owlDebugger.OFGUI.allInstancesGUI;

import it.emarolab.amor.owlDebugger.Logger;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("serial")
public class ClassTableLog extends JPanel {
    
    private static JTable table;
    private JFrame frame;
    private JScrollPane scrollPane;
    
    private static MyTableModel model;
    
    private static final String[] columnNames = new String[] {    "Time", "Class", "Log"};

    
    // constructor
    public ClassTableLog( JFrame jframe) {
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
        table.setDefaultRenderer( String.class, new ColorRenderer(true));
                
        // Custom cell weight
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
             
        // frame resize listener
        frame.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                double a = e.getComponent().getSize().getWidth();
                setTableDimensions( a);
            }
        });

        //Add the scroll pane to this panel.
        add( scrollPane, BorderLayout.CENTER);
        
    }

    // set the preferred dimensions of every cell in accord with the type of table
    // and the frame dimension
    public void setTableDimensions(){
        setTableDimensions( frame.getSize().getWidth());
    }
    public static void setTableDimensions( double tableWidth){
        if( table != null){
            if( table.getColumnCount() == 3){
                table.getColumnModel().getColumn( 0)
                    .setPreferredWidth( ( int) (0.15 * tableWidth)); // time
                table.getColumnModel().getColumn( 1)
                    .setPreferredWidth( ( int) (0.25 * tableWidth)); // instances
                table.getColumnModel().getColumn( 2)
                    .setPreferredWidth( ( int) (0.60 * tableWidth)); // log
            }
        }
    }


    public JTable getTable() {
        return table;
    }

    private static String[] CN ;
    private static Object[][] D;
    private static List< List <Object>> Dprimo = new ArrayList< List< Object>>();
    private int selected;
    public synchronized void update(){

        CN = columnNames;
        List<List<Object>> d = Logger.getLogInfo();

        Dprimo.addAll( d);
        if( ! Dprimo.isEmpty()){
            Object[][] Dsecondo = new Object[ Dprimo.size()][ Dprimo.get(0).size()];
            int r = 0;
            for( List< Object> lo : Dprimo){
                int c = 0;
                for( Object o : lo){
                    Dsecondo [ r][ c] = o;
                    c++;
                }
                r++;
            }
            D = Dsecondo;
        }

     }

    public void saveSelection(){
        selected = table.getSelectedRow();
    }

    public void restoreSelection(){
        if( selected < table.getRowCount() && selected >= 0)
            table.setRowSelectionInterval( selected, selected);
    }

    public static void clear(){
        CN = columnNames;
        D = new Object[ 0][ columnNames.length];
        Dprimo = new ArrayList< List< Object>>();
        model.fireTableDataChanged();
    }

    public MyTableModel getModel(){
        return model;
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
            if( CN != null)
                return CN.length; //columnNames.length;
            return 0;
        }

        public int getRowCount() {
            if( D != null)
                return D.length; //data.length;
            return 0;
        }

        public String getColumnName(int col) {
            if( CN != null)
                return CN[ col];//columnNames[col];
            return( "null");
        }

        public Object getValueAt(int row, int col) {
            if( D != null)
                return D[row][col];
            return( null);
        }

        /*
         * JTable uses this method to determine the default renderer/
         * editor for each cell.  If we didn't implement this method,
         * then the last column would contain text ("true"/"false"),
         * rather than a check box.
         */
        @SuppressWarnings({ "unchecked", "rawtypes" })
        public Class getColumnClass(int c) {
            return String.class;
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

}