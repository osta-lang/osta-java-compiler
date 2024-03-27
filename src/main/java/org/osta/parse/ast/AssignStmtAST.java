package org.osta.parse.ast;

import org.osta.parse.ParseResult;
import org.osta.parse.Parser;

public class AssignStmtAST extends StmtAST {

    private IdentifierAST target;
    private ExprAST value;

    public AssignStmtAST(IdentifierAST target, ExprAST value) {
        this.target = target;
        this.value = value;
    }

    public static Parser<AssignStmtAST> $parser() {
        return Parser.map(Parser.sequence(
                    IdentifierAST.parser(), Parser.skipWhitespace(Parser.literal("=")), ExprAST.parser()),
                (SequenceAST ast) -> new AssignStmtAST(
                        (IdentifierAST) ast.values().get(0),
                        (ExprAST) ast.values().get(2))
        );
    }

}
