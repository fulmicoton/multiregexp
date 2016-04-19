package com.fulmicoton.multiregexp;

import dk.brics.automaton.Automaton;
import dk.brics.automaton.RunAutomaton;
import dk.brics.automaton.State;
import dk.brics.automaton.StatePair;
import dk.brics.automaton.Transition;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MultiPatternSearcher
        implements Serializable {

    private static final long serialVersionUID = -1812442985139693661L;

    private final MultiPatternAutomaton automaton;
    private final List<RunAutomaton> individualAutomatons;
    private final List<RunAutomaton> inverseAutomatons;

    MultiPatternSearcher(final MultiPatternAutomaton automaton,
                         final List<Automaton> individualAutomatons) {
        this(automaton, individualAutomatons, true);
    }

    MultiPatternSearcher(final MultiPatternAutomaton automaton,
                         final List<Automaton> individualAutomatons,
                         boolean tableize) {
        this.automaton = automaton;
        this.individualAutomatons = new ArrayList<>(individualAutomatons.size());
        for (final Automaton individualAutomaton : individualAutomatons) {
            this.individualAutomatons.add(new RunAutomaton(individualAutomaton, tableize));
        }
        this.inverseAutomatons = new ArrayList<>(individualAutomatons.size());
        for (final Automaton individualAutomaton : individualAutomatons) {
            final Automaton inverseAutomaton = inverseAutomaton(individualAutomaton);
            this.inverseAutomatons.add(new RunAutomaton(inverseAutomaton, tableize));
        }
    }

    static Automaton inverseAutomaton(final Automaton automaton) {
        final Map<State, State> stateMapping = new HashMap<>(automaton.getStates().size());
        for (final State state : automaton.getStates()) {
            stateMapping.put(state, new State());
        }
        for (final State state : automaton.getStates()) {
            for (final Transition transition : state.getTransitions()) {
                final State invDest = stateMapping.get(state);
                final State invOrig = stateMapping.get(transition.getDest());
                invOrig.addTransition(new Transition(transition.getMin(), transition.getMax(), invDest));
            }
        }
        final Automaton inverseAutomaton = new Automaton();
        stateMapping.get(automaton.getInitialState()).setAccept(true);
        final State initialState = new State();
        inverseAutomaton.setInitialState(initialState);
        final List<StatePair> epsilons = new ArrayList<>(automaton.getAcceptStates().size());
        for (final State acceptState : automaton.getAcceptStates()) {
            final State invOrigState = stateMapping.get(acceptState);
            final StatePair statePair = new StatePair(initialState, invOrigState);
            epsilons.add(statePair);
        }
        inverseAutomaton.addEpsilons(epsilons);
        return inverseAutomaton;
    }

    public Cursor search(CharSequence s) {
        return search(s, 0);
    }

    public Cursor search(CharSequence s, int position) {
        return new Cursor(s, position);
    }

    public class Cursor {
        private final CharSequence seq;
        private int[] matchingPatterns = null;
        private int[] matchingPatternsStart = null;
        private int[] matchingPatternsEnd = null;
        private int currentPosition = 0;

        Cursor(CharSequence seq, int position) {
            this.seq = seq;
            this.currentPosition = position;
        }

        public int start() {
            return start(0);
        }

        public int start(int patternIndex) {
            if (this.matchingPatterns == null) {
                return -1;
            } else if (this.matchingPatternsStart == null) {
                this.matchingPatternsStart = new int[this.matchingPatterns.length];
                for (int i = 0; i < this.matchingPatterns.length; i++) {
                    this.matchingPatternsStart[i] = -1;
                }
            }

            if (this.matchingPatternsStart[patternIndex] == -1) {
                // we rewind using the backward automaton to find the start of the pattern.
                final RunAutomaton backwardAutomaton = inverseAutomatons.get(this.matchingPatterns[patternIndex]);
                int state = backwardAutomaton.getInitialState();
                for (int pos = this.currentPosition - 1; pos >= 0; pos--) {
                    final char c = this.seq.charAt(pos);
                    state = backwardAutomaton.step(state, c);
                    if (state == -1) {
                        break;
                    }
                    if (backwardAutomaton.isAccept(state)) {
                        this.matchingPatternsStart[patternIndex] = pos;
                    }
                }
            }

            return this.matchingPatternsStart[patternIndex];
        }

        public int end() {
            return end(0);
        }

        public int end(int patternIndex) {
            if (this.matchingPatterns == null) {
                return -1;
            } else if (this.matchingPatternsEnd == null) {
                this.matchingPatternsEnd = new int[this.matchingPatterns.length];
            }

            if (this.matchingPatternsEnd[patternIndex] == 0) {
                final int seqLength = this.seq.length();
                final int start = start(patternIndex);
                // we go forward again using the forward automaton to find the end of the pattern.
                final RunAutomaton forwardAutomaton = individualAutomatons.get(this.matchingPatterns[patternIndex]);
                int state = forwardAutomaton.getInitialState();
                for (int pos = start; pos < seqLength; pos++) {
                    final char c = this.seq.charAt(pos);
                    state = forwardAutomaton.step(state, c);
                    if (state == -1) {
                        break;
                    }
                    if (forwardAutomaton.isAccept(state)) {
                        this.matchingPatternsEnd[patternIndex] = pos + 1;
                    }
                }
            }

            return this.matchingPatternsEnd[patternIndex];
        }


        public int match() {
            return match(0);
        }

        public int match(int patternIndex) {
            return this.matchingPatterns == null ? -1: this.matchingPatterns[patternIndex];
        }

        public String pattern() {
            return pattern(0);
        }

        public String pattern(int patternIndex) {
            final RunAutomaton automaton = individualAutomatons.get(this.matchingPatterns[patternIndex]);
            return automaton.toString();
        }

        public int[] matches() {
            return this.matchingPatterns;
        }

        public boolean found() {
            return this.matchingPatterns != null;
        }


        /* Advances the cursor, to the next match of any pattern.
         * Matches returned cannot overlap.
         *
         * Any ambiguity is solved according to the following method.
         *
         * 1) we advance up to the end of at least one pattern
         * 2) if more than one pattern is found choose the one the highest
         * priority (== lower id)
         * 3) we choose the leftmost possible start for this pattern
         * to match at the end we found.
         * 4) Finally, we extend the pattern as much as possible on the right.
         *
         * The function then returns true and start(), end() will
         * return respectively the starting offset of the pattern.
         * position holds the offset of what would
         * be the character right after the match.
         *
         * If no match is found the function return false.
         */
        public boolean next() {
            this.matchingPatterns = null;
            this.matchingPatternsStart = null;
            this.matchingPatternsEnd = null;
            final int seqLength = this.seq.length();
            // first find a match and "choose the pattern".
            for (int state = 0, pos = this.currentPosition; pos < seqLength; pos++) {
                final char c = this.seq.charAt(pos);
                state = automaton.step(state, c);
                if (automaton.atLeastOneAccept[state]) {
                    // We found a match!
                    this.matchingPatterns = automaton.accept[state];
                    this.currentPosition = pos + 1;
                    break;
                }
            }

            return this.matchingPatterns != null;
        }
    }
}
