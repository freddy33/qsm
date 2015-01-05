package org.freddy33.qsm.vs.selector.random;

import org.freddy33.qsm.vs.base.SimpleState;
import org.freddy33.qsm.vs.base.StateTransition;
import org.freddy33.qsm.vs.event.SpawnedEvent;
import org.freddy33.qsm.vs.selector.common.BaseNextStateSelector;
import org.freddy33.qsm.vs.selector.common.SpawnedEventState;
import org.freddy33.qsm.vs.selector.common.TransitionMode;

import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;


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
        return stateHolder.states.stream().filter(s -> !possiblesNextStates.get(getOriginal().getOpposite()).contains(s))
                .map(state -> new SpawnedEventStateRandom(state, nextStates(state)))
                .collect(Collectors.toList());
    }

    @Override
    public SpawnedEventState createOriginalState(StateTransition transition, StateTransition previousState) {
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
