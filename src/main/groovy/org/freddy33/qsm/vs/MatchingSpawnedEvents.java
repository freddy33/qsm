package org.freddy33.qsm.vs;

import java.util.ArrayList;
import java.util.List;

/**
 * @author freds on 12/9/14.
 */
public class MatchingSpawnedEvents {
    final LengthAndState ls;
    final List<SpawnedEvents> match = new ArrayList<>(3);
    private List<SourceEvent> sourceInvolved;

    public MatchingSpawnedEvents(LengthAndState ls) {
        this.ls = ls;
    }

    void add(SpawnedEvents se) {
        if (se.length != ls.length) {
            throw new IllegalArgumentException("Spawned Event " + se + " not the correct length " + ls.length);
        }
        if (!se.states.contains(ls.st)) {
            throw new IllegalArgumentException("Spawned Event " + se + " does not contain state " + ls.st);
        }
        match.add(se);
    }

    boolean isValid() {
        return match.size() >= 3;
    }

    List<SourceEvent> getSourceInvolved() {
        if (sourceInvolved == null) {
            if (match.size() < 3) {
                throw new IllegalArgumentException("Not enough matching elements");
            }
            sourceInvolved = new ArrayList<>(match.size());
            for (SpawnedEvents se : match) {
                sourceInvolved.add(se.origin);
            }
        }
        return sourceInvolved;
    }
}
