package org.cox.visitor;

import org.cox.env.Environment;
import org.cox.expr.Expr;
import org.cox.stmt.Stmt;
import org.cox.token.Token;
import org.cox.token.TokenType;
import org.cox.utils.Cox;
import org.cox.utils.Pair;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;

public class EvaluateVisitor implements ExprVisitor, StmtVisitor{

    Environment environment = null;

    private static final Token CONTINUE_TOKEN = new Token(null, "continue", null, -1);
    private static final Token BREAK_TOKEN = new Token(null, "break", null, -1);
    private static final Token WHILE_TOKEN = new Token(null, "while", null, -1);

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

            case MODE:
                if (left instanceof BigDecimal && right instanceof BigDecimal)
                    return ((BigDecimal) left).remainder(((BigDecimal) right));
                Cox.error(expr.getOperator().getLine(), "无效的 MODE");
                break;
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
        Environment whileEnv = this.environment.findEnvForContainKey(WHILE_TOKEN);
        EvaluateVisitor visitor = new EvaluateVisitor(inner);
        for (Stmt stmt : block.getStmts()) {

            // 如果已经有标记了，跳过
            if (whileEnv != null && !whileEnv.get(WHILE_TOKEN).equals("")) {
                return;
            }

            stmt.execute(visitor);

            if (inner.isDefined(CONTINUE_TOKEN)){
                if (whileEnv == null){
                    Cox.error(((Stmt.Continue) stmt).getCon().getLine(), "continue 必须定义在循环语句中");
                }
                whileEnv.set(WHILE_TOKEN, CONTINUE_TOKEN.getLexeme());
                return;
            }

            if (inner.isDefined(BREAK_TOKEN)){
                if (whileEnv == null){
                    Cox.error(((Stmt.Break) stmt).getBr().getLine(), "break 必须定义在循环语句中");
                }
                whileEnv.set(WHILE_TOKEN, BREAK_TOKEN.getLexeme());
                return;
            }
        }
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

    @Override
    public void visitWhile(Stmt.While aWhile) {
        if (aWhile.getStartStmt() != null)
            aWhile.getStartStmt().execute(this);

        // 定义while
        this.environment.defineCurrent(WHILE_TOKEN, "");

        while (isTrue(aWhile.getConditionExpr().execute(this))){
            aWhile.getBodyStmt().execute(this);

            Object o = this.environment.get(WHILE_TOKEN);

            if (o.equals(CONTINUE_TOKEN.getLexeme())){
                this.environment.set(WHILE_TOKEN, "");
                if (aWhile.getUpExpr() != null)
                    aWhile.getUpExpr().execute(this);
                continue;
            }

            if (o.equals(BREAK_TOKEN.getLexeme())){
                this.environment.set(WHILE_TOKEN, "");
                break;
            }

            if (aWhile.getUpExpr() != null)
                aWhile.getUpExpr().execute(this);
        }

        this.environment.remove(WHILE_TOKEN);
    }

    @Override
    public void visitContinue(Stmt.Continue aContinue) {
        this.environment.define(CONTINUE_TOKEN, true);
    }

    @Override
    public void visitBreak(Stmt.Break aBreak) {
        this.environment.define(BREAK_TOKEN, true);
    }
}
