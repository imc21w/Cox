package org.cox.call;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public abstract class NativeCallable implements Callable{

    private final int argsCount;
    private final String methodName;

    @Override
    public int getArgsCount() {
        return argsCount;
    }

    @Override
    public String toString() {
        return "<native method: " + methodName + ", argsCount: " + argsCount +">";
    }
}
