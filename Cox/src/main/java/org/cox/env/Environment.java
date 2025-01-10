package org.cox.env;

import org.cox.token.Token;
import org.cox.utils.Cox;

import java.util.HashMap;
import java.util.Map;

public class Environment {

    private final Environment parent;

    public Environment(final Environment parent) {
        this.parent = parent;
    }

    public Environment(){
        this(null);
    }

    private final Map<String, Object> env = new HashMap<>();

    public void define(Token name, Object value) {
        if (isDefined(name))
            Cox.error(name.getLine(), "变量" + name.getLexeme() + "不能重复定义");

        env.put(name.getLexeme(), value);
    }

    public boolean isDefined(final Token name) {
        if (env.containsKey(name.getLexeme()))
            return true;

        if (parent != null)
            return parent.isDefined(name);

        return false;
    }

    public Object get(Token name) {

        if (env.containsKey(name.getLexeme()))
            return env.get(name.getLexeme());

        if (parent != null)
            return parent.get(name);

        Cox.error(name.getLine(), "变量" + name.getLexeme() + "未定义");

        return null;
    }

    public Object set(Token name, Object value) {
        if (env.containsKey(name.getLexeme()))
            return env.put(name.getLexeme(), value);

        if (parent != null)
            return parent.set(name, value);

        Cox.error(name.getLine(), "变量" + name.getLexeme() + "未定义");

        return null;
    }
}
