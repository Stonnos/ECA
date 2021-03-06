package eca.gui.tables.models;

import eca.core.evaluation.Evaluation;
import eca.text.NumericFormatFactory;
import weka.core.Attribute;
import weka.core.Instances;
import weka.core.Utils;

import javax.swing.table.AbstractTableModel;
import java.text.DecimalFormat;

/**
 * @author Roman Batygin
 */
public class ClassificationCostsTableModel extends AbstractTableModel {

    private static final String NAN = "NaN";
    private final Evaluation ev;
    private static final String[] TITLES =
            {"Класс", "TPR", "FPR", "TNR", "FNR", "Полнота", "Точность", "F - мера", "AUC"};
    private Object[][] values;

    private final DecimalFormat format = NumericFormatFactory.getInstance();

    public ClassificationCostsTableModel(Evaluation ev, int digits) {
        this.ev = ev;
        format.setMaximumFractionDigits(digits);
        this.createMatrix();
    }

    public DecimalFormat getFormat() {
        return format;
    }

    @Override
    public int getColumnCount() {
        return TITLES.length;
    }

    @Override
    public int getRowCount() {
        return values.length;
    }

    @Override
    public Object getValueAt(int row, int column) {
        Attribute classAttribute = ev.getData().classAttribute();
        return column == 0 ? classAttribute.value(row) : values[row][column - 1];
    }

    @Override
    public String getColumnName(int column) {
        return TITLES[column];
    }

    private void createMatrix() {
        Instances data = ev.getData();
        values = new Object[data.numClasses()][TITLES.length - 1];
        for (int i = 0; i < data.numClasses(); i++) {
            values[i][0] = format.format(ev.truePositiveRate(i));
            values[i][1] = format.format(ev.falsePositiveRate(i));
            values[i][2] = format.format(ev.trueNegativeRate(i));
            values[i][3] = format.format(ev.falseNegativeRate(i));
            values[i][4] = format.format(ev.recall(i));
            values[i][5] = format.format(ev.precision(i));
            values[i][6] = format.format(ev.fMeasure(i));
            double auc = ev.areaUnderROC(i);
            values[i][7] = Utils.isMissingValue(auc) ? NAN : format.format(auc);
        }
    }

}
