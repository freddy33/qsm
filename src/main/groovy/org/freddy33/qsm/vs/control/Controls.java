package org.freddy33.qsm.vs.control;

public abstract class Controls {
    public static boolean info = false;
    public static boolean debug = false;
    public static boolean numbersOutput = true;

    public static boolean blockCurrentlyUsed = false;
    public static NextStateMode nextStateMode = NextStateMode.incoming;
    public static NextSpawnedMode moveMode = NextSpawnedMode.splitAndMove;
    public static boolean matchAlsoState = false;
}
