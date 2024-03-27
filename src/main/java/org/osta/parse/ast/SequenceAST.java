package org.osta.parse.ast;

import org.osta.parse.visitor.Visitor;

public record SequenceAST(AST... value) implements AST {

    @Override
    public void accept(Visitor visitor) {
        for (AST ast : value) {
            ast.accept(visitor);
        }
    }

}
