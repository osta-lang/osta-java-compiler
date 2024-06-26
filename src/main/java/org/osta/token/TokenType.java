package org.osta.token;

public enum TokenType {
    // Keywords
    STRUCT, TRAIT, ENUM,
    AUTO, STATIC, CONST,
    IF, ELSE,
    WHILE, FOR, DO,
    BREAK, CONTINUE, RETURN, DEFER,
    I8, I16, I32, I64, I128,
    U8, U16, U32, U64, U128,
    F32, F64,
    USIZE,
    // Literals
    IDENTIFIER, INTEGER, FLOAT, STRING, CHAR,
    // Delimiters
    LEFT_PAREN, RIGHT_PAREN, LEFT_BRACE, RIGHT_BRACE, LEFT_BRACKET, RIGHT_BRACKET,
    COMMA, SEMICOLON, COLON, DOT,
    QUESTION, EXCLAMATION,
    // Operators
    PLUS, MINUS, STAR, SLASH, PERCENT,
    AMPERSAND, PIPE, CARET, TILDE, LEFT_SHIFT, RIGHT_SHIFT, ARITHMETIC_RIGHT_SHIFT,
    // Comparison
    EQUAL_EQUAL, NOT_EQUAL, LESS, LESS_EQUAL, GREATER, GREATER_EQUAL,
    // Assignment
    EQUAL, PLUS_EQUAL, MINUS_EQUAL, STAR_EQUAL, SLASH_EQUAL, PERCENT_EQUAL,
    AMPERSAND_EQUAL, PIPE_EQUAL, CARET_EQUAL, LEFT_SHIFT_EQUAL, RIGHT_SHIFT_EQUAL, ARITHMETIC_RIGHT_SHIFT_EQUAL,
    // Misc
    ARROW,
    // EOF
    EOF;
}
