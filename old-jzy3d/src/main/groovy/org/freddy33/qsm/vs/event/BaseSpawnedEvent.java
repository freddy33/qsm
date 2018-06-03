package org.freddy33.qsm.vs.event;

import org.freddy33.qsm.vs.base.Point;
import org.freddy33.qsm.vs.selector.common.SpawnedEventState;

/**
 * @author freds on 12/8/14.
 */
public class BaseSpawnedEvent implements SpawnedEvent {
    public final Point point;
    public final int length;
    public final SpawnedEventState stateHolder;

    public BaseSpawnedEvent(Point point, int length, SpawnedEventState stateHolder) {
        this.point = point;
        this.length = length;
        this.stateHolder = stateHolder;
    }

    @Override
    public Point getPoint() {
        return point;
    }

    @Override
    public int getLength() {
        return length;
    }

    void addStates(SpawnedEventState newStates) {
        this.stateHolder.add(newStates);
    }

    @Override
    public String toString() {
        return "SpawnedEvent{" + point +
                ", l=" + length +
                ", states=" + stateHolder +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BaseSpawnedEvent that = (BaseSpawnedEvent) o;

        if (length != that.length) return false;
        if (!point.equals(that.point)) return false;
        if (!stateHolder.equals(that.stateHolder)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = point.hashCode();
        result = 31 * result + length;
        result = 31 * result + stateHolder.hashCode();
        return result;
    }
}
