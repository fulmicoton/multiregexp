package com.fulmicoton.multiregexp;

import dk.brics.automaton.Automaton;
import dk.brics.automaton.RegExp;
import dk.brics.automaton.RunAutomaton;
import junit.framework.Assert;
import org.junit.Test;

public class UnicodeTest {

    @Test
    public void testAutomatonWithUnicode() {
        final RegExp regexp = new RegExp("([0-9]{2,4}年)?[0-9]{1,2}月[0-9]{1,2}日");
        final Automaton forwardAutomaton = regexp.toAutomaton();
        {
            final RunAutomaton runAutomaton = new RunAutomaton(forwardAutomaton);
            Assert.assertTrue(runAutomaton.run("1982年9月17日"));
            Assert.assertFalse(runAutomaton.run("1982年9月127日"));
        }
    }
}
