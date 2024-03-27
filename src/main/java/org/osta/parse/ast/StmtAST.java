package org.osta.parse.ast;

import org.osta.parse.ParseResult;
import org.osta.parse.Parser;
import org.osta.parse.visitor.Visitor;

public class StmtAST implements AST {

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    @SuppressWarnings("unchecked")
    public static <T extends StmtAST> Parser<T> parser() {
        return Parser.map(
                Parser.sequence(Parser.skipWhitespace(Parser.oneOf(
                        AssignStmtAST.$parser()
                        /* Add more Stmts here */
                )), Parser.literal(";")),
                (SequenceAST ast) -> (T) ast.values().get(0)
        );
    }

}
