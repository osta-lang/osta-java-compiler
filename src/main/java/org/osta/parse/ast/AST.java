package org.osta.parse.ast;

import org.osta.parse.visitor.Visitor;

import java.util.function.Consumer;

public interface AST extends Consumer<Visitor> {

    @Override
    void accept(Visitor visitor);

}
