package org.freddy33.qsm.vs;

/**
 * @author freds on 12/25/14.
 */
public interface SpawnedEventState {
    void add(SpawnedEventState newStates);

    SimpleState getSimpleState();
}
