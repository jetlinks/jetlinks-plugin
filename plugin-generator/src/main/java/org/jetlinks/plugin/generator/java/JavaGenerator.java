package org.jetlinks.plugin.generator.java;

import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.body.MethodDeclaration;
import org.springframework.core.ResolvableType;

import java.util.function.Consumer;

public interface JavaGenerator {

    static JavaGenerator create(String className){
       return new DefaultJavaGenerator(className);
    }

    JavaGenerator extendsClass(ResolvableType clazz);

    JavaGenerator extendsClass(String clazz);

    String getThis();

    JavaGenerator addImport(String clazz);

    JavaGenerator addImport(Class<?> clazz);

    JavaGenerator addMethod(String name,
                            Consumer<MethodDeclaration> customizer);

    JavaGenerator comments(String... comments);

    JavaGenerator addField(ResolvableType type, String name, Modifier.Keyword... modifiers);

    JavaGenerator addField(String type,String name,Modifier.Keyword... modifiers);


    String generate();
}
