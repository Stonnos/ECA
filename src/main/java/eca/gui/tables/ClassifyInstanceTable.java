/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eca.gui.tables;

import eca.gui.enums.AttributesTypes;
import java.awt.Component;
import javax.swing.JTable;

import eca.gui.tables.models.ClassifyInstanceTableModel;
import eca.statistics.AttributeStatistics;
import weka.core.Instances;
import weka.core.DenseInstance;
import weka.core.Attribute;
import weka.core.Instance;
import java.util.Enumeration;
import javax.swing.JTextField;
import javax.swing.table.TableCellRenderer;
import javax.swing.DefaultCellEditor;
import javax.swing.table.TableColumn;
import eca.gui.text.DoubleDocument;

import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.text.DecimalFormat;
import eca.gui.text.NumericFormat;

/**
 *
 * @author Рома
 */
public class ClassifyInstanceTable extends JDataTableBase {

    private static final int DOUBLE_FIELD_LENGTH = 9;
    private static final int ROW_HEIGHT = 18;
    private final DecimalFormat decimalFormat = NumericFormat.getInstance();
    private AttributeStatistics attributeStatistics;

    public ClassifyInstanceTable(Instances data, int digits) {
        super(new ClassifyInstanceTableModel(data));
        decimalFormat.setMaximumFractionDigits(digits);
        attributeStatistics = new AttributeStatistics(data, decimalFormat);
        TableColumn column = this.getColumnModel().getColumn(ClassifyInstanceTableModel.TEXT_INDEX);
        this.getColumnModel().getColumn(0).setPreferredWidth(50);
        this.getColumnModel().getColumn(0).setMaxWidth(50);
        this.getColumnModel().getColumn(0).setMinWidth(50);
        JTextField text = new JTextField(DOUBLE_FIELD_LENGTH);
        text.setDocument(new DoubleDocument(DOUBLE_FIELD_LENGTH));
        column.setCellEditor(new DefaultCellEditor(text));
        this.getColumnModel().getColumn(1).setCellRenderer(new AttributeRenderer(data));
        this.setRowHeight(ROW_HEIGHT);
        this.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent evt) {
                Point p = new Point(evt.getX(), evt.getY());
                int i = ClassifyInstanceTable.this.rowAtPoint(p);
                int j = ClassifyInstanceTable.this.columnAtPoint(p);
                ClassifyInstanceTable.this.changeSelection(i, j, false, false);
            }
        });
        this.setAutoResizeOff(false);
    }

    public AttributeStatistics getAttributeStatistics() {
        return attributeStatistics;
    }

    public DecimalFormat getDecimalFormat() {
        return decimalFormat;
    }

    public Instances data() {
        return model().data();
    }

    public Instance instance() throws Exception {
        Object[] vector = model().values();
        Instances data = data();
        Enumeration<Attribute> en = data.enumerateAttributes();
        Instance ins = new DenseInstance(data.numAttributes());
        ins.setDataset(data);
        try {
            while (en.hasMoreElements()) {
                Attribute a = en.nextElement();
                String strValue = (String) vector[a.index()];
                if (strValue == null || strValue.isEmpty()) {
                    throw new Exception("Не задано значение атрибута '" + a.name() + "'");
                }
                double value = decimalFormat.parse(strValue).doubleValue();
                if (a.isNominal() && (!strValue.matches("^[0-9]+$") || !a.isInRange(value))) {
                    throw new Exception("Недопустимое значение атрибута '" + a.name() + "'");
                }
                ins.setValue(a, value);
            }
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
        return ins;
    }

    public void reset() {
        for (int i = 0; i < this.getRowCount(); i++) {
            this.setValueAt(null, i, ClassifyInstanceTableModel.TEXT_INDEX);
        }
    }

    public ClassifyInstanceTableModel model() {
        return (ClassifyInstanceTableModel) this.getModel();
    }

    public StringBuilder getInfo(int i) {
        Attribute a = data().attribute(i);
        StringBuilder info = new StringBuilder("<html><head><style>"
                + ".attr {font-weight: bold;}</style></head><body>");
        info.append("<table><tr>");
        info.append("<td class = 'attr'>Атрибут:</td>").append("<td>").append(a.name()).append("</td>");
        info.append("</tr><tr>");
        info.append("<td class = 'attr'>Тип:</td>");
        if (a.isNumeric()) {
            info.append("<td>").append(a.isDate()? AttributesTypes.DATE : AttributesTypes.NUMERIC).append("</td>");
            info.append("</tr><tr>");
            info.append("<td class = 'attr'>Минимальное значение:</td>").
                    append("<td>").append(attributeStatistics.getMin(a)).append("</td>");
            info.append("</tr><tr>");
            info.append("<td class = 'attr'>Максимальное значение:</td>").
                    append("<td>").append(attributeStatistics.getMax(a)).
                    append("</td>");
            info.append("</tr><tr>");
            info.append("<td class = 'attr'>Математическое ожидание:</td>").
                    append("<td>").append(attributeStatistics.meanOrMode(a)).append("</td>");
            info.append("</tr><tr>");
            info.append("<td class = 'attr'>Дисперсия:</td>").
                    append("<td>").append(attributeStatistics.variance(a)).append("</td>");
            info.append("</tr><tr>");
            info.append("<td class = 'attr'>Среднеквадратическое отклонение:</td>").
                    append("<td>").append(attributeStatistics.stdDev(a)).append("</td>");
            info.append("</tr>");
        } else {
            info.append("<td>").append(AttributesTypes.NOMINAL).append("</td>");
            info.append("</tr><tr>");
            info.append("<td class = 'attr' colspan = '2' style = 'text-align: center;'>Значения:</td>");
            info.append("</tr>");
            for (int k = 0; k < a.numValues(); k++) {
                info.append("<tr>");
                info.append("<td>Код:</td>").append("<td>").append(k).append(", Значение: ").
                        append(a.value(k)).append("</td>");
                info.append("</tr>");
            }
        }
        info.append("</table></body></html>");
        return info;
    }

    /**
     *
     */
    private class AttributeRenderer extends JTextField
            implements TableCellRenderer {

        Instances data;

        AttributeRenderer(Instances data) {
            this.data = data;
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            int i = row >= data.classIndex() ? row + 1 : row;
            if (isSelected) {
                this.setForeground(table.getSelectionForeground());
                this.setBackground(table.getSelectionBackground());
            } else {
                this.setForeground(table.getForeground());
                this.setBackground(table.getBackground());
            }
            this.setToolTipText(getInfo(i).toString());
            this.setText(value.toString());
            this.setBorder(null);
            this.setFont(ClassifyInstanceTable.this.getTableHeader().getFont());
            return this;
        }

    } // End of class AttributeRender

}
