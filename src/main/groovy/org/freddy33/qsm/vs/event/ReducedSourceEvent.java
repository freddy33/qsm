package org.freddy33.qsm.vs.event;

import org.freddy33.qsm.vs.base.Point;
import org.freddy33.qsm.vs.base.StateTransition;
import org.freddy33.qsm.vs.matcher.MatchingPointsBlock;
import org.freddy33.qsm.vs.selector.common.NextStateSelector;

/**
 * @author freds on 1/5/15.
 */
public class ReducedSourceEvent {
    public final int id;
    final int time;
    final Point origin;
    final StateTransition originalState;
    final NextStateSelector nextStateSelector;
    final int matchLength;
    final MatchingPointsBlock pointsBlock;

    public ReducedSourceEvent(SourceEvent se, int matchLength, MatchingPointsBlock pointsBlock) {
        this.id = se.id;
        this.time = se.time;
        this.origin = se.origin;
        this.originalState = se.originalState;
        this.nextStateSelector = se.nextStateSelector;
        this.matchLength = matchLength;
        this.pointsBlock = pointsBlock;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ReducedSourceEvent that = (ReducedSourceEvent) o;

        if (id != that.id) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return id;
    }
}
