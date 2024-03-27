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

    public static Parser parser() {
        return Parser.map(Parser.sequence(
                    IdentifierAST.parser(), Parser.skipWhitespace(Parser.literal("=")), ExprAST.parser()),
                (AST ast) -> {
                    SequenceAST sequenceAST = (SequenceAST) ast;
                    return new AssignStmtAST(
                            (IdentifierAST) sequenceAST.values().get(0),
                            (ExprAST) sequenceAST.values().get(2));
                }
        );
    }

}
