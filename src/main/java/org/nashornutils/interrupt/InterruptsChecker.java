package org.nashornutils.interrupt;

public class InterruptsChecker implements Runnable {
    @Override
    public void run() {
        if (Thread.interrupted()) {
            throw new RuntimeException(new InterruptedException());
        }
    }
}
