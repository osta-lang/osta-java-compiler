package org.osta.parse.ast;

import org.osta.parse.Parser;

public class FactorExprAST extends ExprAST {
    public static Parser parser() {
        return Parser.oneOf(IntLiteralAST.parser(), IdentifierAST.parser());
    }
}
