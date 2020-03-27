package eca.gui;

import eca.config.ConfigurationService;
import eca.gui.frames.results.ClassificationResultsFrameBase;

import javax.swing.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

/**
 * Evaluation results history model.
 *
 * @author Roman Batygin
 */
public class EvaluationResultsHistoryModel extends DefaultListModel<String> {

    private static final String HISTORY_FORMAT = "%s %s";

    private final SimpleDateFormat simpleDateFormat = new SimpleDateFormat(
            ConfigurationService.getApplicationConfigService().getApplicationConfig().getDateFormat());

    private final Object lifecycleMonitor = new Object();

    private ArrayList<ClassificationResultsFrameBase> resultsFrameBases = new ArrayList<>();

    public void add(ClassificationResultsFrameBase resultsFrameBase) {
        synchronized (lifecycleMonitor) {
            resultsFrameBases.add(resultsFrameBase);
            addElement(String.format(HISTORY_FORMAT, simpleDateFormat.format(resultsFrameBase.getCreationDate()),
                    resultsFrameBase.classifier().getClass().getSimpleName()));
        }
    }

    public ClassificationResultsFrameBase getClassificationResultsFrame(int i) {
        synchronized (lifecycleMonitor) {
            return resultsFrameBases.get(i);
        }
    }

    public void removeItem(int i) {
        synchronized (lifecycleMonitor) {
            resultsFrameBases.remove(i);
            remove(i);
        }
    }

    public void removeAllItems() {
        synchronized (lifecycleMonitor) {
            resultsFrameBases.clear();
            clear();
        }
    }
}
