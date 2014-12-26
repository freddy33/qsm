package org.freddy33.qsm.vs;

/**
 * @author freds on 12/8/14.
 */
public class SpawnedEvent {
    final Point p;
    final int length;
    final SpawnedEventState stateHolder;

    public SpawnedEvent(Point p, int length, SpawnedEventState stateHolder) {
        this.p = p;
        this.length = length;
        this.stateHolder = stateHolder;
    }

    void addStates(SpawnedEventState newStates) {
        this.stateHolder.add(newStates);
    }

    @Override
    public String toString() {
        return "SpawnedEvent{" + p +
                ", l=" + length +
                ", states=" + stateHolder +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SpawnedEvent that = (SpawnedEvent) o;

        if (length != that.length) return false;
        if (!p.equals(that.p)) return false;
        if (!stateHolder.equals(that.stateHolder)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = p.hashCode();
        result = 31 * result + length;
        result = 31 * result + stateHolder.hashCode();
        return result;
    }
}
