package org.nashornutils.interrupt;

import jdk.nashorn.api.scripting.NashornScriptEngine;
import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.ClassFileLocator;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.dynamic.loading.ClassInjector;
import net.bytebuddy.matcher.ElementMatchers;
import net.bytebuddy.utility.JavaModule;

import java.util.Collections;

import static net.bytebuddy.implementation.bytecode.assign.Assigner.Typing.DYNAMIC;

/**
 * define __interrupt_check function for script scope
 */
public class InterruptsFunctionAdderTransformer implements AgentBuilder.Transformer {
    @Override
    public DynamicType.Builder<?> transform(DynamicType.Builder<?> builder, TypeDescription typeDescription, ClassLoader classLoader, JavaModule javaModule) {
        new ClassInjector.UsingUnsafe(classLoader).inject(Collections.singletonMap(
                new TypeDescription.ForLoadedType(InterruptsChecker.class),
                ClassFileLocator.ForClassLoader.read(InterruptsChecker.class)));
        return builder
                .constructor(ElementMatchers.any())
                .intercept(Advice.to(CompilerAdviser.class));
    }

    private static class CompilerAdviser {

        @Advice.OnMethodExit
        public static void onExit(@Advice.This(typing = DYNAMIC) Object returned) {
//            System.out.println("constructor");
            NashornScriptEngine scriptEngine = (NashornScriptEngine) returned;
            scriptEngine.put("__interrupt_check", new InterruptsChecker());
        }
    }
}