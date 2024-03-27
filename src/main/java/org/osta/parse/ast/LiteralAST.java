package org.osta.parse.ast;

import org.osta.parse.visitor.Visitor;

/* NOTE(cdecompilador): I would change LiteralAST, ItemAST, SequenceAST, RegexAST to inherit from a common
 * class IntermediateAST that would be unvisitable such that the resulting AST will fail if
 * it still contains some if this intermediate artifacts on it */
public record LiteralAST(String value) implements AST {

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

}
