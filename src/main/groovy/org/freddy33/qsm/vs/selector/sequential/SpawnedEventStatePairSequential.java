package org.freddy33.qsm.vs.selector.sequential;

import org.freddy33.qsm.vs.base.SimpleState;
import org.freddy33.qsm.vs.selector.common.SimpleStatePair;
import org.freddy33.qsm.vs.selector.common.SpawnedEventState;

import java.util.EnumSet;

/**
 * @author freds on 12/25/14.
 */

public class SpawnedEventStatePairSequential implements SpawnedEventState {
    final SimpleStatePair pair;
    final EnumSet<SimpleState> states;
    final boolean takeFirst;

    SpawnedEventStatePairSequential(SimpleState parentFrom, SimpleState from, boolean takeFirst, SimpleState... ns) {
        this(new SimpleStatePair(parentFrom, from), takeFirst, ns);
    }

    SpawnedEventStatePairSequential(SimpleStatePair pair, boolean takeFirst, SimpleState... ns) {
        this.pair = pair;
        this.takeFirst = takeFirst;
        this.states = EnumSet.of(ns[0], ns);
    }

    @Override
    public synchronized void add(SpawnedEventState newStates) {
        this.states.addAll(((SpawnedEventStatePairSequential) newStates).states);
    }

    @Override
    public SimpleState getSimpleState() {
        return pair.current;
    }

    @Override
    public EnumSet<SimpleState> getStates() {
        return states;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SpawnedEventStatePairSequential that = (SpawnedEventStatePairSequential) o;

        if (!pair.equals(that.pair)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return pair.hashCode();
    }
}
