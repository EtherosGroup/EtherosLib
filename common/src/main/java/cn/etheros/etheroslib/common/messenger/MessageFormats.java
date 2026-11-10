package cn.etheros.etheroslib.common.messenger;

import cn.etheros.etheroslib.common.text.Text;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 消息格式化
 *
 * <p>规则与 {@code Logger} 的 {@code {}} 占位符一致：
 * <ul>
 *   <li>第一个参数是含 {@code {}} 的字符串时，按顺序替换后续参数</li>
 *   <li>其余参数以空格拼接</li>
 *   <li>{@link Text} 参数保留颜色/装饰，字符串按 legacy 颜色码解析，其他对象取 {@code String.valueOf}</li>
 *   <li>末尾 {@link Throwable}：控制台附加完整堆栈，玩家消息只附加简短的异常摘要</li>
 * </ul>
 *
 * @author 西博卡兹
 * @since 2.3.0
 */
final class MessageFormats {

    private MessageFormats() {
    }

    /**
     * @param messages         原始参数
     * @param includeStackTrace 是否附加异常堆栈（控制台 true，玩家 false）
     */
    static Text format(Object[] messages, boolean includeStackTrace) {
        if (messages == null || messages.length == 0) {
            return Text.empty();
        }
        Object[] values = messages;
        Throwable throwable = null;
        if (values[values.length - 1] instanceof Throwable) {
            boolean firstIsPattern = values[0] instanceof String;
            int placeholders = firstIsPattern ? countPlaceholders((String) values[0]) : 0;
            if (!firstIsPattern || placeholders < values.length - 1) {
                throwable = (Throwable) values[values.length - 1];
                values = Arrays.copyOf(values, values.length - 1);
            }
        }
        Text body;
        if (values.length == 0) {
            body = Text.empty();
        } else if (values[0] instanceof String && countPlaceholders((String) values[0]) > 0) {
            body = formatPattern((String) values[0], values);
        } else {
            body = join(values);
        }
        if (throwable != null) {
            if (includeStackTrace) {
                body = Text.concat(body, Text.plain("\n" + stackTrace(throwable)));
            } else {
                body = Text.concat(body, Text.plain(" " + throwable));
            }
        }
        return body;
    }

    private static Text formatPattern(String pattern, Object[] values) {
        List<Text> parts = new ArrayList<Text>();
        int argumentIndex = 1;
        int cursor = 0;
        while (argumentIndex < values.length) {
            int placeholder = pattern.indexOf("{}", cursor);
            if (placeholder < 0) {
                break;
            }
            parts.add(Text.of(pattern.substring(cursor, placeholder)));
            parts.add(toText(values[argumentIndex++]));
            cursor = placeholder + 2;
        }
        parts.add(Text.of(pattern.substring(cursor)));
        for (; argumentIndex < values.length; argumentIndex++) {
            parts.add(Text.of(" "));
            parts.add(toText(values[argumentIndex]));
        }
        return Text.concat(parts.toArray(new Text[0]));
    }

    private static Text join(Object[] values) {
        List<Text> parts = new ArrayList<Text>();
        for (int index = 0; index < values.length; index++) {
            if (index > 0) {
                parts.add(Text.of(" "));
            }
            parts.add(toText(values[index]));
        }
        return Text.concat(parts.toArray(new Text[0]));
    }

    private static Text toText(Object value) {
        if (value == null) {
            return Text.plain("null");
        }
        if (value instanceof Text) {
            return (Text) value;
        }
        if (value instanceof String) {
            return Text.of((String) value);
        }
        return Text.plain(String.valueOf(value));
    }

    private static int countPlaceholders(String text) {
        int count = 0;
        int cursor = 0;
        while ((cursor = text.indexOf("{}", cursor)) >= 0) {
            count++;
            cursor += 2;
        }
        return count;
    }

    private static String stackTrace(Throwable throwable) {
        StringWriter writer = new StringWriter();
        PrintWriter printer = new PrintWriter(writer);
        throwable.printStackTrace(printer);
        printer.flush();
        return writer.toString();
    }
}
