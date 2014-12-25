package org.freddy33.qsm.vs;

import java.util.Collections;
import java.util.EnumSet;

/**
 * @author freds on 12/8/14.
 */
public class SpawnedEvent {
    final Point p;
    final int length;
    final SimpleState from;
    final EnumSet<SimpleState> states;

    public SpawnedEvent(Point p, int length, SimpleState from, SimpleState... s) {
        this.p = p;
        this.length = length;
        this.from = from;
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
        return "SpawnedEvent{" + p +
                ", l=" + length +
                ", from=" + from.name() +
                ", states=" + states +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SpawnedEvent that = (SpawnedEvent) o;

        if (length != that.length) return false;
        if (from != that.from) return false;
        if (!p.equals(that.p)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = p.hashCode();
        result = 31 * result + length;
        result = 31 * result + from.hashCode();
        return result;
    }
}
