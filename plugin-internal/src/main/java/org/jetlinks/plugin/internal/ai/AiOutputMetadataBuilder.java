package org.jetlinks.plugin.internal.ai;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import org.apache.commons.collections4.CollectionUtils;
import org.hswebframework.web.bean.FastBeanCopier;
import org.jetlinks.core.command.Command;
import org.jetlinks.core.metadata.DataType;
import org.jetlinks.core.metadata.PropertyMetadata;
import org.jetlinks.core.metadata.SimplePropertyMetadata;
import org.jetlinks.core.metadata.types.ArrayType;
import org.jetlinks.core.metadata.types.ObjectType;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 模型输出属性模型构建器
 * <pre>
 * 将模型的输出属性模型转换为指定格式模型，如：
 * 原有结构为
 * {@code
 * {
 *     "a": 1,
 *     "map": {
 *         "b": 2,
 *         "c": 3
 *     },
 *     "list": [
 *         {
 *             "d": 4，
 *             "e": 5
 *         },
 *         {
 *             "d": 4，
 *             "e": 5
 *         }
 *     ]
 * }
 * }
 * 1.配置{@code flatProperty("list").keepProperties(true)}则数据模型为
 * {@code
 * {
 *     "a": 1,
 *     "map": {
 *         "b": 2,
 *         "c": 3
 *     },
 *     "d": 4，
 *     "e": 5
 * }
 * }
 * 2.配置{@code includes("a")}则数据模型为
 * {@code
 * {
 *     "a": 1
 * }
 * }
 * 3.配置{@code excludes("map.b").excludes("list")}则数据模型为
 * {@code
 * {
 *     "a": 1,
 *     "map": {
 *         "c": 3
 *     }
 * }
 * }
 *
 * </pre>
 *
 * @author gyl
 * @since 2.3
 */
@Builder
public class AiOutputMetadataBuilder {

    @Schema(description = "平铺(List<Map>类型的)属性，将集合内结构体平铺至外层")
    private String flatProperty;

    @Builder.Default
    @Schema(description = "平铺时是否保留外层属性")
    private boolean keepProperties = true;

    @Schema(description = "保留属性（与移除属性不共存）")
    private Set<String> includes;

    @Schema(description = "移除属性（与保留属性不共存），支持{key}.{key}格式移除结构体内部属性")
    private Set<String> excludes;

    // TODO: 2024/9/30 支持根据配置提供数据准换器？

    public <T extends Command<?>> Collection<PropertyMetadata> generateMetadata(Class<T> aclass) {
        List<PropertyMetadata> src = AICommandHandler.getCommandOutputMetadata(aclass);
        return generateMetadata(src);
    }

    public <T extends Command<?>> Collection<PropertyMetadata> generateMetadata(Collection<PropertyMetadata> src) {
        Map<String, PropertyMetadata> resMap = new HashMap<>();
        Map<String, PropertyMetadata> srcMap = src
            .stream()
            .collect(Collectors.toMap(PropertyMetadata::getId, Function.identity()));

        List<PropertyMetadata> flat = null;
        if (StringUtils.hasText(flatProperty)) {
            flat = generateFlat(srcMap);
            if (!keepProperties) {
                //不包含其余属性，直接返回
                return flat;
            }
            //移除平铺源
            addExclude(flatProperty);
        }
        if (CollectionUtils.isNotEmpty(includes)) {
            //添加
            for (String include : includes) {
                PropertyMetadata propertyMetadata = srcMap.get(include);
                if (propertyMetadata != null) {
                    resMap.put(include, propertyMetadata);
                }
            }
        } else if (CollectionUtils.isNotEmpty(excludes)) {
            //移除
            resMap = new HashMap<>(srcMap);
            for (String exclude : excludes) {
                if (exclude.contains(".")) {
                    String[] split = exclude.split("\\.");
                    String base = split[0];
                    PropertyMetadata property = resMap.get(base);
                    if (property != null) {
                        PropertyMetadata _property = exclude(split.length - 1, split, property);
                        //覆盖
                        resMap.put(base, _property);
                    }
                } else {
                    resMap.remove(exclude);
                }
            }
        }
        Collection<PropertyMetadata> res = resMap.values();
        if (flat != null) {
            //添加平铺数据
            res = new ArrayList<>(res);
            res.addAll(flat);
        }
        return res;


    }

    private static PropertyMetadata exclude(int index, String[] split, PropertyMetadata property) {
        if (property == null) {
            return null;
        }
        int _index = index - 1;
        String id = split[split.length - index];

        DataType dataType = property.getValueType();
        if (ObjectType.ID.equals(dataType.getId())) {
            ObjectType object = (ObjectType) dataType;
            List<PropertyMetadata> properties = object.getProperties();
            Map<String, PropertyMetadata> propertiesMap = properties
                .stream()
                .collect(Collectors.toMap(PropertyMetadata::getId, Function.identity()));

            if (_index == 0) {
                propertiesMap.remove(id);
                ObjectType copy = FastBeanCopier.copy(object, new ObjectType(),"properties");
                copy.setProperties(new ArrayList<>(propertiesMap.values()));
                return SimplePropertyMetadata.of(copy, property.getId(), property.getName(), property.getDescription(), property.getExpands());
            }
            PropertyMetadata metadata = exclude(_index, split, propertiesMap.get(id));
            if (metadata != null) {
                propertiesMap.put(id, metadata);
                ObjectType copy = FastBeanCopier.copy(object, new ObjectType(),"properties");
                copy.setProperties(new ArrayList<>(propertiesMap.values()));
                return SimplePropertyMetadata.of(copy, property.getId(), property.getName(), property.getDescription(), property.getExpands());
            }
        }
        return property;
    }

    private List<PropertyMetadata> generateFlat(Map<String, PropertyMetadata> srcMap) {
        PropertyMetadata flat = srcMap.get(flatProperty);
        DataType dataType = flat.getValueType();
        if (ArrayType.ID.equals(dataType.getType())) {
            ArrayType array = (ArrayType) dataType;
            DataType elementType = array.getElementType();
            if (ObjectType.ID.equals(elementType.getId())) {
                ObjectType object = (ObjectType) elementType;
                return object.getProperties();

            }
        }
        return Collections.emptyList();
    }

    private void addExclude(String... excludes) {
        if (this.excludes == null) {
            this.excludes = new HashSet<>();
        }
        this.excludes.addAll(Arrays.asList(excludes));
    }


}
