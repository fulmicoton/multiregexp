package com.fulmicoton.multiregexp;

import org.junit.Assert;
import org.junit.Test;

public class MultiPatternTest {

    public static MultiPattern multiPattern = MultiPattern.of(
            "ab+",     // 0
            "abc+",    // 1
            "ab?c",    // 2
            "v",       // 3
            "v.*",     // 4
            "(def)+"   // 5
    );

    public static final MultiPatternSearcher multiPatternSearcher = multiPattern.searcher();

    public static final MultiPatternMatcher multiPatternMatcher = multiPattern.matcher();

    @Test
    public void testSearch() {
        MultiPatternSearcher.Cursor cursor = multiPatternSearcher.search("ab abc vvv");
        Assert.assertTrue(cursor.next());
        Assert.assertEquals(cursor.match(), 0);
        Assert.assertTrue(cursor.next());
        Assert.assertTrue(cursor.found());
        Assert.assertEquals(cursor.match(), 0);
        Assert.assertTrue(cursor.next());
        Assert.assertEquals(cursor.match(), 3);
        Assert.assertTrue(cursor.next());
        Assert.assertEquals(cursor.match(), 3);
        Assert.assertTrue(cursor.next());
        Assert.assertEquals(cursor.match(), 3);
        Assert.assertFalse(cursor.next());
        Assert.assertEquals(cursor.match(), -1);
        Assert.assertFalse(cursor.found());
    }

    public static void helper(MultiPatternMatcher matcher, String str, int... vals) {
        Assert.assertArrayEquals(vals, matcher.match(str));
    }

    @Test
    public void testString() {
        helper(multiPatternMatcher, "ab", 0);
        helper(multiPatternMatcher, "abc", 1, 2);
        helper(multiPatternMatcher, "ac", 2);
        helper(multiPatternMatcher, "");
        helper(multiPatternMatcher, "v", 3, 4);
        helper(multiPatternMatcher, "defdef", 5);
        helper(multiPatternMatcher, "defde");
        helper(multiPatternMatcher, "abbbbb", 0);
    }

}
