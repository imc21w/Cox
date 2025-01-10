package org.cox.env;

import org.cox.token.Token;
import org.cox.utils.Cox;

import java.util.HashMap;
import java.util.Map;

public class Environment {

    private final Map<String, Object> env = new HashMap<>();

    public void define(Token name, Object value) {
        if (env.containsKey(name.getLexeme())) {
            Cox.error(name.getLine(), "变量" + name.getLexeme() + "不能重复定义");
        }
        env.put(name.getLexeme(), value);
    }

    public Object get(Token name) {
        if (!env.containsKey(name.getLexeme()))
            Cox.error(name.getLine(), "变量" + name.getLexeme() + "未定义");

        return env.get(name.getLexeme());
    }

    public Object set(Token name, Object value) {
        if (!env.containsKey(name.getLexeme()))
            Cox.error(name.getLine(), "变量" + name.getLexeme() + "未定义");

        return env.put(name.getLexeme(), value);
    }
}
