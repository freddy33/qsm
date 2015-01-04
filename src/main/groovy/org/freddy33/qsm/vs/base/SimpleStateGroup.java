package org.freddy33.qsm.vs.base;

/**
 * @author freds on 12/6/14.
 */
public enum SimpleStateGroup {
    ZERO(1), ONE(3), TWO(4), THREE(5);

    public final int delta;

    SimpleStateGroup(int delta) {
        this.delta = delta;
    }
}
