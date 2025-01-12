package org.cox.visitor;

import org.cox.stmt.Stmt;

public interface StmtVisitor {
    void visitExpression(Stmt.Expression expression);

    void visitPrint(Stmt.Print print);

    void visitLET(Stmt.LET let);

    void visitBlock(Stmt.Block block);

    void visitIF(Stmt.IF anIf);
}
