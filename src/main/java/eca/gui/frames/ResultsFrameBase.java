/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eca.gui.frames;

import eca.core.converters.ModelConverter;
import eca.gui.panels.ROCCurvePanel;
import eca.gui.panels.ClassifyInstancePanel;
import eca.ensemble.ClassifiersSet;
import eca.ensemble.AbstractHeterogeneousClassifier;
import eca.ensemble.StackingClassifier;
import java.awt.event.*;
import java.awt.*;
import javax.imageio.ImageIO;
import javax.swing.*;
import eca.gui.PanelBorderUtils;
import eca.roc.RocCurve;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.xssf.usermodel.XSSFClientAnchor;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import weka.classifiers.Classifier;
import weka.classifiers.AbstractClassifier;
import eca.core.evaluation.Evaluation;
import weka.core.Instances;
import eca.gui.tables.ClassifyInstanceTable;
import eca.gui.tables.MisClassificationMatrix;
import eca.gui.tables.ClassificationCostsMatrix;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.ss.usermodel.*;
import java.io.FileOutputStream;
import java.text.DecimalFormat;
import eca.gui.choosers.SaveResultsChooser;
import eca.gui.choosers.SaveModelChooser;
import eca.beans.ModelDescriptor;
import eca.gui.ClassifierInputInfo;
import eca.gui.tables.EnsembleTable;
import eca.gui.tables.LogisticCoefficientsTable;
import eca.gui.tables.SignificantAttributesTable;
import eca.neural.NetworkVisualizer;
import eca.neural.NeuralNetwork;
import eca.regression.Logistic;
import eca.roc.AttributesSelection;
import eca.trees.DecisionTreeClassifier;
import eca.trees.TreeVisualizer;
import java.util.ArrayList;
import eca.gui.enums.AttributesTypes;
import eca.Reference;
import weka.core.Attribute;
import eca.core.ClassifierIndexer;
import eca.ensemble.EnsembleClassifier;

/**
 *
 * @author Рома
 */
public class ResultsFrameBase extends JFrame {

    private static final String RESULTS_TEXT = "Результаты классификации";
    private static final String STATISTICA_TEXT = "Статистика";
    private static final String MATRIX_TEXT = "Матрица классификации";
    private static final String ROC_CURVES_TEXT = "ROC кривые";

    
    private final Classifier classifier;
    private final Instances data;
    private final Evaluation ev;
    private final ClassifierIndexer indexer = new ClassifierIndexer();
    private JTabbedPane pane;
    private JScrollPane resultPane;

    private JTable statTable;
    private JTable misMatrix;
    private ClassificationCostsMatrix costMatrix;
    private JFrame parent;

    private ROCCurvePanel rocCurvePanel;

    public ResultsFrameBase(JFrame parent, String title, Classifier classifier, Instances data,
            Evaluation ev, final int digits)
            throws Exception {
        this.classifier = classifier;
        this.data = data;
        this.setTitle(title);
        this.parent = parent;
        this.setIconImage(parent.getIconImage());
        this.ev = ev;
        this.makeGUI(digits);
        this.makeMenu(digits);
        this.setLocationRelativeTo(parent);
    }

    public ClassifierIndexer getIndexer() {
        return indexer;
    }

    public Classifier classifier() {
        return classifier;
    }

    public Instances data() {
        return data;
    }

    public Evaluation evaluation() {
        return ev;
    }

    public final void addPanel(String title, Component panel) {
        pane.add(title, panel);
    }

    public final void setStatisticaTable(JTable table) {
        this.statTable = table;
        this.statTable.setRowSelectionAllowed(false);
        this.statTable.setToolTipText(ClassifierInputInfo.getInfo(classifier));
        resultPane.setViewportView(table);
    }

