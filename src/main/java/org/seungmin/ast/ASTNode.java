package org.seungmin.ast;

public interface ASTNode {
    public <T> T accept(ASTVisitor<T> v);
}
