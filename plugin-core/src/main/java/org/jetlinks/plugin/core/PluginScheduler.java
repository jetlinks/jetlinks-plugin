package org.jetlinks.plugin.core;

import reactor.core.Disposable;
import reactor.core.publisher.Mono;

import java.time.Duration;

/**
 * 定时调度管理器,用于创建定时任务等操作
 *
 * @author zhouhao
 * @since 1.0
 */
public interface PluginScheduler {

    /**
     * @param name           任务名称,相同的任务请保证名称相同
     * @param job            异步任务
     * @param cronExpression cron表达式
     * @return Disposable
     * @see PluginScheduler#interval(String, Mono, String, boolean)
     */
    default Disposable interval(String name, Mono<Void> job, String cronExpression) {
        return interval(name, job, cronExpression, true);
    }

    default Disposable interval(String name, Mono<Void> job, Duration interval) {
        return interval(name, job, interval, true);
    }

    /**
     * 使用cron表达式创建一个定时任务,可通过返回值{@link Disposable#dispose()}来取消任务
     *
     * <pre>
     *
     * 0/10 * * * * ? 每10秒执行
     * 0 0 10,14,16 * * ? 每天上午10点，下午2点，4点
     * 0 0 12 ? * WED 每个星期三中午12点
     * 0 0 12 * * ? 每天中午12点触发
     * 0 15 10 ? * * 每天上午10:15触发
     * 0 * 14 * * ? 在每天下午2点到下午2:59期间的每1分钟触发
     * 0 0/5 14 * * ? 在每天下午2点到下午2:55期间的每5分钟触发
     * 0 0/5 14,18 * * ? 在每天下午2点到2:55期间和下午6点到6:55期间的每5分钟触发
     * 0 0-5 14 * * ? 在每天下午2点到下午2:05期间的每1分钟触发
     * 0 10,44 14 ? 3 WED 每年三月的星期三的下午2:10和2:44触发
     * 0 15 10 ? * MON-FRI 周一至周五的上午10:15触发
     * 0 15 10 15 * ? 每月15日上午10:15触发
     * 0 15 10 L * ? 每月最后一日的上午10:15触发
     * 0 15 10 ? * 6L 每月的最后一个星期五上午10:15触发
     * 0 15 10 ? * 6 每月的第三个星期五上午10:15触发
     *
     * </pre>
     *
     * @param job            异步任务
     * @param cronExpression cron 表达式
     * @param singleton      是否单例运行,为true时,集群下只有一个节点会执行
     * @return Disposable
     */
    Disposable interval(String name, Mono<Void> job, String cronExpression, boolean singleton);

    /**
     * 固定周期执行任务,可通过返回值{@link Disposable#dispose()}来取消任务
     *
     * @param job       异步任务
     * @param interval  间隔时间
     * @param singleton 是否单例运行,为true时,集群下只有一个节点会执行
     * @return Disposable
     */
    Disposable interval(String name, Mono<Void> job, Duration interval, boolean singleton);

    /**
     * 延迟执行任务,可通过返回值{@link Disposable#dispose()}来取消任务
     *
     * @param job      任务
     * @param interval 延迟间隔
     * @return Disposable
     */
    Disposable delay(Mono<Void> job, Duration interval);

}
