package org.freddy33.qsm.vs;

import java.util.EnumSet;

/**
 * @author freds on 12/8/14.
 */
public class ReducedSpawnedEvent {
    final int length;
    final EnumSet<SimpleState> states;

    public ReducedSpawnedEvent(SpawnedEvent se) {
        this.length = se.length;
        this.states = se.states;
    }
}
