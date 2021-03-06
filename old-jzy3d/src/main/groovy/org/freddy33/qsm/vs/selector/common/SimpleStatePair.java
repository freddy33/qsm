package org.freddy33.qsm.vs.selector.common;

import org.freddy33.qsm.vs.base.SimpleState;

/**
 * Created by freds on 1/4/15.
 */
public class SimpleStatePair {
    public final SimpleState parent;
    public final SimpleState current;

    public SimpleStatePair(SimpleState parent, SimpleState current) {
        this.parent = parent;
        this.current = current;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SimpleStatePair that = (SimpleStatePair) o;

        if (current != that.current) return false;
        if (parent != that.parent) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = parent.hashCode();
        result = 31 * result + current.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "SimpleStatePair{ " + parent + ", " + current + '}';
    }
}
