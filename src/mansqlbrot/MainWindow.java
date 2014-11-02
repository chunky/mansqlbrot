/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mansqlbrot;

import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Point;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.plaf.basic.BasicButtonListener;
import javax.swing.text.Caret;

/**
 *
 * @author chunky
 */
public class MainWindow extends javax.swing.JFrame {

    Connection conn;
    
    String currentRender;
    /**
     * Creates new form MainWindow
     */
    public MainWindow() {
        initComponents();
        
//        MyCaret textAreaCaret = new MyCaret();
//        textAreaCaret.setMainWindow(this);
//        displayArea.setCaret(textAreaCaret);
        displayArea.setFont(new Font("monospaced", Font.PLAIN, 12));
        
        colorInput.setText(defaultColors);
        calculateCharWidths();
        colors = defaultColors;
        
        try {
            connectDb();
            resetDimensions();
            updateDisplay();
        } catch (SQLException | ClassNotFoundException ex) {
            Logger.getLogger(MainWindow.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void updateFont() {
        displayArea.setFont(displayArea.getFont().deriveFont((float)(Integer)charSizeSpinner.getValue()));
    }
    
    private void calculateCharWidths() {
        final FontMetrics fontMetrics = displayArea.getFontMetrics(displayArea.getFont());
        oneCharWidth = (double) fontMetrics.charWidth('M');
        oneCharHeight = (double)fontMetrics.getHeight();
        resetDimensions();
    }
    
    final static private String defaultColors = " .+*#";
    String colors;
    
    Double oneCharWidth = null;
    Double oneCharHeight = null;
    
    Double minX = -2.0;
    Double maxX = 1.2;
    Double minY = -1.0;
    Double maxY = 1.0;
    Double width = 64.0;
    Double height = 20.0;
    
    public void updateDimensions() {
        Caret c = displayArea.getCaret();
        Point p = c.getMagicCaretPosition();
        
        if(null != p) {
            System.out.println(minX + " " + maxX + "," + minY + " " + maxY);
            
            Double prefWidth = (double)displayArea.getWidth();
            Double prefHeight = (double)displayArea.getHeight();
            Double newCcenterX = minX + (maxX-minX)*(p.getX()/prefWidth);
            Double newCcenterY = minY + (maxY-minY)*(p.getY()/prefHeight);

            Double halfNewCwidth = (maxX-minX)/8.0;
            Double halfNewCheight = (maxY-minY)/8.0;

            minX = newCcenterX - halfNewCwidth;
            maxX = newCcenterX + halfNewCwidth;

            minY = newCcenterY - halfNewCheight;
            maxY = newCcenterY + halfNewCheight;
            
            System.out.println(minX + " " + maxX + "," + minY + " " + maxY);
            System.out.println();
        }
        
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                updateDisplay();
            }
        });
        
    }
    
