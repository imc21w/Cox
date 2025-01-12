package org.cox.builder;

import org.cox.expr.Expr;
import org.cox.stmt.Stmt;
import org.cox.token.Token;
import org.cox.token.TokenType;
import org.cox.utils.Cox;
import org.cox.utils.Pair;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * expression     -> assign ;
 * assign         -> identifier "=" logic_or | logic_or;
 * logic_or       -> logic_and ("or" logic_and) *
 * logic_and      -> equality ("and" equality) *
 * equality       -> comparison ( ( "!=" | "==" ) comparison )* ;
 * comparison     -> term ( ( ">" | ">=" | "<" | "<=" ) term )* ;
 * term           -> mode ( ( "-" | "+" ) mode )* ;
 * mode           -> factor ("%" factor)?;
 * factor         -> unary ( ( "/" | "*" ) unary )* ;
 * unary          -> ( "!" | "-" ) unary
 *                | primary ;
 * primary        -> NUMBER | STRING | "true" | "false" | "null"
 *                | "(" expression ")" | identifier ;
 */

/**
 * stmt           -> expressionStmt | print | let | block | if | while
 * if             -> "(" expression ")" stmt ("when" "(" expression ")" stmt)* ("else" stmt)?
 * while          -> "(" expression ")" stmt
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

    public Stmt parse(){
        List<Stmt> stmts = new ArrayList<>();

        while (!isAtEnd()){
            stmts.add(stmt());
        }

        return new Stmt.Block(stmts);
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
                return buildBlock();
            }

            case IF: {
                advance();
                return buildIF();
            }

            case WHILE: {
                advance();
                return buildWhile();
            }

            case FOR: {
                advance();
                return buildFor();
            }

            case CONTINUE: {
                advance();
                Assert(TokenType.LINE_END, "语句必须以 ; 结尾");
                return new Stmt.Continue(token);
            }

            case BREAK: {
                advance();
                Assert(TokenType.LINE_END, "语句必须以 ; 结尾");
                return new Stmt.Break(token);
            }

            default:
                return buildExprStmt();
        }
    }

    private Stmt buildFor() {
        Assert(TokenType.LEFT_PAREN, "for 后必须接括号");

        Stmt startStmt = null; // 初始条件

        if (match(TokenType.LET)){
            startStmt = buildLetStmt();
        }else if (!match(TokenType.LINE_END)){
            startStmt = new Stmt.Expression(expression());
            Assert(TokenType.LINE_END, "语句必须以 ; 结尾");
        }
        Expr condition = new Expr.Literal(true);
        if (!match(TokenType.LINE_END)){
            condition = expression();
            Assert(TokenType.LINE_END, "语句必须以 ; 结尾");
        }
        Expr expr = new Expr.Literal(true);
        if (!match(TokenType.RIGHT_PAREN)){
            expr = expression();
            Assert(TokenType.RIGHT_PAREN, "for 的括号表达式未闭合");
        }
        Stmt bodyStmt = new Stmt.Block(List.of(stmt()));

        return new Stmt.While(startStmt, condition, bodyStmt, expr);

    }

    private Stmt buildWhile() {
        Assert(TokenType.LEFT_PAREN, "while 后必须接括号");
        Expr expr = expression();
        Assert(TokenType.RIGHT_PAREN, "while 的括号表达式未闭合");
        Stmt stmt = new Stmt.Block(List.of(stmt()));

        return new Stmt.While(null, expr, stmt, null);
    }

    private Stmt buildIF() {
        Assert(TokenType.LEFT_PAREN, "if 后必须接括号");
        Expr expr = expression();
        Assert(TokenType.RIGHT_PAREN, "if 的括号表达式未闭合");
        Stmt stmt = stmt();
        Stmt elseStmt = null;

        List<Pair<Expr, Stmt>> ifList = new ArrayList<>();
        ifList.add(Pair.of(expr, new Stmt.Block(List.of(stmt))));

        while (match(TokenType.WHEN)){
            Assert(TokenType.LEFT_PAREN, "if 后必须接括号");
            Expr expr1 = expression();
            Assert(TokenType.RIGHT_PAREN, "if 的括号表达式未闭合");
            Stmt stmt1 = stmt();
            ifList.add(Pair.of(expr1, new Stmt.Block(List.of(stmt1))));
        }

        if (match(TokenType.ELSE)){
            elseStmt = new Stmt.Block(List.of(stmt()));
        }

        return new Stmt.IF(ifList, elseStmt);
    }

    private Stmt buildBlock() {
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
        Expr expr = logicOr();

        if (match(TokenType.EQUAL)){
            Expr equality = logicOr();
            return new Expr.Assign(((Expr.Variable) expr).getName(), equality);
        }

        return expr;
    }

    private Expr logicOr() {
        Expr expr = logicAnd();

        while (match(TokenType.OR)){
            Expr orExpr = logicAnd();
            expr = new Expr.Or(expr, orExpr);
        }

        return expr;
    }

    private Expr logicAnd() {
        Expr expr = equality();

        while (match(TokenType.AND)){
            Expr orExpr = equality();
            expr = new Expr.And(expr, orExpr);
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
        Expr left = mode();

        while (match(TokenType.PLUS, TokenType.MINUS)){
            Token token = previous();
            Expr right = mode();

            left = new Expr.Binary(left, token, right);
        }

        return left;
    }

    private Expr mode() {
        Expr left = factor();

        while (match(TokenType.MODE)){
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
