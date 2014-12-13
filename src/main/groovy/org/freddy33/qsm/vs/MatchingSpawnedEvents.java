package org.freddy33.qsm.vs;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author freds on 12/9/14.
 */
public class MatchingSpawnedEvents {
    final Point p;
    final LengthAndState ls;
    final Set<SpawnedEvent> match = new HashSet<>(3);
    private Set<SourceEvent> sourceInvolved;

    public MatchingSpawnedEvents(Point p, LengthAndState ls) {
        this.p = p;
        this.ls = ls;
    }

    void add(SpawnedEvent se) {
        if (se.length != ls.length) {
            throw new IllegalArgumentException("Spawned Event " + se + " not the correct length " + ls.length);
        }
        if (!se.states.contains(ls.st)) {
            throw new IllegalArgumentException("Spawned Event " + se + " does not contain state " + ls.st);
        }
        if (!p.equals(se.p)) {
            throw new IllegalArgumentException("Spawned Event " + se + " does not point to " + p);
        }
        match.add(se);
    }

    boolean isValid() {
        return match.size() >= 3 && getSourcesInvolved().size() >= 3;
    }

    Set<SourceEvent> getSourcesInvolved() {
        if (sourceInvolved == null) {
            if (match.size() < 3) {
                throw new IllegalArgumentException("Not enough matching elements");
            }
            sourceInvolved = new HashSet<>(match.size());
            for (SpawnedEvent se : match) {
                sourceInvolved.add(se.origin);
            }
        }
        return sourceInvolved;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MatchingSpawnedEvents that = (MatchingSpawnedEvents) o;

        if (!ls.equals(that.ls)) return false;
        if (!p.equals(that.p)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = p.hashCode();
        result = 31 * result + ls.hashCode();
        return result;
    }
}
