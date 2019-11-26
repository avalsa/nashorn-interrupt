package org.nashornutils;

import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.matcher.ElementMatchers;
import org.nashornutils.interrupt.InterruptChecksAdderTransformer;
import org.nashornutils.interrupt.InterruptsFunctionAdderTransformer;

import java.lang.instrument.Instrumentation;

import static net.bytebuddy.matcher.ElementMatchers.none;

public class Premain {

    public static void premain(String agentArgs, Instrumentation instrumentation) {
//        System.out.println("Premain for js instrumentation inkoved");
        new AgentBuilder.Default()
                .ignore(none())
                .with(AgentBuilder.Listener.StreamWriting.toSystemOut().withErrorsOnly())
                .type(ElementMatchers.named("jdk.nashorn.internal.parser.Parser"))
                .transform(new InterruptChecksAdderTransformer())
                .type(ElementMatchers.named("jdk.nashorn.api.scripting.NashornScriptEngine"))
                .transform(new InterruptsFunctionAdderTransformer())
                .installOn(instrumentation);
    }
}
