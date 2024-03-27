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

        /* FIXME(cdecompilador): this may be bad behaviour which we can't catch inside the integer() combinator, but
         * we can catch later on on expression parsing, but the error in that case would be something like
         * "0154" -> 0 154
         *             ^ expected operand found int literal
         * when we would want something like
         * "0154"
         *  ^ Invalid integer that starts with 0
         *
         *  ...or we could just accept integer with zeros on the left
        assertEquals(
                Parser.integer().parse("0154"),
                new ParseResult(new IntLiteralAST(0), "154")
        );
        */
    }

}
