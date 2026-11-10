package cn.etheros.etheroslib.common.messenger;

import cn.etheros.etheroslib.common.text.Text;

import java.util.Objects;
import java.util.function.Supplier;

/**
 * 消息入口
 *
 * <p>日志走 {@code @InjectLogger}，玩家/控制台消息走本类：
 * 插件启动时构建一次并注册为 DI Bean，业务类注入后通过
 * {@link #console()} / {@link #player(MessageTarget)} 发送
 *
 * <p>前缀与标签都通过 {@link Supplier} 解析，每次发送时求值，配置重载后自动生效
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

    /**
     * 创建构建器
     *
     * @param consoleTarget 控制台消息目标（各平台提供，如 BukkitTargets.console()）
     */
    public static Builder builder(MessageTarget consoleTarget) {
        return new Builder(consoleTarget);
    }

    /** 控制台消息视图（info/warn/error/debug） */
    public ConsoleMessenger console() {
        return new ConsoleMessenger(this);
    }

    /** 玩家消息视图（只有前缀，没有等级标签） */
    public PlayerMessenger player(MessageTarget target) {
        return new PlayerMessenger(this, target);
    }

    MessageTarget consoleTarget() {
        return consoleTarget;
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

    String infoLabel() {
        return resolveLabel(infoLabel);
    }

    String warnLabel() {
        return resolveLabel(warnLabel);
    }

    String errorLabel() {
        return resolveLabel(errorLabel);
    }

    String debugLabel() {
        return resolveLabel(debugLabel);
    }

    private static String resolveLabel(Supplier<String> label) {
        String value = label.get();
        return value == null ? "" : value;
    }

    /**
     * Messenger 构建器
     */
    public static final class Builder {

        private final MessageTarget consoleTarget;
        private Supplier<String> prefix = () -> "";
        private Supplier<String> infoLabel = () -> DEFAULT_INFO_LABEL;
        private Supplier<String> warnLabel = () -> DEFAULT_WARN_LABEL;
        private Supplier<String> errorLabel = () -> DEFAULT_ERROR_LABEL;
        private Supplier<String> debugLabel = () -> DEFAULT_DEBUG_LABEL;
        private Supplier<Boolean> debugEnabled = () -> Boolean.FALSE;

        private Builder(MessageTarget consoleTarget) {
            this.consoleTarget = Objects.requireNonNull(consoleTarget, "consoleTarget");
        }

        /** 消息前缀（支持 & 颜色码），默认空串 */
        public Builder setPrefix(Supplier<String> prefix) {
            this.prefix = Objects.requireNonNull(prefix, "prefix");
            return this;
        }

        /** 消息前缀（固定值） */
        public Builder setPrefix(String prefix) {
            return setPrefix(() -> prefix == null ? "" : prefix);
        }

        /** INFO 标签（支持 & 颜色码），默认 {@value Messenger#DEFAULT_INFO_LABEL} */
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

        public Messenger build() {
            return new Messenger(this);
        }
    }
}
