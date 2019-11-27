package com.github.avalsa.interrupt;

import jdk.nashorn.internal.ir.*;

import java.util.Collections;

public class RewriteNodeVisitor extends jdk.nashorn.internal.ir.visitor.SimpleNodeVisitor {

    @Override
    public Node leaveWhileNode(WhileNode whileNode) {
        Block whileWithCheck = new Block(
                whileNode.getToken(),
                whileNode.getFinish(),
                new ExpressionStatement(whileNode.getLineNumber(),
                        whileNode.getToken(),
                        whileNode.getFinish(),
                        new CallNode(
                                whileNode.getLineNumber(),
                                whileNode.getToken(),
                                whileNode.getFinish(),
                                new IdentNode(
                                        whileNode.getToken(),
                                        whileNode.getFinish(),
                                        "__interrupt_check"
                                ), Collections.emptyList(),
                                false
                        )
                ), new BlockStatement(whileNode.getBody()));

        return whileNode.setBody(this.getLexicalContext(), whileWithCheck);
    }

}
