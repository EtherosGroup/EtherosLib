package cn.etheros.etheroslib.common.log;

import java.util.Arrays;

/**
 * SLF4J 风格 {@code {}} 占位符格式化，供 {@link Logger} 默认方法使用。
 *
 * <p>规则：
 * <ul>
 *   <li>按出现顺序把 {@code {}} 替换为对应参数；参数不足时保留原样</li>
 *   <li>多余参数以空格拼接到末尾</li>
 *   <li>参数比占位符多、且最后一个参数是 {@link Throwable} 时，按异常输出而不是占位</li>
 * </ul>
 *
 * @author 西博卡兹
 * @since 2.1.0
 */
final class LogFormats {

    private static final Object[] EMPTY = new Object[0];

    private LogFormats() {
    }

    static Formatted format(String pattern, Object[] args) {
        Object[] effectiveArgs = args == null ? EMPTY : args;
        String text = pattern == null ? "null" : pattern;

        Throwable throwable = null;
        if (effectiveArgs.length > 0
                && effectiveArgs[effectiveArgs.length - 1] instanceof Throwable
                && countPlaceholders(text) < effectiveArgs.length) {
            throwable = (Throwable) effectiveArgs[effectiveArgs.length - 1];
            effectiveArgs = Arrays.copyOf(effectiveArgs, effectiveArgs.length - 1);
        }

        StringBuilder builder = new StringBuilder(text.length() + 32);
        int argIndex = 0;
        int cursor = 0;
        while (argIndex < effectiveArgs.length) {
            int placeholder = text.indexOf("{}", cursor);
            if (placeholder < 0) {
                break;
            }
            builder.append(text, cursor, placeholder).append(String.valueOf(effectiveArgs[argIndex++]));
            cursor = placeholder + 2;
        }
        builder.append(text.substring(cursor));
        for (; argIndex < effectiveArgs.length; argIndex++) {
            builder.append(' ').append(String.valueOf(effectiveArgs[argIndex]));
        }

        return new Formatted(builder.toString(), throwable);
    }

    static int countPlaceholders(String text) {
        int count = 0;
        int cursor = 0;
        while ((cursor = text.indexOf("{}", cursor)) >= 0) {
            count++;
            cursor += 2;
        }
        return count;
    }

    static final class Formatted {

        final String message;
        final Throwable throwable;

        Formatted(String message, Throwable throwable) {
            this.message = message;
            this.throwable = throwable;
        }
    }
}
