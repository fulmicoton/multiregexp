package com.fulmicoton.multiregexp;


import dk.brics.automaton.State;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

class MultiState {

    private final State[] states;

    public MultiState(State[] states) {
        this.states = states;
    }

    public boolean isNull() {
        for (State state: this.states) {
            if (state != null) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MultiState that = (MultiState) o;
        return Arrays.equals(states, that.states);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(states);
    }

    public MultiState step(char token) {
        State[] nextStates = new State[this.states.length];
        for (int c=0; c< this.states.length; c++) {
            State prevState = this.states[c];
            if (prevState == null) {
                nextStates[c] = null;
            }
            else {
                nextStates[c] = this.states[c].step(token);
            }
        }
        return new MultiState(nextStates);
    }


    public int[] toAcceptValues() {
        List<Integer> acceptValues = new ArrayList<>();
        for (int stateId=0; stateId<this.states.length; stateId++) {
            State curState = this.states[stateId];
            if ((curState != null) && (curState.isAccept())) {
                acceptValues.add(stateId);
            }
        }
        int[] acceptValuesArr = new int[acceptValues.size()];
        for (int c=0; c<acceptValues.size(); c++) {
            acceptValuesArr[c] = acceptValues.get(c);
        }
        return acceptValuesArr;
    }

}
