package org.freddy33.qsm.vs.selector.incoming;

import org.freddy33.qsm.vs.base.SimpleState;
import org.freddy33.qsm.vs.base.StateTransition;
import org.freddy33.qsm.vs.event.SpawnedEvent;
import org.freddy33.qsm.vs.selector.common.BaseNextStateSelector;
import org.freddy33.qsm.vs.selector.common.SpawnedEventState;

import java.util.*;

public class NextStateSelectorIncoming extends BaseNextStateSelector {
    static final Map<SimpleStatePair, EnumSet<StateTransition>> transitionsPerPair = new HashMap<>();
    static final Map<SimpleState, EnumSet<SimpleState>> possiblesNextStates = new HashMap<>();
    static final Map<NextTransitionKey, StateTransition> nextTransitions = new HashMap<>();

    public NextStateSelectorIncoming(SimpleState original) {
        super(original);
    }

    public static void verifyAllNextSelector() {
        for (SimpleState current : SimpleState.values()) {
            List<StateTransition> stateTransitions = StateTransition.transitions.get(current);
            for (StateTransition transition : stateTransitions) {
                EnumSet<SimpleState> nextStates = EnumSet.of(transition.next[0], transition.next);
                EnumSet<SimpleState> existingNextStates = possiblesNextStates.putIfAbsent(current, nextStates);
                if (existingNextStates != null) {
                    existingNextStates.addAll(nextStates);
                }
            }
        }
        for (Map.Entry<SimpleState, EnumSet<SimpleState>> entry : possiblesNextStates.entrySet()) {
            switch (entry.getKey().stateGroup) {
                case ONE:
                case TWO:
                    if (entry.getValue().size() != 8) {
                        throw new IllegalStateException("Possible next state for "
                                + entry.getKey() + " should be 8 and its " + entry.getValue());
                    }
                    break;
                case THREE:
                    if (entry.getValue().size() != 6) {
                        throw new IllegalStateException("Possible next state for "
                                + entry.getKey() + " should be 6 and its " + entry.getValue());
                    }
                    break;
            }

        }
        for (StateTransition transition : StateTransition.values()) {
            for (SimpleState nextState : transition.next) {
                SimpleStatePair key = new SimpleStatePair(nextState, transition.from);
                EnumSet<StateTransition> transitions = transitionsPerPair.get(key);
                if (transitions != null) {
                    transitions.add(transition);
                    if (transitions.size() > 2) {
                        throw new IllegalStateException("There should be only 2 max possibles for a pair " + key
                                + " and got " + transitions);
                    }
                } else {
                    transitionsPerPair.put(key, EnumSet.of(transition));
                }
            }
        }
        for (StateTransition parentTransition : StateTransition.values()) {
            for (SimpleState s : parentTransition.next) {
                EnumSet<StateTransition> possibleTransitions =
                        transitionsPerPair.get(new SimpleStatePair(parentTransition.from, s));
                for (StateTransition possibleTransition : possibleTransitions) {
                    createAndCacheNextTransition(possibleTransition, parentTransition);
                }
            }
        }
    }

    private static void createAndCacheNextTransition(StateTransition transition, StateTransition parentTransition) {
        SpawnedEventStateIncoming se = new SpawnedEventStateIncoming(transition, parentTransition);
        for (SimpleState s : transition.next) {
            NextTransitionKey key = new NextTransitionKey(se, s);
            if (!nextTransitions.containsKey(key)) {
                nextTransitions.put(key, calculateTransition(se, s));
            }
        }
    }

