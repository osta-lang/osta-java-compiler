package org.osta.parse.ast;

import org.osta.parse.Parser;

public class FactorExprAST extends ExprAST {
    public static Parser<? extends AST> $parser() {
        return Parser.skipWhitespace(Parser.anyOf(IntLiteralAST.$parser(), IdentifierAST.$parser()));
    }
}
