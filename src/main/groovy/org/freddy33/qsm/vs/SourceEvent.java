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
    final int transitionOverOriginRatio;
    final Point origin;
    final SimpleState state;
    final Map<Point, SpawnedEvent> used = new HashMap<>();
    final Random random;
    Map<Point, SpawnedEvent> current = new ConcurrentHashMap<>();

    public SourceEvent(int time, Point origin, int transitionOverOriginRatio, SimpleState state) {
        this.transitionOverOriginRatio = transitionOverOriginRatio;
        this.id = counter.getAndIncrement();
        this.time = time;
        this.origin = origin;
        this.state = state;
        SpawnedEvent se = new SpawnedEvent(this, origin, 0);
        this.current.put(origin, se);
        se.add(state);
        random = new Random(id + time + origin.hashCode());
    }

    public Map<Point, SpawnedEvent> pollCurrent() {
        used.putAll(current);
        Map<Point, SpawnedEvent> res = current;
        current = new ConcurrentHashMap<>();
        return res;
    }

    Set<SpawnedEvent> calcNext(SpawnedEvent se) {
        Set<SpawnedEvent> res = new HashSet<>(se.states.size() * 3);
        for (SimpleState s : se.states) {
            StateTransition trs = StateTransition.pickOne(s, random, transitionOverOriginRatio, state);
            if (trs == null) {
                // Just make one new state for original state
                spawnNewEvent(se, res, state);
            } else {
                for (SimpleState ns : trs.next) {
                    spawnNewEvent(se, res, ns);
                }
            }
        }
        return res;
    }

    private void spawnNewEvent(SpawnedEvent se, Set<SpawnedEvent> res, SimpleState ns) {
        Point np = se.p.add(ns);
        // If the next point was never used continue
        if (!used.containsKey(np)) {
            SpawnedEvent newSe = new SpawnedEvent(this, np, se.length + 1);
            current.putIfAbsent(np, newSe);
            SpawnedEvent realNewSe = current.get(np);
            if (!realNewSe.equals(newSe)) {
                throw new IllegalStateException("Spawned Event " + newSe + " should be equal to " + realNewSe);
            }
            realNewSe.add(ns);
            res.add(realNewSe);
        }
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
