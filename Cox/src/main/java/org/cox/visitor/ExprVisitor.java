package org.cox.visitor;

import org.cox.expr.Expr;

public interface ExprVisitor {

    Object visitBinary(Expr.Binary binary);

    Object visitGrouping(Expr.Grouping grouping);

    Object visitLiteral(Expr.Literal literal);

    Object visitUnary(Expr.Unary unary);
}
