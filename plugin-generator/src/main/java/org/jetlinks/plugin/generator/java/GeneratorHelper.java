package org.jetlinks.plugin.generator.java;

import org.jetlinks.core.metadata.DataType;
import org.jetlinks.core.metadata.PropertyMetadata;
import org.jetlinks.core.metadata.types.*;
import org.springframework.core.ResolvableType;

import java.util.List;
import java.util.Map;

public class GeneratorHelper {

    public static JavaGenerator addCommandGetterSetter(JavaGenerator generator,
                                                       List<PropertyMetadata> properties) {
        for (PropertyMetadata input : properties) {
            String id = input.getId();
            String name = input.getName() == null ? id : input.getName();
            String desc = input.getDescription() == null ? "" : input.getDescription();

            DataType type = input.getValueType();
            ResolvableType javaType = toType(type);
            generator.addImport(javaType.toClass());

            char[] c = id.toCharArray();
            c[0] = Character.toUpperCase(c[0]);

            String setter = "set" + new String(c);
            String getter = "get" + new String(c);
            generator.addMethod(
                setter,
                method -> {
                    method.setJavadocComment(String.join(
                        "\n",
                        "设置" + name + "的值",
                        desc,
                        "@param " + id + " 值",
                        "@return this"
                    ));
                    method.setType(generator.getThis());
                    method
                        .addParameter(javaType.toString(), id)
                        .createBody()
                        .addStatement("this.writable().put(\"" + id + "\"," + id + ");")
                        .addStatement("return castSelf();");
                });

            generator.addMethod(
                getter,
                method -> {
                    method.setJavadocComment(String.join(
                        "\n",
                        "获取" + name + "的值",
                        desc,
                        "@return value or null"
                    ));
                    method.setType(javaType.toString());
                    method
                        .createBody()
                        .addStatement("return this.getOrNull(\"" + id + "\"," + javaType
                            .toClass()
                            .getSimpleName() + ".class);");
                });
        }
        return generator;
    }

    private static ResolvableType toType(DataType type) {
        if (type == null) {
            return ResolvableType.forClass(Object.class);
        }
        switch (type.getId()) {
            case StringType.ID:
            case EnumType.ID:
                return ResolvableType.forClass(String.class);
            case BooleanType.ID:
                return ResolvableType.forClass(Boolean.class);
            case IntType.ID:
                return ResolvableType.forClass(Integer.class);
            case LongType.ID:
                return ResolvableType.forClass(Long.class);
            case DoubleType.ID:
                return ResolvableType.forClass(Double.class);
            case FloatType.ID:
                return ResolvableType.forClass(Float.class);
            case GeoType.ID:
                return ResolvableType.forClass(GeoPoint.class);
            case GeoShapeType.ID:
                return ResolvableType.forClass(GeoShape.class);
            case ObjectType.ID:
                return ResolvableType.forClassWithGenerics(Map.class, String.class, Object.class);
            case ArrayType.ID:
                return ResolvableType.forClassWithGenerics(List.class, toType(((ArrayType) type).getElementType()));
        }

        return ResolvableType.forClass(Object.class);
    }

}
