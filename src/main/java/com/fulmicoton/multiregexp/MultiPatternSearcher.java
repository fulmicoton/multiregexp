package com.fulmicoton.multiregexp;

public class MultiPatternSearcher {
    private final MultiPattern multiPattern;
    private final CharSequence seq;
    private int cur = 0;
    private int curState = 0;
    private int matchedPattern = 0;
    private final int seqLength;

    MultiPatternSearcher(MultiPattern multiPattern, CharSequence seq) {
        this.multiPattern = multiPattern;
        this.seq = seq;
        this.seqLength = seq.length();
    }

    public int[] find() {
        this.curState = 0;
        for (; cur < seqLength; ++cur) {
            this.curState = this.multiPattern.step(this.curState, this.seq.charAt(cur));
            if (this.multiPattern.isAtLeastOneAccept(this.curState)) {
                return this.multiPattern.acceptedPatterns(this.curState);
            }
        }
        return null;
    }

}


/**
 *         int p = 0;
 int l = s.length();
 for (int i = 0; i < l; i++) {
 p = step(p, s.charAt(i));
 if (p == -1)
 return NO_MATCH;
 }
 return this.accept[p];
 */