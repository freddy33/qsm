package org.freddy33.qsm.vs;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import static org.freddy33.qsm.vs.SimpleState.S1;

/**
 * @author freds on 12/16/14.
 */
public class JUniverse {
    static boolean stop = false;
    int currentTime = 0;
    Set<SourceEvent> activeSourceEvents = new HashSet<>();

    public static void main(String[] args) {
        StateTransition.verifyAll();
        JUniverse jUniverse = new JUniverse();
        jUniverse.init();
        for (int i = 0; i < 400; i++) {
            jUniverse.findNewEvents();
            jUniverse.calcNext();
            if (stop) break;
        }
    }

    private void init() {
        int trSize = 42;
        addOriginalEvent(new Point(0, 0, 0), S1);
        addOriginalEvent(new Point(0, -trSize, (int) (1.732 * trSize)), S1);
        addOriginalEvent(new Point(0, -trSize, -(int) (1.732 * trSize)), S1);
        addOriginalEvent(new Point(0, 2 * trSize, 0), S1);
    }

    private void calcNext() {
        activeSourceEvents.parallelStream().forEach(sourceEvent -> {
            sourceEvent.pollCurrent(currentTime).parallelStream().forEach(sourceEvent::calcNext);
        });
        currentTime++;
    }

    private void findNewEvents() {
        if (Controls.debug) {
            System.out.printf("%d : %,d\n", currentTime, activeSourceEvents.size());
            for (SourceEvent sourceEvent : activeSourceEvents) {
                System.out.printf("\t%d : %,d\n", sourceEvent.id, sourceEvent.peekCurrent(currentTime).size());
            }
        }
        Map<MatchingSpawnedEvents, MatchingSpawnedEvents> matches = new ConcurrentHashMap<>();
        AtomicInteger count = new AtomicInteger(0);
        activeSourceEvents.parallelStream().forEach(sourceEvent -> {
            sourceEvent.peekCurrent(currentTime).parallelStream().forEach(spawnedEvent -> {
                if (Controls.matchAlsoState) {
                    for (SimpleState state : spawnedEvent.states) {
                        count.incrementAndGet();
                        addMatch(matches, new MatchingLengthAndStateSpawnedEvents(
                                spawnedEvent.p, spawnedEvent.length, state), sourceEvent, spawnedEvent);
                    }
                } else {
                    count.incrementAndGet();
                    addMatch(matches, new MatchingOnlyLengthSpawnedEvents(
                            spawnedEvent.p, spawnedEvent.length), sourceEvent, spawnedEvent);
                }
            });
        });
        int deltaMatchSet = count.get() - matches.size();
        System.out.printf("%d : %,d %,d\n", currentTime, deltaMatchSet, count.get());
        AtomicInteger[] perSourceCount = new AtomicInteger[5];
        for (int i = 0; i < perSourceCount.length; i++) {
            perSourceCount[i] = new AtomicInteger(0);
        }
        Map<Set<SourceEvent>, Set<MatchingSpawnedEvents>> matchPerSourceCollection = new ConcurrentHashMap<>();
        matches.keySet().parallelStream()
                .filter(match -> {
                    int size = match.getSourcesInvolved().size();
                    perSourceCount[size].incrementAndGet();
                    return size >= 3;
                }).forEach(match -> {
            Set<SourceEvent> sourcesInvolved = match.getSourcesInvolved();
                    int nbSources = sourcesInvolved.size();
                    System.out.printf("Found %,d sources involved for %s\n", nbSources, match.getPoint().toString());
                    if (nbSources > 4) {
                        throw new IllegalStateException("Not supported yet");
                    } else if (nbSources == 4) {
                        System.out.printf("Found %,d sources involved for %s\n", nbSources, match.getPoint().toString());
                        Set<Set<SourceEvent>> setOfSets = CollectionUtils.extractSubSets(sourcesInvolved, 3);
                        for (Set<SourceEvent> set : setOfSets) {
                            matchPerSourceCollection.putIfAbsent(set, new HashSet<>(1));
                            matchPerSourceCollection.get(sourcesInvolved).add(match);
                        }
                    } else {
                        matchPerSourceCollection.putIfAbsent(sourcesInvolved, new HashSet<>(1));
                        matchPerSourceCollection.get(sourcesInvolved).add(match);
                    }
                });
        if (!matchPerSourceCollection.isEmpty()) {
            System.out.printf("Found %,d match per source\n", matchPerSourceCollection.size());
            for (Map.Entry<Set<SourceEvent>, Set<MatchingSpawnedEvents>> setSetEntry : matchPerSourceCollection.entrySet()) {
                System.out.printf("\t%s : %,d\n", setSetEntry.getKey(), setSetEntry.getValue().size());
            }
        } else {
            for (int i = 2; i < perSourceCount.length; i++) {
                int c = perSourceCount[i].get();
                if (c > 0) {
                    System.out.printf("\t%d %,d\n", i, c);
                }
            }
        }
        if (matchPerSourceCollection.size() >= 4) {
            System.out.println("We found 4!\n");
            stop = true;
        }
    }

    private void addMatch(Map<MatchingSpawnedEvents, MatchingSpawnedEvents> matches,
                          MatchingSpawnedEvents match, SourceEvent sourceEvent, SpawnedEvent spawnedEvent) {
        matches.putIfAbsent(match, match);
        matches.get(match).add(sourceEvent, spawnedEvent);
    }

    void addOriginalEvent(Point p, SimpleState s) {
        activeSourceEvents.add(new SourceEvent(currentTime, p, s));
    }
}
