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

    private MultiPatternAutomaton makeWithPrefix(String prefix) {
        final List<Automaton> automata = new ArrayList<>();
        for (String ptn: this.patterns) {
            final String prefixedPattern = prefix + ptn;
            final Automaton automaton = new RegExp(prefixedPattern).toAutomaton();
            automata.add(automaton);
        }
        return MultiPatternAutomaton.make(automata);
    }

    /**
     * Equivalent of Pattern.compile, but the result is only valid for pattern search.
     * The searcher will return the first occurence of a pattern.
     *
     * This operation is costly, make sure to cache its result when performing
     * search with the same patterns against the different strings.

     *
     * @return A searcher object
     */
    public MultiPatternSearcher searcher() {
        final MultiPatternAutomaton searcherAutomaton = makeWithPrefix(".*");
        return new MultiPatternSearcher(searcherAutomaton);
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
        final MultiPatternAutomaton matcherAutomaton = makeWithPrefix("");
        return new MultiPatternMatcher(matcherAutomaton);
    }
}
