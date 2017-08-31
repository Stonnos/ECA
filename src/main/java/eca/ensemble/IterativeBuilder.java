/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eca.ensemble;

import eca.core.evaluation.Evaluation;
import weka.classifiers.Classifier;
import weka.core.Instances;

/**
 * Abstract class for iterative building of classifier model.
 *
 * @author Рома
 */
public abstract class IterativeBuilder {

    /**
     * Current index
     **/
    protected int index;

    /**
     * Step between iterations
     **/
    protected int step = 1;

    /**
     * Performs the next iteration.
     *
     * @return the next iteration index
     * @throws Exception
     */
    public abstract int next() throws Exception;

    /**
     * Returns <tt>true</tt> if the next iteration is exist.
     *
     * @return <tt>true</tt> if the next iteration is exist
     */
    public abstract boolean hasNext();

    /**
     * Returns the number of iterations.
     *
     * @return the number of iterations
     */
    public abstract int numIterations();

    /**
     * Returns <tt>Evaluation</tt> object if the model ia
     * already build, null otherwise.
     *
     * @return <tt>Evaluation</tt> object
     * @throws Exception
     */
    public abstract Evaluation evaluation() throws Exception;

    /**
     * Returns the value of next iteration index.
     *
     * @return the value of next iteration index
     */
    public int index() {
        return index;
    }

    /**
     * Returns the value of step between iterations.
     *
     * @return the value of step between iterations
     */
    public int step() {
        return step;
    }

    /**
     * Returns the value of building percent.
     *
     * @return the value of building percent
     */
    public int getPercent() {
        return index() * 100 / numIterations();
    }

    protected Evaluation evaluateModel(Classifier classifier, Instances data) throws Exception {
        if (!hasNext()) {
            Evaluation evaluation = new Evaluation(data);
            evaluation.evaluateModel(classifier, data);
            return evaluation;
        } else {
            return null;
        }
    }

}
