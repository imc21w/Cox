package org.cox.start;

import org.cox.call.Callable;
import org.cox.env.Environment;
import org.cox.token.Token;
import org.cox.visitor.IntegrationVisitor;

import java.math.BigDecimal;
import java.util.List;


public class StartUp {

    public static Environment prepareFoundationEnv(){
        Environment env = new Environment();
        Token token = new Token(null, "time", null, -1);
        env.define(token, new Callable() {
            @Override
            public int getArgsCount() {
                return 0;
            }

            @Override
            public Object call(IntegrationVisitor visitor, List<Object> args) {
                return new BigDecimal(System.currentTimeMillis());
            }

            @Override
            public String toString() {
                return "<native method: time>";
            }
        });

        return env;
    }
}
