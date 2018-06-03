package org.freddy33.qsm.vs.selector.common;

import org.freddy33.qsm.vs.base.SimpleState;
import org.freddy33.qsm.vs.base.SimpleStateGroup;

/**
 * Created by freds on 1/4/15.
 */
public class SimpleStateTriple {
    public final SimpleState grandParent;
    public final SimpleState parent;
    public final SimpleState current;

    public SimpleStateTriple(SimpleState grandParent, SimpleState parent, SimpleState current) {
        this.grandParent = grandParent;
        this.parent = parent;
        this.current = current;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SimpleStateTriple that = (SimpleStateTriple) o;

        if (current != that.current) return false;
        if (grandParent != that.grandParent) return false;
        if (parent != that.parent) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = grandParent.hashCode();
        result = 31 * result + parent.hashCode();
        result = 31 * result + current.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "SimpleStateTriple{ " + grandParent + ", " + parent + ", " + current + '}';
    }

    public boolean isUndecidable() {
        // If current group ONE and parent group TWO => no solution
        return current == grandParent && current.stateGroup == SimpleStateGroup.ONE && parent.stateGroup == SimpleStateGroup.TWO;
    }
}
