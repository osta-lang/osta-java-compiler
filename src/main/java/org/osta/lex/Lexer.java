package org.osta.lex;

import org.osta.lex.token.Token;
import org.osta.lex.token.TokenRule;
import org.osta.lex.token.TokenType;
import org.osta.lex.token.registry.TokenProducerRegistry;
import org.osta.text.BufferCharSequence;

import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Optional;
import java.util.Stack;

public class Lexer implements AutoCloseable {

    private static final TokenProducerRegistry REGISTRY = TokenProducerRegistry.builder()
            // Keywords
            .register(TokenType.STRUCT)
            .register(TokenType.TRAIT)
            .register(TokenType.ENUM)
            .register(TokenType.AUTO)
            .register(TokenType.STATIC)
            .register(TokenType.CONST)
            .register(TokenType.IF)
            .register(TokenType.ELSE)
            .register(TokenType.WHILE)
            .register(TokenType.FOR)
            .register(TokenType.DO)
            .register(TokenType.BREAK)
            .register(TokenType.CONTINUE)
            .register(TokenType.RETURN)
            .register(TokenType.DEFER)
            // Types
            .register(TokenType.I8)
            .register(TokenType.I16)
            .register(TokenType.I32)
            .register(TokenType.I64)
            .register(TokenType.I128)
            .register(TokenType.U8)
            .register(TokenType.U16)
            .register(TokenType.U32)
            .register(TokenType.U64)
            .register(TokenType.U128)
            .register(TokenType.F32)
            .register(TokenType.F64)
            .register(TokenType.USIZE)
            // Literals
            .register(TokenRule.of(TokenType.IDENTIFIER, "^[a-zA-Z_][a-zA-Z0-9_]*", (value, line1, column1) -> new Token(TokenType.IDENTIFIER, value, line1, column1)))
            .register(TokenRule.of(TokenType.INTEGER, "^-?[0-9]+", (value, line1, column1) -> new Token(TokenType.INTEGER, value, line1, column1)))
            .register(TokenRule.of(TokenType.INTEGER, "^-?0[bB][01]+", (value, line1, column1) -> new Token(TokenType.INTEGER, value, line1, column1)))
            .register(TokenRule.of(TokenType.INTEGER, "^-?0[oO][0-7]+", (value, line1, column1) -> new Token(TokenType.INTEGER, value, line1, column1)))
            .register(TokenRule.of(TokenType.INTEGER, "^-?0[xX][0-9a-fA-F]+", (value, line1, column1) -> new Token(TokenType.INTEGER, value, line1, column1)))
            .register(TokenRule.of(TokenType.FLOAT, "^-?[0-9]+\\.[0-9]+", (value, line1, column1) -> new Token(TokenType.FLOAT, value, line1, column1)))
            .register(TokenRule.of(TokenType.FLOAT, "^-?[0-9]+\\.[0-9]+[eE][+-]?[0-9]+", (value, line1, column1) -> new Token(TokenType.FLOAT, value, line1, column1)))
            .register(TokenRule.of(TokenType.STRING, "^\"(?:[^\"\\\\]|\\\\.)*\"", (value, line1, column1) -> new Token(TokenType.STRING, value, line1, column1)))
            .register(TokenRule.of(TokenType.CHAR, "^'(?:[^'\\\\]|\\\\.)'", (value, line1, column1) -> new Token(TokenType.CHAR, value, line1, column1)))
            // Delimiters
            .register(TokenRule.of(TokenType.LEFT_PAREN, "^\\("))
            .register(TokenRule.of(TokenType.RIGHT_PAREN, "^\\)"))
            .register(TokenRule.of(TokenType.LEFT_BRACE, "^\\{"))
            .register(TokenRule.of(TokenType.RIGHT_BRACE, "^\\}"))
            .register(TokenRule.of(TokenType.LEFT_BRACKET, "^\\["))
            .register(TokenRule.of(TokenType.RIGHT_BRACKET, "^\\]"))
            .register(TokenRule.of(TokenType.COMMA, "^,"))
            .register(TokenRule.of(TokenType.SEMICOLON, "^;"))
            .register(TokenRule.of(TokenType.COLON, "^:"))
            .register(TokenRule.of(TokenType.DOT, "^\\."))
            .register(TokenRule.of(TokenType.QUESTION, "^\\?"))
            .register(TokenRule.of(TokenType.EXCLAMATION, "^!"))
            // Operators
            .register(TokenRule.of(TokenType.PLUS, "^\\+"))
            .register(TokenRule.of(TokenType.MINUS, "^-"))
            .register(TokenRule.of(TokenType.STAR, "^\\*"))
            .register(TokenRule.of(TokenType.SLASH, "^/"))
            .register(TokenRule.of(TokenType.PERCENT, "^%"))
            .register(TokenRule.of(TokenType.AMPERSAND, "^&"))
            .register(TokenRule.of(TokenType.PIPE, "^\\|"))
            .register(TokenRule.of(TokenType.CARET, "^\\^"))
            .register(TokenRule.of(TokenType.TILDE, "^~"))
            .register(TokenRule.of(TokenType.LEFT_SHIFT, "^<<"))
            .register(TokenRule.of(TokenType.RIGHT_SHIFT, "^>>"))
            .register(TokenRule.of(TokenType.ARITHMETIC_RIGHT_SHIFT, "^>>>"))
            // Assignment
            .register(TokenRule.of(TokenType.EQUAL, "^="))
            .register(TokenRule.of(TokenType.PLUS_EQUAL, "^\\+="))
            .register(TokenRule.of(TokenType.MINUS_EQUAL, "^-="))
            .register(TokenRule.of(TokenType.STAR_EQUAL, "^\\*="))
            .register(TokenRule.of(TokenType.SLASH_EQUAL, "^/="))
            .register(TokenRule.of(TokenType.PERCENT_EQUAL, "^%="))
            .register(TokenRule.of(TokenType.AMPERSAND_EQUAL, "^&="))
            .register(TokenRule.of(TokenType.PIPE_EQUAL, "^\\|="))
            .register(TokenRule.of(TokenType.CARET_EQUAL, "^\\^="))
            .register(TokenRule.of(TokenType.LEFT_SHIFT_EQUAL, "^<<="))
            .register(TokenRule.of(TokenType.RIGHT_SHIFT_EQUAL, "^>>="))
            .register(TokenRule.of(TokenType.ARITHMETIC_RIGHT_SHIFT_EQUAL, "^>>>="))
            // Comparison
            .register(TokenRule.of(TokenType.EQUAL_EQUAL, "^=="))
            .register(TokenRule.of(TokenType.NOT_EQUAL, "^!="))
            .register(TokenRule.of(TokenType.LESS, "^<"))
            .register(TokenRule.of(TokenType.LESS_EQUAL, "^<="))
            .register(TokenRule.of(TokenType.GREATER, "^>"))
            .register(TokenRule.of(TokenType.GREATER_EQUAL, "^>="))
            // Misc
            .register(TokenRule.of(TokenType.ARROW, "^->"))
            .build();

