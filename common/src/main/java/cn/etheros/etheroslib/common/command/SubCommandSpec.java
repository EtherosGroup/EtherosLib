package cn.etheros.etheroslib.common.command;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * 子命令声明
 *
 * <p>由 {@code @SubCommand} 标注的工厂方法在注册阶段返回，框架据此构建命令树：
 *
 * <pre>{@code
 * @SubCommand("info")
 * public SubCommandSpec info() {
 *     return SubCommandSpec.builder()
 *             .sender(Player.class)
 *             .onError(CommandError.PLAYER_ONLY, "请以玩家身份执行")
 *             .execute(context -> {
 *                 Player player = context.sender(Player.class);
 *             })
 *             .build();
 * }
 * }</pre>
 *
 * <p>错误文案的查找顺序：{@link #builder() 指定的单个错误类型} → 子命令兜底 →
 * 命令实例实现的 {@link CommandErrorHandler} → 注册表默认 → 内置默认
 *
 * @author 西博卡兹
 * @since 2.0.0
 */
public final class SubCommandSpec {

    private final Class<?> senderType;
    private final List<CommandParameter> parameters;
    private final CommandExecutor executor;
    private final Map<CommandError, CommandErrorHandler> handlers;
    private final CommandErrorHandler fallbackHandler;

    private SubCommandSpec(Builder builder) {
        this.senderType = builder.senderType;
        this.parameters = Collections.unmodifiableList(new ArrayList<CommandParameter>(builder.parameters));
        this.executor = builder.executor;
        EnumMap<CommandError, CommandErrorHandler> copiedHandlers =
                new EnumMap<CommandError, CommandErrorHandler>(CommandError.class);
        copiedHandlers.putAll(builder.handlers);
        this.handlers = Collections.unmodifiableMap(copiedHandlers);
        this.fallbackHandler = builder.fallbackHandler;
    }

    /**
     * 创建构建器
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * 仅声明执行器的简写
     */
    public static SubCommandSpec of(CommandExecutor executor) {
        return builder().execute(executor).build();
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

    Map<CommandError, CommandErrorHandler> handlers() {
        return handlers;
    }

    CommandErrorHandler handler(CommandError error) {
        return handlers.get(error);
    }

    CommandErrorHandler fallbackHandler() {
        return fallbackHandler;
    }

    /**
     * 子命令声明构建器
     */
    public static final class Builder {

        private Class<?> senderType = CommandSender.class;
        private final List<CommandParameter> parameters = new ArrayList<CommandParameter>();
        private final Set<String> parameterNames = new LinkedHashSet<String>();
        private final Map<CommandError, CommandErrorHandler> handlers =
                new EnumMap<CommandError, CommandErrorHandler>(CommandError.class);
        private CommandErrorHandler fallbackHandler;
        private CommandExecutor executor;

        private Builder() {
        }

        /**
         * 发送者类型，默认 {@link CommandSender}
         *
         * <p>声明为平台类型（如 Bukkit 的 {@code Player}）时框架负责校验并注入；
         * 不匹配时产生 {@link CommandError#PLAYER_ONLY} 或 {@link CommandError#SENDER_MISMATCH}
         */
        public Builder sender(Class<?> senderType) {
            Objects.requireNonNull(senderType, "senderType");
            if (senderType.isPrimitive()) {
                throw new IllegalStateException("发送者类型不能是基本类型：" + senderType.getName());
            }
            this.senderType = senderType;
            return this;
        }

        /**
         * 追加一个参数
         */
        public Builder parameter(CommandParameter parameter) {
            Objects.requireNonNull(parameter, "parameter");
            if (!parameterNames.add(parameter.name())) {
                throw new IllegalStateException("重复的参数名：" + parameter.name());
            }
            parameters.add(parameter);
            return this;
        }

        /**
         * 追加多个参数
         */
        public Builder parameters(CommandParameter... parameters) {
            Objects.requireNonNull(parameters, "parameters");
            for (CommandParameter parameter : parameters) {
                parameter(parameter);
            }
            return this;
        }

        /**
         * 执行器，注册时必须提供
         */
        public Builder execute(CommandExecutor executor) {
            this.executor = Objects.requireNonNull(executor, "executor");
            return this;
        }

        /**
         * 指定单个错误类型的文案
         *
         * @param error   错误类型
         * @param message 固定文案
         */
        public Builder onError(CommandError error, String message) {
            Objects.requireNonNull(error, "error");
            Objects.requireNonNull(message, "message");
            return onError(error, context -> message);
        }

        /**
         * 指定单个错误类型的文案提供者
         */
        public Builder onError(CommandError error, CommandErrorHandler handler) {
            Objects.requireNonNull(error, "error");
            Objects.requireNonNull(handler, "handler");
            handlers.put(error, handler);
            return this;
        }

        /**
         * 兜底文案提供者：未单独指定的错误类型走这里
         */
        public Builder onError(CommandErrorHandler handler) {
            this.fallbackHandler = Objects.requireNonNull(handler, "handler");
            return this;
        }

        /**
         * 构建声明
         *
         * @throws IllegalStateException 缺少执行器或参数声明非法
         */
        public SubCommandSpec build() {
            if (executor == null) {
                throw new IllegalStateException("子命令缺少执行器");
            }
            return new SubCommandSpec(this);
        }
    }
}
