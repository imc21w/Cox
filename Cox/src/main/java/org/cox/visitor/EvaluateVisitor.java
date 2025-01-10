package org.cox.visitor;

import org.cox.expr.Expr;
import org.cox.utils.Cox;

public class EvaluateVisitor implements ExprVisitor{

    @Override
    public Object visitBinary(Expr.Binary expr) {
        Object left = expr.getLeft().execute(this);
        Object right = expr.getRight().execute(this);

        switch (expr.getOperator().getType()){
            case PLUS:
                if (left instanceof Double && right instanceof Double)
                    return ((Double) left) + ((Double) right);
                if (left instanceof String || right instanceof String)
                    return left.toString() + right.toString();
                Cox.error(expr.getOperator().getLine(), "无效的 PLUS");
                break;

            case MINUS:
                if (left instanceof Double && right instanceof Double)
                    return ((Double) left) - ((Double) right);
                Cox.error(expr.getOperator().getLine(), "无效的 MINUS");
                break;

            case STAR:
                if (left instanceof Double && right instanceof Double)
                    return ((Double) left) * ((Double) right);
                Cox.error(expr.getOperator().getLine(), "无效的 STAR");
                break;

            case SLASH:
                if (left instanceof Double && right instanceof Double)
                    return ((Double) left) / ((Double) right);
                Cox.error(expr.getOperator().getLine(), "无效的 SLASH");
                break;

            case GREATER:
                if (left instanceof Double && right instanceof Double)
                    return ((Double) left) > ((Double) right);
                Cox.error(expr.getOperator().getLine(), "无效的 GREATER");
                break;

            case GREATER_EQUAL:
                if (left instanceof Double && right instanceof Double)
                    return ((Double) left) >= ((Double) right);
                Cox.error(expr.getOperator().getLine(), "无效的 GREATER_EQUAL");
                break;

            case LESS:
                if (left instanceof Double && right instanceof Double)
                    return ((Double) left) < ((Double) right);
                Cox.error(expr.getOperator().getLine(), "无效的 LESS");
                break;

            case LESS_EQUAL:
                if (left instanceof Double && right instanceof Double)
                    return ((Double) left) <= ((Double) right);
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
                if (value instanceof Double)
                    return -((Double) value);
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

}
