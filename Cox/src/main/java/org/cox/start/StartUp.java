package org.cox.start;

import org.cox.call.Callable;
import org.cox.call.NativeCallable;
import org.cox.env.Environment;
import org.cox.token.Token;
import org.cox.visitor.EvaluateVisitor;
import org.cox.visitor.IntegrationVisitor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;


public class StartUp {

    public static Environment prepareFoundationEnv(){
        Environment env = new Environment();

        env.defineCurrent("time", -1, new NativeCallable(0, "time") {
            @Override
            public Object call(IntegrationVisitor visitor, List<Object> args) {
                return new BigDecimal(System.currentTimeMillis());
            }
        });

        env.defineCurrent("length", -1, new NativeCallable(1, "length") {
            @Override
            public Object call(IntegrationVisitor visitor, List<Object> args) {
                Object o = args.get(0);
                if (o == null)
                    return BigDecimal.ZERO;
                if (!(o instanceof String))
                    return BigDecimal.ZERO;
                return new BigDecimal(((String) o).length());
            }
        });

        env.defineCurrent("isBoolType", -1, new NativeCallable(1, "isBoolType") {
            @Override
            public Object call(IntegrationVisitor visitor, List<Object> args) {
                Object o = args.get(0);
                if (o == null)
                    return false;
                return o instanceof Boolean;
            }
        });

        env.defineCurrent("isStringType", -1, new NativeCallable(1, "isStringType") {
            @Override
            public Object call(IntegrationVisitor visitor, List<Object> args) {
                Object o = args.get(0);
                if (o == null)
                    return false;
                return o instanceof String;
            }
        });

        env.defineCurrent("isNumberType", -1, new NativeCallable(1, "isNumberType") {
            @Override
            public Object call(IntegrationVisitor visitor, List<Object> args) {
                Object o = args.get(0);
                if (o == null)
                    return false;
                return o instanceof BigDecimal;
            }
        });

        env.defineCurrent("isNull", -1, new NativeCallable(1, "isNull") {
            @Override
            public Object call(IntegrationVisitor visitor, List<Object> args) {
                return args.get(0) == null;
            }
        });

        env.defineCurrent("println", -1, new NativeCallable(-1, "println") {
            @Override
            public Object call(IntegrationVisitor visitor, List<Object> args) {
                System.out.println(args.stream().map(e -> Objects.isNull(e) ? "null" : e).map(EvaluateVisitor::castInt).map(Object::toString).collect(Collectors.joining()));
                return null;
            }
        });

        return env;
    }
}
