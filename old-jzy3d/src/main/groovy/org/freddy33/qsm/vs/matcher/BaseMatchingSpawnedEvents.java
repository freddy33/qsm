package org.freddy33.qsm.vs.matcher;

import org.freddy33.qsm.vs.base.Point;
import org.freddy33.qsm.vs.event.BaseSpawnedEvent;
import org.freddy33.qsm.vs.event.SourceEvent;

import java.util.HashSet;
import java.util.Set;

/**
 * @author freds on 12/16/14.
 */
public abstract class BaseMatchingSpawnedEvents implements MatchingSpawnedEvents {
    public final Point p;
    public final int length;
    public final Set<SourceEvent> sourcesInvolved = new HashSet<>(3);

    public BaseMatchingSpawnedEvents(Point p, int length) {
        this.p = p;
        this.length = length;
    }

    @Override
    public Point getPoint() {
        return p;
    }

    @Override
    public int getLength() {
        return length;
    }

    @Override
    public Set<SourceEvent> getSourcesInvolved() {
        return sourcesInvolved;
    }

    @Override
    public void add(SourceEvent sourceEvent, BaseSpawnedEvent se) {
        if (!p.equals(se.point)) {
            throw new IllegalArgumentException("Spawned Event " + se + " does not point to " + p);
        }
        if (se.length != length) {
            throw new IllegalArgumentException("Spawned Event " + se + " not the correct length " + length);
        }
        sourcesInvolved.add(sourceEvent);
    }

    @Override
    public boolean isValid() {
        return sourcesInvolved.size() >= 3;
    }
}
