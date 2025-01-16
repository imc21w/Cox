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

    private final Map<String, Object> fieldPool = new HashMap<>();

    public StructInstance(Struct struct, Map<String, Stmt.Fun> funPool) {
        this.struct = struct;
        funPool.forEach((k, v) -> fieldPool.put(k, CallFun.create(v, struct.getInnerEnvironment(), this)));
    }

    @Override
    public String toString() {
        return "<Cox structInstance: " + struct.getStructName().getLexeme() + ">";
    }

    public Object get(Token name) {
        Object o = fieldPool.get(name.getLexeme());

        if (o == null)
            return struct.safeGetStaticCall(name);

        return o;
    }

    public Object set(Token name, Object val) {
        return fieldPool.put(name.getLexeme(), val);
    }

    public Object call(IntegrationVisitor visitor, Token methodName, List<Object> args) {
        Object call = get(methodName);

        if (!(call instanceof Callable))
            Cox.error(methodName.getLine(), "变量" + methodName.getLexeme() + "不是方法");

        return ((Callable) call).call(visitor, args);
    }
}
