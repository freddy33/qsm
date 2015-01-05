package org.freddy33.qsm.vs.selector.incoming;

import org.freddy33.qsm.vs.base.SimpleState;
import org.freddy33.qsm.vs.base.StateTransition;
import org.freddy33.qsm.vs.selector.common.SimpleStatePair;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by freds on 1/4/15.
 */
public class NextStateSelectorTransitionIncoming extends NextStateSelectorIncoming {
    public static final Map<NextTransitionKey, StateTransition> nextTransitions = new HashMap<>();

    public NextStateSelectorTransitionIncoming(SimpleState original) {
        super(original);
    }

    public static void verifyAllTransitionNextSelector() {
        verifyAllBaseNextSelector();
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
}
