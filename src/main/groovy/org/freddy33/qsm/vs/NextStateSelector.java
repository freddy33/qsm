package org.freddy33.qsm.vs;

import java.util.List;

/**
 * @author freds on 12/25/14.
 */
public interface NextStateSelector {
    List<SpawnedEventState> nextSpawnedEvent(SpawnedEvent se);

    SpawnedEventState createOriginalState(StateTransition transition, StateTransition previousState);
}
