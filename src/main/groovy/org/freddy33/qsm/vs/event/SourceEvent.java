package org.freddy33.qsm.vs.event;

import org.freddy33.qsm.vs.base.Point;
import org.freddy33.qsm.vs.base.SimpleState;
import org.freddy33.qsm.vs.base.StateTransition;
import org.freddy33.qsm.vs.control.Controls;
import org.freddy33.qsm.vs.control.NextSpawnedMode;
import org.freddy33.qsm.vs.selector.common.NextStateSelector;
import org.freddy33.qsm.vs.selector.common.SpawnedEventState;
import org.freddy33.qsm.vs.selector.incoming.NextStateSelectorSimpleIncoming;
import org.freddy33.qsm.vs.selector.incoming.NextStateSelectorTransitionIncoming;
import org.freddy33.qsm.vs.selector.random.NextStateSelectorRandom;
import org.freddy33.qsm.vs.selector.sequential.NextStatePairSelectorSequential;
import org.freddy33.qsm.vs.selector.sequential.NextStateSelectorSequential;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author freds on 12/8/14.
 */
public class SourceEvent {
    final static AtomicInteger counter = new AtomicInteger(0);

    public final int id;
    public final int time;
    public final Point origin;
    public final StateTransition originalState;
    public final StateTransition previousOriginalState;
    public final NextStateSelector nextStateSelector;

    final Map<Point, ReducedSpawnedEvent> used = new HashMap<>();

    // All the point present in currentPerTime after polling the active ones
    Map<Point, Point> currentlyUsed = new ConcurrentHashMap<>();
    // Per time slice list of active spawned events
    Map<Integer, Map<BaseSpawnedEvent, BaseSpawnedEvent>> currentPerTime = new HashMap<>();
    // Per time slice list of old spawned events
    Map<Integer, Map<BaseSpawnedEvent, BaseSpawnedEvent>> usedPerTime = new HashMap<>();

    public SourceEvent(int time, Point origin, StateTransition originalState, StateTransition previousState) {
        this.id = counter.getAndIncrement();
        this.time = time;
        this.origin = origin;
        this.originalState = originalState;
        this.previousOriginalState = previousState;
        switch (Controls.nextStateMode) {
            case random:
                this.nextStateSelector = new NextStateSelectorRandom(originalState.from, id + time + origin.hashCode());
                break;
            case sequential:
                this.nextStateSelector = new NextStateSelectorSequential(originalState.from);
                break;
            case sequentialPair:
                this.nextStateSelector = new NextStatePairSelectorSequential(originalState.from);
                break;
            case incomingSimple:
                this.nextStateSelector = new NextStateSelectorSimpleIncoming(originalState.from);
                break;
            case incomingTransition:
                this.nextStateSelector = new NextStateSelectorTransitionIncoming(originalState.from);
                break;
            default:
                throw new IllegalStateException("Next state mode " + Controls.nextStateMode + " not supported!");
        }
        BaseSpawnedEvent se = new BaseSpawnedEvent(origin, 0, this.nextStateSelector.createOriginalState(originalState, previousState));
        Map<BaseSpawnedEvent, BaseSpawnedEvent> currentSpawned = new HashMap<>(1);
        this.currentPerTime.put(time, currentSpawned);
        currentSpawned.put(se, se);
    }

    public Point getOrigin() {
        return origin;
    }

    public Set<BaseSpawnedEvent> peekCurrent(int currentTime) {
        Map<BaseSpawnedEvent, BaseSpawnedEvent> result = currentPerTime.get(currentTime);
        if (result == null) {
            result = usedPerTime.get(currentTime);
            if (result == null) {
                return new HashSet<>(0);
            }
        }
        return result.keySet();
    }

    public synchronized Set<BaseSpawnedEvent> pollCurrent(int currentTime) {
        Map<BaseSpawnedEvent, BaseSpawnedEvent> currentSpawned = currentPerTime.remove(currentTime);
        if (currentSpawned == null) {
            // Nothing in this event
            return new HashSet<>(0);
        }
        usedPerTime.remove(currentTime - 3);
        usedPerTime.put(currentTime, currentSpawned);
        for (SpawnedEvent event : currentSpawned.keySet()) {
            used.put(event.getPoint(), new ReducedSpawnedEvent((BaseSpawnedEvent)event));
//            used.put(event.getPoint(), null);
        }
        if (Controls.blockCurrentlyUsed) {
            currentlyUsed = new ConcurrentHashMap<>();
            for (Map<BaseSpawnedEvent, BaseSpawnedEvent> eventSet : currentPerTime.values()) {
                eventSet.values().parallelStream().forEach(spawnedEvent -> currentlyUsed.put(spawnedEvent.getPoint(), spawnedEvent.getPoint()));
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

    public void calcNext(BaseSpawnedEvent se) {
        List<SpawnedEventState> spawnedEventStates = this.nextStateSelector.nextSpawnedEvent(se);
        if (Controls.moveMode == NextSpawnedMode.moveAndSplit) {
            SimpleState origSimpleState = se.stateHolder.getSimpleState();
            SpawnedEventState first = spawnedEventStates.get(0);
            if (spawnedEventStates.size() > 1) {
                for (SpawnedEventState state : spawnedEventStates) {
                    first.add(state);
                }
            }
            spawnNewEvent(se.getPoint().add(origSimpleState), se.length + origSimpleState.stateGroup.delta, first);
        } else {
            for (SpawnedEventState spawnedEventState : spawnedEventStates) {
                SimpleState nextState = spawnedEventState.getSimpleState();
                spawnNewEvent(se.getPoint().add(nextState), se.length + nextState.stateGroup.delta, spawnedEventState);
            }
        }
    }

    private SpawnedEvent spawnNewEvent(Point np, int length, SpawnedEventState state) {
        // If the next point was never used continue
        if ((!Controls.blockCurrentlyUsed || !currentlyUsed.containsKey(np)) && !used.containsKey(np)) {
            BaseSpawnedEvent newSe = new BaseSpawnedEvent(np, length, state);
            BaseSpawnedEvent existingSe = currentPerTime.get(time + length).putIfAbsent(newSe, newSe);
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
