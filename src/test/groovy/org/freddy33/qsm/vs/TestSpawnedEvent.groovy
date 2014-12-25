package org.freddy33.qsm.vs

import org.junit.Test

import static org.freddy33.qsm.vs.SimpleState.*
import static org.junit.Assert.*

/**
 * @author freds on 12/11/14.
 */
class TestSpawnedEvent {

    @Test
    public void 'test next state incoming and move and split'() {
        Controls.nextStateMode = NextStateMode.incoming
        Controls.moveMode = NextSpawnedMode.moveAndSplit
        StateTransition.verifyAll()
        def o = new Point(0, 0, 0)
        def source = new SourceEvent(0, o, S24, S1)
        SpawnedEvent spawn1 = checkInitialState(source)

        source.calcNext(spawn1)
        def next = source.currentPerTime.get(3);
        assertEquals(1, next.size())
        def spawn2 = next.keySet().iterator().next()
        assertEquals(3, spawn2.length)
        assertEquals(S1, spawn2.from)
        assertEquals(source.origin.add(S1), spawn2.p)
        assertEquals(EnumSet.of(S7, S9, S24), spawn2.states)
        assertEquals(1, source.used.size())
    }

    @Test
    public void 'test next state incoming and split and move'() {
        Controls.nextStateMode = NextStateMode.incoming
        Controls.moveMode = NextSpawnedMode.splitAndMove
        StateTransition.verifyAll()
        def o = new Point(0, 0, 0)
        def source = new SourceEvent(0, o, S24, S1)
        SpawnedEvent spawn1 = checkInitialState(source)

        source.calcNext(spawn1)
        def next4 = source.currentPerTime.get(4)
        assertEquals(2, next4.size())

        def it4 = next4.keySet().iterator()
        def spawn9 = it4.next()
        def spawn7 = it4.next()
        assertEquals(4, spawn9.length)
        assertEquals(S1, spawn9.from)
        assertEquals(source.origin.add(S9), spawn9.p)
        assertEquals(EnumSet.of(S9), spawn9.states)
        assertEquals(4, spawn7.length)
        assertEquals(S1, spawn7.from)
        assertEquals(source.origin.add(S7), spawn7.p)
        assertEquals(EnumSet.of(S7), spawn7.states)

        def next5 = source.currentPerTime.get(5)
        assertEquals(1, next5.size())

        def it5 = next5.keySet().iterator()
        def spawn24 = it5.next()
        assertEquals(5, spawn24.length)
        assertEquals(S1, spawn24.from)
        assertEquals(source.origin.add(S24), spawn24.p)
        assertEquals(EnumSet.of(S24), spawn24.states)

        assertEquals(1, source.used.size())
    }

    private SpawnedEvent checkInitialState(SourceEvent source) {
        assertEquals(1, source.currentPerTime.size())
        assertEquals(1, source.currentPerTime.get(0).size())
        def spawn1 = source.currentPerTime.get(0).keySet().iterator().next()
        assertNotNull(spawn1)
        assertEquals(0, spawn1.length)
        assertEquals(S24, spawn1.from)
        assertEquals(source.origin, spawn1.p)
        assertEquals(EnumSet.of(S1), spawn1.states)
        assertEquals(0, source.used.size())

        def current = source.pollCurrent(0)
        assertEquals(1, source.used.size())
        assertEquals(1, current.size())
        assertNull(source.currentPerTime.get(0))
        assertEquals(0, source.currentlyUsed.size())
        assertEquals(5, source.currentPerTime.size())
        for (Map m : source.currentPerTime.values()) {
            assertEquals(0, m.size())
        }
        spawn1
    }
}
