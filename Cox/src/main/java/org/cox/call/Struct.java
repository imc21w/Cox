package org.cox.call;

import lombok.Getter;
import org.cox.env.Environment;
import org.cox.stmt.Stmt;
import org.cox.token.Token;
import org.cox.token.TokenType;
import org.cox.utils.Cox;
import org.cox.visitor.IntegrationVisitor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

// 对象应该属于一个Callable，标识构造函数,只能叫 init
@Getter
public class Struct implements Callable{

    private final Token structName;  // 结构体名
    private final Map<String, Stmt.Fun> funPool = new HashMap<>();  // 方法池
    private final Map<String, CallFun> staticFunPool = new HashMap<>();    // 静态方法池
    private Stmt.Fun initFun;   //构造方法

    private final Environment innerEnvironment; // 结构体内部环境，引用所属的外部环境

    public Struct(Token structName, List<Stmt.Fun> funList, Environment outEnvironment) {
        this.structName = structName;

        this.innerEnvironment = new Environment(outEnvironment);

        for (Stmt.Fun fun : funList) {
            // 构造方法
            if (initFun == null && fun.getMethod().getLexeme().equals("init")){
                initFun = fun;
            }

            if (fun.getType() != null && fun.getType().getType() == TokenType.STATIC)
                staticFunPool.put(fun.getMethod().getLexeme(), CallFun.createStatic(fun, innerEnvironment));
            else
                funPool.put(fun.getMethod().getLexeme(), fun);
        }

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

    public Callable getStaticCall(Token methodName) {

        Callable callable = safeGetStaticCall(methodName);
        if (callable != null)
            return callable;

        Cox.error(methodName.getLine(), "未定义的静态方法：" + methodName.getLexeme());
        return null;
    }

    public Callable safeGetStaticCall(Token methodName) {

        if (staticFunPool.containsKey(methodName.getLexeme())){
            return staticFunPool.get(methodName.getLexeme());
        }

        return null;
    }

    @Override
    public String toString() {
        return "<Cox struct: " + structName.getLexeme() + ">";
    }
}
