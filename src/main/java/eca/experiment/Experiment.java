/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eca.experiment;

import eca.beans.ClassifierDescriptor;
import java.util.ArrayList;
import weka.core.Instances;
import weka.classifiers.Classifier;

/**
 * Basic interface for automatic selection of optimal options
 * for classifiers based on experiment series.
 * @author Roman93
 * @param <T> classifier type
 */
public interface Experiment<T extends Classifier> {

    /**
     * Begins experiment.
     * @throws Exception
     */
    void beginExperiment() throws Exception;

    /**
     * Return experiment history.
     * @return experiment history
     */
    ArrayList<ClassifierDescriptor> getHistory();

    /**
     * Clears experiment history.
     */
    void clearHistory();

    /**
     * Returns iterations number of experiment.
     * @return iterations number of experiment
     */
    int getNumIterations();

    /**
     * Sets iterations number of experiment.
     * @param n iterations number of experiment
     */
    void setNumIterations(int n);

    /**
     * Returns training set.
     * @return <tt>Instances</tt> object.
     */
    Instances data();

    /**
     * Classifier algorithm.
     * @return classifier algorithm
     */
    T getClassifier();

}
