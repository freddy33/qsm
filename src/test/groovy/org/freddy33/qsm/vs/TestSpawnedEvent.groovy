package org.freddy33.qsm.vs

import org.junit.Test

import static org.freddy33.qsm.vs.SimpleState.*
import static org.freddy33.qsm.vs.StateTransition.*
import static org.junit.Assert.*

/**
 * @author freds on 12/11/14.
 */
class TestSpawnedEvent {

    @Test
    public void 'test next state incoming and move and split'() {
        Controls.nextStateMode = NextStateMode.incoming
        Controls.moveMode = NextSpawnedMode.moveAndSplit
        verifyAll()
        def o = new Point(0, 0, 0)
        def source = new SourceEvent(0, o, S1_1)
        SpawnedEvent spawn1 = checkInitialState(source)

        source.calcNext(spawn1)
        def next = source.currentPerTime.get(3);
        assertEquals(1, next.size())
        def spawn2 = next.keySet().iterator().next()
        assertEquals(3, spawn2.length)
        assertEquals(S7, spawn2.stateHolder.simpleState)
        assertEquals(source.origin.add(S1), spawn2.p)
        assertEquals(EnumSet.of(S8, S14, S1), spawn2.stateHolder.states)
        assertEquals(1, source.used.size())
    }

    @Test
    public void 'test next state incoming and split and move'() {
        Controls.nextStateMode = NextStateMode.incoming
        Controls.moveMode = NextSpawnedMode.splitAndMove
        verifyAll()
        def o = new Point(0, 0, 0)
        def source = new SourceEvent(0, o, S1_1)
        SpawnedEvent spawn1 = checkInitialState(source)

        source.calcNext(spawn1)
        def next4 = source.currentPerTime.get(4)
        assertEquals(2, next4.size())

        def it4 = next4.keySet().iterator()
        def spawn_S7 = it4.next()
        def spawn_S9 = it4.next()

        assertEquals(4, spawn_S7.length)
        assertEquals(source.origin.add(S7), spawn_S7.p)
        assertEquals(S7, spawn_S7.stateHolder.simpleState)
        assertEquals(EnumSet.of(S8, S14, S1), spawn_S7.stateHolder.states)
        def stateHolder7 = (SpawnedEventStateIncoming) spawn_S7.stateHolder
        assertEquals(S1_1, stateHolder7.parentTransition)
        assertEquals(S7_4, stateHolder7.transition)

        assertEquals(4, spawn_S9.length)
        assertEquals(source.origin.add(S9), spawn_S9.p)
        assertEquals(S9, spawn_S9.stateHolder.simpleState)
        assertEquals(EnumSet.of(S8, S11, S1), spawn_S9.stateHolder.states)
        def stateHolder9 = (SpawnedEventStateIncoming) spawn_S9.stateHolder
        assertEquals(S1_1, stateHolder9.parentTransition)
        assertEquals(S9_4, stateHolder9.transition)

        def next5 = source.currentPerTime.get(5)
        assertEquals(1, next5.size())

        def it5 = next5.keySet().iterator()
        def spawn_S24 = it5.next()
        assertEquals(5, spawn_S24.length)
        assertEquals(source.origin.add(S24), spawn_S24.p)
        assertEquals(S24, spawn_S24.stateHolder.simpleState)
        assertEquals(EnumSet.of(S1, S5, S6), spawn_S24.stateHolder.states)
        def stateHolder24 = (SpawnedEventStateIncoming) spawn_S24.stateHolder
        assertEquals(S1_1, stateHolder24.parentTransition)
        assertEquals(S24_1, stateHolder24.transition)

        assertEquals(1, source.used.size())
    }

    private SpawnedEvent checkInitialState(SourceEvent source) {
        assertEquals(1, source.currentPerTime.size())
        assertEquals(1, source.currentPerTime.get(0).size())
        def spawn1 = source.currentPerTime.get(0).keySet().iterator().next()
        assertNotNull(spawn1)
        assertEquals(0, spawn1.length)
        assertEquals(S1, spawn1.stateHolder.simpleState)
        assertEquals(source.origin, spawn1.p)
        assertEquals(EnumSet.of(S7, S9, S24), spawn1.stateHolder.states)
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
