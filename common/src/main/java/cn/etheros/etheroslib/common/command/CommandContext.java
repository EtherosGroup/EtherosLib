package cn.etheros.etheroslib.common.command;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 命令执行上下文
 *
 * <p>发送者与参数都从本类以类型化方式读取：{@code ctx.sender(Player.class)}、
 * {@code ctx.argument("password")}；参数名是 {@link CommandParameter} 声明的查找键，
 * 建议使用稳定英文名，便于接入 i18n
 *
 * @author 西博卡兹
 * @since 1.1.0
 */
public final class CommandContext {

    private final CommandSender sender;
    private final String command;
    private final String label;
    private final String[] args;
    private final List<String> path;
    private final List<CommandParameter> parameters;
    private final Object[] values;
    private final Map<String, Integer> indexes;

    CommandContext(CommandSender sender, String command, String label, String[] args, List<String> path,
                   List<CommandParameter> parameters, Object[] values) {
        this.sender = Objects.requireNonNull(sender, "sender");
        this.command = Objects.requireNonNull(command, "command");
        this.label = label == null ? command : label;
        this.args = args == null ? new String[0] : args.clone();
        this.path = Collections.unmodifiableList(new ArrayList<String>(path));
        this.parameters = Collections.unmodifiableList(new ArrayList<CommandParameter>(parameters));
        this.values = values == null ? new Object[0] : values.clone();
        Map<String, Integer> index = new LinkedHashMap<String, Integer>();
        for (int i = 0; i < this.parameters.size(); i++) {
            index.put(this.parameters.get(i).name(), i);
        }
        this.indexes = Collections.unmodifiableMap(index);
    }

    /** 执行者，平台无关抽象 */
    public CommandSender sender() {
        return sender;
    }

    /**
     * 按声明类型取发送者
     *
     * <p>先匹配 EtherosLib 包装器，再匹配平台原始对象，例如
     * {@code Player player = ctx.sender(Player.class);}
     *
     * @throws IllegalArgumentException 执行者类型与声明不符
     */
    public <T> T sender(Class<T> type) {
        Objects.requireNonNull(type, "type");
        if (type.isInstance(sender)) {
            return type.cast(sender);
        }
        Object raw = sender.raw();
        if (type.isInstance(raw)) {
            return type.cast(raw);
        }
        throw new IllegalArgumentException("发送者类型不匹配：" + type.getName());
    }

    /** 平台原始发送者 */
    public Object rawSender() {
        return sender.raw();
    }

    /** 命令名 */
    public String command() {
        return command;
    }

    /** 实际输入的命令标签，可能为别名 */
    public String label() {
        return label;
    }

    /** 原始参数，含子命令段 */
    public String[] args() {
        return args.clone();
    }

    /** 已匹配的子命令路径 */
    public List<String> path() {
        return path;
    }

    /** 声明的参数个数 */
    public int argumentCount() {
        return parameters.size();
    }

    /** 是否声明了该参数 */
    public boolean hasArgument(String name) {
        return indexes.containsKey(name);
    }

    /**
     * 取参数值，类型按赋值目标推断，不需要传 class
     *
     * <pre>{@code
     * String password = ctx.argument("password");
     * int limit = ctx.argument("limit");
     * }</pre>
     *
     * <p>可选参数未提供时返回 null
     *
     * @throws IllegalArgumentException 参数名未声明
     */
    public <T> T argument(String name) {
        return castUnchecked(name, value(name));
    }

    /**
     * 取参数值并做类型检查
     *
     * @throws IllegalArgumentException 参数名未声明或类型不符
     */
    public <T> T argument(String name, Class<T> type) {
        Objects.requireNonNull(type, "type");
        Object value = value(name);
        if (value == null) {
            return null;
        }
        Class<?> expected = type.isPrimitive() ? box(type) : type;
        if (!expected.isInstance(value)) {
            throw new IllegalArgumentException("参数类型不匹配：" + name + "，期望 "
                    + expected.getName() + "，实际 " + value.getClass().getName());
        }
        return castUnchecked(name, expected.cast(value));
    }

    /** 全部参数值，未提供的可选参数为 null */
    public List<Object> argumentValues() {
        return Collections.unmodifiableList(Arrays.asList(values.clone()));
    }

    @SuppressWarnings("unchecked")
    private <T> T castUnchecked(String name, Object value) {
        return (T) value;
    }

    private Object value(String name) {
        Objects.requireNonNull(name, "name");
        Integer index = indexes.get(name);
        if (index == null) {
            throw new IllegalArgumentException("未知参数：" + name);
        }
        return values[index];
    }

    private static Class<?> box(Class<?> type) {
        if (type == int.class) {
            return Integer.class;
        }
        if (type == long.class) {
            return Long.class;
        }
        if (type == float.class) {
            return Float.class;
        }
        if (type == double.class) {
            return Double.class;
        }
        if (type == boolean.class) {
            return Boolean.class;
        }
        if (type == byte.class) {
            return Byte.class;
        }
        if (type == short.class) {
            return Short.class;
        }
        if (type == char.class) {
            return Character.class;
        }
        return type;
    }
}
