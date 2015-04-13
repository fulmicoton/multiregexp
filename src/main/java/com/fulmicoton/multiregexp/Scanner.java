package com.fulmicoton.multiregexp;

import java.util.ArrayList;

public class Scanner<T extends Enum> {

    private final MultiPatternAutomaton automaton;
    private final CharSequence str;
    private final ArrayList<T> tokenTypes;

    public T type;
    public int start = 0;
    public int end = 0;

    public CharSequence tokenString() {
        return this.str.subSequence(this.start, this.end);
    }

    public Scanner(final MultiPatternAutomaton automaton,
                   final CharSequence str,
                   final ArrayList<T> tokenTypes) {
        this.str = str;
        this.automaton = automaton;
        this.tokenTypes = tokenTypes;
    }


    /**
     * Same as next(), but throws unchecked Exception.
     */
    boolean nextUnchecked() {
        try {
            return this.next();
        } catch (ScanException e) {
            throw new RuntimeException(e);
        }
    }

    boolean next() throws ScanException {
        // we start at the end of the last emitted token
        this.start = this.end;
        // we reached the end of the string.
        if (start == this.str.length()) {
            return false;
        }
        int p = 0;
        int highestPriorityMatch = Integer.MAX_VALUE;
        int lastLetter = start;
        for (int i = start; i < this.str.length(); i++) {
            final char chr = this.str.charAt(i);
            p = this.automaton.step(p, chr);
            if (p == -1) {
                break;
            }
            else {
                final int[] accept = this.automaton.accept[p];
                if (accept.length > 0) {
                    final int minAccept = accept[0];
                    if (minAccept <= highestPriorityMatch) {
                        // HighPriority = low value.
                        // If we find a match with a higher priority
                        // we prefer than one,
                        // If it is the same pattern which is
                        // match we take that too for the sake of greediness.
                        highestPriorityMatch = minAccept;
                        lastLetter = i;
                    }
                }
                // when a match is found, we keep matching
                // as a longer prefix might match a pattern
                // with a higher priority.
            }
        }
        // No tokens have been found. Raised an expression
        // with a bit of context, and the offset in the string.
        if (highestPriorityMatch == Integer.MAX_VALUE) {
            final int contextStart = Math.max(0, this.start - 10);
            final int contextEnd = Math.min(this.start + 10, this.str.length());
            final String context = this.str.subSequence(contextStart, this.start) + "|" +  this.str.subSequence(this.start, contextEnd);
            throw new ScanException(context, this.start);
        }
        this.end = lastLetter + 1;
        this.type = this.tokenTypes.get(highestPriorityMatch);
        return true;
    }

}
