package cn.etheros.etheroslib.common.command.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 命令处理器，标注在命令类上
 *
 * <p>同一插件内 name 不可重复；命令通过 {@code CommandHandlerRegistry#register} 注册
 * <p>aliases 作用于整个命令，声明后子命令不允许再声明别名
 *
 * @author 西博卡兹
 * @since 1.1.0
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface CommandHandler {

    /** 命令名，不含斜杠 */
    String name();

    /** 别名 */
    String[] aliases() default {};

    /** 命令级权限，空串表示不校验 */
    String permission() default "";

    /** 描述 */
    String description() default "";

    /** 用法 */
    String usage() default "";
}
