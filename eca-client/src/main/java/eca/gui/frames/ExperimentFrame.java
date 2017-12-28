/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eca.gui.frames;

import eca.converters.ModelConverter;
import eca.converters.model.ExperimentHistory;
import eca.core.evaluation.EvaluationMethod;
import eca.core.evaluation.EvaluationMethodVisitor;
import eca.core.evaluation.EvaluationResults;
import eca.dataminer.AbstractExperiment;
import eca.dataminer.IterativeExperiment;
import eca.gui.PanelBorderUtils;
import eca.gui.actions.CallbackAction;
import eca.gui.choosers.OpenModelChooser;
import eca.gui.choosers.SaveModelChooser;
import eca.gui.dialogs.EvaluationMethodOptionsDialog;
import eca.gui.dialogs.LoadDialog;
import eca.gui.logging.LoggerUtils;
import eca.gui.service.ClassifierIndexerService;
import eca.gui.service.ClassifierInputOptionsService;
import eca.gui.service.ExecutorService;
import eca.gui.tables.ExperimentTable;
import eca.gui.tables.StatisticsTableBuilder;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import weka.classifiers.AbstractClassifier;
import weka.core.Instances;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * @author Roman Batygin
 */
@Slf4j
public abstract class ExperimentFrame extends JFrame {

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("HH:mm:ss:SSS");

    private static final String BUILDING_PROGRESS_TITLE = "Пожалуйста подождите, идет построение моделей...";
    private static final String LOAD_EXPERIMENT_TITLE = "Пожалуйста подождите, идет загрузка истории эксперимента...";
    private static final int MINIMUM_NUMBER_OF_FOLDS = 2;
    private static final int MAXIMUM_NUMBER_OF_FOLDS = 100;
    private static final int MINIMUM_NUMBER_OF_TESTS = 1;
    private static final int MAXIMUM_NUMBER_OF_TESTS = 100;
    private static final String EXPERIMENT_HISTORY_TITLE = "История эксперимента";
    private static final String INFO_TITLE = "Информация";
    private static final String START_BUTTON_TEXT = "Начать эксперимент";
    private static final String STOP_BUTTON_TEXT = "Остановить эксперимент";
    private static final String OPTIONS_BUTTON_TEXT = "Настройки";
    private static final String SAVE_BUTTON_TEXT = "Сохранить эксперимент";
    private static final String LOAD_BUTTON_TEXT = "Загрузить эксперимент";

    private static final Font TEXT_AREA_FONT = new Font("Arial", Font.BOLD, 13);
    private static final String EXPERIMENT_END_TEXT = "Эксперимент завершен.";
    private static final String EVALUATION_METHOD_TEXT = "Метод оценки точности: ";
    private static final String BEST_CLASSIFIER_STRUCTURES_TEXT = "Наилучшие конфигурации классификаторов:";
    private static final String NUMBER_PREFIX = "№";
    private static final int DEFAULT_WIDTH = 1100;
    private static final int DEFAULT_HEIGHT = 600;
    private static final String START_TIME_TEXT = "00:00:00:00";
    private static final int TIMER_FIELD_LENGTH = 20;
    private static final String TIMER_LABEL_TEXT = "Время выполнения эксперимента";
    private static final int TIMER_DELAY_IN_MILLIS = 1;

    private final long experimentId = System.currentTimeMillis();

    private final AbstractExperiment experiment;

    private JProgressBar progress;
    private JTextArea text;
    private JRadioButton useTrainingSet;
    private JRadioButton useTestingSet;
    private JSpinner foldsSpinner = new JSpinner();
    private JSpinner validationsSpinner = new JSpinner();

    private JButton startButton;
    private JButton optionsButton;
    private JButton saveButton;
    private JButton loadButton;
    private JButton stopButton;
    private JPanel left;

    private JTextField timerField;

    private ExperimentTable table;
    private SwingWorker<Void, Void> worker;
    private SwingWorker<Void, Void> timer;
    private final int digits;

