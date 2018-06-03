package org.freddy33.qsm.vs.matcher;

import org.freddy33.qsm.vs.event.SourceEvent;
import org.freddy33.qsm.vs.utils.CollectionUtils;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * @author freds on 1/5/15.
 */
public class MatchingSourcesBlock {
    public final Set<SourceEvent> sourceEvents;
    public final MatchingPointsBlock original;
    public final int[][] idIndex = new int[4][3];
    public final MatchingPointsBlock[] combinations;
    private int matchingLength;

    public MatchingSourcesBlock(Set<SourceEvent> sourceEvents,
                                Map<Set<SourceEvent>, Set<MatchingSpawnedEvents>> matchPerSourceCollection) {
        if (sourceEvents.size() != 4) {
            throw new IllegalArgumentException("Block of 4 only supported");
        }
        this.sourceEvents = sourceEvents;
        Iterator<SourceEvent> srcIt = sourceEvents.iterator();
        this.original = new MatchingPointsBlock(
                srcIt.next().getOrigin(),
                srcIt.next().getOrigin(),
                srcIt.next().getOrigin(),
                srcIt.next().getOrigin()
        );
        Set<Set<SourceEvent>> sets = CollectionUtils.extractSubSets(sourceEvents, 3);
        MatchingSpawnedEvents[][] toSelectFrom = new MatchingSpawnedEvents[4][];
        int[] indexes = new int[4];
        int nbCombinations = 1;
        int i = 0;
        for (Set<SourceEvent> set : sets) {
            Set<MatchingSpawnedEvents> spawnedEventsSet = matchPerSourceCollection.get(set);
            int j = 0;
            for (SourceEvent event : set) {
                idIndex[i][j++] = event.id;
            }
            if (spawnedEventsSet == null) {
                nbCombinations = 0;
            } else {
                int size = spawnedEventsSet.size();
                nbCombinations *= size;
                indexes[i] = 0;
                toSelectFrom[i] = spawnedEventsSet.toArray(new MatchingSpawnedEvents[size]);
                // TODO: Double check all same length
                matchingLength = toSelectFrom[i][0].getLength();
            }
            i++;
        }
        this.combinations = new MatchingPointsBlock[nbCombinations];
        for (int j = 0; j < nbCombinations; j++) {
            combinations[j] = new MatchingPointsBlock(
                    toSelectFrom[0][indexes[0]].getPoint(),
                    toSelectFrom[1][indexes[1]].getPoint(),
                    toSelectFrom[2][indexes[2]].getPoint(),
                    toSelectFrom[3][indexes[3]].getPoint()
            );
            boolean increased = false;
            for (int k = 0; k < 4; k++) {
                indexes[k]++;
                if (indexes[k] == toSelectFrom[k].length) {
                    indexes[k] = 0;
                } else {
                    increased = true;
                    break;
                }
            }
            if (!increased && j != nbCombinations - 1) {
                System.err.println("Miscalculated!");
            }
        }
    }

    public int getMatchingLength() {
        return matchingLength;
    }

    public boolean isValid() {
        return combinations.length != 0;
    }

    public MatchingPointsBlock getBest() {
        MatchingPointsBlock result = null;
        int currentDiff = Integer.MAX_VALUE;
        for (MatchingPointsBlock pointBlock : combinations) {
            int diff = Math.abs(pointBlock.volume6() - original.volume6());
            if (diff < currentDiff) {
                currentDiff = diff;
                result = pointBlock;
            }
        }
        return result;
    }

    @Override
    public String toString() {
        return "MatchingBlock{\n" + sourceEvents +
                "\n" + original + " v=" + original.volume6() +
                "\n" + Arrays.toString(combinations) +
                '}';
    }

    public int getOppositeIdx(int seId) {
        for (int i = 0; i < idIndex.length; i++) {
            int[] ids = idIndex[i];
            boolean isOpposite = true;
            for (int id : ids) {
                if (id == seId) {
                    isOpposite = false;
                }
            }
            if (isOpposite) {
                return i;
            }
        }
        throw new IllegalStateException("Did not find any opposite of " + seId + " in " + Arrays.toString(idIndex));
    }
}

