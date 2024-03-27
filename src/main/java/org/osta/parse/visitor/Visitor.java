package org.osta.parse.visitor;

import org.osta.parse.ast.*;

public interface Visitor {

    void visit(ItemAST ast);

    void visit(LiteralAST ast);

    void visit(RegexAST ast);

    void visit(ExprAST ast);

}
