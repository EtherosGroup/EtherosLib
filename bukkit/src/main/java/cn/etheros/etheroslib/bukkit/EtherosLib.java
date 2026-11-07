package cn.etheros.etheroslib.bukkit;

import cn.etheros.etherosframework.di.core.ApplicationContext;
import cn.etheros.etheroslib.common.EtherosLibCore;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Bukkit / Paper 平台入口，负责把平台实现绑定到 {@link EtherosLibCore}。
 */
public final class EtherosLib extends JavaPlugin {

    private ApplicationContext context;

    @Override
    public void onEnable() {
        context = ApplicationContext.run(EtherosLib.class);
    }

    @Override
    public void onDisable() {
        if (context != null) {
            context.shutdown();
        }
    }
}
