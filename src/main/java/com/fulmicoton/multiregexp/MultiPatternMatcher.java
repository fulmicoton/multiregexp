package com.fulmicoton.multiregexp;

public class MultiPatternMatcher {

    private final int[] NO_MATCH = {};
    private final MultiPatternAutomaton automaton;

    public MultiPatternMatcher(MultiPatternAutomaton automaton) {
        this.automaton = automaton;
    }

    public int[] match(CharSequence s) {
        int p = 0;
        final int l = s.length();
        for (int i = 0; i < l; i++) {
            p = this.automaton.step(p, s.charAt(i));
            if (p == -1) {
                return NO_MATCH;
            }
        }
        return this.automaton.accept[p];
    }

}
