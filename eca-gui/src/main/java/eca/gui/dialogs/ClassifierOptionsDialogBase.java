/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eca.gui.dialogs;

import weka.classifiers.Classifier;
import weka.core.Instances;

import javax.swing.*;
import java.awt.*;

/**
 * Implements classifier input options dialog.
 *
 * @param <T> - classifier generic type
 * @author Roman Batygin
 */
public abstract class ClassifierOptionsDialogBase<T extends Classifier> extends JDialog {

    public static final int INT_FIELD_LENGTH = 8;
    public static final int TEXT_FIELD_LENGTH = 8;
    public static final String INPUT_ERROR_MESSAGE = "Ошибка ввода";

    private T classifier;
    private Instances data;
    protected boolean dialogResult;

    public ClassifierOptionsDialogBase(Window parent, String title, T classifier, Instances data) {
        super(parent, title);
        this.classifier = classifier;
        this.data = data;
        this.setModal(true);
    }

    public boolean dialogResult() {
        return dialogResult;
    }

    public void showDialog() {
        this.setVisible(true);
    }

    public T classifier() {
        return classifier;
    }

    public Instances data() {
        return data;
    }
}
