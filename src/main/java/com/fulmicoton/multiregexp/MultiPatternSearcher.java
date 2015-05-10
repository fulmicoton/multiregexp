package com.fulmicoton.multiregexp;

public class MultiPatternSearcher {

    private final MultiPatternAutomaton automaton;
    private final boolean[][] inverseAccept;

    MultiPatternSearcher(final MultiPatternAutomaton automaton) {
        this.automaton = automaton;
        final int nbPatterns = this.automaton.getNbPatterns();
        this.inverseAccept = inverseAccept(automaton.accept, nbPatterns);
    }

    private static boolean[][] inverseAccept(int[][] accept, final int nbPatterns) {
        final int nbStates = accept.length;
        final boolean[][] inverseAccept = new boolean[nbPatterns][];
        for (int patternId=0; patternId<nbPatterns; patternId++) {
            inverseAccept[patternId] = new boolean[nbStates];
        }
        for (int stateId=0; stateId<nbStates; stateId++) {
            final int[] acceptedPatterns = accept[stateId];
            for (int patternId: acceptedPatterns) {
                inverseAccept[patternId][stateId] = true;
            }
        }
        return inverseAccept;
    }


    public Cursor search(CharSequence s) {
        return search(s, 0);
    }

    public Cursor search(CharSequence s, int position) {
        return new Cursor(s, position);
    }

    public class Cursor {
        private final CharSequence seq;
        private int position = 0; //< one char after the last char containing in the match.
        private int matchingPattern = -1;

        Cursor(CharSequence seq, int position) {
            this.seq = seq;
            this.position = position;
        }

        public int match() {
            return this.matchingPattern;
        }

        public boolean found() {
            return this.matchingPattern >= 0;
        }

        public int end() {
            return this.position;
        }

        /* Advances the cursor.
         *
         * When one or more pattern is found, the pattern with the highest
         * priority (== with the lower id) is matched in a half-baked greedy manner :
         *
         * (We munch characters as long as we match the pattern, not as long
         * as the match could be matched
         *
         * e.g:
         *  the pattern (ab)+ search on the string "abab"
         *  will first find the match from [0,2).
         *  ... and stop there as aba does not match.
         *
         *  A second match [2,4) will then be returned on next call to next.)
         *
         * The function then returns true and position holds the offset of what would
         * be the character right after the match.
         *
         * If no match is found the function return false.
         */
        public boolean next() {
            int curState = 0;
            this.matchingPattern = -1;
            final int seqLength = this.seq.length();
            while (this.position < seqLength) {
                curState = automaton.step(curState, this.seq.charAt(this.position++));
                if (automaton.atLeastOneAccept[curState]) {
                    // We found a match!
                    this.matchingPattern = automaton.accept[curState][0];
                    break;
                }
            }
            if (this.matchingPattern == -1) {
                return false;
            }
            //  let's keep advancing as long as we match.
            final boolean[] matchingStates = inverseAccept[this.matchingPattern];
            for (;this.position < seqLength; this.position++) {
                curState = automaton.step(curState, this.seq.charAt(this.position));
                if (!matchingStates[curState]) {
                    return true;
                }
            }
            return true;
        }


    }
}
