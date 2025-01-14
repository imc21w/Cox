package org.cox.visitor;

import org.cox.env.Environment;
import org.cox.expr.Expr;
import org.cox.start.StartUp;
import org.cox.stmt.Stmt;
import org.cox.token.Token;
import org.cox.utils.Cox;
import org.cox.utils.Pair;

import java.util.*;

public class VerifyVisitor implements IntegrationVisitor{

    // 每次{}，压栈
    List<Map<String, Boolean>> stack = new ArrayList<>();

    Environment globals = StartUp.prepareFoundationEnv();

    @Override
    public Object visitBinary(Expr.Binary binary) {
        binary.getLeft().execute(this);
        switch (binary.getOperator().getType()) {
            case PLUS:
            case MINUS:
            case STAR:
            case SLASH:
            case GREATER:
            case GREATER_EQUAL:
            case LESS:
            case LESS_EQUAL:
            case BANG_EQUAL:
            case EQUAL_EQUAL:
            case MODE:
                break;
            default:
                Cox.error(binary.getOperator().getLine(), "无效的二元表达式，只能使用+ - * / % > >= < <= != ==");
        }
        binary.getRight().execute(this);
        return null;
    }

    @Override
    public Object visitGrouping(Expr.Grouping grouping) {
        grouping.getExpression().execute(this);
        return null;
    }

    @Override
    public Object visitLiteral(Expr.Literal literal) {
        return null;
    }

    @Override
    public Object visitUnary(Expr.Unary unary) {
        switch (unary.getOperator().getType()) {
            case MINUS:
            case BANG:
                break;
            default:
                Cox.error(unary.getOperator().getLine(), "无效的左表达式，只能使用-和!");
        }

        unary.getRight().execute(this);

        return null;
    }

    @Override
    public Object visitVariable(Expr.Variable variable) {

        Token name = variable.getName();
        if (!isDefine(name.getLexeme()))
            Cox.error(name.getLine(), "变量" + name.getLexeme() + "未定义");

        define(variable.getName().getLexeme());
        return null;
    }

    @Override
    public Object visitAssign(Expr.Assign assign) {
        Token name = assign.getName();

        if (!isDefine(name.getLexeme())) {
            Cox.error(name.getLine(), "变量" + name.getLexeme() + "未定义");
        }

        assign.getExpr().execute(this);

        return null;
    }

    @Override
    public Object visitOr(Expr.Or or) {
        or.getLeft().execute(this);
        or.getRight().execute(this);
        return null;
    }

    @Override
    public Object visitAnd(Expr.And and) {
        and.getLeft().execute(this);
        and.getRight().execute(this);
        return null;
    }

    @Override
    public Object visitCall(Expr.Call call) {
        call.getMethod().execute(this);
        call.getArguments().forEach(e -> e.execute(this));
        return null;
    }

    @Override
    public void visitExpression(Stmt.Expression expression) {
        expression.getExpr().execute(this);
    }

    @Override
    public void visitPrint(Stmt.Print print) {
        print.getExpr().execute(this);
    }

    @Override
    public void visitLET(Stmt.LET let) {
        Token name = let.getName();
        if (isDefine(name.getLexeme())) {
            Cox.error(name.getLine(), "变量" + name.getLexeme() + "已定义");
        }
        define(name.getLexeme());
        let.getExpr().execute(this);
    }

    @Override
    public void visitBlock(Stmt.Block block) {
        push();
        block.getStmts().forEach(e -> e.execute(this));
        pop();
    }

    @Override
    public void visitIF(Stmt.IF anIf) {
        push();
        for (Pair<Expr, Stmt> pair : anIf.getIfList()) {
            pair.getFirst().execute(this);
            pair.getSecond().execute(this);
        }
        Optional.ofNullable(anIf.getElseStmt()).ifPresent(e -> e.execute(this));
        pop();
    }

    @Override
    public void visitWhile(Stmt.While aWhile) {
        define("while");
        push();
        Optional.ofNullable(aWhile.getStartStmt()).ifPresent(e -> e.execute(this));
        aWhile.getBodyStmt().execute(this);
        Optional.ofNullable(aWhile.getUpExpr()).ifPresent(e -> e.execute(this));
        pop();
        clear("while");
    }

    @Override
    public void visitContinue(Stmt.Continue aContinue) {
        if (!isDefine("while"))
            Cox.error(aContinue.getCon().getLine(), "continue语句只能定义在循环内");
    }

    @Override
    public void visitBreak(Stmt.Break aBreak) {
        if (!isDefine("while"))
            Cox.error(aBreak.getBr().getLine(), "break语句只能定义在循环内");
    }

    @Override
    public void visitFun(Stmt.Fun fun) {
        define("fun");
        if (isDefine(fun.getMethod().getLexeme())){
            Cox.error(fun.getMethod().getLine(), "变量" + fun.getMethod().getLexeme() + "已定义");
        }

        define(fun.getMethod().getLexeme());

        push();
        for (Token param : fun.getParams()) {
            define(param.getLexeme());
            new Expr.Variable(param).execute(this);
        }

        fun.getBodyStmt().execute(this);
        pop();
        clear("fun");
    }

    @Override
    public void visitReturn(Stmt.Return aReturn) {
        if (!isDefine("fun"))
            Cox.error(aReturn.getReturnToken().getLine(), "return语句只能定义在循环内");
    }

    private void push(){
        stack.add(new HashMap<>());
    }

    private void pop(){
        stack.remove(stack.size() - 1);
    }

    private void define(String id){
        stack.get(stack.size() - 1).put(id, true);
    }

    private int findDefine(String id){
        for (int i = stack.size() - 1; i >= 0; i--) {
            if (stack.get(i).containsKey(id))
                return stack.size() - 1 - i;
        }

        if (globals.isDefined(new Token(null, id, null, -1)))
            return -2;

        return -1;
    }

    private boolean isDefine(String id){
        return findDefine(id) != -1;
    }

    private void clear(String id){
        stack.get(stack.size() - 1).remove(id);
    }
}
