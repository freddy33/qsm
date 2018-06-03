package org.freddy33.qsm.vs.selector.common;

import org.freddy33.qsm.vs.base.SimpleState;
import org.freddy33.qsm.vs.base.StateTransition;

import java.util.*;

import static org.freddy33.qsm.vs.utils.CollectionUtils.intersect;

/**
 * @author freds on 12/25/14.
 */
public abstract class BaseNextStateSelector implements NextStateSelector {
    public static final Map<SimpleState, EnumSet<SimpleState>> possiblesNextStates = new HashMap<>();
    public static final Map<SimpleStatePair, EnumSet<StateTransition>> transitionsPerPair = new HashMap<>();
    public static final Map<SimpleStateTriple, StateTransition> transitionsPerTriple = new HashMap<>();
    final SimpleState original;

    public BaseNextStateSelector(SimpleState original) {
        this.original = original;
    }

    public static void verifyPossibleNextStates() {
        if (!possiblesNextStates.isEmpty()) {
            return;
        }
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
    }

    public static void verifyTransitionsPerPair() {
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
        for (Map.Entry<SimpleStatePair, EnumSet<StateTransition>> entry : transitionsPerPair.entrySet()) {
            if (entry.getValue().isEmpty()) {
                throw new IllegalStateException("Found empty transitions for pair " + entry.getKey());
            }
            if (entry.getValue().size() > 2) {
                throw new IllegalStateException("Found more than 2 transitions for pair " + entry.getKey()
                        + "\n" + entry.getValue());
            }
        }
    }

