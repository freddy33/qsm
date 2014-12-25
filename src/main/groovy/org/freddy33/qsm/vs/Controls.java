package org.freddy33.qsm.vs;

/**
 * @author freds on 12/11/14.
 */

enum NextSpawnedMode {
    moveAndSplit, splitAndMove
}

enum NextStateMode {
    random, sequential, incoming
}

public abstract class Controls {
    static boolean info = false;
    static boolean debug = false;
    static boolean blockCurrentlyUsed = false;
    static NextStateMode nextStateMode = NextStateMode.incoming;
    static NextSpawnedMode moveMode = NextSpawnedMode.splitAndMove;
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
