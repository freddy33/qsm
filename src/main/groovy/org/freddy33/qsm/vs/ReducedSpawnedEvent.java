package org.freddy33.qsm.vs;

import java.util.EnumSet;

/**
 * @author freds on 12/8/14.
 */
public class ReducedSpawnedEvent {
    final int time;
    final EnumSet<SimpleState> states;

    public ReducedSpawnedEvent(SpawnedEvent se) {
        this.time = se.time;
        this.states = se.states;
    }
}
