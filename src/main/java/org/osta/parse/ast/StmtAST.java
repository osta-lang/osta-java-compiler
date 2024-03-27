package org.osta.parse.ast;

import org.osta.parse.ParseResult;
import org.osta.parse.Parser;
import org.osta.parse.visitor.Visitor;

public class StmtAST implements AST {

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    public static Parser parser() {
        return Parser.sequence(Parser.skipWhitespace(Parser.oneOf(
                AssignStmtAST.parser()
                /* Add more Stmts here */
        )), Parser.literal(";"));
    }

}
