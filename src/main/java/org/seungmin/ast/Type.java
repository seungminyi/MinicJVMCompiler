package org.seungmin.ast;

public interface Type extends ASTNode {

    public <T> T accept(ASTVisitor<T> v);

}
