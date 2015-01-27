package com.fulmicoton.multiregexp;

public class MultiPatternSearcher {

    private final MultiPatternAutomaton automaton;

    MultiPatternSearcher(MultiPatternAutomaton automaton) {
        this.automaton = automaton;
    }

    public Cursor search(CharSequence s) {
        return search(s, 0);
    }

    public Cursor search(CharSequence s, int offset) {
        return new Cursor(s, offset);
    }

    public class Cursor {
        private final CharSequence seq;
        private int cur = 0;
        private final int seqLength;

        Cursor(CharSequence seq, int cur) {
            this.seq = seq;
            this.cur = cur;
            this.seqLength = seq.length();
        }

        /* Advances the cursor and returns as soon as a pattern is matched.
         *
         * It is not greedy.
         *  The cursor ends at the end of the match.
         * The cursor start is lost (at the moment).
         * Returns a sorted array containing the matched pattern ids.
         */
        public int[] next() {
            int curState = 0;
            while (cur < seqLength) {
                curState = automaton.step(curState, this.seq.charAt(cur));
                cur++;
                if (automaton.atLeastOneAccept[curState]) {
                    return automaton.accept[curState];
                }
            }
            return null;
        }


    }
}
