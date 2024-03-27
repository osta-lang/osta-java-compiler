package org.osta.parse;

import org.osta.parse.ast.*;

public class ExprParser implements Parser {
    class BinaryExprParser implements Parser {
        public ParseResult parse(CharSequence input) throws ParseException {
            return Parser.map(
                    Parser.sequence(Parser.integer(), Parser.literal("+"), new ExprParser()),
                    (AST ast) -> {
                        SequenceAST sequenceAst = (SequenceAST) ast;
                        ExprAST left = (ExprAST) sequenceAst.value()[0];
                        ExprAST right = (ExprAST) sequenceAst.value()[2];

                        return new BinExprAST(left, BinExprAST.BinaryOp.ADD, right);
                    }
            ).parse(input);
        }
    }
    
    public ParseResult parse(CharSequence input) throws ParseException {
        return Parser.oneOf(
                new BinaryExprParser(),
                Parser.integer(),
                Parser.sequence(Parser.literal("("), new ExprParser(), Parser.literal(")"))
        ).parse(input);
    }
 
}
