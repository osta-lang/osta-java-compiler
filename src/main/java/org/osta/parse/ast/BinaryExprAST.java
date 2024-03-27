package org.osta.parse.ast;

import org.osta.parse.ParseException;
import org.osta.parse.ParseResult;
import org.osta.parse.Parser;

public class BinaryExprAST extends ExprAST {

    private ExprAST left, right;
    private BinaryOp op;

    public enum BinaryOp {
        ADD,
        SUB,
        MULT,
        DIV,
        MOD;

        public static BinaryOp from(String opStr) {
            switch (opStr) {
                case "+":
                    return BinaryOp.ADD;
                case "-":
                    return BinaryOp.SUB;
                case "*":
                    return BinaryOp.MULT;
                case "/":
                    return BinaryOp.DIV;
                case "%":
                    return BinaryOp.MOD;
                default:
                    throw new RuntimeException("unreachable");
            }
        }
    }

    public BinaryExprAST(ExprAST left, BinaryOp op, ExprAST right) {
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

    public static Parser<BinaryExprAST> $parser() {
        return Parser.map(
                Parser.sequence(
                        FactorExprAST.$parser(),
                        Parser.oneOf(
                                Parser.literal("+"),
                                Parser.literal("-"),
                                Parser.literal("*"),
                                Parser.literal("/"),
                                Parser.literal("%")
                        ),
                        Parser.lazy(ExprAST::parser)
                ),
                (SequenceAST ast) -> {
                    ExprAST left = (ExprAST) ast.values().get(0);
                    LiteralAST op = (LiteralAST) ast.values().get(1);
                    ExprAST right = (ExprAST) ast.values().get(2);

                    return new BinaryExprAST(left, BinaryOp.from(op.value()), right);
                }
        );
    }
}
