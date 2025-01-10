package org.cox.token;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@AllArgsConstructor
@Getter
@ToString
public class Token {
    private TokenType type;
    private String lexeme;
    private Object literal;
    private int line;
}
