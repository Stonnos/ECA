/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eca.gui.panels;

import eca.config.VelocityConfigService;
import eca.gui.ButtonUtils;
import eca.gui.tables.ROCThresholdTable;
import eca.roc.RocCurve;
import eca.roc.ThresholdModel;
import eca.text.NumericFormatFactory;
import org.apache.commons.lang3.StringUtils;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.labels.XYToolTipGenerator;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import weka.core.Attribute;
import weka.core.Instance;
import weka.core.Instances;

import javax.swing.*;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.StringWriter;
import java.text.DecimalFormat;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

/**
 * Roc - curve panel.
 *
 * @author Roman Batygin
 */
public class ROCCurvePanel extends JPanel {

    /**
     * Velocity configuration
     */
    private static final VelocityConfigService VELOCITY_CONFIGURATION =
            VelocityConfigService.getVelocityConfigService();

    private static final String VM_TEMPLATES_ROC_CURVE_VM = "vm-templates/optionsTable.vm";

    private static final String TITLE = "График ROC кривой";
    private static final String X_AXIS_TITLE = "100 - Специфичность (Specificity), %";
    private static final String Y_AXIS_TITLE = "Чувствительность (Sensitivity), %";
    private static final String ROC_CURVE_DATA_TITLE = "Данные ROC - кривой";
    private static final String SHOW_DATA_MENU_TEXT = "Показать данные";
    private static final String ALL_CLASSES_TEXT = "Все классы";
    private static final String CLASS_FORMAT = "Класс %s";
    private static final int IMAGE_WIDTH = 650;
    private static final int IMAGE_HEIGHT = 500;
    private static final Dimension PLOT_BOX_DIM = new Dimension(300, 25);
    private static final String OPTIMAL_THRESHOLD_POINT = "Показать точку оптимального порога";
    private static final String OPTIMAL_THRESHOLD = "Оптимальный порог";

    private final RocCurveTooltipGenerator tooltipGenerator = new RocCurveTooltipGenerator();
    private final DecimalFormat format = NumericFormatFactory.getInstance();
    private final RocCurve rocCurve;
    private ChartPanel chartPanel;
    private JFreeChart[] plots;
    private JFrame[] dataFrames;
    private JComboBox<String> plotBox;
    private final JFrame parentFrame;

    /**
     * Roc - curve panel constructor.
     *
     * @param rocCurve    - roc curve
     * @param parentFrame - parent frame
     * @param digits      - maximum fraction digits
     */
    public ROCCurvePanel(RocCurve rocCurve, JFrame parentFrame, final int digits) {
        this.rocCurve = rocCurve;
        this.parentFrame = parentFrame;
        this.format.setMaximumFractionDigits(digits);
        this.createPlots();
        this.createFrames();
        this.setLayout(new GridBagLayout());
        plotBox = new JComboBox<>();
        plotBox.setPreferredSize(PLOT_BOX_DIM);
        plotBox.setMaximumSize(PLOT_BOX_DIM);
        plotBox.setMinimumSize(PLOT_BOX_DIM);
        Attribute classAttr = rocCurve.getData().classAttribute();
        for (Enumeration i = classAttr.enumerateValues(); i.hasMoreElements(); ) {
            plotBox.addItem(String.format(CLASS_FORMAT, i.nextElement()));
        }
        plotBox.addItem(ALL_CLASSES_TEXT);
        plotBox.setSelectedIndex(plots.length - 1);
        plotBox.addActionListener(evt -> chartPanel.setChart(plots[plotBox.getSelectedIndex()]));

        JMenuItem dataMenu = new JMenuItem(SHOW_DATA_MENU_TEXT);
        dataMenu.addActionListener(evt -> {
            int i = plotBox.getSelectedIndex();
            if (i < plots.length - 1) {
                if (dataFrames[i] == null) {
                    dataFrames[i] = new RocCurveDataFrame(rocCurve.getROCCurve(i), digits,
                            rocCurve.getData().classAttribute().value(i));
                }
                dataFrames[i].setVisible(true);
            }
        });

        JCheckBoxMenuItem optThresholdMenu = new JCheckBoxMenuItem(OPTIMAL_THRESHOLD_POINT);
        optThresholdMenu.addActionListener(evt -> {
            int i = plotBox.getSelectedIndex();
            if (i < plots.length - 1) {
                XYPlot xyPlot = (XYPlot) plots[i].getPlot();
                XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer) xyPlot.getRenderer();
                renderer.setSeriesShapesVisible(0, optThresholdMenu.getState());
                renderer.setSeriesVisibleInLegend(0, optThresholdMenu.getState());
                if (optThresholdMenu.getState()) {
                    renderer.setSeriesToolTipGenerator(0, tooltipGenerator);
                    renderer.setSeriesToolTipGenerator(1, null);
                } else {
                    renderer.setSeriesToolTipGenerator(0, null);
                    renderer.setSeriesToolTipGenerator(1, tooltipGenerator);
                }
                xyPlot.setRenderer(renderer);
            }
        });

