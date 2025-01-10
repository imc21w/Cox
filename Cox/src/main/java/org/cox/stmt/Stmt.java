package org.cox.stmt;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;
import org.cox.anno.Describe;
import org.cox.expr.Expr;
import org.cox.token.Token;
import org.cox.visitor.StmtVisitor;

public abstract class Stmt {

    // 操作
    public abstract void execute(StmtVisitor visitor);

    @AllArgsConstructor
    @ToString
    @Getter
    @Describe(" 表达式; 构成语句")
    public static class Expression extends Stmt{
        private Expr expr;

        @Override
        public void execute(StmtVisitor visitor) {
            visitor.visitExpression(this);
        }
    }

    @AllArgsConstructor
    @ToString
    @Getter
    @Describe(" 输出函数 表达式; 构成语句")
    public static class Print extends Stmt{
        private Expr expr;

        @Override
        public void execute(StmtVisitor visitor) {
            visitor.visitPrint(this);
        }
    }

    @AllArgsConstructor
    @ToString
    @Getter
    @Describe("Let 变量 (= 表达式)?; 构成语句")
    public static class LET extends Stmt{
        private Token name;
        private Expr expr;

        @Override
        public void execute(StmtVisitor visitor) {
            visitor.visitLET(this);
        }
    }
}
