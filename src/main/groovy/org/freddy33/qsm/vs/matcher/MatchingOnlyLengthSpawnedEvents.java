package org.freddy33.qsm.vs.matcher;

import org.freddy33.qsm.vs.base.Point;
import org.freddy33.qsm.vs.event.SourceEvent;
import org.freddy33.qsm.vs.event.SpawnedEvent;

/**
 * @author freds on 12/9/14.
 */
public class MatchingOnlyLengthSpawnedEvents extends BaseMatchingSpawnedEvents {

    public MatchingOnlyLengthSpawnedEvents(Point p, int length) {
        super(p, length);
    }

    public void add(SourceEvent sourceEvent, SpawnedEvent se) {
        super.add(sourceEvent, se);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MatchingOnlyLengthSpawnedEvents that = (MatchingOnlyLengthSpawnedEvents) o;

        if (length != that.length) return false;
        if (!p.equals(that.p)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = p.hashCode();
        result = 31 * result + length;
        return result;
    }
}
