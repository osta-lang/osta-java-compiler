package org.osta.lex.token;

import java.util.Locale;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public record TokenRule(TokenType type, Pattern pattern, TokenProducer producer) {

    public static TokenRule of(TokenType type, String regex, TokenProducer producer) {
        return new TokenRule(type, Pattern.compile(regex), producer);
    }

    public static TokenRule of(TokenType type, String regex) {
        return of(type, regex, (value, line, column) -> new Token(type, value, line, column));
    }

    public static TokenRule of(TokenType tokenType) {
        return new TokenRule(
                tokenType,
                Pattern.compile("^" + tokenType.toString().toLowerCase(Locale.ROOT)),
                (value, line, column) -> new Token(tokenType, value, line, column)
        );
    }

    public Optional<Token> produce(CharSequence input, int line, int column) {
        Matcher matcher = pattern.matcher(input);
        if (matcher.find()) {
            return Optional.of(producer.produce(matcher.group(), line, column));
        }
        return Optional.empty();
    }

}
