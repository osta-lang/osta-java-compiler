package org.osta.parse.ast;

import org.osta.parse.Parser;
import org.osta.parse.visitor.Visitor;

public class ExprAST implements AST {
    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    public static Parser<ExprAST> parser() {
        return Parser.oneOf(
                BinaryExprAST.$parser(),
                FactorExprAST.$parser(),
                Parser.map(
                        Parser.skipWhitespace(Parser.sequence(
                                Parser.literal("("),
                                Parser.lazy(ExprAST::parser),
                                Parser.literal(")")
                        )),
                        (SequenceAST ast) -> ast.values().get(1)
                )
        );
    }
}
