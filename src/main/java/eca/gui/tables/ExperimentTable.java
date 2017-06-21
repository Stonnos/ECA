/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eca.gui.tables;

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.TableCellRenderer;

import eca.gui.ClassifierInputInfo;
import eca.gui.frames.ResultsFrameBase;
import eca.gui.tables.models.ExperimentTableModel;
import weka.core.Instances;
import eca.beans.ClassifierDescriptor;
import java.util.ArrayList;
import javax.swing.table.DefaultTableCellRenderer;

/**
 *
 * @author Roman93
 */
public class ExperimentTable extends JDataTableBase {

    private final JFrame parent;
    private final Instances data;
    private Cursor handCursor = new Cursor(Cursor.HAND_CURSOR);

    public ExperimentTable(ArrayList<ClassifierDescriptor> experiment,
            JFrame parent, Instances data, int digits) throws Exception {
        super(new ExperimentTableModel(experiment, digits));
        this.parent = parent;
        this.data = data;
        this.getColumnModel().getColumn(1).setCellRenderer(new ClassifierRenderer());
        this.getColumnModel().getColumn(3).setCellRenderer(new JButtonRenderer());
        this.getColumnModel().getColumn(3).setCellEditor(new JButtonEditor(new JCheckBox()));
        this.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent evt) {
                Point p = new Point(evt.getX(), evt.getY());
                int i = ExperimentTable.this.rowAtPoint(p);
                int j = ExperimentTable.this.columnAtPoint(p);
                ExperimentTable.this.changeSelection(i, j, false, false);
            }
        });
        this.getColumnModel().getColumn(0).setMaxWidth(50);
        this.setAutoResizeOff(false);
    }

    public ExperimentTableModel experimentModel() {
        return (ExperimentTableModel) this.getModel();
    }

    public void addExperiment(ClassifierDescriptor val) {
        experimentModel().add(val);
    }

    public void clear() {
        experimentModel().clear();
    }
    
    public int getBestNumber() {
        return 10;
    }

    public void setRenderer(final Color color) {
        DefaultTableCellRenderer renderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                Component cell = super.getTableCellRendererComponent(table, value, isSelected,
                        hasFocus, row, column);
                if (row < getBestNumber()) {
                    cell.setForeground(color);
                } else {
                    cell.setForeground(table.getForeground());
                }
                return cell;
            }
        };
        this.getColumnModel().getColumn(2).setCellRenderer(renderer);
    }

    public void sort() {
        experimentModel().sort();
    }

    /**
     *
     */
    private class ClassifierRenderer extends JTextField
            implements TableCellRenderer {

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            if (isSelected) {
                this.setForeground(table.getSelectionForeground());
                this.setBackground(table.getSelectionBackground());
            } else {
                this.setForeground(table.getForeground());
                this.setBackground(table.getBackground());
            }
            this.setToolTipText(ClassifierInputInfo.getInfo(experimentModel().getClassifier(row)));
            this.setText(value.toString());
            this.setBorder(null);
            this.setFont(ExperimentTable.this.getTableHeader().getFont());
            return this;
        }
    } //End of class ClassifierRenderer

    /**
     *
     */
    private class JButtonRenderer extends JButton
            implements TableCellRenderer {

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            if (isSelected) {
                this.setForeground(table.getSelectionForeground());
                this.setBackground(table.getSelectionBackground());
            } else {
                this.setForeground(table.getForeground());
                this.setBackground(table.getBackground());
            }
            this.setFont(new Font(ExperimentTable.this.getFont().getName(), Font.BOLD,
                    ExperimentTable.this.getFont().getSize()));
            this.setText(ExperimentTableModel.RESULT_TITLE);
            return this;
        }

    } // End of class JButtonRender

    private class JButtonEditor extends DefaultCellEditor {

        private JButton button;
        private boolean isPushed;
        private ResultsFrameBase result;
        private ClassifierDescriptor object;

        public JButtonEditor(JCheckBox checkBox) {
            super(checkBox);
            this.setClickCountToStart(0);
            button = new JButton();
            button.setOpaque(true);
            button.setCursor(handCursor);
            button.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    fireEditingStopped();
                }
            });
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value,
                boolean isSelected, int row, int column) {
            if (isSelected) {
                button.setForeground(table.getSelectionForeground());
                button.setBackground(table.getSelectionBackground());
            } else {
                button.setForeground(table.getForeground());
                button.setBackground(table.getBackground());
            }
            button.setText(ExperimentTableModel.RESULT_TITLE);
            button.setFont(new Font(ExperimentTable.this.getFont().getName(), Font.BOLD,
                    ExperimentTable.this.getFont().getSize()));
            isPushed = true;
            object = experimentModel().get(row);
            return button;
        }

        @Override
        public Object getCellEditorValue() {
            if (isPushed) {
                try {
                    ExperimentTableModel model = experimentModel();
                    result = new ResultsFrameBase(parent, object.getClassifier().getClass().getSimpleName(),
                            object.getClassifier(), data, object.getEvaluation(), model.digits());
                    ResultsFrameBase.createResults(result, model.digits());
                    StatisticsTableBuilder stat = new StatisticsTableBuilder(model.digits());
                    result.setStatisticaTable(stat.createStatistica(object.getClassifier(), object.getEvaluation()));
                } catch (Exception e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(ExperimentTable.this.getParent(), e.getMessage(),
                            null, JOptionPane.ERROR_MESSAGE);
                }
                result.setVisible(true);
            }
            isPushed = false;
            return ExperimentTableModel.RESULT_TITLE;
        }

        @Override
        public boolean stopCellEditing() {
            isPushed = false;
            return super.stopCellEditing();
        }

        @Override
        protected void fireEditingStopped() {
            super.fireEditingStopped();
        }
    }

}
