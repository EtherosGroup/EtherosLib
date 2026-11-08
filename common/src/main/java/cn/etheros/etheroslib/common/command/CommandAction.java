package cn.etheros.etheroslib.common.command;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * 已注册的子命令执行配置
 *
 * <p>由 {@link SubCommandSpec} 在注册阶段生成，运行时只读
 *
 * @author 西博卡兹
 * @since 2.0.0
 */
final class CommandAction {

    private final Class<?> senderType;
    private final List<CommandParameter> parameters;
    private final CommandExecutor executor;
    private final Map<CommandError, CommandErrorHandler> handlers;
    private final CommandErrorHandler fallbackHandler;

    CommandAction(SubCommandSpec spec) {
        this.senderType = spec.senderType();
        this.parameters = Collections.unmodifiableList(new ArrayList<CommandParameter>(spec.parameters()));
        this.executor = spec.executor();
        EnumMap<CommandError, CommandErrorHandler> copiedHandlers =
                new EnumMap<CommandError, CommandErrorHandler>(CommandError.class);
        copiedHandlers.putAll(spec.handlers());
        this.handlers = Collections.unmodifiableMap(copiedHandlers);
        this.fallbackHandler = spec.fallbackHandler();
    }

    Class<?> senderType() {
        return senderType;
    }

    List<CommandParameter> parameters() {
        return parameters;
    }

    CommandExecutor executor() {
        return executor;
    }

    CommandErrorHandler handler(CommandError error) {
        return handlers.get(error);
    }

    CommandErrorHandler fallbackHandler() {
        return fallbackHandler;
    }

    int parameterCount() {
        return parameters.size();
    }

    int requiredCount() {
        int required = 0;
        for (CommandParameter parameter : parameters) {
            if (!parameter.optional()) {
                required++;
            }
        }
        return required;
    }

    String parameterUsage() {
        StringBuilder builder = new StringBuilder();
        for (CommandParameter parameter : parameters) {
            if (builder.length() > 0) {
                builder.append(' ');
            }
            builder.append(parameter.usage());
        }
        return builder.toString();
    }

    Collection<String> suggest(int index, TabContext context) {
        if (index < 0 || index >= parameters.size()) {
            return Collections.emptyList();
        }
        return parameters.get(index).suggest(context);
    }
}
