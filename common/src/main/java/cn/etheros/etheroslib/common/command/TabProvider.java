package cn.etheros.etheroslib.common.command;

import java.util.Collection;

/**
 * 补全数据源
 *
 * <p>返回当前参数位置的候选值，前缀过滤由 {@link TabResult} 统一处理
 *
 * @author 西博卡兹
 * @since 1.1.0
 */
@FunctionalInterface
public interface TabProvider {

    Collection<String> suggest(TabContext context);
}
