package org.freddy33.qsm.vs;

import org.freddy33.qsm.vs.base.Point;
import org.freddy33.qsm.vs.base.SimpleStateGroup;
import org.freddy33.qsm.vs.base.StateTransition;
import org.freddy33.qsm.vs.control.Controls;
import org.freddy33.qsm.vs.event.SourceEvent;
import org.freddy33.qsm.vs.event.SpawnedEvent;
import org.freddy33.qsm.vs.matcher.MatchingLengthAndStateSpawnedEvents;
import org.freddy33.qsm.vs.matcher.MatchingOnlyLengthSpawnedEvents;
import org.freddy33.qsm.vs.matcher.MatchingSpawnedEvents;
import org.freddy33.qsm.vs.utils.CollectionUtils;

import java.text.MessageFormat;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import static org.freddy33.qsm.vs.base.StateTransition.*;

/**
 * @author freds on 12/16/14.
 */
public class JUniverse {
    public static final int MAX_ROUND = 400;
    public static final int TRIANGLE_HALF_SIDE = 42;
    static boolean stop = false;

    int currentTime = 0;
    Set<SourceEvent> activeSourceEvents = new HashSet<>();

    public static void main(String[] args) {
        StateTransition.verifyAllTransitions();
        // MAX_ROUND should be at least X
        int minTime = TRIANGLE_HALF_SIDE * SimpleStateGroup.ONE.delta;
        if (MAX_ROUND <= minTime) {
            throw new IllegalStateException("Cannot reach sync in " + MAX_ROUND + " needs to be minimum " + minTime);
        }
        int minWithForward = (int) Math.sqrt(2 * minTime * minTime);
        if (MAX_ROUND <= minWithForward) {
            System.err.println("Cannot reach triangle forward sync in " + MAX_ROUND + " needs to be minimum "
                    + minWithForward + " for a triangle min of " + minTime);
        }
        System.out.println("Triangle forward sync in minimum " + minWithForward + " for a triangle of " + minTime);

        JUniverse jUniverse = new JUniverse();
        jUniverse.init();
        for (int i = 0; i < MAX_ROUND; i++) {
            jUniverse.findNewEvents();
            jUniverse.calcNext();
            if (stop) break;
        }
    }

    private void init() {
        addOriginalEvent(new Point(0, 0, 0), S1_1, S24_1);
        addOriginalEvent(new Point(0, -TRIANGLE_HALF_SIDE, (int) (1.732 * TRIANGLE_HALF_SIDE)), S1_2, S22_1);
        addOriginalEvent(new Point(0, -TRIANGLE_HALF_SIDE, -(int) (1.732 * TRIANGLE_HALF_SIDE)), S1_3, S19_1);
        addOriginalEvent(new Point(0, 2 * TRIANGLE_HALF_SIDE, 0), S1_4, S21_1);
    }

    private void calcNext() {
        activeSourceEvents.parallelStream().forEach(sourceEvent ->
                sourceEvent.pollCurrent(currentTime).parallelStream().forEach(sourceEvent::calcNext));
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
        activeSourceEvents.parallelStream().forEach(sourceEvent ->
                sourceEvent.peekCurrent(currentTime).parallelStream().forEach(spawnedEvent -> {
                    if (Controls.matchAlsoState) {
                        addMatch(matches, new MatchingLengthAndStateSpawnedEvents(
                                spawnedEvent.p, spawnedEvent.length, spawnedEvent.stateHolder.getSimpleState()),
                                sourceEvent, spawnedEvent);
                    } else {
                        addMatch(matches, new MatchingOnlyLengthSpawnedEvents(
                                spawnedEvent.p, spawnedEvent.length), sourceEvent, spawnedEvent);
                    }
                }));
        if (Controls.info) {
            System.out.printf("%d : %,d\n", currentTime, matches.size());
        }
        AtomicInteger[] perSourceCount = new AtomicInteger[5];
        for (int i = 0; i < perSourceCount.length; i++) {
            perSourceCount[i] = new AtomicInteger(0);
        }
        Map<Set<SourceEvent>, Set<MatchingSpawnedEvents>> matchPerSourceCollection = new ConcurrentHashMap<>();
        matches.keySet().parallelStream()
                .filter(match -> {
                    if (Controls.numbersOutput) {
                        int size = match.getSourcesInvolved().size();
                        perSourceCount[size].incrementAndGet();
                        return size >= 3;
                    } else {
                        return match.isValid();
                    }
                }).forEach(match -> {
            Set<SourceEvent> sourcesInvolved = match.getSourcesInvolved();
            int nbSources = sourcesInvolved.size();
            if (Controls.debug) {
                System.out.printf("Found %,d sources involved for %s\n", nbSources, match.getPoint().toString());
            }
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
        if (Controls.numbersOutput && perSourceCount[2].get() > 0) {
            StringBuilder msg = new StringBuilder();
            msg.append(currentTime);
            for (int i = 2; i < perSourceCount.length; i++) {
                int c = perSourceCount[i].get();
                if (c > 0) {
                    msg.append(MessageFormat.format("\t{0}", c));
                }
            }
            msg.append("\n");
            System.out.print(msg.toString());
        }
        if (Controls.info && !matchPerSourceCollection.isEmpty()) {
            System.out.printf("Found %,d match per source\n", matchPerSourceCollection.size());
            for (Map.Entry<Set<SourceEvent>, Set<MatchingSpawnedEvents>> setSetEntry : matchPerSourceCollection.entrySet()) {
                System.out.printf("\t%s : %,d\n", setSetEntry.getKey(), setSetEntry.getValue().size());
            }
        }
        if (matchPerSourceCollection.size() >= 4) {
            System.out.println("We found 4 at " + currentTime + "\n");
            for (Map.Entry<Set<SourceEvent>, Set<MatchingSpawnedEvents>> setSetEntry : matchPerSourceCollection.entrySet()) {
                System.out.printf("\t%s :\n", setSetEntry.getKey());
                setSetEntry.getValue().forEach(mse -> System.out.printf("\t\t%s\n", mse.getPoint()));
            }
            stop = true;
        }
    }

    private void addMatch(Map<MatchingSpawnedEvents, MatchingSpawnedEvents> matches,
                          MatchingSpawnedEvents match, SourceEvent sourceEvent, SpawnedEvent spawnedEvent) {
        matches.putIfAbsent(match, match);
        matches.get(match).add(sourceEvent, spawnedEvent);
    }

    void addOriginalEvent(Point p, StateTransition originalState, StateTransition previousState) {
        activeSourceEvents.add(new SourceEvent(currentTime, p, originalState, previousState));
    }
}
