package org.freddy33.qsm.vs.selector.sequential;

import org.freddy33.qsm.vs.base.SimpleState;
import org.freddy33.qsm.vs.selector.common.SpawnedEventState;

import java.util.EnumSet;

/**
 * @author freds on 12/25/14.
 */

public class SpawnedEventStateSequential implements SpawnedEventState {
    final SimpleState from;
    final EnumSet<SimpleState> states;
    final int counter;

    SpawnedEventStateSequential(SimpleState from, int counter, SimpleState... ns) {
        this.from = from;
        this.counter = counter;
        this.states = EnumSet.of(ns[0], ns);
    }

    @Override
    public synchronized void add(SpawnedEventState newStates) {
        this.states.addAll(((SpawnedEventStateSequential) newStates).states);
    }

    @Override
    public SimpleState getSimpleState() {
        return from;
    }

    @Override
    public EnumSet<SimpleState> getStates() {
        return states;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SpawnedEventStateSequential that = (SpawnedEventStateSequential) o;

        if (from != that.from) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return from.hashCode();
    }
}
