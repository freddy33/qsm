package org.freddy33.qsm.vs;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author freds on 12/8/14.
 */
public class SourceEvent {
    final static AtomicLong counter = new AtomicLong(0);

    final long id;
    final int time;
    final Point origin;
    final SimpleState state;
    final Map<Point, ReducedSpawnedEvent> used = new HashMap<>();
    final Random random;
    final TransitionRatio transitionRatio;
    // All the point present in currentPerTime after polling the active ones
    Set<Point> currentlyUsed = new HashSet<>();
    // Per time slice list of spawned events
    Map<Integer, Map<Point, SpawnedEvent>> currentPerTime = new HashMap<>();

    public SourceEvent(int time, Point origin, SimpleState state) {
        this.id = counter.getAndIncrement();
        this.time = time;
        this.origin = origin;
        this.state = state;
        SpawnedEvent se = new SpawnedEvent(this, origin, time, 0, state);
        HashMap<Point, SpawnedEvent> currentSpawned = new HashMap<>(1);
        this.currentPerTime.put(time, currentSpawned);
        currentSpawned.put(origin, se);
        if (Controls.useRandom) {
            this.random = new Random(id + time + origin.hashCode());
            this.transitionRatio = Controls.defaultRatio;
        } else {
            this.random = null;
            this.transitionRatio = null;
        }
    }

    public synchronized Map<Point, SpawnedEvent> pollCurrent(int currentTime) {
        Map<Point, SpawnedEvent> currentSpawned = currentPerTime.remove(currentTime);
        if (currentSpawned == null) {
            // There should always be something at the time
            throw new IllegalStateException("Asking for states at time " + currentTime + " return null data!");
        }
        for (SpawnedEvent event : currentSpawned.values()) {
            used.put(event.p, new ReducedSpawnedEvent(event));
        }
/*
        currentlyUsed = new HashSet<>();
        for (Map<Point, SpawnedEvent> eventMap : currentPerTime.values()) {
            currentlyUsed.addAll(eventMap.keySet());
        }
*/
        for (int i = 1; i < 6; i++) {
            int nextTime = currentTime + i;
            if (!currentPerTime.containsKey(nextTime)) {
                currentPerTime.put(nextTime, new ConcurrentHashMap<>());
            }
        }
        return currentSpawned;
    }

    void calcNext(SpawnedEvent se) {
        int i = 0;
        for (SimpleState s : se.states) {
            Point nextPoint = se.p.add(s);
            SimpleState[] nextStates = nextStates(se, s);
            spawnNewEvent(nextPoint, se.time + s.stateGroup.deltaTime, se.counter + i, nextStates);
            i++;
        }
    }

    SimpleState[] nextStates(SpawnedEvent se, SimpleState s) {
        if (Controls.useRandom) {
            return nextStatesRandom(se, s);
        } else {
            return nextStatesSequential(se, s);
        }
    }

    SimpleState[] nextStatesSequential(SpawnedEvent se, SimpleState s) {
        int abs = se.counter;
        int left;
        int transitionSelect;
        if (Controls.sequence.length > 1) {
            left = abs % Controls.sequence.length;
            transitionSelect = (abs - left) / Controls.sequence.length;
        } else {
            left = 0;
            transitionSelect = abs;
        }
        TransitionMode transitionMode = Controls.sequence[left];
        return getNextSimpleStates(s, transitionMode, transitionSelect);
    }

    SimpleState[] nextStatesRandom(SpawnedEvent se, SimpleState s) {
        // Blocks in order of TransitionMode
        int modeSelect = random.nextInt(transitionRatio.total());
        TransitionMode mode = null;
        int i = 0;
        for (TransitionMode transitionMode : TransitionMode.values()) {
            int ratio = transitionRatio.mapRatio.get(transitionMode);
            if (modeSelect >= i && modeSelect < (i + ratio)) {
                mode = transitionMode;
                break;
            }
            i += ratio;
        }
        if (mode == null) {
            throw new IllegalStateException("Did not find a mode using " + modeSelect + " from " + transitionRatio.mapRatio);
        }
        return getNextSimpleStates(s, mode, random.nextInt(4));
    }

    private SimpleState[] getNextSimpleStates(SimpleState s, TransitionMode transitionMode, int transitionSelect) {
        switch (transitionMode) {
            case transitionFromOriginal: {
                // Always pick transitions from original state
                List<StateTransition> possibles = StateTransition.transitions.get(state);
                return possibles.get(transitionSelect % possibles.size()).next;
            }
            case transitionFromIncoming: {
                // Pick transitions from incoming state
                List<StateTransition> possibles = StateTransition.transitions.get(s);
                return possibles.get(transitionSelect % possibles.size()).next;
            }
            case incomingContinue:
                return new SimpleState[]{s};
            case backToOriginal:
                return new SimpleState[]{state};
        }
        throw new IllegalStateException("Modulo " + Arrays.toString(TransitionMode.values()) + " should return");
    }

    private SpawnedEvent spawnNewEvent(Point np, int time, int counter, SimpleState[] ns) {
        // If the next point was never used continue
        if (/*!currentlyUsed.contains(np) && */!used.containsKey(np)) {
            SpawnedEvent newSe = new SpawnedEvent(this, np, time, counter, ns);
            SpawnedEvent existingSe = currentPerTime.get(time).putIfAbsent(np, newSe);
            if (existingSe != null) {
                if (!existingSe.equals(newSe)) {
                    throw new IllegalStateException("Spawned Event " + newSe + " should be equal to " + existingSe);
                }
                existingSe.add(ns);
                return existingSe;
            } else {
                return newSe;
            }
        }
        return null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SourceEvent that = (SourceEvent) o;

        if (id != that.id) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return (int) (id ^ (id >>> 32));
    }

    @Override
    public String toString() {
        return "se_id=" + id;
    }

    public String fullString() {
        return "SourceEvent{" +
                "id=" + id +
                ", time=" + time +
                ", origin=" + origin +
                ", state=" + state +
                '}';
    }

}
