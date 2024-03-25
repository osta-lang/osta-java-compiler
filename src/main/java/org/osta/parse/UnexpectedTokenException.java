package org.osta.parse;

import org.osta.lex.token.TokenType;

import java.util.Arrays;
import java.util.List;

public class UnexpectedTokenException extends Exception {

    private final List<TokenType> expected;

    public UnexpectedTokenException(TokenType expected, String actual, int line, int column) {
        super("Unexpected token at " + line + ":" + column + ". Expected " + expected + " but got '..." + actual + "...'.");
        this.expected = List.of(expected);
    }

    public UnexpectedTokenException(TokenType[] types, String slice, int line, int column) {
        super("Unexpected token at " + line + ":" + column + ". Expected one of " + Arrays.toString(types) + " but got '..." + slice + "...'.");
        this.expected = Arrays.asList(types);
    }

    public UnexpectedTokenException(List<TokenType> exceptions, String slice, int line, int column) {
        super("Unexpected token at " + line + ":" + column + ". Expected one of " + exceptions + " but got '..." + slice + "...'.");
        this.expected = List.copyOf(exceptions);
    }

    public List<TokenType> expected() {
        return expected;
    }
}
