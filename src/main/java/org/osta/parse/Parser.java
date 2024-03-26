package org.osta.parse;

import org.osta.lex.Lexer;
import org.osta.lex.token.Token;
import org.osta.lex.token.TokenType;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class Parser {

    private final Lexer lexer;

    public Parser(Lexer lexer) {
        this.lexer = lexer;
    }

    public CST parse() throws UnexpectedTokenException {
        CST root = new CST(null);

        while (lexer.hasNext()) {
            root.child(parseConstruct());
        }

        return root;
    }

    private Token consume(TokenType type) throws UnexpectedTokenException {
        return lexer.nextOf(type)
                .orElseThrow(() -> new UnexpectedTokenException(type, lexer.slice(5), lexer.line(), lexer.column()));
    }

    private Token consume(TokenType... types) throws UnexpectedTokenException {
        return lexer.nextOf(types)
                .orElseThrow(() -> new UnexpectedTokenException(types, lexer.slice(5), lexer.line(), lexer.column()));
    }

    private Optional<Token> peek(TokenType... types) throws UnexpectedTokenException {
        try {
            lexer.backup();
            Token token = consume(types);
            lexer.returnToken(token);
            return Optional.of(token);
        } catch (UnexpectedTokenException e) {
            lexer.restore();
            return Optional.empty();
        }
    }

    @FunctionalInterface
    public interface ProductionRule {
        CST apply() throws UnexpectedTokenException;
    }

    private CST parseOneOf(ProductionRule... rules) throws UnexpectedTokenException {
        List<TokenType> exceptions = new ArrayList<>(rules.length);
        for (ProductionRule rule : rules) {
            try {
                lexer.backup();
                return rule.apply();
            } catch (UnexpectedTokenException e) {
                lexer.restore();
                exceptions.addAll(e.expected());
            } finally {
                lexer.commit();
            }
        }
        throw new UnexpectedTokenException(exceptions, lexer.slice(5), lexer.line(), lexer.column());
    }

    /**
     * Construct ::= Struct | Trait | Enum | VarDecl | Function
     *
     * @return CST
     * @see #parseStruct()
     * @see #parseTrait()
     * @see #parseEnum()
     * @see #parseFunction()
     */
    private CST parseConstruct() throws UnexpectedTokenException {
        Optional<Token> token = peek(TokenType.STRUCT, TokenType.TRAIT, TokenType.ENUM);

        if (token.isEmpty()) {
            return parseFunction();
        }

        return switch (token.get().type()) {
            case STRUCT -> parseStruct();
            case TRAIT -> parseTrait();
            case ENUM -> parseEnum();
            default -> null;
        };
    }

    /**
     * Type ::= Identifier | Identifier '<' '>' | Identifier '<' GenericTypes '>' | Type '*'
     *
     * @return CST
     * @see #parseGenericTypes(CST)
     */
    private CST parseType() throws UnexpectedTokenException {
        CST root = new CST(consume(TokenType.IDENTIFIER));
        try {
            lexer.backup();
            CST node = new CST(consume(TokenType.LESS));
            try {
                parseGenericTypes(node);
            } catch (UnexpectedTokenException e) {
                lexer.restore();
            }
            node.child(consume(TokenType.GREATER));
            root.child(node);
        } catch (UnexpectedTokenException e) {
            lexer.restore();
        } finally {
            lexer.commit();
        }

        CST current = root;
        while (peek(TokenType.STAR).isPresent()) {
            current = current.child(consume(TokenType.STAR));
        }

        return root;
    }

    /**
     * GenericTypes ::= Type | Type ',' GenericTypes
     *
     * @param parent CST
     * @see #parseType()
     */
    private void parseGenericTypes(CST parent) throws UnexpectedTokenException {
        parent.child(parseType());
        try {
            lexer.backup();
            parent.child(consume(TokenType.COMMA));
            parseGenericTypes(parent);
        } catch (UnexpectedTokenException e) {
            lexer.restore();
        } finally {
            lexer.commit();
        }
    }

    /**
     * InheritedType ::= Type ':' GenericTypes
     *
     * @return CST
     * @see #parseType()
     * @see #parseGenericTypes(CST)
     */
    private CST parseInheritedType() throws UnexpectedTokenException {
        CST root = parseType();
        try {
            lexer.backup();
            parseGenericTypes(root.child(consume(TokenType.COLON)));
        } catch (UnexpectedTokenException e) {
            lexer.restore();
        }
        return root;
    }

    /**
     * Struct ::= STRUCT Type '{' Fields '}' | STRUCT InheritedType '{' Fields '}'
     *
     * @return CST
     * @see #parseType()
     * @see #parseInheritedType()
     * @see #parseFields(CST)
     */
    private CST parseStruct() throws UnexpectedTokenException {
        CST root = new CST(consume(TokenType.STRUCT));
        CST body = root.child(parseInheritedType()).child(consume(TokenType.LEFT_BRACE));
        try {
            lexer.backup();
            parseOneOf(() -> {
                parseFunctions(body);
                return body;
            }, () -> {
                parseFields(body);
                return body;
            });
        } catch (UnexpectedTokenException e) {
            lexer.restore();
        } finally {
            lexer.commit();
        }
        body.child(consume(TokenType.RIGHT_BRACE));
        return root;
    }

    /**
     * Field ::= Type Identifier
     *
     * @return CST
     * @see #parseType()
     */
    private CST parseField() throws UnexpectedTokenException {
        CST root = parseType();
        root.child(consume(TokenType.IDENTIFIER));
        return root;
    }

    /**
     * Fields ::= Field ';' | Field ';' Fields
     *
     * @param parent CST
     * @see #parseField()
     */
    private void parseFields(CST parent) throws UnexpectedTokenException {
        CST field = parseField();
        field.ith(-1).child(consume(TokenType.SEMICOLON));
        parent.child(field);
        try {
            lexer.backup();
            parseFields(parent);
        } catch (UnexpectedTokenException e) {
            lexer.restore();
        } finally {
            lexer.commit();
        }
    }

    /**
     * Trait ::= TRAIT Type '{' FuncSigs '}' | TRAIT InheritedType '{' FuncSigs '}'
     *
     * @return CST
     * @see #parseType()
     * @see #parseInheritedType()
     * @see #parseFuncSigs(CST)
     */
    private CST parseTrait() throws UnexpectedTokenException {
        CST root = new CST(consume(TokenType.TRAIT));
        CST body = root.child(parseInheritedType()).child(consume(TokenType.LEFT_BRACE));
        try {
            lexer.backup();
            parseFuncSigs(body);
        } catch (UnexpectedTokenException e) {
            lexer.restore();
        } finally {
            lexer.commit();
        }
        body.child(consume(TokenType.RIGHT_BRACE));
        return root;
    }

    /**
     * Parameters ::= Type Identifier | Type Identifier ',' Parameters
     *
     * @param parent
     * @throws UnexpectedTokenException
     */
    private void parseParameters(CST parent) throws UnexpectedTokenException {
        CST parameter = parseType();
        parameter.child(consume(TokenType.IDENTIFIER));
        parent.child(parameter);
        try {
            lexer.backup();
            parent.child(consume(TokenType.COMMA));
            parseParameters(parent);
        } catch (UnexpectedTokenException e) {
            lexer.restore();
        }
    }

    /**
     * Method ::= Type Identifier '(' Parameters ')'
     *
     * @return CST
     * @see #parseType()
     * @see #parseParameters(CST)
     */
    private CST parseFuncSig() throws UnexpectedTokenException {
        CST root = parseType();
        CST current = root.child(consume(TokenType.IDENTIFIER)).child(consume(TokenType.LEFT_PAREN));
        try {
            lexer.backup();
            parseParameters(current);
        } catch (UnexpectedTokenException e) {
            lexer.restore();
        } finally {
            lexer.commit();
        }
        current.child(consume(TokenType.RIGHT_PAREN));
        return root;
    }

    /**
     * Methods ::= Method ';' | Method ';' Methods
     *
     * @param parent CST
     * @see #parseFuncSig()
     */
    private void parseFuncSigs(CST parent) throws UnexpectedTokenException {
        CST method = parseFuncSig();
        method.ith(-1).child(consume(TokenType.SEMICOLON));
        parent.child(method);
        try {
            lexer.backup();
            parseFuncSigs(parent);
        } catch (UnexpectedTokenException e) {
            lexer.restore();
        } finally {
            lexer.commit();
        }
    }

    /**
     * Enum ::= ENUM Identifier '{' EnumValues '}'
     *
     * @return CST
     */
    private CST parseEnum() throws UnexpectedTokenException {
        CST root = new CST(consume(TokenType.ENUM));
        root.child(consume(TokenType.IDENTIFIER)).child(consume(TokenType.LEFT_BRACE));
        try {
            lexer.backup();
            parseEnumValues(root);
        } catch (UnexpectedTokenException e) {
            lexer.restore();
        } finally {
            lexer.commit();
        }
        root.child(consume(TokenType.RIGHT_BRACE));
        return root;
    }

    /**
     * EnumValue ::= Identifier | Identifier '=' Integer
     *
     * @param parent CST
     */
    private void parseEnumValue(CST parent) throws UnexpectedTokenException {
        CST root = new CST(consume(TokenType.IDENTIFIER));
        try {
            lexer.backup();
            root.child(consume(TokenType.EQUAL)).child(consume(TokenType.INTEGER));
        } catch (UnexpectedTokenException e) {
            lexer.restore();
        } finally {
            lexer.commit();
        }
        parent.child(root);
    }

    /**
     * EnumValues ::= EnumValue | EnumValue ',' EnumValues
     *
     * @param parent CST
     * @see #parseEnumValue(CST)
     */
    private void parseEnumValues(CST parent) throws UnexpectedTokenException {
        parseEnumValue(parent);
        try {
            lexer.backup();
            parent.child(consume(TokenType.COMMA));
            parseEnumValues(parent);
        } catch (UnexpectedTokenException e) {
            lexer.restore();
        } finally {
            lexer.commit();
        }
    }

    /**
     * Function ::= FuncSig Block
     *
     * @return CST
     * @see #parseFuncSig()
     * @see #parseBlock()
     */
    private CST parseFunction() throws UnexpectedTokenException {
        try {
            lexer.backup();
            CST root = parseFuncSig();
            root.ith(0).child(parseBlock());
            return root;
        } catch (UnexpectedTokenException e) {
            lexer.restore();
            throw e;
        } finally {
            lexer.commit();
        }
    }

    /**
     * Functions ::= Function | Function Functions
     *
     * @param parent CST
     * @see #parseFunction()
     */
    private void parseFunctions(CST parent) throws UnexpectedTokenException {
        CST function = parseFunction();
        parent.child(function);
        try {
            lexer.backup();
            parseFunctions(parent);
        } catch (UnexpectedTokenException e) {
            lexer.restore();
        } finally {
            lexer.commit();
        }
    }

    /**
     * Block ::= '{' Statements '}'
     *
     * @return CST
     * @see #parseStatements(CST)
     */
    private CST parseBlock() throws UnexpectedTokenException {
        CST root = new CST(consume(TokenType.LEFT_BRACE));
        try {
            lexer.backup();
            parseStatements(root);
        } catch (UnexpectedTokenException e) {
            lexer.restore();
        } finally {
            lexer.commit();
        }
        root.child(consume(TokenType.RIGHT_BRACE));
        return root;
    }

    /**
     * Statement ::= Block | If | While | For | Do | BREAK ';' | CONTINUE ';' | Return | Defer | Expression ';'
     *
     * @see #parseBlock()
     * @see #parseIf()
     * @see #parseWhile()
     * @see #parseFor()
     * @see #parseDo()
     * @see #parseReturn()
     * @see #parseDefer()
     * @see #parseExpression()
     */
    private CST parseStatement() throws UnexpectedTokenException {
        Optional<Token> token = peek(TokenType.LEFT_BRACE, TokenType.IF, TokenType.WHILE, TokenType.FOR, TokenType.DO, TokenType.BREAK, TokenType.CONTINUE, TokenType.RETURN, TokenType.DEFER);

        if (token.isEmpty()) {
            CST root = parseExpression();
            root.child(consume(TokenType.SEMICOLON));
            return root;
        }

        switch (token.get().type()) {
            case LEFT_BRACE:
                return parseBlock();
            case IF:
                return parseIf();
            case WHILE:
                return parseWhile();
            case FOR:
                return parseFor();
            case DO:
                return parseDo();
            case BREAK: {
                CST root = new CST(consume(TokenType.BREAK));
                root.child(consume(TokenType.SEMICOLON));
                return root;
            }
            case CONTINUE: {
                CST root = new CST(consume(TokenType.CONTINUE));
                root.child(consume(TokenType.SEMICOLON));
                return root;
            }
            case RETURN:
                return parseReturn();
            case DEFER:
                return parseDefer();
            default:
                return null;
        }
    }

    /**
     * Statements ::= Statement | Statement Statements
     *
     * @param parent CST
     * @see #parseStatement()
     */
    private void parseStatements(CST parent) throws UnexpectedTokenException {
        parent.child(parseStatement());
        try {
            lexer.backup();
            parseStatements(parent);
        } catch (UnexpectedTokenException e) {
            lexer.restore();
        } finally {
            lexer.commit();
        }
    }

    /**
     * If ::= IF '(' Expression ')' Statement | IF '(' Expression ')' Statement ELSE Statement
     *
     * @return CST
     * @see #parseExpression()
     * @see #parseStatement()
     */
    private CST parseIf() throws UnexpectedTokenException {
        CST root = new CST(consume(TokenType.IF)).child(consume(TokenType.LEFT_PAREN));
        root.child(parseExpression()).child(consume(TokenType.RIGHT_PAREN));
        root.child(parseStatement());
        try {
            lexer.backup();
            root.child(consume(TokenType.ELSE)).child(parseStatement());
        } catch (UnexpectedTokenException e) {
            lexer.restore();
        } finally {
            lexer.commit();
        }
        return root;
    }

    /**
     * While ::= WHILE '(' Expression ')' Statement
     *
     * @return CST
     * @see #parseExpression()
     * @see #parseStatement()
     */
    private CST parseWhile() throws UnexpectedTokenException {
        CST root = new CST(consume(TokenType.WHILE)).child(consume(TokenType.LEFT_PAREN));
        root.child(parseExpression()).child(consume(TokenType.RIGHT_PAREN));
        root.child(parseStatement());
        return root;
    }

    /**
     * For ::= FOR '(' VarDecl ';' Expression ';' ForUpdate ')' Statement
     *
     * @return CST
     * @see #parseVarDecl()
     * @see #parseExpression()
     * @see #parseStatement()
     */
    private CST parseFor() throws UnexpectedTokenException {
        CST root = new CST(consume(TokenType.FOR)).child(consume(TokenType.LEFT_PAREN));
        root.child(parseVarDecl()).child(consume(TokenType.SEMICOLON));
        root.child(parseExpression()).child(consume(TokenType.SEMICOLON));
        root.child(parseStatement()).child(consume(TokenType.RIGHT_PAREN));
        root.child(parseStatement());
        return root;
    }

    /**
     * VarAssign ::= Path '=' Expression
     *
     * @return CST
     * @see #parsePath()
     * @see #parseExpression()
     */
    private CST parseVarAssign() throws UnexpectedTokenException {
        CST root = parsePath();
        root.child(consume(TokenType.EQUAL));
        root.child(parseExpression());
        return root;
    }

    /**
     * VarAssignChain ::= VarAssign | VarAssign ',' VarAssignChain
     *
     * @param parent CST
     * @see #parseVarAssign()
     */
    private void parseVarAssignChain(CST parent) throws UnexpectedTokenException {
        CST varAssign = parseVarAssign();
        parent.child(varAssign);
        try {
            lexer.backup();
            parent.child(consume(TokenType.COMMA));
            parseVarAssignChain(parent);
        } catch (UnexpectedTokenException e) {
            lexer.restore();
        } finally {
            lexer.commit();
        }
    }

    /**
     * VarDecl ::= Type Identifier | Type VarAssignChain
     *
     * @return CST
     * @see #parseType()
     */
    private CST parseVarDecl() throws UnexpectedTokenException {
        CST root = parseType();
        try {
            lexer.backup();
            parseVarAssignChain(root);
        } catch (UnexpectedTokenException e) {
            lexer.restore();
            root.child(consume(TokenType.IDENTIFIER));
        } finally {
            lexer.commit();
        }
        return root;
    }

    /**
     * Do ::= DO Statement WHILE '(' Expression ')' ';'
     *
     * @return CST
     * @see #parseStatement()
     * @see #parseExpression()
     */
    private CST parseDo() throws UnexpectedTokenException {
        CST root = new CST(consume(TokenType.DO));
        root.child(parseStatement()).child(consume(TokenType.WHILE)).child(consume(TokenType.LEFT_PAREN));
        root.child(parseExpression()).child(consume(TokenType.RIGHT_PAREN)).child(consume(TokenType.SEMICOLON));
        return root;
    }

    /**
     * Return ::= RETURN Expression ';'
     *
     * @return CST
     * @see #parseExpression()
     */
    private CST parseReturn() throws UnexpectedTokenException {
        CST root = new CST(consume(TokenType.RETURN));
        root.child(parseExpression()).child(consume(TokenType.SEMICOLON));
        return root;
    }

    /**
     * Defer ::= DEFER Statement
     *
     * @return CST
     * @see #parseStatement()
     */
    private CST parseDefer() throws UnexpectedTokenException {
        CST root = new CST(consume(TokenType.DEFER));
        root.child(parseStatement());
        return root;
    }

    /**
     * Expression ::= Term | VarAssign | Expression BinaryOp Expression | UnaryOp Expression
     *
     * @return CST
     * @see #parseVarAssign()
     * @see #parseBinaryOp()
     * @see #parseUnaryOp()
     * @see #parseTerm()
     */
    private CST parseExpression() throws UnexpectedTokenException {
        CST root = parseOneOf(this::parseVarAssign, this::parseUnaryOp, this::parseTerm);
        try {
            lexer.backup();
            root.child(parseBinaryOp()).child(parseExpression());
        } catch (UnexpectedTokenException e) {
            lexer.restore();
        } finally {
            lexer.commit();
        }
        return root;
    }

    /**
     * Term ::= Integer | Float | Identifier | '(' Expression ')'
     *
     * @return CST
     */
    private CST parseTerm() throws UnexpectedTokenException {
        return parseOneOf(
                () -> new CST(consume(TokenType.INTEGER)),
                () -> new CST(consume(TokenType.FLOAT)),
                () -> new CST(consume(TokenType.LEFT_PAREN)).child(parseExpression()).child(consume(TokenType.RIGHT_PAREN)),
                this::parseFuncCall,
                this::parsePath,
                () -> new CST(consume(TokenType.IDENTIFIER))
        );
    }

    /**
     * BinaryOp ::= '+' | '-' | '*' | '/' | '%' | '&' | '|' | '^' | '~' | '<<' | '>>' | '>>>' | '==' | '!=' | '<' | '<=' | '>' | '>='
     *
     * @return CST
     */
    private CST parseBinaryOp() throws UnexpectedTokenException {
        return new CST(consume(TokenType.PLUS, TokenType.MINUS, TokenType.STAR, TokenType.SLASH, TokenType.PERCENT,
                TokenType.AMPERSAND, TokenType.PIPE, TokenType.CARET, TokenType.TILDE, TokenType.LEFT_SHIFT, TokenType.RIGHT_SHIFT,
                TokenType.ARITHMETIC_RIGHT_SHIFT, TokenType.EQUAL_EQUAL, TokenType.NOT_EQUAL, TokenType.LESS, TokenType.LESS_EQUAL,
                TokenType.GREATER, TokenType.GREATER_EQUAL));
    }

    /**
     * UnaryOp ::= '+' | '-' | '!' | '~'
     *
     * @return CST
     */
    private CST parseUnaryOp() throws UnexpectedTokenException {
        return new CST(consume(TokenType.PLUS, TokenType.MINUS, TokenType.EXCLAMATION, TokenType.TILDE));
    }

    /**
     * Path ::= Identifier | '.' Identifier | Identifier '.' Path
     *
     * @return CST
     */
    private CST parsePath(boolean dot) throws UnexpectedTokenException {
        Token token = dot ? consume(TokenType.IDENTIFIER, TokenType.DOT) : consume(TokenType.IDENTIFIER);
        CST root = new CST(token);
        CST current = root;

        if (dot && token.type() == TokenType.DOT) {
            current = current.child(consume(TokenType.IDENTIFIER));
        }

        try {
            lexer.backup();
            current.interest(current.child(consume(TokenType.DOT)).child(parsePath()).interest());
        } catch (UnexpectedTokenException e) {
            lexer.restore();
        } finally {
            lexer.commit();
        }

        if (root.interest() == null) {
            root.interest(current);
        }

        return root;
    }

    private CST parsePath() throws UnexpectedTokenException {
        return parsePath(true);
    }

    /**
     * FuncCall ::= Path '(' Arguments ')'
     *
     * @return CST
     * @see #parsePath()
     * @see #parseArguments(CST)
     */
    private CST parseFuncCall() throws UnexpectedTokenException {
        CST root = parsePath();
        CST current = root.child(consume(TokenType.LEFT_PAREN));
        try {
            lexer.backup();
            parseArguments(current);
        } catch (UnexpectedTokenException e) {
            lexer.restore();
        } finally {
            lexer.commit();
        }
        current.child(consume(TokenType.RIGHT_PAREN));
        return root;
    }

    /**
     * Arguments ::= Expression | Expression ',' Arguments
     *
     * @param parent CST
     * @see #parseExpression()
     */
    private void parseArguments(CST parent) throws UnexpectedTokenException {
        CST argument = parseExpression();
        parent.child(argument);
        try {
            lexer.backup();
            parent.child(consume(TokenType.COMMA));
            parseArguments(parent);
        } catch (UnexpectedTokenException e) {
            lexer.restore();
        } finally {
            lexer.commit();
        }
    }

}
