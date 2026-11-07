package cn.etheros.etheroslib.common.command;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * 命令解析与补全
 *
 * <p>执行与补全共用同一棵命令树，平台差异由 CommandSender 抽象承担
 *
 * @author 西博卡兹
 * @since 1.1.0
 */
final class CommandResolver {

    private final CommandNode root;

    CommandResolver(CommandNode root) {
        this.root = Objects.requireNonNull(root, "root");
    }

    boolean execute(CommandSender sender, String label, String[] args) {
        Objects.requireNonNull(sender, "sender");
        String[] rawArgs = args == null ? new String[0] : args;
        if (!root.isPermitted(sender)) {
            sender.sendMessage("无权限：" + root.permission());
            return false;
        }
        CommandNode node = root;
        int index = 0;
        while (index < rawArgs.length) {
            CommandNode child = node.child(rawArgs[index]);
            if (child == null) {
                break;
            }
            if (!child.isPermitted(sender)) {
                sender.sendMessage("无权限：" + child.permission());
                return false;
            }
            node = child;
            index++;
        }
        CommandMethod method = node.method();
        if (method == null) {
            sender.sendMessage(usage(label, node, null));
            return false;
        }
        Object senderArgument = null;
        Class<?> senderType = method.senderParameter();
        if (senderType != null) {
            if (senderType.isInstance(sender)) {
                senderArgument = sender;
            } else if (senderType.isInstance(sender.raw())) {
                senderArgument = sender.raw();
            } else {
                sender.sendMessage(senderMismatchMessage(senderType));
                return false;
            }
        }
        String[] remaining = Arrays.copyOfRange(rawArgs, index, rawArgs.length);
        if (remaining.length < method.requiredCount() || remaining.length > method.argumentCount()) {
            sender.sendMessage(usage(label, node, method));
            return false;
        }
        CommandContext context = new CommandContext(sender, root.name(), label, rawArgs, node.path());
        try {
            method.invoke(senderArgument, context, remaining);
            return true;
        } catch (CommandArgumentException exception) {
            sender.sendMessage("错误：参数无效：" + exception.getMessage());
            return false;
        }
    }

    List<String> suggest(CommandSender sender, String label, String[] args) {
        Objects.requireNonNull(sender, "sender");
        String[] rawArgs = args == null ? new String[0] : args;
        if (!root.isPermitted(sender)) {
            return Collections.emptyList();
        }
        TabContext context = new SimpleTabContext(sender, label, rawArgs);
        CommandNode node = root;
        int relative = 0;
        for (int index = 0; index < rawArgs.length - 1; index++) {
            CommandNode child = node.child(rawArgs[index]);
            if (child != null && child.isPermitted(sender)) {
                node = child;
                relative = 0;
                continue;
            }
            CommandMethod method = node.method();
            if (method != null && relative < method.argumentCount()) {
                relative++;
                continue;
            }
            return Collections.emptyList();
        }
        return suggestionsAt(node, relative, context, sender);
    }

    private List<String> suggestionsAt(CommandNode node, int relative, TabContext context, CommandSender sender) {
        Set<String> candidates = new LinkedHashSet<String>();
        for (CommandNode child : node.childNodes()) {
            if (child.isPermitted(sender)) {
                candidates.add(child.name());
            }
        }
        CommandMethod method = node.method();
        if (method != null && relative < method.argumentCount()) {
            candidates.addAll(method.suggest(relative, context));
        }
        return TabResult.merge(candidates, context.current());
    }

    private String usage(String label, CommandNode node, CommandMethod method) {
        StringBuilder builder = new StringBuilder("用法：/").append(label);
        for (String segment : node.path()) {
            builder.append(' ').append(segment);
        }
        if (method == null) {
            builder.append(" <子命令>");
        } else if (!method.usage().isEmpty()) {
            builder.append(' ').append(method.usage());
        } else if (!method.argumentUsage().isEmpty()) {
            builder.append(' ').append(method.argumentUsage());
        }
        return builder.toString();
    }

    private String senderMismatchMessage(Class<?> type) {
        String simpleName = type.getSimpleName();
        if ("Player".equals(simpleName) || "ProxiedPlayer".equals(simpleName)) {
            return "该命令只能由玩家执行";
        }
        return "命令执行者类型不匹配：" + simpleName;
    }
}
