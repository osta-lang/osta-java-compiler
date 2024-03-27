package org.osta.parse;

import org.osta.parse.ast.AST;

public record ParseResult(AST ast, CharSequence rest) {
}
