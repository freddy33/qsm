package org.freddy33.qsm.vs.base;

/**
 * @author freds on 12/2/14.
 */
public enum SimpleState {
    S0(0, 0, 0),
    S1(1, 0, 0),
    S2(0, 1, 0),
    S3(0, 0, 1),
    S4(-1, 0, 0),
    S5(0, -1, 0),
    S6(0, 0, -1),
    S7(1, 1, 0),
    S8(0, 1, 1),
    S9(1, 0, 1),
    S10(-1, 1, 0),
    S11(0, -1, 1),
    S12(-1, 0, 1),
    S13(1, -1, 0),
    S14(0, 1, -1),
    S15(1, 0, -1),
    S16(-1, -1, 0),
    S17(0, -1, -1),
    S18(-1, 0, -1),
    S19(1, 1, 1),
    S20(-1, 1, 1),
    S21(1, -1, 1),
    S22(1, 1, -1),
    S23(-1, -1, 1),
    S24(1, -1, -1),
    S25(-1, 1, -1),
    S26(-1, -1, -1);

    public final SimpleStateGroup stateGroup;
    public final int x, y, z;
    private SimpleState opposite;

    SimpleState(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
        verify();
        // The state group depends on the sum of the abs
        int s = Math.abs(x) + Math.abs(y) + Math.abs(z);
        this.stateGroup = SimpleStateGroup.values()[s];
    }

    public static void verifyAllStates() {
        if (S26.opposite != null) {
            // Already done
            return;
        }
        for (SimpleState state : values()) {
            for (SimpleState found : values()) {
                if (state.x == -found.x && state.y == -found.y && state.z == -found.z) {
                    if (state.opposite != null) {
                        if (state.opposite != found) {
                            throw new IllegalStateException("Found 2 opposites for " + state.fullString() + " and " + found);
                        }
                    } else {
                        state.opposite = found;
                    }
                    if (found.opposite != null) {
                        if (found.opposite != state) {
                            throw new IllegalStateException("Found 2 opposites for " + found.fullString() + " and " + state);
                        }
                    } else {
                        found.opposite = state;
                    }
                    break;
                }
            }
        }
    }

    void verify() {
        if (x > 1 || x < -1) {
            throw new IllegalArgumentException("X " + x + " should be -1, 0, or 1");
        }
        if (y > 1 || y < -1) {
            throw new IllegalArgumentException("Y " + y + " should be -1, 0, or 1");
        }
        if (z > 1 || z < -1) {
            throw new IllegalArgumentException("Z " + z + " should be -1, 0, or 1");
        }
    }

    public SimpleState getOpposite() {
        return opposite;
    }

    public String fullString() {
        return name() +
                "{x=" + x +
                ", y=" + y +
                ", z=" + z +
                ", group=" + stateGroup +
                ", opposite=" + opposite +
                '}';
    }
}
