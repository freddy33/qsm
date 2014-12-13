package org.freddy33.qsm.vs

import groovyx.gpars.GParsPool

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap

class Universe {
    static boolean debug = true
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
        def trSize = 30
        uni.addOriginalEvent(new Point(0, 0, 0), SimpleState.S1)
        uni.addOriginalEvent(new Point(0, -trSize, (int) (1.732 * trSize)), SimpleState.S1)
        uni.addOriginalEvent(new Point(0, -trSize, -(int) (1.732 * trSize)), SimpleState.S1)
        uni.addOriginalEvent(new Point(0, 2 * trSize, 0), SimpleState.S1)
        println uni.activeEvents.size()
        for (int i = 0; i < 400; i++) {
            uni.calcNext()
            uni.findNewEvents()
            if (stop) break;
        }
        printf("Total calc for %d took %,d ns", uni.currentTime, System.nanoTime() - nt)
    }

    void findNewEvents() {
        Map<SourceEvent, MatchingEventsForSource> matchPerSource = new HashMap<>(activeEvents.size())
        for (SourceEvent sourceEvent : activeEvents) {
            matchPerSource.put(sourceEvent, new MatchingEventsForSource(sourceEvent))
        }
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
                            mse.getSourcesInvolved().each {
                                matchPerSource.get(it).add(mse)
                            }
                        }
                    }
                }
            }
        }
        // Only sources that have more than 3 matching are relevant
        def goodMatches = matchPerSource.findAll { k, v -> v.isValid() }
        // If we get a group of 4 we are good
        if (goodMatches.size() >= 4) {
            println "Found more than 3 good match at $currentTime"
            // Order by increasing size to start computing with small matching event sets
            goodMatches = goodMatches.sort { it.value.spawnedEventsPerSourceSet.size() }
            goodMatches.each { SourceEvent k, MatchingEventsForSource v ->                
                if (debug) {
                    println "For $k found ${v.spawnedEventsPerSourceSet.size()} source sets"
                    v.spawnedEventsPerSourceSet.each { Set<SourceEvent> ses, Set<MatchingSpawnedEvents> mse ->
                        println "For $ses got:\n ${mse.collect { "\t${it.p} ${it.ls}" }.join("\n")}"
                    }
                }
                // Trying to create 3 matching spawned event out of 3 other source event
                

            }
            
            stop = true
        }
    }

    void calcNext() {
        def nt = System.nanoTime()
        headEvents = new ConcurrentHashMap<>((int) (headEvents.size() * 1.2))
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
        printf("%d %,d %,d\n", currentTime, headEvents.size(), System.nanoTime() - nt)
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