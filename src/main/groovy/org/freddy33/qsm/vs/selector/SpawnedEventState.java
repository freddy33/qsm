package org.freddy33.qsm.vs.selector;

import org.freddy33.qsm.vs.base.SimpleState;

import java.util.EnumSet;

/**
 * @author freds on 12/25/14.
 */
public interface SpawnedEventState {
    void add(SpawnedEventState newStates);

    SimpleState getSimpleState();

    EnumSet<SimpleState> getStates();
}
