package com.fulmicoton.multiregexp;

import org.junit.Assert;
import org.junit.Test;

public class MultiPatternTest {


    public static MultiPattern multiPattern = MultiPattern.compile(
            "ab+",     // 0
            "abc+",    // 1
            "ab?c",    // 2
            "v",       // 3
            "v.*",     // 4
            "(def)+"   // 5
    );


    public static void helper(MultiPattern multipattern, String str, int... vals) {
        Assert.assertArrayEquals(vals, multiPattern.match(str));
    }

    @Test
    public void testString() {
        helper(multiPattern, "ab", 0);
        helper(multiPattern, "abc", 1, 2);
        helper(multiPattern, "ac", 2);
        helper(multiPattern, "");
        helper(multiPattern, "v", 3, 4);
        helper(multiPattern, "defdef", 5);
        helper(multiPattern, "defde");
        helper(multiPattern, "abbbbb", 0);
    }

}
