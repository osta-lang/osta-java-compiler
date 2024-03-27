package org.osta.parse.ast;

import org.osta.parse.visitor.Visitor;

import java.util.List;

public record SequenceAST(List<AST> values) implements AST {

    @Override
    public void accept(Visitor visitor) {
        for (AST ast : values) {
            ast.accept(visitor);
        }
    }

}
