package org.osta.parse;

import org.osta.parse.ast.AST;

public record ParseResult<T extends AST>(T ast, CharSequence rest) {

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        ParseResult<?> that = (ParseResult<?>) obj;
        if (!ast.equals(that.ast)) {
            return false;
        }
        return rest.toString().equals(that.rest.toString());
    }

}
