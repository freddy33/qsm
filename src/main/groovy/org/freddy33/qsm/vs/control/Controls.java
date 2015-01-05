package org.freddy33.qsm.vs.control;

import org.freddy33.qsm.vs.selector.common.NextStateMode;

public abstract class Controls {
    public static boolean info = false;
    public static boolean debug = false;
    public static boolean numbersOutput = true;

    public static boolean blockCurrentlyUsed = true;
    public static NextStateMode nextStateMode = NextStateMode.incomingTransition;
    public static NextSpawnedMode moveMode = NextSpawnedMode.splitAndMove;
    public static boolean matchAlsoState = false;
}
