package org.osta.parse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;

import org.osta.parse.ast.*;
import org.osta.parse.*;

public class ExprParseTest {

    @Test
    void binExpr() throws ParseException {
        BinExprAST ast = (BinExprAST) new ExprParser().parse("0+1").ast();
        IntLiteralAST left = (IntLiteralAST) ast.left();
        IntLiteralAST right = (IntLiteralAST) ast.right();

        assertEquals(left.value(), 0);
        assertEquals(ast.op(), BinExprAST.BinaryOp.ADD);
        assertEquals(right.value(), 1);
    }

}