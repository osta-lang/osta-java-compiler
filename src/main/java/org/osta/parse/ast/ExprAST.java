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
                        (SequenceAST ast) -> {
                            /* TODO(cdecompilador): Maybe add here an annotation Expr to tell that this one has maximum
                             * precedence since it goes inside parethesis, such that the visior that does the AST
                             * reordering can take them into account
                             */
                            return ast.values().get(1);
                        }
                )
        );
    }
}
