package cn.etheros.etheroslib.bukkit;

import cn.etheros.etherosframework.di.core.ApplicationContext;
import cn.etheros.etherosframework.di.spi.FieldInjectionProviders;
import cn.etheros.etheroslib.common.EtherosLibCore;
import cn.etheros.etheroslib.common.log.EtherosLog;
import cn.etheros.etheroslib.common.log.LogFieldInjector;
import cn.etheros.etheroslib.common.log.backend.JdkLogBackend;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Bukkit / Paper 平台入口，负责把平台实现绑定到 {@link EtherosLibCore}。
 *
 * @author 西博卡兹
 * @since 1.0.0
 */
public final class EtherosLib extends JavaPlugin {

    private ApplicationContext context;

    @Override
    public void onEnable() {
        // Bukkit 原生日志体系是 JUL；先安装后端与注入器，再启动容器
        EtherosLog.installBackend(new JdkLogBackend());
        FieldInjectionProviders.register(new LogFieldInjector());
        context = ApplicationContext.run(EtherosLib.class);
    }

    @Override
    public void onDisable() {
        if (context != null) {
            context.shutdown();
        }
    }
}
