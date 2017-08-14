package eca.dataminer;

import eca.core.Utils;

/**
 * Data miner utils class.
 *
 * @author Roman Batygin
 */
public class ExperimentUtil {

    /**
     * Returns the number of individual models combination.
     * The number of combinations is <code>P = sum[t = 1..r]r!/(t!(r-t)!)</code>
     *
     * @param r the number of individual models combination
     * @return
     */
    public static int getNumClassifiersCombinations(int r) {
        int fact = Utils.fact(r);
        int p = 0;
        for (int t = 1; t <= r; t++) {
            p += fact / (Utils.fact(t) * Utils.fact(r - t));
        }
        return p;
    }
}
