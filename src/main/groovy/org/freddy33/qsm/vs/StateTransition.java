package org.freddy33.qsm.vs;

import java.util.*;

import static org.freddy33.qsm.vs.SimpleState.*;
import static org.freddy33.qsm.vs.SimpleStateGroup.*;

/**
 * @author freds on 12/2/14.
 */
public enum StateTransition {
    S0_0(S0, S0, S0, S0),
    S1_1(S1, S7, S9, S24),
    S1_2(S1, S9, S13, S22),
    S1_3(S1, S13, S15, S19),
    S1_4(S1, S15, S7, S21),
    S2_1(S2, S7, S8, S25),
    S2_2(S2, S8, S10, S22),
    S2_3(S2, S10, S14, S19),
    S2_4(S2, S14, S7, S20),
    S3_1(S3, S8, S9, S23),
    S3_2(S3, S9, S11, S20),
    S3_3(S3, S11, S12, S19),
    S3_4(S3, S12, S8, S21),
    S4_1(S4, S10, S12, S26),
    S4_2(S4, S12, S16, S25),
    S4_3(S4, S16, S18, S20),
    S4_4(S4, S18, S10, S23),
    S5_1(S5, S11, S13, S26),
    S5_2(S5, S13, S17, S23),
    S5_3(S5, S17, S16, S21),
    S5_4(S5, S16, S11, S24),
    S6_1(S6, S14, S15, S26),
    S6_2(S6, S15, S17, S25),
    S6_3(S6, S17, S18, S22),
    S6_4(S6, S18, S14, S24),
    S7_1(S7, S14, S15, S19),
    S7_2(S7, S8, S9, S22),
    S7_3(S7, S9, S15, S2),
    S7_4(S7, S8, S14, S1),
    S8_1(S8, S10, S12, S19),
    S8_2(S8, S7, S9, S20),
    S8_3(S8, S7, S10, S3),
    S8_4(S8, S9, S12, S2),
    S9_1(S9, S13, S11, S19),
    S9_2(S9, S8, S7, S21),
    S9_3(S9, S7, S13, S3),
    S9_4(S9, S8, S11, S1),
    S10_1(S10, S14, S18, S20),
    S10_2(S10, S8, S12, S25),
    S10_3(S10, S12, S18, S2),
    S10_4(S10, S8, S14, S4),
    S11_1(S11, S12, S16, S21),
    S11_2(S11, S9, S13, S23),
    S11_3(S11, S13, S16, S3),
    S11_4(S11, S9, S12, S5),
    S12_1(S12, S11, S16, S20),
    S12_2(S12, S10, S8, S23),
    S12_3(S12, S8, S11, S4),
    S12_4(S12, S10, S16, S3),
    S13_1(S13, S15, S17, S21),
    S13_2(S13, S9, S11, S24),
    S13_3(S13, S11, S17, S1),
    S13_4(S13, S9, S15, S5),
    S14_1(S14, S10, S18, S22),
    S14_2(S14, S7, S15, S25),
    S14_3(S14, S15, S18, S2),
    S14_4(S14, S7, S10, S6),
    S15_1(S15, S13, S17, S22),
    S15_2(S15, S7, S14, S24),
    S15_3(S15, S14, S17, S1),
    S15_4(S15, S7, S13, S6),
    S16_1(S16, S17, S18, S23),
    S16_2(S16, S11, S12, S26),
    S16_3(S16, S11, S17, S4),
    S16_4(S16, S12, S18, S5),
    S17_1(S17, S18, S16, S24),
    S17_2(S17, S15, S13, S26),
    S17_3(S17, S15, S18, S5),
    S17_4(S17, S13, S16, S6),
    S18_1(S18, S17, S16, S25),
    S18_2(S18, S10, S14, S26),
    S18_3(S18, S14, S17, S4),
    S18_4(S18, S10, S16, S6),
    S19_1(S19, S1, S2, S3),
    S19_2(S19, S7, S8, S9),
    S20_1(S20, S4, S2, S3),
    S20_2(S20, S8, S10, S12),
    S21_1(S21, S1, S5, S3),
    S21_2(S21, S9, S13, S11),
    S22_1(S22, S1, S2, S6),
    S22_2(S22, S7, S14, S15),
    S23_1(S23, S4, S5, S3),
    S23_2(S23, S16, S12, S11),
    S24_1(S24, S1, S5, S6),
    S24_2(S24, S15, S13, S17),
    S25_1(S25, S4, S2, S6),
    S25_2(S25, S10, S14, S18),
    S26_1(S26, S4, S5, S6),
    S26_2(S26, S16, S17, S18);

