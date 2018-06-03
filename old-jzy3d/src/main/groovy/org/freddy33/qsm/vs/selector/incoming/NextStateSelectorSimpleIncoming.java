package org.freddy33.qsm.vs.selector.incoming;

import org.freddy33.qsm.vs.base.SimpleState;
import org.freddy33.qsm.vs.base.StateTransition;
import org.freddy33.qsm.vs.selector.common.SimpleStateTriple;

/**
 * Created by freds on 1/4/15.
 */
public class NextStateSelectorSimpleIncoming extends NextStateSelectorIncoming {
    public NextStateSelectorSimpleIncoming(SimpleState original) {
        super(original);
    }

    @Override
    public StateTransition findTransition(SpawnedEventStateIncoming se, SimpleState s) {
        SimpleStateTriple triple = new SimpleStateTriple(se.parentTransition.from, se.transition.from, s);
        if (triple.isUndecidable()) {
            return se.parentTransition;
        } else {
            return transitionsPerTriple.get(triple);
        }
    }
}
