package com.github.avalsa;

import jdk.nashorn.api.scripting.NashornScriptEngineFactory;
import org.junit.Test;

import javax.script.ScriptEngine;

import java.util.concurrent.atomic.AtomicBoolean;

import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertTrue;

public class RewriteVisitorWithAgentTest {

    @Test
    public void rewriteWhileLoopWorks()  {

        String js = " var i = 0; while (true) { i++;}; ";

        Thread toInterrupt = Thread.currentThread();
        ScriptEngine engine  = new NashornScriptEngineFactory().getScriptEngine();
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
