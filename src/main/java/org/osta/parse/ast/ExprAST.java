package org.osta.parse.ast;

import org.osta.parse.Parser;
import org.osta.parse.visitor.Visitor;

public class ExprAST implements AST {
    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    public static Parser parser() {
        return Parser.oneOf(
                BinaryExprAST.parser(),
                FactorExprAST.parser(),
                Parser.sequence(Parser.literal("("), ExprAST.parser(), Parser.literal(")"))
        );
    }
}
