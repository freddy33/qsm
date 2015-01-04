package org.freddy33.qsm.vs.selector;

import org.freddy33.qsm.vs.base.SimpleState;

/**
 * Created by freds on 1/4/15.
 */
public class NextTransitionKey {
    final SpawnedEventStateIncoming se;
    final SimpleState s;

    NextTransitionKey(SpawnedEventStateIncoming se, SimpleState s) {
        this.se = se;
        this.s = s;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        NextTransitionKey that = (NextTransitionKey) o;

        if (s != that.s) return false;
        if (!se.equals(that.se)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = se.hashCode();
        result = 31 * result + s.hashCode();
        return result;
    }
}
