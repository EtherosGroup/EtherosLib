package cn.etheros.etheroslib.common.command;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * 命令树节点
 *
 * <p>children 存规范名，aliases 存别名，均为小写键；action 为空表示纯路径节点
 *
 * @author 西博卡兹
 * @since 1.1.0
 */
final class CommandNode {

    private final String name;
    private final CommandNode parent;
    private final Map<String, CommandNode> children = new LinkedHashMap<String, CommandNode>();
    private final Map<String, CommandNode> aliases = new LinkedHashMap<String, CommandNode>();
    private final Set<CommandNode> childNodes = new LinkedHashSet<CommandNode>();

    private CommandAction action;
    private String permission = "";
    private String description = "";
    private String usage = "";

    CommandNode(String name, CommandNode parent) {
        this.name = name;
        this.parent = parent;
    }

    String name() {
        return name;
    }

    CommandNode parent() {
        return parent;
    }

    CommandAction action() {
        return action;
    }

    void setAction(CommandAction action) {
        this.action = action;
    }

    String permission() {
        return permission;
    }

    void setPermission(String permission) {
        this.permission = permission == null ? "" : permission;
    }

    String description() {
        return description;
    }

    void setDescription(String description) {
        this.description = description == null ? "" : description;
    }

    String usage() {
        return usage;
    }

    void setUsage(String usage) {
        this.usage = usage == null ? "" : usage;
    }

    CommandNode child(String literal) {
        String key = literal.toLowerCase(Locale.ROOT);
        CommandNode node = children.get(key);
        return node != null ? node : aliases.get(key);
    }

    CommandNode createChild(String literal) {
        String key = literal.toLowerCase(Locale.ROOT);
        CommandNode node = children.get(key);
        if (node == null) {
            node = new CommandNode(literal, this);
            children.put(key, node);
            childNodes.add(node);
        }
        return node;
    }

    void alias(String alias, CommandNode target) {
        String key = alias.toLowerCase(Locale.ROOT);
        CommandNode canonical = children.get(key);
        if (canonical != null && canonical != target) {
            throw new IllegalStateException("别名与子命令冲突：" + alias);
        }
        CommandNode existing = aliases.get(key);
        if (existing != null && existing != target) {
            throw new IllegalStateException("别名冲突：" + alias);
        }
        aliases.put(key, target);
    }

    List<CommandNode> childNodes() {
        return new ArrayList<CommandNode>(childNodes);
    }

    /** 从根子节点开始的规范名路径 */
    List<String> path() {
        List<String> segments = new ArrayList<String>();
        for (CommandNode node = this; node.parent != null; node = node.parent) {
            segments.add(0, node.name);
        }
        return Collections.unmodifiableList(segments);
    }

    boolean isPermitted(CommandSender sender) {
        return permission.isEmpty() || sender.hasPermission(permission);
    }
}
