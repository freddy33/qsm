package org.freddy33.qsm.vs.selector.common;

import org.freddy33.qsm.vs.base.StateTransition;
import org.freddy33.qsm.vs.event.BaseSpawnedEvent;

import java.util.List;

/**
 * @author freds on 12/25/14.
 */
public interface NextStateSelector {
    List<SpawnedEventState> nextSpawnedEvent(BaseSpawnedEvent se);

    SpawnedEventState createOriginalState(StateTransition transition, StateTransition previousState);
}
