package cn.etheros.etheroslib.common.log;

import cn.etheros.etherosframework.di.core.ApplicationContext;
import cn.etheros.etherosframework.di.core.PropertyLoader;
import cn.etheros.etherosframework.di.spi.FieldInjectionProvider;

import java.lang.reflect.Field;

/**
 * {@link Log} 字段注入器：为框架 DI 提供平台日志器。
 *
 * <p>日志器名 = {@link #LOGGER_NAME_PROPERTY}（缺省 {@value #DEFAULT_BASE_NAME}）+ {@code "."}
 * + 后缀；后缀取 {@link Log#value()}，为空时取 Bean 类简名。
 * 配置来自当前应用上下文的 {@code application.properties}（插件 jar 根目录）。
 *
 * @author 西博卡兹
 * @since 1.3.0
 */
public final class LogFieldInjector implements FieldInjectionProvider {

    /**
     * application.properties 中配置日志名前缀的键。
     */
    public static final String LOGGER_NAME_PROPERTY = "logger.name";

    /**
     * 未配置 {@link #LOGGER_NAME_PROPERTY} 时使用的默认前缀。
     */
    public static final String DEFAULT_BASE_NAME = "Etheros";

    @Override
    public boolean supports(Field field) {
        return field.isAnnotationPresent(Log.class);
    }

    @Override
    public Object resolve(Field field, String beanName, ApplicationContext context) {
        if (!Logger.class.isAssignableFrom(field.getType())) {
            throw new IllegalArgumentException("@Log 字段类型必须是 " + Logger.class.getName()
                    + "，实际为 " + field.getType().getName()
                    + "（" + field.getDeclaringClass().getName() + "#" + field.getName() + "）");
        }

        Log annotation = field.getAnnotation(Log.class);
        String suffix = annotation.value().trim().isEmpty()
                ? field.getDeclaringClass().getSimpleName()
                : annotation.value().trim();

        return EtherosLog.get(resolveBaseName(context) + "." + suffix);
    }

    private static String resolveBaseName(ApplicationContext context) {
        if (context == null) {
            return DEFAULT_BASE_NAME;
        }
        PropertyLoader loader = context.getPropertyLoader();
        if (loader == null) {
            return DEFAULT_BASE_NAME;
        }
        String configured = loader.getProperty(LOGGER_NAME_PROPERTY);
        if (configured == null || configured.trim().isEmpty()) {
            return DEFAULT_BASE_NAME;
        }
        return configured.trim();
    }
}
