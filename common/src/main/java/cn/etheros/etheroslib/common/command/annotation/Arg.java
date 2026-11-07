package cn.etheros.etheroslib.common.command.annotation;

import cn.etheros.etheroslib.common.command.TabProvider;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 命令参数，标注在方法参数上
 *
 * <p>参数类型决定解析器与默认补全；completer 指定时改用其补全
 *
 * @author 西博卡兹
 * @since 1.1.0
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface Arg {

    /** 参数名，用于错误提示；空串时取类型名 */
    String value() default "";

    /** 是否可省略 */
    boolean optional() default false;

    /** 自定义补全器，需有无参构造 */
    Class<? extends TabProvider> completer() default TabProvider.class;
}
