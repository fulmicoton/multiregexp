package com.fulmicoton.multiregexp;

import org.junit.Assert;
import org.junit.Test;

import java.util.Iterator;

/**
 * Created by pmasurel on 4/28/16.
 */
public class LexerTest {

    public enum TokenType {
        WORD,
        SYMBOL,
        NUMBER,
        WHITESPACE
    }

    public static Lexer<TokenType> lexer() {
        final Lexer<TokenType> lexer = new Lexer<>();
        lexer.addRule(TokenType.WHITESPACE, "[ \t\n]+");
        lexer.addRule(TokenType.NUMBER, "[0-9][0-9,\\.]+");
        lexer.addRule(TokenType.WORD, "[a-zA-Z0-9]+");
        lexer.addRule(TokenType.SYMBOL, ".");
        return lexer;
    }

    @Test
    public void testLexer() {
        final Lexer<TokenType> lexer = lexer();
        final Iterator<Token<TokenType>> tokens = lexer.scan("100k").iterator();
        {
            final Token token = tokens.next();
            Assert.assertEquals(token.str, "100");
            Assert.assertEquals(token.type, TokenType.NUMBER);
        }
        {
            final Token token = tokens.next();
            Assert.assertEquals(token.str, "k");
            Assert.assertEquals(token.type, TokenType.WORD);
        }
        Assert.assertFalse(tokens.hasNext());
    }
}
