package org.cox.visitor;

import org.cox.env.Environment;
import org.cox.expr.Expr;
import org.cox.stmt.Stmt;
import org.cox.utils.Cox;
import org.cox.utils.Pair;

import java.math.BigDecimal;
import java.math.RoundingMode;
public class EvaluateVisitor implements ExprVisitor, StmtVisitor{

    Environment environment = null;

    public EvaluateVisitor(Environment env) {
        this.environment = env;
    }

    public EvaluateVisitor() {
        this(new Environment());
    }

    @Override
    public Object visitBinary(Expr.Binary expr) {
        Object left = expr.getLeft().execute(this);
        Object right = expr.getRight().execute(this);

        switch (expr.getOperator().getType()){
            case PLUS:
                if (left instanceof BigDecimal && right instanceof BigDecimal)
                    return ((BigDecimal) left).add(((BigDecimal) right));
                else if (left instanceof String || right instanceof String)
                    return castInt(left).toString() + castInt(right).toString();
                Cox.error(expr.getOperator().getLine(), "无效的 PLUS");
                break;

            case MINUS:
                if (left instanceof BigDecimal && right instanceof BigDecimal)
                    return ((BigDecimal) left).subtract(((BigDecimal) right));
                Cox.error(expr.getOperator().getLine(), "无效的 MINUS");
                break;

            case STAR:
                if (left instanceof BigDecimal && right instanceof BigDecimal)
                    return ((BigDecimal) left).multiply(((BigDecimal) right));
                Cox.error(expr.getOperator().getLine(), "无效的 STAR");
                break;

            case SLASH:
                if (left instanceof BigDecimal && right instanceof BigDecimal)
                    return ((BigDecimal) left).divide(((BigDecimal) right), 16, RoundingMode.HALF_UP);
                Cox.error(expr.getOperator().getLine(), "无效的 SLASH");
                break;

            case GREATER:
                if (left instanceof BigDecimal && right instanceof BigDecimal)
                    return ((BigDecimal) left).compareTo(((BigDecimal) right)) > 0;
                else if (left instanceof String && right instanceof String)
                    return ((String) left).compareTo(((String) right)) > 0;
                Cox.error(expr.getOperator().getLine(), "无效的 GREATER");
                break;

            case GREATER_EQUAL:
                if (left instanceof BigDecimal && right instanceof BigDecimal)
                    return ((BigDecimal) left).compareTo(((BigDecimal) right)) >= 0;
                else if (left instanceof String && right instanceof String)
                    return ((String) left).compareTo(((String) right)) >= 0;
                Cox.error(expr.getOperator().getLine(), "无效的 GREATER_EQUAL");
                break;

            case LESS:
                if (left instanceof BigDecimal && right instanceof BigDecimal)
                    return ((BigDecimal) left).compareTo(((BigDecimal) right)) < 0;
                else if (left instanceof String && right instanceof String)
                    return ((String) left).compareTo(((String) right)) < 0;
                Cox.error(expr.getOperator().getLine(), "无效的 LESS");
                break;

            case LESS_EQUAL:
                if (left instanceof BigDecimal && right instanceof BigDecimal)
                    return ((BigDecimal) left).compareTo(((BigDecimal) right)) <= 0;
                else if (left instanceof String && right instanceof String)
                    return ((String) left).compareTo(((String) right)) <= 0;
                Cox.error(expr.getOperator().getLine(), "无效的 LESS_EQUAL");
                break;

            case BANG_EQUAL:
                return !left.equals(right);

            case EQUAL_EQUAL:
                return left.equals(right);
        }

        Cox.error(expr.getOperator().getLine(), "无效的Binary");

        return null;
    }

    @Override
    public Object visitGrouping(Expr.Grouping expr) {
        return expr.getExpression().execute(this);
    }

    @Override
    public Object visitLiteral(Expr.Literal expr) {
        return expr.getValue();
    }

    @Override
    public Object visitUnary(Expr.Unary expr) {
        Object value = expr.getRight().execute(this);
        switch (expr.getOperator().getType()){
            case MINUS:
                if (value instanceof BigDecimal)
                    return ((BigDecimal) value).negate();
                Cox.error(expr.getOperator().getLine(), "-表达式右值只能是数字型");
                break;
            case BANG:
                if (value instanceof Boolean)
                    return !((Boolean) value);
                Cox.error(expr.getOperator().getLine(), "!表达式右值只能是布尔型");
                break;
            default:
                Cox.error(expr.getOperator().getLine(), "无效的Unary");
                break;
        }
        return null;
    }

    @Override
    public void visitExpression(Stmt.Expression expression) {
        expression.getExpr().execute(this);
    }

    @Override
    public void visitPrint(Stmt.Print print) {
        System.out.println(castInt(print.getExpr().execute(this)));
    }

    private Object castInt(Object value){
        if (value instanceof BigDecimal){
            if (((BigDecimal) value).stripTrailingZeros().scale() <= 0) {
                return ((BigDecimal) value).toBigInteger();
            }
        }
        return value;
    }

    @Override
    public void visitLET(Stmt.LET let) {
        Object value = null;
        if (let.getExpr() != null)
            value = let.getExpr().execute(this);
        environment.define(let.getName(), value);
    }

    @Override
    public Object visitVariable(Expr.Variable variable) {
        return environment.get(variable.getName());
    }

    @Override
    public Object visitAssign(Expr.Assign assign) {
        Object execute = assign.getExpr().execute(this);
        environment.set(assign.getName(), execute);
        return execute;
    }

    @Override
    public void visitBlock(Stmt.Block block) {
        Environment inner = new Environment(this.environment);
        EvaluateVisitor visitor = new EvaluateVisitor(inner);
        block.getStmts().forEach(m -> m.execute(visitor));
    }

    @Override
    public void visitIF(Stmt.IF anIf) {
        for (Pair<Expr, Stmt> exprStmtPair : anIf.getIfList()) {
            Object execute = exprStmtPair.getFirst().execute(this);
            
            if (isTrue(execute)) {
                exprStmtPair.getSecond().execute(this);
                return;
            }
        }

        if (anIf.getElseStmt() != null){
            anIf.getElseStmt().execute(this);
        }
    }

    @Override
    public Object visitOr(Expr.Or or) {
        Object val = or.getLeft().execute(this);

        if (isTrue(val)) {
            return val;
        }

        return or.getRight().execute(this);
    }

    @Override
    public Object visitAnd(Expr.And and) {
        Object val = and.getLeft().execute(this);

        if (isTrue(val)) {
            return and.getRight().execute(this);
        }

        return val;
    }

    private boolean isTrue(Object val){
        return val instanceof Boolean && (Boolean) val;
    }
}
