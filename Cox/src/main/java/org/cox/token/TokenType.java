package org.cox.token;

public enum TokenType {
    // Single-character tokens.
    LEFT_PAREN, RIGHT_PAREN, LEFT_BRACE, RIGHT_BRACE,
    COMMA, DOT, MINUS, PLUS, LINE_END, SLASH, STAR, MODE,

    // One or two character tokens.
    BANG, BANG_EQUAL,
    EQUAL, EQUAL_EQUAL,
    GREATER, GREATER_EQUAL,
    LESS, LESS_EQUAL,

    // Literals.
    IDENTIFIER, STRING, NUMBER,

    // Keywords.
    AND, STRUCT, ELSE, FALSE, FUN, FOR, IF, NULL, OR, WHEN, STATIC, EXTENDS,
    PRINT, RETURN, SUPER, THIS, TRUE, LET, WHILE, CONTINUE, BREAK,

    EOF
}
