package org.freddy33.qsm.vs

import org.freddy33.qsm.vs.utils.CollectionUtils
import org.junit.Assert
import org.junit.Test

/**
 * @author freds on 12/16/14.
 */
class TestCollectionSystem {

    @Test
    public void testExpectedSubSetsSize() {
        Assert.assertEquals(4, CollectionUtils.expectedNumberOfSets(4, 3))
        Assert.assertEquals(1, CollectionUtils.expectedNumberOfSets(4, 4))
        Assert.assertEquals(15, CollectionUtils.expectedNumberOfSets(6, 4))
        Assert.assertEquals(10, CollectionUtils.expectedNumberOfSets(5, 2))
    }

    @Test
    public void testSubSetsOf3From4() {
        def events = new HashSet<String>(4)
        def find1 = new HashSet<String>(3)
        def find2 = new HashSet<String>(3)
        def find3 = new HashSet<String>(3)
        def find4 = new HashSet<String>(3)
        events.addAll(['A', 'B', 'C', 'D'].toList())
        find1.addAll(['A', 'B', 'C'].toList())
        find2.addAll(['A', 'B', 'D'].toList())
        find3.addAll(['A', 'C', 'D'].toList())
        find4.addAll(['B', 'C', 'D'].toList())
        def sets = CollectionUtils.extractSubSets(events, 3)
        Assert.assertEquals(4, sets.size())
        Assert.assertTrue("Set " + sets + " does not have " + find1, sets.contains(find1))
        Assert.assertTrue("Set " + sets + " does not have " + find2, sets.contains(find2))
        Assert.assertTrue("Set " + sets + " does not have " + find3, sets.contains(find3))
        Assert.assertTrue("Set " + sets + " does not have " + find4, sets.contains(find4))
    }

    @Test
    public void testSubSetsOf4From6() {
        def events = new HashSet<String>(6)
        def find = new ArrayList<Set<String>>(15)
        events.addAll(['A', 'B', 'C', 'D', 'E', 'F'].toList())
        for (int i = 0; i < 15; i++) {
            find.add(new HashSet<String>(4));
        }
        find[0].addAll(['A', 'B', 'C', 'D'].toList())
        find[1].addAll(['A', 'B', 'C', 'E'].toList())
        find[2].addAll(['A', 'B', 'C', 'F'].toList())
        find[3].addAll(['A', 'B', 'D', 'E'].toList())
        find[4].addAll(['A', 'B', 'D', 'F'].toList())
        find[5].addAll(['A', 'B', 'E', 'F'].toList())
        find[6].addAll(['A', 'C', 'D', 'E'].toList())
        find[7].addAll(['A', 'C', 'D', 'F'].toList())
        find[8].addAll(['A', 'C', 'E', 'F'].toList())
        find[9].addAll(['A', 'D', 'E', 'F'].toList())
        find[10].addAll(['B', 'C', 'D', 'E'].toList())
        find[11].addAll(['B', 'C', 'D', 'F'].toList())
        find[12].addAll(['B', 'C', 'E', 'F'].toList())
        find[13].addAll(['B', 'D', 'E', 'F'].toList())
        find[14].addAll(['C', 'D', 'E', 'F'].toList())
        def sets = CollectionUtils.extractSubSets(events, 4)
        Assert.assertEquals(15, sets.size())
        for (Set<String> toFind : find) {
            Assert.assertTrue("Set " + sets + " does not have " + toFind, sets.contains(toFind))
        }
    }

    @Test
    public void testSubSetsOf2From5() {
        def events = new HashSet<String>(5)
        def find = new ArrayList<Set<String>>(10)
        events.addAll(['A', 'B', 'C', 'D', 'E'].toList())
        for (int i = 0; i < 10; i++) {
            find.add(new HashSet<String>(2));
        }
        find[0].addAll(['A', 'B'].toList())
        find[1].addAll(['A', 'C'].toList())
        find[2].addAll(['A', 'D'].toList())
        find[3].addAll(['A', 'E'].toList())
        find[4].addAll(['B', 'C'].toList())
        find[5].addAll(['B', 'D'].toList())
        find[6].addAll(['B', 'E'].toList())
        find[7].addAll(['C', 'D'].toList())
        find[8].addAll(['C', 'E'].toList())
        find[9].addAll(['D', 'E'].toList())
        def sets = CollectionUtils.extractSubSets(events, 2)
        Assert.assertEquals(10, sets.size())
        for (Set<String> toFind : find) {
            Assert.assertTrue("Set " + sets + " does not have " + toFind, sets.contains(toFind))
        }
    }

    @Test
    public void testPossibleFilter() {
        int found = 0
        for (int i = 0; i < 4; i++) {
            if (i == 0 || i == 3) {
                found |= 1 << i
            }
        }
        Assert.assertEquals(9, found)
        found = 0
        for (int i = 0; i < 4; i++) {
            if (i != 0) {
                found |= 1 << i
            }
        }
        Assert.assertEquals(14, found)
    }
}
