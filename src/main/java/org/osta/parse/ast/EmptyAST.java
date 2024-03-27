package org.osta.parse.ast;

import org.osta.parse.visitor.Visitor;

public record EmptyAST() implements AST {

    @Override
    public void accept(Visitor visitor) {}

}