    public void resetDimensions() {
        minX = -2.0;
        maxX = 1.2;
        minY = -1.0;
        maxY = 1.0;
        
        final int prefWidth = displayArea.getWidth();
        final int prefHeight = displayArea.getHeight();
        width = (double) (prefWidth / oneCharWidth) - 1.0;
        height = (double) (prefHeight / oneCharHeight) - 4.0;
        
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                updateDisplay();
            }
        });
    }
    
    public void updateDisplay() {
        try {
            String sql = "WITH RECURSIVE\n"
                    + "  xaxis(x) AS (VALUES(?) UNION ALL SELECT x+? FROM xaxis WHERE x<?),\n"
                    + "  yaxis(y) AS (VALUES(?) UNION ALL SELECT y+? FROM yaxis WHERE y<?),\n"
                    + "  m(iter, cx, cy, x, y) AS (\n"
                    + "    SELECT 0, x, y, 0.0, 0.0 FROM xaxis, yaxis\n"
                    + "    UNION ALL\n"
                    + "    SELECT iter+1, cx, cy, x*x-y*y + cx, 2.0*x*y + cy FROM m \n"
                    + "     WHERE (x*x + y*y) < 4.0 AND iter<28\n"
                    + "  ),\n"
                    + "  m2(iter, cx, cy) AS (\n"
                    + "    SELECT max(iter), cx, cy FROM m GROUP BY cx, cy\n"
                    + "  ),\n"
                    + "  a(t) AS (\n"
                    + "    SELECT group_concat( substr(?, 1+min(iter/7,?), 1), '') \n"
                    + "    FROM m2 GROUP BY cy\n"
                    + "  )\n"
                    + "SELECT group_concat(rtrim(t),x'0a') FROM a";
         
            String colors = colorInput.getText();
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setDouble(1, minX);
                stmt.setDouble(2, (maxX - minX) / width);
                stmt.setDouble(3, maxX);
                stmt.setDouble(4, minY);
                stmt.setDouble(5, (maxY - minY) / height);
                stmt.setDouble(6, maxY);
                stmt.setString(7, colors);
                stmt.setInt(8, colors.length());

                try (ResultSet rs = stmt.executeQuery()) {
                    StringBuilder sb = new StringBuilder();
                    while (rs.next()) {
                        sb.append(rs.getString(1));
                        sb.append("\n");
                    }
                    currentRender = sb.toString();
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        displayArea.setText(currentRender);
    }

    public void connectDb() throws ClassNotFoundException, SQLException {
        Class.forName("org.sqlite.JDBC");
        conn = DriverManager.getConnection("jdbc:sqlite::memory:");
    }
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jScrollPane1 = new javax.swing.JScrollPane();
        displayArea = new javax.swing.JTextArea();
        jButton1 = new javax.swing.JButton();
        jLabel2 = new javax.swing.JLabel();
        colorInput = new javax.swing.JTextField();
        jLabel1 = new javax.swing.JLabel();
        charSizeSpinner = new javax.swing.JSpinner();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("ManSQLBrot");
        setPreferredSize(new java.awt.Dimension(1024, 678));
        getContentPane().setLayout(new java.awt.GridBagLayout());

        jScrollPane1.addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentResized(java.awt.event.ComponentEvent evt) {
                jScrollPane1ComponentResized(evt);
            }
        });

        displayArea.setColumns(20);
        displayArea.setRows(5);
        displayArea.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                displayAreaMouseClicked(evt);
            }
        });
        jScrollPane1.setViewportView(displayArea);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        getContentPane().add(jScrollPane1, gridBagConstraints);

        jButton1.setText("Reset Zoom");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.weightx = 1.0;
        getContentPane().add(jButton1, gridBagConstraints);

        jLabel2.setText("\"Colors\":");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
        getContentPane().add(jLabel2, gridBagConstraints);

        colorInput.setText("jTextField1");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.weightx = 1.0;
        getContentPane().add(colorInput, gridBagConstraints);

        jLabel1.setText("Font Size:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
        getContentPane().add(jLabel1, gridBagConstraints);

        charSizeSpinner.setModel(new javax.swing.SpinnerNumberModel(12, 1, 20, 1));
        charSizeSpinner.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                charSizeSpinnerStateChanged(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.weightx = 1.0;
        getContentPane().add(charSizeSpinner, gridBagConstraints);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jScrollPane1ComponentResized(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_jScrollPane1ComponentResized
        updateDimensions();
    }//GEN-LAST:event_jScrollPane1ComponentResized

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        resetDimensions();
    }//GEN-LAST:event_jButton1ActionPerformed

    private void displayAreaMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_displayAreaMouseClicked
        updateDimensions();
    }//GEN-LAST:event_displayAreaMouseClicked

    private void charSizeSpinnerStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_charSizeSpinnerStateChanged
        updateFont();
        calculateCharWidths();
        updateDimensions();
    }//GEN-LAST:event_charSizeSpinnerStateChanged

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(MainWindow.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(MainWindow.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(MainWindow.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(MainWindow.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new MainWindow().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JSpinner charSizeSpinner;
    private javax.swing.JTextField colorInput;
    private javax.swing.JTextArea displayArea;
    private javax.swing.JButton jButton1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JScrollPane jScrollPane1;
    // End of variables declaration//GEN-END:variables
}
