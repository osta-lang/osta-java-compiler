package org.osta.parse;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import org.osta.parse.ast.*;

public class ExprParseTest {

    @Test
    void binExpr() throws ParseException {
        BinaryExprAST firstAst = (BinaryExprAST) BinaryExprAST.parser().parse("0+1*a").ast();
        IntLiteralAST num1 = (IntLiteralAST) firstAst.left();
        BinaryExprAST secondAst = (BinaryExprAST) firstAst.right();
        IntLiteralAST num2 = (IntLiteralAST) secondAst.left();
        IdentifierAST id = (IdentifierAST) secondAst.right();

        assertEquals(num1.value(), 0);
        assertEquals(firstAst.op(), BinaryExprAST.BinaryOp.ADD);
        assertEquals(num2.value(), 1);
        assertEquals(secondAst.op(), BinaryExprAST.BinaryOp.MULT);
        assertEquals(id.identifier(), "a");
    }

}