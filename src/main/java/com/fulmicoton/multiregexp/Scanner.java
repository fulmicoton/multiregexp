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

    public Scanner(MultiPatternAutomaton automaton, CharSequence str, ArrayList<T> tokenTypes) {
        this.str = str;
        this.automaton = automaton;
        this.tokenTypes = tokenTypes;
    }

    boolean nextUnchecked() {
        try {
            return this.next();
        } catch (ScanException e) {
            throw new RuntimeException(e);
        }
    }

    boolean next() throws ScanException {
        this.start = this.end;
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
                        highestPriorityMatch = minAccept;
                        lastLetter = i;
                    }
                }
            }
        }
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
