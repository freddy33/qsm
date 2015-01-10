package org.freddy33.qsm.vs;

import org.freddy33.qsm.vs.base.Point;
import org.freddy33.qsm.vs.base.SimpleStateGroup;
import org.freddy33.qsm.vs.base.StateTransition;
import org.freddy33.qsm.vs.control.Controls;
import org.freddy33.qsm.vs.event.ReducedSourceEvent;
import org.freddy33.qsm.vs.event.SourceEvent;
import org.freddy33.qsm.vs.event.SpawnedEvent;
import org.freddy33.qsm.vs.matcher.MatchingLengthSpawnedEvents;
import org.freddy33.qsm.vs.matcher.MatchingPointsBlock;
import org.freddy33.qsm.vs.matcher.MatchingSourcesBlock;
import org.freddy33.qsm.vs.matcher.MatchingSpawnedEvents;
import org.freddy33.qsm.vs.utils.CollectionUtils;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static org.freddy33.qsm.vs.base.StateTransition.*;

/**
 * @author freds on 12/16/14.
 */
public class JUniverse {
    public static final int MAX_ROUND = 4000;
    public static final int TRIANGLE_HALF_SIDE = 42;
    static boolean stop = false;

    int currentTime = 0;
    Set<ReducedSourceEvent> oldSourceEvents = new HashSet<>();
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

        long st = System.nanoTime();
        JUniverse jUniverse = new JUniverse();
        jUniverse.init();
        for (int i = 0; i < MAX_ROUND; i++) {
            jUniverse.findNewEvents();
            jUniverse.calcNext();
            if (stop) break;
        }
        System.out.printf("Run took %,d ns\n", System.nanoTime() - st);
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
        debugStartFindNewEvents();
        Map<MatchingSpawnedEvents, MatchingSpawnedEvents> matches = aggregateAllMatches();
        Map<Set<SourceEvent>, Set<MatchingSpawnedEvents>> matchPerSourceCollection = collectValidMatches(matches);
        Set<SourceEvent> involvedInMatches = new HashSet<>();
        matchPerSourceCollection.keySet().forEach(involvedInMatches::addAll);

        if (matchPerSourceCollection.size() >= 4 && involvedInMatches.size() >= 4) {
            System.out.println("We found " + matchPerSourceCollection.size() + " match of 3 at " + currentTime);
            // Find all block of 4 involved
            Set<Set<SourceEvent>> blocks = CollectionUtils.extractSubSets(involvedInMatches, 4);
            for (Set<SourceEvent> block : blocks) {
                MatchingSourcesBlock matchingSourcesBlock = new MatchingSourcesBlock(block, matchPerSourceCollection);
                if (matchingSourcesBlock.isValid()) {
                    System.out.println(matchingSourcesBlock);
                    MatchingPointsBlock best = matchingSourcesBlock.getBest();
                    int i = 0;
                    for (SourceEvent oldEvent : block) {
                        activeSourceEvents.remove(oldEvent);
                        oldSourceEvents.add(new ReducedSourceEvent(oldEvent, matchingSourcesBlock.getMatchingLength(), best));
                        // Choose the point opposite
                        int oppositeIdx = matchingSourcesBlock.getOppositeIdx(oldEvent.id);
                        addOriginalEvent(best.points[oppositeIdx], oldEvent.originalState, oldEvent.previousOriginalState);
                    }
                    break;
                }
            }
//            stop = true;
        }
    }

    private Map<Set<SourceEvent>, Set<MatchingSpawnedEvents>> collectValidMatches(Map<MatchingSpawnedEvents, MatchingSpawnedEvents> matches) {
        Map<Set<SourceEvent>, Set<MatchingSpawnedEvents>> matchPerSourceCollection = new ConcurrentHashMap<>();
        matches.keySet().parallelStream().filter(MatchingSpawnedEvents::isValid).forEach(match -> {
            Set<SourceEvent> sourcesInvolved = match.getSourcesInvolved();
            if (Controls.debug) {
                int nbSources = sourcesInvolved.size();
                System.out.printf("Found %,d sources involved for %s\n", nbSources, match.getPoint().toString());
            }
            Set<Set<SourceEvent>> setOfSets = CollectionUtils.extractSubSets(sourcesInvolved, 3);
            for (Set<SourceEvent> set : setOfSets) {
                matchPerSourceCollection.putIfAbsent(set, new HashSet<>(1));
                matchPerSourceCollection.get(set).add(match);
            }
        });
        if (Controls.info && !matchPerSourceCollection.isEmpty()) {
            System.out.printf("Found %,d match per source\n", matchPerSourceCollection.size());
            for (Map.Entry<Set<SourceEvent>, Set<MatchingSpawnedEvents>> setSetEntry : matchPerSourceCollection.entrySet()) {
                System.out.printf("\t%s : %,d\n", setSetEntry.getKey(), setSetEntry.getValue().size());
            }
        }
        return matchPerSourceCollection;
    }

    private Map<MatchingSpawnedEvents, MatchingSpawnedEvents> aggregateAllMatches() {
        Map<MatchingSpawnedEvents, MatchingSpawnedEvents> matches = new ConcurrentHashMap<>();
        activeSourceEvents.parallelStream().forEach(sourceEvent ->
                sourceEvent.peekCurrent(currentTime).parallelStream().forEach(spawnedEvent -> {
                    addMatch(matches, new MatchingLengthSpawnedEvents(
                            spawnedEvent.p, spawnedEvent.length), sourceEvent, spawnedEvent);
                }));
        if (Controls.info) {
            System.out.printf("%d : %,d\n", currentTime, matches.size());
        }
        return matches;
    }

    private void debugStartFindNewEvents() {
        if (Controls.debug) {
            System.out.printf("%d : %,d\n", currentTime, activeSourceEvents.size());
            for (SourceEvent sourceEvent : activeSourceEvents) {
                System.out.printf("\t%d : %,d\n", sourceEvent.id, sourceEvent.peekCurrent(currentTime).size());
            }
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
