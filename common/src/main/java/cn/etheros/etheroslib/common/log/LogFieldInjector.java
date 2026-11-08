package cn.etheros.etheroslib.common.log;

import cn.etheros.etherosframework.di.core.ApplicationContext;
import cn.etheros.etherosframework.di.core.PropertyLoader;
import cn.etheros.etherosframework.di.spi.FieldInjectionProvider;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

/**
 * {@link InjectLogger} 字段注入器：为框架 DI 提供平台日志器。
 *
 * <p>支持两种用法：
 * <ul>
 *   <li>字段级：只注入标注字段，{@link InjectLogger#value()} 指定后缀</li>
 *   <li>类级：注入类中所有已声明的 {@link Logger} 字段（单个字段用类简名，
 *       多个字段追加字段名）</li>
 * </ul>
 *
 * <p>日志器名 = {@link #LOGGER_NAME_PROPERTY}（缺省 {@value #DEFAULT_BASE_NAME}）
 * + {@code "."} + 后缀；配置来自当前应用上下文的 {@code application.properties}。
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
        if (field.isAnnotationPresent(InjectLogger.class)) {
            // 显式字段标注：类型 / static 校验交给 resolve，报错更明确
            return true;
        }
        return !Modifier.isStatic(field.getModifiers())
                && field.getDeclaringClass().isAnnotationPresent(InjectLogger.class)
                && Logger.class.isAssignableFrom(field.getType());
    }

    @Override
    public Object resolve(Field field, String beanName, ApplicationContext context) {
        if (Modifier.isStatic(field.getModifiers())) {
            throw new IllegalArgumentException(
                    "@InjectLogger 不支持 static 字段：" + describe(field));
        }
        if (!Logger.class.isAssignableFrom(field.getType())) {
            throw new IllegalArgumentException(
                    "@InjectLogger 字段类型必须是 " + Logger.class.getName()
                            + "，实际为 " + field.getType().getName() + "：" + describe(field));
        }

        return EtherosLog.get(resolveBaseName(context) + "." + resolveSuffix(field));
    }

    private static String resolveSuffix(Field field) {
        InjectLogger annotation = field.getAnnotation(InjectLogger.class);
        if (annotation != null && !annotation.value().trim().isEmpty()) {
            return annotation.value().trim();
        }

        Class<?> declaringClass = field.getDeclaringClass();
        String classSuffix = declaringClass.getSimpleName();
        if (annotation != null) {
            return classSuffix;
        }

        return countLoggerFields(declaringClass) > 1
                ? classSuffix + "." + field.getName()
                : classSuffix;
    }

    private static int countLoggerFields(Class<?> type) {
        int count = 0;
        for (Field candidate : type.getDeclaredFields()) {
            if (Modifier.isStatic(candidate.getModifiers())) {
                continue;
            }
            if (Logger.class.isAssignableFrom(candidate.getType())) {
                count++;
            }
        }
        return count;
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

    private static String describe(Field field) {
        return field.getDeclaringClass().getName() + "#" + field.getName();
    }
}
