package org.cox.visitor;

import org.cox.call.Callable;
import org.cox.env.Environment;
import org.cox.stmt.Stmt;

public interface IntegrationVisitor extends StmtVisitor, ExprVisitor{

    Callable bornCallable(Stmt.Fun fun,  Environment close);
}
