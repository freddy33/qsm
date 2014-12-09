package org.freddy33.qsm.vs;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
    Map<Point, SpawnedEvents> current = new ConcurrentHashMap<>();
    final Map<Point, SpawnedEvents> used = new HashMap<>();

    public SourceEvent(int time, Point origin, SimpleState state) {
        this.id = counter.getAndIncrement();
        this.time = time;
        this.origin = origin;
        this.state = state;
        SpawnedEvents se = new SpawnedEvents(this, origin, 0);
        this.current.put(origin, se);
        se.add(state);
    }

    public Map<Point, SpawnedEvents> pollCurrent() {
        used.putAll(current);
        Map<Point, SpawnedEvents> res = current;
        current = new ConcurrentHashMap<>();
        return res;
    }

    List<SpawnedEvents> calcNext(SpawnedEvents se) {
        List<SpawnedEvents> res = new ArrayList<>(se.states.size() * 3);
        for (SimpleState s : se.states) {
            StateTransition trs = StateTransition.pickOne(s);
            for (SimpleState ns : trs.next) {
                Point np = se.p.add(ns);
                // If the next point was never used continue
                if (!used.containsKey(np)) {
                    current.putIfAbsent(np, new SpawnedEvents(this, np, se.length + 1));
                    SpawnedEvents newSe = current.get(np);
                    newSe.add(ns);
                    res.add(newSe);
                }
            }
        }
        return res;
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
