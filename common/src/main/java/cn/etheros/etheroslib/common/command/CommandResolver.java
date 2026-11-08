package cn.etheros.etheroslib.common.command;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 命令解析与补全
 *
 * <p>执行与补全共用同一棵命令树，平台差异由 CommandSender 抽象承担；
 * 失败时先经 {@link CommandErrorHandler} 生成文案，未命中再回退内置默认
 *
 * @author 西博卡兹
 * @since 1.1.0
 */
final class CommandResolver {

    private static final Logger LOGGER = Logger.getLogger("cn.etheros.etheroslib.command");

    private final CommandNode root;
    private final CommandErrorHandler commandErrorHandler;
    private final CommandErrorHandler defaultErrorHandler;

    CommandResolver(CommandNode root, CommandErrorHandler commandErrorHandler,
                    CommandErrorHandler defaultErrorHandler) {
        this.root = Objects.requireNonNull(root, "root");
        this.commandErrorHandler = commandErrorHandler;
        this.defaultErrorHandler = defaultErrorHandler;
    }

    boolean execute(CommandSender sender, String label, String[] args) {
        Objects.requireNonNull(sender, "sender");
        String[] rawArgs = args == null ? new String[0] : args;
        String command = root.name();

        if (!root.isPermitted(sender)) {
            return deny(CommandErrorContext.permissionDenied(sender, command, label, rawArgs,
                    root.path(), root.permission()), root.action());
        }
        CommandNode node = root;
        int index = 0;
        while (index < rawArgs.length) {
            CommandNode child = node.child(rawArgs[index]);
            if (child == null) {
                break;
            }
            if (!child.isPermitted(sender)) {
                return deny(CommandErrorContext.permissionDenied(sender, command, label, rawArgs,
                        child.path(), child.permission()), child.action());
            }
            node = child;
            index++;
        }
        CommandAction action = node.action();
        if (action == null) {
            return deny(CommandErrorContext.usageRequired(CommandError.NO_EXECUTOR, sender, command, label,
                    rawArgs, node.path(), usage(label, node, null), 0, 0), null);
        }
        Class<?> senderType = action.senderType();
        if (senderType != null && !senderType.isInstance(sender) && !senderType.isInstance(sender.raw())) {
            CommandError error = isPlayerType(senderType)
                    ? CommandError.PLAYER_ONLY : CommandError.SENDER_MISMATCH;
            return deny(CommandErrorContext.senderMismatch(error, sender, command, label, rawArgs,
                    node.path(), senderType), action);
        }
        int remaining = rawArgs.length - index;
        int required = action.requiredCount();
        int maximum = action.parameterCount();
        if (remaining < required || remaining > maximum) {
            return deny(CommandErrorContext.usageRequired(CommandError.INVALID_ARGUMENT_COUNT, sender, command,
                    label, rawArgs, node.path(), usage(label, node, action), required, maximum), action);
        }
        Object[] values = new Object[maximum];
        for (int offset = 0; offset < remaining; offset++) {
            CommandParameter parameter = action.parameters().get(offset);
            try {
                values[offset] = parameter.parse(rawArgs[index + offset]);
            } catch (CommandArgumentException exception) {
                return deny(CommandErrorContext.invalidArgument(sender, command, label, rawArgs,
                        node.path(), parameter.name(), rawArgs[index + offset], exception.getMessage()), action);
            }
        }
        CommandContext context = new CommandContext(sender, command, label, rawArgs, node.path(),
                action.parameters(), values);
        try {
            action.executor().execute(context);
            return true;
        } catch (CommandArgumentException exception) {
            return deny(CommandErrorContext.invalidArgument(sender, command, label, rawArgs,
                    node.path(), "", "", exception.getMessage()), action);
        } catch (Exception exception) {
            CommandErrorContext errorContext = CommandErrorContext.executionError(sender, command, label,
                    rawArgs, node.path(), exception);
            String message = resolveMessage(errorContext, action);
            if (message == null) {
                if (exception instanceof RuntimeException) {
                    throw (RuntimeException) exception;
                }
                throw new IllegalStateException(exception);
            }
            if (!message.isEmpty()) {
                sender.sendMessage(message);
            }
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
            CommandAction action = node.action();
            if (action != null && relative < action.parameterCount()) {
                relative++;
                continue;
            }
            return Collections.emptyList();
        }
        Set<String> candidates = new LinkedHashSet<String>();
        for (CommandNode child : node.childNodes()) {
            if (child.isPermitted(sender)) {
                candidates.add(child.name());
            }
        }
        CommandAction action = node.action();
        if (action != null && relative < action.parameterCount()) {
            candidates.addAll(action.suggest(relative, context));
        }
        return TabResult.merge(candidates, context.current());
    }

    private boolean deny(CommandErrorContext context, CommandAction action) {
        String message = resolveMessage(context, action);
        if (message == null) {
            message = context.defaultMessage();
        }
        if (!message.isEmpty()) {
            context.sender().sendMessage(message);
        }
        return false;
    }

    private String resolveMessage(CommandErrorContext context, CommandAction action) {
        if (action != null) {
            String message = invoke(action.handler(context.error()), context);
            if (message != null) {
                return message;
            }
            message = invoke(action.fallbackHandler(), context);
            if (message != null) {
                return message;
            }
        }
        String message = invoke(commandErrorHandler, context);
        if (message != null) {
            return message;
        }
        return invoke(defaultErrorHandler, context);
    }

    private String invoke(CommandErrorHandler handler, CommandErrorContext context) {
        if (handler == null) {
            return null;
        }
        try {
            return handler.message(context);
        } catch (RuntimeException exception) {
            LOGGER.log(Level.WARNING, "命令错误文案处理失败，回退默认文案", exception);
            return null;
        }
    }

    private boolean isPlayerType(Class<?> type) {
        String simpleName = type.getSimpleName();
        return "Player".equals(simpleName) || "ProxiedPlayer".equals(simpleName);
    }

    private String usage(String label, CommandNode node, CommandAction action) {
        StringBuilder builder = new StringBuilder("用法：/").append(label == null ? node.name() : label);
        for (String segment : node.path()) {
            builder.append(' ').append(segment);
        }
        if (action == null) {
            builder.append(" <子命令>");
        } else if (!node.usage().isEmpty()) {
            builder.append(' ').append(node.usage());
        } else if (!action.parameterUsage().isEmpty()) {
            builder.append(' ').append(action.parameterUsage());
        }
        return builder.toString();
    }
}
