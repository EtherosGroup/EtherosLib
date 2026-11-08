package cn.etheros.etheroslib.common.log;

import cn.etheros.etheroslib.common.log.backend.JdkLogBackend;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 日志器注册表：保存当前平台后端，并缓存按名创建的日志器。
 *
 * <p>平台入口在启动时调用 {@link #installBackend(LogBackend)} 切换后端；
 * 未安装时使用 JDK JUL 后端。
 *
 * @author 西博卡兹
 * @since 1.3.0
 */
public final class EtherosLog {

    /**
     * 未配置 {@code logger.name} 时使用的日志器名前缀。
     */
    public static final String DEFAULT_LOGGER_NAME = "Etheros";

    private static final Map<String, Logger> LOGGERS = new ConcurrentHashMap<>();

    private static volatile LogBackend backend = new JdkLogBackend();

    private EtherosLog() {
    }

    /**
     * 安装平台后端；已缓存的日志器会被清空，后续按新后端重建。
     */
    public static void installBackend(LogBackend newBackend) {
        backend = Objects.requireNonNull(newBackend, "backend");
        LOGGERS.clear();
    }

    public static LogBackend getBackend() {
        return backend;
    }

    /**
     * 获取（并缓存）指定名字的日志器。
     */
    public static Logger get(String name) {
        String key = name == null || name.trim().isEmpty() ? DEFAULT_LOGGER_NAME : name.trim();
        Logger cached = LOGGERS.get(key);
        if (cached != null) {
            return cached;
        }
        Logger created = backend.create(key);
        Logger previous = LOGGERS.putIfAbsent(key, created);
        return previous != null ? previous : created;
    }

    /**
     * 恢复默认后端并清空缓存（测试或平台卸载时使用）。
     */
    public static void reset() {
        backend = new JdkLogBackend();
        LOGGERS.clear();
    }
}
