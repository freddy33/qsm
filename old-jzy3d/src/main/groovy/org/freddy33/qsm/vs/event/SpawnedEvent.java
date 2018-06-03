package org.freddy33.qsm.vs.event;

import org.freddy33.qsm.vs.base.Point;

/**
 * Created by freds on 1/10/15.
 */
public interface SpawnedEvent {
    Point getPoint();

    int getLength();
}
