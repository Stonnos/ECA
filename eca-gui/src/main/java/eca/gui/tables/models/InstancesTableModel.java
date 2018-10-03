/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eca.gui.tables.models;

import eca.config.ConfigurationService;
import eca.text.NumericFormatFactory;
import eca.util.InstancesConverter;
import weka.core.Attribute;
import weka.core.Instances;

import javax.swing.table.AbstractTableModel;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.ListIterator;
import java.util.Objects;

/**
 * @author Roman Batygin
 */
public class InstancesTableModel extends AbstractTableModel {

    private static final ConfigurationService CONFIG_SERVICE =
            ConfigurationService.getApplicationConfigService();

    private static final SimpleDateFormat SIMPLE_DATE_FORMAT =
            new SimpleDateFormat(CONFIG_SERVICE.getApplicationConfig().getDateFormat());

    private static final String NUMBER = "№";

    private final Instances data;
    private final List<List<Object>> values;
    private final DecimalFormat format = NumericFormatFactory.getInstance();

    private int modificationCount;

    public InstancesTableModel(Instances data, int digits) {
        this.data = data;
        this.format.setMaximumFractionDigits(digits);
        this.values = InstancesConverter.toArray(data, format, SIMPLE_DATE_FORMAT);
    }

    public int getModificationCount() {
        return modificationCount;
    }

    public DecimalFormat format() {
        return format;
    }

    public Instances data() {
        return data;
    }

    /**
     * Remove specified row.
     *
     * @param i - row index
     */
    public void remove(int i) {
        values.remove(i);
        modificationCount++;
        fireTableRowsDeleted(i, i);
    }

    /**
     * Replaced all values.
     *
     * @param j      - column index
     * @param oldVal - old value
     * @param newVal - new value
     */
    public void replace(int j, Object oldVal, Object newVal) {
        for (int i = 0; i < values.size(); i++) {
            if ((oldVal.toString().isEmpty() && getValue(i, j) == null) ||
                    (getValue(i, j) != null && getValue(i, j).equals(oldVal))) {
                setValue(i, j, newVal.toString().isEmpty() ? null : newVal);
            }
        }
        this.fireTableDataChanged();
    }

    /**
     * Clear all data
     */
    public void clear() {
        for (List<Object> row : values) {
            row.clear();
        }
        values.clear();
        modificationCount++;
        fireTableDataChanged();
    }

    /**
     * Removed specified rows.
     *
     * @param indices - rows indices
     */
    public void remove(int[] indices) {
        for (int i = 0; i < indices.length; i++) {
            remove(indices[i] - i);
        }
    }

    /**
     * Removes rows with missing values.
     */
    public void removeMissing() {
        ListIterator<List<Object>> iterator = values.listIterator();
        while (iterator.hasNext()) {
            if (iterator.next().contains(null)) {
                iterator.remove();
                modificationCount++;
            }
        }
        fireTableDataChanged();
    }

    /**
     * Adds row with specified value at each cell.
     *
     * @param val - cell value
     */
    public void add(Object val) {
        ArrayList<Object> row = new ArrayList<>(getColumnCount());
        values.add(row);
        for (int i = 0; i < getColumnCount(); i++) {
            row.add(val);
        }
        modificationCount++;
        fireTableRowsInserted(getRowCount() - 1, getRowCount() - 1);
    }

    /**
     * Sorts data by specified column and attribute type.
     *
     * @param columnIndex   - column index
     * @param attributeType - attribute type
     * @param ascending     - sorts by ascending?
     */
    public void sort(final int columnIndex, final int attributeType, final boolean ascending) {
        values.sort((o1, o2) -> {
            Object x = o1.get(columnIndex - 1);
            Object y = o2.get(columnIndex - 1);
            int sign = ascending ? 1 : -1;
            if (Objects.equals(x, y)) {
                return 0;
            } else if (x == null) {
                return ascending ? sign : -sign;
            } else if (y == null) {
                return ascending ? -sign : sign;
            } else {
                switch (attributeType) {
                    case Attribute.DATE:
                        try {
                            Date dateX = SIMPLE_DATE_FORMAT.parse(x.toString());
                            Date dateY = SIMPLE_DATE_FORMAT.parse(y.toString());
                            return sign * dateX.compareTo(dateY);
                        } catch (ParseException ex) {
                            throw new IllegalArgumentException(ex.getMessage());
                        }
                    case Attribute.NUMERIC:
                        try {
                            Number numberX = format.parse(x.toString());
                            Number numberY = format.parse(y.toString());
                            return sign * Double.compare(numberX.doubleValue(), numberY.doubleValue());
                        } catch (ParseException ex) {
                            throw new IllegalArgumentException(ex.getMessage());
                        }
                    case Attribute.NOMINAL:
                        return sign * x.toString().compareTo(y.toString());
                    default:
                        throw new IllegalArgumentException(
                                String.format("Unexpected attribute type for column index %d!", columnIndex));
                }
            }
        });
        modificationCount++;
        fireTableDataChanged();
    }

    @Override
    public int getColumnCount() {
        return data.numAttributes() + 1;
    }

    @Override
    public int getRowCount() {
        return values.size();
    }

    @Override
    public Object getValueAt(int row, int column) {
        return column == 0 ? row + 1 : getValue(row, column - 1);
    }

    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        String value = aValue.toString().trim();
        setValue(rowIndex, columnIndex - 1, value.isEmpty() ? null : value);
        fireTableCellUpdated(rowIndex, columnIndex);
    }

    @Override
    public boolean isCellEditable(int row, int column) {
        return column > 0;
    }

    @Override
    public String getColumnName(int column) {
        return column == 0 ? NUMBER : data.attribute(column - 1).name();
    }

    private Object getValue(int i, int j) {
        return values.get(i).get(j);
    }

    private void setValue(int i, int j, Object val) {
        Object oldVal = getValue(i, j);
        if (!Objects.equals(oldVal, val)) {
            values.get(i).set(j, val);
            modificationCount++;
        }
    }
}
