package com.fulmicoton.multiregexp;

import dk.brics.automaton.Automaton;
import dk.brics.automaton.DkBricsAutomatonHelper;
import dk.brics.automaton.RegExp;
import dk.brics.automaton.State;
import dk.brics.automaton.StatePair;

import java.util.*;

public class MultiPattern {

    private static final int[] NO_MATCH = new int[0];
    private final int[][] accept;
    private final int[] transitions;
    private final char[] points;
    private final int[] alphabet;
    private final boolean[] atLeastOneAccept;

    private MultiPattern(final int[][] accept, final int[] transitions, final char[] points) {
        this.accept = accept;
        this.transitions = transitions;
        this.points = points;
        this.alphabet = alphabet(points);
        this.atLeastOneAccept = new boolean[accept.length];
        for (int i=0; i<accept.length; i++) {
            this.atLeastOneAccept[i] = (this.accept[i].length > 0);
        }
    }

    boolean isAtLeastOneAccept(int p) {
        return this.atLeastOneAccept[p];
    }

    int[] acceptedPatterns(int p) {
        return this.accept[p];
    }

    public int alphabetSize() {
        return this.points.length;
    }

    public int nbStates() {
        return this.accept.length;
    }

    static int[] alphabet(final char[] points) {
        final int[] alphabet = new int[Character.MAX_VALUE - Character.MIN_VALUE + 1];
        int i = 0;
        for (int j = 0; j <= (Character.MAX_VALUE - Character.MIN_VALUE); j++) {
            if (i + 1 < points.length && j == points[i + 1])
                i++;
            alphabet[j] = i;
        }
        return alphabet;
    }

    static MultiState initialState(List<Automaton> automata) {

        final State[] initialStates = new State[automata.size()];
        int c = 0;
        for (final Automaton automaton: automata) {
            initialStates[c] = automaton.getInitialState();
            c += 1;
        }
        return new MultiState(initialStates);
    }

    public static MultiPattern compile(final String... patterns) {
        final List<String> patternList = Arrays.asList(patterns);
        return MultiPattern.compile(patternList);
    }

    public static MultiPattern compileForSearch(final String... patterns) {
        final List<String> patternList = Arrays.asList(patterns);
        return MultiPattern.compileForSearch(patternList);
    }

    public static MultiPattern compile(final Iterable<String> patterns) {
        return compile(patterns, false);
    }

    public static MultiPattern compileForSearch(final Iterable<String> patterns) {
        return compile(patterns, true);
    }


    public static MultiPattern compile(final Iterable<String> patterns, boolean forSearch) {
        final List<Automaton> automata = new ArrayList<Automaton>();
        for (final String pattern: patterns) {
            final Automaton automaton = new RegExp(pattern).toAutomaton();
            automata.add(automaton);
        }
        return make(automata, forSearch);
    }


    private static void addEpsilonToInitialForAllStates(Automaton automaton) {
        List<StatePair> statePairs = new ArrayList<>();
        final State initialState = automaton.getInitialState();
        for (final State state: automaton.getStates()) {
            if (!state.equals(initialState)) {
                statePairs.add(new StatePair(state, initialState));
            }
        }
        automaton.addEpsilons(statePairs);
    }

    static MultiPattern make(final List<Automaton> automata, boolean searchAutomaton) {

        if (searchAutomaton) {
            for (Automaton automaton: automata) {
                addEpsilonToInitialForAllStates(automaton);
            }
        }

        for (final Automaton automaton: automata) {
            automaton.determinize();
        }

        final char[] points = DkBricsAutomatonHelper.pointsUnion(automata);

        // states that are still to be visited
        final Queue<MultiState> statesToVisits = new LinkedList<MultiState>();
        final MultiState initialState = initialState(automata);
        statesToVisits.add(initialState);

        final List<int[]> transitionList = new ArrayList<int[]>();

        final Map<MultiState, Integer> multiStateIndex = new HashMap<MultiState, Integer>();
        multiStateIndex.put(initialState, 0);

        while (!statesToVisits.isEmpty()) {
            final MultiState visitingState = statesToVisits.remove();
            assert multiStateIndex.containsKey(visitingState);
            final int[] curTransitions = new int[points.length];
            for (int c=0; c<points.length; c++) {
                final char point = points[c];
                final MultiState destState = visitingState.step(point);
                if (destState.isNull()) {
                    curTransitions[c] = -1;
                }
                else {
                    final int destStateId;
                    if (!multiStateIndex.containsKey(destState)) {
                        statesToVisits.add(destState);
                        destStateId = multiStateIndex.size();
                        multiStateIndex.put(destState, destStateId);
                    }
                    else {
                        destStateId = multiStateIndex.get(destState);
                    }
                    curTransitions[c] = destStateId;
                }
            }
            transitionList.add(curTransitions);
        }

        assert transitionList.size() == multiStateIndex.size();
        final int nbStates = multiStateIndex.size();

        final int[] transitions = new int[nbStates * points.length];
        for (int stateId=0; stateId<nbStates; stateId++) {
            for (int pointId = 0; pointId<points.length; pointId++) {
                transitions[stateId * points.length + pointId] = transitionList.get(stateId)[pointId];
            }
        }

        final int[][] acceptValues = new int[nbStates][];
        for (final Map.Entry<MultiState, Integer> entry: multiStateIndex.entrySet()) {
            final Integer stateId = entry.getValue();
            final MultiState multiState = entry.getKey();
            acceptValues[stateId] = multiState.toAcceptValues();
        }

        return new MultiPattern(acceptValues, transitions, points);
    }

    int step(final int state, final char c) {
        return transitions[((state * points.length) + alphabet[c - Character.MIN_VALUE])];
    }

    public MultiPatternSearcher search(final CharSequence s) {
        return new MultiPatternSearcher(this, s);
    }

    public int[] match(final CharSequence s) {
        int p = 0;
        int l = s.length();
        for (int i = 0; i < l; i++) {
            p = step(p, s.charAt(i));
            if (p == -1)
                return NO_MATCH;
        }
        return this.accept[p];
    }


}
