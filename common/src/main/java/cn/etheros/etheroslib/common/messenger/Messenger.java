package cn.etheros.etheroslib.common.messenger;

import cn.etheros.etheroslib.common.text.Text;

import java.util.Arrays;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * 消息入口
 *
 * <p>日志走 {@code @InjectLogger}，面向控制台与玩家的消息走本类：
 * 插件启动时通过 {@link Builder} 构建一次并注册为 DI Bean，业务类注入后直接调用
 * {@link #info(Object...)}、{@link #warn(Object...)}、{@link #error(Object...)}、
 * {@link #debug(Object...)} 与 {@link #player(MessageTarget)}
 *
 * <pre>{@code
 * Messenger messenger = new Messenger.Builder()
 *         .setConsole(BukkitTargets.console())
 *         .setPrefix("[EtherosAuth]")
 *         .build();
 *
 * messenger.info("插件已启用");
 * messenger.player(BukkitTargets.of(player)).send("&a欢迎回来");
 * }</pre>
 *
 * <p>前缀与标签都通过 {@link Supplier} 解析，每次发送时求值，配置重载后自动生效
 *
 * <p>控制台输出格式为 {@code prefix + label + message}，不自动补空格；
 * 颜色原样交给平台控制台目标渲染，服务端日志会自行记录纯文本
 *
 * @author 西博卡兹
 * @since 2.3.0
 */
public final class Messenger {

    /** 默认 INFO 标签 */
    public static final String DEFAULT_INFO_LABEL = "[INFO ] ";
    /** 默认 WARN 标签 */
    public static final String DEFAULT_WARN_LABEL = "[WARN ] ";
    /** 默认 ERROR 标签 */
    public static final String DEFAULT_ERROR_LABEL = "[ERROR] ";
    /** 默认 DEBUG 标签 */
    public static final String DEFAULT_DEBUG_LABEL = "[DEBUG] ";

    private final MessageTarget consoleTarget;
    private final Supplier<String> prefix;
    private final Supplier<String> infoLabel;
    private final Supplier<String> warnLabel;
    private final Supplier<String> errorLabel;
    private final Supplier<String> debugLabel;
    private final Supplier<Boolean> debugEnabled;

    private Messenger(Builder builder) {
        this.consoleTarget = builder.consoleTarget;
        this.prefix = builder.prefix;
        this.infoLabel = builder.infoLabel;
        this.warnLabel = builder.warnLabel;
        this.errorLabel = builder.errorLabel;
        this.debugLabel = builder.debugLabel;
        this.debugEnabled = builder.debugEnabled;
    }

    /** 创建构建器，控制台目标由 {@link Builder#setConsole(MessageTarget)} 指定 */
    public static Builder builder() {
        return new Builder();
    }

    /** 创建构建器并指定控制台目标 */
    public static Builder builder(MessageTarget consoleTarget) {
        return new Builder().setConsole(consoleTarget);
    }

    /**
     * INFO 消息
     *
     * <p>首个参数为 {@link Boolean} 时视为 {@code enablePrefix}（缺省 true），
     * 例如 {@code info(false, "静默提示")}
     */
    public void info(Object... messages) {
        log(false, infoLabel, messages);
    }

    /** WARN 消息，参数约定同 {@link #info(Object...)} */
    public void warn(Object... messages) {
        log(false, warnLabel, messages);
    }

    /** ERROR 消息，参数约定同 {@link #info(Object...)}；末尾 {@link Throwable} 会附加堆栈 */
    public void error(Object... messages) {
        log(false, errorLabel, messages);
    }

    /** DEBUG 消息；{@code debugEnabled} 为 false 时不输出 */
    public void debug(Object... messages) {
        log(true, debugLabel, messages);
    }

    /** 玩家消息视图（只有前缀，没有等级标签） */
    public PlayerMessenger player(MessageTarget target) {
        return new PlayerMessenger(this, target);
    }

    private void log(boolean debug, Supplier<String> label, Object[] arguments) {
        if (debug && !debugEnabled()) {
            return;
        }
        if (arguments == null || arguments.length == 0) {
            return;
        }
        boolean enablePrefix = true;
        Object[] messages = arguments;
        if (messages[0] instanceof Boolean) {
            enablePrefix = (Boolean) messages[0];
            messages = Arrays.copyOfRange(messages, 1, messages.length);
        }
        if (messages.length == 0) {
            return;
        }
        Text body = MessageFormats.format(messages, true);
        if (body.isEmpty()) {
            return;
        }
        Text output = Text.concat(prefix(enablePrefix), Text.of(label.get()), body);
        consoleTarget.send(output);
    }

    boolean debugEnabled() {
        Boolean value = debugEnabled.get();
        return value != null && value;
    }

    Text prefix(boolean enable) {
        if (!enable) {
            return Text.empty();
        }
        String value = prefix.get();
        return value == null || value.isEmpty() ? Text.empty() : Text.of(value);
    }

    /**
     * Messenger 构建器
     */
    public static final class Builder {

        private MessageTarget consoleTarget;
        private Supplier<String> prefix = () -> "";
        private Supplier<String> infoLabel = () -> DEFAULT_INFO_LABEL;
        private Supplier<String> warnLabel = () -> DEFAULT_WARN_LABEL;
        private Supplier<String> errorLabel = () -> DEFAULT_ERROR_LABEL;
        private Supplier<String> debugLabel = () -> DEFAULT_DEBUG_LABEL;
        private Supplier<Boolean> debugEnabled = () -> Boolean.FALSE;

        public Builder() {
        }

        /** 控制台消息目标（各平台提供，如 BukkitTargets.console()），构建时必须设置 */
        public Builder setConsole(MessageTarget consoleTarget) {
            this.consoleTarget = Objects.requireNonNull(consoleTarget, "consoleTarget");
            return this;
        }

        /** 消息前缀（支持 {@code &} 颜色码），默认空串 */
        public Builder setPrefix(Supplier<String> prefix) {
            this.prefix = Objects.requireNonNull(prefix, "prefix");
            return this;
        }

        /** 消息前缀（固定值） */
        public Builder setPrefix(String prefix) {
            return setPrefix(() -> prefix == null ? "" : prefix);
        }

        /** INFO 标签（支持 {@code &} 颜色码），默认 {@value Messenger#DEFAULT_INFO_LABEL} */
        public Builder setInfo(Supplier<String> infoLabel) {
            this.infoLabel = Objects.requireNonNull(infoLabel, "infoLabel");
            return this;
        }

        /** INFO 标签（固定值） */
        public Builder setInfo(String infoLabel) {
            return setInfo(() -> infoLabel == null ? "" : infoLabel);
        }

        /** WARN 标签，默认 {@value Messenger#DEFAULT_WARN_LABEL} */
        public Builder setWarn(Supplier<String> warnLabel) {
            this.warnLabel = Objects.requireNonNull(warnLabel, "warnLabel");
            return this;
        }

        /** WARN 标签（固定值） */
        public Builder setWarn(String warnLabel) {
            return setWarn(() -> warnLabel == null ? "" : warnLabel);
        }

        /** ERROR 标签，默认 {@value Messenger#DEFAULT_ERROR_LABEL} */
        public Builder setError(Supplier<String> errorLabel) {
            this.errorLabel = Objects.requireNonNull(errorLabel, "errorLabel");
            return this;
        }

        /** ERROR 标签（固定值） */
        public Builder setError(String errorLabel) {
            return setError(() -> errorLabel == null ? "" : errorLabel);
        }

        /** DEBUG 标签，默认 {@value Messenger#DEFAULT_DEBUG_LABEL} */
        public Builder setDebug(Supplier<String> debugLabel) {
            this.debugLabel = Objects.requireNonNull(debugLabel, "debugLabel");
            return this;
        }

        /** DEBUG 标签（固定值） */
        public Builder setDebug(String debugLabel) {
            return setDebug(() -> debugLabel == null ? "" : debugLabel);
        }

        /** debug 开关，默认关闭；关闭时 debug 消息不输出 */
        public Builder setDebugEnabled(Supplier<Boolean> debugEnabled) {
            this.debugEnabled = Objects.requireNonNull(debugEnabled, "debugEnabled");
            return this;
        }

        /** debug 开关（固定值） */
        public Builder setDebugEnabled(boolean debugEnabled) {
            return setDebugEnabled(() -> debugEnabled);
        }

        /**
         * 构建 Messenger
         *
         * @throws IllegalStateException 未通过 {@link #setConsole(MessageTarget)} 指定控制台目标
         */
        public Messenger build() {
            if (consoleTarget == null) {
                throw new IllegalStateException("控制台消息目标未设置：调用 setConsole(...)");
            }
            return new Messenger(this);
        }
    }
}
