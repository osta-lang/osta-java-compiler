package org.osta.parse;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import org.osta.parse.ast.*;

public class StmtParseTest {
    @Test
    void simple() throws Exception {
        StmtAST.parser().parse("a = 1 + 1;");
    }
}
