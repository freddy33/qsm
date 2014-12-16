package org.freddy33.qsm.vs;

/**
 * @author freds on 12/11/14.
 */

enum NextSpawnedMode {
    moveAndSplit, splitAndMove
}

public abstract class Controls {
    static boolean useRandom = false;
    static boolean blockCurrentlyUsed = true;
    static NextSpawnedMode nextMode = NextSpawnedMode.moveAndSplit;
    static boolean matchAlsoState = false;

    // Random ratios
    static TransitionRatio defaultRatio = new TransitionRatio(1, 3, 9, 0);

    // Non random sequence
    static TransitionMode[] sequence = new TransitionMode[]{
            TransitionMode.transitionFromIncoming
    };
}
