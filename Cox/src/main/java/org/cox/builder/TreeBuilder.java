package org.cox.builder;

import org.cox.expr.Expr;
import org.cox.stmt.Stmt;
import org.cox.token.Token;
import org.cox.token.TokenType;
import org.cox.utils.Cox;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * expression     -> assign ;
 * assign         -> identifier "=" equality | equality;
 * equality       -> comparison ( ( "!=" | "==" ) comparison )* ;
 * comparison     -> term ( ( ">" | ">=" | "<" | "<=" ) term )* ;
 * term           -> factor ( ( "-" | "+" ) factor )* ;
 * factor         -> unary ( ( "/" | "*" ) unary )* ;
 * unary          -> ( "!" | "-" ) unary
 *                | primary ;
 * primary        -> NUMBER | STRING | "true" | "false" | "null"
 *                | "(" expression ")" | identifier ;
 */

/**
 * stmt           -> expressionStmt | print | let | block
 * block          -> "{" stmt* "}"
 * let            -> "let" identifier ( "=" expression ) ?
 * print          -> "print" expression ";"
 * expressionStmt -> expression ";"
 */
public class TreeBuilder {

    private final List<Token> tokens;
    private int current = 0;

    public TreeBuilder(List<Token> tokens) {
        this.tokens = tokens;
    }

    public List<Stmt> parse(){
        List<Stmt> stmts = new ArrayList<>();

        while (!isAtEnd()){
            stmts.add(stmt());
        }

        return stmts;
    }

    private Stmt stmt() {
        Token token = peek();

        switch (token.getType()){
            case PRINT: {
                advance();
                return buildPrintStmt();
            }

            case LET: {
                advance();
                return buildLetStmt();
            }

            case LEFT_BRACE: {
                advance();
                return buildLetBlock();
            }

            default:
                return buildExprStmt();
        }
    }

    private Stmt buildLetBlock() {
        List<Stmt> stmts = new ArrayList<>();

        while (!isAtEnd() && !check(TokenType.RIGHT_BRACE)){
            stmts.add(stmt());
        }

        Assert(TokenType.RIGHT_BRACE, "未闭合右括号");

        return new Stmt.Block(stmts);
    }

    private Stmt buildLetStmt() {

        Assert(TokenType.IDENTIFIER, "不能定义非变量");

        Token name = previous();
        Expr expr = null;

        if (match(TokenType.EQUAL))
            expr = expression();

        Assert(TokenType.LINE_END, "语句必须以 ; 结尾");
        return new Stmt.LET(name, expr);
    }

    private Stmt buildExprStmt() {
        Expr expr = expression();
        Assert(TokenType.LINE_END, "语句必须以 ; 结尾");
        return new Stmt.Expression(expr);
    }

    private Stmt buildPrintStmt() {
        Expr expr = expression();
        Assert(TokenType.LINE_END, "语句必须以 ; 结尾");
        return new Stmt.Print(expr);
    }

    private Expr expression(){
        return assign();
    }

    private Expr assign() {
        Expr expr = equality();

        if (match(TokenType.EQUAL)){
            Expr equality = assign();
            return new Expr.Assign(((Expr.Variable) expr).getName(), equality);
        }

        return expr;
    }

    private Expr equality() {
        Expr left = comparison();

        while (match(TokenType.BANG_EQUAL, TokenType.EQUAL_EQUAL)){
            Token token = previous();
            Expr right = comparison();

            left = new Expr.Binary(left, token, right);
        }

        return left;
    }

    private Expr comparison() {
        Expr left = term();

        while (match(TokenType.GREATER, TokenType.GREATER_EQUAL, TokenType.LESS, TokenType.LESS_EQUAL)){
            Token token = previous();
            Expr right = term();

            left = new Expr.Binary(left, token, right);
        }

        return left;
    }

    private Expr term() {
        Expr left = factor();

        while (match(TokenType.PLUS, TokenType.MINUS)){
            Token token = previous();
            Expr right = factor();

            left = new Expr.Binary(left, token, right);
        }

        return left;
    }

    private Expr factor() {
        Expr left = unary();

        while (match(TokenType.STAR, TokenType.SLASH)){
            Token token = previous();
            Expr right = unary();

            left = new Expr.Binary(left, token, right);
        }

        return left;
    }

    private Expr unary() {
        
        if (match(TokenType.MINUS, TokenType.BANG)){
            Token token = previous();
            Expr right = unary();
            
            return new Expr.Unary(token, right);
        }
        
        return primary();
    }

    private Expr primary() {

        Token token = advance();

        switch (token.getType()){
            case NULL:
                return new Expr.Literal(null);
            case NUMBER:
                return new Expr.Literal(new BigDecimal(token.getLiteral().toString()));
            case STRING:
                return new Expr.Literal(token.getLiteral().toString());
            case TRUE:
                return new Expr.Literal(true);
            case FALSE:
                return new Expr.Literal(false);
            case IDENTIFIER:
                return new Expr.Variable(token);
            case LEFT_PAREN:
                Expr expr = expression();
                Assert(TokenType.RIGHT_PAREN, "必须有匹配的右括号");
                return new Expr.Grouping(expr);
            default:
                Cox.error(peek().getLine(), "未知的字符");
        }

        return null;
    }

    private boolean match(TokenType... tokenTypes) {

        if (isAtEnd())
            return false;

        for (TokenType tokenType : tokenTypes) {
            if (check(tokenType)) {
                advance();
                return true;
            }
        }

        return false;
    }

    private boolean check(TokenType tokenType){
        return peek().getType() == tokenType;
    }

    private Token peek(){
        return tokens.get(current);
    }

    private boolean isAtEnd(){
        if (current >= tokens.size()) return true;
        return tokens.get(current).getType() == TokenType.EOF;
    }

    private Token advance(){
        if (!isAtEnd()) current++;
        return previous();
    }

    protected Token previous() {
        return tokens.get(current-1);
    }

    protected void Assert(TokenType tokenType, String message){
        if (!match(tokenType))
            Cox.error(peek().getLine(), message);
    }
}
