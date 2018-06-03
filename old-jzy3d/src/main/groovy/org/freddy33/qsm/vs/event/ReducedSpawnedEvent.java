package org.freddy33.qsm.vs.event;

import org.freddy33.qsm.vs.base.Point;
import org.freddy33.qsm.vs.selector.common.SpawnedEventState;

/**
 * @author freds on 12/8/14.
 */
public class ReducedSpawnedEvent implements SpawnedEvent {
    public final Point point;
    public final int length;
    public final SpawnedEventState state;

    public ReducedSpawnedEvent(BaseSpawnedEvent se) {
        this.point = se.point;
        this.length = se.length;
        this.state = se.stateHolder;
    }

    public Point getPoint() {
        return point;
    }

    public int getLength() {
        return length;
    }
}
