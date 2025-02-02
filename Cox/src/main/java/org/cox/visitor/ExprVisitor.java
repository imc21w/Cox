package org.cox.visitor;

import org.cox.expr.Expr;

public interface ExprVisitor {

    Object visitBinary(Expr.Binary binary);

    Object visitGrouping(Expr.Grouping grouping);

    Object visitLiteral(Expr.Literal literal);

    Object visitUnary(Expr.Unary unary);

    Object visitVariable(Expr.Variable variable);

    Object visitAssign(Expr.Assign assign);

    Object visitOr(Expr.Or or);

    Object visitAnd(Expr.And and);

    Object visitCall(Expr.Call call);

    Object visitGet(Expr.Get get);

    Object visitSet(Expr.Set set);

    Object visitThis(Expr.This aThis);

    Object visitSuper(Expr.Super aSuper);
}
