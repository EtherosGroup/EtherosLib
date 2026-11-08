package cn.etheros.etheroslib.common.log;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 注入平台日志器到 {@link Logger} 字段。
 *
 * <p>字段级用法（可指定后缀）：
 * <pre>{@code
 * @InjectLogger("database")
 * private Logger dbLogger;
 * }</pre>
 *
 * <p>类级用法：给类本身打注解，该类中所有已声明的 {@link Logger} 字段自动注入，
 * 字段不再需要注解。注意本框架<b>不生成字段</b>，字段仍需自行声明：
 * <pre>{@code
 * @InjectLogger
 * @Service
 * public class AuthService {
 *     private Logger logger;      // 单个字段：<logger.name>.AuthService
 *     private Logger dbLogger;    // 多个字段：<logger.name>.AuthService.dbLogger
 * }
 * }</pre>
 *
 * <p>注入由平台入口注册的 {@link LogFieldInjector} 完成，早于 {@code @PostConstruct}。
 *
 * @author 西博卡兹
 * @since 2.1.0
 */
@Target({ElementType.TYPE, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface InjectLogger {

    /**
     * 日志器名后缀；为空时字段级取 Bean 类简名，类级按字段数量自动生成。
     */
    String value() default "";
}
