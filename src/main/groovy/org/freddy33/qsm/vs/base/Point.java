package org.freddy33.qsm.vs.base;

/**
 * @author freds on 12/2/14.
 */
public class Point {
    public final int x, y, z;

    public Point(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Point(SimpleState s) {
        this(s.x, s.y, s.z);
    }

    public Point abs() {
        return new Point(Math.abs(x), Math.abs(y), Math.abs(z));
    }

    public boolean allEqualsOrZero() {
        return ((x == y) || (x == 0) || (y == 0))
                && ((x == z) || (x == 0) || (z == 0))
                && ((y == z) || (y == 0) || (z == 0));
    }

    public Point sub(Point p) {
        return new Point(x - p.x, y - p.y, z - p.z);
    }

    public Point add(SimpleState s) {
        return new Point(x + s.x, y + s.y, z + s.z);
    }

    public Point add(SimpleState s, int m) {
        return new Point(x + (m * s.x), y + (m * s.y), z + (m * s.z));
    }

    public Point div(int d) {
        return new Point(x / d, y / d, z / d);
    }

    @Override
    public String toString() {
        return "P{" + x + ", " + y + ", " + z + '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Point point = (Point) o;

        if (x != point.x) return false;
        if (y != point.y) return false;
        if (z != point.z) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = x;
        result = 31 * result + y;
        result = 31 * result + z;
        return result;
    }
}
