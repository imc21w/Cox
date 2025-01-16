package org.cox.visitor;

import org.cox.call.Callable;
import org.cox.call.Struct;
import org.cox.call.StructInstance;
import org.cox.env.Environment;
import org.cox.error.TouchTopException;
import org.cox.expr.Expr;
import org.cox.start.StartUp;
import org.cox.stmt.Stmt;
import org.cox.token.Token;
import org.cox.token.TokenType;
import org.cox.utils.Cox;
import org.cox.utils.Pair;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class EvaluateVisitor implements IntegrationVisitor{

    final Environment globals = StartUp.prepareFoundationEnv(); // 顶级

    Environment environment = globals;

    public EvaluateVisitor(Environment env) {
        this.environment = env;
    }

    public EvaluateVisitor() {}

    @Override
    public Object visitBinary(Expr.Binary expr) {
        Object left = expr.getLeft().execute(this);
        Object right = expr.getRight().execute(this);

        switch (expr.getOperator().getType()){
            case PLUS:
                if (left == null && right == null)
                    Cox.error(expr.getOperator().getLine(), "空指针异常");
                else if (left instanceof BigDecimal && right instanceof BigDecimal)
                    return ((BigDecimal) left).add(((BigDecimal) right));
                else if (left instanceof String || right instanceof String) {
                    if (left == null)
                        return castInt(right).toString();
                    else if (right == null)
                        return castInt(left).toString();
                    return castInt(left).toString() + castInt(right).toString();
                }
                else if (left instanceof BigDecimal || right instanceof BigDecimal)
                    return castInt(Objects.requireNonNullElse(left, right));
                Cox.error(expr.getOperator().getLine(), "无效的 PLUS");
                break;

            case MINUS:
                if (left instanceof BigDecimal && right instanceof BigDecimal)
                    return ((BigDecimal) left).subtract(((BigDecimal) right));
                else if (left == null || right == null)
                    Cox.error(expr.getOperator().getLine(), "空指针异常");
                Cox.error(expr.getOperator().getLine(), "无效的 MINUS");
                break;

            case STAR:
                if (left instanceof BigDecimal && right instanceof BigDecimal)
                    return ((BigDecimal) left).multiply(((BigDecimal) right));
                else if (left == null || right == null)
                    Cox.error(expr.getOperator().getLine(), "空指针异常");
                Cox.error(expr.getOperator().getLine(), "无效的 STAR");
                break;

            case SLASH:
                if (left instanceof BigDecimal && right instanceof BigDecimal)
                    return ((BigDecimal) left).divide(((BigDecimal) right), 16, RoundingMode.HALF_UP);
                else if (left == null || right == null)
                    Cox.error(expr.getOperator().getLine(), "空指针异常");
                Cox.error(expr.getOperator().getLine(), "无效的 SLASH");
                break;

            case GREATER:
                if (left instanceof BigDecimal && right instanceof BigDecimal)
                    return ((BigDecimal) left).compareTo(((BigDecimal) right)) > 0;
                else if (left instanceof String && right instanceof String)
                    return ((String) left).compareTo(((String) right)) > 0;
                else if (left == null || right == null)
                    Cox.error(expr.getOperator().getLine(), "空指针异常");
                Cox.error(expr.getOperator().getLine(), "无效的 GREATER");
                break;

            case GREATER_EQUAL:
                if (left instanceof BigDecimal && right instanceof BigDecimal)
                    return ((BigDecimal) left).compareTo(((BigDecimal) right)) >= 0;
                else if (left instanceof String && right instanceof String)
                    return ((String) left).compareTo(((String) right)) >= 0;
                else if (left == null || right == null)
                    Cox.error(expr.getOperator().getLine(), "空指针异常");
                Cox.error(expr.getOperator().getLine(), "无效的 GREATER_EQUAL");
                break;

            case LESS:
                if (left instanceof BigDecimal && right instanceof BigDecimal)
                    return ((BigDecimal) left).compareTo(((BigDecimal) right)) < 0;
                else if (left instanceof String && right instanceof String)
                    return ((String) left).compareTo(((String) right)) < 0;
                else if (left == null || right == null)
                    Cox.error(expr.getOperator().getLine(), "空指针异常");
                Cox.error(expr.getOperator().getLine(), "无效的 LESS");
                break;

            case LESS_EQUAL:
                if (left instanceof BigDecimal && right instanceof BigDecimal)
                    return ((BigDecimal) left).compareTo(((BigDecimal) right)) <= 0;
                else if (left instanceof String && right instanceof String)
                    return ((String) left).compareTo(((String) right)) <= 0;
                else if (left == null || right == null)
                    Cox.error(expr.getOperator().getLine(), "空指针异常");
                Cox.error(expr.getOperator().getLine(), "无效的 LESS_EQUAL");
                break;

            case BANG_EQUAL:
                if (left == null || right == null)
                    return false;
                return !left.equals(right);

            case EQUAL_EQUAL:
                if (left == null || right == null)
                    return false;
                return left.equals(right);

            case MODE:
                if (left instanceof BigDecimal && right instanceof BigDecimal)
                    return ((BigDecimal) left).remainder(((BigDecimal) right));
                else if (left == null || right == null)
                    Cox.error(expr.getOperator().getLine(), "空指针异常");
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
        System.out.print(castInt(print.getExpr().execute(this)));
    }

    public static Object castInt(Object value){
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
        for (Stmt stmt : block.getStmts()) {
            stmt.execute(this);
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
        EvaluateVisitor evaluateVisitor = new EvaluateVisitor(new Environment(this.environment));
        if (aWhile.getStartStmt() != null) {
            aWhile.getStartStmt().execute(evaluateVisitor);
        }

        while (isTrue(aWhile.getConditionExpr().execute(evaluateVisitor))){
            try{
                aWhile.getBodyStmt().execute(evaluateVisitor);
            }catch (TouchTopException e){
                if (e.getToken().getType() == TokenType.BREAK)
                    return;
            }

            if (aWhile.getUpExpr() != null)
                aWhile.getUpExpr().execute(evaluateVisitor);
        }
    }

    @Override
    public void visitContinue(Stmt.Continue aContinue) {
        Cox.touchTop(aContinue.getCon());
    }

    @Override
    public void visitBreak(Stmt.Break aBreak) {
        Cox.touchTop(aBreak.getBr());
    }

    @Override
    public Object visitCall(Expr.Call call) {
        Expr method = call.getMethod();
        List<Object> args = call.getArguments().stream().map(e -> e.execute(this)).collect(Collectors.toList());

        Object execute = method.execute(this);

        if (!(execute instanceof Callable))
            Cox.error(call.getParen().getLine(), "无效的方法名");

        Callable callable = (Callable) execute;

        if (callable.getArgsCount() != -1 && callable.getArgsCount() != args.size())
            Cox.error(call.getParen().getLine(), "方法入参数量不对");

        return callable.call(this, args);
    }

    @Override
    public void visitFun(Stmt.Fun fun) {
        Callable call = bornCallable(fun, this.environment);
        environment.define(fun.getMethod(), call);
    }

    @Override
    public Callable bornCallable(Stmt.Fun fun, Environment close) {
        return new Callable() {
            @Override
            public int getArgsCount() {
                return fun.getParams().size();
            }

            @Override
            public Object call(IntegrationVisitor visitor, List<Object> args) {
                try {
                    EvaluateVisitor innerVisitor = new EvaluateVisitor(new Environment(close){
                        @Override
                        public void define(Token name, Object value) {
                            super.defineCurrent(name, value);
                        }
                    });

                    for (int i = 0; i < fun.getParams().size(); i++) {
                        innerVisitor.environment.defineCurrent(fun.getParams().get(i), args.get(i));
                    }

                    fun.getBodyStmt().execute(innerVisitor);
                } catch (TouchTopException e) {
                    if (e.getToken().getType() == TokenType.RETURN) {
                        return e.getToken().getLiteral();
                    }
                    throw e;
                }
                return null;
            }

            @Override
            public String toString() {
                return "<Cox method: " + fun.getMethod().getLexeme() + ">";
            }
        };
    }

    @Override
    public void visitReturn(Stmt.Return aReturn) {
        Token token = new Token(TokenType.RETURN, "return", aReturn.getExpr().execute(this), aReturn.getReturnToken().getLine());
        Cox.touchTop(token);
    }

    @Override
    public void visitStruct(Stmt.Struct struct) {
        Struct value = new Struct(struct.getStructName(), struct.getFunList(), this.environment);
        this.environment.define(struct.getStructName(), value);
    }

    @Override
    public Object visitGet(Expr.Get get) {
        Expr expr = get.getExpr();
        Object execute = expr.execute(this);

        // 静态调用
        if (execute instanceof Struct){
            return ((Struct) execute).getStaticCall(get.getName());
        }

        if (!(execute instanceof StructInstance))
            Cox.error(get.getName().getLine(), "不能对非对象调用 . 表达式");

        StructInstance instance = (StructInstance) execute;

        return instance.get(get.getName());
    }

    @Override
    public Object visitSet(Expr.Set set) {
        Expr prefix = set.getPrefix();

        Object execute = prefix.execute(this);

        if (!(execute instanceof StructInstance))
            Cox.error(set.getField().getLine(), "set赋值只能针对类对象");

        StructInstance instance = (StructInstance) execute;

        Object val = set.getValue().execute(this);
        return instance.set(set.getField(), val);
    }

    @Override
    public Object visitThis(Expr.This aThis) {
        return environment.get(aThis.getKey());
    }
}
