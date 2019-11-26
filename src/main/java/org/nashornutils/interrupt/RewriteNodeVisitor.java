package org.nashornutils.interrupt;

import jdk.nashorn.internal.ir.*;

import java.util.Collections;

public class RewriteNodeVisitor extends jdk.nashorn.internal.ir.visitor.SimpleNodeVisitor {

    @Override
    public Node leaveWhileNode(WhileNode whileNode) {
        IfNode ifContinue = new IfNode(
                whileNode.getLineNumber(),
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
                ),
                whileNode.getBody(),
                null
        );

        Block ifBlock = new Block(
                whileNode.getToken(),
                whileNode.getFinish(),
                ifContinue);
        return whileNode.setBody(this.getLexicalContext(), ifBlock);
    }

}
