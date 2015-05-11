package com.fulmicoton.multiregexp;

import dk.brics.automaton.Automaton;
import dk.brics.automaton.RegExp;
import dk.brics.automaton.RunAutomaton;
import junit.framework.Assert;
import org.junit.Test;

public class MultiPatternSearcherTest {


    @Test
    public void testInverseAutomaton() {
        final RegExp regexp = new RegExp("abc");
        final Automaton forwardAutomaton = regexp.toAutomaton();
        {
            final RunAutomaton runAutomaton = new RunAutomaton(forwardAutomaton);
            Assert.assertFalse(runAutomaton.run("bca"));
            Assert.assertTrue(runAutomaton.run("abc"));
        }
        {
            final Automaton reverseAutomaton = MultiPatternSearcher.inverseAutomaton(forwardAutomaton);
            final RunAutomaton runAutomaton = new RunAutomaton(reverseAutomaton);
            Assert.assertFalse(runAutomaton.run("bca"));
            Assert.assertFalse(runAutomaton.run("abc"));
            Assert.assertTrue(runAutomaton.run("cba"));
        }

    }

}
