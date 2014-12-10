package org.freddy33.qsm.vs;

/**
 * @author freds on 12/8/14.
 */
public class LengthAndState {
    final int length;
    final SimpleState st;

    public LengthAndState(int length, SimpleState st) {
        this.length = length;
        this.st = st;
    }

    @Override
    public String toString() {
        return "LS{" + st.name() + " " + length + "}";
    }

    public String fullString() {
        return "LengthAndState{" +
                "length=" + length +
                ", st=" + st +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        LengthAndState that = (LengthAndState) o;

        if (length != that.length) return false;
        if (st != that.st) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = length;
        result = 31 * result + st.hashCode();
        return result;
    }
}
