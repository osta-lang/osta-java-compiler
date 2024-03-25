package org.osta.lex.token;

public record Token(TokenType type, String value, int line, int column) {
    public int length() {
        return value.length();
    }

    @Override
    public String toString() {
        return "Token{" +
                "type=" + type +
                ", value='" + value + '\'' +
                ", line=" + line +
                ", column=" + column +
                '}';
    }
}
