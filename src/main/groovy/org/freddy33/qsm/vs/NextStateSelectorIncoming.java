package org.freddy33.qsm.vs;

import java.util.*;

/**
 * @author freds on 12/25/14.
 */
class SpawnedEventStateIncoming implements SpawnedEventState {
    final StateTransition transition;
    final StateTransition parentTransition;

    SpawnedEventStateIncoming(StateTransition transition,
                              StateTransition parentTransition) {
        this.transition = transition;
        this.parentTransition = parentTransition;
    }

    @Override
    public synchronized void add(SpawnedEventState newStates) {
        // Nothing all in equals and hash
        if (Controls.debug) {
            System.out.println("Why you add to " + this + " " + newStates);
        }
    }

    @Override
    public SimpleState getSimpleState() {
        return transition.from;
    }

    @Override
    public EnumSet<SimpleState> getStates() {
        return EnumSet.of(transition.next[0], transition.next);
    }

    @Override
    public String toString() {
        return "SpawnedEventStateIncoming{" +
                "transition=" + transition +
                ", parentTransition=" + parentTransition +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SpawnedEventStateIncoming that = (SpawnedEventStateIncoming) o;

        if (parentTransition != that.parentTransition) return false;
        if (transition != that.transition) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = transition.hashCode();
        result = 31 * result + (parentTransition != null ? parentTransition.hashCode() : 0);
        return result;
    }
}

public class NextStateSelectorIncoming extends BaseNextStateSelector {
    static final Map<SimpleStatePair, EnumSet<StateTransition>> transitionsPerPair = new HashMap<>();
    static final Map<SimpleState, EnumSet<SimpleState>> possiblesNextStates = new HashMap<>();

    public NextStateSelectorIncoming(SimpleState original) {
        super(original);
    }

    static void verifyAll() {
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
    }

    @Override
    public List<SpawnedEventState> nextSpawnedEvent(SpawnedEvent se) {
        SpawnedEventStateIncoming stateIncoming = (SpawnedEventStateIncoming) se.stateHolder;
        List<SpawnedEventState> res = new ArrayList<>(3);
        for (SimpleState state : stateIncoming.transition.next) {
            res.add(new SpawnedEventStateIncoming(findTransition(stateIncoming, state),
                    stateIncoming.transition));
        }
        return res;
    }

    @Override
    public SpawnedEventState createOriginalState(StateTransition transition) {
        return new SpawnedEventStateIncoming(transition, null);
    }

    /**
     * R1: Choose transition containing parent<br/>
     * R2: When multiple choice:<br/>
     * if (grandparent != me) choose the one having transition to grandparent
     * else choose the transition grandparent chose
     */
    public StateTransition findTransition(SpawnedEventStateIncoming se, SimpleState s) {
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

    private StateTransition findFromGrandParent(SpawnedEventStateIncoming se,
                                                SimpleState s,
                                                EnumSet<StateTransition> possibleTransitions) {
        if (se.parentTransition == null) {
            return null;
        }
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
                        System.err.println("Found more than one grandparent equal based on " + se.parentTransition
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
                    }
                }
            }
        }
        return result;
    }

    private StateTransition findFromParent(SpawnedEventStateIncoming se,
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
                    }
                }
            }
        }
        return result;
    }
}

final class SimpleStatePair {
    final SimpleState parent;
    final SimpleState current;

    SimpleStatePair(SimpleState parent, SimpleState current) {
        this.parent = parent;
        this.current = current;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SimpleStatePair that = (SimpleStatePair) o;

        if (current != that.current) return false;
        if (parent != that.parent) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = parent.hashCode();
        result = 31 * result + current.hashCode();
        return result;
    }
}
