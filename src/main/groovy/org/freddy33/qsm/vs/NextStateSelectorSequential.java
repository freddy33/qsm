package org.freddy33.qsm.vs;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

/**
 * @author freds on 12/25/14.
 */

class SpawnedEventStateSequential implements SpawnedEventState {
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

public class NextStateSelectorSequential extends BaseNextStateSelector {
    // Non random sequence
    static TransitionMode[] sequence = new TransitionMode[]{
            TransitionMode.transitionFromIncoming,
            TransitionMode.transitionFromIncoming,
            TransitionMode.transitionFromOriginal
    };

    public NextStateSelectorSequential(SimpleState original) {
        super(original);
    }

    @Override
    public List<SpawnedEventState> nextSpawnedEvent(SpawnedEvent se) {
        SpawnedEventStateSequential stateHolder = (SpawnedEventStateSequential) se.stateHolder;
        List<SpawnedEventState> res = new ArrayList<>(stateHolder.states.size());
        int i = 0;
        for (SimpleState state : stateHolder.states) {
            res.add(new SpawnedEventStateSequential(state,
                    stateHolder.counter + i,
                    nextStates(stateHolder.counter, state)));
            i++;
        }
        return res;
    }

    @Override
    public SpawnedEventState createOriginalState(StateTransition transition) {
        return new SpawnedEventStateSequential(transition.from, 0, transition.next);
    }

    public SimpleState[] nextStates(int counter, SimpleState s) {
        int left;
        int transitionSelect;
        if (sequence.length > 1) {
            left = counter % sequence.length;
            transitionSelect = (counter - left) / sequence.length;
        } else {
            left = 0;
            transitionSelect = counter;
        }
        TransitionMode transitionMode = sequence[left];
        return getNextSimpleStates(s, transitionMode, transitionSelect);
    }
}
