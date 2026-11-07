package cn.etheros.etheroslib.common.command;

import java.util.Collection;
import java.util.Collections;

/**
 * 参数类型：解析字符串并按类型给出补全
 *
 * @author 西博卡兹
 * @since 1.1.0
 */
public interface ArgumentType<T> {

    T parse(String input) throws CommandArgumentException;

    default Collection<String> suggest(TabContext context) {
        return Collections.emptyList();
    }
}
