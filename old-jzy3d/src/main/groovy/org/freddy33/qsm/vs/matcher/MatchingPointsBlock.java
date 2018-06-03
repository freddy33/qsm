package org.freddy33.qsm.vs.matcher;

import org.freddy33.qsm.vs.base.Matrix3x3;
import org.freddy33.qsm.vs.base.Point;

import java.util.Arrays;

/**
 * 4 Matching of the same block
 */
public class MatchingPointsBlock {
    public final Point[] points;

    MatchingPointsBlock(Point... points) {
        if (points == null || points.length != 4) {
            throw new IllegalArgumentException("There should be 4 points not " + points);
        }
        this.points = points;
    }

    public int volume6() {
        return Math.abs(new Matrix3x3(
                points[0].sub(points[1]),
                points[1].sub(points[2]),
                points[2].sub(points[3])
        ).det());
    }

    @Override
    public String toString() {
        return Arrays.toString(points) + " v=" + volume6() + "\n";
    }
}
