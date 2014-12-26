package org.freddy33.qsm.vs

import org.junit.Assert
import org.junit.Test

import static org.freddy33.qsm.vs.SimpleState.*
import static org.freddy33.qsm.vs.StateTransition.S1_1

/**
 * @author freds on 12/16/14.
 */
class TestCollectionSystem {
    private Point origin = new Point(0, 0, 0)

    @Test
    public void testSubSets() {
        def events = new HashSet<SourceEvent>(4);
        def so1 = new SourceEvent(0, origin, S1_1)
        def so2 = new SourceEvent(0, origin.add(S1), S1_1)
        def so3 = new SourceEvent(0, origin.add(S15), S1_1)
        def so4 = new SourceEvent(0, origin.add(S22), S1_1)
        events << so1 << so2 << so3 << so4
        def sets = CollectionUtils.extractSubSets(events, 3)
        Assert.assertEquals(4, sets.size())
    }

    @Test
    public void testPossibleFilter() {
        int found = 0
        for (int i = 0; i < 4; i++) {
            if (i == 0 || i == 3) {
                found |= 1 << i
            }
        }
        Assert.assertEquals(9, found)
        found = 0
        for (int i = 0; i < 4; i++) {
            if (i != 0) {
                found |= 1 << i
            }
        }
        Assert.assertEquals(14, found)
    }
}
