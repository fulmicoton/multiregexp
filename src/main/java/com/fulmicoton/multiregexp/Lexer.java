package com.fulmicoton.multiregexp;


import java.io.Reader;
import java.util.ArrayList;
import java.util.Iterator;

public class Lexer<T extends Enum> {

    private final ArrayList<T> types = new ArrayList<>() ;
    private final ArrayList<String> patterns = new ArrayList<>();
    private transient MultiPatternAutomaton automaton = null;

    public Lexer<T> addRule(final T tokenType, final String pattern) {
        this.types.add(tokenType);
        this.patterns.add(pattern);
        this.automaton = null;
        return this;
    }

    public MultiPatternAutomaton getAutomaton() {
        if (this.automaton == null) {
            this.automaton = MultiPattern.of(patterns).makeAutomatonWithPrefix("");
        }
        return this.automaton;
    }

    public Scanner<T> scannerFor(final Reader reader) {
        return new Scanner<T>(this.getAutomaton(), reader, this.types);
    }

    public Scanner<T> scannerFor(final CharSequence seq) {
        return new Scanner<T>(this.getAutomaton(), seq, this.types);
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
