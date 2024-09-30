package org.jetlinks.plugin.internal.ai;

import com.google.common.collect.Sets;
import org.jetlinks.core.metadata.DataType;
import org.jetlinks.core.metadata.PropertyMetadata;
import org.jetlinks.core.metadata.SimplePropertyMetadata;
import org.jetlinks.core.metadata.types.ArrayType;
import org.jetlinks.core.metadata.types.IntType;
import org.jetlinks.core.metadata.types.ObjectType;
import org.jetlinks.core.metadata.types.StringType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author gyl
 * @since 2.3
 */
public class AiOutputMetadataBuilderTest {

    static List<PropertyMetadata> list;

    static {
        list = Arrays.asList(
            SimplePropertyMetadata.of("string", "string", StringType.GLOBAL),
            SimplePropertyMetadata.of("int", "int", IntType.GLOBAL),
            SimplePropertyMetadata.of("map", "map", new ObjectType()
                .addProperty("k1", "k1", StringType.GLOBAL)
                .addProperty("k2", "k2", new ObjectType()
                    .addProperty("kk1", "kk1", StringType.GLOBAL)
                    .addProperty("kk2", "kk2", StringType.GLOBAL)
                )
            ),
            SimplePropertyMetadata.of("list", "list", new ArrayType()
                .elementType(new ObjectType()
                                 .addProperty("l1", "l1", StringType.GLOBAL)
                                 .addProperty("l2", "l2", StringType.GLOBAL))
            )
        );
    }


    @Test
    public void testFlat() {
        {
            AiOutputMetadataBuilder build = AiOutputMetadataBuilder
                .builder()
                .flatProperty("list")
                .excludes(Sets.newHashSet("string"))
                .build();

            List<PropertyMetadata> metadata = new ArrayList<>(build.generateMetadata(list));
            print(metadata);
            Assertions.assertNotNull(metadata);
            Assertions.assertEquals(metadata.size(), 4);
        }

        {
            AiOutputMetadataBuilder build = AiOutputMetadataBuilder
                .builder()
                .flatProperty("list")
                .keepProperties(false)
                .build();

            List<PropertyMetadata> metadata = new ArrayList<>(build.generateMetadata(list));
            print(metadata);
            Assertions.assertNotNull(metadata);
            Assertions.assertEquals(metadata.size(), 2);
            Assertions.assertEquals(metadata.get(0).getId(), "l1");
            Assertions.assertEquals(metadata.get(1).getId(), "l2");
        }


    }

    @Test
    public void testInclude() {
        {
            AiOutputMetadataBuilder build = AiOutputMetadataBuilder
                .builder()
                .includes(Sets.newHashSet("string", "map"))
                .build();

            List<PropertyMetadata> metadata = new ArrayList<>(build.generateMetadata(list));
            print(metadata);
            Assertions.assertNotNull(metadata);
            Assertions.assertEquals(metadata.size(), 2);
        }

        {
            AiOutputMetadataBuilder build = AiOutputMetadataBuilder
                .builder()
                .includes(Sets.newHashSet("errorCode"))
                .build();

            List<PropertyMetadata> metadata = new ArrayList<>(build.generateMetadata(list));
            print(metadata);
            Assertions.assertNotNull(metadata);
            Assertions.assertEquals(metadata.size(), 0);
        }
    }

    @Test
    public void testExclude() {
        {
            AiOutputMetadataBuilder build = AiOutputMetadataBuilder
                .builder()
                .excludes(Sets.newHashSet("string", "map"))
                .build();

            List<PropertyMetadata> metadata = new ArrayList<>(build.generateMetadata(list));
            print(metadata);
            Assertions.assertNotNull(metadata);
            Assertions.assertEquals(metadata.size(), 2);
        }

        {
            AiOutputMetadataBuilder build = AiOutputMetadataBuilder
                .builder()
                .excludes(Sets.newHashSet("errorCode"))
                .build();

            List<PropertyMetadata> metadata = new ArrayList<>(build.generateMetadata(list));
            print(metadata);
            Assertions.assertNotNull(metadata);
            Assertions.assertEquals(metadata.size(), 4);
        }

    }

    @Test
    public void testExclude2() {
        {
            AiOutputMetadataBuilder build = AiOutputMetadataBuilder
                .builder()
                .excludes(Sets.newHashSet("string", "map.k1"))
                .build();

            List<PropertyMetadata> metadata = new ArrayList<>(build.generateMetadata(list));

            print(metadata);
            Assertions.assertNotNull(metadata);
            Assertions.assertEquals(metadata.size(), 3);

            Map<String, PropertyMetadata> map = metadata
                .stream()
                .collect(Collectors.toMap(PropertyMetadata::getId, Function.identity()));
            PropertyMetadata mapMetadata = map.get("map");
            DataType dataType = mapMetadata.getValueType();
            Assertions.assertEquals(dataType.getId(), ObjectType.ID);
            List<PropertyMetadata> properties = ((ObjectType) dataType).getProperties();
            print(properties);
            Assertions.assertNotNull(properties);
            Assertions.assertEquals(properties.size(), 1);

        }

        {
            AiOutputMetadataBuilder build = AiOutputMetadataBuilder
                .builder()
                .excludes(Sets.newHashSet("map.k2.kk1"))
                .build();

            List<PropertyMetadata> metadata = new ArrayList<>(build.generateMetadata(list));
            print(metadata);
            Assertions.assertNotNull(metadata);
            Assertions.assertEquals(metadata.size(), 4);

            Map<String, PropertyMetadata> map = metadata
                .stream()
                .collect(Collectors.toMap(PropertyMetadata::getId, Function.identity()));
            PropertyMetadata mapMetadata = map.get("map");
            DataType dataType = mapMetadata.getValueType();
            Assertions.assertEquals(dataType.getId(), ObjectType.ID);
            List<PropertyMetadata> properties = ((ObjectType) dataType).getProperties();
            print(properties);
            Assertions.assertNotNull(properties);
            Assertions.assertEquals(properties.size(), 2);
            Map<String, PropertyMetadata> map0 = properties
                .stream()
                .collect(Collectors.toMap(PropertyMetadata::getId, Function.identity()));

            PropertyMetadata k2Metadata = map0.get("k2");
            DataType dataType0 = k2Metadata.getValueType();
            Assertions.assertEquals(dataType0.getId(), ObjectType.ID);
            List<PropertyMetadata> properties0 = ((ObjectType) dataType0).getProperties();
            print(properties0);
            Assertions.assertNotNull(properties0);
            Assertions.assertEquals(properties0.size(), 1);
        }

    }


    private void print(List<PropertyMetadata> metadata) {
        metadata.forEach(x -> System.out.print(x.getId() + ":" + x.getValueType().getId() + ","));
        System.out.println();
        System.out.println("==========");
    }
}
