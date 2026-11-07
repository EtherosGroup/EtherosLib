package cn.etheros.etheroslib.common.command;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 参数类型注册表
 *
 * <p>内置 String、数值、boolean 与枚举；平台类型由各平台后端注册
 *
 * @author 西博卡兹
 * @since 1.1.0
 */
public final class ArgumentTypes {

    private static final Map<Class<?>, ArgumentType<?>> TYPES =
            new ConcurrentHashMap<>();

    static {
        register(String.class, new StringArgument());
        register(Integer.class, new IntegerArgument());
        register(int.class, new IntegerArgument());
        register(Long.class, new LongArgument());
        register(long.class, new LongArgument());
        register(Float.class, new FloatArgument());
        register(float.class, new FloatArgument());
        register(Double.class, new DoubleArgument());
        register(double.class, new DoubleArgument());
        register(Boolean.class, new BooleanArgument());
        register(boolean.class, new BooleanArgument());
    }

    private ArgumentTypes() {
    }

    public static <T> void register(Class<T> type, ArgumentType<T> argumentType) {
        TYPES.put(type, argumentType);
    }

    public static ArgumentType<?> resolve(Class<?> type) {
        ArgumentType<?> argumentType = TYPES.get(type);
        if (argumentType != null) {
            return argumentType;
        }
        if (type.isEnum()) {
            ArgumentType<?> created = new EnumArgument(type);
            ArgumentType<?> existing = TYPES.putIfAbsent(type, created);
            return existing == null ? created : existing;
        }
        return null;
    }

    private static final class StringArgument implements ArgumentType<String> {

        @Override
        public String parse(String input) {
            return input;
        }
    }

    private static final class IntegerArgument implements ArgumentType<Integer> {

        @Override
        public Integer parse(String input) throws CommandArgumentException {
            try {
                return Integer.valueOf(input);
            } catch (NumberFormatException exception) {
                throw new CommandArgumentException("不是整数");
            }
        }
    }

    private static final class LongArgument implements ArgumentType<Long> {

        @Override
        public Long parse(String input) throws CommandArgumentException {
            try {
                return Long.valueOf(input);
            } catch (NumberFormatException exception) {
                throw new CommandArgumentException("不是整数");
            }
        }
    }

    private static final class FloatArgument implements ArgumentType<Float> {

        @Override
        public Float parse(String input) throws CommandArgumentException {
            try {
                return Float.valueOf(input);
            } catch (NumberFormatException exception) {
                throw new CommandArgumentException("不是浮点数");
            }
        }
    }

    private static final class DoubleArgument implements ArgumentType<Double> {

        @Override
        public Double parse(String input) throws CommandArgumentException {
            try {
                return Double.valueOf(input);
            } catch (NumberFormatException exception) {
                throw new CommandArgumentException("不是浮点数");
            }
        }
    }

    private static final class BooleanArgument implements ArgumentType<Boolean> {

        private static final List<String> VALUES =
                Collections.unmodifiableList(Arrays.asList("true", "false"));

        @Override
        public Boolean parse(String input) throws CommandArgumentException {
            if ("true".equalsIgnoreCase(input)) {
                return Boolean.TRUE;
            }
            if ("false".equalsIgnoreCase(input)) {
                return Boolean.FALSE;
            }
            throw new CommandArgumentException("不是布尔值");
        }

        @Override
        public Collection<String> suggest(TabContext context) {
            return VALUES;
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static final class EnumArgument implements ArgumentType<Enum> {

        private final Class<? extends Enum> type;
        private final List<String> names;

        private EnumArgument(Class<?> type) {
            this.type = (Class<? extends Enum>) type;
            List<String> values = new ArrayList<String>();
            for (Enum constant : this.type.getEnumConstants()) {
                values.add(constant.name().toLowerCase(Locale.ROOT));
            }
            this.names = Collections.unmodifiableList(values);
        }

        @Override
        public Enum parse(String input) throws CommandArgumentException {
            for (Enum constant : type.getEnumConstants()) {
                if (constant.name().equalsIgnoreCase(input)) {
                    return constant;
                }
            }
            throw new CommandArgumentException("枚举值不存在");
        }

        @Override
        public Collection<String> suggest(TabContext context) {
            return names;
        }
    }
}
