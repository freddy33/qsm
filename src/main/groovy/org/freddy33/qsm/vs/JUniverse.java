package org.freddy33.qsm.vs;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

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

    private void calcNext() {
        activeSourceEvents.parallelStream().forEach(sourceEvent -> {
            sourceEvent.pollCurrent(currentTime).parallelStream().forEach(sourceEvent::calcNext);
        });
        currentTime++;
    }

    private void findNewEvents() {
        System.out.printf("%d : %,d\n", currentTime, activeSourceEvents.size());
        for (SourceEvent sourceEvent : activeSourceEvents) {
            System.out.printf("\t%d : %,d\n", sourceEvent.id, sourceEvent.peekCurrent(currentTime).size());
        }
        Map<MatchingSpawnedEvents, MatchingSpawnedEvents> matches = new ConcurrentHashMap<>();
        activeSourceEvents.parallelStream().forEach(sourceEvent -> {
            sourceEvent.peekCurrent(currentTime).parallelStream().forEach(spawnedEvent -> {
                if (Controls.matchAlsoState) {
                    for (SimpleState state : spawnedEvent.states) {
                        addMatch(matches,
                                new MatchingLengthAndStateSpawnedEvents(
                                        spawnedEvent.p, spawnedEvent.length, state),
                                sourceEvent, spawnedEvent);
                    }
                } else {
                    addMatch(matches,
                            new MatchingOnlyLengthSpawnedEvents(
                                    spawnedEvent.p, spawnedEvent.length),
                            sourceEvent, spawnedEvent);
                }
            });
        });
        System.out.printf("Found %,d matches\n", matches.size());
        Map<Set<SourceEvent>, Set<MatchingSpawnedEvents>> matchPerSourceCollection = new ConcurrentHashMap<>();
        matches.keySet().parallelStream()
                .filter(MatchingSpawnedEvents::isValid)
                .forEach(match -> {
                    Set<SourceEvent> sourcesInvolved = match.getSourcesInvolved();
                    int nbSources = sourcesInvolved.size();
                    System.out.printf("Found %,d sources involved for %s\n", nbSources, match.getPoint().toString());
                    if (nbSources > 4) {
                        throw new IllegalStateException("Not supported yet");
                    } else if (nbSources == 4) {
                        SourceEvent[] sourceEvents = sourcesInvolved.toArray(new SourceEvent[nbSources]);
                        Set<Set<SourceEvent>> setOfSets = new HashSet<>(4);
                        int first = 0;
                        while (setOfSets.size() != 4) {
                            HashSet<SourceEvent> result = new HashSet<>(3);
                            result.add(sourceEvents[first]);
                            int second = first + 1;
                            if (second == nbSources) {
                                second = 0;
                            }
                            int third = second + 1;
                            if (third == nbSources) {
                                third = 0;
                            }
                            result.add(sourceEvents[second]);
                            result.add(sourceEvents[third]);
                            if (result.size() != 3) {
                                throw new IllegalStateException("Hard!");
                            }
                            setOfSets.add(result);
                            first++;
                        }
                        for (Set<SourceEvent> set : setOfSets) {
                            matchPerSourceCollection.putIfAbsent(set, new HashSet<>(1));
                            matchPerSourceCollection.get(sourcesInvolved).add(match);
                        }
                    } else {
                        matchPerSourceCollection.putIfAbsent(sourcesInvolved, new HashSet<>(1));
                        matchPerSourceCollection.get(sourcesInvolved).add(match);
                    }
                });
        System.out.printf("Found %,d match per source\n", matchPerSourceCollection.size());
        if (matchPerSourceCollection.size() >= 4) {
            System.out.println("We found 4!\n");
            stop = true;
        }
    }

    private void addMatch(Map<MatchingSpawnedEvents, MatchingSpawnedEvents> matches, MatchingSpawnedEvents match, SourceEvent sourceEvent, SpawnedEvent spawnedEvent) {
        matches.putIfAbsent(match, match);
        match = matches.get(match);
        match.add(sourceEvent, spawnedEvent);
    }

    private void init() {
        int trSize = 42;
        addOriginalEvent(new Point(0, 0, 0), S1);
        addOriginalEvent(new Point(0, -trSize, (int) (1.732 * trSize)), S1);
        addOriginalEvent(new Point(0, -trSize, -(int) (1.732 * trSize)), S1);
        addOriginalEvent(new Point(0, 2 * trSize, 0), S1);
    }

    void addOriginalEvent(Point p, SimpleState s) {
        activeSourceEvents.add(new SourceEvent(currentTime, p, s));
    }
}