    static {
        DATE_FORMAT.setTimeZone(TimeZone.getTimeZone("GMT"));
    }

    protected ExperimentFrame(AbstractExperiment experiment, JFrame parent, int digits) throws Exception {
        this.experiment = experiment;
        this.digits = digits;
        this.setIconImage(parent.getIconImage());
        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent evt) {
                if (worker != null && !worker.isCancelled()) {
                    worker.cancel(true);
                }
            }
        });
        this.makeGUI();
        this.setLocationRelativeTo(parent);
    }

    public AbstractExperiment getExperiment() {
        return experiment;
    }

    public int getDigits() {
        return digits;
    }

    public final void setStateForButtons(boolean flag) {
        startButton.setEnabled(flag);
        optionsButton.setEnabled(flag);
        loadButton.setEnabled(flag);
        saveButton.setEnabled(flag);
        stopButton.setEnabled(!flag);
    }

    public final void setStateForOptions(boolean flag) {
        for (Component c : left.getComponents()) {
            c.setEnabled(flag);
        }
        if (flag && useTrainingSet.isSelected()) {
            foldsSpinner.setEnabled(false);
            validationsSpinner.setEnabled(false);
        }
    }

    public final void setDataInfo(Instances data) {
        text.setText(ClassifierInputOptionsService.getInstancesInfo(data));
        text.setCaretPosition(0);
    }

    public final void setResults() {
        setResults(experiment.getData());
    }

    public final void setResults(Instances data) {
        final StringBuilder str = new StringBuilder();
        str.append(EXPERIMENT_END_TEXT).append("\n");
        str.append(ClassifierInputOptionsService.getInstancesInfo(data));
        str.append(EVALUATION_METHOD_TEXT);

        getExperiment().getEvaluationMethod().accept(new EvaluationMethodVisitor<Void>() {

            @Override
            public Void evaluateModel() {
                str.append(StatisticsTableBuilder.TRAINING_DATA_METHOD_TEXT);
                return null;
            }

            @Override
            public Void crossValidateModel() {
                String evaluationMethodStr = String.format(StatisticsTableBuilder.CROSS_VALIDATION_METHOD_FORMAT,
                        (getExperiment().getNumTests() > 1 ? getExperiment().getNumTests() + "*" : StringUtils.EMPTY),
                        getExperiment().getNumFolds());

                str.append(evaluationMethodStr);
                return null;
            }
        });

        str.append("\n\n").append(BEST_CLASSIFIER_STRUCTURES_TEXT).append("\n");
        for (int i = 0; i < Integer.min(table.getBestNumber(), table.getRowCount()); i++) {
            str.append(table.experimentModel().getClassifier(i).getClass().getSimpleName())
                    .append(StringUtils.SPACE).append(NUMBER_PREFIX).append(i).append("\n");
            AbstractClassifier cls = (AbstractClassifier) table.experimentModel().getClassifier(i);
            String[] options = cls.getOptions();
            for (int j = 0; j < options.length; j += 2) {
                str.append(options[j]).append(StringUtils.SPACE).append(options[j + 1]).append("\n");
            }
            str.append("\n");
        }
        text.setText(str.toString());
        text.setCaretPosition(0);
    }

    protected abstract void setOptions();

    protected void doBegin() {
        progress.setValue(0);
        worker = new ExperimentWorker();
        timer = new TimeWorker();
    }

    private void makeGUI() throws Exception {
        this.setSize(DEFAULT_WIDTH, DEFAULT_HEIGHT);
        this.setLayout(new GridBagLayout());
        table = new ExperimentTable(new java.util.ArrayList<>(), this, experiment.getData(), digits);
        JPanel top = new JPanel(new GridBagLayout());
        text = new JTextArea(10, 10);
        text.setEditable(false);
        text.setFont(TEXT_AREA_FONT);
        text.setWrapStyleWord(true);
        text.setLineWrap(true);
        JScrollPane bottom = new JScrollPane(text);
        bottom.setBorder(PanelBorderUtils.createTitledBorder(INFO_TITLE));
        setDataInfo(experiment.getData());
        progress = new JProgressBar();
        progress.setStringPainted(true);
        //----------------------------------------------
        left = new JPanel(new GridBagLayout());
        JScrollPane right = new JScrollPane(table);
        JPanel leftBottom = new JPanel(new GridBagLayout());
        left.setBorder(PanelBorderUtils.createTitledBorder(EvaluationMethodOptionsDialog.METHOD_TITLE));
        //-------------------------------------------------------------
        ButtonGroup group = new ButtonGroup();
        useTrainingSet = new JRadioButton(EvaluationMethod.TRAINING_DATA.getDescription());
        useTestingSet = new JRadioButton(EvaluationMethod.CROSS_VALIDATION.getDescription());
        group.add(useTrainingSet);
        group.add(useTestingSet);
        //---------------------------------
        foldsSpinner.setModel(new SpinnerNumberModel(experiment.getNumFolds(), MINIMUM_NUMBER_OF_FOLDS,
                MAXIMUM_NUMBER_OF_FOLDS, 1));
        validationsSpinner.setModel(new SpinnerNumberModel(experiment.getNumTests(), MINIMUM_NUMBER_OF_TESTS,
                MAXIMUM_NUMBER_OF_TESTS, 1));
        foldsSpinner.setEnabled(false);
        validationsSpinner.setEnabled(false);

        getExperiment().getEvaluationMethod().accept(new EvaluationMethodVisitor<Void>() {

            @Override
            public Void evaluateModel() {
                useTrainingSet.setSelected(true);
                return null;
            }

            @Override
            public Void crossValidateModel() {
                useTestingSet.setSelected(true);
                return null;
            }
        });

        useTestingSet.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent evt) {
                foldsSpinner.setEnabled(useTestingSet.isSelected());
                validationsSpinner.setEnabled(useTestingSet.isSelected());
            }
        });
        //------------------------------------------------------
        left.add(useTrainingSet, new GridBagConstraints(0, 0, 2, 1, 1, 1,
                GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 10, 5, 0), 0, 0));
        left.add(useTestingSet, new GridBagConstraints(0, 1, 2, 1, 1, 1,
                GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 10, 5, 0), 0, 0));
        left.add(new JLabel(EvaluationMethodOptionsDialog.BLOCKS_NUM_TITLE), new GridBagConstraints(0, 2, 1, 1, 1, 1,
                GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0, 10, 10, 10), 0, 0));
        left.add(foldsSpinner, new GridBagConstraints(1, 2, 1, 1, 1, 1,
                GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 10, 10), 0, 0));
        left.add(new JLabel(EvaluationMethodOptionsDialog.TESTS_NUM_TITLE), new GridBagConstraints(0, 3, 1, 1, 1, 1,
                GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0, 10, 10, 10), 0, 0));
        left.add(validationsSpinner, new GridBagConstraints(1, 3, 1, 1, 1, 1,
                GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 10, 10), 0, 0));
        //--------------------------------------------------------------
        startButton = new JButton(START_BUTTON_TEXT);
        stopButton = new JButton(STOP_BUTTON_TEXT);
        stopButton.setEnabled(false);
        optionsButton = new JButton(OPTIONS_BUTTON_TEXT);
        saveButton = new JButton(SAVE_BUTTON_TEXT);
        loadButton = new JButton(LOAD_BUTTON_TEXT);
        //---------------------------------------------------------------
        startButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                experiment.setEvaluationMethod(useTestingSet.isSelected()
                        ? EvaluationMethod.CROSS_VALIDATION : EvaluationMethod.TRAINING_DATA);
                if (useTestingSet.isSelected()) {
                    experiment.setNumFolds(((SpinnerNumberModel) foldsSpinner.getModel()).getNumber().intValue());
                    experiment.setNumTests(
                            ((SpinnerNumberModel) validationsSpinner.getModel()).getNumber().intValue());
                }
                text.setText(BUILDING_PROGRESS_TITLE);
                setStateForButtons(false);
                setStateForOptions(false);
                table.setRenderer(Color.BLACK);
                table.clear();
                doBegin();
                worker.execute();
                timer.execute();
                log.info("Starting experiment with id {} for classifier '{}'.", experimentId,
                        experiment.getClassifier().getClass().getSimpleName());
            }
        });
        //---------------------------------------------------------------
        stopButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                worker.cancel(true);
            }
        });
        //---------------------------------------------------------------
        optionsButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                setOptions();
            }
        });
        //---------------------------------------------------------------
        saveButton.addActionListener(new ActionListener() {

            SaveModelChooser fileChooser;

            @Override
            public void actionPerformed(ActionEvent evt) {
                try {
                    if (fileChooser == null) {
                        fileChooser = new SaveModelChooser();
                    }
                    ClassifierIndexerService indexer = new ClassifierIndexerService();
                    fileChooser.setSelectedFile(new File(indexer.getExperimentIndex(experiment.getClassifier())));
                    File file = fileChooser.getSelectedFile(ExperimentFrame.this);
                    if (file != null) {
                        ModelConverter.saveModel(file,
                                new ExperimentHistory(table.experimentModel().getExperiment(), experiment.getData()));
                    }
                } catch (Exception e) {
                    LoggerUtils.error(log, e);
                    JOptionPane.showMessageDialog(ExperimentFrame.this, e.getMessage(),
                            null, JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        //---------------------------------------------------------------
        loadButton.addActionListener(new ActionListener() {

            OpenModelChooser fileChooser;

            @Override
            public void actionPerformed(ActionEvent evt) {
                if (fileChooser == null) {
                    fileChooser = new OpenModelChooser();
                }
                File file = fileChooser.openFile(ExperimentFrame.this);
                if (file != null) {
                    try {
                        ExperimentLoader loader = new ExperimentLoader(file);
                        LoadDialog progress = new LoadDialog(ExperimentFrame.this,
                                loader, LOAD_EXPERIMENT_TITLE);

                        ExecutorService.process(progress, new CallbackAction() {
                            @Override
                            public void apply() throws Exception {
                                table.setRenderer(Color.RED);
                                ExperimentHistory history = loader.getExperiment();
                                table.setExperiment(history.getExperiment());
                                setResults(history.getDataSet());
                            }
                        }, new CallbackAction() {
                            @Override
                            public void apply() throws Exception {
                                JOptionPane.showMessageDialog(ExperimentFrame.this,
                                        progress.getErrorMessageText(),
                                        null, JOptionPane.WARNING_MESSAGE);
                            }
                        });

                    } catch (Throwable e) {
                        LoggerUtils.error(log, e);
                        JOptionPane.showMessageDialog(ExperimentFrame.this,
                                e.getMessage(), null, JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        });

        timerField = new JTextField(TIMER_FIELD_LENGTH);
        timerField.setEditable(false);
        timerField.setText(START_TIME_TEXT);
        timerField.setBackground(Color.WHITE);
        //---------------------------------------------------------------
        leftBottom.add(optionsButton,
                new GridBagConstraints(0, 0, 1, 1, 1, 0,
                        GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 5, 10, 5), 0, 0));
        leftBottom.add(startButton,
                new GridBagConstraints(0, 1, 1, 1, 1, 0,
                        GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 5, 10, 5), 0, 0));
        leftBottom.add(stopButton,
                new GridBagConstraints(0, 2, 1, 1, 1, 0,
                        GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 5, 10, 5), 0, 0));
        leftBottom.add(loadButton,
                new GridBagConstraints(0, 3, 1, 1, 1, 0,
                        GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 5, 10, 5), 0, 0));
        leftBottom.add(saveButton,
                new GridBagConstraints(0, 4, 1, 1, 1, 0,
                        GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 5, 10, 5), 0, 0));
        leftBottom.add(new JLabel(TIMER_LABEL_TEXT),
                new GridBagConstraints(0, 5, 1, 1, 1, 0,
                        GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(25, 5, 5, 5), 0, 0));
        leftBottom.add(timerField,
                new GridBagConstraints(0, 6, 1, 1, 1, 0,
                        GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 5, 10, 5), 0, 0));
        //---------------------------------------------------------------
        right.setBorder(PanelBorderUtils.createTitledBorder(EXPERIMENT_HISTORY_TITLE));
        top.add(left,
                new GridBagConstraints(0, 0, 1, 1, 0, 0,
                        GridBagConstraints.CENTER, GridBagConstraints.VERTICAL, new Insets(0, 0, 0, 0), 0, 0));
        top.add(leftBottom,
                new GridBagConstraints(0, 1, 1, 1, 0, 1,
                        GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
        top.add(right,
                new GridBagConstraints(1, 0, 1, 2, 1, 1,
                        GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
        //----------------------------------------------
        this.add(top,
                new GridBagConstraints(0, 0, 1, 1, 1, 1,
                        GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(10, 0, 10, 0), 0, 0));
        this.add(bottom,
                new GridBagConstraints(0, 1, 1, 1, 1, 1,
                        GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 10, 0), 0, 0));
        this.add(progress,
                new GridBagConstraints(0, 2, 1, 1, 1, 0,
                        GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 10, 0), 0, 0));
        this.getRootPane().setDefaultButton(startButton);
    }

    private class TimeWorker extends SwingWorker<Void, Void> {

        Date startTime;

        TimeWorker() {
            startTime = new Date();
        }

        @Override
        protected Void doInBackground() {
            while (!worker.isDone()) {
                try {
                    Thread.sleep(TIMER_DELAY_IN_MILLIS);
                    setCurrentTime();
                } catch (InterruptedException ex) {
                    LoggerUtils.error(log, ex);
                }
            }
            return null;
        }

        void setCurrentTime() {
            long currentTimeMillis = new Date().getTime() - startTime.getTime();
            timerField.setText(DATE_FORMAT.format(new Date(currentTimeMillis)));
        }
    }

    /**
     * Experiment worker.
     */
    private class ExperimentWorker extends SwingWorker<Void, Void> {

        IterativeExperiment object;
        boolean error;

        ExperimentWorker() {
            object = getExperiment().getIterativeExperiment();
            this.addPropertyChangeListener(new PropertyChangeListener() {
                @Override
                public void propertyChange(PropertyChangeEvent evt) {
                    if ("progress".equals(evt.getPropertyName())) {
                        progress.setValue((Integer) evt.getNewValue());
                    }
                }
            });

        }

        @Override
        protected Void doInBackground() {
            try {
                while (!isCancelled() && object.hasNext()) {

                    try {
                        EvaluationResults classifier = object.next();
                        if (!isCancelled()) {
                            table.addExperiment(classifier);
                        }
                    } catch (Exception e) {
                        LoggerUtils.error(log, e);
                    }

                    if (!isCancelled()) {
                        int percent = object.getPercent();
                        if (percent != getProgress() && percent % 10 == 0) {
                            log.info("Experiment {} progress: {} %.", experimentId, percent);
                        }
                        setProgress(percent);
                    }
                }
            } catch (Throwable e) {
                LoggerUtils.error(log, e);
                error = true;
                text.setText(e.toString());
            }
            return null;
        }

        @Override
        protected void done() {
            setProgress(100);
            table.setRenderer(Color.RED);
            setStateForButtons(true);
            setStateForOptions(true);
            table.sort();
            if (!error) {
                setResults();
                log.info("Experiment {} has been successfully finished for classifier '{}'.", experimentId,
                        experiment.getClassifier().getClass().getSimpleName());
            }
        }

    }

    /**
     *
     */
    private static class ExperimentLoader implements CallbackAction {

        ExperimentHistory experiment;
        final File file;

        ExperimentLoader(File file) {
            this.file = file;
        }

        ExperimentHistory getExperiment() {
            return experiment;
        }

        @Override
        public void apply() throws Exception {
            experiment = (ExperimentHistory) ModelConverter.loadModel(file);
        }
    }

}
