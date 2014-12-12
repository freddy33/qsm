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
    final Map<Point, SpawnedEvent> used = new HashMap<>();
    final Random random;
    final TransitionRatio transitionRatio;
    Map<Point, SpawnedEvent> current = new ConcurrentHashMap<>();

    public SourceEvent(int time, Point origin, SimpleState state) {
        this.id = counter.getAndIncrement();
        this.time = time;
        this.origin = origin;
        this.state = state;
        SpawnedEvent se = new SpawnedEvent(this, origin, 0, state);
        this.current.put(origin, se);
        if (Controls.useRandom) {
            this.random = new Random(id + time + origin.hashCode());
            this.transitionRatio = Controls.defaultRatio;
        } else {
            this.random = null;
            this.transitionRatio = null;
        }
    }

    public Map<Point, SpawnedEvent> pollCurrent() {
        used.putAll(current);
        Map<Point, SpawnedEvent> res = current;
        current = new ConcurrentHashMap<>();
        return res;
    }

    Set<SpawnedEvent> calcNext(SpawnedEvent se) {
        Set<SpawnedEvent> res = new HashSet<>(se.states.size());
        for (SimpleState s : se.states) {
            Point nextPoint = se.p.add(s);
            SimpleState[] nextStates = nextStates(se, s);
            SpawnedEvent nextEvent = spawnNewEvent(nextPoint, se.length + 1, nextStates);
            if (nextEvent != null) {
                res.add(nextEvent);
            }
        }
        return res;
    }

    SimpleState[] nextStates(SpawnedEvent se, SimpleState s) {
        if (Controls.useRandom) {
            return nextStatesRandom(se, s);
        } else {
            return nextStatesSequential(se, s);
        }
    }

    static int[] sequence = new int[]{0, 2, 0, 0};

    SimpleState[] nextStatesSequential(SpawnedEvent se, SimpleState s) {
        int left = se.length % sequence.length;
        int block = (se.length - left) / sequence.length;
        switch (sequence[left]) {
            case 0:
                List<StateTransition> possibles = StateTransition.transitions.get(s);
                return possibles.get(block % possibles.size()).next;
            case 1:
                return new SimpleState[]{s};
            case 2:
                return new SimpleState[]{state};
        }
        throw new IllegalStateException("Modulo 3 should return 0,1,2");
    }

    SimpleState[] nextStatesRandom(SpawnedEvent se, SimpleState s) {
        List<StateTransition> possibles = StateTransition.transitions.get(s);
        int nbPossibles = possibles.size();
        // 3 blocks in order for origin, split, same
        int index = random.nextInt(transitionRatio.total() * nbPossibles);
        if (index < (nbPossibles * transitionRatio.origin)) {
            return new SimpleState[]{state};
        } else if (index >= (nbPossibles * transitionRatio.origin) && index < (nbPossibles * (transitionRatio.origin + transitionRatio.split))) {
            return possibles.get(index % nbPossibles).next;
        } else {
            return new SimpleState[]{s};
        }
    }

    private SpawnedEvent spawnNewEvent(Point np, int length, SimpleState[] ns) {
        // If the next point was never used continue
        if (!used.containsKey(np)) {
            SpawnedEvent newSe = new SpawnedEvent(this, np, length, ns);
            SpawnedEvent existingSe = current.putIfAbsent(np, newSe);
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
