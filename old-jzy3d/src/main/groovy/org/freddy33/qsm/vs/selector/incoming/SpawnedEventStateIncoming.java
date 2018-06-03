package org.freddy33.qsm.vs.selector.incoming;

import org.freddy33.qsm.vs.base.SimpleState;
import org.freddy33.qsm.vs.base.StateTransition;
import org.freddy33.qsm.vs.control.Controls;
import org.freddy33.qsm.vs.selector.common.SpawnedEventState;

import java.util.EnumSet;

/**
 * @author freds on 12/25/14.
 */
public class SpawnedEventStateIncoming implements SpawnedEventState {
    final StateTransition transition;
    final StateTransition parentTransition;

    SpawnedEventStateIncoming(StateTransition transition,
                              StateTransition parentTransition) {
        if (transition == null || parentTransition == null) {
            throw new NullPointerException("Transition and Parent Transition cannot be null");
        }
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
        result = 31 * result + parentTransition.hashCode();
        return result;
    }
}
