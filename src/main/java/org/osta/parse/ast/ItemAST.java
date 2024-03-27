package org.osta.parse.ast;

import org.osta.parse.visitor.Visitor;

public record ItemAST(Character value) implements AST {

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

}
