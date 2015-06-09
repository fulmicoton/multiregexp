package com.fulmicoton.multiregexp.benchmark;

import com.fulmicoton.multiregexp.MultiPattern;
import com.fulmicoton.multiregexp.MultiPatternSearcher;
import dk.brics.automaton.Automaton;
import dk.brics.automaton.AutomatonMatcher;
import dk.brics.automaton.RegExp;
import dk.brics.automaton.RunAutomaton;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.StringJoiner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public enum PatternMethods
{
    JAVA {
        @Override
        PatternMatchingMethod make(List<String> patterns) {
            final List<Pattern> compiledPatterns = new ArrayList<>();
            for (final String pattern: patterns) {
                compiledPatterns.add(Pattern.compile(pattern));
            }
            return new PatternMatchingMethod() {

                private int countPattern(final Pattern pattern,
                                         final String txt) {

                    final Matcher matcher = pattern.matcher(txt);
                    int count = 0;
                    while(matcher.find()) {
                        count++;
                    }
                    return count;
                }

                @Override
                public int[] matchCounts(String txt) {
                    int[] matchCounts = new int[compiledPatterns.size()];
                    int patternId = 0;
                    for (Pattern compiledPattern: compiledPatterns) {
                        matchCounts[patternId] = countPattern(compiledPattern, txt);
                        patternId++;
                    }
                    return matchCounts;
                }
            };
        }
    },
    JAVA_GROUPS {
        @Override
        PatternMatchingMethod make(List<String> patterns) {
            final List<Pattern> compiledPatterns = new ArrayList<>();
            final StringBuilder builder = new StringBuilder();
            final StringJoiner joiner = new StringJoiner("|");
            for (final String pattern: patterns) {
                joiner.add("(?:" + pattern + ")");
            }
            compiledPatterns.add(Pattern.compile(joiner.toString()));
            return new PatternMatchingMethod() {

                private int countPattern(final Pattern pattern,
                                         final String txt) {

                    final Matcher matcher = pattern.matcher(txt);
                    int count = 0;
                    while(matcher.find()) {
                        count++;
                    }
                    return count;
                }

                @Override
                public int[] matchCounts(String txt) {
                    int[] matchCounts = new int[compiledPatterns.size()];
                    int patternId = 0;
                    for (Pattern compiledPattern: compiledPatterns) {
                        matchCounts[patternId] = countPattern(compiledPattern, txt);
                        patternId++;
                    }
                    return matchCounts;
                }
            };
        }
    },
    DKU {
        @Override
        PatternMatchingMethod make(List<String> patterns) {

            final List<RunAutomaton> compiledPatterns = new ArrayList<>();
            for (final String pattern: patterns) {
                final String simplifiedPattern = pattern.replaceAll("\\?:", "");
                final RegExp regexp = new RegExp(simplifiedPattern);
                final Automaton automaton = regexp.toAutomaton();
                automaton.determinize();
                final RunAutomaton runAutomaton = new RunAutomaton(automaton, true);
                compiledPatterns.add(runAutomaton);
            }
            return new PatternMatchingMethod() {

                private int countPattern(final RunAutomaton pattern,
                                         final String txt) {

                    final AutomatonMatcher matcher = pattern.newMatcher(txt);
                    int count = 0;
                    while(matcher.find()) {
                        count++;
                    }
                    return count;
                }

                @Override
                public int[] matchCounts(String txt) {
                    int[] matchCounts = new int[compiledPatterns.size()];
                    int patternId = 0;
                    for (RunAutomaton compiledPattern: compiledPatterns) {
                        matchCounts[patternId] = countPattern(compiledPattern, txt);
                        patternId++;
                    }
                    return matchCounts;
                }
            };
        }
    },
    MULTIPATTERN {
        @Override
        PatternMatchingMethod make(final List<String> patterns) {
            final List<String> simplifiedPatterns = new ArrayList<>();
            for (String pattern: patterns) {
                final String simplifiedPattern = pattern.replaceAll("\\?:", "");
                simplifiedPatterns.add(simplifiedPattern);
            }

            final MultiPattern multipattern = MultiPattern.of(simplifiedPatterns);
            final MultiPatternSearcher searcher = multipattern.searcher();
            return new PatternMatchingMethod() {
                @Override
                public int[] matchCounts(String txt) {
                    int[] matchCounts = new int[patterns.size()];
                    Arrays.fill(matchCounts, 0);
                    final MultiPatternSearcher.Cursor cursor = searcher.search(txt);
                    while (cursor.next()) {
                        matchCounts[cursor.match()] += 1;
                    }
                    return matchCounts;
                }
            };
        }
    };

    abstract PatternMatchingMethod make(final List<String> patterns);

    public static interface PatternMatchingMethod {
        public int[] matchCounts(String txt);

    }





}
