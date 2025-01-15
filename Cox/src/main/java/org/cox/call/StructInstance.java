package org.cox.call;

import lombok.Getter;
import org.cox.env.Environment;
import org.cox.stmt.Stmt;
import org.cox.token.Token;
import org.cox.token.TokenType;
import org.cox.utils.Cox;
import org.cox.visitor.EvaluateVisitor;
import org.cox.visitor.IntegrationVisitor;

import java.util.List;
import java.util.Map;

@Getter
public class StructInstance {

    private final Struct struct;

    private final Environment instancePool;       // 实例环境

    public final static Token THIS = new Token(TokenType.THIS, "this", null, -1);

    public StructInstance(Struct struct, Map<String, Stmt.Fun> funPool) {
        this.struct = struct;
        this.instancePool = new Environment(struct.getStructOfEnvironment()){
            @Override
            public void define(Token name, Object value) {
                super.defineCurrent(name, value);
            }
        };
        this.instancePool.defineCurrent(THIS, this);
        EvaluateVisitor evaluateVisitor = new EvaluateVisitor(this.instancePool);
        funPool.values().forEach(e -> e.execute(evaluateVisitor));
    }

    @Override
    public String toString() {
        return "<Cox structInstance: " + struct.getStructName().getLexeme() + ">";
    }

    public Object get(Token name) {

        if (instancePool.isDefined(name))
            return instancePool.get(name);

        Cox.error(name.getLine(), "未知的变量名:" + name.getLexeme());

        return null;
    }

    public Object set(Token name, Object val) {
        instancePool.remove(name);
        instancePool.defineCurrent(name, val);
        return val;
    }

    public Callable findCallable(Token name) {

        Object get = get(name);
        if (get == null )
            Cox.error(name.getLine(), "未声明的方法:" + name.getLexeme());

        if (!(get instanceof Callable)) {
            Cox.error(name.getLine(), "变量" + name.getLexeme() + "不是方法");
        }

        return ((Callable) get);
    }

    public Object call(IntegrationVisitor visitor, Token methodName, List<Object> args) {
        Callable callable = findCallable(methodName);

        if (callable != null)
            return callable.call(visitor, args);

        Cox.error(methodName.getLine(), "未定义的方法名：" + methodName.getLexeme());
        return null;
    }
}
