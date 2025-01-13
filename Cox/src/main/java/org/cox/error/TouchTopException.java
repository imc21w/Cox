package org.cox.error;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.cox.token.Token;
import org.cox.token.TokenType;

import java.util.HashMap;
import java.util.Map;

@Getter
@AllArgsConstructor
public class TouchTopException extends RuntimeException{

    private final Token token;

    final static Map<TokenType, String> typeMap = new HashMap<>();

    static {
        typeMap.put(TokenType.CONTINUE, "continue语句只能被定义在循环语句中");
        typeMap.put(TokenType.BREAK, "break语句只能被定义在循环语句中");
        typeMap.put(TokenType.RETURN, "return语句只能被定义在函数中");
    }

    @Override
    public String getMessage() {
        return "第" + token.getLine() + "行发生了错误, msg:" + typeMap.get(token.getType());
    }
}
