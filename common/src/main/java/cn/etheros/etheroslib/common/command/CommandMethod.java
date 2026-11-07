package cn.etheros.etheroslib.common.command;

import cn.etheros.etheroslib.common.command.annotation.Arg;
import cn.etheros.etheroslib.common.command.annotation.SubCommand;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

/**
 * 子命令方法
 *
 * <p>第一个形参不带 @Arg 时视为发送者或 CommandContext，其余形参必须标 @Arg
 * <p>发送者可声明为 CommandSender，也可声明为平台原始类型，由 raw() 注入
 *
 * @author 西博卡兹
 * @since 1.1.0
 */
final class CommandMethod {

    private final Method method;
    private final Object owner;
    private final List<ArgBinding> bindings;
    private final boolean contextParameter;
    private final Class<?> senderParameter;
    private final String permission;
    private final String usage;

    private CommandMethod(Method method, Object owner, List<ArgBinding> bindings,
                          boolean contextParameter, Class<?> senderParameter,
                          String permission, String usage) {
        this.method = method;
        this.owner = owner;
        this.bindings = Collections.unmodifiableList(bindings);
        this.contextParameter = contextParameter;
        this.senderParameter = senderParameter;
        this.permission = permission == null ? "" : permission;
        this.usage = usage == null ? "" : usage;
    }

    static CommandMethod parse(Object owner, Method method, SubCommand annotation) {
        method.setAccessible(true);
        List<ArgBinding> bindings = new ArrayList<ArgBinding>();
        boolean contextParameter = false;
        Class<?> senderParameter = null;
        Parameter[] parameters = method.getParameters();
        int index = 0;
        if (parameters.length > 0) {
            Parameter first = parameters[0];
            if (CommandContext.class.isAssignableFrom(first.getType())) {
                contextParameter = true;
                index = 1;
            } else if (first.getAnnotation(Arg.class) == null) {
                if (first.getType().isPrimitive()) {
                    throw new IllegalStateException("命令执行者参数不能是基本类型：" + method.getName());
                }
                senderParameter = first.getType();
                index = 1;
            }
        }
        for (; index < parameters.length; index++) {
            Parameter parameter = parameters[index];
            Arg arg = parameter.getAnnotation(Arg.class);
            if (arg == null) {
                throw new IllegalStateException("参数缺少 @Arg：" + method.getName());
            }
            bindings.add(ArgBinding.parse(parameter, arg, method));
        }
        return new CommandMethod(method, owner, bindings, contextParameter, senderParameter,
                annotation.permission(), annotation.usage());
    }

    String permission() {
        return permission;
    }

    String usage() {
        return usage;
    }

    Class<?> senderParameter() {
        return senderParameter;
    }

    int argumentCount() {
        return bindings.size();
    }

    int requiredCount() {
        int required = 0;
        for (ArgBinding binding : bindings) {
            if (!binding.optional()) {
                required++;
            }
        }
        return required;
    }

    Collection<String> suggest(int relative, TabContext context) {
        if (relative < 0 || relative >= bindings.size()) {
            return Collections.emptyList();
        }
        return bindings.get(relative).suggest(context);
    }

    String argumentUsage() {
        StringBuilder builder = new StringBuilder();
        for (ArgBinding binding : bindings) {
            if (builder.length() > 0) {
                builder.append(' ');
            }
            builder.append(binding.optional() ? '[' : '<').append(binding.name())
                    .append(binding.optional() ? ']' : '>');
        }
        return builder.toString();
    }

    void invoke(Object senderArgument, CommandContext context, String[] rawArgs) throws CommandArgumentException {
        Object[] values = buildArguments(senderArgument, context, rawArgs);
        try {
            method.invoke(owner, values);
        } catch (IllegalAccessException exception) {
            throw new IllegalStateException("命令方法不可访问：" + method.getName(), exception);
        } catch (InvocationTargetException exception) {
            Throwable cause = exception.getCause();
            if (cause instanceof RuntimeException) {
                throw (RuntimeException) cause;
            }
            if (cause instanceof Error) {
                throw (Error) cause;
            }
            throw new IllegalStateException(cause);
        }
    }

    private Object[] buildArguments(Object senderArgument, CommandContext context, String[] rawArgs)
            throws CommandArgumentException {
        Object[] values = new Object[method.getParameterCount()];
        int parameterIndex = 0;
        if (contextParameter) {
            values[parameterIndex++] = context;
        } else if (senderParameter != null) {
            values[parameterIndex++] = senderArgument;
        }
        for (int index = 0; index < bindings.size(); index++) {
            Object value = index < rawArgs.length ? bindings.get(index).parse(rawArgs[index]) : null;
            values[parameterIndex++] = value;
        }
        return values;
    }

    private static final class ArgBinding {

        private final String name;
        private final boolean optional;
        private final ArgumentType<?> argumentType;
        private final TabProvider completer;

        private ArgBinding(String name, boolean optional, ArgumentType<?> argumentType, TabProvider completer) {
            this.name = name;
            this.optional = optional;
            this.argumentType = argumentType;
            this.completer = completer;
        }

        static ArgBinding parse(Parameter parameter, Arg arg, Method method) {
            String name = arg.value().isEmpty()
                    ? parameter.getType().getSimpleName().toLowerCase(Locale.ROOT)
                    : arg.value();
            if (arg.optional() && parameter.getType().isPrimitive()) {
                throw new IllegalStateException("可选参数不能是基本类型：" + name + "（" + method.getName() + "）");
            }
            ArgumentType<?> argumentType = ArgumentTypes.resolve(parameter.getType());
            if (argumentType == null) {
                throw new IllegalStateException("不支持的参数类型：" + parameter.getType().getName()
                        + "（" + method.getName() + "）");
            }
            TabProvider completer = arg.completer() == TabProvider.class
                    ? null
                    : instantiate(arg.completer(), name);
            return new ArgBinding(name, arg.optional(), argumentType, completer);
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

        Object parse(String input) throws CommandArgumentException {
            Objects.requireNonNull(input, "input");
            try {
                return argumentType.parse(input);
            } catch (CommandArgumentException exception) {
                throw new CommandArgumentException(name + "：" + exception.getMessage());
            }
        }

        Collection<String> suggest(TabContext context) {
            return completer == null ? argumentType.suggest(context) : completer.suggest(context);
        }
    }
}
