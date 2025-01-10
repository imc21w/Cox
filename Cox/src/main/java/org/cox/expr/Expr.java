package org.cox.expr;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.ToString;
import org.cox.anno.Describe;
import org.cox.token.Token;
import org.cox.visitor.ExprVisitor;

// 表达式
public abstract class Expr {

    // 使用访问者模式访问 Expr
    public abstract Object execute(ExprVisitor visitor);

    @Getter
    @AllArgsConstructor
    @Describe("左表达式 操作符 右表达式")
    @ToString
    public static class Binary extends Expr {
        final Expr left;
        final Token operator;
        final Expr right;

        @Override
        public Object execute(ExprVisitor visitor) {
            return visitor.visitBinary(this);
        }
    }

    @Getter
    @AllArgsConstructor
    @Describe("（ 表达式 ）")
    @ToString
    public static class Grouping extends Expr {
        final Expr expression;

        @Override
        public Object execute(ExprVisitor visitor) {
            return visitor.visitGrouping(this);
        }
    }

    @Getter
    @AllArgsConstructor
    @Describe("一个单独的字面量")
    @ToString
    public static class Literal extends Expr {
        final Object value;

        @Override
        public Object execute(ExprVisitor visitor) {
            return visitor.visitLiteral(this);
        }
    }

    @Getter
    @AllArgsConstructor
    @Describe("操作符 右表达式 （取反，取负操作）")
    @ToString
    public static class Unary extends Expr {
        final Token operator;
        final Expr right;

        @Override
        public Object execute(ExprVisitor visitor) {
            return visitor.visitUnary(this);
        }
    }
}
