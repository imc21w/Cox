package org.cox.call;

import org.cox.visitor.IntegrationVisitor;

import java.util.List;

public interface Callable {

    int getArgsCount();

    Object call(IntegrationVisitor visitor, List<Object> args);
}
