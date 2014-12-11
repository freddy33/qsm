package org.freddy33.qsm.vs

import org.junit.Assert
import org.junit.Test

import static org.junit.Assert.assertEquals
import static org.junit.Assert.assertNotNull

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
        Assert.assertEquals(1, source.current.size())
        def spawn1 = source.current.get(o)
        assertNotNull(spawn1)
        assertEquals(0, spawn1.length)
        assertEquals(source, spawn1.origin)
        assertEquals(source.origin, spawn1.p)
        assertEquals(EnumSet.of(SimpleState.S1), spawn1.states)
        assertEquals(0, source.used.size())

        def current = source.pollCurrent()
        assertEquals(1, source.used.size())
        assertEquals(1, current.size())
        assertEquals(0, source.current.size())

        def next = source.calcNext(spawn1)
        assertEquals(1, next.size())
        def spawn2 = next.iterator().next()
        assertEquals(1, spawn2.length)
        assertEquals(source, spawn2.origin)
        assertEquals(source.origin.add(SimpleState.S1), spawn2.p)
        assertEquals(EnumSet.of(SimpleState.S7, SimpleState.S9, SimpleState.S24), spawn2.states)
        assertEquals(1, source.used.size())

    }
}
