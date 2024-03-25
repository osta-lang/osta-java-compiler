package org.osta.parse;

import org.osta.lex.token.Token;

import java.util.ArrayList;
import java.util.List;

public class CST {

    private final Token token;
    private final List<CST> children = new ArrayList<>();

    public CST(Token token) {
        this.token = token;
    }

    public CST child(CST cst) {
        children.add(cst);
        return cst;
    }

    public CST child(Token token) {
        return child(new CST(token));
    }

    public CST ith(int i) {
        if (children.isEmpty()) {
            return null;
        }

        i %= children.size();
        if (i < 0) {
            i += children.size();
        }

        return children.get(i);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        List<CST> pending = new ArrayList<>();
        pending.add(this);
        while (!pending.isEmpty()) {
            CST node = pending.remove(0);
            builder.append(node.hashCode());
            if (node.token != null) {
                builder.append(" [label=\"")
                        .append(node.token.type());
                if (node.token.value() != null) {
                    builder.append(": ").append(node.token.value().replaceAll("\"", "\\\\\""));
                }
            } else {
                builder.append(" [label=\"ROOT");
            }
            builder.append("\"]\n");

            for (CST child : node.children) {
                builder.append(node.hashCode()).append(" -> ").append(child.hashCode()).append("\n");
            }

            pending.addAll(node.children);
        }
        return builder.toString();
    }
}
