package org.osta.parse;

import org.jetbrains.annotations.NotNull;
import org.osta.parse.ast.*;

import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@FunctionalInterface
public interface Parser {
    ParseResult parse(CharSequence input) throws ParseException;

    static Parser test(Parser parser, Predicate<AST> predicate, Supplier<ParseException> exceptionSupplier) {
        return input -> {
            ParseResult result = parser.parse(input);
            if (predicate.test(result.ast())) {
                return result;
            }
            throw exceptionSupplier.get();
        };
    }

    static Parser test(Parser parser, Predicate<AST> predicate, String message) {
        return test(parser, predicate, () -> new ParseException(message));
    }

    static Parser sequence(Parser... parsers) {
        if (parsers.length == 0) {
            throw new IllegalArgumentException("At least one parser must be provided");
        }

        return input -> {
            CharSequence rest = input;
            AST[] asts = new AST[parsers.length];
            for (int i = 0; i < parsers.length; i++) {
                ParseResult result = parsers[i].parse(rest);
                asts[i] = result.ast();
                rest = result.rest();
            }
            return new ParseResult(new SequenceAST(asts), rest);
        };
    }

    static Parser oneOf(Parser... parsers) {
        if (parsers.length == 0) {
            throw new IllegalArgumentException("At least one parser must be provided");
        }

        return input -> {
            ParseException exception = null;
            for (Parser parser : parsers) {
                try {
                    return parser.parse(input);
                } catch (ParseException e) {
                    exception = e;
                }
            }
            throw exception;
        };
    }

    static Parser optional(Parser parser) {
        return input -> {
            try {
                return parser.parse(input);
            } catch (ParseException e) {
                return new ParseResult(new EmptyAST(), input);
            }
        };
    }

    static Parser item() {
        return input -> {
            if (input.isEmpty()) {
                throw ParseException.UNEXPECTED_EOF();
            }
            return new ParseResult(new ItemAST(input.charAt(0)), input.subSequence(1, input.length()));
        };
    }

    static Parser literal(@NotNull String literal) {
        return input -> {
            if (input.length() < literal.length()) {
                throw ParseException.UNEXPECTED_EOF();
            }
            if (input.subSequence(0, literal.length()).toString().equals(literal)) {
                return new ParseResult(new LiteralAST(literal), input.subSequence(literal.length(), input.length()));
            }
            throw ParseException.EXPECTED_LITERAL(literal);
        };
    }

    static Parser regex(@NotNull String regex, @NotNull Supplier<ParseException> exceptionSupplier) {
        Pattern pattern = Pattern.compile(regex);
        return input -> {
            Matcher matcher = pattern.matcher(input);
            if (matcher.lookingAt()) {
                String[] groups = new String[matcher.groupCount()];
                for (int i = 1; i <= matcher.groupCount(); i++) {
                    groups[i - 1] = matcher.group(i);
                }
                return new ParseResult(new RegexAST(matcher.group(), groups), input.subSequence(matcher.end(), input.length()));
            }
            throw exceptionSupplier.get();
        };
    }

    static Parser regex(@NotNull String regex, @NotNull String message) {
        return regex(regex, () -> new ParseException(message));
    }

    static Parser integer() {
        return oneOf(
                regex("([+-]?\\d+)(?:[eE](\\+?\\d+))?", "Expected an integer"),
                regex("[+-]?0[bB][01]+", "Expected an integer"),
                regex("[+-]?0[oO][0-7]+", "Expected an integer"),
                regex("[+-]?0[xX][0-9a-fA-F]+", "Expected an integer")
        );
    }

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
}
