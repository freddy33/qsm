package org.freddy33.qsm.vs;

/**
 * @author freds on 12/8/14.
 */
public class ReducedSpawnedEvent {
    final int length;
    final SpawnedEventState state;

    public ReducedSpawnedEvent(SpawnedEvent se) {
        this.length = se.length;
        this.state = se.stateHolder;
    }
}
