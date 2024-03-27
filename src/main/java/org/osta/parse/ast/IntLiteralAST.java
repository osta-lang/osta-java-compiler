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

    public static Parser parser() {
        return Parser.integer();
    }
}
