package cn.etheros.etheroslib.common.command.provider;

import cn.etheros.etheroslib.common.command.TabContext;
import cn.etheros.etheroslib.common.command.TabProvider;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * 枚举常量候选，输出小写名称
 *
 * @author 西博卡兹
 * @since 1.1.0
 */
public final class EnumTabProvider<E extends Enum<E>> implements TabProvider {

    private final List<String> candidates;

    private EnumTabProvider(Class<E> type) {
        E[] constants = type.getEnumConstants();
        List<String> names = new ArrayList<String>(constants.length);
        for (E constant : constants) {
            names.add(constant.name().toLowerCase(Locale.ROOT));
        }
        this.candidates = Collections.unmodifiableList(names);
    }

    public static <E extends Enum<E>> EnumTabProvider<E> of(Class<E> type) {
        return new EnumTabProvider<E>(type);
    }

    @Override
    public Collection<String> suggest(TabContext context) {
        return candidates;
    }
}
