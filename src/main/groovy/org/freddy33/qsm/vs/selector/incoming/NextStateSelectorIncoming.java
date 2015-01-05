package org.freddy33.qsm.vs.selector.incoming;

import org.freddy33.qsm.vs.base.SimpleState;
import org.freddy33.qsm.vs.base.StateTransition;
import org.freddy33.qsm.vs.event.SpawnedEvent;
import org.freddy33.qsm.vs.selector.common.BaseNextStateSelector;
import org.freddy33.qsm.vs.selector.common.SpawnedEventState;

import java.util.ArrayList;
import java.util.List;

public abstract class NextStateSelectorIncoming extends BaseNextStateSelector {
    public NextStateSelectorIncoming(SimpleState original) {
        super(original);
    }

    public static void verifyAllBaseNextSelector() {
        verifyPossibleNextStates();
        verifyTransitionsPerPair();
        verifyTransitionsPerTriple();
    }

    public abstract StateTransition findTransition(SpawnedEventStateIncoming se, SimpleState s);

    @Override
    public List<SpawnedEventState> nextSpawnedEvent(SpawnedEvent se) {
        SpawnedEventStateIncoming stateIncoming = (SpawnedEventStateIncoming) se.stateHolder;
        List<SpawnedEventState> res = new ArrayList<>(3);
        for (SimpleState state : stateIncoming.transition.next) {
            if (!possiblesNextStates.get(getOriginal().getOpposite()).contains(state)) {
                res.add(new SpawnedEventStateIncoming(
                        findTransition(stateIncoming, state), stateIncoming.transition));
            }
        }
        return res;
    }

    @Override
    public SpawnedEventState createOriginalState(StateTransition transition, StateTransition previousState) {
        return new SpawnedEventStateIncoming(transition, previousState);
    }
}