    final static Map<SimpleState, List<StateTransition>> transitions = new HashMap<>(90);
    final SimpleState from;
    final SimpleState[] next;

    StateTransition(SimpleState from, SimpleState... next) {
        if (from == null || next == null) {
            throw new NullPointerException("From and next cannot be null!");
        }
        this.from = from;
        this.next = next;
        verify();
    }

    static void verifyAll() {
        if (!transitions.isEmpty()) {
            return;
        }
        for (StateTransition str : values()) {
            List<StateTransition> fromTrans = transitions.get(str.from);
            if (fromTrans == null) {
                fromTrans = new ArrayList<>(4);
                transitions.put(str.from, fromTrans);
            }
            fromTrans.add(str);
        }
        for (SimpleState ss : SimpleState.values()) {
            int ordinal = ss.ordinal();
            if (ordinal == 0) {
                if (transitions.get(ss).size() != 1) {
                    throw new IllegalStateException("There should be 1 state transition for " + ss);
                }
            }
            if (ordinal >= 1 && ordinal <= 6) {
                if (transitions.get(ss).size() != 4) {
                    throw new IllegalStateException("There should be 4 state transition for " + ss);
                }
            }
        }
        NextStateSelectorIncoming.verifyAll();
    }

    private void checkStates(SimpleStateGroup[] states) {
        for (int i = 0; i < states.length; i++) {
            if (next[i].stateGroup != states[i]) {
                throw new IllegalStateException("Next[" + i + "] of " + name() + " : " + ordinal() + " : " + next[i]
                        + " should be of group " + states[i]);
            }
        }
    }

    void verify() {
        if (next.length != 3) {
            throw new IllegalArgumentException("The number of next state for " + name() + " should be 3!");
        }
        if (from != S0) {
            for (SimpleState ns : next) {
                if (ns == from) {
                    throw new IllegalArgumentException("Next state " + ns.name()
                            + " cannot be equal to from " + from.name()
                            + " in transition " + name());
                }
            }
        }

        // Check the next state groups based on original group
        switch (from.stateGroup) {
            case ZERO:
                checkStates(new SimpleStateGroup[]{ZERO, ZERO, ZERO});
                break;
            case ONE:
                checkStates(new SimpleStateGroup[]{TWO, TWO, THREE});
                break;
            case TWO:
                int r4 = ordinal() % 4;
                if (r4 == 2 || r4 == 1)
                    checkStates(new SimpleStateGroup[]{TWO, TWO, THREE});
                else
                    checkStates(new SimpleStateGroup[]{TWO, TWO, ONE});
                break;
            case THREE:
                if (ordinal() % 2 == 1)
                    checkStates(new SimpleStateGroup[]{ONE, ONE, ONE});
                else
                    checkStates(new SimpleStateGroup[]{TWO, TWO, TWO});
                break;
        }

        // The sum of the next should be a+b+c or a+b+2c
        Point st = new Point(0, 0, 0);
        switch (from.stateGroup) {
            case ZERO:
            case ONE:
            case THREE:
                for (SimpleState ns : next) {
                    st.x += ns.x;
                    st.y += ns.y;
                    st.z += ns.z;
                }
                break;
            case TWO:
                st = st.add(next[0]).add(next[1]).add(next[2], 2);
                break;
        }

        // All abs() of x, y, z should be equal
        Point sta = st.abs();
        if (!sta.allEqualsOrZero()) {
            throw new IllegalArgumentException("The sum of " + toString() + " next sum " + st + " does not have equal abs or zero!");
        }
        int maxAbs = Math.max(Math.max(sta.x, sta.y), sta.z);
        if (maxAbs != 0) {
            // total divided by max abs should be equal to from
            if (!new Point(from).equals(st.div(maxAbs))) {
                throw new IllegalArgumentException("The sum of " + toString() + " next sum " + st + " does not have reduce to " + from);
            }
        }
    }

    @SuppressWarnings("UnusedDeclaration")
    public String fullString() {
        return name() +
                "{from=" + from +
                ", next=" + Arrays.toString(next) +
                '}';
    }
}
