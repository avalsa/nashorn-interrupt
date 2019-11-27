package com.github.avalsa;

import com.github.avalsa.interrupt.RewriteNodeVisitor;
import jdk.nashorn.internal.ir.*;
import jdk.nashorn.internal.ir.visitor.SimpleNodeVisitor;
import jdk.nashorn.internal.parser.Parser;
import jdk.nashorn.internal.runtime.Context;
import jdk.nashorn.internal.runtime.ErrorManager;
import jdk.nashorn.internal.runtime.Source;
import jdk.nashorn.internal.runtime.options.Options;
import org.junit.Test;

import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.UUID;

import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;


public class RewriteNodeTest {

    @Test
    public void rewriteWhileLoopWorks() {
        String js = " while (true) { invokeSomeFunc() }";
        FunctionNode origin = parse(js);
        Node afterModify = origin.accept(new RewriteNodeVisitor());
        assertNotSame(origin, afterModify);
        afterModify.accept(
                new SimpleNodeVisitor() {
                    @Override
                    public boolean enterWhileNode(WhileNode whileNode) {
                        Block st = whileNode.getBody();
                        assertTrue(st.getStatements().get(0).toString().contains("__interrupt_check"));
                        assertTrue(st.getStatements().get(1).toString().contains("invokeSomeFunc()"));
                        return super.enterWhileNode(whileNode);
                    }
                });
    }

    @Test
    public void rewriteForLoopWorks() {
        String js = " var i; for (i = 0; i >= 0; i++) { invokeSomeFunc() }";
        FunctionNode origin = parse(js);
        Node afterModify = origin.accept(new RewriteNodeVisitor());
        assertNotSame(origin, afterModify);
        afterModify.accept(
                new SimpleNodeVisitor() {
                    @Override
                    public boolean enterForNode(ForNode forNode) {
                        Block st = forNode.getBody();
                        assertTrue(st.getStatements().get(0).toString().contains("__interrupt_check"));
                        assertTrue(st.getStatements().get(1).toString().contains("invokeSomeFunc()"));
                        return super.enterForNode(forNode);
                    }
                });
    }

    @Test
    public void rewriteFunctionsWorks() {
        String js = "myFunc = function(value) { return myFunc(value + 1); }; f(0);";
        FunctionNode origin = parse(js);
        Node afterModify = origin.accept(new RewriteNodeVisitor());
        assertNotSame(origin, afterModify);
        afterModify.accept(
                new SimpleNodeVisitor() {
                    @Override
                    public boolean enterFunctionNode(FunctionNode functionNode) {
                        Block st = functionNode.getBody();
                        assertTrue(st.getStatements().get(0).toString().contains("__interrupt_check"));
                        assertTrue(st.getStatements().get(1).toString().contains("myFunc"));
                        return super.enterFunctionNode(functionNode);
                    }
                });
    }

    private static FunctionNode parse(String js) {
        Options options = new Options("nashorn");
        options.set("anon.functions", true);
        options.set("parse.only", true);
        options.set("scripting", true);

        ErrorManager errors = new ErrorManager(new PrintWriter(new OutputStream() {
            @Override
            public void write(int b) {
            }
        }));
        Context context = new Context(options, errors, Thread.currentThread().getContextClassLoader());

        Source source = Source.sourceFor(UUID.randomUUID().toString(), js);
        Parser parser = new Parser(context.getEnv(), source, errors);
        return parser.parse();
    }

}
