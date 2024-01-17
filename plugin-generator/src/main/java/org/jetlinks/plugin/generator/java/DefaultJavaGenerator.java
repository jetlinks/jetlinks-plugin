package org.jetlinks.plugin.generator.java;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import org.springframework.core.ResolvableType;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.Consumer;

class DefaultJavaGenerator implements JavaGenerator {

    static JavaParser parser = new JavaParser();
    final CompilationUnit cu;

    final ClassOrInterfaceDeclaration clazz;

    final String classPackage;
    final String classSimpleName;

    DefaultJavaGenerator(String className) {
        cu = new CompilationUnit();
        if (className.contains(".")) {
            int lastIndex = className.lastIndexOf(".");
            cu.setPackageDeclaration(classPackage = className.substring(0, lastIndex));
            className = classSimpleName = className.substring(lastIndex + 1);
        } else {
            classPackage = "";
            classSimpleName = className;
        }
        clazz = cu.addClass(className);
    }

    @Override
    public JavaGenerator extendsClass(ResolvableType clazz) {
        addImport(clazz.toClass());
        return extendsClass(clazz.toString());
    }

    @Override
    public JavaGenerator extendsClass(String clazz) {

        this.clazz.addExtendedType(
            parser
                .parseClassOrInterfaceType(clazz)
                .getResult()
                .orElseThrow(() -> new IllegalArgumentException("can not parse class:" + clazz)));
        return this;
    }

    @Override
    public String getThis() {
        return classSimpleName;
    }

    @Override
    public JavaGenerator addMethod(String name, Consumer<MethodDeclaration> customizer) {
        MethodDeclaration methodDeclaration = clazz.addMethod(name);
        methodDeclaration.setPublic(true);
        customizer.accept(methodDeclaration);
        return this;
    }

    @Override
    public JavaGenerator comments(String... comments) {
        clazz.setJavadocComment(String.join("\n", comments));
        return this;
    }

    @Override
    public JavaGenerator addField(String type, String name, Modifier.Keyword... modifiers) {
        clazz.addField(type, name, modifiers);
        return this;
    }

    @Override
    public JavaGenerator addField(ResolvableType type, String name, Modifier.Keyword... modifiers) {
        return addField(type.toString(), name, modifiers);
    }

    @Override
    public JavaGenerator addImport(Class<?> clazz) {
        cu.addImport(clazz);
        return this;
    }

    @Override
    public JavaGenerator addImport(String clazz) {
        cu.addImport(clazz);
        return this;
    }

    @Override
    public String generate() {
        return cu.toString();
    }

    @Override
    public String toString() {
        return generate();
    }
}
