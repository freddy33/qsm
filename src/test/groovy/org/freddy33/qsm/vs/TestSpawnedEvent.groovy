package org.freddy33.qsm.vs

import org.junit.Assert
import org.junit.Test

import static org.junit.Assert.*

/**
 * @author freds on 12/11/14.
 */
class TestSpawnedEvent {

    @Test
    public void nextFlowTest() {
        Controls.useRandom = false
        StateTransition.verifyAll()
        def o = new Point(0, 0, 0)
        def source = new SourceEvent(0, o, SimpleState.S1)
        Assert.assertEquals(1, source.currentPerTime.size())
        Assert.assertEquals(1, source.currentPerTime.get(0).size())
        def spawn1 = source.currentPerTime.get(0).get(o)
        assertNotNull(spawn1)
        assertEquals(0, spawn1.time)
        assertEquals(source, spawn1.origin)
        assertEquals(source.origin, spawn1.p)
        assertEquals(EnumSet.of(SimpleState.S1), spawn1.states)
        assertEquals(0, source.used.size())

        def current = source.pollCurrent(0)
        assertEquals(1, source.used.size())
        assertEquals(1, current.size())
        assertNull(source.currentPerTime.get(0))
        assertEquals(0, source.currentlyUsed.size())
        assertEquals(3, source.currentPerTime.size())
        for (Map m : source.currentPerTime.values()) {
            assertEquals(0, m.size())
        }

        source.calcNext(spawn1)
        def next = source.currentPerTime.get(3);
        assertEquals(1, next.size())
        def spawn2 = next.get(o.add(SimpleState.S1))
        assertEquals(3, spawn2.time)
        assertEquals(source, spawn2.origin)
        assertEquals(source.origin.add(SimpleState.S1), spawn2.p)
        assertEquals(EnumSet.of(SimpleState.S7, SimpleState.S9, SimpleState.S24), spawn2.states)
        assertEquals(1, source.used.size())
    }
}
