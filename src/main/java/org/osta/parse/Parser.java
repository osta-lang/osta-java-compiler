package org.osta.parse;

import org.jetbrains.annotations.NotNull;
import org.osta.parse.ast.*;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public interface Parser<T extends AST> {

    ParseResult<T> parse(CharSequence input) throws ParseException;

    static Parser<EmptyAST> noop() {
        return input -> new ParseResult<>(new EmptyAST(), input);
    }

    static <T extends AST> Parser<T> lazy(Supplier<Parser<T>> supplier) {
        return input -> supplier.get().parse(input);
    }

    static <T extends AST> Parser<T> test(Parser<T> parser, Predicate<AST> predicate, Supplier<ParseException> exceptionSupplier) {
        return input -> {
            ParseResult<T> result = parser.parse(input);
            if (predicate.test(result.ast())) {
                return result;
            }
            throw exceptionSupplier.get();
        };
    }

    static <T extends AST> Parser<T> test(Parser<T> parser, Predicate<AST> predicate, String message) {
        return test(parser, predicate, () -> new ParseException(message));
    }

    @SafeVarargs
    static Parser<SequenceAST> sequence(Parser<? extends AST>... parsers) {
        if (parsers.length == 0) {
            throw new IllegalArgumentException("At least one parser must be provided");
        }

        return input -> {
            List<AST> asts = new ArrayList<>(parsers.length);
            for (Parser<?> parser : parsers) {
                ParseResult<?> result = parser.parse(input);
                asts.add(result.ast());
                input = result.rest();
            }
            return new ParseResult<>(new SequenceAST(asts), input);
        };
    }

    /**
     * Returns a parser that tries to parse the input with each of the provided parsers in order.
     * The first successful parser will determine the return value of the new parser.
     * If none of the parsers succeed, the parser will throw a {@link ParseException}.
     * @param parsers The parsers to try
     * @return A parser that tries to parse the input with each of the provided parsers in order
     * @param <T> The return type of the parsers
     * @throws IllegalArgumentException If no parsers are provided
     * @see #oneOf(Parser[]) For a parser that tries to parse the input with each of the provided parsers in order where the parsers have the same return type
     */
    @SuppressWarnings("unchecked")
    @SafeVarargs
    static <T extends AST> Parser<T> anyOf(Parser<? extends AST>... parsers) {
        if (parsers.length == 0) {
            throw new IllegalArgumentException("At least one parser must be provided");
        }

        return input -> {
            ParseException exception = null;
            for (Parser<? extends AST> parser : parsers) {
                try {
                    return (ParseResult<T>) parser.parse(input);
                } catch (ParseException e) {
                    exception = e;
                }
            }
            throw exception;
        };
    }

    /**
     * Returns a parser that tries to parse the input with each of the provided parsers in order.
     * The first successful parser will determine the return value of the new parser.
     * If none of the parsers succeed, the parser will throw a {@link ParseException}.
     * @param parsers The parsers to try, all of which must have the same return type
     * @return A parser that tries to parse the input with each of the provided parsers in order
     * @param <T> The return type of the parsers
     * @throws IllegalArgumentException If no parsers are provided
     * @see #anyOf(Parser[]) For a parser that tries to parse the input with each of the provided parsers in order where the parsers may have different return types
     */
    @SafeVarargs
    static <T extends AST> Parser<T> oneOf(Parser<T>... parsers) {
        if (parsers.length == 0) {
            throw new IllegalArgumentException("At least one parser must be provided");
        }

        return input -> {
            ParseException exception = null;
            for (Parser<T> parser : parsers) {
                try {
                    return parser.parse(input);
                } catch (ParseException e) {
                    exception = e;
                }
            }
            throw exception;
        };
    }

    static <T extends AST> Parser<OptionalAST<T>> optional(Parser<T> parser) {
        return OptionalAST.parser(parser);
    }

    @FunctionalInterface
    interface ParserMapLambda<I extends AST, O extends AST> {
        O apply(I value);
    }

    static <I extends AST, O extends AST> Parser<O> map(Parser<I> parser, ParserMapLambda<I, O> map) {
        return input -> {
            var result = parser.parse(input);
            return new ParseResult<>(map.apply(result.ast()), result.rest());
        };
    }

    static Parser<SequenceAST> zeroOrMore(Parser<?> parser) {
        return input -> {
            var asts = new ArrayList<AST>();

            while (true) {
                try {
                    ParseResult<?> result = parser.parse(input);
                    asts.add(result.ast());
                    input = result.rest();
                } catch (Exception e) {
                    break;
                }
            }

            return new ParseResult<>(new SequenceAST(asts), input);
        };
    }

    static Parser<SequenceAST> oneOrMore(Parser<?> parser) {
        return map(sequence(parser, zeroOrMore(parser)),
                (SequenceAST ast) -> {
                    AST head = ast.values().get(0);
                    SequenceAST tail = ((SequenceAST) ast.values().get(1));
                    tail.values().add(0, head);

                    return tail;
                });
    }

    static Parser<ItemAST> item() {
        return input -> {
            if (input.isEmpty()) {
                throw ParseException.UNEXPECTED_EOF();
            }
            return new ParseResult<>(new ItemAST(input.charAt(0)), input.subSequence(1, input.length()));
        };
    }

    static Parser<LiteralAST> literal(@NotNull String literal) {
        return input -> {
            if (input.length() < literal.length()) {
                throw ParseException.UNEXPECTED_EOF();
            }
            if (input.subSequence(0, literal.length()).toString().equals(literal)) {
                return new ParseResult<>(new LiteralAST(literal), input.subSequence(literal.length(), input.length()));
            }

            throw ParseException.EXPECTED_LITERAL(literal);
        };
    }

    static Parser<RegexAST> regex(@NotNull String regex, @NotNull Supplier<ParseException> exceptionSupplier) {
        Pattern pattern = Pattern.compile(regex);
        return input -> {
            Matcher matcher = pattern.matcher(input);
            if (matcher.lookingAt()) {
                String[] groups = new String[matcher.groupCount()];
                for (int i = 1; i <= matcher.groupCount(); i++) {
                    groups[i - 1] = matcher.group(i);
                }
                return new ParseResult<>(new RegexAST(matcher.group(), groups), input.subSequence(matcher.end(), input.length()));
            }
            throw exceptionSupplier.get();
        };
    }

    static Parser<RegexAST> regex(@NotNull String regex, @NotNull String message) {
        return regex(regex, () -> new ParseException(message));
    }

    @SuppressWarnings("unchecked")
    static <T extends AST> Parser<T> surroundedBy(Parser<T> inner, Parser<?> surrounder) {
        return map(
                sequence(surrounder, inner, surrounder),
                (SequenceAST ast) -> (T) ast.values().get(1)
        );
    }

    static <T extends AST> Parser<T> skipWhitespace(Parser<T> inner) {
        return surroundedBy(inner, regex("\\s*", "Expected whitespace"));
    }

    /* TODO: Add the FloatLiteral type
    static Parser decimal() {
        return sequence(
                oneOf(
                        regex("[+-]?\\d+\\.\\d*", "Expected a decimal"),
                        regex("[+-]?\\d*\\.\\d+", "Expected a decimal")
                ),
                optional(
                        sequence(
                                regex("[eE]", "Expected an exponent"),
                                decimal()
                        )
                )
        );
    }
    */
}
