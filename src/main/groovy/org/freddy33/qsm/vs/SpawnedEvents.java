package org.freddy33.qsm.vs;

import java.util.EnumSet;

/**
 * @author freds on 12/8/14.
 */
public class SpawnedEvents {
    final SourceEvent origin;
    final Point p;
    final int length;
    final EnumSet<SimpleState> states;

    public SpawnedEvents(SourceEvent origin, Point p, int length) {
        this.origin = origin;
        this.p = p;
        this.length = length;
        this.states = EnumSet.noneOf(SimpleState.class);
    }

    void add(SimpleState s) {
        synchronized (states) {
            states.add(s);
        }
    }

    @Override
    public String toString() {
        return "SpawnedEvents{" +
                "origin=" + origin +
                ", p=" + p +
                ", length=" + length +
                ", states=" + states +
                '}';
    }
}
