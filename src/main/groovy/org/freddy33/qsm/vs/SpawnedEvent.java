package org.freddy33.qsm.vs;

import java.util.Collections;
import java.util.EnumSet;

/**
 * @author freds on 12/8/14.
 */
public class SpawnedEvent {
    final SourceEvent origin;
    final Point p;
    final int time;
    final int counter;
    final EnumSet<SimpleState> states;

    public SpawnedEvent(SourceEvent origin, Point p, int time, int counter, SimpleState... s) {
        this.origin = origin;
        this.p = p;
        this.time = time;
        this.counter = counter;
        if (s.length == 1) {
            this.states = EnumSet.of(s[0]);
        } else {
            this.states = EnumSet.of(s[0], s);
        }
    }

    void add(SimpleState[] newStates) {
        synchronized (this.states) {
            Collections.addAll(this.states, newStates);
        }
    }

    @Override
    public String toString() {
        return "SpawnedEvent{" +
                "origin=" + origin +
                ", p=" + p +
                ", length=" + time +
                ", states=" + states +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SpawnedEvent that = (SpawnedEvent) o;

        if (time != that.time) return false;
        if (!origin.equals(that.origin)) return false;
        if (!p.equals(that.p)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = origin.hashCode();
        result = 31 * result + p.hashCode();
        result = 31 * result + time;
        return result;
    }
}
