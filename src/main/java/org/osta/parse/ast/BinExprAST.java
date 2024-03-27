package org.osta.parse.ast;

public class BinExprAST extends ExprAST {

    private ExprAST left, right;
    private BinaryOp op;

    public enum BinaryOp {
        ADD,
        SUB,
        MULT,
        DIV,
        MOD
    }

    public BinExprAST(ExprAST left, BinaryOp op, ExprAST right) {
        this.left = left;
        this.op = op;
        this.right = right;
    }

    /* FIXME(cdecompilador): Remove this, since for testing we will use visitors */
    public ExprAST left() {
        return left;
    }
    public ExprAST right() {
        return right;
    }
    public BinaryOp op() {
        return op;
    }
}
