package org.osta.parse;

public class ParseException extends Exception {

    public ParseException(String msg) {
        super(msg);
    }

    public static ParseException UNEXPECTED_EOF() {
        return new ParseException("Unexpected end of input");
    }

    public static ParseException EXPECTED_LITERAL(String literal) {
        return new ParseException("Expected literal: " + literal);
    }
}
