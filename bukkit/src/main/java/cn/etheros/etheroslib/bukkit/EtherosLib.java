package cn.etheros.etheroslib.bukkit;

import cn.etheros.etheroslib.common.EtherosLibCore;
import cn.etheros.etheroslib.common.Platform;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Bukkit / Paper 平台入口，负责把平台实现绑定到 {@link EtherosLibCore}。
 */
public final class EtherosLib extends JavaPlugin {

    @Override
    public void onEnable() {
        EtherosLibCore core = EtherosLibCore.get();
        core.initialize(Platform.BUKKIT, getDescription().getVersion());
        getLogger().info("EtherosLib " + core.getVersion()
                + " 已加载（" + core.getPlatform().getDisplayName() + "）");
    }

    @Override
    public void onDisable() {
        EtherosLibCore.get().clearServices();
        getLogger().info("EtherosLib 已卸载");
    }
}
