package org.freddy33.qsm.vs;

import java.util.EnumMap;

/**
 * @author freds on 12/11/14.
 */
public class TransitionRatio {
    final EnumMap<TransitionMode, Integer> mapRatio;

    public TransitionRatio(int origin, int splitOriginal, int splitIncoming, int same) {
        mapRatio = new EnumMap<>(TransitionMode.class);
        mapRatio.put(TransitionMode.backToOriginal, origin);
        mapRatio.put(TransitionMode.transitionFromOriginal, splitOriginal);
        mapRatio.put(TransitionMode.transitionFromIncoming, splitIncoming);
        mapRatio.put(TransitionMode.incomingContinue, same);
    }

    int total() {
        return mapRatio.values().stream().reduce(0, Integer::sum);
    }
}
