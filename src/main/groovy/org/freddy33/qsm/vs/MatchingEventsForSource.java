package org.freddy33.qsm.vs;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @author freds on 12/13/14.
 */
public class MatchingEventsForSource {
    final SourceEvent source;
    final ConcurrentMap<Set<SourceEvent>, Set<MatchingSpawnedEvents>> spawnedEventsPerSourceSet;

    public MatchingEventsForSource(SourceEvent source) {
        this.source = source;
        this.spawnedEventsPerSourceSet = new ConcurrentHashMap<>(1);
    }

    void add(MatchingSpawnedEvents mse) {
        Set<SourceEvent> sourcesInvolved = mse.getSourcesInvolved();
        if (!sourcesInvolved.contains(source)) {
            throw new IllegalArgumentException("Matching event "+mse+" does not match "+source);
        }
        spawnedEventsPerSourceSet.putIfAbsent(sourcesInvolved, new HashSet<>(1));
        Set<MatchingSpawnedEvents> matchingSpawnedEventsSet = spawnedEventsPerSourceSet.get(sourcesInvolved);
        synchronized (matchingSpawnedEventsSet) {
            matchingSpawnedEventsSet.add(mse);
        }
    }

    boolean isValid() {
        return spawnedEventsPerSourceSet.size() >= 3;
    }
}
