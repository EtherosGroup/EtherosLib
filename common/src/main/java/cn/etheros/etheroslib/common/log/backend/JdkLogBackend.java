package cn.etheros.etheroslib.common.log.backend;

import cn.etheros.etheroslib.common.log.LogBackend;
import cn.etheros.etheroslib.common.log.Logger;

import java.util.logging.Level;

/**
 * JDK JUL 后端，用于 Bukkit / BungeeCord 与默认回退。
 *
 * <p>JUL 记录的等级映射：info → INFO、warn → WARNING、error → SEVERE、debug → FINE。
 *
 * @author 西博卡兹
 * @since 1.3.0
 */
public final class JdkLogBackend implements LogBackend {

    @Override
    public Logger create(String name) {
        return new JdkLogger(java.util.logging.Logger.getLogger(name));
    }

    private static final class JdkLogger implements Logger {

        private final java.util.logging.Logger delegate;

        private JdkLogger(java.util.logging.Logger delegate) {
            this.delegate = delegate;
        }

        @Override
        public String getName() {
            return delegate.getName();
        }

        @Override
        public void info(String message) {
            delegate.log(Level.INFO, message);
        }

        @Override
        public void info(String message, Throwable throwable) {
            delegate.log(Level.INFO, message, throwable);
        }

        @Override
        public void warn(String message) {
            delegate.log(Level.WARNING, message);
        }

        @Override
        public void warn(String message, Throwable throwable) {
            delegate.log(Level.WARNING, message, throwable);
        }

        @Override
        public void error(String message) {
            delegate.log(Level.SEVERE, message);
        }

        @Override
        public void error(String message, Throwable throwable) {
            delegate.log(Level.SEVERE, message, throwable);
        }

        @Override
        public void debug(String message) {
            if (isDebugEnabled()) {
                delegate.log(Level.FINE, message);
            }
        }

        @Override
        public void debug(String message, Throwable throwable) {
            if (isDebugEnabled()) {
                delegate.log(Level.FINE, message, throwable);
            }
        }

        @Override
        public boolean isDebugEnabled() {
            return delegate.isLoggable(Level.FINE);
        }
    }
}
