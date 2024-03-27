package org.osta.parse.visitor;

import org.osta.parse.ast.ItemAST;
import org.osta.parse.ast.LiteralAST;
import org.osta.parse.ast.RegexAST;

public interface Visitor {

    void visit(ItemAST ast);

    void visit(LiteralAST ast);

    void visit(RegexAST ast);
}
