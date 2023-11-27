package org.jetlinks.plugin.example;

import org.apache.commons.collections4.MapUtils;
import org.jetlinks.core.metadata.ConfigMetadata;
import org.jetlinks.core.metadata.DefaultConfigMetadata;
import org.jetlinks.core.metadata.types.StringType;
import org.jetlinks.plugin.core.*;
import org.jetlinks.plugin.internal.InternalPluginDriver;
import org.jetlinks.plugin.internal.InternalPluginType;
import reactor.core.publisher.Mono;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;

public class UpperCasePluginDriver implements InternalPluginDriver {

    @Nonnull
    @Override
    public Description getDescription() {

        Map<String, Object> others = new HashMap<>();

        DefaultConfigMetadata metadata = new DefaultConfigMetadata();

        metadata.add("prefix", "前缀", "前缀", StringType.GLOBAL);
        metadata.add("suffix", "后缀", "后缀", StringType.GLOBAL);

        others.put(InternalPluginDriver.PLUGIN_CONFIG_METADATA, metadata);

        return Description.of(
            "upperCase",
            "大写转换",
            "将输入的字符串转换为大写",
            Version.platform_2_2,
            VersionRange.of(Version.platform_2_2, Version.platform_latest),
            others
        );
    }

    @Nonnull
    @Override
    public PluginType getType() {
        return InternalPluginType.ruleEngine;
    }

    @Nonnull
    @Override
    public Mono<? extends Plugin> createPlugin(@Nonnull String pluginId, @Nonnull PluginContext context) {
        return Mono.just(new UpperCasePlugin(pluginId, context));
    }
}