    /**
     * R1: Choose transition containing parent<br/>
     * R2: When multiple choice:<br/>
     * if (grandparent != me) choose the one having transition to grandparent
     * else choose the transition grandparent chose
     */
    public static StateTransition calculateTransition(SpawnedEventStateIncoming se, SimpleState s) {
        EnumSet<StateTransition> possibleTransitions = transitionsPerPair.get(new SimpleStatePair(se.transition.from, s));
        if (possibleTransitions.isEmpty()) {
            throw new IllegalStateException("Something fishy about " + se + " transition for " + s);
        }
        if (possibleTransitions.size() == 1) {
            return possibleTransitions.iterator().next();
        }
        // Apply rule 2
        // If there is a grandparent transition and s == grandparent take it
        StateTransition result = findFromGrandParent(se, s, possibleTransitions);
        if (result != null) {
            return result;
        }

        // Else take any possible transitions going back to parent
        result = findFromParent(se, s, possibleTransitions);
        if (result != null) {
            return result;
        }

        throw new IllegalStateException("Did not find any transition for " + s + " starting from " + se);
    }

    private static StateTransition findFromGrandParent(SpawnedEventStateIncoming se,
                                                       SimpleState s,
                                                       EnumSet<StateTransition> possibleTransitions) {
        SimpleState parentState = se.parentTransition.from;
        // Only grand parent can use this rule
        if (parentState == s) {
            return se.parentTransition;
        }
        // For all possibles transition if one goes back to grandparent or transition to grandparent
        // choose it (there can be only one :)
        StateTransition result = null;
        for (StateTransition possibleTransition : possibleTransitions) {
            for (SimpleState nextState : possibleTransition.next) {
                if (nextState == parentState) {
                    if (result == null) {
                        result = possibleTransition;
                    } else {
                        throw new IllegalStateException("Found more than one grandparent equal based on " + se.parentTransition
                                + " for " + possibleTransitions
                                + " s=" + s + " and se=" + se);
                    }
                }
            }
        }
        if (result != null) {
            return result;
        }
        for (StateTransition possibleTransition : possibleTransitions) {
            for (SimpleState nextState : possibleTransition.next) {
                if (nextState != parentState && nextState != se.transition.from
                        && possiblesNextStates.get(nextState).contains(parentState)
                        ) {
                    if (result == null || result == possibleTransition) {
                        result = possibleTransition;
                    } else {
                        System.err.println("Found more than one grandparent transition to based on " + se.parentTransition
                                + " for " + possibleTransitions
                                + " s=" + s + " and se=" + se);
                        return null;
                    }
                }
            }
        }
        return result;
    }

    private static StateTransition findFromParent(SpawnedEventStateIncoming se,
                                                  SimpleState s,
                                                  EnumSet<StateTransition> possibleTransitions) {
        SimpleState parentState = se.transition.from;
        if (parentState == s) {
            // Something wrong cannot transition to myself
            throw new IllegalStateException("Transition from " + parentState + " to " + s + " not possible");
        }
        // For all possibles transition if one transition back to parent
        // choose it (there can be only one :)
        StateTransition result = null;
        for (StateTransition possibleTransition : possibleTransitions) {
            for (SimpleState nextState : possibleTransition.next) {
                if (nextState != parentState
                        && possiblesNextStates.get(nextState).contains(parentState)
                        ) {
                    if (result == null) {
                        result = possibleTransition;
                    } else {
                        System.err.println("Found more than one parent transition to based on " + se.transition
                                + " for " + possibleTransitions
                                + " s=" + s + " and se=" + se);
                        return null;
                    }
                }
            }
        }
        return result;
    }

    public StateTransition findTransition(SpawnedEventStateIncoming se, SimpleState s) {
        return nextTransitions.get(new NextTransitionKey(se, s));
    }

    @Override
    public List<SpawnedEventState> nextSpawnedEvent(SpawnedEvent se) {
        SpawnedEventStateIncoming stateIncoming = (SpawnedEventStateIncoming) se.stateHolder;
        List<SpawnedEventState> res = new ArrayList<>(3);
        for (SimpleState state : stateIncoming.transition.next) {
            if (!possiblesNextStates.get(getOriginal().getOpposite()).contains(state)) {
                res.add(new SpawnedEventStateIncoming(findTransition(stateIncoming, state),
                        stateIncoming.transition));
            }
        }
        return res;
    }

    @Override
    public SpawnedEventState createOriginalState(StateTransition transition, StateTransition previousState) {
        return new SpawnedEventStateIncoming(transition, previousState);
    }
}

