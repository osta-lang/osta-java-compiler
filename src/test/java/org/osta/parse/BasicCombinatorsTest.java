package org.osta.parse;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.osta.parse.ast.LiteralAST;
import org.osta.parse.ast.OptionalAST;
import org.osta.parse.ast.SequenceAST;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BasicCombinatorsTest {

    @Test
    void optionalTest() throws Exception {
        Parser<OptionalAST<LiteralAST>> parser = Parser.optional(Parser.literal("ab"));
        ParseResult<OptionalAST<LiteralAST>> result = parser.parse("bab");
        assertEquals(result.rest(), "bab");

        result = parser.parse("abab");
        Optional<LiteralAST> astOptional = result.ast().value();
        assertTrue(astOptional.isPresent());
        LiteralAST ast = astOptional.get();
        assertEquals(ast.value(), "ab");
    }

    @Test
    void zeroOrMoreTest() throws Exception {
        Parser<SequenceAST> parser = Parser.zeroOrMore(Parser.literal("ab"));
        ParseResult<SequenceAST> result = parser.parse("ababz");
        SequenceAST ast = result.ast();
        assertEquals(((LiteralAST)ast.values().get(0)).value(), "ab");
        assertEquals(((LiteralAST)ast.values().get(1)).value(), "ab");
        assertEquals(result.rest(), "z");
    }

    @Test
    void literal() throws Exception {
        assertEquals(
            Parser.literal("hello").parse("hello world"),
            new ParseResult<>(new LiteralAST("hello"), " world")
        );
    }

    /* FIXME(cdecompilador): broken somehow, I think there is a bug in optional */
    @Test
    void skipWhitespace() throws Exception {
        ParseResult<LiteralAST> result = Parser.skipWhitespace(Parser.literal("a")).parse("   a    b");
        LiteralAST ast = result.ast();
        String val = ast.value();
        assertEquals(val, "a");
        assertEquals(result.rest(), "b");
    }


    /* FIXME(cdecompilador): this should work since we are using CharSequence */
    @Disabled
    @Test
    void unicodeLiteral() throws Exception {
        assertEquals(
            Parser.literal("ñña").parse("ññaa"),
            new ParseResult<>(new LiteralAST("ñña"), "ñ")
        );
    }
 
}
