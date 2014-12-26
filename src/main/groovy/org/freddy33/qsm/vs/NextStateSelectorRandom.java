package org.freddy33.qsm.vs;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Random;

/**
 * @author freds on 12/25/14.
 */
class SpawnedEventStateRandom implements SpawnedEventState {
    final SimpleState from;
    final EnumSet<SimpleState> states;

    SpawnedEventStateRandom(SimpleState from, SimpleState... ns) {
        this.from = from;
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


public class NextStateSelectorRandom extends BaseNextStateSelector {
    // Random ratios
    static TransitionRatio transitionRatio = new TransitionRatio(0, 1, 3, 0);

    final Random random;

    public NextStateSelectorRandom(SimpleState original, int seed) {
        super(original);
        this.random = new Random(seed);
    }

    @Override
    public List<SpawnedEventState> nextSpawnedEvent(SpawnedEvent se) {
        SpawnedEventStateRandom stateHolder = (SpawnedEventStateRandom) se.stateHolder;
        List<SpawnedEventState> res = new ArrayList<>(stateHolder.states.size());
        for (SimpleState state : stateHolder.states) {
            res.add(new SpawnedEventStateRandom(state, nextStates(state)));
        }
        return res;
    }

    @Override
    public SpawnedEventState createOriginalState(StateTransition transition) {
        return new SpawnedEventStateRandom(transition.from, transition.next);
    }

    public SimpleState[] nextStates(SimpleState s) {
        // Blocks in order of TransitionMode
        int modeSelect = random.nextInt(transitionRatio.total());
        TransitionMode mode = null;
        int i = 0;
        for (TransitionMode transitionMode : TransitionMode.values()) {
            int ratio = transitionRatio.mapRatio.get(transitionMode);
            if (modeSelect >= i && modeSelect < (i + ratio)) {
                mode = transitionMode;
                break;
            }
            i += ratio;
        }
        if (mode == null) {
            throw new IllegalStateException("Did not find a mode using " + modeSelect + " from " + transitionRatio.mapRatio);
        }
        return getNextSimpleStates(s, mode, random.nextInt(4));
    }

}
