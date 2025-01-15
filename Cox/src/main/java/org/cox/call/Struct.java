package org.cox.call;

import lombok.Getter;
import org.cox.env.Environment;
import org.cox.stmt.Stmt;
import org.cox.token.Token;
import org.cox.token.TokenType;
import org.cox.utils.Cox;
import org.cox.visitor.EvaluateVisitor;
import org.cox.visitor.IntegrationVisitor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

// 对象应该属于一个Callable，标识构造函数,只能叫 init
@Getter
public class Struct implements Callable{

    private final Token structName;  // 结构体名
    private final Map<String, Stmt.Fun> funPool = new HashMap<>();  // 方法池
    private final Map<String, Stmt.Fun> staticFunPool = new HashMap<>();    // 静态方法池
    private Stmt.Fun initFun;   //构造方法

    private final Environment outEnvironment;   // 结构体所属的外部环境
    private final Environment structOfEnvironment;

    public Struct(Token structName, List<Stmt.Fun> funList, Environment outEnvironment) {
        this.structName = structName;

        this.outEnvironment = outEnvironment;
        this.structOfEnvironment = new Environment(outEnvironment){
            @Override
            public void define(Token name, Object value) {
                super.defineCurrent(name, value);
            }
        };

        EvaluateVisitor evaluateVisitor = new EvaluateVisitor(this.structOfEnvironment);

        for (Stmt.Fun fun : funList) {
            // 构造方法
            if (initFun == null && fun.getMethod().getLexeme().equals("init")){
                initFun = fun;
            }

            if (fun.getType() != null && fun.getType().getType() == TokenType.STATIC)
                staticFunPool.put(fun.getMethod().getLexeme(), fun);
            else
                funPool.put(fun.getMethod().getLexeme(), fun);
        }

        // 方法注册
        staticFunPool.values().forEach(e -> e.execute(evaluateVisitor));

        if (initFun != null && initFun.getType() != null && initFun.getType().getType() == TokenType.STATIC){
            Cox.error(initFun.getMethod().getLine(), "构造函数" + initFun.getMethod().getLexeme() + "不能被static 修饰");
        }
    }


    @Override
    public int getArgsCount() {
        return initFun != null ? initFun.getParams().size() : 0;
    }

    @Override
    public Object call(IntegrationVisitor visitor, List<Object> args) {
        StructInstance structInstance = new StructInstance(this, funPool);
        if (initFun != null)
            structInstance.call(visitor, initFun.getMethod(), args);
        return structInstance;
    }

    public Callable getStaticCall(Stmt.Fun fun) {

        if (structOfEnvironment.isDefined(fun.getMethod())){
            return ((Callable) structOfEnvironment.get(fun.getMethod()));
        }

        Cox.error(fun.getMethod().getLine(), "未定义的方法：" + fun.getMethod().getLexeme());
        return null;
    }

    public Stmt.Fun findStaticFun(String name) {
        return staticFunPool.get(name);
    }

    @Override
    public String toString() {
        return "<Cox struct: " + structName.getLexeme() + ">";
    }
}
