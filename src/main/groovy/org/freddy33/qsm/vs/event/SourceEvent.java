package org.freddy33.qsm.vs.event;

import org.freddy33.qsm.vs.base.Point;
import org.freddy33.qsm.vs.base.SimpleState;
import org.freddy33.qsm.vs.base.StateTransition;
import org.freddy33.qsm.vs.control.Controls;
import org.freddy33.qsm.vs.control.NextSpawnedMode;
import org.freddy33.qsm.vs.selector.common.NextStateSelector;
import org.freddy33.qsm.vs.selector.common.SpawnedEventState;
import org.freddy33.qsm.vs.selector.incoming.NextStateSelectorIncoming;
import org.freddy33.qsm.vs.selector.random.NextStateSelectorRandom;
import org.freddy33.qsm.vs.selector.sequential.NextStateSelectorSequential;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author freds on 12/8/14.
 */
public class SourceEvent {
    final static AtomicInteger counter = new AtomicInteger(0);

    public final int id;
    final int time;
    final Point origin;
    final StateTransition originalState;
    final NextStateSelector nextStateSelector;

    final Map<Point, ReducedSpawnedEvent> used = new HashMap<>();
    // All the point present in currentPerTime after polling the active ones
    Map<Point, Point> currentlyUsed = new ConcurrentHashMap<>();
    // Per time slice list of spawned events
    Map<Integer, Map<SpawnedEvent, SpawnedEvent>> currentPerTime = new HashMap<>();

    public SourceEvent(int time, Point origin, StateTransition originalState, StateTransition previousState) {
        this.id = counter.getAndIncrement();
        this.time = time;
        this.origin = origin;
        this.originalState = originalState;
        switch (Controls.nextStateMode) {
            case random:
                this.nextStateSelector = new NextStateSelectorRandom(originalState.from, id + time + origin.hashCode());
                break;
            case sequential:
                this.nextStateSelector = new NextStateSelectorSequential(originalState.from);
                break;
            case incoming:
                this.nextStateSelector = new NextStateSelectorIncoming(originalState.from);
                break;
            default:
                throw new IllegalStateException("Next state mode " + Controls.nextStateMode + " not supported!");
        }
        SpawnedEvent se = new SpawnedEvent(origin, 0, this.nextStateSelector.createOriginalState(originalState, previousState));
        Map<SpawnedEvent, SpawnedEvent> currentSpawned = new HashMap<>(1);
        this.currentPerTime.put(time, currentSpawned);
        currentSpawned.put(se, se);
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

    public void calcNext(SpawnedEvent se) {
        List<SpawnedEventState> spawnedEventStates = this.nextStateSelector.nextSpawnedEvent(se);
        if (Controls.moveMode == NextSpawnedMode.moveAndSplit) {
            SimpleState origSimpleState = se.stateHolder.getSimpleState();
            SpawnedEventState first = spawnedEventStates.get(0);
            if (spawnedEventStates.size() > 1) {
                for (SpawnedEventState state : spawnedEventStates) {
                    first.add(state);
                }
            }
            spawnNewEvent(se.p.add(origSimpleState), se.length + origSimpleState.stateGroup.delta, first);
        } else {
            for (SpawnedEventState spawnedEventState : spawnedEventStates) {
                SimpleState nextState = spawnedEventState.getSimpleState();
                spawnNewEvent(se.p.add(nextState), se.length + nextState.stateGroup.delta, spawnedEventState);
            }
        }
    }

    private SpawnedEvent spawnNewEvent(Point np, int length, SpawnedEventState state) {
        // If the next point was never used continue
        if ((!Controls.blockCurrentlyUsed || !currentlyUsed.containsKey(np)) && !used.containsKey(np)) {
            SpawnedEvent newSe = new SpawnedEvent(np, length, state);
            SpawnedEvent existingSe = currentPerTime.get(time + length).putIfAbsent(newSe, newSe);
            if (existingSe != null) {
                existingSe.addStates(state);
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
        return id;
    }

    @Override
    public String toString() {
        return "se_id=" + id;
    }

    @SuppressWarnings("UnusedDeclaration")
    public String fullString() {
        return "SourceEvent{" +
                "id=" + id +
                ", time=" + time +
                ", origin=" + origin +
                ", state=" + originalState +
                '}';
    }

}
