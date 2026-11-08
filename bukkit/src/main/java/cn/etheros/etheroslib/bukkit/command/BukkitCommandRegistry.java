package cn.etheros.etheroslib.bukkit.command;

import cn.etheros.etherosframework.di.annotation.GlobalService;
import cn.etheros.etherosframework.di.annotation.PreDestroy;
import cn.etheros.etheroslib.common.command.CommandDefinition;
import cn.etheros.etheroslib.common.command.CommandErrorHandler;
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
 * <p>可为单个插件设置默认错误文案处理器，统一接入 i18n：
 * {@code commandRegistry.setDefaultErrorHandler(this, i18n::message);}
 *
 * @author 西博卡兹
 * @since 1.1.0
 */
@GlobalService
public class BukkitCommandRegistry {

    private final Map<Plugin, BukkitCommandRegistrar> registrars =
            new ConcurrentHashMap<Plugin, BukkitCommandRegistrar>();
    private final Map<Plugin, CommandErrorHandler> defaultErrorHandlers =
            new ConcurrentHashMap<Plugin, CommandErrorHandler>();

    public CommandDefinition register(Plugin plugin, Object handler) {
        Objects.requireNonNull(plugin, "plugin");
        Objects.requireNonNull(handler, "handler");
        CommandDefinition definition = CommandHandlerParser.parse(handler, defaultErrorHandlers.get(plugin));
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

    /**
     * 设置插件级默认错误文案处理器，注册前调用
     *
     * <p>查找顺序：子命令指定 → 子命令兜底 → 命令类实现 {@link CommandErrorHandler} →
     * 本处理器 → 内置默认；适合接入 i18n
     */
    public void setDefaultErrorHandler(Plugin plugin, CommandErrorHandler handler) {
        Objects.requireNonNull(plugin, "plugin");
        if (handler == null) {
            defaultErrorHandlers.remove(plugin);
        } else {
            defaultErrorHandlers.put(plugin, handler);
        }
    }

    public void unregister(Plugin plugin) {
        defaultErrorHandlers.remove(plugin);
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
        defaultErrorHandlers.clear();
    }

    private BukkitCommandRegistrar registrar(Plugin plugin) {
        return registrars.computeIfAbsent(plugin, BukkitCommandRegistrar::new);
    }
}
