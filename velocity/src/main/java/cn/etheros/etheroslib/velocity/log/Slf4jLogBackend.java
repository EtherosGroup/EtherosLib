package cn.etheros.etheroslib.velocity.log;

import cn.etheros.etheroslib.common.log.LogBackend;
import cn.etheros.etheroslib.common.log.Logger;
import org.slf4j.LoggerFactory;

/**
 * Velocity 的 SLF4J 日志后端。
 *
 * @author 西博卡兹
 * @since 1.3.0
 */
public final class Slf4jLogBackend implements LogBackend {

    @Override
    public Logger create(String name) {
        return new Slf4jLogger(LoggerFactory.getLogger(name));
    }

    private static final class Slf4jLogger implements Logger {

        private final org.slf4j.Logger delegate;

        private Slf4jLogger(org.slf4j.Logger delegate) {
            this.delegate = delegate;
        }

        @Override
        public String getName() {
            return delegate.getName();
        }

        @Override
        public void info(String message) {
            delegate.info(message);
        }

        @Override
        public void info(String message, Throwable throwable) {
            delegate.info(message, throwable);
        }

        @Override
        public void warn(String message) {
            delegate.warn(message);
        }

        @Override
        public void warn(String message, Throwable throwable) {
            delegate.warn(message, throwable);
        }

        @Override
        public void error(String message) {
            delegate.error(message);
        }

        @Override
        public void error(String message, Throwable throwable) {
            delegate.error(message, throwable);
        }

        @Override
        public void debug(String message) {
            delegate.debug(message);
        }

        @Override
        public void debug(String message, Throwable throwable) {
            delegate.debug(message, throwable);
        }

        @Override
        public void info(String format, Object... args) {
            delegate.info(format, args);
        }

        @Override
        public void warn(String format, Object... args) {
            delegate.warn(format, args);
        }

        @Override
        public void error(String format, Object... args) {
            delegate.error(format, args);
        }

        @Override
        public void debug(String format, Object... args) {
            delegate.debug(format, args);
        }

        @Override
        public boolean isDebugEnabled() {
            return delegate.isDebugEnabled();
        }
    }
}
