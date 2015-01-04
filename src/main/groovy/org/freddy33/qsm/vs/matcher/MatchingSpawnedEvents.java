package org.freddy33.qsm.vs.matcher;

import org.freddy33.qsm.vs.base.Point;
import org.freddy33.qsm.vs.event.SourceEvent;
import org.freddy33.qsm.vs.event.SpawnedEvent;

import java.util.Set;

/**
 * @author freds on 12/16/14.
 */
public interface MatchingSpawnedEvents {
    Point getPoint();

    int getLength();

    void add(SourceEvent sourceEvent, SpawnedEvent se);

    boolean isValid();

    Set<SourceEvent> getSourcesInvolved();
}
