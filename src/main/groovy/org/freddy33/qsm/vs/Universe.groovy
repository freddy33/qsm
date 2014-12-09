package org.freddy33.qsm.vs

import groovyx.gpars.GParsPool

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap

class Universe {
    int currentTime = 0
    List<SourceEvent> activeEvents = []
    List<SourceEvent> deadEvents = []
    ConcurrentMap<Point, List<SpawnedEvents>> headEvents = new ConcurrentHashMap<>()

    static {
        StateTransition.verifyAll()
    }

    public static void main(String[] args) {
        def nt = System.nanoTime()
        def uni = new Universe()
        uni.addOriginalEvent(new Point(0, 0, 0), SimpleState.S1)
        uni.addOriginalEvent(new Point(0, -10, 10), SimpleState.S1)
        uni.addOriginalEvent(new Point(0, -10, -10), SimpleState.S1)
        uni.addOriginalEvent(new Point(0, 22, 0), SimpleState.S1)
        println uni.activeEvents.size()
        for (int i = 0; i < 12; i++) {
            uni.calcNext()
            println uni.activeEvents.size()
            println uni.headEvents.size()
            uni.findNewEvents()
        }
        println "Total calc for ${uni.currentTime} took ${System.nanoTime() - nt}ns"
    }

    void findNewEvents() {
        ConcurrentMap<SourceEvent, List<MatchingSpawnedEvents>> matchPerSource = new ConcurrentHashMap<>(activeEvents.size())
        GParsPool.withPool {
            headEvents.eachParallel { Point p, List<SpawnedEvents> ses ->
                // Find if we have 3 identical spawned events (length + state)
                if (ses && ses.size() >= 3) {
                    Map<LengthAndState, MatchingSpawnedEvents> result = new HashMap<>()
                    ses.each { SpawnedEvents se ->
                        se.states.each { SimpleState s ->
                            def key = new LengthAndState(se.length, s)
                            MatchingSpawnedEvents events = result.get(key)
                            if (events == null) {
                                events = new MatchingSpawnedEvents(key)
                                result.put(key, events)
                            }
                            events.add(se)
                        }
                    }
                    result.values().each { MatchingSpawnedEvents mse ->
                        if (mse.isValid()) {
                            mse.getSourceInvolved().each {
                                matchPerSource.putIfAbsent(it, new ArrayList<MatchingSpawnedEvents>(1))
                                def lmse = matchPerSource.get(it)
                                synchronized (lmse) {
                                    lmse.add(mse)
                                }
                            }
                        }
                    }
                }
            }
        }
        matchPerSource.each { SourceEvent k, List<MatchingSpawnedEvents> v ->
            println "Found ${v.size()} match for $k"
        }
    }

    void calcNext() {
        def nt = System.nanoTime()
        headEvents = new ConcurrentHashMap<>((int) ((headEvents.size() + 3) * 2))
        GParsPool.withPool {
            activeEvents.eachParallel { SourceEvent oe ->
                Map<Point, SpawnedEvents> current = oe.pollCurrent()
                GParsPool.withPool {
                    current.eachParallel { Point p, SpawnedEvents nse ->
                        def next = oe.calcNext(nse)
                        next.each {
                            headEvents.putIfAbsent(it.p, new ArrayList<SpawnedEvents>(1))
                            def ses = headEvents.get(it.p)
                            synchronized (ses) {
                                ses.add(it)
                            }
                        }
                    }
                }
            }
        }
        println "calc next of $currentTime took ${System.nanoTime() - nt}ns"
        currentTime++
    }

    void addOriginalEvent(Point p, SimpleState s) {
        activeEvents.add(new SourceEvent(currentTime, p, s))
    }

}