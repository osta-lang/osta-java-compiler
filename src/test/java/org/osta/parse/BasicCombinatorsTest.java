package org.osta.parse;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Disabled;

import org.osta.parse.*;
import org.osta.parse.ast.*;

class BasicCombinatorsTest {

    @Test
    void optionalTest() throws Exception {
        var p = Parser.optional(Parser.literal("ab"));
        ParseResult r = p.parse("bab");
        assertEquals(r.rest(), "bab");

        r = p.parse("abab");
        LiteralAST ast = (LiteralAST)r.ast();
        assertEquals(ast.value(), "ab");

        r = Parser.zeroOrMore(Parser.literal("ab")).parse("ababz");
        SequenceAST sequenceAST = (SequenceAST) r.ast();
        assertEquals(((LiteralAST)sequenceAST.values().get(0)).value(), "ab");
        assertEquals(((LiteralAST)sequenceAST.values().get(1)).value(), "ab");
        assertEquals(r.rest(), "z");
    }

    @Test
    void literal() throws Exception {
        assertEquals(
            Parser.literal("hello").parse("hello world"),
            new ParseResult(new LiteralAST("hello"), " world") 
        );
    }

    /* FIXME(cdecompilador): broken somehow, I think there is a bug in optional */
    @Test
    void skipWhitespace() throws Exception {
        ParseResult r = Parser.skipWhitespace(Parser.literal("a")).parse("   a    b");
        LiteralAST ast = ((LiteralAST) r.ast());
        String val = ast.value();
        assertEquals(val, "a");
        assertEquals(r.rest(), "b");
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
