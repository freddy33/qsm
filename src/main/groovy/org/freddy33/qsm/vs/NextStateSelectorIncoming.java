package org.freddy33.qsm.vs;

import java.util.*;

/**
 * @author freds on 12/25/14.
 */
class SpawnedEventStateIncoming implements SpawnedEventState {
    final StateTransition transition;
    final StateTransition parentTransition;

    SpawnedEventStateIncoming(StateTransition transition, StateTransition parentTransition) {
        this.transition = transition;
        this.parentTransition = parentTransition;
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

public class NextStateSelectorIncoming implements NextStateSelector {
    static final Map<SimpleStatePair, EnumSet<StateTransition>> fromCurrentTransitions = new HashMap<>();
    static final Map<SimpleState, EnumSet<SimpleState>> possiblesNextStates = new HashMap<>();

    static void verifyAll() {
        for (SimpleState current : SimpleState.values()) {
            List<StateTransition> stateTransitions = StateTransition.transitions.get(current);
            for (StateTransition transition : stateTransitions) {
                EnumSet<SimpleState> nextStates = EnumSet.of(transition.next[0], transition.next);
                EnumSet<SimpleState> existingNextStates = possiblesNextStates.putIfAbsent(current, nextStates);
                if (existingNextStates != null) {
                    existingNextStates.addAll(nextStates);
                }
                for (SimpleState nextState : transition.next) {
                    SimpleStatePair key = new SimpleStatePair(current, nextState);
                    EnumSet<StateTransition> transitions = fromCurrentTransitions.get(key);
                    if (transitions != null) {
                        transitions.add(transition);
                    } else {
                        fromCurrentTransitions.put(key, EnumSet.of(transition));
                    }
                }
            }
        }
    }

    @Override
    public List<SpawnedEventState> nextSpawnedEvent(SpawnedEvent se) {
        SpawnedEventStateIncoming stateIncoming = (SpawnedEventStateIncoming) se.stateHolder;
        List<SpawnedEventState> res = new ArrayList<>(3);
        for (SimpleState state : stateIncoming.transition.next) {
            res.add(new SpawnedEventStateIncoming(findTransition(stateIncoming, state), stateIncoming.transition));
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
     *
     * @param se
     * @param s
     * @return
     */
    public StateTransition findTransition(SpawnedEventStateIncoming se, SimpleState s) {
        EnumSet<StateTransition> possibleTransitions = fromCurrentTransitions.get(new SimpleStatePair(se.transition.from, s));
        if (possibleTransitions.isEmpty()) {
            throw new IllegalStateException("Something fishy about " + se + " transition for " + s);
        }
        if (possibleTransitions.size() == 1) {
            return possibleTransitions.iterator().next();
        }
        // Apply rule 2
        if (se.parentTransition.from == s) {
            return se.parentTransition;
        }
        // For all possibles transition if one goes back to grandparent or transition to grandparent
        // choose it (there can be only one :)
        for (StateTransition possibleTransition : possibleTransitions) {
            for (SimpleState nextState : possibleTransition.next) {
                if (nextState != se.transition.from &&
                        (nextState == se.parentTransition.from ||
                                possiblesNextStates.get(nextState)
                                        .contains(se.parentTransition.from))) {
                    return possibleTransition;
                }
            }
        }
        throw new IllegalStateException("Did not find any transition for " + s + " starting from " + se);
    }
}

final class SimpleStatePair {
    final SimpleState from;
    final SimpleState current;

    SimpleStatePair(SimpleState from, SimpleState current) {
        this.from = from;
        this.current = current;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SimpleStatePair that = (SimpleStatePair) o;

        if (current != that.current) return false;
        if (from != that.from) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = from.hashCode();
        result = 31 * result + current.hashCode();
        return result;
    }
}
