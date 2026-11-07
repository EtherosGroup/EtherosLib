package cn.etheros.etheroslib.bukkit.command;

import cn.etheros.etheroslib.common.command.CommandDefinition;
import org.bukkit.command.Command;

import java.util.List;

/**
 * 命令定义在 Bukkit 的载体
 *
 * @author 西博卡兹
 * @since 1.1.0
 */
public final class HandlerCommand extends Command {

    private final CommandDefinition definition;

    HandlerCommand(CommandDefinition definition) {
        super(definition.name(), definition.description(), definition.usage(), definition.aliases());
        this.definition = definition;
        if (!definition.permission().isEmpty()) {
            setPermission(definition.permission());
        }
    }

    @Override
    public boolean execute(org.bukkit.command.CommandSender sender, String commandLabel, String[] args) {
        return definition.execute(new BukkitCommandSender(sender), commandLabel, args);
    }

    @Override
    public List<String> tabComplete(org.bukkit.command.CommandSender sender, String alias, String[] args) {
        return definition.suggest(new BukkitCommandSender(sender), alias, args);
    }

    CommandDefinition definition() {
        return definition;
    }
}
