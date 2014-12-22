package org.freddy33.qsm.vs;

/**
 * @author freds on 12/11/14.
 */

enum NextSpawnedMode {
    moveAndSplit, splitAndMove
}

public abstract class Controls {
    static boolean debug = false;
    static boolean useRandom = true;
    static boolean blockCurrentlyUsed = false;
    static NextSpawnedMode nextMode = NextSpawnedMode.splitAndMove;
    static boolean matchAlsoState = false;

    // Random ratios
    static TransitionRatio defaultRatio = new TransitionRatio(0, 1, 3, 0);

    // Non random sequence
    static TransitionMode[] sequence = new TransitionMode[]{
            TransitionMode.transitionFromIncoming,
            TransitionMode.transitionFromIncoming,
            TransitionMode.transitionFromOriginal
    };
}
