package cn.etheros.etheroslib.common.command;

import cn.etheros.etheroslib.common.command.annotation.CommandHandler;
import cn.etheros.etheroslib.common.command.annotation.SubCommand;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Objects;

/**
 * 注解解析，生成命令定义
 *
 * <p>路径每段支持 {@code 规范名|别名1|别名2}，别名写在段内
 * <p>{@link SubCommand#aliases()} 作为最后一段的补充别名
 * <p>命令声明 aliases 时，子命令不允许再声明别名
 *
 * @author 西博卡兹
 * @since 1.1.0
 */
public final class CommandHandlerParser {

    private CommandHandlerParser() {
    }

    public static CommandDefinition parse(Object handler) {
        Objects.requireNonNull(handler, "handler");
        Class<?> type = handler.getClass();
        CommandHandler annotation = type.getAnnotation(CommandHandler.class);
        if (annotation == null) {
            throw new IllegalArgumentException("缺少 @CommandHandler：" + type.getName());
        }
        CommandNode root = new CommandNode(annotation.name(), null);
        root.setPermission(annotation.permission());
        root.setDescription(annotation.description());
        root.setUsage(annotation.usage());
        boolean commandHasAliases = annotation.aliases().length > 0;
        for (Method method : type.getDeclaredMethods()) {
            SubCommand subCommand = method.getAnnotation(SubCommand.class);
            if (subCommand == null) {
                continue;
            }
            if (commandHasAliases) {
                if (subCommand.aliases().length > 0) {
                    throw new IllegalStateException("命令已声明别名，子命令不允许再声明别名：" + subCommand.value());
                }
                if (subCommand.value().contains("|")) {
                    throw new IllegalStateException("命令已声明别名，子命令路径不允许写别名：" + subCommand.value());
                }
            }
            CommandNode node = resolve(root, subCommand.value());
            if (node.method() != null) {
                throw new IllegalStateException("重复的子命令：" + join(node.path()));
            }
            node.setMethod(CommandMethod.parse(handler, method, subCommand));
            node.setDescription(subCommand.description());
            node.setUsage(subCommand.usage());
            if (node.parent() == null) {
                // 根执行器默认沿用命令级权限，显式声明时覆盖
                if (!subCommand.permission().isEmpty()) {
                    node.setPermission(subCommand.permission());
                }
                if (subCommand.aliases().length > 0) {
                    throw new IllegalStateException("根执行器不能设置别名：" + method.getName());
                }
                continue;
            }
            node.setPermission(subCommand.permission());
            for (String alias : subCommand.aliases()) {
                if (!alias.isEmpty()) {
                    node.parent().alias(alias, node);
                }
            }
        }
        return new CommandDefinition(annotation.name(), Arrays.asList(annotation.aliases()),
                annotation.permission(), annotation.description(), annotation.usage(),
                new CommandResolver(root));
    }

    private static CommandNode resolve(CommandNode root, String value) {
        String trimmed = value == null ? "" : value.trim();
        if (trimmed.isEmpty()) {
            return root;
        }
        CommandNode node = root;
        for (String segment : trimmed.split("\\s+")) {
            String[] names = segment.split("\\|");
            String canonical = names[0].trim();
            if (canonical.isEmpty()) {
                throw new IllegalStateException("子命令名为空：" + value);
            }
            CommandNode parent = node;
            node = parent.createChild(canonical);
            for (int index = 1; index < names.length; index++) {
                String alias = names[index].trim();
                if (!alias.isEmpty()) {
                    parent.alias(alias, node);
                }
            }
        }
        return node;
    }

    private static String join(java.util.List<String> segments) {
        StringBuilder builder = new StringBuilder();
        for (String segment : segments) {
            if (builder.length() > 0) {
                builder.append(' ');
            }
            builder.append(segment);
        }
        return builder.toString();
    }
}
