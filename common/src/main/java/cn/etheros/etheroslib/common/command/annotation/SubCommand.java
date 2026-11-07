package cn.etheros.etheroslib.common.command.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 子命令，标注在方法上
 *
 * <p>value 为空格分隔的路径，如 {@code "admin reload"}；空串表示根执行器
 * <p>命令未声明别名时，每段可写 {@code 规范名|别名1|别名2}，中间段同样支持
 * <p>aliases 作为最后一段的补充别名；命令已声明别名时两者都不允许使用
 *
 * @author 西博卡兹
 * @since 1.1.0
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface SubCommand {

    /** 空格分隔的子命令路径，空串表示根执行器；每段可写 {@code 规范名|别名} */
    String value() default "";

    /** 最后一段的补充别名 */
    String[] aliases() default {};

    /** 子命令级权限，空串表示不校验 */
    String permission() default "";

    /** 描述 */
    String description() default "";

    /** 用法，空串时按参数自动生成 */
    String usage() default "";
}
