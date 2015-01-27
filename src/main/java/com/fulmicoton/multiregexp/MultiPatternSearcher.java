package com.fulmicoton.multiregexp;

public class MultiPatternSearcher {

    private final MultiPatternAutomaton automaton;

    MultiPatternSearcher(MultiPatternAutomaton automaton) {
        this.automaton = automaton;
    }

    public Cursor search(CharSequence s) {
        return search(s, 0);
    }

    public Cursor search(CharSequence s, int position) {
        return new Cursor(s, position);
    }

    public class Cursor {
        private final CharSequence seq;
        private int position = 0;
        private final int seqLength;
        private int[] matches;

        Cursor(CharSequence seq, int position) {
            this.seq = seq;
            this.position = position;
            this.seqLength = seq.length();
            this.next();
        }

        public int[] matches() {
            return this.matches;
        }

        public boolean found() {
            return this.matches != null;
        }

        public int position() {
            return this.position;
        }

        /* Advances the cursor and returns as soon as a pattern is matched.
         *
         * It is not greedy.
         *  The cursor ends at the end of the match.
         * The cursor start is lost (at the moment).
         * Returns a sorted array containing the matched pattern ids.
         */
        public boolean next() {
            int curState = 0;
            while (position < seqLength) {
                curState = automaton.step(curState, this.seq.charAt(position));
                position++;
                if (automaton.atLeastOneAccept[curState]) {
                    this.matches = automaton.accept[curState];
                    return true;
                }
            }
            this.matches = null;
            return false;
        }


    }
}
