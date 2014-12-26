package org.freddy33.qsm.vs;

import java.util.Arrays;
import java.util.List;

/**
 * @author freds on 12/25/14.
 */
public abstract class BaseNextStateSelector implements NextStateSelector {
    final SimpleState original;

    public BaseNextStateSelector(SimpleState original) {
        this.original = original;
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
