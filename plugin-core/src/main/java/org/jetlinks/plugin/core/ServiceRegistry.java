package org.jetlinks.plugin.core;


import java.util.List;
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
     * <p>外部插件使用 {@code PluginService.class + serviceId} 获取平台命令服务引用。
     * 返回引用不代表命令已获授权，实际调用仍由平台按 context、generation 和 allowlist 校验。</p>
     *
     * @param type 服务类型，不可为空
     * @param name 服务名称；平台命令服务使用 canonical service id
     * @param <T> 服务类型
     * @return 已注册的服务或受限服务代理
     * @throws UnsupportedOperationException 服务不存在时抛出
     */
    default <T> T getServiceNow(Class<T> type, String name) {
        return getService(type, name)
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
