package org.osta.parse.ast;

import org.osta.parse.Parser;

public class IdentifierAST extends ExprAST {

    private final String identifier;

    public IdentifierAST(String identifier) {
        this.identifier = identifier;
    }

    public String identifier() {
        return identifier;
    }

    // TODO
    public static Parser<IdentifierAST> $parser() {
        return Parser.map(
                Parser.regex("[_a-zA-Z][_a-zA-Z0-9]*", "Invalid identifier"),
                (RegexAST ast) -> new IdentifierAST(ast.value())
        );
    }
}
