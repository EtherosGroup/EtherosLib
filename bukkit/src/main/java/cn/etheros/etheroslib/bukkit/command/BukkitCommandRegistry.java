package cn.etheros.etheroslib.bukkit.command;

import cn.etheros.etherosframework.di.annotation.GlobalService;
import cn.etheros.etherosframework.di.annotation.PreDestroy;
import cn.etheros.etheroslib.common.command.CommandDefinition;
import cn.etheros.etheroslib.common.command.CommandHandlerParser;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Bukkit 命令注册入口
 *
 * <p>解析 @CommandHandler 实例并交给 BukkitCommandRegistrar；注销按插件维度清理
 *
 * @author 西博卡兹
 * @since 1.1.0
 */
@GlobalService
public class BukkitCommandRegistry {

    private final Map<Plugin, BukkitCommandRegistrar> registrars =
            new ConcurrentHashMap<Plugin, BukkitCommandRegistrar>();

    public CommandDefinition register(Plugin plugin, Object handler) {
        Objects.requireNonNull(plugin, "plugin");
        Objects.requireNonNull(handler, "handler");
        CommandDefinition definition = CommandHandlerParser.parse(handler);
        registrar(plugin).register(definition);
        return definition;
    }

    public List<CommandDefinition> registerAll(Plugin plugin, Object... handlers) {
        List<CommandDefinition> definitions = new ArrayList<CommandDefinition>(handlers.length);
        for (Object handler : handlers) {
            definitions.add(register(plugin, handler));
        }
        return definitions;
    }

    public void unregister(Plugin plugin) {
        BukkitCommandRegistrar registrar = registrars.remove(plugin);
        if (registrar != null) {
            registrar.unregisterAll();
        }
    }

    @PreDestroy
    private void clear() {
        for (Plugin plugin : new ArrayList<Plugin>(registrars.keySet())) {
            unregister(plugin);
        }
    }

    private BukkitCommandRegistrar registrar(Plugin plugin) {
        return registrars.computeIfAbsent(plugin, BukkitCommandRegistrar::new);
    }
}
