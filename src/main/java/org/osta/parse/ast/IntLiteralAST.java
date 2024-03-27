package org.osta.parse.ast;

import java.util.Objects;

public final class IntLiteralAST extends ExprAST {
    private final Integer value;

    public IntLiteralAST(Integer value) {
        this.value = value;
    }

    public Integer value() {
        return value;
    }
}
