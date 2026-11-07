package cn.etheros.etheroslib.bukkit.command;

import cn.etheros.etheroslib.common.command.CommandDefinition;
import cn.etheros.etheroslib.common.command.CommandRegistrar;
import org.bukkit.command.Command;
import org.bukkit.command.CommandMap;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

/**
 * Bukkit 命令注册后端
 *
 * <p>注册到 CommandMap，注销时按名字移除并清理别名
 *
 * @author 西博卡兹
 * @since 1.1.0
 */
public final class BukkitCommandRegistrar implements CommandRegistrar {

    private final Plugin plugin;
    private final Map<String, Command> commands = new LinkedHashMap<String, Command>();

    public BukkitCommandRegistrar(Plugin plugin) {
        this.plugin = Objects.requireNonNull(plugin, "plugin");
        BukkitArgumentTypes.register();
    }

    @Override
    public void register(CommandDefinition definition) {
        Objects.requireNonNull(definition, "definition");
        CommandMap commandMap = commandMap();
        if (commandMap.getCommand(definition.name()) != null) {
            throw new IllegalStateException("命令已存在：" + definition.name());
        }
        HandlerCommand command = new HandlerCommand(definition);
        commandMap.register(plugin.getName().toLowerCase(Locale.ROOT), command);
        commands.put(definition.name().toLowerCase(Locale.ROOT), command);
    }

    @Override
    public void unregister(String name) {
        Command command = commands.remove(Objects.requireNonNull(name, "name").toLowerCase(Locale.ROOT));
        if (command != null) {
            command.unregister(commandMap());
        }
    }

    public void unregisterAll() {
        CommandMap commandMap = commandMap();
        for (Command command : new ArrayList<Command>(commands.values())) {
            command.unregister(commandMap);
        }
        commands.clear();
    }

    public List<String> registeredNames() {
        return new ArrayList<String>(commands.keySet());
    }

    private CommandMap commandMap() {
        return plugin.getServer().getCommandMap();
    }
}
