package org.freddy33.qsm.vs.selector.common;

import org.freddy33.qsm.vs.base.StateTransition;
import org.freddy33.qsm.vs.event.SpawnedEvent;

import java.util.List;

/**
 * @author freds on 12/25/14.
 */
public interface NextStateSelector {
    List<SpawnedEventState> nextSpawnedEvent(SpawnedEvent se);

    SpawnedEventState createOriginalState(StateTransition transition, StateTransition previousState);
}
