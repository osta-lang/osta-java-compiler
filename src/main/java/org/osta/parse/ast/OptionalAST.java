package org.osta.parse.ast;

import org.jetbrains.annotations.NotNull;
import org.osta.parse.Parser;
import org.osta.parse.visitor.Visitor;

import java.util.Optional;

public record OptionalAST<T extends AST>(Optional<T> value) implements AST {

    @Override
    public void accept(Visitor visitor) {}

    public static <T extends AST> OptionalAST<T> of(@NotNull T ast) {
        return new OptionalAST<>(Optional.of(ast));
    }

    public static <T extends AST> OptionalAST<T> empty() {
        return new OptionalAST<>(Optional.empty());
    }

    public static <T extends AST> Parser<OptionalAST<T>> parser(Parser<T> parser) {
        return Parser.oneOf(
                Parser.map(parser, OptionalAST::of),
                Parser.map(Parser.noop(), (EmptyAST ast) -> OptionalAST.empty())
        );
    }

}
