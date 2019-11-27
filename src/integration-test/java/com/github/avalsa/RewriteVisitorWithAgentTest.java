package com.github.avalsa;

import jdk.nashorn.api.scripting.NashornScriptEngineFactory;
import org.junit.Test;

import javax.script.ScriptEngine;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertTrue;

public class RewriteVisitorWithAgentTest {

    @Test
    public void rewriteWhileLoopWorks() {
        waitAndInterrupt(" var i = 0; while (true) { i++;}; ");
    }

    @Test
    public void rewriteForLoopWorks() {
        waitAndInterrupt(" var i = 0; for(i = 0; i < 10; i++ ) { i--;}; ");
    }

    @Test
    public void rewriteFunctionsWorks() {
        waitAndInterrupt("fun = function(value) { waitUninterruptable(100); return fun(value+1);}; fun(0); ");
    }

    private static void waitAndInterrupt(String js) {
        Thread toInterrupt = Thread.currentThread();
        ScriptEngine engine = new NashornScriptEngineFactory().getScriptEngine();
        //in your code u must perform checks of interrupt status yourself
        engine.put("waitUninterruptable", (Consumer<Integer>) integer -> {
            long c = System.currentTimeMillis() + integer;
            //noinspection StatementWithEmptyBody
            while (System.currentTimeMillis() < c) {
            }
        });
        AtomicBoolean awaited = new AtomicBoolean(false);
        new Thread(() -> {
            try {
                Thread.sleep(4000);  //wait 4 sec and interrupt
            } catch (InterruptedException e) {
                throw new AssertionError();
            }
            awaited.set(true);
            toInterrupt.interrupt();
        }).start();

        try {
            engine.eval(js);
        } catch (Exception e) {
            assertTrue(e instanceof RuntimeException);
            assertTrue(e.getCause() instanceof InterruptedException);
            assertTrue(awaited.get());
            assertFalse(Thread.currentThread().isInterrupted());
        }
    }

}
