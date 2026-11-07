package cn.etheros.etheroslib.common;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 跨平台核心：记录运行平台与版本，并提供跨模块共享的服务注册表。
 * <p>
 * 各平台模块在启动时调用 {@link #initialize(Platform, String)} 完成绑定。
 */
public final class EtherosLibCore {

    private static final EtherosLibCore INSTANCE = new EtherosLibCore();

    private final Map<Class<?>, Object> services = new ConcurrentHashMap<>();

    private volatile Platform platform = Platform.UNKNOWN;
    private volatile String version = "unknown";
    private volatile boolean initialized;

    private EtherosLibCore() {
    }

    public static EtherosLibCore get() {
        return INSTANCE;
    }

    /**
     * 绑定平台与版本，由各平台入口在启动时调用。
     */
    public synchronized void initialize(Platform platform, String version) {
        this.platform = Objects.requireNonNull(platform, "platform");
        this.version = Objects.requireNonNull(version, "version");
        this.initialized = true;
    }

    public boolean isInitialized() {
        return initialized;
    }

    public Platform getPlatform() {
        return platform;
    }

    public String getVersion() {
        return version;
    }

    /**
     * 注册服务，同类型重复注册时返回被替换的旧实例。
     */
    public <T> T registerService(Class<T> type, T service) {
        Objects.requireNonNull(type, "type");
        Objects.requireNonNull(service, "service");
        Object previous = services.put(type, service);
        return previous == null ? null : type.cast(previous);
    }

    /**
     * 获取服务，未注册时返回 {@code null}。
     */
    public <T> T getService(Class<T> type) {
        Objects.requireNonNull(type, "type");
        Object service = services.get(type);
        return service == null ? null : type.cast(service);
    }

    /**
     * 获取服务，未注册时抛出 {@link IllegalStateException}。
     */
    public <T> T requireService(Class<T> type) {
        T service = getService(type);
        if (service == null) {
            throw new IllegalStateException("Service not registered: " + type.getName());
        }
        return service;
    }

    public void clearServices() {
        services.clear();
    }
}
