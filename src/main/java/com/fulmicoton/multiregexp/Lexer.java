package com.fulmicoton.multiregexp;


import java.util.ArrayList;
import java.util.Iterator;

public class Lexer<T extends Enum> {

    private final ArrayList<T> types = new ArrayList<T>() ;
    private final ArrayList<String> patterns = new ArrayList<String>();

    public Lexer addRule(final T tokenType, final String pattern) {
        this.types.add(tokenType);
        this.patterns.add(pattern);
        return this;
    }

    public Scanner<T> scannerFor(CharSequence seq) {
        final MultiPattern multiPattern = MultiPattern.of(patterns);
        final MultiPatternAutomaton automaton = multiPattern.makeWithPrefix("");
        return new Scanner<T>(automaton, seq, this.types);
    }

    public Iterable<Token<T>> scan(final CharSequence seq) {
        final Scanner<T> scanner = this.scannerFor(seq);
        scanner.nextUnchecked();
        return new Iterable<Token<T>>() {

            Token<T> next = Token.fromScanner(scanner);

            @Override
            public Iterator<Token<T>> iterator() {
                return new Iterator<Token<T>>() {
                    @Override
                    public boolean hasNext() {
                        return next != null;
                    }

                    @Override
                    public Token<T> next() {
                        Token<T> buff = next;
                        if (scanner.nextUnchecked()) {
                            next = Token.fromScanner(scanner);
                        }
                        else {
                            next = null;
                        }
                        return buff;
                    }

                    @Override
                    public void remove() {
                        throw new UnsupportedOperationException();
                    }
                };
            }
        };
    }

}
