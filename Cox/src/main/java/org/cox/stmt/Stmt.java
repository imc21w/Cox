package org.cox.stmt;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;
import org.cox.anno.Describe;
import org.cox.call.StructInstance;
import org.cox.env.Environment;
import org.cox.expr.Expr;
import org.cox.token.Token;
import org.cox.token.TokenType;
import org.cox.utils.Pair;
import org.cox.visitor.ExprVisitor;
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

    @AllArgsConstructor
    @ToString
    @Getter
    @Describe("while语句")
    public static class While extends Stmt{
        private Stmt startStmt;   // for用
        private Expr conditionExpr;
        private Stmt bodyStmt;
        private Expr upExpr;        // for用

        @Override
        public void execute(StmtVisitor visitor) {
            visitor.visitWhile(this);
        }
    }

    @AllArgsConstructor
    @ToString
    @Getter
    @Describe("continue语句")
    public static class Continue extends Stmt{
        private Token con;

        @Override
        public void execute(StmtVisitor visitor) {
            visitor.visitContinue(this);
        }
    }

    @AllArgsConstructor
    @ToString
    @Getter
    @Describe("Break语句")
    public static class Break extends Stmt{
        private Token br;

        @Override
        public void execute(StmtVisitor visitor) {
            visitor.visitBreak(this);
        }
    }

    @AllArgsConstructor
    @ToString
    @Getter
    @Describe("fun语句")
    public static class Fun extends Stmt{
        private Token type;
        private Token method;
        private List<Token> params;
        private Block bodyStmt;

        @Override
        public void execute(StmtVisitor visitor) {
            visitor.visitFun(this);
        }
    }

    @Getter
    @AllArgsConstructor
    @Describe("Return")
    @ToString
    public static class Return extends Stmt {
        final Token returnToken;    // 记录位置用
        final Expr expr;

        @Override
        public void execute(StmtVisitor visitor) {
            visitor.visitReturn(this);
        }
    }

    @Getter
    @AllArgsConstructor
    @Describe("class")
    @ToString
    public static class Struct extends Stmt {
        final Token structName;
        final Token parentName;    // 父类
        final List<Fun> funList;

        @Override
        public void execute(StmtVisitor visitor) {
            visitor.visitStruct(this);
        }
    }
}
