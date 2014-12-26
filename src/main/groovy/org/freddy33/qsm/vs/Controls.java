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
    static boolean numbersOutput = true;

    static boolean blockCurrentlyUsed = false;
    static NextStateMode nextStateMode = NextStateMode.random;
    static NextSpawnedMode moveMode = NextSpawnedMode.splitAndMove;
    static boolean matchAlsoState = false;
}
