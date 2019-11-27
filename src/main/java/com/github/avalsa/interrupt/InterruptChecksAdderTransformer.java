package com.github.avalsa.interrupt;

import jdk.nashorn.internal.ir.FunctionNode;
import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.ClassFileLocator;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.dynamic.loading.ClassInjector;
import net.bytebuddy.utility.JavaModule;

import java.util.Collections;

import static net.bytebuddy.implementation.bytecode.assign.Assigner.Typing.DYNAMIC;
import static net.bytebuddy.matcher.ElementMatchers.named;
import static net.bytebuddy.matcher.ElementMatchers.takesArguments;

/**
 * update script AST
 */
public class InterruptChecksAdderTransformer implements AgentBuilder.Transformer {
    @Override
    public DynamicType.Builder<?> transform(DynamicType.Builder<?> builder, TypeDescription typeDescription, ClassLoader classLoader, JavaModule javaModule) {
        new ClassInjector.UsingUnsafe(classLoader).inject(Collections.singletonMap(
                new TypeDescription.ForLoadedType(RewriteNodeVisitor.class),
                ClassFileLocator.ForClassLoader.read(RewriteNodeVisitor.class)));
        return builder
                .method(named("parse").and(takesArguments(String.class, Integer.TYPE, Integer.TYPE, Boolean.TYPE)))
                .intercept(Advice.to(CompilerAdviser.class));
    }

    private static class CompilerAdviser {

        @Advice.OnMethodExit
        public static void onExit(@Advice.Return(readOnly = false, typing = DYNAMIC) Object returned) {
            if (returned instanceof FunctionNode) {
//            System.out.println("FunctionNode found");
                returned = ((FunctionNode) returned).accept(new RewriteNodeVisitor());
            }
        }
    }

}
