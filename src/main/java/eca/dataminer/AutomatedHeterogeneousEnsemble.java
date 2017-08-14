/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eca.dataminer;

import eca.model.ClassifierDescriptor;
import eca.core.PermutationsSearch;
import eca.ensemble.AbstractHeterogeneousClassifier;
import eca.ensemble.ClassifiersSet;
import eca.ensemble.HeterogeneousClassifier;
import eca.ensemble.Sampler;
import weka.classifiers.AbstractClassifier;
import weka.core.Instances;

import java.util.NoSuchElementException;

/**
 * Implements automatic selection of optimal options
 * for heterogeneous ensemble algorithms based on experiment series.
 *
 * @author Roman93
 */
public class AutomatedHeterogeneousEnsemble extends AbstractExperiment<AbstractHeterogeneousClassifier> {

    /**
     * Available sampling methods
     **/
    private static final int[] SAMPLE_METHOD = {
            Sampler.BAGGING,
            Sampler.INITIAL,
            Sampler.RANDOM,
            Sampler.RANDOM_BAGGING
    };

    /**
     * Available voting methods
     **/
    private static final boolean[] VOTING_METHOD = {true, false};

    /**
     * Available classifier selection methods
     **/
    private static final boolean[] CLASSIFIER_SELECTION_METHOD = {true, false};

    /**
     * Creates <tt>AutomatedHeterogeneousEnsemble</tt> object with given options.
     *
     * @param classifier heterogeneous classifier algorithm
     * @param data       training set
     */
    public AutomatedHeterogeneousEnsemble(AbstractHeterogeneousClassifier classifier, Instances data) {
        super(data, classifier);
    }

    @Override
    public IterativeExperiment getIterativeExperiment() {
        return new AutomatedHeterogeneousBuilder();
    }

    /**
     *
     */
    private class AutomatedHeterogeneousBuilder implements IterativeExperiment {

        int i, s, a = -1, index, state, it;
        ClassifiersSet set = classifier.getClassifiersSet();
        ClassifiersSet currentSet = new ClassifiersSet();
        int[] marks = new int[classifier.getClassifiersSet().size()];
        PermutationsSearch permutationsSearch = new PermutationsSearch();
        int numCombinations = getNumCombinations();

        AutomatedHeterogeneousBuilder() {
            clearHistory();
        }

        @Override
        public ClassifierDescriptor next() throws Exception {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }

            ClassifierDescriptor object = null;

            while (object == null) {
                object = nextState();
            }
            return object;
        }

        @Override
        public boolean hasNext() {
            return index < numCombinations;
        }

        @Override
        public int getPercent() {
            return index * 100 / numCombinations;
        }

        ClassifierDescriptor nextState() throws Exception {

            switch (state) {

                case 0: {
                    for (int j = 0; j < marks.length; j++) {
                        marks[j] = j <= it ? 1 : 0;
                    }
                    permutationsSearch.setValues(marks);
                    state = 1;
                    break;
                }

                case 1: {
                    if (permutationsSearch.nextPermutation()) {

                        currentSet.clear();
                        for (int k = 0; k < marks.length; k++) {
                            if (marks[k] == 1) {
                                currentSet.addClassifier(set.getClassifier(k));
                            }
                        }

                        state = 2;
                    } else {
                        it++;
                        state = 0;
                    }

                    break;
                }

                case 2: {
                    if (classifier instanceof HeterogeneousClassifier) {
                        for (; s < SAMPLE_METHOD.length; s++) {
                            for (; i < CLASSIFIER_SELECTION_METHOD.length; i++) {
                                for (++a; a < VOTING_METHOD.length; ) {
                                    HeterogeneousClassifier m_Model =
                                            (HeterogeneousClassifier) AbstractClassifier.makeCopy(classifier);
                                    m_Model.sampler().setSampling(SAMPLE_METHOD[s]);
                                    m_Model.setUseRandomClassifier(CLASSIFIER_SELECTION_METHOD[i]);
                                    m_Model.setUseWeightedVotesMethod(VOTING_METHOD[a]);
                                    m_Model.setClassifiersSet(currentSet.clone());
                                    index++;
                                    return evaluateModel(m_Model);
                                }
                                a = -1;
                            }
                            i = 0;
                        }

                        s = 0;

                        state = 1;
                    } else {
                        AbstractHeterogeneousClassifier model
                                = (AbstractHeterogeneousClassifier) AbstractClassifier.makeCopy(classifier);
                        model.setClassifiersSet(currentSet.clone());
                        index++;
                        ClassifierDescriptor classifierDescriptor = evaluateModel(model);
                        state = 1;
                        return classifierDescriptor;
                    }

                    break;
                }

            }

            return null;
        }

        int getNumCombinations() {
            return getAdditionalParamCombinationsNum() * ExperimentUtil.getNumClassifiersCombinations(set.size());
        }

        int getAdditionalParamCombinationsNum() {
            if (classifier instanceof HeterogeneousClassifier) {
                return SAMPLE_METHOD.length * CLASSIFIER_SELECTION_METHOD.length * VOTING_METHOD.length;
            } else {
                return 1;
            }
        }
    }

}
