package com.github.avalsa.interrupt;

import jdk.nashorn.internal.ir.*;

import java.util.Collections;

public class RewriteNodeVisitor extends jdk.nashorn.internal.ir.visitor.SimpleNodeVisitor {

    @Override
    public Node leaveWhileNode(WhileNode node) {
        return node.setBody(this.getLexicalContext(),
                addInterrupt(node.getToken(), node.getFinish(), node.getLineNumber(), node.getBody()));

    }

    @Override
    public Node leaveForNode(ForNode node) {
        return node.setBody(this.getLexicalContext(),
                addInterrupt(node.getToken(), node.getFinish(), node.getLineNumber(), node.getBody()));
    }

    @Override
    public Node leaveFunctionNode(FunctionNode node) {
        if (node.getName().equals(InterruptsFunctionAdderTransformer.INTERRUPT_FUNCTION_NAME)) {
            return node;
        }
        return node.setBody(this.getLexicalContext(),
                addInterrupt(node.getToken(), node.getFinish(), node.getLineNumber(), node.getBody()));
    }

    private static Block addInterrupt(long token, int finish, int lineNumber, Block innerBody) {
        return new Block(token, finish,
                new ExpressionStatement(lineNumber, token, finish,
                        new CallNode(lineNumber, token, finish,
                                new IdentNode(token,
                                        finish,
                                        InterruptsFunctionAdderTransformer.INTERRUPT_FUNCTION_NAME
                                ), Collections.emptyList(), false
                        )
                ), new BlockStatement(innerBody));
    }
}
