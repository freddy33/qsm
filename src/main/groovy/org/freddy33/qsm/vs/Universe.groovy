package org.freddy33.qsm.vs

import groovyx.gpars.GParsPool

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap

class Universe {
    static boolean debug = false
    static boolean stop = false
    int currentTime = 0
    List<SourceEvent> activeEvents = []
    List<SourceEvent> deadEvents = []
    ConcurrentMap<Point, Set<SpawnedEvent>> headEvents = new ConcurrentHashMap<>()

    static {
        StateTransition.verifyAll()
    }

    public static void main(String[] args) {
        def nt = System.nanoTime()
        def uni = new Universe()
        def trSize = 42
        uni.addOriginalEvent(new Point(0, 0, 0), SimpleState.S1)
        uni.addOriginalEvent(new Point(0, -trSize, (int) (1.732 * trSize)), SimpleState.S1)
        uni.addOriginalEvent(new Point(0, -trSize, -(int) (1.732 * trSize)), SimpleState.S1)
        uni.addOriginalEvent(new Point(0, 2 * trSize, 0), SimpleState.S1)
        println uni.activeEvents.size()
        for (int i = 0; i < 400; i++) {
            uni.calcNext()
            println "$i: ${uni.activeEvents.size()} ${uni.headEvents.size()}"
            uni.findNewEvents()
            if (stop) break;
        }
        println "Total calc for ${uni.currentTime} took ${System.nanoTime() - nt}ns"
    }

    void findNewEvents() {
        ConcurrentMap<SourceEvent, Set<MatchingSpawnedEvents>> matchPerSource = new ConcurrentHashMap<>(activeEvents.size())
        GParsPool.withPool {
            headEvents.eachParallel { Point p, Set<SpawnedEvent> ses ->
                // Find if we have 3 identical spawned events (length + state)
                if (ses && ses.size() >= 3) {
                    Map<LengthAndState, MatchingSpawnedEvents> result = new HashMap<>()
                    ses.each { SpawnedEvent se ->
                        se.states.each { SimpleState s ->
                            def key = new LengthAndState(se.length, s)
                            MatchingSpawnedEvents events = result.get(key)
                            if (events == null) {
                                events = new MatchingSpawnedEvents(p, key)
                                result.put(key, events)
                            }
                            events.add(se)
                        }
                    }
                    result.values().each { MatchingSpawnedEvents mse ->
                        if (mse.isValid()) {
                            mse.getSourceInvolved().each {
                                matchPerSource.putIfAbsent(it, new HashSet<MatchingSpawnedEvents>(1))
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
        // Only sources that have more than 3 matching are relevant
        def goodMatches = matchPerSource.findAll { k, v -> v.size() >= 3 }
        // If we get a group of 4 we are good
        if (goodMatches.size() >= 4) {
            goodMatches.each { SourceEvent k, Set<MatchingSpawnedEvents> v ->
                println "For $k found ${v.size()}"
                if (debug) {
                    println "match:\n ${v.collect { "${it.p} ${it.ls}" }.join("\n")}"
                }
            }
            stop = true
        }
    }

    void calcNext() {
        def nt = System.nanoTime()
        headEvents = new ConcurrentHashMap<>((int) ((headEvents.size() + 3) * 2))
        GParsPool.withPool {
            activeEvents.eachParallel { SourceEvent oe ->
                Map<Point, SpawnedEvent> current = oe.pollCurrent()
                GParsPool.withPool {
                    current.eachParallel { Point p, SpawnedEvent nse ->
                        def next = oe.calcNext(nse)
                        next.each {
                            headEvents.putIfAbsent(it.p, new HashSet<SpawnedEvent>(1))
                            def ses = headEvents.get(it.p)
                            synchronized (ses) {
                                ses.add(it)
                            }
                        }
                    }
                }
            }
        }
        if (debug)
            println "calc next of $currentTime took ${System.nanoTime() - nt}ns"
        currentTime++
    }

    /**
     *
     * @param p
     * @param s
     */
    void addOriginalEvent(Point p, SimpleState s) {
        activeEvents.add(new SourceEvent(currentTime, p, s))
    }

}