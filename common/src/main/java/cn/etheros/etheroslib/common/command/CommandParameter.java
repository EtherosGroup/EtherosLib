package cn.etheros.etheroslib.common.command;

import java.util.Collection;
import java.util.Locale;
import java.util.Objects;

/**
 * 命令参数声明
 *
 * <p>按声明顺序解析；解析器由参数类型决定，补全可按 {@link #completer(TabProvider)} 覆盖
 *
 * <pre>{@code
 * CommandParameter.required("玩家", Player.class)
 * CommandParameter.optional("上限", Integer.class)
 * CommandParameter.required("模式", String.class).completer(ModeTabProvider.class)
 * }</pre>
 *
 * @author 西博卡兹
 * @since 2.0.0
 */
public final class CommandParameter {

    private final String name;
    private final boolean optional;
    private final ArgumentType<?> argumentType;
    private TabProvider completer;

    private CommandParameter(String name, boolean optional, ArgumentType<?> argumentType) {
        this.name = name;
        this.optional = optional;
        this.argumentType = argumentType;
    }

    /**
     * 必选参数，解析器由类型决定
     *
     * @param name 参数名，用于用法与错误提示；空串时取类型简名的小写
     * @param type 参数类型，须已在 {@link ArgumentTypes} 注册或为枚举
     */
    public static CommandParameter required(String name, Class<?> type) {
        return create(name, false, type);
    }

    /**
     * 可选参数，解析器由类型决定
     *
     * @param name 参数名，用于用法与错误提示；空串时取类型简名的小写
     * @param type 参数类型，须已在 {@link ArgumentTypes} 注册或为枚举；不能是基本类型
     */
    public static CommandParameter optional(String name, Class<?> type) {
        return create(name, true, type);
    }

    /**
     * 必选参数，使用自定义解析器
     *
     * @param name 参数名，不能为空
     * @param type 自定义解析器
     */
    public static CommandParameter required(String name, ArgumentType<?> type) {
        return create(name, false, type);
    }

    /**
     * 可选参数，使用自定义解析器
     *
     * @param name 参数名，不能为空
     * @param type 自定义解析器
     */
    public static CommandParameter optional(String name, ArgumentType<?> type) {
        return create(name, true, type);
    }

    private static CommandParameter create(String name, boolean optional, Class<?> type) {
        Objects.requireNonNull(type, "type");
        if (optional && type.isPrimitive()) {
            throw new IllegalStateException("可选参数不能是基本类型：" + type.getName());
        }
        ArgumentType<?> argumentType = ArgumentTypes.resolve(type);
        if (argumentType == null) {
            throw new IllegalStateException("不支持的参数类型：" + type.getName());
        }
        String resolved = name == null || name.trim().isEmpty()
                ? type.getSimpleName().toLowerCase(Locale.ROOT) : name;
        return new CommandParameter(resolved, optional, argumentType);
    }

    private static CommandParameter create(String name, boolean optional, ArgumentType<?> type) {
        Objects.requireNonNull(type, "type");
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalStateException("自定义参数类型必须声明参数名");
        }
        return new CommandParameter(name, optional, type);
    }

    static CommandParameter of(String name, boolean optional, ArgumentType<?> argumentType, TabProvider completer) {
        CommandParameter parameter = new CommandParameter(name, optional, argumentType);
        parameter.completer = completer;
        return parameter;
    }

    /**
     * 指定补全器实例
     */
    public CommandParameter completer(TabProvider completer) {
        this.completer = Objects.requireNonNull(completer, "completer");
        return this;
    }

    /**
     * 指定补全器类型，注册时实例化（需无参构造）
     */
    public CommandParameter completer(Class<? extends TabProvider> completer) {
        Objects.requireNonNull(completer, "completer");
        this.completer = instantiate(completer, name);
        return this;
    }

    private static TabProvider instantiate(Class<? extends TabProvider> type, String name) {
        try {
            return type.getDeclaredConstructor().newInstance();
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("补全器无法实例化：" + name + "：" + type.getName(), exception);
        }
    }

    String name() {
        return name;
    }

    boolean optional() {
        return optional;
    }

    ArgumentType<?> argumentType() {
        return argumentType;
    }

    TabProvider tabProvider() {
        return completer;
    }

    String usage() {
        return optional ? '[' + name + ']' : '<' + name + '>';
    }

    Object parse(String input) throws CommandArgumentException {
        Objects.requireNonNull(input, "input");
        return argumentType.parse(input);
    }

    Collection<String> suggest(TabContext context) {
        return completer == null ? argumentType.suggest(context) : completer.suggest(context);
    }
}
