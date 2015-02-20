package org.freddy33.qsm.vs.selector.sequential;

import org.freddy33.qsm.vs.base.SimpleState;
import org.freddy33.qsm.vs.base.StateTransition;
import org.freddy33.qsm.vs.event.BaseSpawnedEvent;
import org.freddy33.qsm.vs.selector.common.BaseNextStateSelector;
import org.freddy33.qsm.vs.selector.common.SimpleStatePair;
import org.freddy33.qsm.vs.selector.common.SpawnedEventState;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;

public class NextStatePairSelectorSequential extends BaseNextStateSelector {

    public NextStatePairSelectorSequential(SimpleState original) {
        super(original);
    }

    @Override
    public List<SpawnedEventState> nextSpawnedEvent(BaseSpawnedEvent se) {
        SpawnedEventStatePairSequential stateHolder = (SpawnedEventStatePairSequential) se.stateHolder;
        List<SpawnedEventState> res = new ArrayList<>(stateHolder.states.size());
        boolean takeFirst = stateHolder.takeFirst;
        for (SimpleState state : stateHolder.states) {
            if (!possiblesNextStates.get(getOriginal().getOpposite()).contains(state)) {
                SimpleStatePair pair = new SimpleStatePair(stateHolder.getSimpleState(), state);
                EnumSet<StateTransition> stateTransitions = transitionsPerPair.get(pair);
                if (stateTransitions == null || stateTransitions.isEmpty()) {
                    throw new IllegalStateException("Did not find transitions for " + pair);
                }
                Iterator<StateTransition> transitionIterator = stateTransitions.iterator();
                StateTransition found = transitionIterator.next();
                if (stateTransitions.size() > 1 && !takeFirst) {
                    found = transitionIterator.next();
                }
                takeFirst = !takeFirst;
                res.add(new SpawnedEventStatePairSequential(pair, takeFirst, found.next));
            }
        }
        return res;
    }

    @Override
    public SpawnedEventState createOriginalState(StateTransition transition, StateTransition previousState) {
        return new SpawnedEventStatePairSequential(previousState.from, transition.from, true, transition.next);
    }
}
