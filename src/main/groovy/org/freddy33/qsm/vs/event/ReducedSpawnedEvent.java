package org.freddy33.qsm.vs.event;

import org.freddy33.qsm.vs.selector.SpawnedEventState;

/**
 * @author freds on 12/8/14.
 */
public class ReducedSpawnedEvent {
    public final int length;
    public final SpawnedEventState state;

    public ReducedSpawnedEvent(SpawnedEvent se) {
        this.length = se.length;
        this.state = se.stateHolder;
    }
}
