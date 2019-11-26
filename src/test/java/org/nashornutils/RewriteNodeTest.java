package org.nashornutils;

import jdk.nashorn.internal.ir.FunctionNode;
import jdk.nashorn.internal.ir.IfNode;
import jdk.nashorn.internal.ir.Node;
import jdk.nashorn.internal.ir.WhileNode;
import jdk.nashorn.internal.ir.visitor.SimpleNodeVisitor;
import jdk.nashorn.internal.parser.Parser;
import jdk.nashorn.internal.runtime.Context;
import jdk.nashorn.internal.runtime.ErrorManager;
import jdk.nashorn.internal.runtime.Source;
import jdk.nashorn.internal.runtime.options.Options;
import org.junit.Test;
import org.nashornutils.interrupt.RewriteNodeVisitor;

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
                        IfNode st = (IfNode) whileNode.getBody().getStatements().get(0);
                        assertTrue(st.getTest().toString().contains("__interrupt_check"));
                        assertTrue(st.getPass().toString().contains("invokeSomeFunc()"));
                        return super.enterWhileNode(whileNode);
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
