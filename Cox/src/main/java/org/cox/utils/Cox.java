package org.cox.utils;

import org.cox.builder.TreeBuilder;
import org.cox.error.TouchTopException;
import org.cox.scanner.TokenScanner;
import org.cox.stmt.Stmt;
import org.cox.token.Token;
import org.cox.visitor.EvaluateVisitor;
import org.cox.visitor.VerifyVisitor;

import java.util.List;

public class Cox {
    public static void error(int line, String msg) {
        throw new RuntimeException("第" + line + "行发生了错误, msg:" + msg);
    }

    public static void run(String source) {
        List<Token> tokens = TokenScanner.readTokens(source);
        TreeBuilder treeBuilder = new TreeBuilder(tokens);
        Stmt stmt = treeBuilder.parse();        // 编译
        stmt.execute(new VerifyVisitor());      // 验证
        stmt.execute(new EvaluateVisitor());    // 执行
    }

    public static void touchTop(Token token){
        throw new TouchTopException(token);
    }

}
