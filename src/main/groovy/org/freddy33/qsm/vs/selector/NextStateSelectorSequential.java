package org.freddy33.qsm.vs.selector;

import org.freddy33.qsm.vs.base.SimpleState;
import org.freddy33.qsm.vs.base.StateTransition;
import org.freddy33.qsm.vs.event.SpawnedEvent;

import java.util.ArrayList;
import java.util.List;

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
    public SpawnedEventState createOriginalState(StateTransition transition, StateTransition previousState) {
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
