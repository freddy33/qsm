package org.freddy33.qsm.vs.utils;

import org.freddy33.qsm.vs.event.SourceEvent;

import java.util.HashSet;
import java.util.Set;

/**
 * @author freds on 12/16/14.
 */
public abstract class CollectionUtils {

    public static Set<Set<SourceEvent>> extractSubSets(Set<SourceEvent> sourcesInvolved, int subSetSize) {
        Set<Set<SourceEvent>> setOfSets = new HashSet<>(4);
        int nbSources = sourcesInvolved.size();
        if (nbSources < subSetSize) {
            // Not enough sources
            return setOfSets;
        }
        if (nbSources == subSetSize) {
            // Already OK
            setOfSets.add(sourcesInvolved);
            return setOfSets;
        }
        SourceEvent[] sourceEvents = sourcesInvolved.toArray(new SourceEvent[nbSources]);
        int first = 0;
        while (setOfSets.size() != 4) {
            HashSet<SourceEvent> result = new HashSet<>(3);
            result.add(sourceEvents[first]);
            int second = first + 1;
            if (second == nbSources) {
                second = 0;
            }
            int third = second + 1;
            if (third == nbSources) {
                third = 0;
            }
            result.add(sourceEvents[second]);
            result.add(sourceEvents[third]);
            if (result.size() != 3) {
                throw new IllegalStateException("Hard!");
            }
            setOfSets.add(result);
            first++;
        }
        return setOfSets;
    }
}
