package org.osta;

import org.osta.lex.Lexer;
import org.osta.parse.Parser;

public class Main {

    public static void main(String[] args) {
        try (Lexer lexer = new Lexer("examples/program.osta")) {
            Parser parser = new Parser(lexer);
            System.out.println(parser.parse().toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
