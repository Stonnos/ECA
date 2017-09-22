/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eca.gui.tables;

import eca.core.InstancesHandler;
import eca.gui.logging.LoggerUtils;
import eca.core.evaluation.Evaluation;
import eca.ensemble.EnsembleClassifier;
import eca.gui.service.ClassifierInputOptionsService;
import eca.gui.GuiUtils;
import eca.gui.JButtonEditor;
import eca.gui.JButtonRenderer;
import eca.gui.frames.ClassificationResultsFrameBase;
import eca.gui.tables.models.EnsembleTableModel;
import lombok.extern.slf4j.Slf4j;
import weka.classifiers.Classifier;
import weka.core.Instances;

import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;

/**
 * @author Roman Batygin
 */
@Slf4j
public class EnsembleTable extends JDataTableBase {

    private final JFrame parent;
    private final int digits;

    public EnsembleTable(EnsembleClassifier struct, JFrame parent, int digits) throws Exception {
        super(new EnsembleTableModel(struct));
        this.parent = parent;
        this.digits = digits;
        this.getColumnModel().getColumn(1).setCellRenderer(new ClassifierRenderer());
        this.getColumnModel().getColumn(2)
                .setCellRenderer(new JButtonRenderer(EnsembleTableModel.RESULT_TITLE));
        this.getColumnModel().getColumn(2).setCellEditor(new JButtonEnsembleEditor());
        this.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent evt) {
                Point p = new Point(evt.getX(), evt.getY());
                int i = EnsembleTable.this.rowAtPoint(p);
                int j = EnsembleTable.this.columnAtPoint(p);
                EnsembleTable.this.changeSelection(i, j, false, false);
            }
        });
        this.getColumnModel().getColumn(0).setMaxWidth(50);
        this.setAutoResizeOff(false);
    }

    public EnsembleTableModel ensembleModel() {
        return (EnsembleTableModel) this.getModel();
    }

    /**
     *
     */
    private class ClassifierRenderer extends JTextField
            implements TableCellRenderer {

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus, int row, int column) {
            GuiUtils.updateForegroundAndBackGround(this, table, isSelected);
            this.setToolTipText(ClassifierInputOptionsService.getInputOptionsInfoAsHtml(ensembleModel().get(row)));
            this.setText(value.toString());
            this.setBorder(null);
            this.setFont(EnsembleTable.this.getTableHeader().getFont());
            return this;
        }
    } //End of class ClassifierRenderer

    /**
     *
     */
    private class JButtonEnsembleEditor extends JButtonEditor {

        private Classifier classifier;

        JButtonEnsembleEditor() {
            super(EnsembleTableModel.RESULT_TITLE);
        }

        @Override
        protected void doOnPushing(JTable table, Object value,
                                   boolean isSelected, int row, int column) {
            classifier = ensembleModel().get(row);
        }

        @Override
        protected void doAfterPushing() {
            try {
                Instances data = ((InstancesHandler) classifier).getData();
                Evaluation e = new Evaluation(data);
                e.evaluateModel(classifier, data);
                ClassificationResultsFrameBase result = new ClassificationResultsFrameBase(parent, classifier.getClass().getSimpleName(),
                        classifier, data, e, digits);
                ClassificationResultsFrameBase.createResults(result, digits);
                StatisticsTableBuilder stat = new StatisticsTableBuilder(digits);
                result.setStatisticsTable(stat.createStatistics(classifier, e));
                result.setVisible(true);
            } catch (Exception e) {
                LoggerUtils.error(log, e);
                JOptionPane.showMessageDialog(EnsembleTable.this.getParent(), e.getMessage(),
                        null, JOptionPane.ERROR_MESSAGE);
            }
        }
    }

}
