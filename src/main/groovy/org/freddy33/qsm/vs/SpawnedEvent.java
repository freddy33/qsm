package org.freddy33.qsm.vs;

import java.util.Collections;
import java.util.EnumSet;

/**
 * @author freds on 12/8/14.
 */
public class SpawnedEvent {
    final Point p;
    final int length;
    final int counter;
    final EnumSet<SimpleState> states;

    public SpawnedEvent(Point p, int length, int counter, SimpleState... s) {
        this.p = p;
        this.length = length;
        this.counter = counter;
        if (s.length == 1) {
            this.states = EnumSet.of(s[0]);
        } else {
            this.states = EnumSet.of(s[0], s);
        }
    }

    void add(SimpleState... newStates) {
        synchronized (this.states) {
            Collections.addAll(this.states, newStates);
        }
    }

    @Override
    public String toString() {
        return "SpawnedEvent{" +
                ", p=" + p +
                ", length=" + length +
                ", states=" + states +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SpawnedEvent that = (SpawnedEvent) o;

        if (length != that.length) return false;
        if (!p.equals(that.p)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = p.hashCode();
        result = 31 * result + length;
        return result;
    }
}
