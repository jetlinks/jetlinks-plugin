package org.jetlinks.plugin.core;


import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * 插件可用服务的统一注册中心。
 *
 * <p>本地运行时可以注册 Java 服务对象；外部运行时可以按类型和名称返回受限代理。
 * 注册中心只负责发现服务，不负责远程协议、权限绕过或生命周期管理。</p>
 *
 * @author zhouhao
 * @since 1.0
 * @see PluginContext#services()
 */
public interface ServiceRegistry {

    /**
     * 按类型立即获取服务。
     *
     * @param type 服务类型，不可为空
     * @param <T> 服务类型
     * @return 已注册的服务
     * @throws UnsupportedOperationException 服务不存在时抛出
     */
    default <T> T getServiceNow(Class<T> type) {
        return getService(type)
                .orElseThrow(() -> new UnsupportedOperationException("unsupported service:" + type.getSimpleName()));
    }

    /**
     * 按类型和名称立即获取服务。
     *
     * @param type 服务类型，不可为空
     * @param name 服务名称，不可为空
     * @param <T> 服务类型
     * @return 已注册的服务或受限服务代理
     * @throws UnsupportedOperationException 服务不存在时抛出
     * @since 1.0.6
     */
    default <T> T getServiceNow(Class<T> type, String name) {
        return getService(type, name)
                .orElseThrow(() -> new UnsupportedOperationException(
                        "unsupported service:" + type.getSimpleName() + ":" + name));
    }

    /**
     * 按类型、名称和解析参数立即获取服务。
     *
     * <p>解析参数用于创建带上下文的服务引用。默认实现仅兼容空参数；实现不支持非空参数时
     * 必须返回空结果，不得静默忽略。</p>
     *
     * @param type 服务类型，不可为空
     * @param name 服务名称，不可为空
     * @param options 服务解析参数，不可为空
     * @param <T> 服务类型
     * @return 已注册的服务或受限服务代理
     * @throws UnsupportedOperationException 服务不存在或不支持解析参数时抛出
     * @since 1.0.6
     */
    default <T> T getServiceNow(Class<T> type,
                                String name,
                                Map<String, Object> options) {
        return getService(type, name, options)
                .orElseThrow(() -> new UnsupportedOperationException(
                        "unsupported service:" + type.getSimpleName() + ":" + name));
    }

    /**
     * 按类型查找服务。
     *
     * @param type 服务类型，不可为空
     * @param <T> 服务类型
     * @return 服务；未注册时返回空
     */
    <T> Optional<T> getService(Class<T> type);

    /**
     * 按类型和名称查找服务。
     *
     * @param type 服务类型，不可为空
     * @param name 服务名称，不可为空
     * @param <T> 服务类型
     * @return 服务；未注册时返回空
     */
    <T> Optional<T> getService(Class<T> type, String name);

    /**
     * 按类型、名称和解析参数查找服务。
     *
     * <p>默认实现只将空参数委托给具名查询。非空参数返回空结果，使旧实现保持二进制兼容，
     * 同时避免调用方误以为解析参数已经生效。</p>
     *
     * @param type 服务类型，不可为空
     * @param name 服务名称，不可为空
     * @param options 服务解析参数，不可为空
     * @param <T> 服务类型
     * @return 服务；未注册或不支持解析参数时返回空
     * @since 1.0.6
     */
    default <T> Optional<T> getService(Class<T> type,
                                       String name,
                                       Map<String, Object> options) {
        Objects.requireNonNull(options, "options");
        return options.isEmpty()
                ? getService(type, name)
                : Optional.empty();
    }

    /**
     * 获取指定类型的全部已注册服务。
     *
     * <p>实现必须返回有限快照；不应通过此方法枚举动态远程服务。</p>
     *
     * @param type 服务类型，不可为空
     * @param <T> 服务类型
     * @return 有限服务列表，没有服务时返回空列表
     */
    <T> List<T> getServices(Class<T> type);


}
