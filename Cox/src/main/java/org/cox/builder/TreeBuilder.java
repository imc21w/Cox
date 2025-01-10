package org.cox.builder;

import org.cox.expr.Expr;
import org.cox.token.Token;
import org.cox.token.TokenType;
import org.cox.utils.Cox;

import java.util.ArrayList;
import java.util.List;

/**
 * expression     → equality ;
 * equality       → comparison ( ( "!=" | "==" ) comparison )* ;
 * comparison     → term ( ( ">" | ">=" | "<" | "<=" ) term )* ;
 * term           → factor ( ( "-" | "+" ) factor )* ;
 * factor         → unary ( ( "/" | "*" ) unary )* ;
 * unary          → ( "!" | "-" ) unary
 *                | primary ;
 * primary        → NUMBER | STRING | "true" | "false" | "null"
 *                | "(" expression ")" ;
 */
public class TreeBuilder {

    private final List<Token> tokens;
    private int current = 0;

    public TreeBuilder(List<Token> tokens) {
        this.tokens = tokens;
    }

    public Expr expression(){
        return equality();
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
                return new Expr.Literal(Double.parseDouble(token.getLiteral().toString()));
            case STRING:
                return new Expr.Literal(token.getLiteral().toString());
            case TRUE:
                return new Expr.Literal(true);
            case FALSE:
                return new Expr.Literal(false);
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
