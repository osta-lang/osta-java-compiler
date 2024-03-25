package org.osta.lex.token.registry;

import org.osta.lex.token.Token;
import org.osta.lex.token.TokenRule;
import org.osta.lex.token.TokenType;

import java.nio.ByteBuffer;
import java.util.*;

public class TokenProducerRegistry {

    private final Map<TokenType, List<TokenRule>> rules;

    public TokenProducerRegistry(Map<TokenType, List<TokenRule>> rules) {
        this.rules = Map.copyOf(rules);
    }

    public Optional<Token> produce(TokenType type, CharSequence value, int line, int column) {
        return rules.getOrDefault(type, List.of())
                .stream()
                .map(rule -> rule.produce(value, line, column))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .findFirst();
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final Map<TokenType, List<TokenRule>> rules = new HashMap<>();

        private Builder() {
        }

        public Builder register(TokenType type, TokenRule rule) {
            this.rules.computeIfAbsent(type, k -> new ArrayList<>()).add(rule);
            return this;
        }

        public Builder register(TokenRule rule) {
            return register(rule.type(), rule);
        }

        public Builder register(TokenType tokenType) {
            return register(TokenRule.of(tokenType));
        }

        public Builder registerAll(List<TokenRule> rules) {
            rules.forEach(rule -> register(rule.type(), rule));
            return this;
        }

        public TokenProducerRegistry build() {
            return new TokenProducerRegistry(rules);
        }
    }

}
