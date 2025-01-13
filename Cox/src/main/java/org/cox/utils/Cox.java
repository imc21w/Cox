package org.cox.utils;

import org.cox.builder.TreeBuilder;
import org.cox.error.TouchTopException;
import org.cox.scanner.TokenScanner;
import org.cox.token.Token;
import org.cox.visitor.EvaluateVisitor;

import java.util.List;

public class Cox {
    public static void error(int line, String msg) {
        throw new RuntimeException("第" + line + "行发生了错误, msg:" + msg);
    }

    public static void run(String source) {
        List<Token> tokens = TokenScanner.readTokens(source);
        TreeBuilder treeBuilder = new TreeBuilder(tokens);
        treeBuilder.parse().execute(new EvaluateVisitor());
    }

    public static void touchTop(Token token){
        throw new TouchTopException(token);
    }

}
