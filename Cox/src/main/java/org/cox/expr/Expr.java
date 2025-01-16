package org.cox.expr;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.ToString;
import org.cox.anno.Describe;
import org.cox.token.Token;
import org.cox.visitor.ExprVisitor;

import java.util.List;

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

    @Getter
    @AllArgsConstructor
    @Describe("变量")
    @ToString
    public static class Variable extends Expr {
        final Token name;

        @Override
        public Object execute(ExprVisitor visitor) {
            return visitor.visitVariable(this);
        }
    }

    @Getter
    @AllArgsConstructor
    @Describe("赋值")
    @ToString
    public static class Assign extends Expr {
        final Token name;
        final Expr expr;

        @Override
        public Object execute(ExprVisitor visitor) {
            return visitor.visitAssign(this);
        }
    }

    @Getter
    @AllArgsConstructor
    @Describe("or")
    @ToString
    public static class Or extends Expr {
        final Expr left;
        final Expr right;

        @Override
        public Object execute(ExprVisitor visitor) {
            return visitor.visitOr(this);
        }
    }

    @Getter
    @AllArgsConstructor
    @Describe("and")
    @ToString
    public static class And extends Expr {
        final Expr left;
        final Expr right;

        @Override
        public Object execute(ExprVisitor visitor) {
            return visitor.visitAnd(this);
        }
    }

    @Getter
    @AllArgsConstructor
    @Describe("call")
    @ToString
    public static class Call extends Expr {
        final Expr method;
        final Token paren;  // 右括号，用来定位
        final List<Expr> arguments;

        @Override
        public Object execute(ExprVisitor visitor) {
            return visitor.visitCall(this);
        }
    }

    @Getter
    @AllArgsConstructor
    @Describe("getXXX方法")
    @ToString
    public static class Get extends Expr {
        final Expr expr;
        final Token name;   // 最后的属性

        @Override
        public Object execute(ExprVisitor visitor) {
            return visitor.visitGet(this);
        }
    }

    @Getter
    @AllArgsConstructor
    @Describe("setXXX方法")
    @ToString
    public static class Set extends Expr {
        final Expr prefix;
        final Token field;
        final Expr value;

        @Override
        public Object execute(ExprVisitor visitor) {
            return visitor.visitSet(this);
        }
    }

    @Getter
    @AllArgsConstructor
    @Describe("this")
    @ToString
    public static class This extends Expr {
        final Token key;

        @Override
        public Object execute(ExprVisitor visitor) {
            return visitor.visitThis(this);
        }
    }

    @Getter
    @AllArgsConstructor
    @Describe("super")
    @ToString
    public static class Super extends Expr {
        final Token key;
        final Token field;

        @Override
        public Object execute(ExprVisitor visitor) {
            return visitor.visitSuper(this);
        }
    }
}
