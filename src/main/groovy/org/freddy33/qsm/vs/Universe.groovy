package org.freddy33.qsm.vs

import groovyx.gpars.GParsPool

import java.util.concurrent.ConcurrentHashMap

class Universe {
    static boolean debug = true
    static boolean stop = false
    int currentTime = 0
    Set<SourceEvent> activeSourceEvents = new HashSet<>()

    static {
        StateTransition.verifyAll()
    }

    public static void main(String[] args) {
        def nt = System.nanoTime()
        def uni = new Universe()
        def trSize = 60
        uni.addOriginalEvent(new Point(0, 0, 0), SimpleState.S1)
        uni.addOriginalEvent(new Point(0, -trSize, (int) (1.732 * trSize)), SimpleState.S1)
        uni.addOriginalEvent(new Point(0, -trSize, -(int) (1.732 * trSize)), SimpleState.S1)
        uni.addOriginalEvent(new Point(0, 2 * trSize, 0), SimpleState.S1)
        println uni.activeSourceEvents.size()
        for (int i = 0; i < 400; i++) {
            uni.calcNext(uni.findNewEvents())
            if (stop) break;
        }
        printf("Total calc for %d took %,d ns", uni.currentTime, System.nanoTime() - nt)
    }

    Set<SpawnedEvent> findNewEvents() {
        Set<SpawnedEvent> toCalcNext = new HashSet<>()
        Map<Point, Set<SpawnedEvent>> possibleMatch = new ConcurrentHashMap<>()

        Map<SourceEvent, MatchingEventsForSource> matchPerSource = new HashMap<>(activeSourceEvents.size())
        for (SourceEvent sourceEvent : activeSourceEvents) {
            matchPerSource.put(sourceEvent, new MatchingEventsForSource(sourceEvent))
            def current = sourceEvent.pollCurrent(currentTime)
            GParsPool.withPool {
                current.eachParallel { p, se ->
                    possibleMatch.putIfAbsent(p, new HashSet<>())
                    def events = possibleMatch.get(p)
                    synchronized (events) {
                        events.add(se)
                    }
                }
            }
            toCalcNext.addAll(current.values())
        }
        GParsPool.withPool {
            possibleMatch.eachParallel { Point p, Set<SpawnedEvent> ses ->
                // Find if we have 3 identical spawned events (length + state)
                if (ses && ses.size() >= 3) {
                    Map<LengthAndState, MatchingSpawnedEvents> result = new HashMap<>()
                    ses.each { SpawnedEvent se ->
                        se.states.each { SimpleState s ->
                            def key = new LengthAndState(se.time, s)
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
        return toCalcNext
    }

    void calcNext(Set<SpawnedEvent> events) {
        def nt = System.nanoTime()
        GParsPool.withPool {
            events.eachParallel { SpawnedEvent se ->
                se.origin.calcNext(se)
            }
        }
        printf("%d %,d %,d\n", currentTime, events.size(), System.nanoTime() - nt)
        currentTime++
    }

    /**
     *
     * @param p
     * @param s
     */
    void addOriginalEvent(Point p, SimpleState s) {
        activeSourceEvents.add(new SourceEvent(currentTime, p, s))
    }

}