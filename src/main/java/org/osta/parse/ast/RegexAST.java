package org.osta.parse.ast;

import org.osta.parse.visitor.Visitor;

public record RegexAST(String value, String... groups) implements AST {

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

}
