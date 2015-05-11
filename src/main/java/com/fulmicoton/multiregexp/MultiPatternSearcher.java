package com.fulmicoton.multiregexp;

import dk.brics.automaton.Automaton;
import dk.brics.automaton.RunAutomaton;
import dk.brics.automaton.State;
import dk.brics.automaton.StatePair;
import dk.brics.automaton.Transition;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MultiPatternSearcher {

    private final MultiPatternAutomaton automaton;
    // private final boolean[][] inverseAccept;
    private final List<RunAutomaton> individualAutomatons;
    private final List<RunAutomaton> inverseAutomatons;

    MultiPatternSearcher(final MultiPatternAutomaton automaton,
                         final List<Automaton> individualAutomatons) {
        this.automaton = automaton;
        this.individualAutomatons = new ArrayList<>();
        for (final Automaton individualAutomaton: individualAutomatons) {
            this.individualAutomatons.add(new RunAutomaton(individualAutomaton));
        }
        this.inverseAutomatons = new ArrayList<>(this.individualAutomatons.size());
        for (final Automaton individualAutomaton: individualAutomatons) {
            final Automaton inverseAutomaton = inverseAutomaton(individualAutomaton);
            this.inverseAutomatons.add(new RunAutomaton(inverseAutomaton));
        }
        final int nbPatterns = this.automaton.getNbPatterns();
        // this.inverseAccept = inverseAccept(automaton.accept, nbPatterns);
    }

    static Automaton inverseAutomaton(final Automaton automaton) {
        final Map<State, State> stateMapping = new HashMap<>();
        for (final State state: automaton.getStates()) {
            stateMapping.put(state, new State());
        }
        for (final State state: automaton.getStates()) {
            for (final Transition transition: state.getTransitions()) {
                final State invDest = stateMapping.get(state);
                final State invOrig = stateMapping.get(transition.getDest());
                invOrig.addTransition(new Transition(transition.getMin(), transition.getMax(), invDest));
            }
        }
        final Automaton inverseAutomaton = new Automaton();
        stateMapping.get(automaton.getInitialState()).setAccept(true);
        final State initialState = new State();
        inverseAutomaton.setInitialState(initialState);
        final List<StatePair> epsilons = new ArrayList<>();
        for (final State acceptState: automaton.getAcceptStates()) {
            final State invOrigState = stateMapping.get(acceptState);
            final StatePair statePair = new StatePair(initialState, invOrigState);
            epsilons.add(statePair);
        }
        inverseAutomaton.addEpsilons(epsilons);
        return inverseAutomaton;
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
        private int matchingPattern = -1;
        private int end = 0;
        private int start = -1;

        Cursor(CharSequence seq, int position) {
            this.seq = seq;
            this.end = position;
        }

        public int start() {
            return this.start;
        }

        public int end() {
            return this.end;
        }


        public int match() {
            return this.matchingPattern;
        }

        public boolean found() {
            return this.matchingPattern >= 0;
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
            this.start = -1;
            this.matchingPattern = -1;
            final int seqLength = this.seq.length();
            {
                int state = 0;
                for (int pos=this.end; pos < seqLength; pos++) {
                    final char c = this.seq.charAt(pos);
                    state = automaton.step(state, c);
                    if (automaton.atLeastOneAccept[state]) {
                        // We found a match!
                        this.matchingPattern = automaton.accept[state][0];
                        this.end = pos;
                        break;
                    }
                }
                if (this.matchingPattern == -1) {
                    return false;
                }
            }
            {   // we rewind using the backward automaton to find the start of the pattern.
                final RunAutomaton backwardAutomaton = inverseAutomatons.get(this.matchingPattern);
                int state = backwardAutomaton.getInitialState();
                for (int pos = this.end; pos >= 0; pos--) {
                    final char c = this.seq.charAt(pos);
                    state = backwardAutomaton.step(state, c);
                    if (state == -1) {
                        break;
                    }
                    if (backwardAutomaton.isAccept(state)) {
                        start = pos;
                    }
                }
            }

            {   // we go forward again using the forward automaton to find the end of the pattern.
                final RunAutomaton forwardAutomaton = individualAutomatons.get(this.matchingPattern);
                int state = forwardAutomaton.getInitialState();
                for (int pos = this.start; pos < seqLength; pos++) {
                    final char c = this.seq.charAt(pos);
                    state = forwardAutomaton.step(state, c);
                    if (state == -1) {
                        break;
                    }
                    if (forwardAutomaton.isAccept(state)) {
                        this.end = pos + 1;
                    }
                }
            }

            return true;
        }


    }
}
