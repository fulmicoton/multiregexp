package com.fulmicoton.multiregexp;

public class ScanException extends Exception {

    private final int offset;

    public ScanException(String context, int offset) {
        super("Could not find any token at (" + offset + "):\"" + context +"\"");
        this.offset = offset;
    }

    public int getOffset() {
        return offset;
    }
}
