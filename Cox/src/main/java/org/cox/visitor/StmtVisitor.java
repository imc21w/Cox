package org.cox.visitor;

import org.cox.stmt.Stmt;

public interface StmtVisitor {
    void visitExpression(Stmt.Expression expression);

    void visitPrint(Stmt.Print print);

    void visitLET(Stmt.LET let);

    void visitBlock(Stmt.Block block);

    void visitIF(Stmt.IF anIf);

    void visitWhile(Stmt.While aWhile);

    void visitContinue(Stmt.Continue aContinue);

    void visitBreak(Stmt.Break aBreak);
}
