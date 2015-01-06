package org.freddy33.qsm.vs.base;

/**
 * @author freds on 1/5/15.
 */
public class Matrix3x3 {
    final int m[][] = new int[3][3];

    public Matrix3x3(Point... points) {
        if (points == null || points.length != 3) {
            throw new IllegalArgumentException("Matrix 3x3 created out of 3 points only");
        }
        for (int j = 0; j < points.length; j++) {
            Point p = points[j];
            m[0][j] = p.x;
            m[1][j] = p.y;
            m[2][j] = p.z;
        }
    }

    public int det() {
        return (m[0][0] * (m[1][1] * m[2][2] - m[1][2] * m[2][1]) -
                m[1][0] * (m[0][1] * m[2][2] - m[0][2] * m[2][1]) +
                m[2][0] * (m[0][1] * m[1][2] - m[0][2] * m[1][1]));
    }
}
