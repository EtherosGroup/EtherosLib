package cn.etheros.etheroslib.common.log;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 注入平台日志器到 {@link Logger} 字段。
 *
 * <p>日志器名 = {@code application.properties} 中的 {@code logger.name}（缺省 {@value LogFieldInjector#DEFAULT_BASE_NAME}）
 * + {@code "."} + 后缀；后缀默认取 Bean 类简名，可用 {@link #value()} 覆盖。
 *
 * <p>注入由平台入口注册的 {@link LogFieldInjector} 完成，早于 {@code @PostConstruct}。
 *
 * @author 西博卡兹
 * @since 1.3.0
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Log {

    /**
     * 日志器名后缀，为空时使用 Bean 类简名。
     */
    String value() default "";
}
