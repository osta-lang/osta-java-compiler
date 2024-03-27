package org.osta.parse.ast;

import org.osta.parse.Parser;

import java.util.Objects;

public final class IntLiteralAST extends FactorExprAST {
    private final Integer value;

    public IntLiteralAST(Integer value) {
        this.value = value;
    }

    public Integer value() {
        return value;
    }

    public static Parser<IntLiteralAST> $parser() {
        return Parser.map(Parser.oneOf(
                Parser.regex("([+-]?\\d+)(?:[eE](\\+?\\d+))?", "Expected an integer"),
                Parser.regex("[+-]?0[bB][01]+", "Expected an integer"),
                Parser.regex("[+-]?0[oO][0-7]+", "Expected an integer"),
                Parser.regex("[+-]?0[xX][0-9a-fA-F]+", "Expected an integer")
        ), (AST ast) -> {
            RegexAST r = (RegexAST)ast;
            if (r.groups().length > 0) {
                return new IntLiteralAST(Integer.parseInt(r.value()));
            }
            int value = Integer.parseInt(r.groups()[0]);
            int exponent = Integer.parseInt(r.groups()[1]);
            if (exponent == 0) {
                return new IntLiteralAST(value);
            }
            return new IntLiteralAST((int) (value * Math.pow(10, exponent)));
        });
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        IntLiteralAST that = (IntLiteralAST) o;
        return Objects.equals(value, that.value);
    }

}
