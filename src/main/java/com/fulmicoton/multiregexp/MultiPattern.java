package com.fulmicoton.multiregexp;

import dk.brics.automaton.Automaton;
import dk.brics.automaton.DkBricsAutomatonHelper;
import dk.brics.automaton.RegExp;
import dk.brics.automaton.State;

import java.util.*;

public class MultiPattern {

    private final static int[] NO_MATCH = new int[0];
    private final int[][] accept;
    private final int[] transitions;
    private final char[] points;
    private final int[] alphabet;

    private MultiPattern(int[][] accept, int[] transitions, char[] points) {
        this.accept = accept;
        this.transitions = transitions;
        this.points = points;
        this.alphabet = alphabet(points);
    }

    public int alphabetSize() {
        return this.points.length;
    }

    public int nbStates() {
        return this.accept.length;
    }

    static int[] alphabet(char[] points) {
        final int[] alphabet = new int[Character.MAX_VALUE - Character.MIN_VALUE + 1];
        int i = 0;
        for (int j = 0; j <= Character.MAX_VALUE - Character.MIN_VALUE; j++) {
            if (i + 1 < points.length && j == points[i + 1])
                i++;
            alphabet[j] = i;
        }
        return alphabet;
    }

    static MultiState initialState(List<Automaton> automata) {
        State[] initialStates = new State[automata.size()];
        int c = 0;
        for (Automaton automaton: automata) {
            initialStates[c] = automaton.getInitialState();
            c += 1;
        }
        return new MultiState(initialStates);
    }

    public static MultiPattern compile(String... patterns) {
        List<String> patternList = Arrays.asList(patterns);
        return MultiPattern.compile(patternList);
    }

    public static MultiPattern compile(Iterable<String> patterns) {
        List<Automaton> automata = new ArrayList<Automaton>();
        for (String pattern: patterns) {
            Automaton automaton = new RegExp(pattern).toAutomaton();
            automata.add(automaton);
        }
        return make(automata);
    }

    static MultiPattern make(List<Automaton> automata) {
        for (Automaton automaton: automata) {
            automaton.determinize();
        }

        final char[] points = DkBricsAutomatonHelper.pointsUnion(automata);

        // states that are still to be visited
        Queue<MultiState> statesToVisits = new LinkedList<MultiState>();
        MultiState initialState = initialState(automata);
        statesToVisits.add(initialState);

        List<int[]> transitionList = new ArrayList<int[]>();

        Map<MultiState, Integer> multiStateIndex = new HashMap<MultiState, Integer>();
        multiStateIndex.put(initialState, 0);

        while (statesToVisits.size() > 0) {
            MultiState visitingState = statesToVisits.remove();
            assert multiStateIndex.containsKey(visitingState);
            int[] curTransitions = new int[points.length];
            for (int c=0; c<points.length; c++) {
                final char point = points[c];
                MultiState destState = visitingState.step(point);
                if (destState.isNull()) {
                    curTransitions[c] = -1;
                }
                else {
                    int destStateId;
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
        int nbStates = multiStateIndex.size();

        int[] transitions = new int[nbStates * points.length];
        for (int stateId=0; stateId<nbStates; stateId++) {
            for (int pointId = 0; pointId<points.length; pointId++) {
                transitions[stateId * points.length + pointId] = transitionList.get(stateId)[pointId];
            }
        }

        int[][] acceptValues = new int[nbStates][];
        for (Map.Entry<MultiState, Integer> entry: multiStateIndex.entrySet()) {
            Integer stateId = entry.getValue();
            MultiState multiState = entry.getKey();
            acceptValues[stateId] = multiState.toAcceptValues();
        }

        return new MultiPattern(acceptValues, transitions, points);
    }

    private int step(int state, char c) {
        return transitions[state * points.length + alphabet[c - Character.MIN_VALUE]];
    }

    public int[] match(String s) {
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
