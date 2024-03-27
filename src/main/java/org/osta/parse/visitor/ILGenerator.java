package org.osta.parse.visitor;

import org.osta.parse.ast.*;

public class ILGenerator implements Visitor {

    StringBuilder sb = new StringBuilder();

    @Override
    public void visit(ItemAST ast) {
        sb.append(ast.value());
    }

    @Override
    public void visit(LiteralAST ast) {
        sb.append(ast.value());
    }

    @Override
    public void visit(RegexAST ast) {
        sb.append(ast.value());
    }

    @Override 
    public void visit(ExprAST ast) {
        // TODO
    }

    @Override
    public void visit(StmtAST stmtAST) {
        // TODO
    }

    public String generate() {
        return sb.toString();
    }
}
