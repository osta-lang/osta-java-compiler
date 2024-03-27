package org.osta.parse.ast;

import org.osta.parse.visitor.Visitor;

public class ExprAST implements AST {
    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }
}
