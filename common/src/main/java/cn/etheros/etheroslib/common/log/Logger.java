package cn.etheros.etheroslib.common.log;

/**
 * 平台无关日志器门面，后端由 {@link LogBackend} 提供。
 *
 * <p>占位符统一使用 SLF4J 风格的 {@code {}}，Bukkit 与 Velocity 两端语义一致：
 * <pre>{@code
 * logger.warn("玩家 {} 在 {} 登录失败", playerName, time);
 * logger.error("加载失败", exception);   // 末尾 Throwable 按异常输出
 * }</pre>
 *
 * @author 西博卡兹
 * @since 1.3.0
 */
public interface Logger {

    /**
     * 日志器名，形如 {@code EtherosAuth.AuthService}。
     */
    String getName();

    void info(String message);

    void info(String message, Throwable throwable);

    void warn(String message);

    void warn(String message, Throwable throwable);

    void error(String message);

    void error(String message, Throwable throwable);

    void debug(String message);

    void debug(String message, Throwable throwable);

    /**
     * 当前后端是否输出 debug 级别。
     */
    boolean isDebugEnabled();

    default void info(String format, Object... args) {
        LogFormats.Formatted formatted = LogFormats.format(format, args);
        if (formatted.throwable == null) {
            info(formatted.message);
        } else {
            info(formatted.message, formatted.throwable);
        }
    }

    default void warn(String format, Object... args) {
        LogFormats.Formatted formatted = LogFormats.format(format, args);
        if (formatted.throwable == null) {
            warn(formatted.message);
        } else {
            warn(formatted.message, formatted.throwable);
        }
    }

    default void error(String format, Object... args) {
        LogFormats.Formatted formatted = LogFormats.format(format, args);
        if (formatted.throwable == null) {
            error(formatted.message);
        } else {
            error(formatted.message, formatted.throwable);
        }
    }

    default void debug(String format, Object... args) {
        if (!isDebugEnabled()) {
            return;
        }
        LogFormats.Formatted formatted = LogFormats.format(format, args);
        if (formatted.throwable == null) {
            debug(formatted.message);
        } else {
            debug(formatted.message, formatted.throwable);
        }
    }
}
