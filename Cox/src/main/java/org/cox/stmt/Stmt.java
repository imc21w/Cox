package org.cox.stmt;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;
import org.cox.anno.Describe;
import org.cox.expr.Expr;
import org.cox.token.Token;
import org.cox.utils.Pair;
import org.cox.visitor.StmtVisitor;

import java.util.List;

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
    @Describe("let 变量 (= 表达式)?; 构成语句")
    public static class LET extends Stmt{
        private Token name;
        private Expr expr;

        @Override
        public void execute(StmtVisitor visitor) {
            visitor.visitLET(this);
        }
    }

    @AllArgsConstructor
    @ToString
    @Getter
    @Describe("{...} 构成语句")
    public static class Block extends Stmt{
        private List<Stmt> stmts;

        @Override
        public void execute(StmtVisitor visitor) {
            visitor.visitBlock(this);
        }
    }

    @AllArgsConstructor
    @ToString
    @Getter
    @Describe("IF语句")
    public static class IF extends Stmt{
        private List<Pair<Expr, Stmt>> ifList;
        private Stmt elseStmt;

        @Override
        public void execute(StmtVisitor visitor) {
            visitor.visitIF(this);
        }
    }
}
