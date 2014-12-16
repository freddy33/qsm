package org.freddy33.qsm.vs;

/**
 * @author freds on 12/9/14.
 */
public class MatchingLengthAndStateSpawnedEvents extends BaseMatchingSpawnedEvents {
    final SimpleState state;

    public MatchingLengthAndStateSpawnedEvents(Point p, int length, SimpleState state) {
        super(p, length);
        this.state = state;
    }

    @Override
    public void add(SourceEvent sourceEvent, SpawnedEvent se) {
        if (!se.states.contains(state)) {
            throw new IllegalArgumentException("Spawned Event " + se + " does not contain state " + state);
        }
        super.add(sourceEvent, se);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MatchingLengthAndStateSpawnedEvents that = (MatchingLengthAndStateSpawnedEvents) o;

        if (length != that.length) return false;
        if (!p.equals(that.p)) return false;
        if (!state.equals(that.state)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = p.hashCode();
        result = 31 * result + length;
        result = 31 * result + state.hashCode();
        return result;
    }
}