    public static void verifyTransitionsPerTriple() {
        for (Map.Entry<SimpleStatePair, EnumSet<StateTransition>> entry : transitionsPerPair.entrySet()) {
            SimpleStatePair pair = entry.getKey();
            SimpleState grandParent = pair.parent;
            for (StateTransition transition : entry.getValue()) {
                for (SimpleState state : transition.next) {
                    SimpleStateTriple triple = new SimpleStateTriple(grandParent, pair.current, state);
                    if (triple.isUndecidable()) {
                        continue;
                    }
                    EnumSet<StateTransition> nextPossibles = transitionsPerPair.get(new SimpleStatePair(pair.current, state));
                    StateTransition goodTransition;
                    if (nextPossibles.size() == 1) {
                        // all good
                        goodTransition = nextPossibles.iterator().next();
                    } else {
                        // Select the good one based on pair.parent
                        Iterator<StateTransition> iterator = nextPossibles.iterator();
                        StateTransition st1 = iterator.next();
                        StateTransition st2 = iterator.next();
                        if (st1.hasNext(grandParent) && !st2.hasNext(grandParent)) {
                            goodTransition = st1;
                        } else if (!st1.hasNext(grandParent) && st2.hasNext(grandParent)) {
                            goodTransition = st2;
                        } else {
                            // Use the 2 other states without triple.parent == pair.current for st1 and st2
                            // and first look if they are triple.grandParent == pair.parent
                            // then if any in the list of possible next of the opposite of pair.parent
                            EnumSet<SimpleState> otherNextSt1 = st1.others(pair.current);
                            EnumSet<SimpleState> otherNextSt2 = st2.others(pair.current);
                            if (grandParent == triple.current) {
                                // Grand parent and current equal, needs to use other of parent to current
                                EnumSet<StateTransition> parentPossiblesTransitions = entry.getValue();
                                EnumSet<SimpleState> parentNextStates;
                                if (parentPossiblesTransitions.size() == 1) {
                                    parentNextStates = parentPossiblesTransitions.iterator().next().others(grandParent);
                                } else {
                                    Iterator<StateTransition> itParent = parentPossiblesTransitions.iterator();
                                    parentNextStates = itParent.next().others(grandParent);
                                    parentNextStates.addAll(itParent.next().others(grandParent));
                                }
                                boolean st1ParentIntersects = intersect(otherNextSt1, parentNextStates);
                                boolean st2ParentIntersects = intersect(otherNextSt2, parentNextStates);
                                if (st1ParentIntersects && !st2ParentIntersects) {
                                    goodTransition = st1;
                                } else if (!st1ParentIntersects && st2ParentIntersects) {
                                    goodTransition = st2;
                                } else {
                                    // Using current transition
                                    EnumSet<SimpleState> others = transition.others(state);
                                    boolean st1Intersects = intersect(otherNextSt1, others);
                                    boolean st2Intersects = intersect(otherNextSt2, others);
                                    if (st1Intersects && !st2Intersects) {
                                        goodTransition = st1;
                                    } else if (!st1Intersects && st2Intersects) {
                                        goodTransition = st2;
                                    } else {
                                        if (transitionsPerTriple.containsKey(triple)) {
                                            // Already found with another good transition
                                            goodTransition = transitionsPerTriple.get(triple);
                                            System.out.println("Have 2 sol " + st1 + " " + st2
                                                    + " for " + triple
                                                    + " but found " + goodTransition + " before");
                                        } else {
                                            System.err.println("Have 2 sol " + st1 + " " + st2 + " for " + triple);
                                            goodTransition = null;
                                        }
                                    }
                                }
                            } else {
                                EnumSet<SimpleState> currentNextStates = possiblesNextStates.get(grandParent);
                                EnumSet<SimpleState> oppositeNextStates = possiblesNextStates.get(grandParent.getOpposite());
                                boolean nextS1HasNext = intersect(otherNextSt1, currentNextStates);
                                boolean nextS2HasNext = intersect(otherNextSt2, currentNextStates);
                                boolean nextS1HasOpposite = intersect(otherNextSt1, oppositeNextStates);
                                boolean nextS2HasOpposite = intersect(otherNextSt2, oppositeNextStates);
                                if (otherNextSt1.contains(grandParent) && !otherNextSt2.contains(grandParent)) {
                                    goodTransition = st1;
                                } else if (!otherNextSt1.contains(grandParent) && otherNextSt2.contains(grandParent)) {
                                    goodTransition = st2;
                                } else if (nextS1HasNext && !nextS2HasNext) {
                                    goodTransition = st1;
                                } else if (!nextS1HasNext && nextS2HasNext) {
                                    goodTransition = st2;
                                } else if (!nextS1HasOpposite && nextS2HasOpposite) {
                                    goodTransition = st1;
                                } else if (nextS1HasOpposite && !nextS2HasOpposite) {
                                    goodTransition = st2;
                                } else {
                                    throw new IllegalStateException("Have 2 sol " + st1 + " " + st2 + " for " + triple);
                                }
                            }
                        }
                    }
                    StateTransition existing = transitionsPerTriple.put(triple, goodTransition);
                    if (existing != null && existing != goodTransition) {
                        throw new IllegalStateException("Entry for triple state unstable for " + triple
                                + " found " + existing + " instead of " + goodTransition);
                    }
                }
            }
        }
        for (Map.Entry<SimpleStateTriple, StateTransition> entry : transitionsPerTriple.entrySet()) {
            if (entry.getValue() == null) {
                throw new IllegalStateException("Found empty transitions for triple " + entry.getKey());
            }
        }
    }

    public SimpleState getOriginal() {
        return original;
    }

    protected SimpleState[] getNextSimpleStates(SimpleState s, TransitionMode transitionMode, int transitionSelect) {
        switch (transitionMode) {
            case transitionFromOriginal: {
                // Always pick transitions from original state
                List<StateTransition> possibles = StateTransition.transitions.get(original);
                return possibles.get(transitionSelect % possibles.size()).next;
            }
            case transitionFromIncoming: {
                // Pick transitions from incoming state
                List<StateTransition> possibles = StateTransition.transitions.get(s);
                return possibles.get(transitionSelect % possibles.size()).next;
            }
            case incomingContinue:
                return new SimpleState[]{s};
            case backToOriginal:
                return new SimpleState[]{original};
        }
        throw new IllegalStateException("Modulo " + Arrays.toString(TransitionMode.values()) + " should return");
    }
}
