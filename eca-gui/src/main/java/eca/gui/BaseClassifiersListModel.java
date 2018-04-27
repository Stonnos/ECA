package eca.gui;

import eca.dictionary.ClassifiersNamesDictionary;
import eca.gui.dialogs.ClassifierOptionsDialogBase;
import eca.gui.dialogs.DecisionTreeOptionsDialog;
import eca.gui.dialogs.J48OptionsDialog;
import eca.gui.dialogs.KNNOptionDialog;
import eca.gui.dialogs.LogisticOptionsDialogBase;
import eca.gui.dialogs.NetworkOptionsDialog;
import eca.metrics.KNearestNeighbours;
import eca.neural.NeuralNetwork;
import eca.regression.Logistic;
import eca.trees.C45;
import eca.trees.CART;
import eca.trees.CHAID;
import eca.trees.ID3;
import eca.trees.J48;
import weka.classifiers.Classifier;
import weka.core.Instances;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Roman Batygin
 */

public class BaseClassifiersListModel extends DefaultListModel<String> {

    private ArrayList<ClassifierOptionsDialogBase> frames = new ArrayList<>();

    private Instances data;

    private Window parent;

    private int digits;

    public BaseClassifiersListModel(Instances data, Window parent, int digits) {
        this.data = data;
        this.parent = parent;
        this.digits = digits;
    }

    public List<ClassifierOptionsDialogBase> getFrames() {
        return frames;
    }

    public ClassifierOptionsDialogBase getWindow(int i) {
        return frames.get(i);
    }

    @Override
    public void clear() {
        frames.clear();
        super.clear();
    }

    public boolean isEmpty() {
        return frames.isEmpty();
    }

    public void addClassifier(Classifier classifier) {
        String name = null;
        if (classifier instanceof C45) {
            name = ClassifiersNamesDictionary.C45;
            frames.add(new DecisionTreeOptionsDialog(parent,
                    name, (C45) classifier, data));
        } else if (classifier instanceof ID3) {
            name = ClassifiersNamesDictionary.ID3;
            frames.add(new DecisionTreeOptionsDialog(parent,
                    name, (ID3) classifier, data));
        } else if (classifier instanceof CART) {
            name = ClassifiersNamesDictionary.CART;
            frames.add(new DecisionTreeOptionsDialog(parent,
                    name, (CART) classifier, data));
        } else if (classifier instanceof CHAID) {
            name = ClassifiersNamesDictionary.CHAID;
            frames.add(new DecisionTreeOptionsDialog(parent,
                    name, (CHAID) classifier, data));
        } else if (classifier instanceof Logistic) {
            name = ClassifiersNamesDictionary.LOGISTIC;
            frames.add(new LogisticOptionsDialogBase(parent,
                    name, (Logistic) classifier, data));
        } else if (classifier instanceof NeuralNetwork) {
            name = ClassifiersNamesDictionary.NEURAL_NETWORK;
            frames.add(new NetworkOptionsDialog(parent,
                    name, (NeuralNetwork) classifier, data));
        } else if (classifier instanceof KNearestNeighbours) {
            name = ClassifiersNamesDictionary.KNN;
            frames.add(new KNNOptionDialog(parent,
                    name, (KNearestNeighbours) classifier, data));
        } else if (classifier instanceof J48) {
            name = ClassifiersNamesDictionary.J48;
            frames.add(new J48OptionsDialog(parent,
                    name, (J48) classifier, data));
        }
        super.addElement(name);
    }

    @Override
    public void addElement(String classifier) {
        switch (classifier) {
            case ClassifiersNamesDictionary.ID3:
                frames.add(new DecisionTreeOptionsDialog(parent,
                        ClassifiersNamesDictionary.ID3, new ID3(), data));
                break;

            case ClassifiersNamesDictionary.C45:
                frames.add(new DecisionTreeOptionsDialog(parent,
                        ClassifiersNamesDictionary.C45, new C45(), data));
                break;

            case ClassifiersNamesDictionary.CART:
                frames.add(new DecisionTreeOptionsDialog(parent,
                        ClassifiersNamesDictionary.CART, new CART(), data));
                break;

            case ClassifiersNamesDictionary.CHAID:
                frames.add(new DecisionTreeOptionsDialog(parent,
                        ClassifiersNamesDictionary.CHAID, new CHAID(), data));
                break;

            case ClassifiersNamesDictionary.NEURAL_NETWORK:
                NeuralNetwork neuralNetwork = new NeuralNetwork(data);
                neuralNetwork.getDecimalFormat().setMaximumFractionDigits(digits);
                frames.add(new NetworkOptionsDialog(parent,
                        ClassifiersNamesDictionary.NEURAL_NETWORK, neuralNetwork, data));
                break;

            case ClassifiersNamesDictionary.LOGISTIC:
                frames.add(new LogisticOptionsDialogBase(parent,
                        ClassifiersNamesDictionary.LOGISTIC, new Logistic(), data));
                break;

            case ClassifiersNamesDictionary.KNN:
                KNearestNeighbours kNearestNeighbours = new KNearestNeighbours();
                kNearestNeighbours.getDecimalFormat().setMaximumFractionDigits(digits);
                frames.add(new KNNOptionDialog(parent,
                        ClassifiersNamesDictionary.KNN, kNearestNeighbours, data));
                break;
            case ClassifiersNamesDictionary.J48:
                frames.add(new J48OptionsDialog(parent,
                        ClassifiersNamesDictionary.J48, new J48(), data));
                break;

        }
        super.addElement(classifier);
    }

    @Override
    public String remove(int i) {
        ClassifierOptionsDialogBase frame = frames.remove(i);
        frame.dispose();
        return super.remove(i);
    }

}
