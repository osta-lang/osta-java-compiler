package org.osta.parse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;

import org.osta.parse.ast.IntLiteralAST;
import org.osta.parse.*;

public class NumberParseTest {

    @Test
    void simpleInteger() throws ParseException {
        /* FIXME: IntegerLiteral doesnt implement equals anymore
        assertEquals(
                Parser.integer().parse("0"),
                new ParseResult(new IntLiteralAST(0), "")
        );
        assertEquals(
                Parser.integer().parse("-1234 a"),
                new ParseResult(new IntLiteralAST(-1234), " a")
        );
        */

        assertEquals(
                IntLiteralAST.parser().parse("0154"),
                new ParseResult<>(new IntLiteralAST(154), "")
        );
    }

}
