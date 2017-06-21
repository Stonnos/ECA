/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eca.gui.frames;

import javax.swing.JFrame;
import eca.experiment.AbstractExperiment;
import eca.experiment.AutomatedStacking;
import eca.gui.dialogs.StackingOptionsDialog;

import javax.swing.JOptionPane;

/**
 *
 * @author Roman93
 */
public class AutomatedStackingFrame extends ExperimentFrame {

    public AutomatedStackingFrame(String title, AbstractExperiment experiment,
            JFrame parent, int digits) throws Exception {
        super(experiment, parent, digits);
        this.setTitle(title);
    }

    @Override
    protected void setOptions() {
        AutomatedStacking exp = (AutomatedStacking) this.getExperiment();
        StackingOptionsDialog options
                = new StackingOptionsDialog(this, "Настройка параметров",
                        exp.getClassifier(), exp.data());
        options.setMetaEnabled(false);
        try {
            options.addClassifiers(exp.getClassifier().getClassifiers().clone());
            options.showDialog();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    e.getMessage(),
                    "", JOptionPane.WARNING_MESSAGE);
        }
    }

}
