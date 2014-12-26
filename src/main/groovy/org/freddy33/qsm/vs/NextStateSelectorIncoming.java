package org.freddy33.qsm.vs;

import java.util.*;

/**
 * @author freds on 12/25/14.
 */
class SpawnedEventStateIncoming implements SpawnedEventState {
    final StateTransition transition;
    final StateTransition parentTransition;
    final StateTransition grandParentTransition;

    SpawnedEventStateIncoming(StateTransition transition,
                              StateTransition parentTransition,
                              StateTransition grandParentTransition) {
        this.transition = transition;
        this.parentTransition = parentTransition;
        this.grandParentTransition = grandParentTransition;
    }

    @Override
    public synchronized void add(SpawnedEventState newStates) {
        // Nothing all in equals and hash
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
                ", grandParentTransition=" + grandParentTransition +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SpawnedEventStateIncoming that = (SpawnedEventStateIncoming) o;

        if (grandParentTransition != that.grandParentTransition) return false;
        if (parentTransition != that.parentTransition) return false;
        if (transition != that.transition) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = transition.hashCode();
        result = 31 * result + (parentTransition != null ? parentTransition.hashCode() : 0);
        result = 31 * result + (grandParentTransition != null ? grandParentTransition.hashCode() : 0);
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
        for (StateTransition transition : StateTransition.values()) {
            for (SimpleState nextState : transition.next) {
                SimpleStatePair key = new SimpleStatePair(nextState, transition.from);
                EnumSet<StateTransition> transitions = transitionsPerPair.get(key);
                if (transitions != null) {
                    transitions.add(transition);
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
                    stateIncoming.transition, stateIncoming.parentTransition));
        }
        return res;
    }

    @Override
    public SpawnedEventState createOriginalState(StateTransition transition) {
        return new SpawnedEventStateIncoming(transition, null, null);
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
        // Apply rule 2 works only with a parent transition
        StateTransition result;
        result = findFromParent(se, s, possibleTransitions, se.transition);
        if (result != null) {
            return result;
        }

        if (se.parentTransition != null) {
            result = findFromParent(se, s, possibleTransitions, se.parentTransition);
            if (result != null) {
                return result;
            }
        }

        if (se.grandParentTransition != null) {
            result = findFromParent(se, s, possibleTransitions, se.grandParentTransition);
            if (result != null) {
                System.err.println("Needed all the way to grand parent for " + s + " starting from " + se);
                return result;
            }
        }

        throw new IllegalStateException("Did not find any transition for " + s + " starting from " + se);
    }

    private StateTransition findFromParent(SpawnedEventStateIncoming se,
                                           SimpleState s,
                                           EnumSet<StateTransition> possibleTransitions,
                                           StateTransition parentTransition) {
        SimpleState parentState = parentTransition.from;
        if (parentState == s) {
            return parentTransition;
        }
        // For all possibles transition if one goes back to grandparent or transition to grandparent
        // choose it (there can be only one :)
        for (StateTransition possibleTransition : possibleTransitions) {
            for (SimpleState nextState : possibleTransition.next) {
                if (nextState != se.transition.from &&
                        (nextState == parentState
                                || possiblesNextStates.get(nextState).contains(parentState)
                        )) {
                    return possibleTransition;
                }
            }
        }
        return null;
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
