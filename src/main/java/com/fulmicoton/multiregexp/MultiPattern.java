package com.fulmicoton.multiregexp;

import dk.brics.automaton.Automaton;
import dk.brics.automaton.RegExp;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MultiPattern {

    private final List<String> patterns;

    private MultiPattern(List<String> patterns) {
        this.patterns = new ArrayList<>(patterns);
    }

    public static MultiPattern of(List<String> patterns) {
        return new MultiPattern(patterns);
    }

    public static MultiPattern of(String... patterns) {
        return new MultiPattern(Arrays.asList(patterns));
    }

    public MultiPatternAutomaton makeAutomatonWithPrefix(String prefix) {
        final List<Automaton> automata = new ArrayList<>();
        for (final String ptn: this.patterns) {
            final String prefixedPattern = prefix + ptn;
            final Automaton automaton = new RegExp(prefixedPattern).toAutomaton();
            automaton.minimize();
            automata.add(automaton);
        }
        return MultiPatternAutomaton.make(automata);
    }

    /**
     * Equivalent of Pattern.compile, but the result is only valid for pattern search.
     * The searcher will return the first occurrence of a pattern.
     *
     * This operation is costly, make sure to cache its result when performing
     * search with the same patterns against the different strings.
     *
     * @return A searcher object
     */
    public MultiPatternSearcher searcher() {
        final MultiPatternAutomaton searcherAutomaton = makeAutomatonWithPrefix(".*");
        final List<Automaton> indidivualAutomatons = new ArrayList<>();
        for (final String pattern: this.patterns) {
            final Automaton automaton = new RegExp(pattern).toAutomaton();
            automaton.minimize();
            automaton.determinize();
            indidivualAutomatons.add(automaton);
        }
        return new MultiPatternSearcher(searcherAutomaton, indidivualAutomatons);
    }


    /**
     * Equivalent of Pattern.compile, but the result is only valid for full string matching.
     *
     * If more than one pattern are matched, with a match ending at the same offset,
     * return all of the pattern ids in a sorted array.
     *
     * This operation is costly, make sure to cache its result when performing
     * search with the same patterns against the different strings.
     *
     * @return A searcher object
     */
    public MultiPatternMatcher matcher() {
        final MultiPatternAutomaton matcherAutomaton = makeAutomatonWithPrefix("");
        return new MultiPatternMatcher(matcherAutomaton);
    }

}