        chartPanel.getPopupMenu().addPopupMenuListener(new PopupMenuListener() {

            @Override
            public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
                boolean enabled = plotBox.getSelectedIndex() < plots.length - 1;
                dataMenu.setEnabled(enabled);
                optThresholdMenu.setEnabled(enabled);
            }

            @Override
            public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {

            }

            @Override
            public void popupMenuCanceled(PopupMenuEvent e) {

            }
        });
        chartPanel.getPopupMenu().add(dataMenu);
        chartPanel.getPopupMenu().add(optThresholdMenu);
        parentFrame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent evt) {
                for (JFrame frame : dataFrames) {
                    if (frame != null) {
                        frame.dispose();
                    }
                }
            }
        });
        this.add(chartPanel, new GridBagConstraints(0, 0, 1, 1, 1, 1,
                GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(5, 0, 5, 0), 0, 0));
        this.add(plotBox, new GridBagConstraints(0, 1, 1, 1, 0, 0,
                GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(5, 0, 5, 0), 0, 0));
    }

    private void createFrames() {
        dataFrames = new RocCurveDataFrame[rocCurve.getData().numClasses()];
    }

    private void createPlots() {
        plots = new JFreeChart[rocCurve.getData().numClasses() + 1];
        XYSeriesCollection allPlots = new XYSeriesCollection();
        for (int i = 0; i < rocCurve.getData().numClasses(); i++) {
            Instances rocSet = rocCurve.getROCCurve(i);
            XYSeriesCollection plot = new XYSeriesCollection();
            XYSeries points = new XYSeries(rocCurve.getData().classAttribute().value(i));
            rocSet.forEach(obj -> points.add(obj.value(RocCurve.SPECIFICITY_INDEX) * 100,
                    obj.value(RocCurve.SENSITIVITY_INDEX) * 100));
            calculateOptimalThreshold(plot, i);
            plot.addSeries(points);
            allPlots.addSeries(points);
            createChart(plot, i);
        }
        plots[plots.length - 1] = ChartFactory.createXYLineChart(TITLE, X_AXIS_TITLE, Y_AXIS_TITLE,
                allPlots, PlotOrientation.VERTICAL, true, true, false);
        XYPlot xyPlot = (XYPlot) plots[plots.length - 1].getPlot();
        xyPlot.getRenderer().setBaseToolTipGenerator(tooltipGenerator);
        chartPanel = new ChartPanel(plots[plots.length - 1]);
    }

    public Image createImage() {
        JFreeChart chart = chartPanel.getChart();
        return chart.createBufferedImage(IMAGE_WIDTH, IMAGE_HEIGHT);
    }

    private void createChart(XYSeriesCollection plot, int i) {
        plots[i] = ChartFactory.createXYLineChart(TITLE, X_AXIS_TITLE, Y_AXIS_TITLE,
                plot, PlotOrientation.VERTICAL, true, true, false);
        XYPlot xyPlot = (XYPlot) plots[i].getPlot();
        XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
        renderer.setSeriesShapesVisible(1, false);
        renderer.setSeriesShapesVisible(0, false);
        renderer.setSeriesLinesVisible(0, false);
        renderer.setSeriesVisibleInLegend(0, false);
        renderer.setSeriesToolTipGenerator(1, tooltipGenerator);
        xyPlot.setRenderer(renderer);
    }

    private void calculateOptimalThreshold(XYSeriesCollection xySeriesCollection, int classIndex) {
        ThresholdModel thresholdModel = rocCurve.findOptimalThreshold(classIndex);
        XYSeries points = new XYSeries(OPTIMAL_THRESHOLD);
        points.add(thresholdModel.getSpecificity() * 100, thresholdModel.getSensitivity() * 100);
        xySeriesCollection.addSeries(points);
    }

    /**
     * Roc - curve plot tool tip generator.
     */
    private class RocCurveTooltipGenerator implements XYToolTipGenerator {

        static final String PARAMS = "params";
        static final String SPECIFICITY = "Специфичность:";
        static final String SENSITIVITY = "Чувствительность:";

        @Override
        public String generateToolTip(XYDataset xyDataset, int series, int item) {
            Template template = VELOCITY_CONFIGURATION.getTemplate(VM_TEMPLATES_ROC_CURVE_VM);
            VelocityContext context = new VelocityContext();
            context.put(PARAMS, fillDataSetMap(xyDataset, series, item));
            StringWriter stringWriter = new StringWriter();
            template.merge(context, stringWriter);
            return stringWriter.toString();
        }

        private Map<String, String> fillDataSetMap(XYDataset xyDataset, int series, int item) {
            Map<String, String> params = new HashMap<>();
            params.put(SPECIFICITY, format.format(100.0 - xyDataset.getXValue(series, item)));
            params.put(SENSITIVITY, format.format(xyDataset.getYValue(series, item)));
            return params;
        }
    }

    /**
     * Roc - curve data frame.
     */
    private class RocCurveDataFrame extends JFrame {

        RocCurveDataFrame(Instances data, int digits, String className) {
            this.setTitle(ROC_CURVE_DATA_TITLE);
            this.setLayout(new GridBagLayout());
            this.setIconImage(parentFrame.getIconImage());
            ROCThresholdTable table = new ROCThresholdTable(data, digits, className);
            JScrollPane scrollPanel = new JScrollPane(table);
            JButton okButton = ButtonUtils.createOkButton();
            okButton.addActionListener(evt -> setVisible(false));
            this.add(scrollPanel, new GridBagConstraints(0, 0, 1, 1, 1, 1,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 0, 0, 0), 0, 0));
            this.add(okButton, new GridBagConstraints(0, 1, 1, 1, 0, 0,
                    GridBagConstraints.CENTER, GridBagConstraints.NONE,
                    new Insets(4, 0, 4, 0), 0, 0));
            this.pack();
            this.setLocationRelativeTo(parentFrame);
            this.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        }
    }

}
