package org.cox.call;

import lombok.Getter;
import org.cox.stmt.Stmt;
import org.cox.token.Token;
import org.cox.utils.Cox;
import org.cox.visitor.IntegrationVisitor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
public class StructInstance {

    private final Struct struct;
    private final StructInstance parent;

    private final Map<String, Object> fieldPool = new HashMap<>();

    public StructInstance(Struct struct, Map<String, Stmt.Fun> funPool, IntegrationVisitor visitor, List<Object> args) {
        this.struct = struct;
        this.parent = struct.getParent() == null ? null : (StructInstance) struct.getParent().call(visitor, args);  // 初始化父类
        funPool.forEach((k, v) -> fieldPool.put(k, CallFun.create(v, struct.getInnerEnvironment(), this, this.parent)));
        initCall(visitor, args, funPool.get("init"));   // 执行构造
    }

    private void initCall(IntegrationVisitor visitor, List<Object> args, Stmt.Fun initFun) {
        if (initFun == null)
            return;

        call(visitor, initFun.getMethod(), args);
        set(initFun.getMethod(), null);
    }

    @Override
    public String toString() {
        return "<Cox structInstance: " + struct.getStructName().getLexeme() + ">";
    }

    public Object getDeep(String lexName){
        if (fieldPool.containsKey(lexName))
            return fieldPool.get(lexName);

        if (parent != null)
            return parent.getDeep(lexName);

        return null;
    }

    public Object get(Token name) {
        Object o = this.getDeep(name.getLexeme());

        if (o == null)
            return struct.safeGetStaticCall(name);

        return o;
    }

    public Object set(Token name, Object val) {
        return fieldPool.put(name.getLexeme(), val);
    }

    public Object call(IntegrationVisitor visitor, Token methodName, List<Object> args) {
        Object call = get(methodName);

        if (call == null)
            Cox.error(methodName.getLine(), "方法" + methodName.getLexeme() + "不存在");

        if (!(call instanceof Callable))
            Cox.error(methodName.getLine(), "变量" + methodName.getLexeme() + "不是方法");

        return ((Callable) call).call(visitor, args);
    }
}
