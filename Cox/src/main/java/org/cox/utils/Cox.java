package org.cox.utils;

import org.cox.scanner.TokenScanner;
import org.cox.token.Token;

import java.util.List;

public class Cox {
    public static void error(int line, String msg) {
        throw new RuntimeException("第" + line + "行发生了错误, msg:" + msg);
    }

    public static List<Token> readTokens(String source) {
        return TokenScanner.readTokens(source);
    }
}
