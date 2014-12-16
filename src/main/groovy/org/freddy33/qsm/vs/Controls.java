package org.freddy33.qsm.vs;

/**
 * @author freds on 12/11/14.
 */
public abstract class Controls {
    static boolean useRandom = false;
    static boolean blockCurrentlyUsed = false;
    
    // Random ratios
    static TransitionRatio defaultRatio = new TransitionRatio(1, 3, 9, 0);

    // Non random sequence
    static TransitionMode[] sequence = new TransitionMode[]{
            TransitionMode.transitionFromIncoming,
            TransitionMode.transitionFromIncoming,
            TransitionMode.transitionFromOriginal,
            TransitionMode.transitionFromIncoming,
            TransitionMode.transitionFromIncoming,
            TransitionMode.transitionFromOriginal,
            TransitionMode.transitionFromIncoming,
            TransitionMode.transitionFromIncoming,
            TransitionMode.backToOriginal
    };
}
