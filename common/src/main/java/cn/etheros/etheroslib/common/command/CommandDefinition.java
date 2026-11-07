package cn.etheros.etheroslib.common.command;

import java.util.Collections;
import java.util.List;

/**
 * 命令定义：元数据 + 解析后的命令树
 *
 * <p>由 {@link CommandHandlerParser#parse(Object)} 生成，平台后端只依赖本类
 *
 * @author 西博卡兹
 * @since 1.1.0
 */
public final class CommandDefinition {

    private final String name;
    private final List<String> aliases;
    private final String permission;
    private final String description;
    private final String usage;
    private final CommandResolver resolver;

    CommandDefinition(String name, List<String> aliases, String permission, String description,
                      String usage, CommandResolver resolver) {
        this.name = name;
        this.aliases = Collections.unmodifiableList(aliases);
        this.permission = permission == null ? "" : permission;
        this.description = description == null ? "" : description;
        this.usage = usage == null ? "" : usage;
        this.resolver = resolver;
    }

    public String name() {
        return name;
    }

    public List<String> aliases() {
        return aliases;
    }

    public String permission() {
        return permission;
    }

    public String description() {
        return description;
    }

    public String usage() {
        return usage;
    }

    public boolean execute(CommandSender sender, String label, String[] args) {
        return resolver.execute(sender, label, args);
    }

    public List<String> suggest(CommandSender sender, String label, String[] args) {
        return resolver.suggest(sender, label, args);
    }
}
