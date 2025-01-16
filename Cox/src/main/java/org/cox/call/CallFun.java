package org.cox.call;

import org.cox.env.Environment;
import org.cox.stmt.Stmt;
import org.cox.token.TokenType;
import org.cox.utils.Cox;
import org.cox.visitor.IntegrationVisitor;

import java.util.List;

public class CallFun implements Callable{

    private final Stmt.Fun fun;
    private final Environment outEnvironment;
    private final TokenType tokenType;
    private volatile Callable callable = null;

    private CallFun(Stmt.Fun fun, Environment outEnvironment, TokenType tokenType) {
        this.fun = fun;
        this.outEnvironment = outEnvironment;
        this.tokenType = tokenType;
        this.outEnvironment.defineCurrent(fun.getMethod(), this);
    }

    public static CallFun create(Stmt.Fun fun, Environment outEnvironment, StructInstance instance) {
        Environment environment = new Environment(outEnvironment);
        environment.defineCurrent("this", fun.getMethod().getLine(), instance);
        return new CallFun(fun, environment, TokenType.FUN);
    }

    public static CallFun createStatic(Stmt.Fun fun, Environment outEnvironment) {
        if (fun.getType() == null || fun.getType().getType() != TokenType.STATIC)
            Cox.error(fun.getMethod().getLine(), "方法"+fun.getMethod().getLexeme() + "不是静态方法");
        return new CallFun(fun, outEnvironment, TokenType.STATIC);
    }

    @Override
    public int getArgsCount() {
        return fun.getParams().size();
    }

    @Override
    public Object call(IntegrationVisitor visitor, List<Object> args) {
        if (callable == null){
            callable = createCall(visitor, args);
        }
        return callable.call(visitor, args);
    }

    private Callable createCall(IntegrationVisitor visitor, List<Object> args) {
        return visitor.bornCallable(fun, outEnvironment);
    }
}
