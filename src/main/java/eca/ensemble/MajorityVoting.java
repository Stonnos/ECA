/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eca.ensemble;

import weka.core.Instance;

/**
 * Implements majority voting method.
 *
 * @author Рома
 */
public class MajorityVoting extends VotingMethod {

    /**
     * Creates <tt>MajorityVoting</tt> object.
     *
     * @param aggregator <tt>Aggregator</tt>
     */
    public MajorityVoting(Aggregator aggregator) {
        super(aggregator);
    }

    @Override
    public double classifyInstance(Instance obj) throws Exception {
        return aggregator().aggregate(obj);
    }

    @Override
    public double[] distributionForInstance(Instance obj) throws Exception {
        return aggregator().distributionForInstance(obj);
    }

}
