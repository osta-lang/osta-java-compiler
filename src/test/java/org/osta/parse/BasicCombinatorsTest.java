package org.osta.parse;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Disabled;

import org.osta.parse.*;
import org.osta.parse.ast.*;

class BasicCombinatorsTest {

    @Test
    void literal() throws Exception {
        assertEquals(
            Parser.literal("hello").parse("hello world"),
            new ParseResult(new LiteralAST("hello"), " world") 
        );
    }

    /* FIXME(cdecompilador): broken somehow, I think there is a bug in optional */
    @Disabled
    @Test
    void skipWhitespace() throws Exception {
        AST ast = Parser.skipWhitespace(Parser.literal("a")).parse("   a    b").ast();
        String val = ((LiteralAST)ast).value();
        assertEquals(val, "a");
    }


    /* FIXME(cdecompilador): this should work since we are using CharSequence */
    @Disabled
    @Test
    void unicodeLiteral() throws Exception {
        assertEquals(
            Parser.literal("ñña").parse("ññaa"),
            new ParseResult(new LiteralAST("ñña"), "ñ")
        );
    }
 
}
