package cn.etheros.etheroslib.common.log;

/**
 * 平台无关日志器门面，后端由 {@link LogBackend} 提供。
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
}
