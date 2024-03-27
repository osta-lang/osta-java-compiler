package org.osta.parse.ast;

import org.osta.parse.Parser;

public class IdentifierAST extends FactorExprAST {

    private final String identifier;

    public IdentifierAST(String identifier) {
        this.identifier = identifier;
    }

    // TODO
    public static Parser parser() {
        return Parser.map(
                Parser.regex("[_a-zA-Z][_a-zA-Z0-9]+", "Invalid identifier"),
                (AST ast) -> new IdentifierAST(((RegexAST)ast).value())
        );
    }
}