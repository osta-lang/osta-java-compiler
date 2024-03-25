package org.osta.lex.token;

@FunctionalInterface
public interface TokenProducer {

    Token produce(String value, int line, int column);

}
