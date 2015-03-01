package com.fulmicoton.multiregexp;

public class Token<T> {

    public final T type;
    public final String str;

    public Token(T type, String str) {
        this.type = type;
        this.str = str;
    }

    public static <T extends Enum> Token<T> fromScanner(Scanner<T> scanner) {
        return new Token<T>(scanner.type, scanner.tokenString().toString());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Token token = (Token) o;

        if (!str.equals(token.str)) return false;
        if (!type.equals(token.type)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = type.hashCode();
        result = 31 * result + str.hashCode();
        return result;
    }

    public String toString() {
        return this.type.toString() + "(" + this.str + ")";
    }
}
