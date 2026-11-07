package cn.etheros.etheroslib.velocity;

import cn.etheros.etherosframework.di.core.ApplicationContext;
import cn.etheros.etheroslib.common.EtherosLibCore;
import cn.etheros.etheroslib.common.Platform;
import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.proxy.ProxyServer;
import org.slf4j.Logger;

/**
 * Velocity 代理端入口，负责把平台实现绑定到 {@link EtherosLibCore}。
 * <p>
 * 版本号由 Gradle 构建时生成的 {@link BuildInfo} 提供，与工程版本保持一致。
 *
 * @author 西博卡兹
 * @since 1.0.0
 */
@Plugin(
        id = "etheroslib",
        name = "EtherosLib",
        version = BuildInfo.VERSION,
        description = "EtherosLib 跨平台核心库 - Velocity 实现",
        authors = {"Etheros"}
)
public final class EtherosLibVelocity {

    private final ProxyServer server;
    private final Logger logger;

    private ApplicationContext context;

    @Inject
    public EtherosLibVelocity(ProxyServer server, Logger logger) {
        this.server = server;
        this.logger = logger;
    }

    @Subscribe
    public void onProxyInitialize(ProxyInitializeEvent event) {
        context = ApplicationContext.run(EtherosLibVelocity.class);
        EtherosLibCore core = EtherosLibCore.get();
        core.initialize(Platform.VELOCITY, BuildInfo.VERSION);
        logger.info("EtherosLib {} 已加载（{}）", core.getVersion(), core.getPlatform().getDisplayName());
    }

    @Subscribe
    public void onProxyShutdown(ProxyShutdownEvent event) {
        if (context != null) {
            context.shutdown();
            context = null;
        }
        EtherosLibCore.get().clearServices();
        logger.info("EtherosLib 已卸载");
    }

    public ProxyServer getServer() {
        return server;
    }
}
