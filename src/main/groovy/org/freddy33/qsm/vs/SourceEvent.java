package org.freddy33.qsm.vs;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author freds on 12/8/14.
 */
public class SourceEvent {
    final static AtomicInteger counter = new AtomicInteger(0);

    final int id;
    final int time;
    final Point origin;
    final SimpleState state;
    final Map<Point, ReducedSpawnedEvent> used = new HashMap<>();
    final Random random;
    final TransitionRatio transitionRatio;
    // All the point present in currentPerTime after polling the active ones
    Map<Point, Point> currentlyUsed = new ConcurrentHashMap<>();
    // Per time slice list of spawned events
    Map<Integer, Map<SpawnedEvent, SpawnedEvent>> currentPerTime = new HashMap<>();

    public SourceEvent(int time, Point origin, SimpleState state) {
        this.id = counter.getAndIncrement();
        this.time = time;
        this.origin = origin;
        this.state = state;
        SpawnedEvent se = new SpawnedEvent(origin, 0, 0, state);
        Map<SpawnedEvent, SpawnedEvent> currentSpawned = new HashMap<>(1);
        this.currentPerTime.put(time, currentSpawned);
        currentSpawned.put(se, se);
        if (Controls.useRandom) {
            this.random = new Random(id + time + origin.hashCode());
            this.transitionRatio = Controls.defaultRatio;
        } else {
            this.random = null;
            this.transitionRatio = null;
        }
    }

    public Set<SpawnedEvent> peekCurrent(int currentTime) {
        return currentPerTime.get(currentTime).keySet();
    }

    public synchronized Set<SpawnedEvent> pollCurrent(int currentTime) {
        Map<SpawnedEvent, SpawnedEvent> currentSpawned = currentPerTime.remove(currentTime);
        if (currentSpawned == null) {
            // There should always be something at the time
            throw new IllegalStateException("Asking for states at time " + currentTime + " return null data!");
        }
        for (SpawnedEvent event : currentSpawned.keySet()) {
            used.put(event.p, new ReducedSpawnedEvent(event));
        }
        if (Controls.blockCurrentlyUsed) {
            currentlyUsed = new ConcurrentHashMap<>();
            for (Map<SpawnedEvent, SpawnedEvent> eventSet : currentPerTime.values()) {
                eventSet.values().parallelStream().forEach(spawnedEvent -> {
                    currentlyUsed.put(spawnedEvent.p, spawnedEvent.p);
                });
            }
        }
        for (int i = 1; i < 6; i++) {
            int nextTime = currentTime + i;
            if (!currentPerTime.containsKey(nextTime)) {
                currentPerTime.put(nextTime, new ConcurrentHashMap<>());
            }
        }
        return currentSpawned.keySet();
    }

    void calcNext(SpawnedEvent se) {
        int i = 1;
        for (SimpleState s : se.states) {
            SimpleState[] nextStates = nextStates(se, s);
            if (Controls.nextMode == NextSpawnedMode.moveAndSplit) {
                spawnNewEvent(se.p.add(s), se.length + s.stateGroup.delta, se.counter + i, nextStates);
            } else if (Controls.nextMode == NextSpawnedMode.splitAndMove) {
                int j = 0;
                for (SimpleState nextState : nextStates) {
                    spawnNewEvent(se.p.add(nextState), se.length + nextState.stateGroup.delta, se.counter + i + j, nextState);
                    j++;
                }
            } else {
                throw new IllegalStateException("Next mode " + Controls.nextMode + " not supported!");
            }
            i++;
        }
    }

    private SpawnedEvent spawnNewEvent(Point np, int length, int counter, SimpleState... ns) {
        // If the next point was never used continue
        if ((!Controls.blockCurrentlyUsed || !currentlyUsed.containsKey(np)) && !used.containsKey(np)) {
            SpawnedEvent newSe = new SpawnedEvent(np, length, counter, ns);
            SpawnedEvent existingSe = currentPerTime.get(time + length).putIfAbsent(newSe, newSe);
            if (existingSe != null) {
                existingSe.add(ns);
                return existingSe;
            } else {
                return newSe;
            }
        }
        return null;
    }

    SimpleState[] nextStates(SpawnedEvent se, SimpleState s) {
        if (Controls.useRandom) {
            return nextStatesRandom(s);
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

    SimpleState[] nextStatesRandom(SimpleState s) {
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
        return id;
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
