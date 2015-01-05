package org.freddy33.qsm.vs.utils;

import java.util.*;

/**
 * @author freds on 12/16/14.
 */
public abstract class CollectionUtils {

    public static <T extends Enum<T>> boolean intersect(EnumSet<T> s1, EnumSet<T> s2) {
        for (T e : s1) {
            if (s2.contains(e)) return true;
        }
        return false;
    }

    public static int expectedNumberOfSets(int setSize, int subSetSize) {
        if (setSize < subSetSize) {
            // Not enough sources
            throw new IllegalArgumentException("Cannot create subsets of size " + subSetSize
                    + " out of set of size " + setSize);
        }
        int expectedNumberOfSets = 1;
        int divider = 1;
        for (int i = 0; i < (setSize - subSetSize); i++) {
            expectedNumberOfSets *= setSize - i;
            divider *= i + 1;
        }
        return expectedNumberOfSets / divider;
    }

    public static <T> Set<Set<T>> extractSubSets(Set<T> initialSet, int subSetSize) {
        int nbSources = initialSet.size();
        int expectedNumberOfSets = expectedNumberOfSets(nbSources, subSetSize);
        Set<Set<T>> setOfSets = new HashSet<>(expectedNumberOfSets);
        if (nbSources == subSetSize) {
            // Already OK
            setOfSets.add(initialSet);
            return setOfSets;
        }
        List<T> setAsList = new ArrayList<>(initialSet);
        int[] iterators = new int[subSetSize];
        for (int i = 0; i < iterators.length; i++) {
            iterators[i] = i;
        }
        while (setOfSets.size() != expectedNumberOfSets) {
            HashSet<T> result = new HashSet<>(subSetSize);
            for (int pos : iterators) {
                result.add(setAsList.get(pos));
            }
            if (result.size() != subSetSize) {
                throw new IllegalStateException("Hard!");
            }
            setOfSets.add(result);
            int maxPos = -1;
            for (int i = 0; i < iterators.length; i++) {
                int pos = iterators[i];
                if (pos == (nbSources - iterators.length + i)) {
                    maxPos = i;
                    break;
                }
            }
            if (maxPos == -1) {
                // Up last iterator
                iterators[iterators.length - 1]++;
            } else if (maxPos == 0) {
                // Finished
                if (setOfSets.size() != expectedNumberOfSets) {
                    System.err.println("Something wrong!");
                }
            } else {
                // Up the one before maxPos and reinit the others
                iterators[maxPos - 1]++;
                for (int i = maxPos; i < iterators.length; i++) {
                    iterators[i] = iterators[i - 1] + 1;
                }
            }
        }
        return setOfSets;
    }
}
