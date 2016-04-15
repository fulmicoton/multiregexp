package com.fulmicoton.multiregexp;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;

import dk.brics.automaton.Automaton;
import dk.brics.automaton.DkBricsAutomatonHelper;
import dk.brics.automaton.State;

public class MultiPatternAutomaton
        implements Serializable {

    private static final long serialVersionUID = -8269666436361824366L;

    public final int[][] accept;
    final boolean[] atLeastOneAccept;
    private final int stride;
    private final int[] transitions;
    private final int[] alphabet;
    private final int nbPatterns;

    private MultiPatternAutomaton(final int[][] accept,
                                  final int[] transitions,
                                  final char[] points,
                                  final int nbPatterns) {
        this.accept = accept;
        this.transitions = transitions;
        this.alphabet = alphabet(points);
        this.stride = points.length;
        this.atLeastOneAccept = new boolean[accept.length];
        for (int i=0; i<accept.length; i++) {
            this.atLeastOneAccept[i] = this.accept[i].length > 0;
        }
        this.nbPatterns = nbPatterns;
    }

    private static int[] alphabet(final char[] points) {
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

    static MultiPatternAutomaton multithreadedMake(final List<Automaton> automata) {
        for (final Automaton automaton: automata) {
            automaton.determinize();
        }

        final char[] points = DkBricsAutomatonHelper.pointsUnion(automata);

        // states that are still to be visited
        final Queue<MultiState> statesToVisits = new ConcurrentLinkedQueue<>();
        final MultiState initialState = initialState(automata);
        statesToVisits.add(initialState);

        final Map<Integer, int[]> transitionMap = new ConcurrentHashMap<>();

        final Map<MultiState, Integer> multiStateIndex = new ConcurrentHashMap<>();
        multiStateIndex.put(initialState, 0);

        final int numberOfThreads = Runtime.getRuntime().availableProcessors();

        final Object lockObject = new Object();
        final List<Thread> activeThreads = Collections.synchronizedList(new ArrayList<Thread>());
        final CountDownLatch doneSignal = new CountDownLatch(numberOfThreads);
        for (int thread = 0; thread < numberOfThreads; thread++) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    activeThreads.add(Thread.currentThread());

                    while (true) {
                        MultiState visitingState;
                        while ((visitingState = statesToVisits.poll()) != null) {
//                            assert multiStateIndex.containsKey(visitingState);

                            int stateId = multiStateIndex.get(visitingState);
                            final int[] curTransitions = new int[points.length];
                            transitionMap.put(stateId, curTransitions);

                            for (int c = 0; c < points.length; c++) {
                                final char point = points[c];
                                final MultiState destState = visitingState.step(point);
                                if (destState.isNull()) {
                                    curTransitions[c] = -1;
                                } else {
                                    final int destStateId;
                                    synchronized (multiStateIndex) {
                                        if (!multiStateIndex.containsKey(destState)) {
                                            destStateId = multiStateIndex.size();
                                            multiStateIndex.put(destState, destStateId);
                                            statesToVisits.add(destState);
                                            synchronized (lockObject) {
                                                // wake a thread to process destState
                                                lockObject.notify();
                                            }
                                        } else {
                                            destStateId = multiStateIndex.get(destState);
                                        }
                                    }
                                    curTransitions[c] = destStateId;
                                }
                            }
                        }
                        activeThreads.remove(Thread.currentThread());
                        // if there are no active threads then we are done
                        if (activeThreads.isEmpty()) {
                            synchronized (lockObject) {
                                // wake waiting threads so they can end
                                lockObject.notifyAll();
                            }
                            // end this thread
                            break;
                        } else {
                            synchronized (lockObject) {
                                try {
                                    lockObject.wait();
                                }
                                catch (InterruptedException ignore) {
                                }
                            }
                            activeThreads.add(Thread.currentThread());
                        }
                    }
                    doneSignal.countDown();
                }
            }).start();
        }
        try {
            // wait for all to finish
            doneSignal.await();
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }

        assert transitionMap.size() == multiStateIndex.size();

        final int[] transitions = new int[transitionMap.size() * points.length];
        for (final Map.Entry<Integer, int[]> entry : transitionMap.entrySet()) {
            System.arraycopy(entry.getValue(), 0, transitions, entry.getKey() * points.length, points.length);
        }

        final int[][] acceptValues = new int[multiStateIndex.size()][];
        for (final Map.Entry<MultiState, Integer> entry : multiStateIndex.entrySet()) {
            acceptValues[entry.getValue()] = entry.getKey().toAcceptValues();
        }

        return new MultiPatternAutomaton(acceptValues, transitions, points, automata.size());
    }

    static MultiPatternAutomaton make(final List<Automaton> automata) {
        for (final Automaton automaton: automata) {
            automaton.determinize();
        }

        final char[] points = DkBricsAutomatonHelper.pointsUnion(automata);

        // states that are still to be visited
        final Queue<MultiState> statesToVisits = new LinkedList<>();
        final MultiState initialState = initialState(automata);
        statesToVisits.add(initialState);

        final List<int[]> transitionList = new ArrayList<>();

        final Map<MultiState, Integer> multiStateIndex = new HashMap<>();
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

        return new MultiPatternAutomaton(acceptValues, transitions, points, automata.size());
    }

    public int step(final int state, final char c) {
        return transitions[(state * this.stride) + alphabet[c - Character.MIN_VALUE]];
    }

    public int getNbPatterns() {
        return this.nbPatterns;
    }

}
