package org.freddy33.qsm.vs;

import java.util.Collections;
import java.util.EnumSet;

/**
 * @author freds on 12/8/14.
 */
public class SpawnedEvent {
    final SourceEvent origin;
    final Point p;
    final int length;
    final EnumSet<SimpleState> states;

    public SpawnedEvent(SourceEvent origin, Point p, int length) {
        this.origin = origin;
        this.p = p;
        this.length = length;
        this.states = EnumSet.noneOf(SimpleState.class);
    }

    public SpawnedEvent(SourceEvent origin, Point p, int length, SimpleState... s) {
        this.origin = origin;
        this.p = p;
        this.length = length;
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
                ", length=" + length +
                ", states=" + states +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SpawnedEvent that = (SpawnedEvent) o;

        if (!origin.equals(that.origin)) return false;
        if (!p.equals(that.p)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = origin.hashCode();
        result = 31 * result + p.hashCode();
        return result;
    }
}