    private void makeMenu(final int digits) {
        JMenuBar menu = new JMenuBar();
        JMenu fileMenu = new JMenu("Файл");
        JMenu serviceMenu = new JMenu("Сервис");
        JMenu helpMenu = new JMenu("Справка");
        JMenuItem saveModelMenu = new JMenuItem("Сохранить модель");
        JMenuItem inputMenu = new JMenuItem("Входные параметры модели");
        JMenuItem refMenu = new JMenuItem("Показать справку");
        refMenu.setAccelerator(KeyStroke.getKeyStroke("F1"));
        JMenuItem attrMenu = new JMenuItem("Информация об атрибутах");
        JMenuItem dataMenu = new JMenuItem("Иcходные данные");
        JMenuItem statMenu = new JMenuItem("Статистика по атрибутам");
        //--------------------------------------------
        saveModelMenu.setAccelerator(KeyStroke.getKeyStroke("ctrl S"));
        //--------------------------------------------
        saveModelMenu.addActionListener(new ActionListener() {

            SaveModelChooser fileChooser;

            @Override
            public void actionPerformed(ActionEvent evt) {
                try {
                    if (fileChooser == null) {
                        fileChooser = new SaveModelChooser();
                    }
                    fileChooser.setSelectedFile(new File(indexer.getIndex(classifier())));
                    File file = fileChooser.saveFile(ResultsFrameBase.this);
                    if (file != null) {
                        ModelConverter.saveModel(file,
                                new ModelDescriptor((AbstractClassifier) classifier, data, ev, getTitle(), digits));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(ResultsFrameBase.this, e.getMessage(),
                            null, JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        //--------------------------------------------
        inputMenu.addActionListener(new ActionListener() {

            InfoFrame inputParamInfo;

            @Override
            public void actionPerformed(ActionEvent evt) {
                if (inputParamInfo == null) {
                    inputParamInfo = new InfoFrame(inputMenu.getText(), getInputInfo(), ResultsFrameBase.this);
                    ResultsFrameBase.this.addWindowListener(new WindowAdapter() {
                        @Override
                        public void windowClosing(WindowEvent evt) {
                            inputParamInfo.dispose();
                        }
                    });
                }
                inputParamInfo.setVisible(true);
            }
        });
        //--------------------------------------------
        attrMenu.addActionListener(new ActionListener() {

            InfoFrame attributesInfo;

            @Override
            public void actionPerformed(ActionEvent evt) {
                if (attributesInfo == null) {
                    attributesInfo = new InfoFrame(attrMenu.getText(), getAttributesInfo(), ResultsFrameBase.this);
                    ResultsFrameBase.this.addWindowListener(new WindowAdapter() {
                        @Override
                        public void windowClosing(WindowEvent evt) {
                            attributesInfo.dispose();
                        }
                    });
                }
                attributesInfo.setVisible(true);
            }
        });
        //--------------------------------------------
        refMenu.addActionListener(new ActionListener() {

            Reference ref;

            @Override
            public void actionPerformed(ActionEvent evt) {
                try {
                    if (ref == null) {
                        ref = new Reference();
                    }
                    ref.openReference();
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(ResultsFrameBase.this, e.getMessage(),
                            null, JOptionPane.ERROR_MESSAGE);
                }

            }
        });
        //-------------------------------------------------
        dataMenu.addActionListener(new ActionListener() {

            InstancesFrame dataFrame;

            @Override
            public void actionPerformed(ActionEvent evt) {
                if (dataFrame == null) {
                    dataFrame = new InstancesFrame(data, ResultsFrameBase.this);
                    dataFrame.setModal(false);
                    ResultsFrameBase.this.addWindowListener(new WindowAdapter() {
                        @Override
                        public void windowClosing(WindowEvent evt) {
                            dataFrame.dispose();
                        }
                    });
                }
                dataFrame.setVisible(true);
            }
        });
        //-------------------------------------------------
        statMenu.addActionListener(new ActionListener() {

            AttributesStatisticsFrame frame;

            @Override
            public void actionPerformed(ActionEvent evt) {
                if (frame == null) {
                    frame = new AttributesStatisticsFrame(data, ResultsFrameBase.this, digits);
                }
                frame.setVisible(true);
            }
        });
        //--------------------------------------------
        fileMenu.add(saveModelMenu);
        serviceMenu.add(dataMenu);
        serviceMenu.add(inputMenu);
        serviceMenu.add(attrMenu);
        serviceMenu.add(statMenu);
        helpMenu.add(refMenu);
        menu.add(fileMenu);
        menu.add(serviceMenu);
        menu.add(helpMenu);
        this.setJMenuBar(menu);
    }

    private void makeGUI(final int digits) throws Exception {
        this.setSize(875, 650);
        pane = new JTabbedPane();
        JPanel resultPanel = new JPanel(new GridBagLayout());
        //------------------------------------------------------
        resultPane = new JScrollPane();
        resultPane.setBorder(PanelBorderUtils.createTitledBorder(STATISTICA_TEXT));
        //------------------------------------------------------
        misMatrix = new MisClassificationMatrix(data, classifier, ev);
        JScrollPane misClassPane = new JScrollPane(misMatrix);
        misClassPane.setBorder(PanelBorderUtils.createTitledBorder(MATRIX_TEXT));
        //----------------------------------------
        costMatrix = new ClassificationCostsMatrix(data, ev, digits);
        JScrollPane costsPane = new JScrollPane(costMatrix);
        costsPane.setBorder(PanelBorderUtils.
                createTitledBorder(RESULTS_TEXT));
        //---------------------------------
        resultPanel.add(resultPane, new GridBagConstraints(0, 0, 1, 1, 1, 0.5,
                GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(5, 0, 5, 0), 0, 0));
        resultPanel.add(costsPane, new GridBagConstraints(0, 1, 1, 1, 1, 0.25,
                GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(5, 0, 5, 0), 0, 0));
        resultPanel.add(misClassPane, new GridBagConstraints(0, 2, 1, 1, 1, 0.25,
                GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(5, 0, 5, 0), 0, 0));
        //-----------------------------------
        JButton saveButton = new JButton("Сохранить");
        Dimension dim = new Dimension(150, 25);
        saveButton.setPreferredSize(dim);
        saveButton.setMinimumSize(dim);
        resultPanel.add(saveButton, new GridBagConstraints(0, 3, 1, 1, 1, 0,
                GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(5, 0, 5, 10), 0, 0));
        //----------------------------------
        saveButton.addActionListener(new ActionListener() {

            SaveResultsChooser chooser;
            XlsResultsSaver xlsResultsSaver;

            @Override
            public void actionPerformed(ActionEvent evt) {
                File file = null;
                if (chooser == null) {
                    chooser = new SaveResultsChooser();
                }
                chooser.setSelectedFile(new File(indexer.getResultsIndex(classifier())));
                try {
                    file = chooser.saveFile(ResultsFrameBase.this);
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(ResultsFrameBase.this, e.getMessage(),
                            null, JOptionPane.ERROR_MESSAGE);
                }
                if (file != null) {
                    if (xlsResultsSaver == null) {
                        xlsResultsSaver = new XlsResultsSaver();
                    }
                    try {
                        xlsResultsSaver.save(file);
                    } catch (Exception e) {
                        e.printStackTrace();
                        JOptionPane.showMessageDialog(ResultsFrameBase.this, e.getMessage(),
                                null, JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        });
        //---------------------------------
        rocCurvePanel = new ROCCurvePanel(new RocCurve(ev), this, digits);

        pane.add(RESULTS_TEXT, resultPanel);
        pane.add("Классификация", new ClassifyInstancePanel(
                new ClassifyInstanceTable(data, digits), classifier));
        pane.add(ROC_CURVES_TEXT, rocCurvePanel);
        //---------------------------------
        this.add(pane);
    }

    public JFrame getParentFrame() {
        return parent;
    }

    public static void createResults(ResultsFrameBase res, int digits) throws Exception {
        if (res != null) {
            if (res.classifier() instanceof DecisionTreeClassifier) {
                JScrollPane pane
                        = new JScrollPane(new TreeVisualizer((DecisionTreeClassifier) res.classifier(),
                                digits));
                res.addPanel("Структура дерева", pane);
                JScrollBar bar = pane.getHorizontalScrollBar();
                bar.setValue(bar.getMaximum());
            } else if (res.classifier() instanceof NeuralNetwork) {
                NeuralNetwork net = (NeuralNetwork) res.classifier();
                JScrollPane pane = new JScrollPane(new NetworkVisualizer(net, res, digits));
                res.addPanel("Структура нейронной сети", pane);
            } else if (res.classifier() instanceof Logistic) {
                LogisticCoefficientsTable table
                        = new LogisticCoefficientsTable((Logistic) res.classifier(), res.data(), digits);
                JScrollPane pane = new JScrollPane(table);
                res.addPanel("Оценки коэффициентов", pane);
                //-----------------------------------------
                AttributesSelection roc = new AttributesSelection(res.data());
                roc.calculate();
                SignificantAttributesTable signTable
                        = new SignificantAttributesTable(roc, digits);
                JScrollPane signPane = new JScrollPane(signTable);
                res.addPanel("Значимые атрибуты", signPane);
            } else if (res.classifier() instanceof EnsembleClassifier) {
                EnsembleTable table = new EnsembleTable((EnsembleClassifier) res.classifier(),
                        res.getParentFrame(), digits);
                JScrollPane pane = new JScrollPane(table);
                res.addPanel("Структура ансамбля", pane);
            }
        }
    }

    public String getAttributesInfo() {
        String separator = System.getProperty("line.separator");
        StringBuilder attrInfo = new StringBuilder();
        for (int i = 0; i < data().numAttributes(); i++) {
            attrInfo.append(getInfo(data().attribute(i))).append(separator).append(separator);
        }
        return attrInfo.toString();
    }

    public StringBuilder getInfo(Attribute a) {
        String separator = System.getProperty("line.separator");
        StringBuilder attrInfo = new StringBuilder();
        ClassifyInstancePanel panel = (ClassifyInstancePanel) pane.getComponentAt(1);
        attrInfo.append("Атрибут:  ").append(a.name()).append(separator);
        attrInfo.append("Тип:  ");
        if (a.isNumeric()) {
            attrInfo.append(a.isDate() ? AttributesTypes.DATE : AttributesTypes.NUMERIC).append(separator);
            attrInfo.append("Минимальное значение:  ").
                    append(panel.getAttributeStatistics().getMin(a)).append(separator);
            attrInfo.append("Максимальное значение:  ").
                    append(panel.getAttributeStatistics().getMax(a)).
                    append(separator);
            attrInfo.append("Математическое ожидание:  ").
                    append(panel.getAttributeStatistics().meanOrMode(a)).append(separator);
            attrInfo.append("Дисперсия:  ")
                    .append(panel.getAttributeStatistics().variance(a)).append(separator);
            attrInfo.append("Среднеквадратическое отклонение:  ").
                    append(panel.getAttributeStatistics().stdDev(a)).append(separator);
        } else {
            attrInfo.append(AttributesTypes.NOMINAL).append(separator);
            attrInfo.append("Значения:").append(separator);
            for (int k = 0; k < a.numValues(); k++) {
                attrInfo.append("Код:  ").append(k).append(", Значение: ").
                        append(a.value(k)).append(separator);
            }
        }
        return attrInfo;
    }

    private String getInputInfo() {
        String separator = System.getProperty("line.separator");
        AbstractClassifier cls = (AbstractClassifier) classifier;
        StringBuilder inputInfo = new StringBuilder();
        String[] options = cls.getOptions();
        setOptions(inputInfo, options);
        if (cls instanceof AbstractHeterogeneousClassifier) {
            inputInfo.append(separator).append("Входные параметры базовых классификаторов:").append(separator);
            AbstractHeterogeneousClassifier ens = (AbstractHeterogeneousClassifier) cls;
            ClassifiersSet set = ens.getClassifiersSet();
            setOptionsForEnsemble(inputInfo, set.toList());
        }
        if (cls instanceof StackingClassifier) {
            inputInfo.append(separator).append("\nВходные параметры базовых классификаторов:").append(separator);
            StackingClassifier ens = (StackingClassifier) cls;
            setOptionsForEnsemble(inputInfo, ens.getClassifiers().toList());
            //----------------------------------------------------
            inputInfo.append("Входные параметры мета-классификатора:");
            String[] metaOptions = ((AbstractClassifier) ens.getMetaClassifier()).getOptions();
            inputInfo.append(separator).append("Мета-классификатор:   ").
                    append(ens.getMetaClassifier().getClass().getSimpleName()).
                    append(separator).append("Параметры:").append(separator);
            setOptions(inputInfo, metaOptions);
        }
        return inputInfo.toString();
    }

    private void setOptions(StringBuilder info, String[] options) {
        String separator = System.getProperty("line.separator");
        for (int i = 0; i < options.length; i += 2) {
            info.append(options[i]).append("   ").append(options[i + 1]).append(separator);
        }
    }

    private void setOptionsForEnsemble(StringBuilder info, ArrayList<Classifier> set) {
        String separator = System.getProperty("line.separator");
        for (int i = 0; i < set.size(); i++) {
            AbstractClassifier single = (AbstractClassifier) set.get(i);
            info.append("Базовый классификатор:  ").append(single.getClass().getSimpleName()).append(separator);
            info.append("Входные параметры:").append(separator);
            String[] singleOptions = single.getOptions();
            setOptions(info, singleOptions);
            info.append(separator);
        }
    }

    /**
     *
     */
    private class XlsResultsSaver {

        void save(File file) throws Exception {
            try (FileOutputStream stream = new FileOutputStream(file)) {
                Workbook book = file.getName().endsWith(".xls") ?
                        new HSSFWorkbook() : new XSSFWorkbook();

                Font font = book.createFont();
                font.setBold(true);
                font.setFontHeightInPoints((short) 12);
                CellStyle style = book.createCellStyle();
                style.setFont(font);

                createXlsInputParamSheet(book, style);
                createXlsResultsSheet(book, style);
                writePicture(file, book, (BufferedImage) rocCurvePanel.createImage(),ROC_CURVES_TEXT,5,5);

                book.write(stream);
            }
        }

        void createXlsInputParamSheet(Workbook book, CellStyle style) {
            Sheet sheet = book.createSheet("Входные параметры");
            AbstractClassifier cls = (AbstractClassifier) classifier;

            createTitle(sheet, style, "Входные параметры классификатора");
            createPair(sheet, style , "Классификатор", cls.getClass().getSimpleName());

            String[] options = cls.getOptions();
            setXlsClassifierOptions(sheet, options);
            if (cls instanceof AbstractHeterogeneousClassifier) {
                createTitle(sheet, style, "Входные параметры базовых классификаторов:");
                AbstractHeterogeneousClassifier ens = (AbstractHeterogeneousClassifier) cls;
                ClassifiersSet set = ens.getClassifiersSet();
                setXlsEnsembleOptions(sheet, style, set.toList());
            }
            if (cls instanceof StackingClassifier) {
                createTitle(sheet, style, "Входные параметры базовых классификаторов:");
                StackingClassifier ens = (StackingClassifier) cls;
                setXlsEnsembleOptions(sheet, style, ens.getClassifiers().toList());
                createTitle(sheet, style, "Входные параметры мета-классификатора");
                String[] metaOptions = ((AbstractClassifier) ens.getMetaClassifier()).getOptions();
                createPair(sheet, style, "Мета-классификатор:",
                        ens.getMetaClassifier().getClass().getSimpleName());
                setXlsClassifierOptions(sheet, metaOptions);
            }
        }

        void setXlsClassifierOptions(Sheet sheet, String[] options) {
            for (int i = 0; i < options.length; i += 2) {
                Row row = sheet.createRow(sheet.getPhysicalNumberOfRows());
                Cell cell = row.createCell(0);
                cell.setCellValue(options[i]);
                sheet.autoSizeColumn(0);
                cell = row.createCell(1);
                cell.setCellValue(options[i + 1]);
                sheet.autoSizeColumn(1);
            }
        }

        void setXlsEnsembleOptions(Sheet sheet, CellStyle style, ArrayList<Classifier> set) {
            for (int i = 0; i < set.size(); i++) {
                AbstractClassifier single = (AbstractClassifier) set.get(i);
                createPair(sheet, style, "Базовый классификатор:", single.getClass().getSimpleName());
                createTitle(sheet, style, "Входные параметры:");
                String[] singleOptions = single.getOptions();
                setXlsClassifierOptions(sheet, singleOptions);
            }
        }

        void createTitle(Sheet sheet, CellStyle style, String title) {
            Row row = sheet.createRow(sheet.getPhysicalNumberOfRows());
            Cell cell = row.createCell(0);
            cell.setCellStyle(style);
            cell.setCellValue(title);
            sheet.autoSizeColumn(0);
        }

        void createPair(Sheet sheet, CellStyle style, String title1, String title2) {
            Row row = sheet.createRow(sheet.getPhysicalNumberOfRows());
            Cell cell = row.createCell(0);
            cell.setCellStyle(style);
            cell.setCellValue(title1);
            sheet.autoSizeColumn(0);
            cell = row.createCell(1);
            cell.setCellStyle(style);
            cell.setCellValue(title2);
            sheet.autoSizeColumn(1);
        }

        void createXlsResultsSheet(Workbook book, CellStyle style) throws Exception {
            Sheet sheet = book.createSheet(RESULTS_TEXT);
            Row row = sheet.createRow(sheet.getPhysicalNumberOfRows());
            Cell cell = row.createCell(0);
            cell.setCellStyle(style);
            cell.setCellValue(STATISTICA_TEXT);
            DecimalFormat fmt = costMatrix.getFormat();
            //------------------------------
            for (int i = 0; i < statTable.getRowCount(); i++) {
                row = sheet.createRow(sheet.getPhysicalNumberOfRows());
                cell = row.createCell(0);
                cell.setCellValue((String) statTable.getValueAt(i, 0));
                cell = row.createCell(1);
                try {
                    cell.setCellValue(fmt.parse((String) statTable.getValueAt(i, 1)).doubleValue());
                } catch (Exception e) {
                    cell.setCellValue((String) statTable.getValueAt(i, 1));
                }
            }
            //------------------------------------------
            row = sheet.createRow(sheet.getPhysicalNumberOfRows());
            cell = row.createCell(0);
            cell.setCellStyle(style);
            cell.setCellValue(RESULTS_TEXT);
            row = sheet.createRow(sheet.getPhysicalNumberOfRows());
            for (int i = 0; i < costMatrix.getColumnCount(); i++) {
                cell = row.createCell(i);
                cell.setCellValue(costMatrix.getColumnName(i));
            }
            //----------------------------
            for (int i = 0; i < costMatrix.getRowCount(); i++) {
                row = sheet.createRow(sheet.getPhysicalNumberOfRows());
                for (int j = 0; j < costMatrix.getColumnCount(); j++) {
                    cell = row.createCell(j);
                    if (j == 0) {
                        cell.setCellValue((Integer) costMatrix.getValueAt(i, j));
                    } else {
                        cell.setCellValue(fmt.parse((String) costMatrix.getValueAt(i, j)).doubleValue());
                    }
                }
            }
            //------------------------------
            row = sheet.createRow(sheet.getPhysicalNumberOfRows());
            cell = row.createCell(0);
            cell.setCellStyle(style);
            cell.setCellValue(MATRIX_TEXT);
            row = sheet.createRow(sheet.getPhysicalNumberOfRows());
            for (int i = 0; i < misMatrix.getColumnCount(); i++) {
                cell = row.createCell(i);
                cell.setCellValue(misMatrix.getColumnName(i));
                sheet.autoSizeColumn(i);
            }
            //-------------------------------------------------
            for (int i = 0; i < misMatrix.getRowCount(); i++) {
                row = sheet.createRow(sheet.getPhysicalNumberOfRows());
                for (int j = 0; j < misMatrix.getColumnCount(); j++) {
                    cell = row.createCell(j);
                    cell.setCellValue((Integer) misMatrix.getValueAt(i, j));
                }
            }
        }

        void writePicture(File file, Workbook book, BufferedImage bImage, String title, int col, int row) throws Exception {
            Sheet sheet = book.createSheet(title);
            ByteArrayOutputStream byteArrayImg = new ByteArrayOutputStream();
            ImageIO.write(bImage, "PNG", byteArrayImg);
            int pictureIdx = sheet.getWorkbook().addPicture(
                    byteArrayImg.toByteArray(),
                    sheet.getWorkbook().PICTURE_TYPE_PNG);

            short col1=0, col2=0;
            ClientAnchor anchor;

            if (file.getName().endsWith(".xls")) {
                anchor = new HSSFClientAnchor(0, 0, 0, 0, col1, 0, col2, 0);
            }
            else {
                anchor = new XSSFClientAnchor(0, 0, 0, 0, col1, 0, col2, 0);
            }

            anchor.setCol1(col);
            anchor.setRow1(row);

            Drawing drawing = sheet.createDrawingPatriarch();
            Picture pict = drawing.createPicture(anchor, pictureIdx);
            pict.resize();
        }

    }

}
