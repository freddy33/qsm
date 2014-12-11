package org.freddy33.qsm.vs;

/**
 * @author freds on 12/11/14.
 */
public class TransitionRatio {
    /**
     * Ratio for using the simple state of the origin
     */
    final int origin;
    /**
     * Ratio for using the State Transition splitting in 3
     */
    final int split;
    /**
     * Ratio for using the same simple state
     */
    final int same;

    public TransitionRatio(int origin, int split, int same) {
        this.origin = origin;
        this.split = split;
        this.same = same;
    }

    int total() {
        return origin + split + same;
    }
}