    private final RandomAccessFile file;
    private final FileChannel channel;
    private final MappedByteBuffer buffer;
    private final Stack<Integer> backup = new Stack<>();
    private final Stack<Token> returned = new Stack<>();
    private int line = 0;
    private int column = 0;

    public Lexer(String path) throws Exception {
        file = new RandomAccessFile(path, "r");
        channel = file.getChannel();
        buffer = channel.map(FileChannel.MapMode.READ_ONLY, 0, channel.size());
    }

    private void skipWhitespace() {
        while (buffer.hasRemaining()) {
            char c = (char) buffer.get();
            if (c == '\n') {
                ++line;
                column = 0;
            } else if (Character.isWhitespace(c)) {
                ++column;
            } else {
                buffer.position(buffer.position() - 1);
                break;
            }
        }
    }

    public Optional<Token> nextOf(TokenType... types) {
        if (!returned.isEmpty()) {
            return Optional.of(returned.pop());
        }

        while (buffer.hasRemaining()) {
            skipWhitespace();

            Token selected = null;
            for (TokenType type : types) {
                Optional<Token> tokenOptional = REGISTRY.produce(type, new BufferCharSequence(buffer), line, column);
                if (tokenOptional.isPresent()) {
                    Token token = tokenOptional.get();
                    if (selected == null || token.length() > selected.length()) {
                        selected = token;
                    }
                }
            }
            if (selected == null) {
                return Optional.empty();
            }

            buffer.position(buffer.position() + selected.length());
            column += selected.length();

            return Optional.of(selected);
        }

        return Optional.of(new Token(TokenType.EOF, "", line, column));
    }

    @Override
    public void close() throws Exception {
        channel.close();
        file.close();
    }

    public String slice(int length) {
        int position = buffer.position();
        length = Math.min(length, buffer.remaining());
        byte[] bytes = new byte[length];
        buffer.get(bytes);
        buffer.position(position);
        return new String(bytes);
    }

    public int line() {
        return line;
    }

    public int column() {
        return column;
    }

    public void backup() {
        backup.push(buffer.position());
    }

    public void restore() {
        buffer.position(backup.pop());
    }

    public void returnToken(Token token) {
        returned.push(token);
    }

    public boolean hasNext() {
        skipWhitespace();
        return buffer.hasRemaining();
    }
}
