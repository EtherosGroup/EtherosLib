package cn.etheros.etheroslib.velocity.event;

import cn.etheros.etherosframework.di.annotation.GlobalService;
import cn.etheros.etherosframework.di.annotation.PreDestroy;
import cn.etheros.etheroslib.common.event.EtherosEventBus;

/**
 * 事件总线的 Velocity 注册适配
 *
 * <p>实现位于 common，本类仅负责被 Velocity 侧的 DI 扫描发现并注册为全局 Bean
 *
 * @author 西博卡兹
 * @since 1.1.0
 */
@GlobalService
public class VelocityEventBus extends EtherosEventBus {

    @PreDestroy
    private void cleanup() {
        shutdown();
    }
}
