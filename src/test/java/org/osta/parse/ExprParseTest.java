package org.osta.parse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;

import org.osta.parse.ast.*;

public class ExprParseTest {

    @Test
    void binExpr() throws ParseException {
        var a = FactorExprAST.parser().parse("snake_case world");
        /*
        BinaryExprAST ast = (BinaryExprAST) BinaryExprAST.parser().parse("0+1").ast();
        IntLiteralAST left = (IntLiteralAST) ast.left();
        IntLiteralAST right = (IntLiteralAST) ast.right();

        assertEquals(left.value(), 0);
        assertEquals(ast.op(), BinaryExprAST.BinaryOp.ADD);
        assertEquals(right.value(), 1);

         */
    }

}